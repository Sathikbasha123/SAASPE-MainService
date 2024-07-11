package saaspe.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import saaspe.constant.Constant;
import saaspe.constant.ContractType;
import saaspe.constant.ProductType;
import saaspe.currency.entity.ApiKeys;
import saaspe.currency.entity.CurrencyEntity;
import saaspe.currency.repository.ApiKeysRepository;
import saaspe.currency.repository.CurrencyRepository;
import saaspe.entity.ApplicationContractDetails;
import saaspe.entity.ApplicationDetails;
import saaspe.entity.ApplicationLicenseDetails;
import saaspe.entity.ApplicationLogoEntity;
import saaspe.entity.ApplicationOwnerDetails;
import saaspe.entity.ApplicationSubscriptionDetails;
import saaspe.entity.Applications;
import saaspe.entity.ContractOnboardingDetails;
import saaspe.entity.PaymentDetails;
import saaspe.entity.SequenceGenerator;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.ApplicationContractDetailsResponse;
import saaspe.model.ApplicationContractDetailsUpdateRequest;
import saaspe.model.CommonResponse;
import saaspe.model.ContractDetailsOverviewResponse;
import saaspe.model.ContractLicenseDetailResponse;
import saaspe.model.ContractOnboadingRequesListView;
import saaspe.model.ContractOnboardingRequest;
import saaspe.model.ContractOnboardingResponse;
import saaspe.model.ContractReviewerDetails;
import saaspe.model.ContractsListResponse;
import saaspe.model.CurrencyConverterResponse;
import saaspe.model.DeptOnboardingWorkFlowRequest;
import saaspe.model.NewContractOnboardingResposne;
import saaspe.model.Products;
import saaspe.model.Response;
import saaspe.model.SupportDocumentsResponse;
import saaspe.repository.ApplicationContractDetailsRepository;
import saaspe.repository.ApplicationDetailsRepository;
import saaspe.repository.ApplicationLicenseDetailsRepository;
import saaspe.repository.ApplicationLogoRepository;
import saaspe.repository.ApplicationOwnerRepository;
import saaspe.repository.ApplicationSubscriptionDetailsRepository;
import saaspe.repository.ApplicationsRepository;
import saaspe.repository.ContractsOnboardingRespository;
import saaspe.repository.PaymentDetailsRepository;
import saaspe.repository.SequenceGeneratorRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.service.ContractService;
import saaspe.utils.CommonUtil;

@Service
public class ContractServiceImpl implements ContractService {

	@Autowired
	private ApplicationContractDetailsRepository applicationContractDetailsRepository;

	@Autowired
	private SequenceGeneratorRepository sequenceGeneratorRepository;

	@Autowired
	private ApplicationLogoRepository applicationLogoRepository;

	@Autowired
	private ApplicationLicenseDetailsRepository applicationLicenseDetailsRepository;

	@Autowired
	private PaymentDetailsRepository paymentDetailsRepository;

	@Autowired
	private ApplicationSubscriptionDetailsRepository applicationSubscriptionDetailsRepository;

	@Autowired
	private ContractsOnboardingRespository contractsOnboardingRespository;

	@Autowired
	private ApplicationDetailsRepository applicationDetailsRepository;

	@Autowired
	private ApplicationOwnerRepository applicationOwnerRepository;

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	private CloudBlobClient cloudBlobClient;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private ApiKeysRepository apiKeysRepository;

	@Autowired
	private Configuration config;

	@Autowired
	private ApplicationsRepository applicationRepository;

	@Value("${azure.storage.container.name}")
	private String supportingDocsUri;

	@Value("${azure.storage.container.supporting.name}")
	private String supportingDocsPath;

	@Value("${redirecturl.path}")
	private String redirectUrl;

	@Value("${sendgrid.domain.name}")
	private String mailDomainName;

	@Value("${sendgrid.domain.sendername}")
	private String senderName;

	@Value("${contract.service.url}")
	private String contractUrl;

	private static Calendar dateToCalendar(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	@Override
	@Transactional
	public CommonResponse modifyApplicationContractDetails(String contractId,
			ApplicationContractDetailsUpdateRequest updateRequest) throws DataValidationException {
		if (contractId != null && applicationContractDetailsRepository.findByContractId(contractId) != null) {
			ApplicationContractDetails details = applicationContractDetailsRepository.findByContractId(contractId);
			details.setReminderDate(updateRequest.getReminderDate());
			applicationContractDetailsRepository.save(details);
			List<Applications> applications = applicationRepository.findByContractId(contractId);
			if (!applications.isEmpty()) {
				for (Applications application : applications) {
					application.setReminderDate(updateRequest.getReminderDate());
					applicationRepository.save(application);
				}
			}
		} else {
			throw new DataValidationException(Constant.APPLICATION_CONTACT_NOT_FOUND, "404", HttpStatus.NOT_FOUND);
		}
		return new CommonResponse(HttpStatus.OK,
				new Response("ContractModifyResponse", Constant.APPLICATION_DETAILS_UPDATED),
				"Details modify successfully");
	}

	@Override
	public CommonResponse getContractsListView() throws DataValidationException {
		List<ContractsListResponse> list = new ArrayList<>();
		List<ApplicationContractDetails> applicationContractDetails = applicationContractDetailsRepository.findAll();
		if (applicationContractDetails.isEmpty()) {
			throw new DataValidationException("No Contract Details to show", null, null);
		}
		for (ApplicationContractDetails app : applicationContractDetails) {
			ContractsListResponse contractsListResponse = new ContractsListResponse();
			contractsListResponse.setContractId(app.getContractId());
			contractsListResponse.setContractName(app.getContractName());
			contractsListResponse.setApplicationName(app.getApplicationId().getApplicationName());
			contractsListResponse.setApplicationId(app.getApplicationId().getApplicationId());
			contractsListResponse.setApplicationLogo(app.getApplicationId().getLogoUrl());
			contractsListResponse.setContractStatus(app.getContractStatus());
			contractsListResponse.setContractType(app.getContractType());
			ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
					.findByApplicationName(app.getApplicationId().getApplicationName());
			contractsListResponse.setProviderName(applicationLogoEntity.getProviderId().getProviderName());
			contractsListResponse.setProviderLogo(applicationLogoEntity.getProviderId().getLogoUrl());
			ApplicationDetails depDetails = app.getApplicationId();
			contractsListResponse.setDepartmentName(depDetails.getOwnerDepartment());
			BigDecimal totalCost = BigDecimal.valueOf(0);
			BigDecimal adminCost = BigDecimal.valueOf(0);
			Integer quantity = 0;
			for (ApplicationLicenseDetails licenseDetails : app.getLicenseDetails()) {
				quantity = quantity + licenseDetails.getQuantity();
				totalCost = totalCost.add(licenseDetails.getTotalCost(), MathContext.DECIMAL32);
				adminCost = adminCost.add(licenseDetails.getConvertedCost(), MathContext.DECIMAL32);
			}
			contractsListResponse.setProductQuantity(quantity);
			contractsListResponse.setAdminCost(adminCost);
			contractsListResponse.setTotalCost(totalCost);
			contractsListResponse.setCurrencyCode(app.getContractCurrency());
			contractsListResponse.setContractStartDate(app.getContractStartDate());
			contractsListResponse.setContractEndDate(app.getContractEndDate());
			list.add(contractsListResponse);
		}
		List<ContractsListResponse> activeContractList = list.stream()
				.filter(p -> p.getContractStatus().equalsIgnoreCase("active")).collect(Collectors.toList());
		return new CommonResponse(HttpStatus.OK, new Response("contractsListResponse", activeContractList),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getApplicationContractDetailView(String contractId, String category)
			throws DataValidationException, URISyntaxException, StorageException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		ContractDetailsOverviewResponse contractDetailsOverviewResponse = new ContractDetailsOverviewResponse();
		ApplicationContractDetails applicationContractDetails = applicationContractDetailsRepository
				.getByContractId(contractId);
		if (applicationContractDetails != null && StringUtils.isEmpty(category)) {
			ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
					.findByApplicationName(applicationContractDetails.getApplicationId().getApplicationName());
			contractDetailsOverviewResponse
					.setApplicaitonName(applicationContractDetails.getApplicationId().getApplicationName());
			contractDetailsOverviewResponse
					.setSubscriptionId(applicationContractDetails.getSubscriptionDetails().getSubscriptionNumber());
			contractDetailsOverviewResponse.setContractName(applicationContractDetails.getContractName());
			contractDetailsOverviewResponse.setProviderName(applicationLogoEntity.getProviderId().getProviderName());
			contractDetailsOverviewResponse.setContractStartDate(applicationContractDetails.getContractStartDate());
			contractDetailsOverviewResponse.setContractEndDate(applicationContractDetails.getContractEndDate());
			if (Boolean.TRUE.equals(applicationContractDetails.getAutoRenew())) {
				contractDetailsOverviewResponse.setUpcomingRenewalDate(applicationContractDetails.getRenewalDate());
			} else {
				contractDetailsOverviewResponse.setUpcomingRenewalDate(applicationContractDetails.getContractEndDate());
			}
			contractDetailsOverviewResponse.setReminderDate(applicationContractDetails.getReminderDate());
			List<ApplicationLicenseDetails> applicationLicenseDetails = applicationLicenseDetailsRepository
					.getdByContractId(contractId);
			BigDecimal totalCost = BigDecimal.valueOf(0);
			BigDecimal adminCost = BigDecimal.valueOf(0);
			Integer quantity = 0;
			Integer mapped = 0;
			Integer unMapped = 0;
			for (ApplicationLicenseDetails licenseDetails : applicationLicenseDetails) {
				quantity = quantity + licenseDetails.getQuantity();
				mapped = mapped + licenseDetails.getLicenseMapped();
				unMapped = unMapped + licenseDetails.getLicenseUnMapped();
				totalCost = totalCost.add(licenseDetails.getTotalCost(), MathContext.DECIMAL32);
				adminCost = adminCost.add(licenseDetails.getConvertedCost(), MathContext.DECIMAL32);
			}
			contractDetailsOverviewResponse.setLicenseMapped(mapped);
			contractDetailsOverviewResponse.setLicenseUnMapped(unMapped);
			contractDetailsOverviewResponse.setTotalLicenses(quantity);
			contractDetailsOverviewResponse.setTotalCost(totalCost);
			contractDetailsOverviewResponse.setAdminCost(adminCost);
			contractDetailsOverviewResponse.setContractType(applicationContractDetails.getContractType());
			contractDetailsOverviewResponse
					.setContractStatus(Boolean.parseBoolean(applicationContractDetails.getContractStatus()));
			contractDetailsOverviewResponse.setBillingFrequency(applicationContractDetails.getBillingFrequency());
			contractDetailsOverviewResponse
					.setAutoRenewalCancellation(applicationContractDetails.getAutoRenewalCancellation());

			PaymentDetails paymentDetails = paymentDetailsRepository.findByContractId(contractId.trim());
			Boolean status = applicationContractDetails.getAutoRenew();
			String wallet = Constant.WALLET;
			if (applicationContractDetails.getContractPaymentMethod() != null
					&& (!applicationContractDetails.getContractPaymentMethod().isEmpty())) {
				if (Boolean.TRUE.equals(status)
						&& !applicationContractDetails.getContractPaymentMethod().equalsIgnoreCase(wallet)) {
					contractDetailsOverviewResponse.setAutoRenewal(status);
					contractDetailsOverviewResponse
							.setPaymentMethod(applicationContractDetails.getContractPaymentMethod());
					contractDetailsOverviewResponse.setCardHolderName(paymentDetails.getCardholderName());
					if (paymentDetails.getCardNumber() != null) {
						byte[] decodedBytes = Base64.getDecoder().decode(paymentDetails.getCardNumber());
						String decodedString = new String(decodedBytes);
						String trimmedCardNo = decodedString.replaceAll("\\s", "").trim();
						if (trimmedCardNo.length() >= 16) {
							String last4Digits = trimmedCardNo.substring(trimmedCardNo.length() - 4);
							String maskedDigits = new String(new char[trimmedCardNo.length() - 4]).replace('\0', '*');
							String maskedCardNo = maskedDigits + last4Digits;
							contractDetailsOverviewResponse.setCardNumber(maskedCardNo);
						}
					}
					contractDetailsOverviewResponse.setValidThrough(paymentDetails.getValidThrough());
				}
				if (Boolean.TRUE.equals(status)
						&& applicationContractDetails.getContractPaymentMethod().equalsIgnoreCase(wallet)) {
					contractDetailsOverviewResponse
							.setPaymentMethod(applicationContractDetails.getContractPaymentMethod());
					contractDetailsOverviewResponse.setWalletName(paymentDetails.getWalletName());
				}

			}
			contractDetailsOverviewResponse.setAutoRenewal(status);
			contractDetailsOverviewResponse.setCurrencyCode(applicationContractDetails.getContractCurrency());
			List<ContractLicenseDetailResponse> list = new ArrayList<>();
			for (ApplicationLicenseDetails licenseDetails : applicationContractDetails.getLicenseDetails()) {
				BigDecimal costPerTerm = BigDecimal.valueOf(0);
				ContractLicenseDetailResponse contractLicenseDetailResponse = new ContractLicenseDetailResponse();
				contractLicenseDetailResponse.setContractId(contractId);
				contractLicenseDetailResponse.setUnitPrice(licenseDetails.getUnitPrice());
				costPerTerm = costPerTerm.add(licenseDetails.getTotalCost(), MathContext.DECIMAL32);
				BigDecimal divide = licenseDetails.getTotalCost().divide(licenseDetails.getUnitPrice(), 2,
						RoundingMode.FLOOR);
				BigDecimal convertedPerLicenseCost = licenseDetails.getConvertedCost().divide(divide, 2,
						RoundingMode.FLOOR);
				contractLicenseDetailResponse.setUnitPrice(convertedPerLicenseCost);
				contractLicenseDetailResponse.setTotalCost(costPerTerm);
				contractLicenseDetailResponse.setAdminCost(licenseDetails.getConvertedCost());
				contractLicenseDetailResponse.setUnitPriceType(licenseDetails.getUnitPriceType());
				contractLicenseDetailResponse.setProductName(licenseDetails.getProductName());
				contractLicenseDetailResponse.setQuantity(licenseDetails.getQuantity());
				contractLicenseDetailResponse.setProductType(licenseDetails.getProductCategory());
				list.add(contractLicenseDetailResponse);
			}
			contractDetailsOverviewResponse.setProducts(list);
			response.setData(contractDetailsOverviewResponse);
		}
		if (!StringUtils.isEmpty(category) && category.equalsIgnoreCase("licenses")) {
			if (applicationContractDetails == null) {
				throw new DataValidationException("No contracts", null, null);
			}
			List<ApplicationLicenseDetails> applicationLicenseDetails = applicationLicenseDetailsRepository
					.getdByContractId(applicationContractDetails.getContractId());
			if (applicationLicenseDetails == null) {
				throw new DataValidationException("No licenses mapped to this contract", null, null);
			}
			List<ApplicationContractDetailsResponse> listApp = new ArrayList<>();
			for (ApplicationLicenseDetails appLicense : applicationLicenseDetails) {
				ApplicationContractDetailsResponse contractDetailsLicensesResponse = new ApplicationContractDetailsResponse();
				contractDetailsLicensesResponse.setLicenseId(appLicense.getLicenseId());
				contractDetailsLicensesResponse.setProductName(appLicense.getProductName());
				contractDetailsLicensesResponse.setQuantity(appLicense.getQuantity());
				contractDetailsLicensesResponse.setUnitPriceType(appLicense.getUnitPriceType());
				BigDecimal divide = appLicense.getTotalCost().divide(appLicense.getUnitPrice(), 2, RoundingMode.FLOOR);
				BigDecimal convertedPerLicenseCost = appLicense.getConvertedCost().divide(divide, 2,
						RoundingMode.FLOOR);
				contractDetailsLicensesResponse.setUnitPrice(convertedPerLicenseCost);
				contractDetailsLicensesResponse.setTotalCost(appLicense.getTotalCost());
				contractDetailsLicensesResponse.setAdminCost(appLicense.getConvertedCost());
				contractDetailsLicensesResponse.setTotalCost(appLicense.getTotalCost());
				contractDetailsLicensesResponse.setMappedLicenses(appLicense.getLicenseMapped());
				contractDetailsLicensesResponse.setUnmappedLicenses(appLicense.getLicenseUnMapped());
				contractDetailsLicensesResponse.setCurrencyCode(appLicense.getCurrency());
				listApp.add(contractDetailsLicensesResponse);
			}
			response.setData(listApp);
		}

		if (!StringUtils.isEmpty(category) && category.equalsIgnoreCase("documents")) {
			ApplicationContractDetailsResponse contractDetailresponce = new ApplicationContractDetailsResponse();
			contractDetailresponce.setFileUrl(
					getBlobURII(supportingDocsPath + applicationContractDetails.getApplicationId().getApplicationId()));
			response.setData(contractDetailresponce);
		}

		response.setAction("contractDetailsOverviewResponse");
		commonResponse.setMessage(Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
		commonResponse.setResponse(response);
		commonResponse.setStatus(HttpStatus.OK);
		return commonResponse;
	}

	private List<URI> getBlobURII(String fileName) throws URISyntaxException, StorageException {
		List<URI> uris = new ArrayList<>();
		try {
			CloudBlobContainer container = cloudBlobClient.getContainerReference(supportingDocsUri);
			for (ListBlobItem blobItem : container.listBlobs(fileName + "/")) {
				uris.add(blobItem.getUri());
			}
		} catch (URISyntaxException e) {
			throw new URISyntaxException("URL Error", "Unable to Connect to Azure, Please Check URL in properties");
		} catch (StorageException e) {
			throw new StorageException(Constant.INSUFFICIENT_STORAGE, Constant.CLIENT_ERROR, e);
		}
		return uris;
	}

	@Override
	@Transactional
	public CommonResponse upCommingContractRenewalReminderEmail()
			throws IOException, TemplateException, DataValidationException, MessagingException, InterruptedException {
		List<ApplicationContractDetails> applicationContractDetails = applicationContractDetailsRepository
				.getContactDetails();
		if (applicationContractDetails == null) {
			throw new DataValidationException("There is no contracts for renewal-reminder", null, null);
		}
		for (ApplicationContractDetails contractDetails : applicationContractDetails) {
			if (contractDetails.getRenewalDate() != null) {
				long timeDifference = contractDetails.getContractEndDate().getTime() - new Date().getTime();
				long daysDifference = TimeUnit.MILLISECONDS.toDays(timeDifference) % 365;
				if (daysDifference <= 45 && daysDifference >= 0) {
					ApplicationSubscriptionDetails applicationSubscriptionDetails = applicationSubscriptionDetailsRepository
							.findByApplicationId(contractDetails.getApplicationId().getApplicationId());
					ApplicationOwnerDetails applicationOwnerdetails = applicationOwnerRepository
							.findByAppId(contractDetails.getApplicationId().getApplicationId());
					if (applicationSubscriptionDetails == null) {
						throw new DataValidationException("No subscription details is available for this application",
								null, null);
					}
					if (applicationOwnerdetails.getOwnerEmail() != null) {
						sendReminderEmail(contractDetails.getContractName(), applicationOwnerdetails.getOwnerEmail(),
								contractDetails.getApplicationId().getApplicationName(), daysDifference,
								contractDetails.getContractEndDate(), contractDetails.getContractId());
						Thread.sleep(30000);
					}
				}
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("ContractRenewalReminderResponse", new ArrayList<>()),
				"Reminder mail had been sent successfully");
	}

	private void sendReminderEmail(String contractName, String emailAddress, String applicationName,
			long daysDifference, Date renewalDate, String contractId)
			throws IOException, TemplateException, MessagingException {
		String toAddress = emailAddress;
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String url = "{{host}}?search={{ContactId}}";
		url = url.replace("{{host}}", redirectUrl);
		url = url.replace("{{ContactId}}", contractId);
		String subject = Constant.CONTRACT_EMAIL_SUBJECT;
		Map<String, Object> model = new HashMap<>();
		Template t = config.getTemplate("renewal-reminder.html");
		String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
		content = content.replace("{{url}}", url);
		content = content.replace("{{application}}", applicationName);
		content = content.replace("{{days}}", String.valueOf(daysDifference));
		content = content.replace("{{contract}}", contractName);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		content = content.replace("{{renewalDate}}", String.valueOf(formatter.format(renewalDate)));
		try {
			helper.setFrom(mailDomainName, senderName);
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setText(content, true);
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedEncodingException(e.getMessage());
		} catch (MessagingException e) {
			throw new MessagingException(e.getMessage());
		}
		mailSender.send(message);
	}

	@Override
	@Transactional
	public CommonResponse addApplicationContract(ContractOnboardingRequest contractOnboardingRequest,
			String applicationId, UserLoginDetails profile) throws DataValidationException, ParseException {
		NewContractOnboardingResposne onboardingResponse = new NewContractOnboardingResposne();
		ContractOnboardingDetails contractOnboardingDetails = new ContractOnboardingDetails();
		ApplicationDetails applicationDetails = applicationDetailsRepository.findByApplicationId(applicationId.trim());
		if (applicationDetails == null) {
			throw new DataValidationException("Application not found", "404", HttpStatus.CONFLICT);
		}
		if (applicationContractDetailsRepository
				.findByContractName(contractOnboardingRequest.getContractName()) != null) {
			throw new DataValidationException("Contract name already exist", "404", HttpStatus.CONFLICT);
		}
		ContractOnboardingDetails list = contractsOnboardingRespository
				.findByContractName(contractOnboardingRequest.getContractName());
		List<ContractOnboardingDetails> apps = new ArrayList<>();
		ApplicationContractDetails deletedApplication = null;
		if (list != null) {
			List<ContractOnboardingDetails> loop = new ArrayList<>();
			loop.add(list);
			apps = loop.stream().filter(p -> p.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW))
					.collect(Collectors.toList());
			deletedApplication = applicationContractDetailsRepository
					.getDeletedContractsByContractName(contractOnboardingRequest.getContractName());
		}
		try {
			applicatoinContractValidator(contractOnboardingRequest);
		} catch (DataValidationException e) {
			throw new DataValidationException(e.getMessage(), null, null);
		}
		if (apps.isEmpty() && deletedApplication == null) {
			String name = "REQ_CONT_0";
			Integer sequence = sequenceGeneratorRepository.getContractRequestNumberSequence();
			name = name.concat(sequence.toString());
			SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
			updateSequence.setContractRequestSequenceId(++sequence);
			sequenceGeneratorRepository.save(updateSequence);
			ObjectMapper obj = new ObjectMapper();
			String objToString;
			try {
				objToString = obj.writeValueAsString(contractOnboardingRequest);
				contractOnboardingDetails.setContractOnboardingRequest(objToString);
			} catch (JsonProcessingException e) {
				throw new DataValidationException(e.getLocalizedMessage(), null, HttpStatus.CONFLICT);
			}
			contractOnboardingDetails.setApprovedRejected(Constant.REVIEW);
			contractOnboardingDetails.setRequestNumber(name);
			contractOnboardingDetails.setCreatedBy(profile.getEmailAddress());
			contractOnboardingDetails.setCreatedOn(new Date());
			contractOnboardingDetails.setOnboardDate(new Date());
			contractOnboardingDetails.setApplicationName(applicationDetails.getApplicationName());
			contractOnboardingDetails.setApplicationId(applicationDetails.getApplicationId());
			contractOnboardingDetails.setOnboardedByUserEmail(profile.getEmailAddress());
			contractOnboardingDetails.setOnboardingStatus(Constant.PENDING_WITH_REVIEWER);
			contractOnboardingDetails.setContractName(contractOnboardingRequest.getContractName());
			contractOnboardingDetails.setWorkGroup(Constant.REVIEWER);
			contractOnboardingDetails.setWorkGroupUserEmail(profile.getEmailAddress());
			onboardingResponse.setRequestId(name);
			contractsOnboardingRespository.save(contractOnboardingDetails);
		} else {
			throw new DataValidationException("Contract already In review or Approved", "404", HttpStatus.CONFLICT);
		}
		return new CommonResponse(HttpStatus.CREATED, new Response("contactOnboardingResponse", onboardingResponse),
				"Contract onboarding details submitted successfully");
	}

	@Override
	public CommonResponse getContractsByApplicationId(String applicationId) throws DataValidationException {
		List<ContractsListResponse> list = new ArrayList<>();
		List<ApplicationContractDetails> applicationContractDetails = applicationContractDetailsRepository
				.getContractsByApplicationId(applicationId);
		if (applicationContractDetails == null) {
			throw new DataValidationException("No Contract Details to show", null, null);
		}
		for (ApplicationContractDetails contract : applicationContractDetails) {
			ContractsListResponse contractsListResponse = new ContractsListResponse();
			contractsListResponse.setContractId(contract.getContractId());
			contractsListResponse.setContractName(contract.getContractName());
			contractsListResponse.setApplicationName(contract.getApplicationId().getApplicationName());
			contractsListResponse.setApplicationId(contract.getApplicationId().getApplicationId());
			contractsListResponse.setApplicationLogo(contract.getApplicationId().getLogoUrl());
			ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
					.findByApplicationName(contract.getApplicationId().getApplicationName());
			contractsListResponse.setProviderName(applicationLogoEntity.getProviderId().getProviderName());
			contractsListResponse.setProviderLogo(applicationLogoEntity.getProviderId().getLogoUrl());

			ApplicationDetails depDetails = contract.getApplicationId();
			contractsListResponse.setDepartmentName(depDetails.getOwnerDepartment());
			contractsListResponse.setAutoRenewal(contract.getAutoRenew());
			contractsListResponse.setAutoRenewalCancellation(contract.getRenewalTerm());
			contractsListResponse.setBillingFrequency(contract.getContractPaymentTerm());
			contractsListResponse.setContractEndDate(contract.getContractEndDate());
			contractsListResponse.setContractStartDate(contract.getContractStartDate());
			contractsListResponse.setContractType(contract.getContractType());
			contractsListResponse.setContractStatus(contract.getContractStatus());
			contractsListResponse.setUpcomingRenewalDate(contract.getRenewalDate());
			contractsListResponse.setBillingFrequency(contract.getContractPaymentTerm());
			contractsListResponse.setPaymentMethod(contract.getContractPaymentMethod());
			List<PaymentDetails> paymentDetails = paymentDetailsRepository.findByApplicationId(applicationId);
			if (!paymentDetails.isEmpty()) {
				for (PaymentDetails payment : paymentDetails) {
					if (payment.getCardNumber() != null) {
						contractsListResponse.setCardHolderName(payment.getCardholderName());
						byte[] decodeData = Base64.getDecoder().decode(payment.getCardNumber());
						contractsListResponse.setCardNumber(new String(decodeData, StandardCharsets.UTF_8));
						contractsListResponse.setValidThrough(payment.getValidThrough());
						contractsListResponse.setWalletName(payment.getWalletName());
					}
				}
			}
			BigDecimal totalCost = BigDecimal.valueOf(0);
			BigDecimal adminCost = BigDecimal.valueOf(0);
			Integer quantity = 0;
			List<Products> produtsList = new ArrayList<>();
			for (ApplicationLicenseDetails licenseDetails : contract.getLicenseDetails()) {
				BigDecimal divide = licenseDetails.getTotalCost().divide(licenseDetails.getUnitPrice(), 2,
						RoundingMode.FLOOR);
				BigDecimal convertedPerLicenseCost = licenseDetails.getConvertedCost().divide(divide, 2,
						RoundingMode.FLOOR);
				Products product = new Products();
				product.setCurrencyCode(licenseDetails.getCurrency());
				product.setProductName(licenseDetails.getProductName());
				product.setProductType(licenseDetails.getProductCategory());
				product.setQuantity(licenseDetails.getQuantity());
				product.setUnitPrice(licenseDetails.getUnitPrice());
				product.setAdminUnitPrice(convertedPerLicenseCost);
				product.setUnitPriceType(licenseDetails.getUnitPriceType());
				quantity = quantity + licenseDetails.getQuantity();
				totalCost = totalCost.add(licenseDetails.getTotalCost(), MathContext.DECIMAL32);
				product.setTotalCost(licenseDetails.getTotalCost());
				product.setAdminCost(licenseDetails.getConvertedCost());
				adminCost = adminCost.add(licenseDetails.getConvertedCost(), MathContext.DECIMAL32);
				produtsList.add(product);
			}
			contractsListResponse.setProductQuantity(quantity);
			contractsListResponse.setTotalCost(totalCost);
			contractsListResponse.setCurrencyCode(contract.getContractCurrency());
			contractsListResponse.setAdminCost(adminCost);
			contractsListResponse.setProducts(produtsList);
			list.add(contractsListResponse);
		}
		return new CommonResponse(HttpStatus.OK, new Response("contractsListResponse", list),
				Constant.DATA_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse contractReviewerApproverListView(UserLoginDetails profile) {
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
			List<ContractOnboadingRequesListView> listOfDept = getListOfContracts(Constant.REVIEWER, Constant.REVIEW);
			response.setData(listOfDept);
			response.setAction(Constant.CONTRACT_ONBOARDING_LIST_VIEW_RESPONSE);
			commonResponse.setStatus(HttpStatus.OK);
			commonResponse.setMessage(Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
			commonResponse.setResponse(response);
			return commonResponse;
		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER)) {
			List<ContractOnboadingRequesListView> listOfDept = getListOfContracts(Constant.APPROVER, Constant.REVIEW);
			response.setData(listOfDept);
			response.setAction(Constant.CONTRACT_ONBOARDING_LIST_VIEW_RESPONSE);
			commonResponse.setStatus(HttpStatus.OK);
			commonResponse.setMessage(Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
			commonResponse.setResponse(response);
			return commonResponse;
		}
		List<ContractOnboardingDetails> contractOnbaording = contractsOnboardingRespository.findAllSuperAdminListView();
		List<ContractOnboadingRequesListView> list = new ArrayList<>();
		for (ContractOnboardingDetails contOnboarding : contractOnbaording) {
			ContractOnboadingRequesListView listViewResponse = new ContractOnboadingRequesListView();
			ApplicationDetails applicationDetails = applicationDetailsRepository
					.findByApplicationId(contOnboarding.getApplicationId());
			listViewResponse.setDepartmentName(applicationDetails.getOwnerDepartment());
			listViewResponse.setContractName(contOnboarding.getContractName());
			listViewResponse.setApplicationName(contOnboarding.getApplicationName());
			listViewResponse.setOnboardedByEmail(contOnboarding.getOnboardedByUserEmail());
			listViewResponse.setRequestId(contOnboarding.getRequestNumber());
			ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
					.findByApplicationName(applicationDetails.getApplicationName());
			listViewResponse.setApplicationLogo(applicationLogoEntity.getLogoUrl());
			listViewResponse.setChildRequestId(contOnboarding.getChildRequestNumber());
			listViewResponse.setOnboardingStatus(contOnboarding.getOnboardingStatus());
			if (contOnboarding.getChildRequestNumber() != null) {
				listViewResponse.setChildRequestId(contOnboarding.getChildRequestNumber());
			}
			list.add(listViewResponse);
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.CONTRACT_ONBOARDING_LIST_VIEW_RESPONSE, list),
				Constant.DATA_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	@Transactional
	public CommonResponse contractOnboardReview(String requestId, UserLoginDetails profile,
			DeptOnboardingWorkFlowRequest onboardingWorkFlowRequest)
			throws DataValidationException, TemplateException, java.text.ParseException, IOException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		if (requestId == null) {
			response.setData("Check Parameters");
			response.setAction("Onboarding Work flow Action Response");
			commonResponse.setMessage("Wrong Param or Null values in param");
			commonResponse.setResponse(response);
			commonResponse.setStatus(HttpStatus.CONFLICT);
			return commonResponse;
		}
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)
				|| userDetails.getUserRole().equalsIgnoreCase("super_admin")) {
			if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
				if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
					if (requestId != null) {
						ContractOnboardingDetails parentRequest = contractsOnboardingRespository
								.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
						if (parentRequest == null) {
							throw new DataValidationException(Constant.VALID_VALUE, requestId, HttpStatus.CONFLICT);
						}
						ContractOnboardingDetails contractOnboarding = new ContractOnboardingDetails();
						contractOnboarding.setWorkGroup(Constant.APPROVER);
						contractOnboarding.setApprovedRejected(Constant.REVIEW);
						contractOnboarding.setCreatedOn(new Date());
						contractOnboarding.setCreatedBy(parentRequest.getCreatedBy());
						contractOnboarding.setOpID(Constant.SAASPE);
						contractOnboarding.setComments(onboardingWorkFlowRequest.getComments());
						contractOnboarding.setBuID(Constant.BUID);
						contractOnboarding.setOnboardingStatus(Constant.PENDING_WITH_APPROVER);
						contractOnboarding.setWorkGroupUserEmail(profile.getEmailAddress());
						contractOnboarding.setUpdatedOn(new Date());
						contractOnboarding.setComments(onboardingWorkFlowRequest.getComments());
						contractOnboarding.setRequestNumber(parentRequest.getRequestNumber());
						contractOnboarding.setContractOnboardingRequest(parentRequest.getContractOnboardingRequest());
						contractOnboarding.setContractName(parentRequest.getContractName());
						contractOnboarding.setOnboardDate(new Date());
						contractOnboarding.setApplicationName(parentRequest.getApplicationName());
						contractOnboarding.setOnboardedByUserEmail(parentRequest.getOnboardedByUserEmail());
						contractOnboarding.setApplicationId(parentRequest.getApplicationId());

						parentRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						parentRequest.setOnboardingStatus("Approved By Reviewer");
						parentRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						parentRequest.setEndDate(new Date());
						parentRequest.setComments(onboardingWorkFlowRequest.getComments());
						parentRequest.setUpdatedBy(profile.getFirstName());
						parentRequest.setUpdatedOn(new Date());
						parentRequest.setOnboardDate(parentRequest.getOnboardDate());
						parentRequest.setContractName(parentRequest.getContractName());

						contractsOnboardingRespository.save(contractOnboarding);
						contractsOnboardingRespository.save(parentRequest);
					}
					return reviewSuccessResponse();
				} else {

					if (requestId != null) {
						ContractOnboardingDetails rejectRequest = contractsOnboardingRespository
								.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
						if (rejectRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null, HttpStatus.CONFLICT);
						}
						rejectRequest.setApprovedRejected(Constant.REJECTED);
						if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
							rejectRequest.setOnboardingStatus("Rejected by Reviewer");
						} else {
							rejectRequest.setWorkGroup(Constant.SUPER_ADMIN);
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
						}
						rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
						contractsOnboardingRespository.save(rejectRequest);
						return reviewFailureResponse();

					}

				}
			}
			if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase("Approve")) {
				try {
					superAdminsaveData(requestId, profile, onboardingWorkFlowRequest);
				} catch (DataValidationException e) {
					throw new DataValidationException(Constant.VALID_VALUE, requestId, HttpStatus.CONFLICT);
				}
				return reviewSuccessResponse();
			} else {

				if (requestId != null) {
					ContractOnboardingDetails rejectRequestForReviewer = contractsOnboardingRespository
							.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
					ContractOnboardingDetails rejectRequestForApprover = contractsOnboardingRespository
							.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
					if (rejectRequestForReviewer != null) {
						rejectRequestForReviewer.setApprovedRejected(Constant.REJECTED);
						rejectRequestForReviewer.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
						rejectRequestForReviewer.setWorkGroup(Constant.SUPERADMIN);
						rejectRequestForReviewer.setComments(onboardingWorkFlowRequest.getComments());
						contractsOnboardingRespository.save(rejectRequestForReviewer);
						return reviewFailureResponse();
					}
					if (rejectRequestForApprover != null) {
						rejectRequestForApprover.setApprovedRejected(Constant.REJECTED);
						rejectRequestForApprover.setWorkGroup(Constant.SUPERADMIN);
						rejectRequestForApprover.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
						rejectRequestForApprover.setComments(onboardingWorkFlowRequest.getComments());
						contractsOnboardingRespository.save(rejectRequestForApprover);
						return reviewFailureResponse();
					}
					throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null, HttpStatus.CONFLICT);
				}

			}

		} else {
			if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase("Approve")) {
				if (userDetails.getUserRole().equalsIgnoreCase("approver")) {
					if (requestId != null) {
						ContractOnboardingDetails parentRequest = contractsOnboardingRespository
								.findByRequestNumber(requestId, Constant.APPROVER, "Review");
						if (parentRequest == null) {
							throw new DataValidationException("Provide Valid Id", requestId, HttpStatus.CONFLICT);
						}
						parentRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						parentRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						parentRequest.setComments(onboardingWorkFlowRequest.getComments());
						parentRequest.setWorkGroup(Constant.APPROVER);
						parentRequest.setOnboardingStatus("Approved By Approver");
						parentRequest.setEndDate(new Date());
						parentRequest.setUpdatedBy(profile.getFirstName());
						parentRequest.setUpdatedOn(new Date());
						contractsOnboardingRespository.save(parentRequest);
						String contractId = null;
						String contractSeqId = Constant.CONTRACT_ID;
						Integer contractSequence = sequenceGeneratorRepository.getContractSequence();
						contractSeqId = contractSeqId.concat(contractSequence.toString());
						SequenceGenerator contractupdateSequence = sequenceGeneratorRepository.getById(1);
						contractupdateSequence.setApplicationContacts(++contractSequence);
						contractId = contractSeqId;
						ObjectMapper obj = new ObjectMapper();
						ContractOnboardingRequest contract = obj.readValue(parentRequest.getContractOnboardingRequest(),
								ContractOnboardingRequest.class);
						ApplicationDetails applicationDetails = applicationDetailsRepository
								.findByApplicationId(parentRequest.getApplicationId());
						ApplicationContractDetails applicationContractDetails = new ApplicationContractDetails();
						applicationContractDetails.setContractId(contractId);
						applicationContractDetails.setAutoRenew(contract.getAutoRenewal());
						applicationContractDetails.setBuID(Constant.BUID);
						applicationContractDetails.setOpID(Constant.SAASPE);
						applicationContractDetails.setContractCurrency(contract.getCurrencyCode());
						applicationContractDetails.setApplicationId(applicationDetails);
						applicationContractDetails.setContractName(parentRequest.getContractName());
						applicationContractDetails.setContractStartDate(contract.getContractStartDate());
						applicationContractDetails.setContractEndDate(contract.getContractEndDate());
						applicationContractDetails.setContractOwner(applicationDetails.getOwner());
						applicationContractDetails.setContractOwnerEmail(applicationDetails.getOwnerEmail());
						ZoneId defaultZoneId = ZoneId.systemDefault();
						LocalDate today = LocalDate.now();
						Date currentDate = Date.from(today.atStartOfDay(defaultZoneId).toInstant());
						if (applicationContractDetails.getContractEndDate().before(currentDate)) {
							applicationContractDetails.setContractStatus(Constant.EXPIRED);
						} else if (applicationContractDetails.getContractStartDate().after(currentDate)) {
							applicationContractDetails.setContractStatus(Constant.INACTIVE);
						} else {
							applicationContractDetails.setContractStatus(Constant.ACTIVE);
						}
						applicationContractDetails.setContractPaymentMethod(contract.getPaymentMethod());
						applicationContractDetails.setBillingFrequency(contract.getBillingFrequency());
						applicationContractDetails.setAutoRenewalCancellation(contract.getAutoRenewalCancellation());
						applicationContractDetails.setContractType(contract.getContractType());
						applicationContractDetails.setStartDate(new Date());
						applicationContractDetails.setCreatedOn(new Date());
						if (ContractType.monthToMonth(contract.getContractType())) {
							applicationContractDetails.setBillingFrequency(null);
							applicationContractDetails.setContractTenure(null);
						} else {
							applicationContractDetails.setBillingFrequency(contract.getBillingFrequency());
							applicationContractDetails.setContractTenure(Integer.valueOf(contract.getContractTenure()));
						}
						ApplicationSubscriptionDetails subscriptionId = applicationSubscriptionDetailsRepository
								.findByApplicationId(applicationDetails.getApplicationId());
						applicationContractDetails.setSubscriptionDetails(subscriptionId);

						String invseSeq = Constant.INVOICE_ID;
						Integer invSequence = sequenceGeneratorRepository.getPaymentReqSequence();
						invseSeq = invseSeq.concat(invSequence.toString());
						SequenceGenerator invoiceUpdateSequence = sequenceGeneratorRepository.getById(1);
						invoiceUpdateSequence.setPaymentSequenceId(++invSequence);

						PaymentDetails paymentDetails = new PaymentDetails();
						paymentDetails.setInvoiceNo(invseSeq);
						paymentDetails.setApplicationId(applicationDetails.getApplicationId());
						paymentDetails.setBuID(Constant.BUID);
						paymentDetails.setCreatedBy(profile.getEmailAddress());
						paymentDetails.setCreatedOn(new Date());
						paymentDetails.setStartDate(new Date());
						if (Boolean.TRUE.equals(contract.getAutoRenewal())) {
							applicationContractDetails.setRenewalDate(contract.getUpcomingRenewalDate());
							paymentDetails.setPaymentMethod(contract.getPaymentMethod());
							if (contract.getPaymentMethod().equalsIgnoreCase(Constant.WALLET)) {
								paymentDetails.setWalletName(contract.getWalletName());
							} else {
								paymentDetails.setCardholderName(contract.getCardHolderName());
								if (contract.getCardNumber() != null || contract.getCardNumber().length() != 0) {
									String cardNo = Base64.getEncoder()
											.encodeToString(contract.getCardNumber().getBytes(StandardCharsets.UTF_8));
									String trimmedCardNo = cardNo.replaceAll("\\s", "").trim();
									paymentDetails.setCardNumber(trimmedCardNo);
								}
								paymentDetails.setValidThrough(contract.getValidThrough());
							}
						}
						paymentDetails.setContractId(contractId);
						paymentDetailsRepository.save(paymentDetails);
						sequenceGeneratorRepository.save(invoiceUpdateSequence);

						applicationContractDetailsRepository.save(applicationContractDetails);
						sequenceGeneratorRepository.save(contractupdateSequence);

						for (Products license : contract.getProducts()) {
							String licenseSeq = Constant.LICENSE_ID;
							Integer licenseSequence = sequenceGeneratorRepository.getLicenseSequence();
							licenseSeq = licenseSeq.concat(licenseSequence.toString());
							SequenceGenerator licenseUpdateSequence = sequenceGeneratorRepository.getById(1);
							licenseUpdateSequence.setApplicatiionLicense(++licenseSequence);
							ApplicationLicenseDetails applicationLicenseDetails = licenseContractTotalCost(
									applicationContractDetails, license);
							applicationLicenseDetails.setApplicationId(applicationDetails);
							applicationLicenseDetails.setLicenseId(licenseSeq);
							applicationLicenseDetails.setContractId(applicationContractDetails);
							applicationLicenseDetails.setCreatedBy(profile.getFirstName());
							applicationLicenseDetails.setCreatedOn(new Date());
							applicationLicenseDetails.setBuID(Constant.BUID);
							applicationLicenseDetails.setStartDate(new Date());
							applicationLicenseDetails
									.setLicenseStartDate(applicationContractDetails.getContractStartDate());
							applicationLicenseDetails
									.setLicenseEndDate(applicationContractDetails.getContractEndDate());
							applicationLicenseDetails.setProductName(license.getProductName());
							applicationLicenseDetails.setCurrency(license.getCurrencyCode());
							applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
							applicationLicenseDetails.setUnitPrice(license.getUnitPrice());
							applicationLicenseDetails.setProductCategory(license.getProductType());
							applicationLicenseDetails.setLicenseMapped(0);
							BigDecimal afterCost = BigDecimal.valueOf(0);
							BigDecimal beforeCost = BigDecimal.valueOf(0);
							if (applicationContractDetails.getContractStartDate().after(new Date())) {
								afterCost = getConvertedLicenseCost(applicationContractDetails.getStartDate(),
										applicationContractDetails.getContractCurrency(),
										applicationLicenseDetails.getTotalCost());
								applicationLicenseDetails.setConvertedCost(afterCost);
							} else {
								beforeCost = getConvertedLicenseCost(applicationContractDetails.getContractStartDate(),
										applicationContractDetails.getContractCurrency(),
										applicationLicenseDetails.getTotalCost());
								applicationLicenseDetails.setConvertedCost(beforeCost);
							}
							if (ProductType.professionalServices(license.getProductType())
									|| ProductType.platform(license.getProductType())) {
								applicationLicenseDetails.setQuantity(1);
								applicationLicenseDetails.setLicenseUnMapped(1);
							} else {
								applicationLicenseDetails.setQuantity(license.getQuantity());
								applicationLicenseDetails.setLicenseUnMapped(license.getQuantity());
							}
							applicationLicenseDetailsRepository.save(applicationLicenseDetails);
							sequenceGeneratorRepository.save(licenseUpdateSequence);

							Applications application = applicationRepository
									.findByAppId(parentRequest.getApplicationId());
							Applications contractDetails = new Applications();
							if (application != null) {
								try {
									BeanUtils.copyProperties(contractDetails, application);
								} catch (IllegalAccessException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InvocationTargetException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								contractDetails.setId(0);
								contractDetails.setContractId(applicationContractDetails.getContractId());
								contractDetails.setAutoRenew(applicationContractDetails.getAutoRenew());
								contractDetails.setContractCurrency(applicationContractDetails.getContractCurrency());
								contractDetails
										.setContractDescription(applicationContractDetails.getContractDescription());
								contractDetails.setContractEndDate(applicationContractDetails.getContractEndDate());
								contractDetails.setContractName(applicationContractDetails.getContractName());
								contractDetails
										.setContractNoticeDate(applicationContractDetails.getContractNoticeDate());
								contractDetails.setContractOwner(applicationContractDetails.getContractOwner());
								contractDetails
										.setContractOwnerEmail(applicationContractDetails.getContractOwnerEmail());
								contractDetails.setPaymentMethod(applicationContractDetails.getContractPaymentMethod());
								contractDetails.setPaymentTerm(applicationContractDetails.getContractPaymentTerm());
								contractDetails.setContractProvider(applicationContractDetails.getContractProvider());
								contractDetails.setContractStartDate(applicationContractDetails.getContractStartDate());
								contractDetails.setContractStatus(applicationContractDetails.getContractStatus());
								contractDetails.setReminderDate(applicationContractDetails.getReminderDate());
								contractDetails.setRenewalTerm(applicationContractDetails.getRenewalTerm());
								contractDetails.setContractType(applicationContractDetails.getContractType());
								contractDetails.setBillingFrequency(contractDetails.getBillingFrequency());
								if (applicationContractDetails.getAutoRenewalCancellation() != null)
									contractDetails.setAutoRenewalCancellation(
											applicationContractDetails.getAutoRenewalCancellation());
								contractDetails.setContractTenure(applicationContractDetails.getContractTenure());
								contractDetails.setLicenseId(licenseSeq);
								contractDetails.setLicenseStartDate(applicationContractDetails.getContractStartDate());
								contractDetails.setLicenseEndDate(applicationContractDetails.getContractEndDate());
								contractDetails.setUnitPriceType(license.getUnitPriceType());
								contractDetails.setProductName(license.getProductName());
								contractDetails.setCurrency(license.getCurrencyCode());
								contractDetails.setUnitPrice(license.getUnitPrice());
								contractDetails.setProductCategory(license.getProductType());
								contractDetails.setLicenseMapped(0);
								if (ProductType.professionalServices(license.getProductType())
										|| ProductType.platform(license.getProductType())) {
									contractDetails.setQuantity(1);
									contractDetails.setLicenseUnmapped(1);
								} else {
									contractDetails.setQuantity(license.getQuantity());
									contractDetails.setLicenseUnmapped(license.getQuantity());
								}
								contractDetails.setTotalCost(applicationLicenseDetails.getTotalCost());
								if (applicationContractDetails.getContractStartDate().after(new Date())) {
									contractDetails.setConvertedCost(afterCost);
								} else {
									contractDetails.setConvertedCost(beforeCost);
								}
								contractDetails.setPaymentDescription(paymentDetails.getDescription());
								contractDetails.setPaymentTransactionDate(paymentDetails.getTransactionDate());
								contractDetails.setPaymentAmount(paymentDetails.getAmount());
								contractDetails.setCardholderName(paymentDetails.getCardholderName());
								contractDetails.setCardNumber(paymentDetails.getCardNumber());
								contractDetails.setValidThrough(paymentDetails.getValidThrough());
								contractDetails.setWalletName(paymentDetails.getWalletName());
								contractDetails.setPaymentStartDate(paymentDetails.getStartDate());
								contractDetails.setPaymentEndDate(paymentDetails.getEndDate());
								contractDetails.setPaymentSecretKey(paymentDetails.getSecretKey());
								applicationRepository.save(contractDetails);

							}

						}

					}
					return reviewSuccessResponse();
				} else {
					if (requestId != null) {
						ContractOnboardingDetails rejectRequest = contractsOnboardingRespository
								.findByRequestNumber(requestId, Constant.SUPER_ADMIN, "review");
						rejectRequest.setApprovedRejected("Rejected");
						rejectRequest.setOnboardingStatus("Rejected by SuperAdmin");
						rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
						contractsOnboardingRespository.save(rejectRequest);
						return reviewFailureResponse();
					}
				}

			} else {
				if (requestId != null) {
					ContractOnboardingDetails rejectRequest = contractsOnboardingRespository
							.findByRequestNumber(requestId, Constant.APPROVER, "Review");
					if (rejectRequest == null) {
						throw new DataValidationException(Constant.VALID_VALUE, requestId, HttpStatus.CONFLICT);
					}
					rejectRequest.setOnboardingStatus("Rejected by Approver");
					rejectRequest.setApprovedRejected("Rejected");
					rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
					contractsOnboardingRespository.save(rejectRequest);
					return reviewFailureResponse();
				}
			}
		}
		return commonResponse;
	}

	private ApplicationLicenseDetails licenseContractTotalCost(ApplicationContractDetails contractDetails,
			Products license) {
		ApplicationLicenseDetails applicationLicenseDetails = new ApplicationLicenseDetails();
		if (contractDetails != null) {
			if (contractDetails.getContractTenure() != null) {
				int months = 12;
				int totalMonths = months * contractDetails.getContractTenure();
				int quanity = license.getQuantity();
				if (license.getUnitPriceType().trim().equalsIgnoreCase("per year")) {
					BigDecimal yearPrice = license.getUnitPrice();
					if (contractDetails.getContractTenure() == 1) {
						applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
						yearPrice = yearPrice.multiply(new BigDecimal(quanity), MathContext.DECIMAL32);
						applicationLicenseDetails.setTotalCost(yearPrice);
					} else if (contractDetails.getContractTenure() > 1) {
						yearPrice = yearPrice.multiply(new BigDecimal(contractDetails.getContractTenure()),
								MathContext.DECIMAL32);
						yearPrice = yearPrice.multiply(new BigDecimal(quanity), MathContext.DECIMAL32);
						applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
						applicationLicenseDetails.setTotalCost(yearPrice);
					}
				}
				if (license.getUnitPriceType().trim().equalsIgnoreCase("per month")) {
					BigDecimal monthPrice = license.getUnitPrice();
					if (contractDetails.getContractTenure() == 1) {
						monthPrice = monthPrice.multiply(new BigDecimal(months), MathContext.DECIMAL32);
						monthPrice = monthPrice.multiply(new BigDecimal(quanity), MathContext.DECIMAL32);
						applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
						applicationLicenseDetails.setTotalCost(monthPrice);
					} else if (contractDetails.getContractTenure() > 1) {
						monthPrice = monthPrice.multiply(new BigDecimal(totalMonths), MathContext.DECIMAL32);
						monthPrice = monthPrice.multiply(new BigDecimal(quanity), MathContext.DECIMAL32);
						applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
						applicationLicenseDetails.setTotalCost(monthPrice);
					}
				}
				if (license.getUnitPriceType().trim().equalsIgnoreCase("per contract tenure")) {
					BigDecimal lotPrice = license.getUnitPrice().multiply(BigDecimal.valueOf(quanity));
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setTotalCost(lotPrice);
				}
			} else if (ContractType.monthToMonth(contractDetails.getContractType())) {
				if (license.getUnitPriceType().trim().equalsIgnoreCase("per year")) {
					BigDecimal yearPrice = license.getUnitPrice();
					yearPrice = yearPrice.multiply(new BigDecimal(license.getQuantity()), MathContext.DECIMAL32);
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setTotalCost(yearPrice);
				}
				if (license.getUnitPriceType().trim().equalsIgnoreCase("per month")) {
					BigDecimal monthPrice = license.getUnitPrice();
					monthPrice = monthPrice.multiply(new BigDecimal(license.getQuantity()), MathContext.DECIMAL32);
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setTotalCost(monthPrice);
				}
				if (license.getUnitPriceType().trim().equalsIgnoreCase("per contract tenure")) {
					BigDecimal lotPrice = license.getUnitPrice().multiply(BigDecimal.valueOf(license.getQuantity()));
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setTotalCost(lotPrice);
				}
			}
		}
		return applicationLicenseDetails;
	}

	private CommonResponse reviewSuccessResponse() {
		return new CommonResponse(HttpStatus.OK,
				new Response("OnboardingWorkflowActionResponse", "Approved Successfully"), "Workflow action completed");
	}

	private List<ContractOnboadingRequesListView> getListOfContracts(String role, String key) {
		List<ContractOnboardingDetails> contractOnboardingDetails = contractsOnboardingRespository.getAllByName(role,
				key);
		List<ContractOnboadingRequesListView> list = new ArrayList<>();
		for (ContractOnboardingDetails contractOnboarding : contractOnboardingDetails) {
			ContractOnboadingRequesListView listViewResponse = new ContractOnboadingRequesListView();
			ApplicationDetails applicationDetails = applicationDetailsRepository
					.findByApplicationId(contractOnboarding.getApplicationId());
			listViewResponse.setDepartmentName(applicationDetails.getOwnerDepartment());
			listViewResponse.setApplicationName(contractOnboarding.getApplicationName());
			listViewResponse.setContractName(contractOnboarding.getContractName());
			listViewResponse.setOnboardedByEmail(contractOnboarding.getOnboardedByUserEmail());
			listViewResponse.setRequestId(contractOnboarding.getRequestNumber());
			ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
					.findByApplicationName(contractOnboarding.getApplicationName());
			listViewResponse.setApplicationLogo(applicationLogoEntity.getLogoUrl());
			listViewResponse.setChildRequestId(contractOnboarding.getChildRequestNumber());
			listViewResponse.setOnboardingStatus(contractOnboarding.getOnboardingStatus());
			if (role.equalsIgnoreCase(Constant.APPROVER)) {
				listViewResponse.setReviewedByEmail(contractOnboarding.getWorkGroupUserEmail());
			}
			if (contractOnboarding.getChildRequestNumber() != null) {
				listViewResponse.setChildRequestId(contractOnboarding.getChildRequestNumber());
			}
			list.add(listViewResponse);
		}
		return list;
	}

	private CommonResponse reviewFailureResponse() {
		return new CommonResponse(HttpStatus.OK, new Response("OnboardingWorkflowActionResponse", "Workflow rejected"),
				"Workflow action completed");
	}

	private void superAdminsaveData(String requestId, UserLoginDetails profile,
			DeptOnboardingWorkFlowRequest onboardingWorkFlowRequest)
			throws DataValidationException, IOException, java.text.ParseException {
		if (requestId != null) {
			ContractOnboardingDetails superAdminRequest = contractsOnboardingRespository
					.findAllBySuperAdminRequestId(requestId);
			if (superAdminRequest == null) {
				throw new DataValidationException(Constant.VALID_VALUE, requestId, HttpStatus.CONFLICT);
			}
			if (superAdminRequest.getWorkGroup().equalsIgnoreCase(Constant.REVIEWER)) {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setUpdatedOn(new Date());
				superAdminRequest.setWorkGroup(Constant.SUPER_ADMIN);
				contractsOnboardingRespository.save(superAdminRequest);
				String contractId = null;
				String contractSeqId = Constant.CONTRACT_ID;
				Integer contractSequence = sequenceGeneratorRepository.getContractSequence();
				contractSeqId = contractSeqId.concat(contractSequence.toString());
				SequenceGenerator contractupdateSequence = sequenceGeneratorRepository.getById(1);
				contractupdateSequence.setApplicationContacts(++contractSequence);
				contractId = contractSeqId;
				ObjectMapper obj = new ObjectMapper();
				ContractOnboardingRequest contract = obj.readValue(superAdminRequest.getContractOnboardingRequest(),
						ContractOnboardingRequest.class);
				ApplicationDetails applicationDetails = applicationDetailsRepository
						.findByApplicationId(superAdminRequest.getApplicationId());
				ApplicationContractDetails applicationContractDetails = new ApplicationContractDetails();
				applicationContractDetails.setContractId(contractId);
				applicationContractDetails.setAutoRenew(contract.getAutoRenewal());
				applicationContractDetails.setBuID(Constant.BUID);
				applicationContractDetails.setOpID(Constant.SAASPE);
				applicationContractDetails.setContractCurrency(contract.getCurrencyCode());
				applicationContractDetails.setApplicationId(applicationDetails);
				applicationContractDetails.setContractName(superAdminRequest.getContractName());
				applicationContractDetails.setContractStartDate(contract.getContractStartDate());
				applicationContractDetails.setContractEndDate(contract.getContractEndDate());
				applicationContractDetails.setRenewalDate(contract.getUpcomingRenewalDate());
				applicationContractDetails.setStartDate(new Date());
				applicationContractDetails.setCreatedOn(new Date());
				applicationContractDetails.setRenewalTerm(contract.getBillingFrequency());
				applicationContractDetails.setContractOwner(applicationDetails.getOwner());
				applicationContractDetails.setContractOwnerEmail(applicationDetails.getOwnerEmail());
				ZoneId defaultZoneId = ZoneId.systemDefault();
				LocalDate today = LocalDate.now();
				Date currentDate = Date.from(today.atStartOfDay(defaultZoneId).toInstant());
				if (applicationContractDetails.getContractEndDate().before(currentDate)) {
					applicationContractDetails.setContractStatus(Constant.EXPIRED);
				} else if (applicationContractDetails.getContractStartDate().after(currentDate)) {
					applicationContractDetails.setContractStatus(Constant.INACTIVE);
				} else {
					applicationContractDetails.setContractStatus(Constant.ACTIVE);
				}
				if (ContractType.monthToMonth(contract.getContractType())) {
					applicationContractDetails.setBillingFrequency(null);
					applicationContractDetails.setContractTenure(null);
				} else {
					applicationContractDetails.setBillingFrequency(contract.getBillingFrequency());
					applicationContractDetails.setContractTenure(Integer.valueOf(contract.getContractTenure()));
				}
				applicationContractDetails.setContractPaymentMethod(contract.getPaymentMethod());
				applicationContractDetails.setBillingFrequency(contract.getBillingFrequency());
				applicationContractDetails.setAutoRenewalCancellation(contract.getAutoRenewalCancellation());
				applicationContractDetails.setContractType(contract.getContractType());
				ApplicationSubscriptionDetails subscriptionId = applicationSubscriptionDetailsRepository
						.findByApplicationId(applicationDetails.getApplicationId());
				applicationContractDetails.setSubscriptionDetails(subscriptionId);

				String invseSeq = Constant.INVOICE_ID;
				Integer invSequence = sequenceGeneratorRepository.getPaymentReqSequence();
				invseSeq = invseSeq.concat(invSequence.toString());
				SequenceGenerator invoiceUpdateSequence = sequenceGeneratorRepository.getById(1);
				invoiceUpdateSequence.setPaymentSequenceId(++invSequence);

				PaymentDetails paymentDetails = new PaymentDetails();
				paymentDetails.setInvoiceNo(invseSeq);
				paymentDetails.setApplicationId(applicationDetails.getApplicationId());
				paymentDetails.setBuID(Constant.BUID);
				paymentDetails.setCreatedBy(profile.getEmailAddress());
				paymentDetails.setCreatedOn(new Date());
				paymentDetails.setStartDate(new Date());

				if (Boolean.TRUE.equals(contract.getAutoRenewal())) {
					paymentDetails.setPaymentMethod(contract.getPaymentMethod());
					if (contract.getPaymentMethod().equalsIgnoreCase(Constant.WALLET)) {
						paymentDetails.setWalletName(contract.getWalletName());
					} else {
						paymentDetails.setCardholderName(contract.getCardHolderName());
						if (contract.getCardNumber() != null || contract.getCardNumber().length() != 0) {
							String cardNo = Base64.getEncoder()
									.encodeToString(contract.getCardNumber().getBytes(StandardCharsets.UTF_8));
							String trimmedCardNo = cardNo.replaceAll("\\s", "").trim();
							paymentDetails.setCardNumber(trimmedCardNo);
						}
						paymentDetails.setValidThrough(contract.getValidThrough());
					}
				}
				paymentDetails.setContractId(contractId);
				paymentDetailsRepository.save(paymentDetails);
				sequenceGeneratorRepository.save(invoiceUpdateSequence);
				applicationContractDetailsRepository.save(applicationContractDetails);
				sequenceGeneratorRepository.save(contractupdateSequence);
				for (Products license : contract.getProducts()) {
					String licenseSeq = Constant.LICENSE_ID;
					Integer licenseSequence = sequenceGeneratorRepository.getLicenseSequence();
					licenseSeq = licenseSeq.concat(licenseSequence.toString());
					SequenceGenerator licenseUpdateSequence = sequenceGeneratorRepository.getById(1);
					licenseUpdateSequence.setApplicatiionLicense(++licenseSequence);
					ApplicationLicenseDetails applicationLicenseDetails = licenseContractTotalCost(
							applicationContractDetails, license);
					applicationLicenseDetails.setApplicationId(applicationDetails);
					applicationLicenseDetails.setLicenseId(licenseSeq);
					applicationLicenseDetails.setContractId(applicationContractDetails);
					applicationLicenseDetails.setCreatedBy(profile.getFirstName());
					applicationLicenseDetails.setCreatedOn(new Date());
					applicationLicenseDetails.setBuID(Constant.BUID);
					applicationLicenseDetails.setStartDate(new Date());
					applicationLicenseDetails.setLicenseStartDate(applicationContractDetails.getContractStartDate());
					applicationLicenseDetails.setLicenseEndDate(applicationContractDetails.getContractEndDate());
					applicationLicenseDetails.setProductName(license.getProductName());
					applicationLicenseDetails.setCurrency(license.getCurrencyCode());
					applicationLicenseDetails.setUnitPrice(license.getUnitPrice());
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setProductCategory(license.getProductType());
					applicationLicenseDetails.setLicenseMapped(0);
					BigDecimal afterCost = BigDecimal.valueOf(0);
					BigDecimal beforeCost = BigDecimal.valueOf(0);
					if (applicationContractDetails.getContractStartDate().after(new Date())) {
						afterCost = getConvertedLicenseCost(applicationContractDetails.getStartDate(),
								applicationContractDetails.getContractCurrency(),
								applicationLicenseDetails.getTotalCost());
						applicationLicenseDetails.setConvertedCost(afterCost);
					} else {
						beforeCost = getConvertedLicenseCost(applicationContractDetails.getContractStartDate(),
								applicationContractDetails.getContractCurrency(),
								applicationLicenseDetails.getTotalCost());
						applicationLicenseDetails.setConvertedCost(beforeCost);
					}
					if (ProductType.professionalServices(license.getProductType())
							|| ProductType.platform(license.getProductType())) {
						applicationLicenseDetails.setQuantity(1);
						applicationLicenseDetails.setLicenseUnMapped(1);
					} else {
						applicationLicenseDetails.setQuantity(license.getQuantity());
						applicationLicenseDetails.setLicenseUnMapped(license.getQuantity());
					}
					applicationLicenseDetailsRepository.save(applicationLicenseDetails);
					sequenceGeneratorRepository.save(licenseUpdateSequence);

					Applications application = applicationRepository.findByAppId(superAdminRequest.getApplicationId());
					Applications contractDetails = new Applications();
					if (application != null) {
						try {
							BeanUtils.copyProperties(contractDetails, application);
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						contractDetails.setId(0);
						contractDetails.setContractId(applicationContractDetails.getContractId());
						contractDetails.setAutoRenew(applicationContractDetails.getAutoRenew());
						contractDetails.setContractCurrency(applicationContractDetails.getContractCurrency());
						contractDetails.setContractDescription(applicationContractDetails.getContractDescription());
						contractDetails.setContractEndDate(applicationContractDetails.getContractEndDate());
						contractDetails.setContractName(applicationContractDetails.getContractName());
						contractDetails.setContractNoticeDate(applicationContractDetails.getContractNoticeDate());
						contractDetails.setContractOwner(applicationContractDetails.getContractOwner());
						contractDetails.setContractOwnerEmail(applicationContractDetails.getContractOwnerEmail());
						contractDetails.setPaymentMethod(applicationContractDetails.getContractPaymentMethod());
						contractDetails.setPaymentTerm(applicationContractDetails.getContractPaymentTerm());
						contractDetails.setContractProvider(applicationContractDetails.getContractProvider());
						contractDetails.setContractStartDate(applicationContractDetails.getContractStartDate());
						contractDetails.setContractStatus(applicationContractDetails.getContractStatus());
						contractDetails.setReminderDate(applicationContractDetails.getReminderDate());
						contractDetails.setRenewalTerm(applicationContractDetails.getRenewalTerm());
						contractDetails.setContractType(applicationContractDetails.getContractType());
						contractDetails.setBillingFrequency(contractDetails.getBillingFrequency());
						if (applicationContractDetails.getAutoRenewalCancellation() != null)
							contractDetails.setAutoRenewalCancellation(
									applicationContractDetails.getAutoRenewalCancellation());
						contractDetails.setContractTenure(applicationContractDetails.getContractTenure());
						contractDetails.setLicenseId(licenseSeq);
						contractDetails.setLicenseStartDate(applicationContractDetails.getContractStartDate());
						contractDetails.setLicenseEndDate(applicationContractDetails.getContractEndDate());
						contractDetails.setUnitPriceType(license.getUnitPriceType());
						contractDetails.setProductName(license.getProductName());
						contractDetails.setCurrency(license.getCurrencyCode());
						contractDetails.setUnitPrice(license.getUnitPrice());
						contractDetails.setProductCategory(license.getProductType());
						contractDetails.setLicenseMapped(0);
						if (ProductType.professionalServices(license.getProductType())
								|| ProductType.platform(license.getProductType())) {
							contractDetails.setQuantity(1);
							contractDetails.setLicenseUnmapped(1);
						} else {
							contractDetails.setQuantity(license.getQuantity());
							contractDetails.setLicenseUnmapped(license.getQuantity());
						}
						contractDetails.setTotalCost(applicationLicenseDetails.getTotalCost());
						if (applicationContractDetails.getContractStartDate().after(new Date())) {
							contractDetails.setConvertedCost(afterCost);
						} else {
							contractDetails.setConvertedCost(beforeCost);
						}
						contractDetails.setPaymentDescription(paymentDetails.getDescription());
						contractDetails.setPaymentTransactionDate(paymentDetails.getTransactionDate());
						contractDetails.setPaymentAmount(paymentDetails.getAmount());
						contractDetails.setCardholderName(paymentDetails.getCardholderName());
						contractDetails.setCardNumber(paymentDetails.getCardNumber());
						contractDetails.setValidThrough(paymentDetails.getValidThrough());
						contractDetails.setWalletName(paymentDetails.getWalletName());
						contractDetails.setPaymentStartDate(paymentDetails.getStartDate());
						contractDetails.setPaymentEndDate(paymentDetails.getEndDate());
						contractDetails.setPaymentSecretKey(paymentDetails.getSecretKey());
						applicationRepository.save(contractDetails);

					}

				}

			} else {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setWorkGroup(Constant.SUPER_ADMIN);
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setOnboardDate(new Date());
				superAdminRequest.setUpdatedOn(new Date());
				contractsOnboardingRespository.save(superAdminRequest);
				String contractId = null;
				ApplicationContractDetails applicationContractDetails = new ApplicationContractDetails();
				String contractSeqId = Constant.CONTRACT_ID;
				Integer contractSequence = sequenceGeneratorRepository.getContractSequence();
				contractSeqId = contractSeqId.concat(contractSequence.toString());
				SequenceGenerator contractupdateSequence = sequenceGeneratorRepository.getById(1);
				contractupdateSequence.setApplicationContacts(++contractSequence);
				contractId = contractSeqId;
				applicationContractDetails.setContractId(contractId);
				ObjectMapper obj = new ObjectMapper();
				ContractOnboardingRequest contract = obj.readValue(superAdminRequest.getContractOnboardingRequest(),
						ContractOnboardingRequest.class);
				ApplicationDetails applicationDetails = applicationDetailsRepository
						.findByApplicationId(superAdminRequest.getApplicationId());
				applicationContractDetails.setContractId(contractId);
				applicationContractDetails.setAutoRenew(contract.getAutoRenewal());
				applicationContractDetails.setBuID(Constant.BUID);
				applicationContractDetails.setOpID(Constant.SAASPE);
				applicationContractDetails.setContractCurrency(contract.getCurrencyCode());
				applicationContractDetails.setApplicationId(applicationDetails);
				applicationContractDetails.setContractName(superAdminRequest.getContractName());
				applicationContractDetails.setContractStartDate(contract.getContractStartDate());
				applicationContractDetails.setContractEndDate(contract.getContractEndDate());
				applicationContractDetails.setStartDate(new Date());
				applicationContractDetails.setCreatedOn(new Date());
				applicationContractDetails.setRenewalDate(contract.getUpcomingRenewalDate());
				applicationContractDetails.setRenewalTerm(contract.getBillingFrequency());
				applicationContractDetails.setContractOwner(applicationDetails.getOwner());
				applicationContractDetails.setContractOwnerEmail(applicationDetails.getOwnerEmail());
				ZoneId defaultZoneId = ZoneId.systemDefault();
				LocalDate today = LocalDate.now();
				Date currentDate = Date.from(today.atStartOfDay(defaultZoneId).toInstant());
				if (applicationContractDetails.getContractEndDate().before(currentDate)) {
					applicationContractDetails.setContractStatus(Constant.EXPIRED);
				} else if (applicationContractDetails.getContractStartDate().after(currentDate)) {
					applicationContractDetails.setContractStatus(Constant.INACTIVE);
				} else {
					applicationContractDetails.setContractStatus(Constant.ACTIVE);
				}
				if (ContractType.monthToMonth(contract.getContractType())) {
					applicationContractDetails.setBillingFrequency(null);
					applicationContractDetails.setContractTenure(null);
				} else {
					applicationContractDetails.setBillingFrequency(contract.getBillingFrequency());
					applicationContractDetails.setContractTenure(Integer.valueOf(contract.getContractTenure()));
				}
				applicationContractDetails.setContractPaymentMethod(contract.getPaymentMethod());
				applicationContractDetails.setBillingFrequency(contract.getBillingFrequency());
				applicationContractDetails.setAutoRenewalCancellation(contract.getAutoRenewalCancellation());
				applicationContractDetails.setContractType(contract.getContractType());
				ApplicationSubscriptionDetails subscriptionId = applicationSubscriptionDetailsRepository
						.findByApplicationId(applicationDetails.getApplicationId());
				applicationContractDetails.setSubscriptionDetails(subscriptionId);
				String invseSeq = Constant.INVOICE_ID;
				Integer invSequence = sequenceGeneratorRepository.getPaymentReqSequence();
				invseSeq = invseSeq.concat(invSequence.toString());
				SequenceGenerator invoiceUpdateSequence = sequenceGeneratorRepository.getById(1);
				invoiceUpdateSequence.setPaymentSequenceId(++invSequence);
				PaymentDetails paymentDetails = new PaymentDetails();
				paymentDetails.setInvoiceNo(invseSeq);
				paymentDetails.setApplicationId(applicationDetails.getApplicationId());
				paymentDetails.setBuID(Constant.BUID);
				paymentDetails.setCreatedBy(profile.getEmailAddress());
				paymentDetails.setCreatedOn(new Date());
				paymentDetails.setStartDate(new Date());
				if (Boolean.TRUE.equals(contract.getAutoRenewal())) {
					paymentDetails.setPaymentMethod(contract.getPaymentMethod());
					if (contract.getPaymentMethod().equalsIgnoreCase(Constant.WALLET)) {
						paymentDetails.setWalletName(contract.getWalletName());
					} else {
						paymentDetails.setCardholderName(contract.getCardHolderName());
						if (contract.getCardNumber() != null || contract.getCardNumber().length() != 0) {
							String cardNo = Base64.getEncoder()
									.encodeToString(contract.getCardNumber().getBytes(StandardCharsets.UTF_8));
							String trimmedCardNo = cardNo.replaceAll("\\s", "").trim();
							paymentDetails.setCardNumber(trimmedCardNo);
						}
						paymentDetails.setValidThrough(contract.getValidThrough());
					}
				}
				paymentDetails.setContractId(contractId);
				paymentDetailsRepository.save(paymentDetails);
				sequenceGeneratorRepository.save(invoiceUpdateSequence);
				applicationContractDetailsRepository.save(applicationContractDetails);
				sequenceGeneratorRepository.save(contractupdateSequence);
				for (Products license : contract.getProducts()) {
					String licenseSeq = Constant.LICENSE_ID;
					Integer licenseSequence = sequenceGeneratorRepository.getLicenseSequence();
					licenseSeq = licenseSeq.concat(licenseSequence.toString());
					SequenceGenerator licenseUpdateSequence = sequenceGeneratorRepository.getById(1);
					licenseUpdateSequence.setApplicatiionLicense(++licenseSequence);
					ApplicationLicenseDetails applicationLicenseDetails = licenseContractTotalCost(
							applicationContractDetails, license);
					applicationLicenseDetails.setApplicationId(applicationDetails);
					applicationLicenseDetails.setLicenseId(licenseSeq);
					applicationLicenseDetails.setContractId(applicationContractDetails);
					applicationLicenseDetails.setCreatedBy(profile.getFirstName());
					applicationLicenseDetails.setCreatedOn(new Date());
					applicationLicenseDetails.setBuID(Constant.BUID);
					applicationLicenseDetails.setStartDate(new Date());
					applicationLicenseDetails.setLicenseStartDate(applicationContractDetails.getContractStartDate());
					applicationLicenseDetails.setLicenseEndDate(applicationContractDetails.getContractEndDate());
					applicationLicenseDetails.setProductName(license.getProductName());
					applicationLicenseDetails.setCurrency(license.getCurrencyCode());
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setUnitPrice(license.getUnitPrice());
					applicationLicenseDetails.setProductCategory(license.getProductType());
					applicationLicenseDetails.setLicenseMapped(0);
					BigDecimal afterCost = BigDecimal.valueOf(0);
					BigDecimal beforeCost = BigDecimal.valueOf(0);
					if (applicationContractDetails.getContractStartDate().after(new Date())) {
						afterCost = getConvertedLicenseCost(applicationContractDetails.getStartDate(),
								applicationContractDetails.getContractCurrency(),
								applicationLicenseDetails.getTotalCost());
						applicationLicenseDetails.setConvertedCost(afterCost);
					} else {
						beforeCost = getConvertedLicenseCost(applicationContractDetails.getContractStartDate(),
								applicationContractDetails.getContractCurrency(),
								applicationLicenseDetails.getTotalCost());
						applicationLicenseDetails.setConvertedCost(beforeCost);
					}
					if (ProductType.professionalServices(license.getProductType())
							|| ProductType.platform(license.getProductType())) {
						applicationLicenseDetails.setQuantity(1);
						applicationLicenseDetails.setLicenseUnMapped(1);
					} else {
						applicationLicenseDetails.setQuantity(license.getQuantity());
						applicationLicenseDetails.setLicenseUnMapped(license.getQuantity());
					}
					applicationLicenseDetailsRepository.save(applicationLicenseDetails);
					sequenceGeneratorRepository.save(licenseUpdateSequence);

					Applications application = applicationRepository.findByAppId(superAdminRequest.getApplicationId());
					Applications contractDetails = new Applications();
					if (application != null) {
						try {
							BeanUtils.copyProperties(contractDetails, application);
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						contractDetails.setId(0);
						contractDetails.setContractId(applicationContractDetails.getContractId());
						contractDetails.setAutoRenew(applicationContractDetails.getAutoRenew());
						contractDetails.setContractCurrency(applicationContractDetails.getContractCurrency());
						contractDetails.setContractDescription(applicationContractDetails.getContractDescription());
						contractDetails.setContractEndDate(applicationContractDetails.getContractEndDate());
						contractDetails.setContractName(applicationContractDetails.getContractName());
						contractDetails.setContractNoticeDate(applicationContractDetails.getContractNoticeDate());
						contractDetails.setContractOwner(applicationContractDetails.getContractOwner());
						contractDetails.setContractOwnerEmail(applicationContractDetails.getContractOwnerEmail());
						contractDetails.setPaymentMethod(applicationContractDetails.getContractPaymentMethod());
						contractDetails.setPaymentTerm(applicationContractDetails.getContractPaymentTerm());
						contractDetails.setContractProvider(applicationContractDetails.getContractProvider());
						contractDetails.setContractStartDate(applicationContractDetails.getContractStartDate());
						contractDetails.setContractStatus(applicationContractDetails.getContractStatus());
						contractDetails.setReminderDate(applicationContractDetails.getReminderDate());
						contractDetails.setRenewalTerm(applicationContractDetails.getRenewalTerm());
						contractDetails.setContractType(applicationContractDetails.getContractType());
						contractDetails.setBillingFrequency(contractDetails.getBillingFrequency());
						if (applicationContractDetails.getAutoRenewalCancellation() != null)
							contractDetails.setAutoRenewalCancellation(
									applicationContractDetails.getAutoRenewalCancellation());
						contractDetails.setContractTenure(applicationContractDetails.getContractTenure());
						contractDetails.setLicenseId(licenseSeq);
						contractDetails.setLicenseStartDate(applicationContractDetails.getContractStartDate());
						contractDetails.setLicenseEndDate(applicationContractDetails.getContractEndDate());
						contractDetails.setUnitPriceType(license.getUnitPriceType());
						contractDetails.setProductName(license.getProductName());
						contractDetails.setCurrency(license.getCurrencyCode());
						contractDetails.setUnitPrice(license.getUnitPrice());
						contractDetails.setProductCategory(license.getProductType());
						contractDetails.setLicenseMapped(0);
						if (ProductType.professionalServices(license.getProductType())
								|| ProductType.platform(license.getProductType())) {
							contractDetails.setQuantity(1);
							contractDetails.setLicenseUnmapped(1);
						} else {
							contractDetails.setQuantity(license.getQuantity());
							contractDetails.setLicenseUnmapped(license.getQuantity());
						}
						contractDetails.setTotalCost(applicationLicenseDetails.getTotalCost());
						if (applicationContractDetails.getContractStartDate().after(new Date())) {
							contractDetails.setConvertedCost(afterCost);
						} else {
							contractDetails.setConvertedCost(beforeCost);
						}
						contractDetails.setPaymentDescription(paymentDetails.getDescription());
						contractDetails.setPaymentTransactionDate(paymentDetails.getTransactionDate());
						contractDetails.setPaymentAmount(paymentDetails.getAmount());
						contractDetails.setCardholderName(paymentDetails.getCardholderName());
						contractDetails.setCardNumber(paymentDetails.getCardNumber());
						contractDetails.setValidThrough(paymentDetails.getValidThrough());
						contractDetails.setWalletName(paymentDetails.getWalletName());
						contractDetails.setPaymentStartDate(paymentDetails.getStartDate());
						contractDetails.setPaymentEndDate(paymentDetails.getEndDate());
						contractDetails.setPaymentSecretKey(paymentDetails.getSecretKey());
						applicationRepository.save(contractDetails);

					}
				}
			}

		}
	}

	@Override
	public CommonResponse contractReviewerApproverDetailsView(String requestId, UserLoginDetails profile)
			throws URISyntaxException, StorageException, DataValidationException {
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		ContractOnboardingResponse detailViewResponse = new ContractOnboardingResponse();
		ContractReviewerDetails reviewerDetails = new ContractReviewerDetails();
		List<SupportDocumentsResponse> list = new ArrayList<>();
		SupportDocumentsResponse documentsResponse = new SupportDocumentsResponse();

		ContractOnboardingDetails contractDetails = contractsOnboardingRespository.findByRequest(requestId);
		if ((contractDetails.getApprovedRejected().equalsIgnoreCase(Constant.APPROVE)
				|| contractDetails.getApprovedRejected().equalsIgnoreCase(Constant.REJECTED))
				|| (contractDetails.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW)
						&& contractDetails.getWorkGroup().equalsIgnoreCase(Constant.APPROVER)
						&& (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)))) {
			throw new DataValidationException("Requested contract is onboarded already", null, HttpStatus.NO_CONTENT);
		}
		ContractOnboardingDetails apppId = contractsOnboardingRespository.findByRequestIdForApplication(requestId);
		if (apppId.getApplicationId().trim() != null) {
			documentsResponse.setFileUrl(getBlobURI(supportingDocsPath + apppId.getApplicationId().trim()));
			list.add(documentsResponse);
		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER) && (requestId != null)) {
			ContractOnboardingDetails applicationReviewer = contractsOnboardingRespository
					.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
			ContractOnboardingRequest applicationDetails = applicationObjectDeserializer(
					applicationReviewer.getContractOnboardingRequest());
			detailViewResponse.setContractName(applicationDetails.getContractName());
			detailViewResponse.setContractType(applicationDetails.getContractType());
			detailViewResponse.setContractEndDate(applicationDetails.getContractEndDate());
			detailViewResponse.setContractStartDate(applicationDetails.getContractStartDate());
			detailViewResponse.setBillingFrequency(applicationDetails.getBillingFrequency());
			detailViewResponse.setAutoRenewal(applicationDetails.getAutoRenewal());
			detailViewResponse.setPaymentMethod(applicationDetails.getPaymentMethod());
			if (Boolean.TRUE.equals(applicationDetails.getAutoRenewal())) {
				detailViewResponse.setUpcomingRenewalDate(applicationDetails.getUpcomingRenewalDate());
				if (applicationDetails.getPaymentMethod().equalsIgnoreCase(Constant.WALLET)) {
					detailViewResponse.setWalletName(applicationDetails.getWalletName());
				} else {
					if (applicationDetails.getCardNumber() != null
							&& applicationDetails.getCardNumber().length() >= 12) {
						String trimmedCardNo = applicationDetails.getCardNumber().replaceAll("\\s", "").trim();
						String first12Digits = trimmedCardNo.substring(0, 12);
						byte[] decodeData = Base64.getDecoder().decode(first12Digits);
						detailViewResponse.setCardHolderName(applicationDetails.getCardHolderName());
						if (trimmedCardNo.length() > 12) {
							String remainingDigits = trimmedCardNo.substring(12);
							String decodedCardNo = new String(decodeData, StandardCharsets.UTF_8) + remainingDigits;
							detailViewResponse.setCardNumber(decodedCardNo);
						} else {
							detailViewResponse.setCardNumber(new String(decodeData, StandardCharsets.UTF_8));
						}
					}
					detailViewResponse.setValidThrough(applicationDetails.getValidThrough());
				}
			}
			detailViewResponse.setAutoRenewalCancellation(applicationDetails.getAutoRenewalCancellation());
			detailViewResponse.setProducts(applicationDetails.getProducts());
			List<Products> products = applicationDetails.getProducts();
			if (products != null) {
				for (Products product : products) {
					product.setCurrencyCode(applicationDetails.getCurrencyCode());
				}
			}
			detailViewResponse.setSupportingDocsInfo(list);

		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER) && (requestId != null)) {
			ContractOnboardingDetails departmentApprover = contractsOnboardingRespository.findByRequestNumber(requestId,
					Constant.APPROVER, Constant.REVIEW);
			ContractOnboardingDetails departmentReviewer = contractsOnboardingRespository.findByRequestNumber(requestId,
					Constant.REVIEWER, Constant.APPROVE);
			ContractOnboardingRequest applicationDetails = applicationObjectDeserializer(
					departmentApprover.getContractOnboardingRequest());
			reviewerDetails.setApprovedByEmail(departmentReviewer.getWorkGroupUserEmail());
			reviewerDetails.setWorkGroupName(departmentReviewer.getWorkGroup());
			reviewerDetails.setComments(departmentReviewer.getComments());
			reviewerDetails.setApprovalTimeStamp(departmentReviewer.getEndDate());
			detailViewResponse.setContractName(applicationDetails.getContractName());
			detailViewResponse.setContractType(applicationDetails.getContractType());
			detailViewResponse.setContractEndDate(applicationDetails.getContractEndDate());
			detailViewResponse.setContractStartDate(applicationDetails.getContractStartDate());
			detailViewResponse.setBillingFrequency(applicationDetails.getBillingFrequency());
			detailViewResponse.setAutoRenewal(applicationDetails.getAutoRenewal());
			detailViewResponse.setPaymentMethod(applicationDetails.getPaymentMethod());
			if (Boolean.TRUE.equals(applicationDetails.getAutoRenewal())) {
				detailViewResponse.setUpcomingRenewalDate(applicationDetails.getUpcomingRenewalDate());
				if (applicationDetails.getPaymentMethod().equalsIgnoreCase(Constant.WALLET)) {
					detailViewResponse.setWalletName(applicationDetails.getWalletName());
				} else {
					if (applicationDetails.getCardNumber() != null
							&& applicationDetails.getCardNumber().length() >= 12) {
						String trimmedCardNo = applicationDetails.getCardNumber().replaceAll("\\s", "").trim();
						String first12Digits = trimmedCardNo.substring(0, 12);
						byte[] decodeData = Base64.getDecoder().decode(first12Digits);
						detailViewResponse.setCardHolderName(applicationDetails.getCardHolderName());
						if (trimmedCardNo.length() > 12) {
							String remainingDigits = trimmedCardNo.substring(12);
							String decodedCardNo = new String(decodeData, StandardCharsets.UTF_8) + remainingDigits;
							detailViewResponse.setCardNumber(decodedCardNo);
						} else {
							detailViewResponse.setCardNumber(new String(decodeData, StandardCharsets.UTF_8));
						}
					}
					detailViewResponse.setValidThrough(applicationDetails.getValidThrough());
				}
			}
			detailViewResponse.setAutoRenewalCancellation(applicationDetails.getAutoRenewalCancellation());
			detailViewResponse.setProducts(applicationDetails.getProducts());
			List<Products> products = applicationDetails.getProducts();
			if (products != null) {
				for (Products product : products) {
					product.setCurrencyCode(applicationDetails.getCurrencyCode());
				}
			}
			detailViewResponse.setSupportingDocsInfo(list);
			detailViewResponse.setReviewerDetails(reviewerDetails);

		}
		if (userDetails.getUserRole().equalsIgnoreCase("super_admin") && (requestId != null)) {
			ContractOnboardingDetails departmentApprover = contractsOnboardingRespository.findByRequestNumber(requestId,
					Constant.APPROVER, Constant.REVIEW);
			ContractOnboardingDetails departmentReviewerApproved = contractsOnboardingRespository
					.findByRequestNumber(requestId, Constant.REVIEWER, Constant.APPROVE);
			ContractOnboardingDetails departmentReviewer = contractsOnboardingRespository.findByRequestNumber(requestId,
					Constant.REVIEWER, Constant.REVIEW);
			if (departmentApprover != null) {
				ContractOnboardingRequest applicationDetails = applicationObjectDeserializer(
						departmentApprover.getContractOnboardingRequest());
				detailViewResponse.setContractName(applicationDetails.getContractName());
				detailViewResponse.setContractType(applicationDetails.getContractType());
				detailViewResponse.setContractEndDate(applicationDetails.getContractEndDate());
				detailViewResponse.setContractStartDate(applicationDetails.getContractStartDate());
				detailViewResponse.setBillingFrequency(applicationDetails.getBillingFrequency());
				detailViewResponse.setAutoRenewal(applicationDetails.getAutoRenewal());
				detailViewResponse.setPaymentMethod(applicationDetails.getPaymentMethod());
				if (Boolean.TRUE.equals(applicationDetails.getAutoRenewal())) {
					detailViewResponse.setUpcomingRenewalDate(applicationDetails.getUpcomingRenewalDate());
					if (applicationDetails.getPaymentMethod().equalsIgnoreCase(Constant.WALLET)) {
						detailViewResponse.setWalletName(applicationDetails.getWalletName());
					} else {
						if (applicationDetails.getCardNumber() != null
								&& applicationDetails.getCardNumber().length() >= 12) {
							String trimmedCardNo = applicationDetails.getCardNumber().replaceAll("\\s", "").trim();
							String first12Digits = trimmedCardNo.substring(0, 12);
							byte[] decodeData = Base64.getDecoder().decode(first12Digits);
							detailViewResponse.setCardHolderName(applicationDetails.getCardHolderName());
							if (trimmedCardNo.length() > 12) {
								String remainingDigits = trimmedCardNo.substring(12);
								String decodedCardNo = new String(decodeData, StandardCharsets.UTF_8) + remainingDigits;
								detailViewResponse.setCardNumber(decodedCardNo);
							} else {
								detailViewResponse.setCardNumber(new String(decodeData, StandardCharsets.UTF_8));
							}
						}
						detailViewResponse.setValidThrough(applicationDetails.getValidThrough());
					}
				}
				detailViewResponse.setAutoRenewalCancellation(applicationDetails.getAutoRenewalCancellation());
				detailViewResponse.setProducts(applicationDetails.getProducts());
				List<Products> products = applicationDetails.getProducts();
				if (products != null) {
					for (Products product : products) {
						product.setCurrencyCode(applicationDetails.getCurrencyCode());
					}
				}
				detailViewResponse.setSupportingDocsInfo(list);
				reviewerDetails.setApprovedByEmail(departmentReviewerApproved.getWorkGroupUserEmail());
				reviewerDetails.setWorkGroupName(departmentReviewerApproved.getWorkGroup());
				reviewerDetails.setComments(departmentReviewerApproved.getComments());
				reviewerDetails.setApprovalTimeStamp(departmentReviewerApproved.getEndDate());
				detailViewResponse.setReviewerDetails(reviewerDetails);
			} else {
				ContractOnboardingRequest applicationDetails = applicationObjectDeserializer(
						departmentReviewer.getContractOnboardingRequest());
				detailViewResponse.setContractName(applicationDetails.getContractName());
				detailViewResponse.setContractType(applicationDetails.getContractType());
				detailViewResponse.setContractEndDate(applicationDetails.getContractEndDate());
				detailViewResponse.setContractStartDate(applicationDetails.getContractStartDate());
				detailViewResponse.setBillingFrequency(applicationDetails.getBillingFrequency());
				detailViewResponse.setAutoRenewal(applicationDetails.getAutoRenewal());
				detailViewResponse.setPaymentMethod(applicationDetails.getPaymentMethod());
				if (Boolean.TRUE.equals(applicationDetails.getAutoRenewal())) {
					detailViewResponse.setUpcomingRenewalDate(applicationDetails.getUpcomingRenewalDate());
					if (applicationDetails.getPaymentMethod().equalsIgnoreCase(Constant.WALLET)) {
						detailViewResponse.setWalletName(applicationDetails.getWalletName());
					} else {
						if (applicationDetails.getCardNumber() != null
								&& applicationDetails.getCardNumber().length() >= 12) {
							String trimmedCardNo = applicationDetails.getCardNumber().replaceAll("\\s", "").trim();
							String first12Digits = trimmedCardNo.substring(0, 12);
							byte[] decodeData = Base64.getDecoder().decode(first12Digits);
							detailViewResponse.setCardHolderName(applicationDetails.getCardHolderName());
							if (trimmedCardNo.length() > 12) {
								String remainingDigits = trimmedCardNo.substring(12);
								String decodedCardNo = new String(decodeData, StandardCharsets.UTF_8) + remainingDigits;
								detailViewResponse.setCardNumber(decodedCardNo);
							} else {
								detailViewResponse.setCardNumber(new String(decodeData, StandardCharsets.UTF_8));
							}
						}
						detailViewResponse.setValidThrough(applicationDetails.getValidThrough());
					}
				}
				detailViewResponse.setAutoRenewalCancellation(applicationDetails.getAutoRenewalCancellation());
				detailViewResponse.setProducts(applicationDetails.getProducts());
				List<Products> products = applicationDetails.getProducts();
				if (products != null) {
					for (Products product : products) {
						product.setCurrencyCode(applicationDetails.getCurrencyCode());
					}
				}
				detailViewResponse.setSupportingDocsInfo(list);
				detailViewResponse.setReviewerDetails(reviewerDetails);
			}

		}
		return new CommonResponse(HttpStatus.OK,
				new Response("OnboardingRequestDetailViewResponse", detailViewResponse),
				Constant.DATA_RETRIEVED_SUCCESSFULLY);
	}

	private List<URI> getBlobURI(String fileName) throws URISyntaxException, StorageException {
		List<URI> uris = new ArrayList<>();
		try {
			CloudBlobContainer container = cloudBlobClient.getContainerReference(supportingDocsUri);
			for (ListBlobItem blobItem : container.listBlobs(fileName + "/")) {
				uris.add(blobItem.getUri());
			}
		} catch (URISyntaxException e) {
			throw new URISyntaxException("URL Error", "Unable to Connect to Azure, Please Check URL in properties");
		} catch (StorageException e) {
			throw new StorageException(Constant.INSUFFICIENT_STORAGE, Constant.CLIENT_ERROR, e);
		}
		return uris;
	}

	private ContractOnboardingRequest applicationObjectDeserializer(String applicationOnboardingRequest)
			throws DataValidationException {
		ObjectMapper obj = new ObjectMapper();
		ContractOnboardingRequest status = new ContractOnboardingRequest();
		try {
			status = obj.readValue(applicationOnboardingRequest, ContractOnboardingRequest.class);
		} catch (JsonProcessingException e) {
			throw new DataValidationException(e.getLocalizedMessage(), null, HttpStatus.CONFLICT);
		}
		return status;
	}

	@Override
	@Transactional
	public CommonResponse updateContractStatus() throws DataValidationException {
		List<ApplicationContractDetails> contractDetails = applicationContractDetailsRepository.findAll();
		List<Applications> applications = applicationRepository.findAll();
		for (ApplicationContractDetails contract : contractDetails) {
			if (ContractType.annual(contract.getContractType())) {
				if (contract.getApplicationId().getEndDate() == null
						&& contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(LocalDate.now()))) {
					contract.setContractStatus(Constant.EXPIRED);
					for (ApplicationLicenseDetails applicationLicenseDetails : contract.getLicenseDetails()) {
						ApplicationLicenseDetails licenseDetails = applicationLicenseDetailsRepository
								.findByLicenseId(applicationLicenseDetails.getLicenseId());
						for (UserDetails userDetails : licenseDetails.getUserId()) {
							userDetailsRepository.removeUserEmail(userDetails.getUserEmail(),
									licenseDetails.getApplicationId().getApplicationId());
						}
						licenseDetails.getUserId().removeAll(applicationLicenseDetails.getUserId());
						applicationLicenseDetails.setUpdatedOn(new Date());
						applicationLicenseDetailsRepository.save(applicationLicenseDetails);
					}
					for (Applications application : applications) {
						if (application.getContractId() != null
								&& application.getContractId().equals(contract.getContractId())) {
							application.setContractStatus(Constant.EXPIRED);
							applicationRepository.save(application);
						}
					}
				}
				if (contract.getApplicationId().getEndDate() != null
						&& contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(LocalDate.now()))) {
					contract.setContractStatus(Constant.EXPIRED);
					contract.setEndDate(new Date());
					for (ApplicationLicenseDetails applicationLicenseDetails : contract.getLicenseDetails()) {
						ApplicationLicenseDetails licenseDetails = applicationLicenseDetailsRepository
								.findByLicenseId(applicationLicenseDetails.getLicenseId());
						for (UserDetails userDetails : licenseDetails.getUserId()) {
							userDetailsRepository.removeUserEmail(userDetails.getUserEmail(),
									licenseDetails.getApplicationId().getApplicationId());
						}
						licenseDetails.getUserId().removeAll(applicationLicenseDetails.getUserId());
						applicationLicenseDetails.setUpdatedOn(new Date());
						applicationLicenseDetails.setEndDate(new Date());
						applicationLicenseDetailsRepository.save(applicationLicenseDetails);
					}
					for (Applications application : applications) {
						if (application.getContractId() != null
								&& application.getContractId().equals(contract.getContractId())) {
							application.setContractStatus(Constant.EXPIRED);
							applicationRepository.save(application);
						}
					}
				}
				if (contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(LocalDate.now()))
						&& contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(LocalDate.now()))) {
					for (Applications application : applications) {
						if (application.getContractId() != null
								&& application.getContractId().equals(contract.getContractId())) {
							application.setContractStatus(Constant.ACTIVE);
							applicationRepository.save(application);
						}
					}
					contract.setContractStatus(Constant.ACTIVE);
				}
				if (contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(LocalDate.now()))) {
					for (Applications application : applications) {
						if (application.getContractId() != null
								&& application.getContractId().equals(contract.getContractId())) {
							application.setContractStatus("InActive");
							applicationRepository.save(application);
						}
					}
					contract.setContractStatus("InActive");
				}
				applicationContractDetailsRepository.save(contract);
			} else if (ContractType.monthToMonth(contract.getContractType())) {
				if (contract.getApplicationId().getEndDate() == null
						&& contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(LocalDate.now()))) {
					contract.setContractStatus(Constant.EXPIRED);
					for (ApplicationLicenseDetails applicationLicenseDetails : contract.getLicenseDetails()) {
						ApplicationLicenseDetails licenseDetails = applicationLicenseDetailsRepository
								.findByLicenseId(applicationLicenseDetails.getLicenseId());
						for (UserDetails userDetails : licenseDetails.getUserId()) {
							userDetailsRepository.removeUserEmail(userDetails.getUserEmail(),
									licenseDetails.getApplicationId().getApplicationId());
						}
						licenseDetails.getUserId().removeAll(applicationLicenseDetails.getUserId());
						applicationLicenseDetails.setUpdatedOn(new Date());
						applicationLicenseDetailsRepository.save(applicationLicenseDetails);
					}
					for (Applications application : applications) {
						if (application.getContractId() != null
								&& application.getContractId().equals(contract.getContractId())) {
							application.setContractStatus(Constant.EXPIRED);
							applicationRepository.save(application);
						}
					}
				}
				if (contract.getApplicationId().getEndDate() != null
						&& contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(LocalDate.now()))) {
					contract.setContractStatus(Constant.EXPIRED);
					contract.setEndDate(new Date());
					for (ApplicationLicenseDetails applicationLicenseDetails : contract.getLicenseDetails()) {
						ApplicationLicenseDetails licenseDetails = applicationLicenseDetailsRepository
								.findByLicenseId(applicationLicenseDetails.getLicenseId());
						for (UserDetails userDetails : licenseDetails.getUserId()) {
							userDetailsRepository.removeUserEmail(userDetails.getUserEmail(),
									licenseDetails.getApplicationId().getApplicationId());
						}
						licenseDetails.getUserId().removeAll(applicationLicenseDetails.getUserId());
						applicationLicenseDetails.setEndDate(new Date());
						applicationLicenseDetails.setUpdatedOn(new Date());
						applicationLicenseDetailsRepository.save(applicationLicenseDetails);
					}
					for (Applications application : applications) {
						if (application.getContractId() != null
								&& application.getContractId().equals(contract.getContractId())) {
							application.setContractStatus(Constant.EXPIRED);
							applicationRepository.save(application);
						}
					}
				}
				if (contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(LocalDate.now()))
						&& contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(LocalDate.now()))) {
					for (Applications application : applications) {
						if (application.getContractId() != null
								&& application.getContractId().equals(contract.getContractId())) {
							application.setContractStatus(Constant.ACTIVE);
							applicationRepository.save(application);
						}
					}
					contract.setContractStatus(Constant.ACTIVE);
				}
				if (contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(LocalDate.now()))) {
					for (Applications application : applications) {
						if (application.getContractId() != null
								&& application.getContractId().equals(contract.getContractId())) {
							application.setContractStatus("InActive");
							applicationRepository.save(application);
						}
					}
					contract.setContractStatus("InActive");
				}
				applicationContractDetailsRepository.save(contract);

			}
		}
		List<ApplicationDetails> listOfNotDeletedApp = applicationDetailsRepository.findNotDeletedApplications();
		for (ApplicationDetails application : listOfNotDeletedApp) {
			int activeContractsCount = 0;
			if (application.getSubscriptionDetails() == null || application.getContractDetails().isEmpty()) {
				application.setActiveContracts(null);
				application.setApplicationStatus(Constant.ACTIVE);
				applicationDetailsRepository.save(application);
				for (Applications app : applications) {
					if (app.getApplicationId().equals(application.getApplicationId())) {
						application.setActiveContracts(null);
						application.setApplicationStatus(Constant.ACTIVE);
						applicationRepository.save(app);
					}
				}
			} else {
				for (ApplicationContractDetails contract : application.getContractDetails()) {
					if (contract.getContractStatus().equalsIgnoreCase(Constant.ACTIVE)) {
						activeContractsCount = +1;
					}
				}
				application.setActiveContracts(activeContractsCount);
				if (activeContractsCount == 0) {
					application.setApplicationStatus("Inactive");
				} else {
					application.setApplicationStatus(Constant.ACTIVE);
				}
				application.setUpdatedOn(new Date());
				applicationDetailsRepository.save(application);
				for (Applications app : applications) {
					if (app.getApplicationId().equals(application.getApplicationId())) {
						app.setActiveContracts(activeContractsCount);
						if (activeContractsCount == 0) {
							app.setApplicationStatus("Inactive");
						} else {
							app.setApplicationStatus(Constant.ACTIVE);
						}
						app.setUpdatedOn(new Date());
						applicationRepository.save(app);
					}
				}
			}
		}
		return null;

	}

	private void applicatoinContractValidator(ContractOnboardingRequest contractOnboardingRequest)
			throws DataValidationException, java.text.ParseException {
		Date contractStartDate = contractOnboardingRequest.getContractStartDate();
		Date contractEndtDate = contractOnboardingRequest.getContractEndDate();
		if (ContractType.annual(contractOnboardingRequest.getContractType())) {
			Integer tenure = Integer.parseInt(contractOnboardingRequest.getContractTenure());
			if (tenure != null) {
				Calendar contractStart = dateToCalendar(contractStartDate);
				contractStart.add(Calendar.YEAR, tenure);
				Date actualEndDate = contractStart.getTime();
				LocalDate localDateContractEndDate = CommonUtil.dateToLocalDate(actualEndDate);
				Date dateConverted = CommonUtil.convertLocalDatetoDate(localDateContractEndDate.minusDays(1));
				if (CommonUtil.simpleDateFormat(contractEndtDate).after(dateConverted)) {
					throw new DataValidationException("For " + contractOnboardingRequest.getContractName()
							+ " ContractEndDate is grater than ContarctTenure = "
							+ contractOnboardingRequest.getContractTenure(), null, null);
				} else if (CommonUtil.simpleDateFormat(contractEndtDate).before(dateConverted)) {
					throw new DataValidationException("For " + contractOnboardingRequest.getContractName()
							+ " ContractEndDate is less than ContarctTenure = "
							+ contractOnboardingRequest.getContractTenure(), null, null);
				}
			}
		} else if (ContractType.monthToMonth(contractOnboardingRequest.getContractType())) {
			LocalDate localDateContractEndDate = CommonUtil.dateToLocalDate(contractStartDate);
			int days = CommonUtil.getDaysBasedOnDate(localDateContractEndDate);
			Date dateConverted = CommonUtil
					.convertLocalDatetoDate(localDateContractEndDate.plusDays(days).minusDays(1));
			if (CommonUtil.simpleDateFormat(contractEndtDate).after(dateConverted)) {
				throw new DataValidationException("For " + contractOnboardingRequest.getContractName()
						+ " ContractEndDate is grater than expected EndDate", null, null);
			} else if (CommonUtil.simpleDateFormat(contractEndtDate).before(dateConverted)) {
				throw new DataValidationException("For " + contractOnboardingRequest.getContractName()
						+ "  ContractEndDate is less than expected EndDate", null, null);
			}
		}
	}

	public BigDecimal getConvertedLicenseCost(Date contractStartDate, String contractCurrency, BigDecimal totalCost)
			throws java.text.ParseException {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = formatter.format(contractStartDate);
		BigDecimal totalSpend = new BigDecimal(0);
		ApiKeys apiKey = apiKeysRepository.getById();
		List<String> currency = Constant.CURRENCY;
		List<String> apiKeys = Arrays.asList(apiKey.getApiKey().split(","));
		UserDetails superAdminCurrency = userDetailsRepository.getCurrency();
		CurrencyEntity entryCheck = currencyRepository
				.findByDateAndCurrecncy(formatter.parse(formatter.format(contractStartDate)), contractCurrency);
		if (entryCheck != null) {
			if (contractCurrency.equalsIgnoreCase(superAdminCurrency.getCurrency())) {
				totalSpend = totalSpend.add(totalCost);
			} else {
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("INR")
						&& !contractCurrency.equalsIgnoreCase("INR")) {
					totalSpend = totalSpend.add(totalCost.multiply(entryCheck.getInr()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("USD")
						&& !contractCurrency.equalsIgnoreCase("USD")) {
					totalSpend = totalSpend.add(totalCost.multiply(entryCheck.getUsd()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("MYR")
						&& !contractCurrency.equalsIgnoreCase("MYR")) {
					totalSpend = totalSpend.add(totalCost.multiply(entryCheck.getMyr()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("PHP")
						&& !contractCurrency.equalsIgnoreCase("PHP")) {
					totalSpend = totalSpend.add(totalCost.multiply(entryCheck.getPhp()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("SGD")
						&& !contractCurrency.equalsIgnoreCase("SGD")) {
					totalSpend = totalSpend.add(totalCost.multiply(entryCheck.getSgd()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("EUR")
						&& !contractCurrency.equalsIgnoreCase("EUR")) {
					totalSpend = totalSpend.add(totalCost.multiply(entryCheck.getEur()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("GBP")
						&& !contractCurrency.equalsIgnoreCase("GBP")) {
					totalSpend = totalSpend.add(totalCost.multiply(entryCheck.getGbp()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("AUD")
						&& !contractCurrency.equalsIgnoreCase("AUD")) {
					totalSpend = totalSpend.add(totalCost.multiply(entryCheck.getAud()));
				}
			}

		} else {
			ZoneId defaultZoneId = ZoneId.systemDefault();
			for (String curr : currency) {
				String url = contractUrl;
				url = url.replace("{{date}}", currentDate);
				url = url.replace("{{base}}", curr);
				try {
					CurrencyConverterResponse data = WebClient.create().get().uri(url)
							.header(Constant.APIKEY, apiKeys.get(0)).retrieve()
							.bodyToMono(CurrencyConverterResponse.class).block();
					if (data.getMessage() == null) {
						CurrencyEntity entity = new CurrencyEntity();
						entity.setDate(Date.from(LocalDate.parse(currentDate).atStartOfDay(defaultZoneId).toInstant()));
						entity.setAud(data.getRates().getAUD());
						entity.setUsd(data.getRates().getUSD());
						entity.setInr(data.getRates().getINR());
						entity.setMyr(data.getRates().getMYR());
						entity.setAed(data.getRates().getAED());
						entity.setCad(data.getRates().getCAD());
						entity.setPhp(data.getRates().getPHP());
						entity.setSgd(data.getRates().getSGD());
						entity.setEur(data.getRates().getEUR());
						entity.setGbp(data.getRates().getGBP());
						entity.setBase(data.getBase());
						currencyRepository.save(entity);
					} else {
						apiKeys.remove(0);
						CurrencyConverterResponse secondaryData = WebClient.create().get().uri(url)
								.header(Constant.APIKEY, apiKeys.get(0)).retrieve()
								.bodyToMono(CurrencyConverterResponse.class).block();
						CurrencyEntity entity = new CurrencyEntity();
						entity.setDate(Date.from(LocalDate.parse(currentDate).atStartOfDay(defaultZoneId).toInstant()));
						entity.setAud(secondaryData.getRates().getAUD());
						entity.setUsd(secondaryData.getRates().getUSD());
						entity.setInr(secondaryData.getRates().getINR());
						entity.setMyr(secondaryData.getRates().getMYR());
						entity.setAed(secondaryData.getRates().getAED());
						entity.setCad(secondaryData.getRates().getCAD());
						entity.setPhp(secondaryData.getRates().getPHP());
						entity.setSgd(secondaryData.getRates().getSGD());
						entity.setEur(secondaryData.getRates().getEUR());
						entity.setGbp(secondaryData.getRates().getGBP());
						entity.setBase(secondaryData.getBase());
						currencyRepository.save(entity);
					}
				} catch (WebClientResponseException ex) {
					if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
						apiKeys.remove(0);
						CurrencyConverterResponse secondaryData = WebClient.create().get().uri(url)
								.header(Constant.APIKEY, apiKeys.get(0)).retrieve()
								.bodyToMono(CurrencyConverterResponse.class).block();
						CurrencyEntity entity = new CurrencyEntity();
						entity.setDate(Date.from(LocalDate.parse(currentDate).atStartOfDay(defaultZoneId).toInstant()));
						entity.setAud(secondaryData.getRates().getAUD());
						entity.setUsd(secondaryData.getRates().getUSD());
						entity.setInr(secondaryData.getRates().getINR());
						entity.setMyr(secondaryData.getRates().getMYR());
						entity.setAed(secondaryData.getRates().getAED());
						entity.setCad(secondaryData.getRates().getCAD());
						entity.setPhp(secondaryData.getRates().getPHP());
						entity.setSgd(secondaryData.getRates().getSGD());
						entity.setEur(secondaryData.getRates().getEUR());
						entity.setGbp(secondaryData.getRates().getGBP());
						entity.setBase(secondaryData.getBase());
						currencyRepository.save(entity);
					} else {
						try {
							CurrencyConverterResponse secondaryData = WebClient.create().get().uri(url)
									.header(Constant.APIKEY, apiKeys.get(0)).retrieve()
									.bodyToMono(CurrencyConverterResponse.class).block();
							CurrencyEntity entity = new CurrencyEntity();
							entity.setDate(
									Date.from(LocalDate.parse(currentDate).atStartOfDay(defaultZoneId).toInstant()));
							entity.setAud(secondaryData.getRates().getAUD());
							entity.setUsd(secondaryData.getRates().getUSD());
							entity.setInr(secondaryData.getRates().getINR());
							entity.setMyr(secondaryData.getRates().getMYR());
							entity.setAed(secondaryData.getRates().getAED());
							entity.setCad(secondaryData.getRates().getCAD());
							entity.setPhp(secondaryData.getRates().getPHP());
							entity.setSgd(secondaryData.getRates().getSGD());
							entity.setEur(secondaryData.getRates().getEUR());
							entity.setGbp(secondaryData.getRates().getGBP());
							entity.setBase(secondaryData.getBase());
							currencyRepository.save(entity);
						} catch (WebClientResponseException e) {
							if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
								apiKeys.remove(0);
								CurrencyConverterResponse secondaryData = WebClient.create().get().uri(url)
										.header(Constant.APIKEY, apiKeys.get(0)).retrieve()
										.bodyToMono(CurrencyConverterResponse.class).block();
								CurrencyEntity entity = new CurrencyEntity();
								entity.setDate(Date
										.from(LocalDate.parse(currentDate).atStartOfDay(defaultZoneId).toInstant()));
								entity.setAud(secondaryData.getRates().getAUD());
								entity.setUsd(secondaryData.getRates().getUSD());
								entity.setInr(secondaryData.getRates().getINR());
								entity.setMyr(secondaryData.getRates().getMYR());
								entity.setAed(secondaryData.getRates().getAED());
								entity.setCad(secondaryData.getRates().getCAD());
								entity.setPhp(secondaryData.getRates().getPHP());
								entity.setSgd(secondaryData.getRates().getSGD());
								entity.setEur(secondaryData.getRates().getEUR());
								entity.setGbp(secondaryData.getRates().getGBP());
								entity.setBase(secondaryData.getBase());
								currencyRepository.save(entity);
							} else {
								CurrencyConverterResponse secondaryData = WebClient.create().get().uri(url)
										.header(Constant.APIKEY, apiKeys.get(0)).retrieve()
										.bodyToMono(CurrencyConverterResponse.class).block();
								CurrencyEntity entity = new CurrencyEntity();
								entity.setDate(Date
										.from(LocalDate.parse(currentDate).atStartOfDay(defaultZoneId).toInstant()));
								entity.setAud(secondaryData.getRates().getAUD());
								entity.setUsd(secondaryData.getRates().getUSD());
								entity.setInr(secondaryData.getRates().getINR());
								entity.setMyr(secondaryData.getRates().getMYR());
								entity.setAed(secondaryData.getRates().getAED());
								entity.setCad(secondaryData.getRates().getCAD());
								entity.setPhp(secondaryData.getRates().getPHP());
								entity.setSgd(secondaryData.getRates().getSGD());
								entity.setEur(secondaryData.getRates().getEUR());
								entity.setGbp(secondaryData.getRates().getGBP());
								entity.setBase(secondaryData.getBase());
								currencyRepository.save(entity);
							}

						}

					}
				}
			}
			if (contractCurrency.equalsIgnoreCase(superAdminCurrency.getCurrency())) {
				totalSpend = totalSpend.add(totalSpend);
			} else {
				CurrencyEntity currencyValue = currencyRepository.findByDateAndCurrecncy(contractStartDate,
						contractCurrency);
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("INR")
						&& !contractCurrency.equalsIgnoreCase("INR")) {
					totalSpend = totalSpend.add(totalCost.multiply(currencyValue.getInr()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("USD")
						&& !contractCurrency.equalsIgnoreCase("USD")) {
					totalSpend = totalSpend.add(totalCost.multiply(currencyValue.getUsd()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("MYR")
						&& !contractCurrency.equalsIgnoreCase("MYR")) {
					totalSpend = totalSpend.add(totalCost.multiply(currencyValue.getMyr()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("PHP")
						&& !contractCurrency.equalsIgnoreCase("PHP")) {
					totalSpend = totalSpend.add(totalCost.multiply(currencyValue.getPhp()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("SGD")
						&& !contractCurrency.equalsIgnoreCase("SGD")) {
					totalSpend = totalSpend.add(totalCost.multiply(currencyValue.getSgd()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("EUR")
						&& !contractCurrency.equalsIgnoreCase("EUR")) {
					totalSpend = totalSpend.add(totalCost.multiply(currencyValue.getEur()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("GBP")
						&& !contractCurrency.equalsIgnoreCase("GBP")) {
					totalSpend = totalSpend.add(totalCost.multiply(currencyValue.getGbp()));
				}
				if (superAdminCurrency.getCurrency().equalsIgnoreCase("AUD")
						&& !contractCurrency.equalsIgnoreCase("AUD")) {
					totalSpend = totalSpend.add(totalCost.multiply(currencyValue.getAud()));
				}
			}

		}
		apiKey.setApiKey(String.join(",", apiKeys));
		apiKeysRepository.save(apiKey);
		return totalSpend;

	}

}
