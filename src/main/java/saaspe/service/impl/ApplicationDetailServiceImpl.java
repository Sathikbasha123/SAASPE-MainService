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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.NonNull;
import saaspe.adaptor.model.CRMUsersResponse;
import saaspe.adaptor.model.DatadogGetUserResponse;
import saaspe.adaptor.model.GithubUser;
import saaspe.adaptor.model.GitlabDeleteUserRequest;
import saaspe.adaptor.model.GitlabInvitationResponse;
import saaspe.adaptor.model.GitlabUser;
import saaspe.adaptor.model.HubSpotGetUserlistResponse;
import saaspe.adaptor.model.Microsoft365getUserlistResponse;
import saaspe.adaptor.model.QuickBooksUsers;
import saaspe.adaptor.model.QuickbooksUrls;
import saaspe.adaptor.model.RemoveUserRequest;
import saaspe.adaptor.model.ZoomgetUserlistResponse;
import saaspe.adaptor.service.ConfluenceWrapperService;
import saaspe.adaptor.service.DatadogWrapperService;
import saaspe.adaptor.service.FreshdeskWrapperService;
import saaspe.adaptor.service.GithubWrapperService;
import saaspe.adaptor.service.GitlabWrapperService;
import saaspe.adaptor.service.HubSpotWrapperService;
import saaspe.adaptor.service.JiraWrapperService;
import saaspe.adaptor.service.Microsoft365WrapperService;
import saaspe.adaptor.service.QuickBookWrapperService;
import saaspe.adaptor.service.SalesforceService;
import saaspe.adaptor.service.ZohoAnalyticsService;
import saaspe.adaptor.service.ZohoCRMWrapperService;
import saaspe.adaptor.service.ZohoPeopleService;
import saaspe.adaptor.service.ZoomWrapperService;
import saaspe.configuration.DateParser;
import saaspe.constant.Constant;
import saaspe.constant.ContractType;
import saaspe.constant.ProductType;
import saaspe.constant.UnitPriceType;
import saaspe.currency.entity.ApiKeys;
import saaspe.currency.entity.CurrencyEntity;
import saaspe.currency.repository.ApiKeysRepository;
import saaspe.currency.repository.CurrencyRepository;
import saaspe.entity.AdaptorCredential;
import saaspe.entity.AdaptorDetails;
import saaspe.entity.ApplicationCategoryMaster;
import saaspe.entity.ApplicationContractDetails;
import saaspe.entity.ApplicationDetails;
import saaspe.entity.ApplicationLicenseDetails;
import saaspe.entity.ApplicationLogoEntity;
import saaspe.entity.ApplicationOnboarding;
import saaspe.entity.ApplicationOwnerDetails;
import saaspe.entity.ApplicationProviderDetails;
import saaspe.entity.ApplicationSubscriptionDetails;
import saaspe.entity.Applications;
import saaspe.entity.AtlassianJiraUsers;
import saaspe.entity.AuthenticationEntity;
import saaspe.entity.ContractOnboardingDetails;
import saaspe.entity.DepartmentDetails;
import saaspe.entity.DepartmentOwnerDetails;
import saaspe.entity.Departments;
import saaspe.entity.PaymentDetails;
import saaspe.entity.ProjectDetails;
import saaspe.entity.ProjectManagerDetails;
import saaspe.entity.Projects;
import saaspe.entity.SequenceGenerator;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLastLoginDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.entity.Users;
import saaspe.exception.DataValidationException;
import saaspe.model.AdaptorCredentialListView;
import saaspe.model.AdaptorKeyValues;
import saaspe.model.AdaptorValue;
import saaspe.model.ApplicationDetailsLicensesResponse;
import saaspe.model.ApplicationDetailsOverviewResponse;
import saaspe.model.ApplicationDetailsResponse;
import saaspe.model.ApplicationDetailsUpdateRequest;
import saaspe.model.ApplicationDetailsUsersResponse;
import saaspe.model.ApplicationIdsRemoveRequest;
import saaspe.model.ApplicationListViewResponse;
import saaspe.model.ApplicationOnboardingListViewResponse;
import saaspe.model.ApplicationWorkFlowDetailsView;
import saaspe.model.CommonResponse;
import saaspe.model.Credentails;
import saaspe.model.CurrencyConverterResponse;
import saaspe.model.DepartmentReviewerDetails;
import saaspe.model.DeptOnboardingWorkFlowRequest;
import saaspe.model.NewAppContractInfo;
import saaspe.model.NewAppLicenseInfo;
import saaspe.model.NewAppOnboardInfo;
import saaspe.model.NewApplicationOnboardingResposne;
import saaspe.model.NewSingeApplicationOnboardingRequest;
import saaspe.model.PurchasedApplcationRequest;
import saaspe.model.PurchasedSingleAppOnboardingRequest;
import saaspe.model.Response;
import saaspe.model.SingleNewApplicationOnboardingRequest;
import saaspe.model.SupportDocumentsResponse;
import saaspe.model.newApplicationOwnerDetailsRequest;
import saaspe.repository.AdaptorCredentialRepository;
import saaspe.repository.AdaptorDetailsRepsitory;
import saaspe.repository.ApplicationCategoryMasterRepository;
import saaspe.repository.ApplicationContractDetailsRepository;
import saaspe.repository.ApplicationDetailsRepository;
import saaspe.repository.ApplicationLicenseDetailsRepository;
import saaspe.repository.ApplicationLogoRepository;
import saaspe.repository.ApplicationOnboardingRepository;
import saaspe.repository.ApplicationOwnerRepository;
import saaspe.repository.ApplicationProviderDetailsRepository;
import saaspe.repository.ApplicationSubscriptionDetailsRepository;
import saaspe.repository.ApplicationsRepository;
import saaspe.repository.AtlassianJiraUsersRepository;
import saaspe.repository.AuthenticaionServiceRepository;
import saaspe.repository.ContractsOnboardingRespository;
import saaspe.repository.DepartmentOwnerRepository;
import saaspe.repository.DepartmentRepository;
import saaspe.repository.DepartmentsRepository;
import saaspe.repository.PaymentDetailsRepository;
import saaspe.repository.ProjectDetailsRepository;
import saaspe.repository.ProjectOwnerRepository;
import saaspe.repository.ProjectsRepository;
import saaspe.repository.SequenceGeneratorRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.repository.UserLastLoginDetailRepository;
import saaspe.repository.UsersRepository;
import saaspe.service.ApplicationDetailService;
import saaspe.utils.CommonUtil;

@Service
public class ApplicationDetailServiceImpl implements ApplicationDetailService {

	@Autowired
	private ApplicationDetailsRepository applicationDetailsRepository;

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	private ApplicationCategoryMasterRepository categoryMasterRepository;

	@Autowired
	private ApplicationLicenseDetailsRepository licenseDetailsRepository;

	@Autowired
	private SequenceGeneratorRepository sequenceGeneratorRepository;

	@Autowired
	private ApplicationLogoRepository applicationLogoRepository;

	@Autowired
	private ApplicationContractDetailsRepository contractDetailsRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private ApplicationProviderDetailsRepository providerDetailsRepository;

	@Autowired
	private ApplicationSubscriptionDetailsRepository applicationSubscriptionDetailsRepository;

	@Autowired
	private ApplicationOnboardingRepository applicationOnboardingRepository;

	@Autowired
	private ProjectDetailsRepository projectDetailsRepository;

	@Autowired
	private PaymentDetailsRepository paymentDetailsRepository;

	@Autowired
	private CloudBlobClient cloudBlobClient;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private UserLastLoginDetailRepository userLastLoginDetailRepository;

	@Autowired
	private Configuration config;

	@Autowired
	private AuthenticaionServiceRepository authenticaionServiceRepository;

	@Autowired
	private ContractsOnboardingRespository contractsOnboardingRespository;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private ApiKeysRepository apiKeysRepository;

	@Autowired
	private ApplicationOwnerRepository applicationOwnerRepository;

	@Autowired
	private DepartmentOwnerRepository departmentOwnerRepository;

	@Autowired
	private AdaptorDetailsRepsitory adaptorDetailsRepository;

	@Value("${azure.storage.container.name}")
	private String supportingDocsUri;

	@Value("${azure.storage.container.supporting.name}")
	private String supportingDocsPath;

	@Value("${sendgrid.domain.name}")
	private String mailDomainName;

	@Value("${sendgrid.domain.sendername}")
	private String senderName;

	@Value("${application.detail.url}")
	private String applicationDetailUrl;

	@Value("${redirecturl.be.path}")
	private String domain;

	@Value("${adapters.host.url}")
	private String adaptorsHost;

	@Value("${microsoft.getrefreshtoken.api.url}")
	private String refreshTokenUrl;

	@Value("${zoom.getrefreshtoken.api.url}")
	private String zoomRefreshTokenUrl;

	@Autowired
	private HubSpotWrapperService hubSpotService;

	@Autowired
	private Microsoft365WrapperService microsoft365Service;

	@Autowired
	private ConfluenceWrapperService confluenceWrapperService;

	@Autowired
	private ZohoCRMWrapperService zohoCRMservice;

	@Autowired
	private GithubWrapperService githubService;

	@Autowired
	private DatadogWrapperService datadogWrapperService;

	@Autowired
	private GitlabWrapperService gitlabService;

	@Autowired
	private QuickBookWrapperService quickbooksService;

	@Autowired
	private AdaptorCredentialRepository adaptorCredentialRepository;

	@Autowired
	private AtlassianJiraUsersRepository atlassianJiraUsersRepository;

	@Autowired
	private JiraWrapperService jiraWrapperService;

	@Autowired
	ZohoPeopleService zohoPeopleService;

	@Value("${quickbooks-urls-file}")
	private String quickbooksUrls;
	@Autowired
	private ZohoAnalyticsService zohoAnalyticsService;

	@Autowired
	private SalesforceService salesforceService;

	@Autowired
	private FreshdeskWrapperService freshdeskService;

	@Autowired
	private ZoomWrapperService zoomService;

	@Autowired
	private ApplicationsRepository applicationRepository;

	@Autowired
	private ProjectsRepository projectsRepository;

	@Autowired
	private ProjectOwnerRepository projectOwnerRepository;

	@Autowired
	private DepartmentsRepository departmentsRepository;

	@Autowired
	private UsersRepository usersRepository;

	private static Calendar dateToCalendar(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	public QuickbooksUrls getQuickbooksUrls() {
		ClassPathResource resource = new ClassPathResource(quickbooksUrls);
		ObjectMapper objectMapper = new ObjectMapper();
		QuickbooksUrls quickbooksUrl = null;
		try {
			quickbooksUrl = objectMapper.readValue(resource.getInputStream(), QuickbooksUrls.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return quickbooksUrl;
	}

	private static final Logger log = LoggerFactory.getLogger(ApplicationDetailServiceImpl.class);

	@Override
	public CommonResponse modifyApplicationDetails(String applicationId, ApplicationDetailsUpdateRequest updateRequest)
			throws DataValidationException {
		if (applicationDetailsRepository.existsById(applicationId) && applicationId != null) {
			ApplicationDetails details = applicationDetailsRepository.findByApplicationId(applicationId);
			List<Applications> apps = applicationRepository.findByApplicationId(applicationId);
			List<ApplicationContractDetails> applicationContractDetails = contractDetailsRepository
					.findByApplicationId(applicationId);
			List<String> categoryName = categoryMasterRepository.findCategoryName();
			if (categoryName.stream().noneMatch(updateRequest.getApplicationCetegory()::equals)) {
				throw new DataValidationException(
						"Category name " + updateRequest.getApplicationCetegory() + Constant.MISMATCH_ERROR, "400",
						HttpStatus.BAD_REQUEST);
			}
			ApplicationCategoryMaster applicationCategoryMaster = categoryMasterRepository
					.findByCategoryName(updateRequest.getApplicationCetegory());
			details.setCategoryId(applicationCategoryMaster.getCategoryId());
			DepartmentDetails departmentDetails = departmentRepository
					.findByDepartmentName(updateRequest.getApplicationOwnerDepartment());
			details.setOwnerDepartment(departmentDetails.getDepartmentName());
			details.setOwnerEmail(updateRequest.getApplicationOwnerEmail());
		
			for (ApplicationContractDetails contracts : applicationContractDetails) {
				if (contracts != null) {
					contracts.setAutoRenew(updateRequest.isAutoRenewal());
					contractDetailsRepository.save(contracts);
				}
			}
			for (Applications app : apps) {
				app.setCategoryId(applicationCategoryMaster.getCategoryId());
				app.setCategoryName(updateRequest.getApplicationCetegory());
				app.setOwnerDepartment(departmentDetails.getDepartmentName());
				if (app.getContractId() != null) {
					app.setAutoRenew(updateRequest.isAutoRenewal());
				}
				app.setUpdatedOn(new Date());
				applicationRepository.save(app);
			}
			ApplicationLogoEntity logoEntity = applicationLogoRepository
					.findByApplicationName(details.getApplicationName());
			logoEntity.setApplicationPageUrl(updateRequest.getApplicationLink());
			applicationLogoRepository.save(logoEntity);
			applicationDetailsRepository.save(details);
		} else {
			throw new DataValidationException(Constant.APPLICATION_NOT_FOUND, "404", HttpStatus.NOT_FOUND);
		}
		return new CommonResponse(HttpStatus.OK,
				new Response("ModifyApplicationDetailsResponse", "application payment details updated!"),
				Constant.DETAILS_UPDATED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse removeApplicationDetails(String applicationId) throws DataValidationException {
		ApplicationDetailsResponse applicationDetailsResponse = new ApplicationDetailsResponse();
		ApplicationDetails applicationDetails = applicationDetailsRepository.findByApplicationId(applicationId);
		List<UserLastLoginDetails> lastloginbyappid = userLastLoginDetailRepository.findApplicationId(applicationId);
		List<ContractOnboardingDetails> contractOnboardingDetails = contractsOnboardingRespository
				.findByApplicationId(applicationId);
		List<ContractOnboardingDetails> onboardingDetails = contractOnboardingDetails.stream()
				.filter(p -> p.getApprovedRejected().equalsIgnoreCase("Review")).collect(Collectors.toList());
		List<Applications> apps = applicationRepository.findByApplicationId(applicationId);
		if (onboardingDetails != null) {
			throw new DataValidationException(
					"Contract under this application " + applicationId + "is still in review stage!!", null, null);
		}
		if (applicationDetails != null) {
			List<ApplicationContractDetails> applicationContractDetails = contractDetailsRepository
					.findExpiredContracts(applicationId);
			ApplicationSubscriptionDetails applicationSubscriptionDetails = applicationSubscriptionDetailsRepository
					.findByApplicationId(applicationId);
			for (ApplicationContractDetails contracts : applicationContractDetails) {
				for (ApplicationLicenseDetails license : contracts.getLicenseDetails()) {
					if (license != null && license.getLicenseId() != null) {
						license.setEndDate(new Date());
						licenseDetailsRepository.save(license);
					}
				}
			}
			for (ApplicationContractDetails contracts : applicationContractDetails) {
				if (contracts != null && contracts.getContractId() != null) {
					contracts.setEndDate(new Date());
					contractDetailsRepository.save(contracts);
				}
			}
			if (applicationSubscriptionDetails != null && applicationSubscriptionDetails.getSubscriptionId() != null) {
				applicationSubscriptionDetails.setEndDate(new Date());
				applicationSubscriptionDetailsRepository.save(applicationSubscriptionDetails);
			}

			for (DepartmentDetails departmentDetails : applicationDetails.getDepartmentDetails()) {
				departmentDetails.getApplicationId().remove(applicationDetails);
			}
			for (UserDetails details : applicationDetails.getUserDetails()) {
				for (ApplicationLicenseDetails licenseDetails : details.getLicenseId()) {
					if ("Expired".equalsIgnoreCase(licenseDetails.getContractId().getContractStatus())) {
						details.getApplicationId().remove(applicationDetails);
						licenseDetails.getUserId().remove(details);
					}

				}
			}
			for (UserLastLoginDetails lastlogin : lastloginbyappid) {
				if (lastlogin != null) {
					lastlogin.setEndDate(new Date());
					userLastLoginDetailRepository.save(lastlogin);
				}
			}
			for (Applications app : apps) {
				if (app.getSubscriptionId() != null) {
					app.setSubscriptionEndDate(new Date());
				}
				app.setEndDate(new Date());
				applicationRepository.save(app);
			}
			applicationDetails.setEndDate(new Date());
			applicationDetailsRepository.save(applicationDetails);
			applicationDetailsResponse.setMessage("Application with ID " + applicationId + " has removed");
		} else {
			throw new DataValidationException(Constant.APPLICATION_DETAILS_NOT_FOUND, "404", HttpStatus.NOT_FOUND);
		}
		return new CommonResponse(HttpStatus.OK, new Response("RemoveApplicationDetails", applicationDetailsResponse),
				"Details updated successfully");
	}

	@Override
	public CommonResponse getApplicationListView() {
		List<ApplicationDetails> applicationList = applicationDetailsRepository.findRemainingApplications();
		List<ApplicationListViewResponse> applicationListViewResponses = new ArrayList<>();
		for (ApplicationDetails applicationDetails : applicationList) {
			ApplicationListViewResponse listViewResponse = new ApplicationListViewResponse();
			ApplicationCategoryMaster listadd = categoryMasterRepository
					.findByCategoryId(applicationDetails.getCategoryId());
			BigDecimal totalCost = BigDecimal.valueOf(0.0);
			BigDecimal adminCost = BigDecimal.valueOf(0.0);
			Integer licneseCount = 0;
			Integer count = 0;
			if (applicationDetails.getUserDetails() != null) {
				for (UserDetails userDetails : applicationDetails.getUserDetails()) {
					if (userDetails.getEndDate() == null) {
						count = count + 1;
					}
				}
			}
			List<ApplicationContractDetails> activeContractsforApp = contractDetailsRepository
					.findActiveContracts(applicationDetails.getApplicationId());
			Integer activeContractCount = 0;
			for (ApplicationContractDetails details : activeContractsforApp) {
				if (details.getContractStatus().equalsIgnoreCase("Active")) {
					activeContractCount = activeContractCount + 1;
				}
			}

			List<ApplicationContractDetails> contractListForTotalCost = contractDetailsRepository
					.findActiveExpiredContracts(applicationDetails.getApplicationId());
			for (ApplicationContractDetails contractDetails : contractListForTotalCost) {
				for (ApplicationLicenseDetails license : contractDetails.getLicenseDetails()) {
					totalCost = totalCost.add(license.getTotalCost(), MathContext.DECIMAL32);
					adminCost = adminCost.add(license.getConvertedCost(), MathContext.DECIMAL32);
				}
				listViewResponse.setCurrencyCode(contractDetails.getContractCurrency());
			}
			List<ApplicationContractDetails> activeContracts = contractDetailsRepository
					.findActiveContracts(applicationDetails.getApplicationId());
			for (ApplicationContractDetails contractDetails : activeContracts) {
				for (ApplicationLicenseDetails license : contractDetails.getLicenseDetails()) {
					licneseCount = licneseCount + license.getQuantity();
				}
			}

			listViewResponse.setApplicationContracts(activeContractCount);
			listViewResponse.setDepartmentName(applicationDetails.getOwnerDepartment());
			DepartmentDetails departmentDetails = departmentRepository
					.findByDepartmentName(applicationDetails.getOwnerDepartment());
			listViewResponse.setDepartmentId(departmentDetails.getDepartmentId());
			List<ApplicationOwnerDetails> applicationOwners = applicationOwnerRepository
					.findByApplicationId(applicationDetails.getApplicationId());
			List<newApplicationOwnerDetailsRequest> ownerDetails = new ArrayList<>();
			if (applicationDetails.getOwnerEmail() != null) {
				UserDetails userdetails = userDetailsRepository.findByuserEmail(applicationDetails.getOwnerEmail());
				newApplicationOwnerDetailsRequest owner = new newApplicationOwnerDetailsRequest();
				owner.setApplicaitonOwnerName(userdetails.getUserName());
				owner.setApplicationOwnerEmail(userdetails.getUserEmail());
				owner.setPriority(1);
				ownerDetails.add(owner);
			}
			applicationOwners.forEach(p -> {
				newApplicationOwnerDetailsRequest owner = new newApplicationOwnerDetailsRequest();
				owner.setApplicaitonOwnerName(p.getOwner());
				owner.setApplicationOwnerEmail(p.getOwnerEmail());
				owner.setPriority(p.getPriority());
				ownerDetails.add(owner);
			});
			listViewResponse.setOwners(ownerDetails);
			listViewResponse.setApplicationActiveUsers(count);
			listViewResponse.setApplicationId(applicationDetails.getApplicationId());
			listViewResponse.setApplicationLicenses(licneseCount);
			Map<String, BigDecimal> spend = updatedGetTotalCostYTD(LocalDate.now().withDayOfYear(1), LocalDate.now(),
					applicationDetails.getApplicationId());
			listViewResponse.setApplicationSpend(spend.get(Constant.TOTAL));
			listViewResponse.setAdminCostYtd(spend.get(Constant.ADMIN));
			listViewResponse.setTotalSpend(totalCost);
			listViewResponse.setAdminCost(adminCost);
			listViewResponse.setApplicationName(applicationDetails.getApplicationName());
			listViewResponse.setApplicationLogo(applicationDetails.getLogoUrl());
			listViewResponse.setApplicationCategory(listadd.getCategoryName());
			if (applicationDetails.getContractDetails() != null) {
				applicationListViewResponses.add(listViewResponse);
			}
		}
		if (applicationListViewResponses == null || applicationListViewResponses.isEmpty()) {
			return new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("applicationListViewResponse", applicationListViewResponses),
					Constant.NO_APPLICATIONS_FOUND);

		}
		return new CommonResponse(HttpStatus.OK,
				new Response("applicationListViewResponse", applicationListViewResponses),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private Map<String, BigDecimal> updatedGetTotalCostYTD(LocalDate firstDayOfQuarter, LocalDate lastDayOfQuarter,
			String applicationId) {
		Map<String, BigDecimal> spend = new HashMap<>();
		BigDecimal totalSpend = BigDecimal.valueOf(0);
		BigDecimal adminSpend = BigDecimal.valueOf(0);
		List<ApplicationContractDetails> applicationContractDetails = contractDetailsRepository
				.findActiveExpiredContracts(applicationId);
		for (ApplicationContractDetails contract : applicationContractDetails) {

			LocalDate localContractStart = contract.getContractStartDate().toInstant().atZone(ZoneId.systemDefault())
					.toLocalDate();
			LocalDate localContractEnd = contract.getContractEndDate().toInstant().atZone(ZoneId.systemDefault())
					.toLocalDate();

			BigDecimal totalContractCost = BigDecimal

					.valueOf(contract.getLicenseDetails().stream().mapToLong(p -> p.getTotalCost().longValue()).sum());
			BigDecimal totalAdminContractCost = BigDecimal.valueOf(
					contract.getLicenseDetails().stream().mapToLong(p -> p.getConvertedCost().longValue()).sum());

			if (ContractType.annual(contract.getContractType())) {
				BigDecimal monthlyCost = totalContractCost.divide(
						BigDecimal.valueOf(contract.getContractTenure()).multiply(BigDecimal.valueOf(12)), 2,
						RoundingMode.FLOOR);
				BigDecimal monthlyAdminCost = totalAdminContractCost.divide(
						BigDecimal.valueOf(contract.getContractTenure()).multiply(BigDecimal.valueOf(12)), 2,
						RoundingMode.FLOOR);

				if (contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
						&& (contract.getContractEndDate()
								.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)))) {
					if (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
						if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusYears(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
									}
									i++;
								}
							}
						}
						if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 6;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
									}
									i = i + 6;
								}
							}
						}
						if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 3;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
									}
									i = i + 3;
								}
							}
						}
						if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
									}
									i++;
								}
							}
						}
					}
					if (contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
						if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusYears(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(localContractEnd))
										|| CommonUtil.convertLocalDatetoDate(plusYears)
												.compareTo(CommonUtil.convertLocalDatetoDate(localContractEnd)) == 0) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
									}
									i++;
								}
							}
						}
						if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 6;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(localContractEnd))
										|| CommonUtil.convertLocalDatetoDate(plusYears)
												.compareTo(CommonUtil.convertLocalDatetoDate(localContractEnd)) == 0) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
									}
									i = i + 6;
								}
							}
						}
						if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 3;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(localContractEnd))
										|| CommonUtil.convertLocalDatetoDate(plusYears)
												.compareTo(CommonUtil.convertLocalDatetoDate(localContractEnd)) == 0) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
									}
									i = i + 3;
								}
							}
						}
						if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(localContractEnd))
										|| CommonUtil.convertLocalDatetoDate(plusYears)
												.compareTo(CommonUtil.convertLocalDatetoDate(localContractEnd)) == 0) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
									}
									i++;
								}
							}
						}

					}
					if (contract.getContractEndDate()
							.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {

						if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusYears(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(localContractEnd))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
									}
									i++;
								}
							}
						}
						if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 6;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(localContractEnd))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
									}
									i = i + 6;
								}
							}
						}
						if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 3;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(localContractEnd))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
									}
									i = i + 3;
								}
							}
						}
						if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(localContractEnd))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
											|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
													CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
									}
									i++;
								}
							}
						}
					}

				}
				if (contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))) {
					if (contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
						if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(totalContractCost);
							adminSpend = adminSpend.add(totalAdminContractCost);
						}
						if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusYears(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
								}
								i++;
							}
						}
						if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 6;
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
								}
								i = i + 6;
							}
						}
						if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							int i = 3;
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
								}
								i = i + 3;
							}
						}
						if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
								}
								i++;
							}
						}

					}
					if (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
						if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(totalContractCost);
							adminSpend = adminSpend.add(totalAdminContractCost);
						}
						if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusYears(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0
											|| CommonUtil.convertLocalDatetoDate(plusYears)
													.before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
									}
									i++;
								}
							}
						}
						if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
							int i = 6;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0
											|| CommonUtil.convertLocalDatetoDate(plusYears)
													.before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
									}
									i = i + 6;
								}
							}
						}
						if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
							int i = 3;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0
											|| CommonUtil.convertLocalDatetoDate(plusYears)
													.before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
									}
									i = i + 3;
								}
							}
						}
						if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0
											|| CommonUtil.convertLocalDatetoDate(plusYears)
													.before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
									}
									i++;
								}
							}
						}

					}
					if (contract.getContractEndDate()
							.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
						if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(totalContractCost);
							adminSpend = adminSpend.add(totalAdminContractCost);
						}
						if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusYears(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
									i++;
								}
							}
						}
						if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
							int i = 6;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
									i = i + 6;
								}
							}
						}
						if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
							int i = 3;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
									i = i + 3;
								}
							}
						}
						if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
									i++;
								}
							}
						}

					}

				}
				if (contract.getContractStartDate()
						.compareTo(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
					if (contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
						if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(totalContractCost);
							adminSpend = adminSpend.add(totalAdminContractCost);
						}
						if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusYears(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
									i++;
								}
							}
						}
						if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
							int i = 6;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
									i = i + 6;
								}
							}
						}
						if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
							int i = 3;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
									i = i + 3;
								}
							}
						}
						if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
									i++;
								}
							}
						}

					}
					if (contract.getContractEndDate()
							.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
						if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(totalContractCost);
							adminSpend = adminSpend.add(totalAdminContractCost);
						}
						if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusYears(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
									i++;
								}
							}
						}
						if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
							int i = 6;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
									i = i + 6;
								}
							}
						}
						if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
							int i = 3;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
									i = i + 3;
								}
							}
						}
						if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
									i++;
								}
							}
						}

					}
					if (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
						if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(totalContractCost);
							adminSpend = adminSpend.add(totalAdminContractCost);
						}
						if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusYears(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
									}
									i++;
								}
							}
						}
						if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
							int i = 6;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
									}
									i = i + 6;
								}
							}
						}
						if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
							int i = 3;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
									}
									i = i + 3;
								}
							}
						}
						if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
							adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
							int i = 1;
							while (i != 0) {
								LocalDate plusYears = localContractStart.plusMonths(i);
								if (CommonUtil.convertLocalDatetoDate(plusYears)
										.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
									i = 0;
								} else {
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
										adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(1)));
									}
									i++;
								}
							}
						}

					}
				}
			}
			if (ContractType.monthToMonth(contract.getContractType())) {
				if (contract.getContractStartDate()
						.compareTo(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
					totalSpend = totalSpend.add(totalContractCost);
					adminSpend = adminSpend.add(totalAdminContractCost);
				}
				if (contract.getContractStartDate()
						.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
					totalSpend = totalSpend.add(totalContractCost);
					adminSpend = adminSpend.add(totalAdminContractCost);
				}
				if (contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
						&& contract.getContractStartDate()
								.before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
					totalSpend = totalSpend.add(totalContractCost);
					adminSpend = adminSpend.add(totalAdminContractCost);
				}
			}

		}
		spend.put(Constant.ADMIN, adminSpend);
		spend.put(Constant.TOTAL, totalSpend);
		return spend;

	}

	@Override
	@Transactional
	public CommonResponse deleteAllByApplicationIds(ApplicationIdsRemoveRequest applicationIds)
			throws DataValidationException, JsonProcessingException {

		log.info("inside service method {}", "deleteAllByApplicationIds");
		ApplicationDetailsResponse applicationDetailsResponse = new ApplicationDetailsResponse();
		for (String applicationId : applicationIds.getApplicationIds()) {

			ApplicationDetails applicationDetails = applicationDetailsRepository.findByApplicationId(applicationId);
			List<Applications> apps = applicationRepository.findByApplicationId(applicationId);
			try {
				if (applicationDetails != null && applicationDetails.getApplicationName() != null
						&& applicationDetails.getApplicationName().equalsIgnoreCase("HubSpot")) {
					log.info("the application id is hubspot {}", applicationId);
					deleteUsersFromHubSpot(applicationId);
				}
				if (applicationDetails != null && applicationDetails.getApplicationName() != null
						&& applicationDetails.getApplicationName().equalsIgnoreCase("Microsoft365")) {
					log.info("the application id is Microsoft365 {}", applicationId);
					deleteUsersFromMicrosoft365(applicationId);
				}
				if (applicationDetails != null && applicationDetails.getApplicationName() != null
						&& applicationDetails.getApplicationName().equalsIgnoreCase(Constant.CONFLUENCE)) {
					log.info("the application id is Confluence {}", applicationId);
					deleteUsersFromConfluence(applicationId);
				}
				if (applicationDetails != null && applicationDetails.getApplicationName() != null
						&& applicationDetails.getApplicationName().equalsIgnoreCase(Constant.DATADOG)) {
					log.info("the application id is Datadog {}", applicationId);
					deleteUsersFromDatadog(applicationId);
				}
				if (applicationDetails != null && applicationDetails.getApplicationName() != null
						&& applicationDetails.getApplicationName().equalsIgnoreCase(Constant.ZOHOCRM)) {
					deleteUsersFromZohocrm(applicationId);
				}
				if (applicationDetails.getApplicationName() != null
						&& applicationDetails.getApplicationName().equalsIgnoreCase("Gitlab")) {
					deleteUsersFromGitlab(applicationDetails.getApplicationId());
				}
				if (applicationDetails.getApplicationName() != null
						&& applicationDetails.getApplicationName().equalsIgnoreCase("Github")) {
					deleteUserFromGithub(applicationDetails.getApplicationId());
				}
				if (applicationDetails.getApplicationName() != null
						&& applicationDetails.getApplicationName().equalsIgnoreCase(Constant.QUICKBOOOKS)) {
					deleteUsersFromQuickbooks(applicationDetails.getApplicationId());
				}
				if (applicationDetails != null && applicationDetails.getApplicationName() != null
						&& applicationDetails.getApplicationName().equalsIgnoreCase("JIRA")) {
					log.info("the application id is jira {}", applicationId);
					deleteUsersFromJira(applicationId);
				}
				if (applicationDetails != null && applicationDetails.getApplicationName() != null
						&& applicationDetails.getApplicationName().equalsIgnoreCase("Zoom")) {
					log.info("the application id is zoom {}", applicationId);
					deleteUsersFromZoom(applicationId);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			List<ContractOnboardingDetails> contractOnboardingDetails = contractsOnboardingRespository
					.findByApplicationId(applicationId);

			List<ContractOnboardingDetails> onboardingDetails = contractOnboardingDetails.stream()
					.filter(p -> p.getApprovedRejected().equalsIgnoreCase("Review")).collect(Collectors.toList());
			if (!onboardingDetails.isEmpty()) {
				throw new DataValidationException(
						"Contract under this application " + applicationId + " is still in review stage!!", null, null);
			}
			if (applicationDetails != null) {
				List<ApplicationContractDetails> applicationContractDetails = contractDetailsRepository
						.findExpiredContracts(applicationId);
				ApplicationSubscriptionDetails applicationSubscriptionDetails = applicationSubscriptionDetailsRepository
						.findByApplicationId(applicationId);
				for (ApplicationContractDetails contracts : applicationContractDetails) {
					for (ApplicationLicenseDetails license : contracts.getLicenseDetails()) {
						if (license != null && license.getLicenseId() != null) {
							license.setEndDate(new Date());
							licenseDetailsRepository.save(license);
						}
					}
				}
				for (ApplicationContractDetails contracts : applicationContractDetails) {
					if (contracts != null && contracts.getContractId() != null) {
						contracts.setEndDate(new Date());
						contractDetailsRepository.save(contracts);
					}
				}
				if (applicationSubscriptionDetails != null
						&& applicationSubscriptionDetails.getSubscriptionId() != null) {
					applicationSubscriptionDetails.setEndDate(new Date());
					applicationSubscriptionDetailsRepository.save(applicationSubscriptionDetails);
				}

				for (DepartmentDetails departmentDetails : applicationDetails.getDepartmentDetails()) {
					departmentDetails.getApplicationId().remove(applicationDetails);
				}
				for (UserDetails details : applicationDetails.getUserDetails()) {
					if (applicationDetails.getApplicationName().equalsIgnoreCase(Constant.ZOHOPEOPLE)) {
						deleteUsersFromZohoPeople(applicationDetails.getApplicationId(), details.getUserEmail());
					}
					if (applicationDetails.getApplicationName().equalsIgnoreCase(Constant.ZOHOANALYTICS)) {
						zohoAnalyticsService.revokeAccess(details.getUserEmail(),
								applicationDetails.getApplicationId());
					}
					if (applicationDetails.getApplicationName().equalsIgnoreCase(Constant.SALESFORCE)) {
						salesforceService.revokeAccess(applicationId, details.getUserEmail(), details.getUserId());
					}
					if (applicationDetails.getApplicationName().equalsIgnoreCase("Freshdesk")) {
						freshdeskService.revokeUserAccess(applicationId, details.getUserEmail());
					}
					for (ApplicationLicenseDetails licenseDetails : details.getLicenseId()) {
						if ("Expired".equalsIgnoreCase(licenseDetails.getContractId().getContractStatus())) {
							details.getApplicationId().remove(applicationDetails);
							licenseDetails.getUserId().remove(details);
						}

					}
				}
				for (Applications app : apps) {
					if (app.getSubscriptionId() != null) {
						app.setSubscriptionEndDate(new Date());
					}
					app.setEndDate(new Date());
					applicationRepository.save(app);
				}
				applicationDetails.setEndDate(new Date());
				applicationDetailsRepository.save(applicationDetails);
				applicationDetailsResponse.setMessage("Application with ID " + applicationId + " has removed");

			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("ApplicationDeleteResponse", null),
				"Details updated successfully");

	}

	@Override
	public CommonResponse getApplicationOverview(String applicationId, String category, UserLoginDetails profile) {
		ZoneId defaultZoneId = ZoneId.systemDefault();
		LocalDate today = LocalDate.now();
		Date currentDate = Date.from(today.atStartOfDay(defaultZoneId).toInstant());
		Response response = new Response();
		ApplicationDetails applicationDetails = applicationDetailsRepository.findByApplicationId(applicationId);
		ApplicationLogoEntity logoDetails = applicationLogoRepository
				.findByApplicationName(applicationDetails.getApplicationName());
		ApplicationProviderDetails providerDetails = providerDetailsRepository
				.findByProviderId(logoDetails.getProviderId().getProviderId());
		List<ApplicationContractDetails> applicationContractDetails = contractDetailsRepository
				.findRemaingContracts(applicationId, currentDate);
		List<ApplicationContractDetails> activeContracts = applicationContractDetails.stream()
				.filter(p -> p.getContractStatus().equalsIgnoreCase("Active")).collect(Collectors.toList());
		List<ApplicationContractDetails> contractsForTotalCost = contractDetailsRepository
				.findActiveExpiredContracts(applicationId);
		if (applicationDetails != null && StringUtils.isEmpty(category)) {
			ApplicationDetailsOverviewResponse detailsOverviewResponse = new ApplicationDetailsOverviewResponse();
			detailsOverviewResponse.setApplciationStatus(applicationDetails.getApplicationStatus());
			detailsOverviewResponse.setApplicationActiveContracts(activeContracts.size());
			detailsOverviewResponse
					.setApplicationCategory(applicationDetails.getApplicationCategoryMaster().getCategoryName());
			detailsOverviewResponse.setApplicationDescription(applicationDetails.getApplicationDescription());
			detailsOverviewResponse.setApplicationId(applicationDetails.getApplicationId());
			detailsOverviewResponse.setApplicationLogo(applicationDetails.getLogoUrl());
			detailsOverviewResponse.setApplicationName(applicationDetails.getApplicationName());
			List<ApplicationOwnerDetails> applicationOwners = applicationOwnerRepository
					.findByApplicationId(applicationId);
			List<newApplicationOwnerDetailsRequest> ownerDetails = new ArrayList<>();
			if (applicationDetails.getOwnerEmail() != null) {
				newApplicationOwnerDetailsRequest owner = new newApplicationOwnerDetailsRequest();
				owner.setApplicationOwnerEmail(applicationDetails.getOwnerEmail());
				ownerDetails.add(owner);
			} else {
				applicationOwners.forEach(p -> {
					newApplicationOwnerDetailsRequest owner = new newApplicationOwnerDetailsRequest();
					owner.setApplicaitonOwnerName(p.getOwner());
					owner.setApplicationOwnerEmail(p.getOwnerEmail());
					owner.setPriority(p.getPriority());
					ownerDetails.add(owner);
				});
			}
			detailsOverviewResponse.setOwnerDetails(ownerDetails);
			detailsOverviewResponse.setApplicationLink(logoDetails.getApplicationPageUrl());
			Integer count = 0;
			if (applicationDetails.getUserDetails() != null) {
				for (UserDetails userDetails : applicationDetails.getUserDetails()) {
					if (userDetails.getEndDate() == null) {
						count = count + 1;
					}
				}
			}

			String currency = null;
			Integer quantity = 0;
			Integer mapped = 0;
			Integer unMapped = 0;
			BigDecimal totalSpend = BigDecimal.valueOf(0.0);
			BigDecimal adminCost = BigDecimal.valueOf(0.0);
			BigDecimal totalAVSpend = BigDecimal.valueOf(0.0);
			BigDecimal adminAVSpend = BigDecimal.valueOf(0.0);
			int applicationAverageUsage = 0;
			for (ApplicationContractDetails contract : contractsForTotalCost) {
				BigDecimal totalCost = BigDecimal.valueOf(0.0);
				BigDecimal totalAdmin = BigDecimal.valueOf(0.0);
				for (ApplicationLicenseDetails licenseDetails : contract.getLicenseDetails()) {
					totalCost = totalCost.add(licenseDetails.getTotalCost(), MathContext.DECIMAL32);
					totalAdmin = totalAdmin.add(licenseDetails.getConvertedCost(), MathContext.DECIMAL32);
				}
				adminCost = adminCost.add(totalAdmin, MathContext.DECIMAL32);
				totalSpend = totalSpend.add(totalCost, MathContext.DECIMAL32);
				if (contract.getContractStatus().equalsIgnoreCase("ACTIVE")) {
					if (ContractType.annual(contract.getContractType())) {
						totalAVSpend = totalAVSpend.add(totalCost.divide(
								BigDecimal.valueOf(contract.getContractTenure()).multiply(BigDecimal.valueOf(12)), 2,
								RoundingMode.FLOOR));
						adminAVSpend = adminAVSpend.add(totalAdmin.divide(
								BigDecimal.valueOf(contract.getContractTenure()).multiply(BigDecimal.valueOf(12)), 2,
								RoundingMode.FLOOR));
					} else {
						totalAVSpend = totalAVSpend.add(totalCost);
						adminAVSpend = adminAVSpend.add(totalAdmin);
					}
				}
				currency = contract.getContractCurrency();
			}
			List<ApplicationContractDetails> activeContractsList = contractDetailsRepository
					.findActiveContracts(applicationDetails.getApplicationId());
			for (ApplicationContractDetails contract : activeContractsList) {
				for (ApplicationLicenseDetails details : contract.getLicenseDetails()) {
					quantity = quantity + details.getQuantity();
					mapped = mapped + details.getLicenseMapped();
					unMapped = unMapped + details.getLicenseUnMapped();
				}
				if (quantity != 0) {
					double percentage = (double) mapped / quantity * 100;
					applicationAverageUsage = (int) Math.round(percentage);
				}
				currency = contract.getContractCurrency();
			}
			if (activeContractsList.isEmpty()) {
				applicationAverageUsage = 0;
			}
			AdaptorDetails adaptor = adaptorDetailsRepository.findByApplicationId(applicationId);
			AdaptorCredential adaptoravailable = adaptorCredentialRepository
					.findByApplicationName(applicationDetails.getApplicationName());
			detailsOverviewResponse.setIsAdaptorAvailable(adaptoravailable != null);
			detailsOverviewResponse.setIsAdaptorConnected(adaptor != null);
			detailsOverviewResponse.setApplicationAvgUsage(applicationAverageUsage);
			detailsOverviewResponse.setCurrencyCode(currency);
			detailsOverviewResponse.setApplicationAvgMonthlySpend(totalAVSpend);
			detailsOverviewResponse.setApplicationTotalSpend(totalSpend);
			detailsOverviewResponse.setAdminCost(adminCost);
			detailsOverviewResponse.setAdminAvgCost(adminAVSpend);
			detailsOverviewResponse.setTotalLicenses(quantity);
			detailsOverviewResponse.setMappedLicenses(mapped);
			detailsOverviewResponse.setUnmappedLicenses(unMapped);
			detailsOverviewResponse.setApplicationActiveUserCount(count);
			detailsOverviewResponse.setApplicationProviderName(providerDetails.getProviderName());
			detailsOverviewResponse.setApplicationDepartment(applicationDetails.getOwnerDepartment());
			AuthenticationEntity authenticationEntity = authenticaionServiceRepository
					.findBySsoIdentityProvider("Azure AD");
			detailsOverviewResponse.setIsApplicationMapped(applicationDetails.getGraphApplicationId() != null);
			if (authenticationEntity != null) {
				detailsOverviewResponse.setIdentityProvider(authenticationEntity.getSsoIdentityProvider());
				detailsOverviewResponse.setIsSsoIntegrated(authenticationEntity.getIsConnected());
			}
			for (DepartmentDetails departmentDetails : applicationDetails.getDepartmentDetails()) {
				detailsOverviewResponse.setDepartmentId(departmentDetails.getDepartmentId());
			}
			response.setData(detailsOverviewResponse);
		}
		if (!StringUtils.isBlank(category) && !StringUtils.isEmpty(category) && category.equalsIgnoreCase("user")) {
			List<ApplicationDetailsUsersResponse> usersResponses = new ArrayList<>();
			for (UserDetails userDetails : applicationDetails.getUserDetails()) {
				if (userDetails.getEndDate() == null) {
					ApplicationDetailsUsersResponse detailsUsersResponse = new ApplicationDetailsUsersResponse();
					detailsUsersResponse.setUserId(userDetails.getUserId());
					detailsUsersResponse.setUserDesignation(userDetails.getUserDesigination());
					UserLastLoginDetails lastLoginDetails = userLastLoginDetailRepository
							.findByGraphUserIdAppId(userDetails.getIdentityId(), applicationDetails.getApplicationId());
					if (lastLoginDetails != null) {
						detailsUsersResponse.setUserLastLogin(lastLoginDetails.getLastLoginTime());
					} else {
						detailsUsersResponse.setUserLastLogin(userDetails.getLastLoginTime());
					}
					detailsUsersResponse.setUserEmail(userDetails.getUserEmail());
					detailsUsersResponse.setUserLogo(userDetails.getLogoUrl());
					detailsUsersResponse.setUserName(userDetails.getUserName());
					detailsUsersResponse.setUserStatus(userDetails.getUserStatus());
					usersResponses.add(detailsUsersResponse);

				}
			}
			response.setData(usersResponses);
		}
		if (!StringUtils.isBlank(category) && !StringUtils.isEmpty(category) && category.equalsIgnoreCase("license")) {
			List<ApplicationDetailsLicensesResponse> licensesResponses = new ArrayList<>();
			for (ApplicationContractDetails contract : applicationContractDetails) {
				if (contract.getContractStatus().equalsIgnoreCase("active")) {
					for (ApplicationLicenseDetails details : contract.getLicenseDetails()) {
						BigDecimal divide = details.getTotalCost().divide(details.getUnitPrice(), 2,
								RoundingMode.FLOOR);
						BigDecimal convertedPerLicenseCost = details.getConvertedCost().divide(divide, 2,
								RoundingMode.FLOOR);
						ApplicationDetailsLicensesResponse licensesResponse = new ApplicationDetailsLicensesResponse();
						if (applicationContractDetails != null) {
							licensesResponse.setContractId(contract.getContractId());
							licensesResponse.setContractName(contract.getContractName());
						}
						licensesResponse.setQuantity(details.getQuantity());
						licensesResponse.setMappedLicenses(details.getLicenseMapped());
						licensesResponse.setUnmappedLicenses(details.getLicenseUnMapped());
						licensesResponse.setLicenseId(details.getLicenseId());
						licensesResponse.setCurrencyCode(contract.getContractCurrency());
						licensesResponse.setUnitPrice(details.getUnitPrice());
						licensesResponse.setUnitPriceType(details.getUnitPriceType());
						licensesResponse.setProductType(details.getProductCategory());
						licensesResponse.setProductName(details.getProductName());
						licensesResponse.setTotalCost(details.getTotalCost());
						licensesResponse.setAdminCost(details.getConvertedCost());
						licensesResponse.setAdminUnitCost(convertedPerLicenseCost);
						licensesResponses.add(licensesResponse);
					}
				}
			}

			response.setData(licensesResponses);
		}
		response.setAction("applicationOverviewResponse");
		return new CommonResponse(HttpStatus.OK, response, Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	@Transactional
	public CommonResponse newApplicationOnboarding(SingleNewApplicationOnboardingRequest onboardingRequest,
			UserLoginDetails profile) throws DataValidationException, JsonGenerationException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		NewApplicationOnboardingResposne onboardingResposne = new NewApplicationOnboardingResposne();
		Integer childNum = 1;
		String request = Constant.REQUEST_APP;
		Integer sequence = sequenceGeneratorRepository.getRequestNumberSequence();
		request = request.concat(sequence.toString());
		for (NewSingeApplicationOnboardingRequest applicationOnboardingRequest : onboardingRequest
				.getCreateApplicationRequestNew()) {
			List<String> appName = applicationLogoRepository.findApplicationName();
			if (appName.stream()
					.noneMatch(applicationOnboardingRequest.getApplicationInfo().getApplicationName()::equals)) {
				throw new DataValidationException("Application with name "
						+ applicationOnboardingRequest.getApplicationInfo().getApplicationName()
						+ Constant.MISMATCH_ERROR, "400", HttpStatus.BAD_REQUEST);
			}
			if (departmentRepository.findByDepartmentName(
					applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment()) == null) {
				throw new DataValidationException(Constant.DEPARTMENT_WITH_NAME
						+ applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment()
						+ Constant.NOT_EXIST, null, null);
			}
			if (projectDetailsRepository.findByProjectName(
					applicationOnboardingRequest.getApplicationInfo().getProjectName().trim()) == null) {
				throw new DataValidationException(Constant.PROJECT_WITH_NAME
						+ applicationOnboardingRequest.getApplicationInfo().getProjectName() + Constant.NOT_EXIST, null,
						null);
			}
			if (departmentRepository.findByDepartmentName(
					applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment()) != null) {
				DepartmentDetails departmentDetails = departmentRepository.findByDepartmentName(
						applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment());
				if (projectDetailsRepository.findByProjectIdAndDeptId(
						applicationOnboardingRequest.getApplicationInfo().getProjectName().trim(),
						departmentDetails.getDepartmentId()) == null) {
					throw new DataValidationException(Constant.DEPARTMENT_PROJECT
							+ applicationOnboardingRequest.getApplicationInfo().getProjectName() + Constant.NOT_EXIST,
							null, null);
				}
				Integer i = 0;
				List<String> owneremails = new ArrayList<>();
				for (UserDetails userDetails : departmentDetails.getUserDetails()) {
					for (newApplicationOwnerDetailsRequest ownerDeetails : applicationOnboardingRequest
							.getApplicationInfo().getOwnerDetails()) {
						if (userDetails.getUserEmail().trim()
								.equalsIgnoreCase(ownerDeetails.getApplicationOwnerEmail())) {
							owneremails.add(ownerDeetails.getApplicationOwnerEmail());
							i++;
						}
					}
				}
				if (i == 0) {
					throw new DataValidationException(Constant.APPLICATION_EMAIL_ADDRESS
							+ String.join(", ", owneremails) + Constant.FOR_DEPARTMENT_WITH_NAME
							+ applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment()
							+ Constant.MISMATCH_ERROR, null, null);
				}
			}
			ApplicationOnboarding list = applicationOnboardingRepository.findByAppNameandDeptNameandProjectName(
					applicationOnboardingRequest.getApplicationInfo().getApplicationName().trim(),
					applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment().trim(),
					applicationOnboardingRequest.getApplicationInfo().getProjectName());
			List<ApplicationOnboarding> apps = new ArrayList<>();
			ApplicationDetails deletedApplication = null;
			if (list != null) {
				List<ApplicationOnboarding> loop = new ArrayList<>();
				loop.add(list);
				List<ApplicationOnboarding> applicationOnboardings = getApplicationStatus(loop);
				apps = applicationOnboardings.stream()
						.filter(p -> p.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW))
						.collect(Collectors.toList());
				deletedApplication = applicationDetailsRepository.getDeletedApplicationByAppNameAndProjectName(
						applicationOnboardingRequest.getApplicationInfo().getApplicationName().trim(),
						applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment().trim(),
						applicationOnboardingRequest.getApplicationInfo().getProjectName());
			}
			if (apps.isEmpty() && deletedApplication == null) {
				ApplicationOnboarding applicationOnboarding = new ApplicationOnboarding();
				String childRequestNum = null;
				childRequestNum = request.concat("_0" + childNum);
				childNum++;
				ObjectMapper obj = new ObjectMapper();
				String objToString;
				try {
					objToString = obj.writeValueAsString(applicationOnboardingRequest);
					applicationOnboarding.setApplcationOnboardRequest(objToString);
				} catch (JsonProcessingException e) {
					throw new JsonGenerationException(e, null);
				}
				PurchasedSingleAppOnboardingRequest applicationObject = applicationObjectDeserializer(objToString);
				try {
					applicatoinObjectValidator(applicationObject);
				} catch (DataValidationException e) {
					throw new DataValidationException(e.getMessage(), null, null);
				}
				List<NewAppLicenseInfo> appLicenseInfos = applicationOnboardingRequest.getProducts();
				for (NewAppLicenseInfo licenseInfo : appLicenseInfos) {
					if (Constant.PRODUCT_TYPE.stream().noneMatch(licenseInfo.getProductType()::equals)) {
						throw new DataValidationException("product type does not match", "400", HttpStatus.BAD_REQUEST);
					}
					if (Constant.UNIT_PRICE.stream().noneMatch(licenseInfo.getUnitPriceType()::equals)) {
						throw new DataValidationException(
								"Unit price " + licenseInfo.getUnitPriceType() + Constant.MISMATCH_ERROR, "400",
								HttpStatus.BAD_REQUEST);
					}
					if (Constant.CURRENCY.stream().noneMatch(licenseInfo.getCurrencyCode()::equals)) {
						throw new DataValidationException(Constant.CURRENCY_MISMATCH, "400", HttpStatus.BAD_REQUEST);
					}
				}
				applicationOnboarding.setApplicationName(
						applicationOnboardingRequest.getApplicationInfo().getApplicationName().trim());
				applicationOnboarding.setRequestNumber(request);
				applicationOnboarding.setCreatedOn(new Date());

				if (onboardingRequest.getCreateApplicationRequestNew().size() != 1) {
					applicationOnboarding.setChildRequestNumber(childRequestNum);
				}
				applicationOnboarding.setOwnerDepartment(
						applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment().trim());
				applicationOnboarding.setBuID(Constant.BUID);
				applicationOnboarding.setCreatedBy(profile.getEmailAddress());
				applicationOnboarding.setApprovedRejected(Constant.REVIEW);
				applicationOnboarding.setOpID(Constant.SAASPE);
				applicationOnboarding.setOnboardingStatus(Constant.PENDING_WITH_REVIEWER);
				applicationOnboarding.setOnboardedByUserEmail(profile.getEmailAddress());
				applicationOnboarding.setWorkGroup(Constant.REVIEWER);
				applicationOnboarding.setWorkGroupUserEmail(profile.getEmailAddress());
				applicationOnboarding
						.setProjectName(applicationOnboardingRequest.getApplicationInfo().getProjectName());
				applicationOnboardingRepository.save(applicationOnboarding);
			} else {
				return new CommonResponse(HttpStatus.CONFLICT, new Response("createApplicationResponse", response),
						"Application With Name "
								+ applicationOnboardingRequest.getApplicationInfo().getApplicationName()
								+ " Already Exists");
			}
			onboardingResposne.setRequestId(request);
			SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
			updateSequence.setRequestId(++sequence);
			sequenceGeneratorRepository.save(updateSequence);
			response.setData(onboardingResposne);
			response.setAction("createApplicationResponse");
			commonResponse.setStatus(HttpStatus.CREATED);
			commonResponse.setMessage("Onboarding request submitted");
			commonResponse.setResponse(response);
		}
		return commonResponse;
	}

	@Override
	@Transactional
	public CommonResponse purchasedApplicationOnboard(PurchasedApplcationRequest onboardingRequest,
			UserLoginDetails profile) throws DataValidationException, JsonGenerationException, ParseException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		NewApplicationOnboardingResposne onboardingResposne = new NewApplicationOnboardingResposne();
		Integer childNum = 1;
		String request = Constant.REQUEST_APP;
		Integer sequence = sequenceGeneratorRepository.getRequestNumberSequence();
		request = request.concat(sequence.toString());
		for (PurchasedSingleAppOnboardingRequest applicationOnboardingRequest : onboardingRequest
				.getCreateApplicationRequest()) {
			List<String> appName = applicationLogoRepository.findApplicationName();
			if (appName.stream()
					.noneMatch(applicationOnboardingRequest.getApplicationInfo().getApplicationName()::equals)) {
				throw new DataValidationException("Application with name "
						+ applicationOnboardingRequest.getApplicationInfo().getApplicationName()
						+ Constant.MISMATCH_ERROR, "400", HttpStatus.BAD_REQUEST);
			}
			if (departmentRepository.findByDepartmentName(
					applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment().trim()) == null) {
				throw new DataValidationException(Constant.DEPARTMENT_WITH_NAME
						+ applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment()
						+ Constant.NOT_EXIST, null, null);
			}
			if (projectDetailsRepository.findByProjectName(
					applicationOnboardingRequest.getApplicationInfo().getProjectName().trim()) == null) {
				throw new DataValidationException(Constant.PROJECT_WITH_NAME
						+ applicationOnboardingRequest.getApplicationInfo().getProjectName() + Constant.NOT_EXIST, null,
						null);
			}
			if (applicationSubscriptionDetailsRepository.findBySubscriptionNumber(
					applicationOnboardingRequest.getApplicationInfo().getSubscriptionId()) != null) {
				throw new DataValidationException("Subscription with Id "
						+ applicationOnboardingRequest.getApplicationInfo().getSubscriptionId() + " Already Exist",
						null, null);
			}
			if (departmentRepository.findByDepartmentName(
					applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment()) != null) {
				DepartmentDetails departmentDetails = departmentRepository.findByDepartmentName(
						applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment());
				if (projectDetailsRepository.findByProjectIdAndDeptId(
						applicationOnboardingRequest.getApplicationInfo().getProjectName().trim(),
						departmentDetails.getDepartmentId()) == null) {
					throw new DataValidationException(Constant.DEPARTMENT_PROJECT
							+ applicationOnboardingRequest.getApplicationInfo().getProjectName() + Constant.NOT_EXIST,
							null, null);
				}
				Integer i = 0;
				List<String> owneremails = new ArrayList<>();
				for (UserDetails userDetails : departmentDetails.getUserDetails()) {
					for (newApplicationOwnerDetailsRequest ownerDeetails : applicationOnboardingRequest
							.getApplicationInfo().getOwnerDetails()) {
						if (userDetails.getUserEmail().trim()
								.equalsIgnoreCase(ownerDeetails.getApplicationOwnerEmail())) {
							owneremails.add(ownerDeetails.getApplicationOwnerEmail());
							i++;
						}
					}
				}
				if (i == 0) {
					throw new DataValidationException(Constant.APPLICATION_EMAIL_ADDRESS + String.join(",", owneremails)
							+ Constant.FOR_DEPARTMENT_WITH_NAME
							+ applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment()
							+ Constant.MISMATCH_ERROR, null, null);
				}
			}
			List<String> contractDuplicate = new ArrayList<>();
			for (NewAppContractInfo contract : applicationOnboardingRequest.getContractInfo()) {
				contractDuplicate.add(contract.getContractName());
				if (contractDetailsRepository.findByContractName(contract.getContractName().trim()) != null) {
					throw new DataValidationException(
							"Contract with " + contract.getContractName().trim() + " Already exist", null, null);
				}
			}
			List<String> contract = contractDuplicate.stream().distinct().collect(Collectors.toList());
			if (contract.size() != contractDuplicate.size()) {
				throw new DataValidationException("Duplicate Contract Name exist", null, null);
			}

			ApplicationOnboarding list = applicationOnboardingRepository.findByAppNameandDeptNameandProjectName(
					applicationOnboardingRequest.getApplicationInfo().getApplicationName().trim(),
					applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment().trim(),
					applicationOnboardingRequest.getApplicationInfo().getProjectName());
			List<ApplicationOnboarding> apps = new ArrayList<>();
			ApplicationDetails deletedApplication = null;
			if (list != null) {
				List<ApplicationOnboarding> loop = new ArrayList<>();
				loop.add(list);
				List<ApplicationOnboarding> applicationOnboardings = getApplicationStatus(loop);
				apps = applicationOnboardings.stream()
						.filter(p -> p.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW))
						.collect(Collectors.toList());
				deletedApplication = applicationDetailsRepository.getDeletedApplicationByAppNameAndProjectName(
						applicationOnboardingRequest.getApplicationInfo().getApplicationName().trim(),
						applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment().trim(),
						applicationOnboardingRequest.getApplicationInfo().getProjectName());
			}
			if (apps.isEmpty() && deletedApplication == null) {
				ApplicationOnboarding applicationOnboarding = new ApplicationOnboarding();
				String childRequestNum = null;
				childRequestNum = request.concat("_0" + childNum);
				childNum++;
				ObjectMapper obj = new ObjectMapper();
				String objToString;
				try {
					objToString = obj.writeValueAsString(applicationOnboardingRequest);
					applicationOnboarding.setApplcationOnboardRequest(objToString);
				} catch (JsonProcessingException e) {
					throw new JsonGenerationException(e, null);
				}
				PurchasedSingleAppOnboardingRequest applicationObject = applicationObjectDeserializer(objToString);
				try {
					applicatoinObjectValidator(applicationObject);
					applicatoinContractValidator(applicationObject);
				} catch (DataValidationException e) {
					throw new DataValidationException(e.getMessage(), null, null);
				}
				applicationOnboarding.setApplicationName(
						applicationOnboardingRequest.getApplicationInfo().getApplicationName().trim());
				applicationOnboarding.setRequestNumber(request);
				applicationOnboarding.setCreatedOn(new Date());
				if (onboardingRequest.getCreateApplicationRequest().size() != 1) {
					applicationOnboarding.setChildRequestNumber(childRequestNum);
				}
				applicationOnboarding.setOwnerDepartment(
						applicationOnboardingRequest.getApplicationInfo().getApplicationOwnerDepartment().trim());
				applicationOnboarding.setBuID(Constant.BUID);
				applicationOnboarding.setCreatedBy(profile.getEmailAddress());
				applicationOnboarding.setOpID(Constant.SAASPE);
				applicationOnboarding.setApprovedRejected(Constant.REVIEW);
				applicationOnboarding.setOnboardingStatus("Pending With Reviewer");
				applicationOnboarding.setOnboardedByUserEmail(profile.getEmailAddress());
				applicationOnboarding.setWorkGroup(Constant.REVIEWER);
				applicationOnboarding.setWorkGroupUserEmail(profile.getEmailAddress());
				applicationOnboarding
						.setProjectName(applicationOnboardingRequest.getApplicationInfo().getProjectName());
				applicationOnboardingRepository.save(applicationOnboarding);
			} else {
				response.setData("");
				response.setAction("purchasedApplicationResponse");
				commonResponse.setStatus(HttpStatus.CONFLICT);
				commonResponse.setMessage("Application with Name  "
						+ applicationOnboardingRequest.getApplicationInfo().getApplicationName()
						+ "  Already Exists OR In the Stage Of WorkFlow");
				commonResponse.setResponse(response);
				return commonResponse;
			}
			SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
			updateSequence.setRequestId(++sequence);
			sequenceGeneratorRepository.save(updateSequence);
		}
		onboardingResposne.setRequestId(request);
		return new CommonResponse(HttpStatus.CREATED, new Response("purchasedApplicationResponse", onboardingResposne),
				"Onboarding request submitted");
	}

	@Override
	@Transactional
	public CommonResponse saveApplicatoinOnboarding(MultipartFile applicationFile, UserLoginDetails profile)
			throws IOException, DataValidationException, JSONException, ParseException {
		CommonResponse commonResponse;
		ObjectMapper mapper = new ObjectMapper();
		List<String> errors = new ArrayList<>();
		NewApplicationOnboardingResposne applicationOnboardingResposne = new NewApplicationOnboardingResposne();
		List<JSONObject> list = new ArrayList<>();
		XSSFWorkbook workbook = null;
		List<String> headers = new ArrayList<>();
		Integer childNum = 1;
		String request = Constant.REQUEST_APP;
		Integer appReqSequence = sequenceGeneratorRepository.getRequestNumberSequence();
		SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
		request = request.concat(appReqSequence.toString());
		try {
			String filename = applicationFile.getName().toLowerCase();
			if (filename != null) {
				workbook = new XSSFWorkbook(applicationFile.getInputStream());
				for (int i = 0; i < 1; i++) {
					XSSFSheet sheet = workbook.getSheetAt(i);
					int l = 1;
					for (int j = 0; j <= sheet.getLastRowNum(); j++) {
						XSSFRow row = sheet.getRow(j);
						if (row != null) {
							if (j == 0) {
								for (int k = 0; k < 32; k++) {
									headers.add(row.getCell(k).getStringCellValue());
								}
							} else {
								JSONObject jsonObject = new JSONObject();
								for (int k = 0; k < headers.size(); k++) {
									Cell cell = row.getCell(k);

									String headerName = headers.get(k).trim();
									if (cell != null) {
										jsonObject.put(Constant.ROW_NUMBER, l);
										switch (cell.getCellType()) {
										case FORMULA:
											jsonObject.put(headerName.replaceAll("\\s", ""),
													cell.getCellFormula().trim());
											break;
										case BOOLEAN:
											jsonObject.put(headerName.replaceAll("\\s", ""),
													cell.getBooleanCellValue());
											break;
										case NUMERIC:
											double s = cell.getNumericCellValue();
											int value = (int) s;
											jsonObject.put(headerName.replaceAll("\\s", ""),
													String.valueOf(value).trim());
											break;
										case BLANK:
											jsonObject.put(headerName.replaceAll("\\s", ""), "");
											break;
										default:
											jsonObject.put(headerName.replaceAll("\\s", ""),
													cell.getStringCellValue().trim());
											break;
										}
									} else {
										jsonObject.put(headerName.replaceAll("\\s", ""), "");
									}
								}
								list.add(jsonObject);
								l++;
							}
						}
					}
				}

			} else {
				return new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response(Constant.EXCEL_UPLOAD_APPLICATION, Arrays.asList("File format not supported.")),
						Constant.EXCEL_UPLOAD_FAILED);
			}
		} catch (Exception e) {
			throw new DataValidationException(e.getMessage(), null, HttpStatus.BAD_REQUEST);
		}
		if (!headers.contains("Application Name")) {
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(Constant.EXCEL_UPLOAD_APPLICATION,
					Arrays.asList("Seems Your Uploading Wrong Excel File")), Constant.EXCEL_UPLOAD_FAILED);
		}
		int i = 1;
		for (JSONObject checkObject : list) {
			List<String> objectError = new ArrayList<>();
			int empty = 0;
			if (checkObject.get(Constant.APPLICATION_NAME).toString().length() == 0) {
				objectError.add("ApplicationName Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.APP_CATEGORY).toString().length() == 0) {
				objectError.add("ApplicationCategory Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.APP_OWNER_NAME).toString().length() == 0) {
				objectError.add("ApplicationOwnerName Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.APP_OWNER_EMAIL).toString().length() == 0) {
				objectError.add("ApplicationOwnerEmailAddress Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.OWNER_DEPT).toString().length() == 0) {
				objectError.add("OwnerDepartment Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.PROJECT_NAME).toString().length() == 0) {
				objectError.add("ProjectName Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get("SubscriptionName").toString().length() == 0) {
				objectError.add("SubscriptionName Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.SUB_ID).toString().length() == 0) {
				objectError.add("SubscriptionId Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.PROVIDER_NAME).toString().length() == 0) {
				objectError.add("ProviderName Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.CONT_NAME).toString().length() == 0) {
				objectError.add("ContractName Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.CONT_START_DATE).toString().length() == 0) {
				objectError.add("ContractStartDate Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.CONT_END_DATE).toString().length() == 0) {
				objectError.add("ContractEndDate Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.AUTO_RENEWAL).toString().length() == 0) {
				objectError.add("AutoRenewal Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.PROD_TYPE).toString().length() == 0) {
				objectError.add("ProductType Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.PROD_NAME).toString().length() == 0) {
				objectError.add("ProductName Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.UNIT_PRICE_STRING).toString().length() == 0) {
				objectError.add("UnitPrice Empty at Row " + i);
				empty = empty + 1;
			}
			if (excelNumberFormatValidation(checkObject.get(Constant.UNIT_PRICE_STRING).toString()).equals(false)) {
				objectError.add("Provide valid UnitPrice at Row " + i);
			}
			if (checkObject.get(Constant.UNIT_PRICE_TYPE).toString().length() == 0) {
				objectError.add("UnitPriceType Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.QUANTITY).toString().length() == 0) {
				objectError.add("Quantity Empty at Row " + i);
				empty = empty + 1;
			}
			if (excelNumberFormatValidation(checkObject.get(Constant.QUANTITY).toString()).equals(false)) {
				objectError.add("Provide valid Quantity at Row " + i);
			}
			if (checkObject.get(Constant.TOTAL_COST).toString().length() == 0) {
				objectError.add("TotalCost Empty at Row " + i);
				empty = empty + 1;
			}
			if (excelNumberFormatValidation(checkObject.get(Constant.TOTAL_COST).toString()).equals(false)) {
				objectError.add("Provide valid TotalCost at Row " + i);
			}
			if (checkObject.get(Constant.CONT_TYPE).toString().length() == 0) {
				objectError.add("ContractType Empty at Row " + i);
				empty = empty + 1;
			}
			if (checkObject.get(Constant.CURRENCY_STRING).toString().length() == 0) {
				objectError.add("Currency Empty at Row " + i);
				empty = empty + 1;
			}
			if (!ContractType.monthToMonth(checkObject.get(Constant.CONT_TYPE).toString())) {
				if (checkObject.get(Constant.BILL_FREQUENCY).toString().length() == 0) {
					objectError.add("BillingFrequency Empty at Row " + i);
					empty = empty + 1;
				}
				if (checkObject.get(Constant.CONT_TENURE).toString().length() == 0) {
					objectError.add("ContractTenure(Years) Empty at Row " + i);
					empty = empty + 1;
					if (excelNumberFormatValidation(checkObject.get(Constant.CONT_TENURE).toString()).equals(false)) {
						objectError.add("Provide valid ContractTenure(Years) Empty at Row " + i);
					}

				}
			} else {
				if (checkObject.get(Constant.UNIT_PRICE_TYPE).toString().equalsIgnoreCase(Constant.PER_YEAR)) {
					objectError.add(
							"UnitPriceType Should be per month or per contract tenure when Contract is Month-to-Month at row"
									+ i);
				}
			}
			if (checkObject.get(Constant.AUTO_RENEWAL).equals("On")) {
				if (checkObject.get(Constant.NEXT_RENEWAL).toString().length() == 0) {
					objectError.add("NextRenewalDate Empty at Row " + i);
					empty = empty + 1;
				}
				if (checkObject.get(Constant.PAY_METHOD).toString().trim().length() != 0) {
					if (checkObject.get(Constant.PAY_METHOD).toString().trim().equalsIgnoreCase("wallet")) {
						if (checkObject.get(Constant.WALLET_NAME).toString().length() == 0) {
							objectError.add("Wallet Name Empty at Row " + i);
							empty = empty + 1;
						}
					} else {
						if (checkObject.get(Constant.CARD_HOLDER_NAME).toString().length() == 0) {
							objectError.add("Valid Through Empty at Row " + i);
							empty = empty + 1;
						}
						if (checkObject.get(Constant.CARD_NUMBER).toString().length() == 0) {
							objectError.add("Card Number Empty at Row " + i);
							empty = empty + 1;
						}
						if (checkObject.get(Constant.VALID_THROUGH).toString().length() == 0) {
							objectError.add("Valid Through Empty at Row " + i);
							empty = empty + 1;
						}
					}
					if (!ContractType.monthToMonth(checkObject.get(Constant.CONT_TYPE).toString())
							&& (checkObject.get(Constant.CANCELLATION_NOTICE).toString().length() == 0)) {
						objectError.add("CancellationNotice(Days) Empty at Row " + i);
						empty = empty + 1;
						if (Boolean.FALSE.equals(excelNumberFormatValidation(
								checkObject.get(Constant.CANCELLATION_NOTICE).toString()))) {
							objectError.add("Provide valid CancellationNotice(Days) Empty at Row " + i);
						}

					}
				} else {
					objectError.add("Payment Method Empty at Row " + i);
					empty = empty + 1;
				}
			}
			if (i == 1 && empty == 23) {
				workbook.close();
				return new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response(Constant.EXCEL_UPLOAD_APPLICATION, Arrays.asList("Please Insert Date Correctly")),
						Constant.EXCEL_UPLOAD_FAILED);
			}
			if (i >= 1 && empty < 23) {
				errors.addAll(objectError);
			}
			i++;
		}
		if (!errors.isEmpty()) {
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(Constant.EXCEL_UPLOAD_APPLICATION, errors),
					Constant.EXCEL_UPLOAD_FAILED);
		}
		for (JSONObject jsonObject : list) {
			String applicationName = jsonObject.get(Constant.APPLICATION_NAME).toString();
			String ownerDepartment = jsonObject.get(Constant.OWNER_DEPT).toString();

			List<ApplicationDetails> existingAppDetailsList = applicationDetailsRepository
					.findByApplicationNameAndOwnerDept(applicationName, ownerDepartment);

			if (!existingAppDetailsList.isEmpty()) {
				String errorMessage = Constant.APPLN + applicationName + " already exists for department "
						+ ownerDepartment + " in ApplicationDetails at row "
						+ jsonObject.get(Constant.ROW_NUMBER).toString();
				if (!errors.contains(errorMessage)) {
					errors.add(errorMessage);
				}
			}

			List<ApplicationOnboarding> existingApps = applicationOnboardingRepository
					.findByApplicationNameAndOwnerDept(applicationName, ownerDepartment);

			if (!existingApps.isEmpty() && existingApps.stream().anyMatch(p -> p.getEndDate() == null)) {
				String errorMessage = Constant.APPLN + applicationName + " already exists for department "
						+ ownerDepartment + " in ApplicationOnboarding at row "
						+ jsonObject.get(Constant.ROW_NUMBER).toString();
				if (!errors.contains(errorMessage)) {
					errors.add(errorMessage);
				}
			}
		}
		for (JSONObject contractDateValids : list) {
			String stratDate = contractDateValids.get(Constant.CONT_START_DATE).toString();
			String endDate = contractDateValids.get(Constant.CONT_END_DATE).toString();
			Date contractStartDate = DateParser.parse(stratDate);
			Date contractEndtDate = DateParser.parse(endDate);
			if (ContractType.annual(contractDateValids.get(Constant.CONT_TYPE).toString().trim())) {
				Integer tenure = Integer.parseInt(contractDateValids.get(Constant.CONT_TENURE).toString().trim());
				if (tenure != null) {
					Calendar contractStart = dateToCalendar(contractStartDate);
					contractStart.add(Calendar.YEAR, tenure);
					Date actualEndDate = contractStart.getTime();
					LocalDate localDateContractEndDate = CommonUtil.dateToLocalDate(actualEndDate);
					Date dateConverted = CommonUtil.convertLocalDatetoDate(localDateContractEndDate.minusDays(1));
					if (CommonUtil.simpleDateFormat(contractEndtDate).after(dateConverted)) {
						errors.add("For " + contractDateValids.get(Constant.CONT_NAME).toString()
								+ " ContractEndDate is grater than ContarctTenure = "
								+ contractDateValids.get(Constant.CONT_TENURE).toString().trim() + "at row "
								+ contractDateValids.get(Constant.ROW_NUMBER).toString());
					} else if (CommonUtil.simpleDateFormat(contractEndtDate).before(dateConverted)) {
						errors.add("For " + contractDateValids.get(Constant.CONT_NAME).toString()
								+ " ContractEndDate is less than ContarctTenure = "
								+ contractDateValids.get(Constant.CONT_TENURE).toString().trim() + "at row "
								+ contractDateValids.get(Constant.ROW_NUMBER).toString());
					}
				}
			} else if (ContractType.monthToMonth(contractDateValids.get(Constant.CONT_TYPE).toString())) {
				LocalDate localDateContractEndDate = CommonUtil.dateToLocalDate(contractStartDate);
				int days = CommonUtil.getDaysBasedOnDate(localDateContractEndDate);
				Date dateConverted = CommonUtil
						.convertLocalDatetoDate(localDateContractEndDate.plusDays(days).minusDays(1));
				if (contractStartDate == null || contractEndtDate == null) {
					errors.add("Contract Start date or End date is either null or entered incorrectly " + endDate);
				} else if (CommonUtil.simpleDateFormat(contractEndtDate).after(dateConverted)) {
					errors.add("For " + contractDateValids.get(Constant.CONT_NAME).toString()
							+ " ContractEndDate is grater than expected EndDate");
				} else if (CommonUtil.simpleDateFormat(contractEndtDate).before(dateConverted)) {
					errors.add("For " + contractDateValids.get(Constant.CONT_NAME).toString()
							+ "  ContractEndDate is less than expected EndDate");
				}
			}
		}
		List<JSONObject> sameAppsDetProjDistinct = new ArrayList<>();
		List<ApplicationWorkFlowDetailsView> flowDetailsViewList = new ArrayList<>();
		List<JSONObject> duplicheck = new ArrayList<>();
		for (JSONObject object : list) {
			List<JSONObject> sameAppandContList = list.stream()
					.filter(p -> p.get(Constant.APPLICATION_NAME).equals(object.get(Constant.APPLICATION_NAME))
							&& p.get(Constant.CONT_NAME).equals(object.get(Constant.CONT_NAME)))
					.collect(Collectors.toList());
			if (sameAppandContList.size() > 1 && duplicheck.stream()
					.filter(p -> p.get(Constant.APPLICATION_NAME).equals(object.get(Constant.APPLICATION_NAME))
							&& p.get(Constant.CONT_NAME).equals(object.get(Constant.CONT_NAME)))
					.collect(Collectors.toList()).isEmpty()) {
				Collections.sort(sameAppandContList, Comparator.comparing(json -> json.getInt(Constant.ROW_NUMBER)));
				JSONObject contractConstant = sameAppandContList.get(0);

				for (JSONObject objCheck : sameAppandContList) {
					duplicheck.add(objCheck);
					if (contractConstant.get(Constant.CONT_TYPE).toString().length() > 0
							&& !contractConstant.get(Constant.CONT_TYPE).toString()
									.equalsIgnoreCase(objCheck.get(Constant.CONT_TYPE).toString())) {
						errors.add("Contract type Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
					if (!contractConstant.get(Constant.CONT_START_DATE).toString()
							.equalsIgnoreCase(objCheck.get(Constant.CONT_START_DATE).toString())) {
						errors.add("ContractStartDate Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
					if (!contractConstant.get(Constant.CONT_END_DATE).toString()
							.equalsIgnoreCase(objCheck.get(Constant.CONT_END_DATE).toString())) {
						errors.add("ContractEndDate Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
					if (!contractConstant.get(Constant.BILL_FREQUENCY).toString()
							.equalsIgnoreCase(objCheck.get(Constant.BILL_FREQUENCY).toString())) {
						errors.add("BillingFrequency Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
					if (!contractConstant.get(Constant.CONT_TENURE).toString()
							.equalsIgnoreCase(objCheck.get(Constant.CONT_TENURE).toString())) {
						errors.add("ContractTenure Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
					if (!contractConstant.get(Constant.AUTO_RENEWAL).toString()
							.equalsIgnoreCase(objCheck.get(Constant.AUTO_RENEWAL).toString())) {
						errors.add("AutoRenewal Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
					if (!contractConstant.get(Constant.WALLET_NAME).toString()
							.equalsIgnoreCase(objCheck.get(Constant.WALLET_NAME).toString())) {
						errors.add("WalletName Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
					if (!contractConstant.get(Constant.PAY_METHOD).toString()
							.equalsIgnoreCase(objCheck.get(Constant.PAY_METHOD).toString())) {
						errors.add("PaymentMethod Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
					if (!contractConstant.get(Constant.NEXT_RENEWAL).toString()
							.equalsIgnoreCase(objCheck.get(Constant.NEXT_RENEWAL).toString())) {
						errors.add("NextRenewalDate Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
					if (!contractConstant.get(Constant.CARD_NUMBER).toString()
							.equalsIgnoreCase(objCheck.get(Constant.CARD_NUMBER).toString())) {
						errors.add("CardNumber Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
					if (!contractConstant.get(Constant.VALID_THROUGH).toString()
							.equalsIgnoreCase(objCheck.get(Constant.VALID_THROUGH).toString())) {
						errors.add("ValidThrough Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
					if (!contractConstant.get(Constant.CARD_HOLDER_NAME).toString()
							.equalsIgnoreCase(objCheck.get(Constant.CARD_HOLDER_NAME).toString())) {
						errors.add("CardholderName Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
					if (!contractConstant.get(Constant.CANCELLATION_NOTICE).toString()
							.equalsIgnoreCase(objCheck.get(Constant.CANCELLATION_NOTICE).toString())) {
						errors.add("NextRenewalDate Missmatch in Row " + objCheck.get(Constant.ROW_NUMBER).toString()
								+ Constant.FOR_CONTRACT + objCheck.get(Constant.CONT_NAME).toString());
					}
				}
			}
			if (object.get(Constant.APPLICATION_NAME).toString().length() != 0) {
				List<JSONObject> sameAppsDeptProj = list.stream()
						.filter(p -> p.get(Constant.APPLICATION_NAME).equals(object.get(Constant.APPLICATION_NAME))
								&& p.get(Constant.OWNER_DEPT).equals(object.get(Constant.OWNER_DEPT))
								&& p.get(Constant.PROJECT_NAME).equals(object.get(Constant.PROJECT_NAME)))
						.collect(Collectors.toList());
				List<JSONObject> appDeptProjChcek = sameAppsDetProjDistinct.stream()
						.filter(p -> p.get(Constant.APPLICATION_NAME).equals(object.get(Constant.APPLICATION_NAME))
								&& p.get(Constant.OWNER_DEPT).equals(object.get(Constant.OWNER_DEPT))
								&& p.get(Constant.PROJECT_NAME).equals(object.get(Constant.PROJECT_NAME)))
						.collect(Collectors.toList());
				if (appDeptProjChcek.isEmpty()) {
					for (JSONObject jsonObject : sameAppsDeptProj) {
						if (jsonObject.get(Constant.SECONDARY_OWNER_EMAIL).toString().length() != 0
								&& (jsonObject.get(Constant.APP_OWNER_EMAIL).toString()
										.equalsIgnoreCase(jsonObject.get(Constant.SECONDARY_OWNER_EMAIL).toString()))) {
							errors.add("Application Owner primary Email addresss "
									+ jsonObject.get(Constant.APP_OWNER_EMAIL).toString()
									+ " and Secondary owner Email address"
									+ jsonObject.get(Constant.SECONDARY_OWNER_EMAIL).toString() + " are same at row "
									+ jsonObject.get(Constant.ROW_NUMBER).toString());

						}
						if (departmentRepository
								.findByDepartmentName(jsonObject.get(Constant.OWNER_DEPT).toString()) == null) {
							errors.add(Constant.DEPARTMENT_WITH_NAME + jsonObject.get(Constant.OWNER_DEPT).toString()
									+ Constant.NOT_EXIST + Constant.AT_ROW
									+ jsonObject.get(Constant.ROW_NUMBER).toString());
						}
						if (projectDetailsRepository
								.findByProjectName(jsonObject.get(Constant.PROJECT_NAME).toString()) == null) {
							errors.add(Constant.PROJECT_WITH_NAME + jsonObject.get(Constant.PROJECT_NAME).toString()
									+ Constant.NOT_EXIST + Constant.AT_ROW
									+ jsonObject.get(Constant.ROW_NUMBER).toString());
						}
						if (applicationSubscriptionDetailsRepository
								.findBySubscriptionNumber(jsonObject.get(Constant.SUB_ID).toString()) != null) {
							errors.add("Subscriptiion with Id " + jsonObject.get(Constant.SUB_ID).toString()
									+ " Already Exist" + Constant.AT_ROW
									+ jsonObject.get(Constant.ROW_NUMBER).toString());
						}

						if (departmentRepository
								.findByDepartmentName(jsonObject.get(Constant.OWNER_DEPT).toString()) != null) {
							DepartmentDetails departmentDetails = departmentRepository
									.findByDepartmentName(jsonObject.get(Constant.OWNER_DEPT).toString());
							if (projectDetailsRepository.findByProjectIdAndDeptId(
									jsonObject.get(Constant.PROJECT_NAME).toString(),
									departmentDetails.getDepartmentId()) == null) {
								errors.add(Constant.DEPARTMENT_PROJECT
										+ jsonObject.get(Constant.PROJECT_NAME).toString() + Constant.NOT_EXIST
										+ Constant.AT_ROW + jsonObject.get(Constant.ROW_NUMBER).toString());
							}

							List<UserDetails> activeuserslist = departmentDetails.getUserDetails().stream()
									.filter(s -> s.getEndDate() == null).collect(Collectors.toList());
							boolean foundApplicationOwner = false;
							boolean foundSecondaryOwner = false;
							for (UserDetails userDetails : activeuserslist) {
								String userEmail = userDetails.getUserEmail().trim();
								if (userEmail
										.equalsIgnoreCase(jsonObject.get(Constant.APP_OWNER_EMAIL).toString().trim())) {
									foundApplicationOwner = true;
								}
								if (userEmail.equalsIgnoreCase(
										jsonObject.get(Constant.SECONDARY_OWNER_EMAIL).toString().trim())) {
									foundSecondaryOwner = true;
								}

								if (foundApplicationOwner && foundSecondaryOwner) {
									break;
								}
							}
							if (!foundApplicationOwner) {
								errors.add("Application Owner with Email address "
										+ jsonObject.get(Constant.APP_OWNER_EMAIL).toString()
										+ Constant.FOR_DEPARTMENT_WITH_NAME
										+ jsonObject.get(Constant.OWNER_DEPT).toString() + Constant.MISMATCH_ERROR
										+ Constant.AT_ROW + jsonObject.get(Constant.ROW_NUMBER).toString());
							}
							if (!foundSecondaryOwner
									&& jsonObject.get(Constant.SECONDARY_OWNER_EMAIL).toString().length() != 0) {
								errors.add("Secondary Owner with Email address "
										+ jsonObject.get(Constant.SECONDARY_OWNER_EMAIL).toString()
										+ Constant.FOR_DEPARTMENT_WITH_NAME
										+ jsonObject.get(Constant.OWNER_DEPT).toString() + Constant.MISMATCH_ERROR
										+ Constant.AT_ROW + jsonObject.get(Constant.ROW_NUMBER).toString());
							}
						}
						ApplicationDetails applicationCheck = applicationDetailsRepository
								.getDeletedApplicationByAppName(jsonObject.get(Constant.APPLICATION_NAME).toString(),
										jsonObject.get(Constant.OWNER_DEPT).toString());
						if (applicationCheck != null && applicationCheck.getProjectName()
								.equalsIgnoreCase(jsonObject.get(Constant.PROJECT_NAME).toString())) {
							errors.add(Constant.APPLN + jsonObject.get(Constant.APPLICATION_NAME).toString()
									+ " for Department " + jsonObject.get(Constant.OWNER_DEPT).toString()
									+ " Already Exists" + Constant.AT_ROW
									+ jsonObject.get(Constant.ROW_NUMBER).toString());
						}
						ApplicationOnboarding applist = applicationOnboardingRepository
								.findByAppNameandDeptNameandProjectName(
										jsonObject.get(Constant.APPLICATION_NAME).toString(),
										jsonObject.get(Constant.OWNER_DEPT).toString(),
										jsonObject.get(Constant.PROJECT_NAME).toString());

						List<ApplicationOnboarding> apps = new ArrayList<>();
						ApplicationDetails deletedApplication = null;
						if (applist != null) {
							List<ApplicationOnboarding> loop = new ArrayList<>();
							loop.add(applist);
							List<ApplicationOnboarding> applicationOnboardings = getApplicationStatus(loop);
							apps = applicationOnboardings.stream()
									.filter(p -> p.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW))
									.collect(Collectors.toList());
							deletedApplication = applicationDetailsRepository
									.getDeletedApplicationByAppNameAndProjectName(
											jsonObject.get(Constant.APPLICATION_NAME).toString(),
											jsonObject.get(Constant.OWNER_DEPT).toString(),
											jsonObject.get(Constant.PROJECT_NAME).toString());

						}
						if (!apps.isEmpty() && deletedApplication != null) {
							errors.add("Application with Name " + jsonObject.get(Constant.APPLICATION_NAME).toString()
									+ " Already Exists Or In The Stage of Workflow " + " at row "
									+ jsonObject.get(Constant.ROW_NUMBER).toString());
						}
						if (categoryMasterRepository
								.findByCategoryName(jsonObject.get(Constant.APP_CATEGORY).toString()) == null) {
							errors.add(Constant.CATEGORY_WITH_NAME + jsonObject.get(Constant.APP_CATEGORY).toString()
									+ Constant.NOT_EXIST + Constant.AT_ROW
									+ jsonObject.get(Constant.ROW_NUMBER).toString());
						} else if (userDetailsRepository
								.findByuserEmail(jsonObject.get(Constant.APP_OWNER_EMAIL).toString().trim()) == null) {
							errors.add(Constant.USER_WITH_EMAIL + jsonObject.get(Constant.APP_OWNER_EMAIL).toString()
									+ Constant.NOT_EXIST + Constant.AT_ROW
									+ jsonObject.get(Constant.ROW_NUMBER).toString());
						} else if (departmentRepository
								.findByDepartmentName(jsonObject.get(Constant.OWNER_DEPT).toString()) != null) {
							DepartmentDetails departmentDetails = departmentRepository
									.findByDepartmentName(jsonObject.get(Constant.OWNER_DEPT).toString());

							UserDetails userList = userDetailsRepository.findByDepartmentIdAndUserEmail(
									departmentDetails.getDepartmentId(),
									jsonObject.get(Constant.APP_OWNER_EMAIL).toString().trim());

							if (userList == null) {
								errors.add("User Email with department is not matched at row "
										+ jsonObject.get(Constant.ROW_NUMBER).toString());
							} else if (!userList.getUserName().replaceAll(Constant.URI, "").equalsIgnoreCase(
									jsonObject.get(Constant.APP_OWNER_NAME).toString().replaceAll(Constant.URI, ""))) {
								errors.add("user name is not matched with user details at row "
										+ jsonObject.get(Constant.ROW_NUMBER).toString());
							}
							if (jsonObject.get(Constant.SECONDARY_OWNER_EMAIL).toString().length() != 0) {
								UserDetails userList2 = userDetailsRepository.findByDepartmentIdAndUserEmail(
										departmentDetails.getDepartmentId(),
										jsonObject.get(Constant.SECONDARY_OWNER_EMAIL).toString().trim());
								if (userList2 == null) {
									errors.add("User Email with department does not matched at row "
											+ jsonObject.get(Constant.ROW_NUMBER).toString());
								} else if (!userList2.getUserName().replaceAll(Constant.URI, "")
										.equalsIgnoreCase(jsonObject.get("SecondaryOwnerName").toString().trim())) {
									errors.add("user name not matched with user details at row "
											+ jsonObject.get(Constant.ROW_NUMBER).toString());
								}
							}
						} else if (providerDetailsRepository
								.findByProviderName(jsonObject.get(Constant.PROVIDER_NAME).toString()) == null) {
							errors.add("Provider Name with " + jsonObject.get(Constant.PROVIDER_NAME).toString()
									+ " Doesn't Exist at row " + jsonObject.get(Constant.ROW_NUMBER).toString());
						}
						int quantity = Integer.parseInt(jsonObject.get(Constant.QUANTITY).toString());
						BigDecimal unitPrice = jsonObject.getBigDecimal(Constant.UNIT_PRICE_STRING);
						BigDecimal totalCost = jsonObject.getBigDecimal(Constant.TOTAL_COST);
						BigDecimal priceCheck = unitPrice.multiply(BigDecimal.valueOf(quantity));
						if (priceCheck.compareTo(totalCost) != 0) {
							errors.add("Invalid total cost at row " + jsonObject.get(Constant.ROW_NUMBER).toString());
						}
						String productType = jsonObject.get(Constant.PROD_TYPE).toString();
						if (((productType.equals("Professional Services")) || (productType.equals("Platform"))
								|| (productType.equals("ProfessionalServices"))) && quantity != 1) {
							errors.add("Product Type as " + productType + " can't have more than 1 quantity at row "
									+ jsonObject.get(Constant.ROW_NUMBER).toString());
						}
					}
					ApplicationWorkFlowDetailsView workFlowDetailsView = new ApplicationWorkFlowDetailsView();
					List<NewAppLicenseInfo> licenseInfos = new ArrayList<>();
					List<NewAppContractInfo> contractInfos = new ArrayList<>();
					NewAppOnboardInfo applicationInfo = new NewAppOnboardInfo();
					List<newApplicationOwnerDetailsRequest> detailsRequests = new ArrayList<>();
					newApplicationOwnerDetailsRequest primarydetailsRequest = new newApplicationOwnerDetailsRequest();
					primarydetailsRequest.setApplicaitonOwnerName(object.get(Constant.APP_OWNER_NAME).toString());
					primarydetailsRequest.setApplicationOwnerEmail(object.get(Constant.APP_OWNER_EMAIL).toString());
					detailsRequests.add(primarydetailsRequest);
					if (object.get(Constant.SECONDARY_OWNER_EMAIL).toString().length() != 0) {
						newApplicationOwnerDetailsRequest secondarydetailsRequest = new newApplicationOwnerDetailsRequest();
						secondarydetailsRequest.setApplicaitonOwnerName(object.get("SecondaryOwnerName").toString());
						secondarydetailsRequest
								.setApplicationOwnerEmail(object.get(Constant.SECONDARY_OWNER_EMAIL).toString());
						detailsRequests.add(secondarydetailsRequest);
					}
					applicationInfo.setOwnerDetails(detailsRequests);
					applicationInfo.setApplicationCategory(object.get(Constant.APP_CATEGORY).toString());
					ApplicationLogoEntity logoEntity = applicationLogoRepository
							.findByApplicationName(object.get(Constant.APPLICATION_NAME).toString().trim());
					applicationInfo.setApplicationLogoUrl(logoEntity.getLogoUrl());
					applicationInfo.setApplicationName(object.get(Constant.APPLICATION_NAME).toString());
					applicationInfo.setApplicationOwnerDepartment(object.get(Constant.OWNER_DEPT).toString());
					applicationInfo.setApplicationProviderName(object.get(Constant.PROVIDER_NAME).toString());
					applicationInfo.setProjectName(object.get(Constant.PROJECT_NAME).toString());
					applicationInfo.setSubscriptionName(object.get("SubscriptionName").toString());
					applicationInfo.setSubscriptionNumber(object.get(Constant.SUB_ID).toString());
					applicationInfo.setSubscriptionId(object.get(Constant.SUB_ID).toString());
					for (JSONObject contract : sameAppsDeptProj) {
						NewAppLicenseInfo licenseInfo = new NewAppLicenseInfo();
						List<JSONObject> contLice = sameAppsDeptProj.stream()
								.filter(p -> p.get(Constant.CONT_NAME).equals(contract.get(Constant.CONT_NAME))
										&& p.get(Constant.PROD_NAME).equals(contract.get(Constant.PROD_NAME)))
								.collect(Collectors.toList());
						ApplicationContractDetails contractDetails = contractDetailsRepository
								.findByContractName(contract.get(Constant.CONT_NAME).toString());
						if (contractDetails != null) {
							errors.add("Contract With " + contract.get(Constant.CONT_NAME).toString()
									+ " name Already exist at row " + contract.get(Constant.ROW_NUMBER).toString());
						}
						List<NewAppContractInfo> contractCheck = contractInfos.stream().filter(
								p -> p.getContractName().equalsIgnoreCase(contract.get(Constant.CONT_NAME).toString()))
								.collect(Collectors.toList());
						if (contLice.size() == 1) {
							if (contractCheck.isEmpty()) {
								NewAppContractInfo contractInfo = new NewAppContractInfo();
								if (contract.get(Constant.AUTO_RENEWAL).equals("On")) {
									if (contract.get(Constant.PAY_METHOD).toString().equalsIgnoreCase("wallet")) {
										contractInfo.setWalletName(contract.get(Constant.WALLET_NAME).toString());
									} else {
										contractInfo.setCardNumber(Long.valueOf(
												contract.get(Constant.CARD_NUMBER).toString().trim().replace(" ", "")));
										contractInfo.setValidThrough(contract.get(Constant.VALID_THROUGH).toString());
										contractInfo.setPaymentMethod(contract.get(Constant.PAY_METHOD).toString());
										contractInfo
												.setCardHolderName(contract.get(Constant.CARD_HOLDER_NAME).toString());
									}
									if (!ContractType.monthToMonth(contract.get(Constant.CONT_TYPE).toString())) {
										contractInfo.setAutoRenewalCancellation(
												Integer.valueOf(contract.get(Constant.CANCELLATION_NOTICE).toString()));
									}

									contractInfo.setAutoRenewal(true);
									String renewalDate = contract.get(Constant.NEXT_RENEWAL).toString();
									Date renewal = DateParser.parse(renewalDate);
									contractInfo.setUpcomingRenewalDate(renewal);
								} else {
									contractInfo.setAutoRenewal(false);
								}
								contractInfo.setContractName(contract.get(Constant.CONT_NAME).toString());
								contractInfo.setContractType(contract.get(Constant.CONT_TYPE).toString());
								if (!ContractType.monthToMonth(contract.get(Constant.CONT_TYPE).toString())) {
									contractInfo.setBillingFrequency(contract.get(Constant.BILL_FREQUENCY).toString());
									contractInfo.setContractTenure(contract.get(Constant.CONT_TENURE).toString());

								}
								String contractStartDate = contract.get(Constant.CONT_START_DATE).toString();
								Date startDate = DateParser.parse(contractStartDate);
								String contractEndDate = contract.get(Constant.CONT_END_DATE).toString();
								Date endDate = DateParser.parse(contractEndDate);
								contractInfo.setContractStartDate(startDate);
								contractInfo.setContractEndDate(endDate);
								contractInfo.setCurrencyCode(contract.get(Constant.CURRENCY_STRING).toString());
								contractInfos.add(contractInfo);
							}
							licenseInfo.setProductName(contract.get(Constant.PROD_NAME).toString());
							licenseInfo.setContractName(contract.get(Constant.CONT_NAME).toString());
							licenseInfo.setProductType(contract.get(Constant.PROD_TYPE).toString());
							licenseInfo
									.setUnitPrice(new BigDecimal(contract.get(Constant.UNIT_PRICE_STRING).toString()));
							licenseInfo.setTotalCost(new BigDecimal(contract.get(Constant.TOTAL_COST).toString()));
							licenseInfo.setQuantity(Integer.valueOf(contract.get(Constant.QUANTITY).toString()));
							licenseInfo.setCurrencyCode(contract.get(Constant.CURRENCY_STRING).toString());
							licenseInfo.setUnitPriceType(contract.get(Constant.UNIT_PRICE_TYPE).toString());
							licenseInfos.add(licenseInfo);
						} else {
							String number = "";
							for (JSONObject contError : contLice) {
								number = number.concat(" " + contError.get(Constant.ROW_NUMBER).toString());
							}
							errors.add("Same Contract And License at Rows " + number);
						}
					}
					sameAppsDetProjDistinct.addAll(sameAppsDeptProj);
					workFlowDetailsView.setApplicationInfo(applicationInfo);
					workFlowDetailsView.setContractInfo(contractInfos);
					workFlowDetailsView.setProducts(licenseInfos);
					flowDetailsViewList.add(workFlowDetailsView);
				}
			}
		}
		List<ApplicationOnboarding> onboarding = new ArrayList<>();
		for (ApplicationWorkFlowDetailsView app : flowDetailsViewList) {
			String childRequestNum = null;
			childRequestNum = request.concat("_0" + childNum);
			childNum++;
			ApplicationOnboarding applicationOnboarding = new ApplicationOnboarding();
			String saveRequest = mapper.writeValueAsString(app);
			applicationOnboarding.setOwnerDepartment(app.getApplicationInfo().getApplicationOwnerDepartment());
			applicationOnboarding.setApplicationName(app.getApplicationInfo().getApplicationName());
			applicationOnboarding.setApplcationOnboardRequest(saveRequest);
			applicationOnboarding.setCreatedOn(new Date());
			applicationOnboarding.setBuID(Constant.BUID);
			applicationOnboarding.setOnboardedByUserEmail(profile.getEmailAddress());
			applicationOnboarding.setOpID(Constant.SAASPE);
			applicationOnboarding.setWorkGroup(Constant.REVIEWER);
			applicationOnboarding.setApprovedRejected(Constant.REVIEW);
			applicationOnboarding.setOnboardingStatus("Pending With Reviewer");
			applicationOnboarding.setProjectName(app.getApplicationInfo().getProjectName());
			applicationOnboarding.setCreatedBy(profile.getEmailAddress());
			applicationOnboarding.setWorkGroupUserEmail(profile.getEmailAddress());
			applicationOnboarding.setRequestNumber(request);
			if (flowDetailsViewList.size() != 1) {
				applicationOnboarding.setChildRequestNumber(childRequestNum);
			}
			onboarding.add(applicationOnboarding);
		}

		if (errors.isEmpty()) {
			updateSequence.setRequestId(++appReqSequence);
			sequenceGeneratorRepository.save(updateSequence);
			for (ApplicationOnboarding applicationOnboarding : onboarding) {
				applicationOnboardingRepository.save(applicationOnboarding);
			}
			applicationOnboardingResposne.setRequestId(request);
			commonResponse = new CommonResponse(HttpStatus.CREATED,
					new Response(Constant.EXCEL_UPLOAD_APPLICATION, applicationOnboardingResposne),
					"Application Excel Upload Success");
		} else {
			commonResponse = new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.EXCEL_UPLOAD_APPLICATION, errors), Constant.EXCEL_UPLOAD_FAILED);
		}
		return commonResponse;
	}

	private Boolean excelNumberFormatValidation(String s) {
		boolean flag = true;
		try {
			BigDecimal number = new BigDecimal(s);
			if (number.compareTo(BigDecimal.ZERO) < 0) {
				flag = false;
			}
		} catch (NumberFormatException e) {
			flag = false;
		}
		return flag;
	}

	@Override
	public CommonResponse applicatoinReviewerApproverListView(UserLoginDetails profile) {
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
			List<ApplicationOnboardingListViewResponse> listOfDept = getListOfApplications(Constant.REVIEWER,
					Constant.REVIEW);
			return new CommonResponse(HttpStatus.OK,
					new Response(Constant.APPLICATION_ONBOARDING_LIST_VIEW_RESPONSE, listOfDept),
					Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER)) {
			List<ApplicationOnboardingListViewResponse> listOfDept = getListOfApplications(Constant.APPROVER,
					Constant.REVIEW);
			return new CommonResponse(HttpStatus.OK,
					new Response(Constant.APPLICATION_ONBOARDING_LIST_VIEW_RESPONSE, listOfDept),
					Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
		}
		List<ApplicationOnboarding> applicationOnboardng = applicationOnboardingRepository.findAllSuperAdminListView();
		List<ApplicationOnboardingListViewResponse> list = new ArrayList<>();
		for (ApplicationOnboarding deptonboarding : applicationOnboardng) {
			ApplicationOnboardingListViewResponse listViewResponse = new ApplicationOnboardingListViewResponse();
			listViewResponse.setApplicationName(deptonboarding.getApplicationName());
			listViewResponse.setOnboardedByEmail(deptonboarding.getOnboardedByUserEmail());
			listViewResponse.setRequestId(deptonboarding.getRequestNumber());
			ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
					.findByApplicationName(deptonboarding.getApplicationName());
			listViewResponse.setApplicationLogo(applicationLogoEntity.getLogoUrl());
			listViewResponse.setChildRequestId(deptonboarding.getChildRequestNumber());
			listViewResponse.setOnboardingStatus(deptonboarding.getOnboardingStatus());
			if (deptonboarding.getChildRequestNumber() != null) {
				listViewResponse.setChildRequestId(deptonboarding.getChildRequestNumber());
			}
			list.add(listViewResponse);
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.APPLICATION_ONBOARDING_LIST_VIEW_RESPONSE, list),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private List<ApplicationOnboardingListViewResponse> getListOfApplications(String role, String key) {
		List<ApplicationOnboarding> applicationOnboardings = applicationOnboardingRepository.getAllByName(role, key);
		List<ApplicationOnboardingListViewResponse> list = new ArrayList<>();
		for (ApplicationOnboarding applicationOnboarding : applicationOnboardings) {
			ApplicationOnboardingListViewResponse listViewResponse = new ApplicationOnboardingListViewResponse();
			listViewResponse.setApplicationName(applicationOnboarding.getApplicationName());
			listViewResponse.setOnboardedByEmail(applicationOnboarding.getOnboardedByUserEmail());
			listViewResponse.setRequestId(applicationOnboarding.getRequestNumber());
			ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
					.findByApplicationName(applicationOnboarding.getApplicationName());
			listViewResponse.setApplicationLogo(applicationLogoEntity.getLogoUrl());
			listViewResponse.setChildRequestId(applicationOnboarding.getChildRequestNumber());
			listViewResponse.setOnboardingStatus(applicationOnboarding.getOnboardingStatus());
			if (role.equalsIgnoreCase(Constant.APPROVER)) {
				listViewResponse.setReviewedByEmail(applicationOnboarding.getWorkGroupUserEmail());
			}
			if (applicationOnboarding.getChildRequestNumber() != null) {
				listViewResponse.setChildRequestId(applicationOnboarding.getChildRequestNumber());
			}
			list.add(listViewResponse);
		}
		return list;
	}

	@Override
	public CommonResponse applicatoinReviewerApproverDetailsView(String childRequestId, String requestId,
			UserLoginDetails profile)
			throws URISyntaxException, StorageException, JsonGenerationException, DataValidationException {
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		ApplicationWorkFlowDetailsView detailViewResponse = new ApplicationWorkFlowDetailsView();
		DepartmentReviewerDetails reviewerDetails = new DepartmentReviewerDetails();
		List<SupportDocumentsResponse> list = new ArrayList<>();
		ApplicationOnboarding applicationDetail = applicationOnboardingRepository.findByRequest(requestId,
				childRequestId);
		if ((applicationDetail.getApprovedRejected().equalsIgnoreCase(Constant.APPROVE)
				|| applicationDetail.getApprovedRejected().equalsIgnoreCase(Constant.REJECTED))
				|| (applicationDetail.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW)
						&& applicationDetail.getWorkGroup().equalsIgnoreCase(Constant.APPROVER)
						&& (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)))) {
			throw new DataValidationException("Onboarding flow for the requested application is completed already",
					null, HttpStatus.NO_CONTENT);
		}
		SupportDocumentsResponse documentsResponse = new SupportDocumentsResponse();
		if (requestId != null) {
			documentsResponse.setFileUrl(getBlobURI(supportingDocsPath + requestId));
			list.add(documentsResponse);
		} else {
			int underScoreLastIndex = childRequestId.lastIndexOf("_");
			String reqId = childRequestId.substring(0, underScoreLastIndex);
			documentsResponse.setFileUrl(getBlobURI(supportingDocsPath + reqId));
			list.add(documentsResponse);
		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
			if (requestId != null && childRequestId == null) {
				ApplicationOnboarding applicationReviewer = applicationOnboardingRepository
						.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
				PurchasedSingleAppOnboardingRequest applicationDetails = applicationObjectDeserializer(
						applicationReviewer.getApplcationOnboardRequest());
				NewAppOnboardInfo info = new NewAppOnboardInfo();
				info.setOwnerDetails(applicationDetails.getApplicationInfo().getOwnerDetails());
				info.setApplicationCategory(applicationDetails.getApplicationInfo().getApplicationCategory());
				info.setApplicationJustification(applicationDetails.getApplicationInfo().getApplicationJustification());
				info.setApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
				info.setApplicationOwnerDepartment(
						applicationDetails.getApplicationInfo().getApplicationOwnerDepartment());
				info.setApplicationProviderName(applicationDetails.getApplicationInfo().getApplicationProviderName());
				info.setSubscriptionNumber(applicationDetails.getApplicationInfo().getSubscriptionId());
				info.setSubscriptionName(applicationDetails.getApplicationInfo().getSubscriptionName());
				ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
						.findByApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
				info.setApplicationLogoUrl(applicationLogoEntity.getLogoUrl());
				info.setProjectName(applicationDetails.getApplicationInfo().getProjectName());

				detailViewResponse.setApplicationInfo(info);
				detailViewResponse.setProducts(applicationDetails.getProducts());
				detailViewResponse.setContractInfo(applicationDetails.getContractInfo());

				detailViewResponse.setSupportingDocsInfo(list);
			}
			if (requestId == null && childRequestId != null) {
				ApplicationOnboarding applicationReviewer = applicationOnboardingRepository
						.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
				PurchasedSingleAppOnboardingRequest applicationDetails = applicationObjectDeserializer(
						applicationReviewer.getApplcationOnboardRequest());
				NewAppOnboardInfo info = new NewAppOnboardInfo();
				info.setOwnerDetails(applicationDetails.getApplicationInfo().getOwnerDetails());
				info.setApplicationCategory(applicationDetails.getApplicationInfo().getApplicationCategory());
				info.setApplicationJustification(applicationDetails.getApplicationInfo().getApplicationJustification());
				info.setApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
				info.setApplicationOwnerDepartment(
						applicationDetails.getApplicationInfo().getApplicationOwnerDepartment());
				info.setApplicationProviderName(applicationDetails.getApplicationInfo().getApplicationProviderName());
				info.setSubscriptionNumber(applicationDetails.getApplicationInfo().getSubscriptionId());
				info.setSubscriptionName(applicationDetails.getApplicationInfo().getSubscriptionName());
				ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
						.findByApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
				info.setApplicationLogoUrl(applicationLogoEntity.getLogoUrl());
				info.setProjectName(applicationDetails.getApplicationInfo().getProjectName());

				detailViewResponse.setApplicationInfo(info);
				detailViewResponse.setProducts(applicationDetails.getProducts());
				detailViewResponse.setContractInfo(applicationDetails.getContractInfo());
				detailViewResponse.setSupportingDocsInfo(list);
			}
		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER)) {
			if (requestId != null && childRequestId == null) {
				ApplicationOnboarding departmentApprover = applicationOnboardingRepository
						.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
				ApplicationOnboarding departmentReviewer = applicationOnboardingRepository
						.findByRequestNumber(requestId, Constant.REVIEWER, Constant.APPROVE);
				PurchasedSingleAppOnboardingRequest applicationDetails = applicationObjectDeserializer(
						departmentApprover.getApplcationOnboardRequest());
				reviewerDetails.setApprovedByEmail(departmentReviewer.getWorkGroupUserEmail());
				reviewerDetails.setWorkGroupName(departmentReviewer.getWorkGroup());
				reviewerDetails.setComments(departmentReviewer.getComments());
				reviewerDetails.setApprovalTimeStamp(departmentReviewer.getEndDate());

				NewAppOnboardInfo info = new NewAppOnboardInfo();
				info.setOwnerDetails(applicationDetails.getApplicationInfo().getOwnerDetails());
				info.setApplicationCategory(applicationDetails.getApplicationInfo().getApplicationCategory());
				info.setApplicationJustification(applicationDetails.getApplicationInfo().getApplicationJustification());
				info.setApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
				info.setApplicationOwnerDepartment(
						applicationDetails.getApplicationInfo().getApplicationOwnerDepartment());
				info.setApplicationProviderName(applicationDetails.getApplicationInfo().getApplicationProviderName());
				info.setSubscriptionNumber(applicationDetails.getApplicationInfo().getSubscriptionId());
				info.setSubscriptionName(applicationDetails.getApplicationInfo().getSubscriptionName());
				ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
						.findByApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
				info.setApplicationLogoUrl(applicationLogoEntity.getLogoUrl());
				info.setProjectName(applicationDetails.getApplicationInfo().getProjectName());
				detailViewResponse.setApplicationInfo(info);

				detailViewResponse.setProducts(applicationDetails.getProducts());
				detailViewResponse.setContractInfo(applicationDetails.getContractInfo());
				detailViewResponse.setSupportingDocsInfo(list);
				detailViewResponse.setReviewerDetails(reviewerDetails);
			}
			if (requestId == null && childRequestId != null) {

				ApplicationOnboarding departmentApprover = applicationOnboardingRepository
						.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
				ApplicationOnboarding departmentReviewer = applicationOnboardingRepository
						.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.APPROVE);
				PurchasedSingleAppOnboardingRequest applicationDetails = applicationObjectDeserializer(
						departmentApprover.getApplcationOnboardRequest());
				reviewerDetails.setApprovedByEmail(departmentReviewer.getWorkGroupUserEmail());
				reviewerDetails.setWorkGroupName(departmentReviewer.getWorkGroup());
				reviewerDetails.setComments(departmentReviewer.getComments());
				reviewerDetails.setApprovalTimeStamp(departmentReviewer.getEndDate());

				NewAppOnboardInfo info = new NewAppOnboardInfo();
				info.setOwnerDetails(applicationDetails.getApplicationInfo().getOwnerDetails());
				info.setApplicationCategory(applicationDetails.getApplicationInfo().getApplicationCategory());
				info.setApplicationJustification(applicationDetails.getApplicationInfo().getApplicationJustification());
				info.setApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
				info.setApplicationOwnerDepartment(
						applicationDetails.getApplicationInfo().getApplicationOwnerDepartment());
				info.setApplicationProviderName(applicationDetails.getApplicationInfo().getApplicationProviderName());
				info.setSubscriptionNumber(applicationDetails.getApplicationInfo().getSubscriptionId());
				info.setSubscriptionName(applicationDetails.getApplicationInfo().getSubscriptionName());
				ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
						.findByApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
				info.setApplicationLogoUrl(applicationLogoEntity.getLogoUrl());
				info.setProjectName(applicationDetails.getApplicationInfo().getProjectName());

				detailViewResponse.setApplicationInfo(info);
				detailViewResponse.setProducts(applicationDetails.getProducts());
				detailViewResponse.setContractInfo(applicationDetails.getContractInfo());
				detailViewResponse.setSupportingDocsInfo(list);
				detailViewResponse.setReviewerDetails(reviewerDetails);
			}
		}
		if (userDetails.getUserRole().equalsIgnoreCase("super_admin")) {
			if (requestId != null && childRequestId == null) {
				ApplicationOnboarding departmentApprover = applicationOnboardingRepository
						.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
				ApplicationOnboarding departmentReviewerApproved = applicationOnboardingRepository
						.findByRequestNumber(requestId, Constant.REVIEWER, Constant.APPROVE);
				ApplicationOnboarding departmentReviewer = applicationOnboardingRepository
						.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
				if (departmentApprover != null) {
					PurchasedSingleAppOnboardingRequest applicationDetails = applicationObjectDeserializer(
							departmentApprover.getApplcationOnboardRequest());
					detailViewResponse.setApplicationInfo(applicationDetails.getApplicationInfo());
					NewAppOnboardInfo info = new NewAppOnboardInfo();
					info.setOwnerDetails(applicationDetails.getApplicationInfo().getOwnerDetails());
					info.setApplicationCategory(applicationDetails.getApplicationInfo().getApplicationCategory());
					info.setApplicationJustification(
							applicationDetails.getApplicationInfo().getApplicationJustification());
					info.setApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
					info.setApplicationOwnerDepartment(
							applicationDetails.getApplicationInfo().getApplicationOwnerDepartment());
					info.setApplicationProviderName(
							applicationDetails.getApplicationInfo().getApplicationProviderName());
					info.setSubscriptionNumber(applicationDetails.getApplicationInfo().getSubscriptionId());
					info.setSubscriptionName(applicationDetails.getApplicationInfo().getSubscriptionName());
					ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
							.findByApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
					info.setApplicationLogoUrl(applicationLogoEntity.getLogoUrl());
					info.setProjectName(applicationDetails.getApplicationInfo().getProjectName());

					detailViewResponse.setApplicationInfo(info);
					detailViewResponse.setProducts(applicationDetails.getProducts());
					detailViewResponse.setContractInfo(applicationDetails.getContractInfo());
					detailViewResponse.setSupportingDocsInfo(list);
					reviewerDetails.setApprovedByEmail(departmentReviewerApproved.getWorkGroupUserEmail());
					reviewerDetails.setWorkGroupName(departmentReviewerApproved.getWorkGroup());
					reviewerDetails.setComments(departmentReviewerApproved.getComments());
					reviewerDetails.setApprovalTimeStamp(departmentReviewerApproved.getEndDate());
					detailViewResponse.setReviewerDetails(reviewerDetails);
				} else {
					PurchasedSingleAppOnboardingRequest applicationDetails = applicationObjectDeserializer(
							departmentReviewer.getApplcationOnboardRequest());
					NewAppOnboardInfo info = new NewAppOnboardInfo();
					info.setOwnerDetails(applicationDetails.getApplicationInfo().getOwnerDetails());
					info.setApplicationCategory(applicationDetails.getApplicationInfo().getApplicationCategory());
					info.setApplicationJustification(
							applicationDetails.getApplicationInfo().getApplicationJustification());
					info.setApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
					info.setApplicationOwnerDepartment(
							applicationDetails.getApplicationInfo().getApplicationOwnerDepartment());
					info.setApplicationProviderName(
							applicationDetails.getApplicationInfo().getApplicationProviderName());
					info.setSubscriptionNumber(applicationDetails.getApplicationInfo().getSubscriptionId());
					info.setSubscriptionName(applicationDetails.getApplicationInfo().getSubscriptionName());
					ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
							.findByApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
					info.setApplicationLogoUrl(applicationLogoEntity.getLogoUrl());
					info.setProjectName(applicationDetails.getApplicationInfo().getProjectName());

					detailViewResponse.setApplicationInfo(info);
					detailViewResponse.setProducts(applicationDetails.getProducts());
					detailViewResponse.setContractInfo(applicationDetails.getContractInfo());
					detailViewResponse.setSupportingDocsInfo(list);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				}
			}
			if (requestId == null && childRequestId != null) {
				ApplicationOnboarding departmentApprover = applicationOnboardingRepository
						.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
				ApplicationOnboarding departmentReviewerApproved = applicationOnboardingRepository
						.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.APPROVE);
				ApplicationOnboarding departmentReviewer = applicationOnboardingRepository
						.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);

				if (departmentApprover != null) {
					PurchasedSingleAppOnboardingRequest applicationDetails = applicationObjectDeserializer(
							departmentApprover.getApplcationOnboardRequest());
					NewAppOnboardInfo info = new NewAppOnboardInfo();
					info.setOwnerDetails(applicationDetails.getApplicationInfo().getOwnerDetails());
					info.setApplicationCategory(applicationDetails.getApplicationInfo().getApplicationCategory());
					info.setApplicationJustification(
							applicationDetails.getApplicationInfo().getApplicationJustification());
					info.setApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
					info.setApplicationOwnerDepartment(
							applicationDetails.getApplicationInfo().getApplicationOwnerDepartment());
					info.setApplicationProviderName(
							applicationDetails.getApplicationInfo().getApplicationProviderName());
					info.setSubscriptionNumber(applicationDetails.getApplicationInfo().getSubscriptionId());
					info.setSubscriptionName(applicationDetails.getApplicationInfo().getSubscriptionName());
					ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
							.findByApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
					info.setApplicationLogoUrl(applicationLogoEntity.getLogoUrl());
					info.setProjectName(applicationDetails.getApplicationInfo().getProjectName());

					detailViewResponse.setApplicationInfo(info);
					detailViewResponse.setProducts(applicationDetails.getProducts());
					detailViewResponse.setContractInfo(applicationDetails.getContractInfo());
					detailViewResponse.setSupportingDocsInfo(list);
					reviewerDetails.setApprovedByEmail(departmentReviewerApproved.getWorkGroupUserEmail());
					reviewerDetails.setWorkGroupName(departmentReviewerApproved.getWorkGroup());
					reviewerDetails.setComments(departmentReviewerApproved.getComments());
					reviewerDetails.setApprovalTimeStamp(departmentReviewerApproved.getEndDate());
					detailViewResponse.setReviewerDetails(reviewerDetails);
				} else {
					PurchasedSingleAppOnboardingRequest applicationDetails = applicationObjectDeserializer(
							departmentReviewer.getApplcationOnboardRequest());
					NewAppOnboardInfo info = new NewAppOnboardInfo();
					info.setOwnerDetails(applicationDetails.getApplicationInfo().getOwnerDetails());
					info.setApplicationCategory(applicationDetails.getApplicationInfo().getApplicationCategory());
					info.setApplicationJustification(
							applicationDetails.getApplicationInfo().getApplicationJustification());
					info.setApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
					info.setApplicationOwnerDepartment(
							applicationDetails.getApplicationInfo().getApplicationOwnerDepartment());
					info.setApplicationProviderName(
							applicationDetails.getApplicationInfo().getApplicationProviderName());
					info.setSubscriptionNumber(applicationDetails.getApplicationInfo().getSubscriptionId());
					info.setSubscriptionName(applicationDetails.getApplicationInfo().getSubscriptionName());
					ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
							.findByApplicationName(applicationDetails.getApplicationInfo().getApplicationName());
					info.setApplicationLogoUrl(applicationLogoEntity.getLogoUrl());
					info.setProjectName(applicationDetails.getApplicationInfo().getProjectName());

					detailViewResponse.setApplicationInfo(info);
					detailViewResponse.setProducts(applicationDetails.getProducts());
					detailViewResponse.setContractInfo(applicationDetails.getContractInfo());
					detailViewResponse.setSupportingDocsInfo(list);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				}
			}
		}
		return new CommonResponse(HttpStatus.OK,
				new Response("OnboardingRequestDetailViewResponse", detailViewResponse),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
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
		} catch (StorageException ex) {
			throw new StorageException(Constant.INSUFFICIENT_STORAGE, Constant.CLIENT_ERROR, ex);
		}
		return uris;
	}

	private PurchasedSingleAppOnboardingRequest applicationObjectDeserializer(String applicationOnboardingRequest)
			throws JsonGenerationException {
		ObjectMapper obj = new ObjectMapper();
		PurchasedSingleAppOnboardingRequest status = new PurchasedSingleAppOnboardingRequest();
		try {
			status = obj.readValue(applicationOnboardingRequest, PurchasedSingleAppOnboardingRequest.class);
		} catch (JsonProcessingException e) {
			throw new JsonGenerationException(e, null);
		}
		return status;
	}

	@Override
	@Transactional
	public CommonResponse applicationOnboardReview(String childRequestId, String requestId, UserLoginDetails profile,
			DeptOnboardingWorkFlowRequest onboardingWorkFlowRequest)
			throws DataValidationException, MessagingException, IOException, TemplateException, ParseException {
		if (childRequestId == null && requestId == null) {
			return new CommonResponse(HttpStatus.CONFLICT,
					new Response("Onboarding Work flow Action Response", "Check Parameters"),
					"Wrong Param or Null values in param");
		}
		if (childRequestId != null && requestId != null) {
			return new CommonResponse(HttpStatus.CONFLICT,
					new Response("Onboarding Work flow Action Response", "Check Parameters"),
					"Provide Either ChildRequestId or RequestId");
		}
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		if (userDetails.getUserRole().equalsIgnoreCase("reviewer")
				|| userDetails.getUserRole().equalsIgnoreCase("super_admin")) {
			if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
				if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
					if (requestId == null && childRequestId != null) {
						ApplicationOnboarding childRequest = applicationOnboardingRepository
								.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
						if (childRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null, HttpStatus.CONFLICT);
						}
						ApplicationOnboarding applicationOnboarding = new ApplicationOnboarding();
						applicationOnboarding.setChildRequestNumber(childRequestId);
						applicationOnboarding.setWorkGroup(Constant.APPROVER);
						applicationOnboarding.setApprovedRejected(Constant.REVIEW);
						applicationOnboarding.setOnboardingStatus(Constant.PENDING_WITH_APPROVER);
						applicationOnboarding.setRequestNumber(childRequest.getRequestNumber());
						applicationOnboarding.setApplcationOnboardRequest(childRequest.getApplcationOnboardRequest());
						applicationOnboarding.setApplicationName(childRequest.getApplicationName());
						applicationOnboarding.setBuID(childRequest.getBuID());
						applicationOnboarding.setOpID(childRequest.getOpID());
						applicationOnboarding.setComments(onboardingWorkFlowRequest.getComments());
						applicationOnboarding.setOwnerDepartment(childRequest.getOwnerDepartment());
						applicationOnboarding.setCreatedOn(new Date());
						applicationOnboarding.setCreatedBy(profile.getEmailAddress());
						applicationOnboarding.setUpdatedOn(new Date());
						applicationOnboarding.setRequestNumber(childRequest.getRequestNumber());
						applicationOnboarding.setUpdatedBy(profile.getFirstName() + " " + profile.getLastName());
						applicationOnboarding.setOnBoardDate(childRequest.getOnBoardDate());
						applicationOnboarding.setOnboardedByUserEmail(childRequest.getOnboardedByUserEmail());
						applicationOnboarding.setWorkGroupUserEmail(profile.getEmailAddress());
						applicationOnboarding.setProjectName(childRequest.getProjectName());
						childRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						childRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						childRequest.setOnboardingStatus("Approved by Reviewer");
						childRequest.setEndDate(new Date());
						childRequest.setComments(onboardingWorkFlowRequest.getComments());
						childRequest.setUpdatedBy(profile.getFirstName() + " " + profile.getLastName());
						childRequest.setUpdatedOn(new Date());
						applicationOnboardingRepository.save(applicationOnboarding);
						applicationOnboardingRepository.save(childRequest);
					}
					if (requestId != null && childRequestId == null) {
						ApplicationOnboarding parentRequest = applicationOnboardingRepository
								.findByRequestNumber(requestId, "Reviewer", Constant.REVIEW);
						if (parentRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null, HttpStatus.CONFLICT);
						}
						ApplicationOnboarding applicationOnboarding = new ApplicationOnboarding();
						applicationOnboarding.setChildRequestNumber(childRequestId);
						applicationOnboarding.setWorkGroup(Constant.APPROVER);
						applicationOnboarding.setApprovedRejected(Constant.REVIEW);
						applicationOnboarding.setOnboardingStatus("Pending With Approver");
						applicationOnboarding.setRequestNumber(parentRequest.getRequestNumber());
						applicationOnboarding.setApplcationOnboardRequest(parentRequest.getApplcationOnboardRequest());
						applicationOnboarding.setApplicationName(parentRequest.getApplicationName());
						applicationOnboarding.setBuID(parentRequest.getBuID());
						applicationOnboarding.setOpID(parentRequest.getOpID());
						applicationOnboarding.setProjectName(parentRequest.getProjectName());
						applicationOnboarding.setComments(onboardingWorkFlowRequest.getComments());
						applicationOnboarding.setCreatedOn(new Date());
						applicationOnboarding.setOwnerDepartment(parentRequest.getOwnerDepartment());
						applicationOnboarding.setRequestNumber(parentRequest.getRequestNumber());
						applicationOnboarding.setCreatedBy(profile.getEmailAddress());
						applicationOnboarding.setUpdatedOn(new Date());
						applicationOnboarding.setUpdatedBy(profile.getFirstName() + profile.getLastName());
						applicationOnboarding.setOnBoardDate(parentRequest.getOnBoardDate());
						applicationOnboarding.setOnboardedByUserEmail(parentRequest.getOnboardedByUserEmail());
						applicationOnboarding.setWorkGroupUserEmail(profile.getEmailAddress());

						parentRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						parentRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						parentRequest.setOnboardingStatus("Approved by Reviewer");
						parentRequest.setEndDate(new Date());
						parentRequest.setComments(onboardingWorkFlowRequest.getComments());
						parentRequest.setUpdatedBy(profile.getFirstName() + " " + profile.getLastName());
						parentRequest.setUpdatedOn(new Date());
						applicationOnboardingRepository.save(applicationOnboarding);
						applicationOnboardingRepository.save(parentRequest);
					}
					return reviewSuccessResponse();
				} else {

					if (requestId != null) {
						ApplicationOnboarding rejectRequest = applicationOnboardingRepository
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
						applicationOnboardingRepository.save(rejectRequest);
						return reviewFailureResponse();

					} else {
						ApplicationOnboarding rejectRequest = applicationOnboardingRepository
								.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
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
						applicationOnboardingRepository.save(rejectRequest);
						return reviewFailureResponse();
					}

				}
			} else {

				if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
					try {
						superAdminSaveData(requestId, childRequestId, profile, onboardingWorkFlowRequest);
					} catch (DataValidationException e) {
						if (e.getMessage().equalsIgnoreCase("404")) {
							throw new DataValidationException("Finance Department Does Not Exists To send Mail",
									requestId, HttpStatus.CONFLICT);
						} else {
							throw new DataValidationException("Provide Valid Id", requestId, HttpStatus.CONFLICT);
						}
					}
					return reviewSuccessResponse();
				} else {
					if (requestId != null) {
						ApplicationOnboarding rejectRequestForReviewer = applicationOnboardingRepository
								.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
						ApplicationOnboarding rejectRequestForApprover = applicationOnboardingRepository
								.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
						if (rejectRequestForReviewer != null) {
							rejectRequestForReviewer.setApprovedRejected(Constant.REJECTED);
							rejectRequestForReviewer.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequestForReviewer.setComments(onboardingWorkFlowRequest.getComments());
							applicationOnboardingRepository.save(rejectRequestForReviewer);
							return reviewFailureResponse();
						}
						if (rejectRequestForApprover != null) {
							rejectRequestForApprover.setApprovedRejected(Constant.REJECTED);
							rejectRequestForApprover.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequestForApprover.setComments(onboardingWorkFlowRequest.getComments());
							applicationOnboardingRepository.save(rejectRequestForApprover);
							return reviewFailureResponse();
						}
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null, HttpStatus.CONFLICT);
					} else {
						ApplicationOnboarding rejectRequestForReviewer = applicationOnboardingRepository
								.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
						ApplicationOnboarding rejectRequestForApprover = applicationOnboardingRepository
								.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
						if (rejectRequestForReviewer != null) {
							rejectRequestForReviewer.setApprovedRejected(Constant.REJECTED);
							rejectRequestForReviewer.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequestForReviewer.setComments(onboardingWorkFlowRequest.getComments());
							applicationOnboardingRepository.save(rejectRequestForReviewer);
							return reviewFailureResponse();
						}
						if (rejectRequestForApprover != null) {
							rejectRequestForApprover.setApprovedRejected(Constant.REJECTED);
							rejectRequestForApprover.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequestForApprover.setComments(onboardingWorkFlowRequest.getComments());
							applicationOnboardingRepository.save(rejectRequestForApprover);
							return reviewFailureResponse();
						}
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null, HttpStatus.CONFLICT);
					}
				}

			}
		} else {
			if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
				if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER)) {
					if (requestId == null && childRequestId != null) {
						ApplicationOnboarding childRequest = applicationOnboardingRepository
								.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
						if (childRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null, HttpStatus.CONFLICT);
						}
						childRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						childRequest.setApprovedRejected(Constant.APPROVE);
						childRequest.setOnboardingStatus("Approved by Approver");
						childRequest.setEndDate(new Date());
						childRequest.setUpdatedBy(profile.getFirstName());
						childRequest.setComments(onboardingWorkFlowRequest.getComments());
						childRequest.setUpdatedOn(new Date());
						ApplicationDetails applicationDetails = new ApplicationDetails();
						ApplicationSubscriptionDetails subscriptionDetails = new ApplicationSubscriptionDetails();
						PurchasedSingleAppOnboardingRequest applicationObject = applicationObjectDeserializer(
								childRequest.getApplcationOnboardRequest());
						try {
							applicatoinObjectValidator(applicationObject);
						} catch (DataValidationException e) {
							throw new DataValidationException(e.getMessage(), null, null);
						}
						ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
								.findByApplicationName(applicationObject.getApplicationInfo().getApplicationName());
						String name = Constant.APPLICATION_ID;
						Integer sequence = sequenceGeneratorRepository.getApplicatiionDetailSequence();
						name = name.concat(sequence.toString());
						SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
						updateSequence.setApplicationDetails(updateSequence.getApplicationDetails() + 1);

						ApplicationCategoryMaster category = categoryMasterRepository
								.findByCategoryName(applicationObject.getApplicationInfo().getApplicationCategory());
						applicationDetails.setApplicationId(name);
						applicationDetails
								.setApplicationName(applicationObject.getApplicationInfo().getApplicationName());
						applicationDetails.setBuID(Constant.BUID);
						applicationDetails.setApplicationCategoryMaster(category);
						applicationDetails.setStartDate(new Date());
						applicationDetails.setCategoryId(category.getCategoryId());
						applicationDetails.setOwnerDepartment(
								applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
						applicationDetails.setCreatedOn(new Date());
						if (applicationObject.getContractInfo() != null) {
							applicationDetails.setActiveContracts(applicationObject.getContractInfo().size());
						}
						applicationDetails.setCreatedBy(profile.getEmailAddress());
						applicationDetails.setLogoUrl(applicationLogoEntity.getLogoUrl());
						applicationDetails.setApplicationStatus(Constant.ACTIVE);
						applicationDetails.setApplicationDescription(applicationLogoEntity.getDescription());
						applicationDetails
								.setProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
						DepartmentDetails details = departmentRepository.findByDepartmentName(
								applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
						List<ApplicationDetails> appDetails = details.getApplicationId();
						appDetails.add(applicationDetails);
						details.setApplicationId(appDetails);
						
						List<Departments> departmentsList = departmentsRepository
								.findByDepartmentName(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
						if (!departmentsList.isEmpty()) {
							List<ApplicationDetails> appDetailsForDepartments = details.getApplicationId();
							StringBuilder applicationIdBuilder = new StringBuilder();
							for (ApplicationDetails detail : appDetailsForDepartments) {
								if (applicationIdBuilder.length() > 0) {
									applicationIdBuilder.append(", ");
								}
								applicationIdBuilder.append(detail.getApplicationId());
							}
							String applicationIds = applicationIdBuilder.toString();
							for (Departments department : departmentsList) {
								department.setApplicationId(applicationIds);
								try {
									BeanUtils.copyProperties(department, applicationDetails);
								} catch (IllegalAccessException | InvocationTargetException e) {
									e.printStackTrace();
								}
							}
							departmentsRepository.saveAll(departmentsList);
						}

						Applications application = new Applications();
						application.setApplicationId(name);
						application.setApplicationName(applicationObject.getApplicationInfo().getApplicationName());
						application.setBuId(Constant.BUID);
						application.setCategoryName(category.getCategoryName());
						application.setStartDate(new Date());
						application.setCategoryId(category.getCategoryId());
						application.setOwnerDepartment(
								applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
						application.setCreatedOn(new Date());
						if (applicationObject.getContractInfo() != null) {
							application.setActiveContracts(applicationObject.getContractInfo().size());
						}
						application.setCreatedBy(profile.getEmailAddress());
						application.setLogoUrl(applicationLogoEntity.getLogoUrl());
						application.setApplicationStatus(Constant.ACTIVE);
						application.setApplicationDescription(applicationLogoEntity.getDescription());
						application.setProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
						application.setDepartmentId(details.getDepartmentId());
						application.setDepartmentName(details.getDepartmentName());
						departmentRepository.save(details);
						applicationDetailsRepository.save(applicationDetails);
						int i = 1;
						for (newApplicationOwnerDetailsRequest owners : applicationObject.getApplicationInfo()
								.getOwnerDetails()) {
							ApplicationOwnerDetails applicationOwnerDetails = new ApplicationOwnerDetails();
							applicationOwnerDetails.setApplicationId(name);
							applicationOwnerDetails.setCreatedOn(new Date());
							applicationOwnerDetails.setOwner(owners.getApplicaitonOwnerName());
							applicationOwnerDetails.setOwnerDepartment(
									applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
							applicationOwnerDetails.setOwnerEmail(owners.getApplicationOwnerEmail());
							applicationOwnerDetails.setPriority(i);
							applicationOwnerRepository.save(applicationOwnerDetails);
							i++;
						}
						ProjectDetails projectDetails = projectDetailsRepository
								.findByProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
						List<ApplicationDetails> aps = projectDetails.getApplicationId();
						aps.add(applicationDetails);
						projectDetails.setApplicationId(aps);
						projectDetailsRepository.save(projectDetails);
						if (applicationObject.getApplicationInfo().getSubscriptionName().length() > 0
								|| !applicationObject.getApplicationInfo().getSubscriptionName().isEmpty()) {
							String subId = Constant.SUBSCRIPTION_ID;
							Integer subIdSequence = sequenceGeneratorRepository.getApplicationSubscriptionSequence();
							subId = subId.concat(subIdSequence.toString());
							SequenceGenerator updatesubIdSequence = sequenceGeneratorRepository.getById(1);
							updatesubIdSequence.setApplicationSubscription(++subIdSequence);
							String subscriptionId = subId;
							subscriptionDetails.setSubscriptionId(subscriptionId);
							subscriptionDetails
									.setSubscriptionName(applicationObject.getApplicationInfo().getSubscriptionName());
							subscriptionDetails
									.setSubscriptionNumber(applicationObject.getApplicationInfo().getSubscriptionId());
							subscriptionDetails.setApplicationId(applicationDetails);
							subscriptionDetails.setCreatedOn(new Date());
							subscriptionDetails.setBuID(Constant.BUID);
							subscriptionDetails.setOpID(Constant.SAASPE);
							subscriptionDetails.setStartDate(new Date());
							subscriptionDetails.setSubscriptionOwner(profile.getEmailAddress());
							subscriptionDetails.setSubscriptionStatus(Constant.ACTIVE);
							subscriptionDetails.setCreatedBy(profile.getFirstName() + " " + profile.getLastName());
							subscriptionDetails
									.setSubscriptionProvider(applicationLogoEntity.getProviderId().getProviderName());
							application.setSubscriptionId(subscriptionId);
							application
									.setSubscriptionName(applicationObject.getApplicationInfo().getSubscriptionName());
							application.setSubscriptionStartDate(new Date());
							application.setSubscriptionOwner(profile.getEmailAddress());
							application
									.setSubscriptionProvider(applicationLogoEntity.getProviderId().getProviderName());
							applicationSubscriptionDetailsRepository.save(subscriptionDetails);
							sequenceGeneratorRepository.save(updatesubIdSequence);
						}
						if (applicationObject.getContractInfo() != null) {
							for (NewAppContractInfo contract : applicationObject.getContractInfo()) {
								ApplicationContractDetails applicationContractDetails = new ApplicationContractDetails();
								String contractSeqId = Constant.CONTRACT_ID;
								Integer contractSequence = sequenceGeneratorRepository.getContractSequence();
								contractSeqId = contractSeqId.concat(contractSequence.toString());
								SequenceGenerator contractupdateSequence = sequenceGeneratorRepository.getById(1);
								contractupdateSequence.setApplicationContacts(++contractSequence);
								applicationContractDetails.setContractId(contractSeqId);
								applicationContractDetails.setApplicationId(applicationDetails);
								applicationContractDetails.setContractName(contract.getContractName());
								applicationContractDetails.setContractType(contract.getContractType());
								applicationContractDetails
										.setAutoRenewalCancellation(contract.getAutoRenewalCancellation());
								if (ContractType.monthToMonth(contract.getContractType())) {
									applicationContractDetails.setBillingFrequency(null);
									applicationContractDetails.setContractTenure(null);
								} else {
									applicationContractDetails.setBillingFrequency(contract.getBillingFrequency());
									applicationContractDetails
											.setContractTenure(Integer.valueOf(contract.getContractTenure()));
								}
								applicationContractDetails.setBuID(Constant.BUID);
								applicationContractDetails.setCreatedOn(new Date());
								applicationContractDetails.setStartDate(new Date());
								applicationContractDetails.setCreatedBy(profile.getEmailAddress());
								applicationContractDetails.setContractStartDate(contract.getContractStartDate());
								applicationContractDetails.setContractEndDate(contract.getContractEndDate());
								if (Boolean.TRUE.equals(contract.getAutoRenewal())) {
									applicationContractDetails.setRenewalDate(contract.getUpcomingRenewalDate());
								} else {
									applicationContractDetails.setRenewalDate(contract.getContractEndDate());
								}
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
								applicationContractDetails.setAutoRenew(contract.getAutoRenewal());
								applicationContractDetails.setSubscriptionDetails(subscriptionDetails);
								applicationContractDetails.setContractPaymentMethod(contract.getPaymentMethod());
								applicationContractDetails.setContractCurrency(contract.getCurrencyCode());
								applicationContractDetails
										.setContractOwner(profile.getFirstName() + " " + profile.getLastName());
								String invseSeq = Constant.INVOICE_ID;
								Integer invSequence = sequenceGeneratorRepository.getPaymentReqSequence();
								invseSeq = invseSeq.concat(invSequence.toString());
								SequenceGenerator invoiceUpdateSequence = sequenceGeneratorRepository.getById(1);
								invoiceUpdateSequence.setPaymentSequenceId(++invSequence);
								PaymentDetails paymentDetails = new PaymentDetails();
								paymentDetails.setInvoiceNo(invseSeq);
								paymentDetails.setApplicationId(name);
								paymentDetails.setBuID(Constant.BUID);
								paymentDetails.setCreatedBy(profile.getEmailAddress());
								paymentDetails.setCreatedOn(new Date());
								paymentDetails.setStartDate(new Date());
								paymentDetails.setPaymentMethod(contract.getPaymentMethod());
								if (contract.getCardNumber() != null) {
									String cardNo = Base64.getEncoder().encodeToString(
											contract.getCardNumber().toString().getBytes(StandardCharsets.UTF_8));
									String trimmedCardNo = cardNo.replaceAll("\\s", "").trim();
									paymentDetails.setCardNumber(trimmedCardNo);
								}
								paymentDetails.setCardholderName(contract.getCardHolderName());
								paymentDetails.setValidThrough(contract.getValidThrough());
								paymentDetails.setWalletName(contract.getWalletName());
								paymentDetails.setContractId(contractSeqId);
								paymentDetailsRepository.save(paymentDetails);
								sequenceGeneratorRepository.save(invoiceUpdateSequence);
								contractDetailsRepository.save(applicationContractDetails);
								sequenceGeneratorRepository.save(contractupdateSequence);
							}
						}
						for (NewAppLicenseInfo license : applicationObject.getProducts()) {
							String licenseSeq = Constant.LICENSE_ID;
							Integer licenseSequence = sequenceGeneratorRepository.getLicenseSequence();
							licenseSeq = licenseSeq.concat(licenseSequence.toString());
							SequenceGenerator licenseUpdateSequence = sequenceGeneratorRepository.getById(1);
							licenseUpdateSequence.setApplicatiionLicense(++licenseSequence);
							ApplicationContractDetails contractDetails = contractDetailsRepository
									.findByContractName(license.getContractName());
							ApplicationLicenseDetails applicationLicenseDetails = licenseContractTotalCost(
									contractDetails, license);
							PaymentDetails pay = paymentDetailsRepository
									.findByContractId(contractDetails.getContractId());
							applicationLicenseDetails.setApplicationId(applicationDetails);
							BigDecimal afterCost = BigDecimal.valueOf(0);
							BigDecimal beforeCost = BigDecimal.valueOf(0);
							if (contractDetails.getContractStartDate().after(new Date())) {
								afterCost = getConvertedLicenseCost(contractDetails.getStartDate(),
										contractDetails.getContractCurrency(),
										applicationLicenseDetails.getTotalCost());
								applicationLicenseDetails.setConvertedCost(afterCost);
							} else {
								beforeCost = getConvertedLicenseCost(contractDetails.getContractStartDate(),
										contractDetails.getContractCurrency(),
										applicationLicenseDetails.getTotalCost());
								applicationLicenseDetails.setConvertedCost(beforeCost);
							}
							applicationLicenseDetails.setLicenseId(licenseSeq);
							applicationLicenseDetails.setContractId(contractDetails);
							applicationLicenseDetails.setCreatedBy(profile.getFirstName());
							applicationLicenseDetails.setCreatedOn(new Date());
							applicationLicenseDetails.setLicenseStartDate(contractDetails.getContractStartDate());
							applicationLicenseDetails.setLicenseEndDate(contractDetails.getContractEndDate());
							applicationLicenseDetails.setBuID(Constant.BUID);
							applicationLicenseDetails.setStartDate(new Date());
							applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
							applicationLicenseDetails.setProductName(license.getProductName());
							applicationLicenseDetails.setCurrency(license.getCurrencyCode());
							applicationLicenseDetails.setUnitPrice(license.getUnitPrice());
							applicationLicenseDetails.setProductCategory(license.getProductType());
							applicationLicenseDetails.setLicenseMapped(0);
							if (ProductType.professionalServices(license.getProductType())
									|| ProductType.platform(license.getProductType())) {
								applicationLicenseDetails.setQuantity(1);
								applicationLicenseDetails.setLicenseUnMapped(1);
							} else {
								applicationLicenseDetails.setQuantity(license.getQuantity());
								applicationLicenseDetails.setLicenseUnMapped(license.getQuantity());
							}
							if (application.getLicenseId() == null) {
								application.setContractId(contractDetails.getContractId());
								application.setAutoRenew(contractDetails.getAutoRenew());
								application.setContractCurrency(contractDetails.getContractCurrency());
								application.setContractDescription(contractDetails.getContractDescription());
								application.setContractEndDate(contractDetails.getContractEndDate());
								application.setContractName(contractDetails.getContractName());
								application.setContractNoticeDate(contractDetails.getContractNoticeDate());
								application.setContractOwner(contractDetails.getContractOwner());
								application.setContractOwnerEmail(contractDetails.getContractOwnerEmail());
								application.setPaymentMethod(contractDetails.getContractPaymentMethod());
								application.setPaymentTerm(contractDetails.getContractPaymentTerm());
								application.setContractProvider(contractDetails.getContractProvider());
								application.setContractStartDate(contractDetails.getContractStartDate());
								application.setContractStatus(contractDetails.getContractStatus());
								application.setReminderDate(contractDetails.getReminderDate());
								application.setRenewalTerm(contractDetails.getRenewalTerm());
								application.setContractType(contractDetails.getContractType());
								application.setBillingFrequency(contractDetails.getBillingFrequency());
								if (contractDetails.getAutoRenewalCancellation() != null)
									application
											.setAutoRenewalCancellation(contractDetails.getAutoRenewalCancellation());
								application.setContractTenure(contractDetails.getContractTenure());
								application.setLicenseId(licenseSeq);
								application.setLicenseStartDate(contractDetails.getContractStartDate());
								application.setLicenseEndDate(contractDetails.getContractEndDate());
								application.setUnitPriceType(license.getUnitPriceType());
								application.setProductName(license.getProductName());
								application.setCurrency(license.getCurrencyCode());
								application.setUnitPrice(license.getUnitPrice());
								application.setProductCategory(license.getProductType());
								application.setLicenseMapped(0);
								if (ProductType.professionalServices(license.getProductType())
										|| ProductType.platform(license.getProductType())) {
									application.setQuantity(1);
									application.setLicenseUnmapped(1);
								} else {
									application.setQuantity(license.getQuantity());
									application.setLicenseUnmapped(license.getQuantity());
								}
								application.setTotalCost(applicationLicenseDetails.getTotalCost());
								if (contractDetails.getContractStartDate().after(new Date())) {
									application.setConvertedCost(afterCost);
								} else {
									application.setConvertedCost(beforeCost);
								}
								application.setPaymentDescription(pay.getDescription());
								application.setPaymentTransactionDate(pay.getTransactionDate());
								application.setPaymentAmount(pay.getAmount());
								application.setCardholderName(pay.getCardholderName());
								application.setCardNumber(pay.getCardNumber());
								application.setValidThrough(pay.getValidThrough());
								application.setWalletName(pay.getWalletName());
								application.setPaymentStartDate(pay.getStartDate());
								application.setPaymentEndDate(pay.getEndDate());
								application.setPaymentSecretKey(pay.getSecretKey());
								applicationRepository.save(application);
							} else if (!application.getLicenseId().equals(licenseSeq)) {
								Applications app = new Applications();
								try {
									BeanUtils.copyProperties(app, application);
									app.setId(0);
								} catch (IllegalAccessException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InvocationTargetException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								app.setContractId(contractDetails.getContractId());
								app.setAutoRenew(contractDetails.getAutoRenew());
								app.setContractCurrency(contractDetails.getContractCurrency());
								app.setContractDescription(contractDetails.getContractDescription());
								app.setContractEndDate(contractDetails.getContractEndDate());
								app.setContractName(contractDetails.getContractName());
								app.setContractNoticeDate(contractDetails.getContractNoticeDate());
								app.setContractOwner(contractDetails.getContractOwner());
								app.setContractOwnerEmail(contractDetails.getContractOwnerEmail());
								app.setPaymentMethod(contractDetails.getContractPaymentMethod());
								app.setPaymentTerm(contractDetails.getContractPaymentTerm());
								app.setContractProvider(contractDetails.getContractProvider());
								app.setContractStartDate(contractDetails.getContractStartDate());
								app.setContractStatus(contractDetails.getContractStatus());
								app.setReminderDate(contractDetails.getReminderDate());
								app.setRenewalTerm(contractDetails.getRenewalTerm());
								app.setContractType(contractDetails.getContractType());
								app.setBillingFrequency(contractDetails.getBillingFrequency());
								if (contractDetails.getAutoRenewalCancellation() != null)
									app.setAutoRenewalCancellation(contractDetails.getAutoRenewalCancellation());
								app.setContractTenure(contractDetails.getContractTenure());
								app.setLicenseId(licenseSeq);
								app.setLicenseStartDate(contractDetails.getContractStartDate());
								app.setLicenseEndDate(contractDetails.getContractEndDate());
								app.setUnitPriceType(license.getUnitPriceType());
								app.setProductName(license.getProductName());
								app.setCurrency(license.getCurrencyCode());
								app.setUnitPrice(license.getUnitPrice());
								app.setProductCategory(license.getProductType());
								app.setLicenseMapped(0);
								if (ProductType.professionalServices(license.getProductType())
										|| ProductType.platform(license.getProductType())) {
									app.setQuantity(1);
									app.setLicenseUnmapped(1);
								} else {
									app.setQuantity(license.getQuantity());
									app.setLicenseUnmapped(license.getQuantity());
								}
								app.setTotalCost(applicationLicenseDetails.getTotalCost());
								if (contractDetails.getContractStartDate().after(new Date())) {
									app.setConvertedCost(afterCost);
								} else {
									app.setConvertedCost(beforeCost);
								}
								app.setPaymentDescription(pay.getDescription());
								app.setPaymentTransactionDate(pay.getTransactionDate());
								app.setPaymentAmount(pay.getAmount());
								app.setCardholderName(pay.getCardholderName());
								app.setCardNumber(pay.getCardNumber());
								app.setValidThrough(pay.getValidThrough());
								app.setWalletName(pay.getWalletName());
								app.setPaymentStartDate(pay.getStartDate());
								app.setPaymentEndDate(pay.getEndDate());
								app.setPaymentSecretKey(pay.getSecretKey());
								applicationRepository.save(app);
							}

							licenseDetailsRepository.save(applicationLicenseDetails);
							sequenceGeneratorRepository.save(licenseUpdateSequence);
						}

						List<ProjectManagerDetails> proManagers = projectOwnerRepository
								.findByProjectIdOrderByEndDate(projectDetails.getProjectId());
						List<Projects> existingProject = projectsRepository
								.findByProjectId(projectDetails.getProjectId());
						for (ProjectManagerDetails proManager : proManagers) {
							if (!existingProject.isEmpty()) {
								for (Projects project : existingProject) {
									if (project.getProjectManagerEmail().equals(proManager.getProjectManagerEmail())) {
										project.setProjectManagerEmail(proManager.getProjectManagerEmail());
										project.setPriority(proManager.getPriority());
										project.setProjectManagerStartDate(proManager.getStartDate());
										project.setProjectManagerEndDate(proManager.getEndDate());
										project.setProjectManagerCreatedOn(proManager.getCreatedOn());
										project.setProjectManagerUpdatedOn(proManager.getUpdatedOn());
										project.setApplicationId(applicationDetails.getApplicationId());
										project.setProjectStatus(
												projectDetails.getEndDate().before(new Date()) ? false : true);
										projectsRepository.save(project);
									}
								}
							} else {
								Projects project = new Projects();
								project.setProjectId(projectDetails.getProjectId());
								project.setProjectName(projectDetails.getProjectName());
								project.setProjectManagerEmail(proManager.getProjectManagerEmail());
								project.setBudget(projectDetails.getProjectBudget());
								project.setBudgetCurrency(projectDetails.getBudgetCurrency());
								project.setDescription(projectDetails.getProjectDescription());
								project.setCreatedBy(projectDetails.getCreatedBy());
								project.setUpdtaedBy(projectDetails.getUpdatedBy());
								project.setProjectCreatedOn(projectDetails.getCreatedOn());
								project.setProjectUpdatedOn(projectDetails.getUpdatedOn());
								project.setProjectStartDate(projectDetails.getStartDate());
								project.setProjectEndDate(projectDetails.getEndDate());
								project.setProjectCode(projectDetails.getProjectCode());
								project.setPriority(proManager.getPriority());
								project.setProjectManagerStartDate(proManager.getStartDate());
								project.setProjectManagerEndDate(proManager.getEndDate());
								project.setProjectManagerCreatedOn(proManager.getCreatedOn());
								project.setProjectManagerUpdatedOn(proManager.getUpdatedOn());
								project.setBuId(projectDetails.getBuID());
								project.setOpId(projectDetails.getOpID());
								project.setApplicationId(applicationDetails.getApplicationId());
								project.setProjectStatus(projectDetails.getEndDate().before(new Date()) ? false : true);
								projectsRepository.save(project);
							}
						}

						sequenceGeneratorRepository.save(updateSequence);
						applicationOnboardingRepository.save(childRequest);
						if (applicationObject.getContractInfo() == null) {
							try {
								newApplicationEmailVerfication(childRequest, null, childRequestId);
							} catch (IOException e) {
								throw new IOException(e);
							} catch (TemplateException e) {
								throw new TemplateException(e, null);
							} catch (DataValidationException e) {
								throw new DataValidationException(e.getMessage(), null, null);
							}
						}
					}
					if (requestId != null && childRequestId == null) {
						ApplicationOnboarding parentRequest = applicationOnboardingRepository
								.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
						if (parentRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null, HttpStatus.CONFLICT);
						}
						parentRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						parentRequest.setApprovedRejected(Constant.APPROVE);
						parentRequest.setOnboardingStatus("Approved by Approver");
						parentRequest.setComments(onboardingWorkFlowRequest.getComments());
						parentRequest.setEndDate(new Date());
						parentRequest.setUpdatedBy(profile.getFirstName());
						parentRequest.setComments(onboardingWorkFlowRequest.getComments());
						parentRequest.setUpdatedOn(new Date());

						ApplicationDetails applicationDetails = new ApplicationDetails();
						ApplicationSubscriptionDetails subscriptionDetails = new ApplicationSubscriptionDetails();
						PurchasedSingleAppOnboardingRequest applicationObject = applicationObjectDeserializer(
								parentRequest.getApplcationOnboardRequest());
						applicatoinObjectValidator(applicationObject, 0);
						ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
								.findByApplicationName(applicationObject.getApplicationInfo().getApplicationName());

						String name = Constant.APPLICATION_ID;
						Integer sequence = sequenceGeneratorRepository.getApplicatiionDetailSequence();
						name = name.concat(sequence.toString());
						SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
						updateSequence.setApplicationDetails(updateSequence.getApplicationDetails() + 1);

						ApplicationCategoryMaster category = categoryMasterRepository
								.findByCategoryName(applicationObject.getApplicationInfo().getApplicationCategory());

						applicationDetails.setApplicationId(name);
						applicationDetails
								.setApplicationName(applicationObject.getApplicationInfo().getApplicationName());
						applicationDetails.setCategoryId(category.getCategoryId());
						applicationDetails.setApplicationCategoryMaster(category);
						applicationDetails.setOwnerDepartment(
								applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
						applicationDetails.setStartDate(new Date());
						applicationDetails.setCreatedOn(new Date());
						if (applicationObject.getContractInfo() != null) {
							applicationDetails.setActiveContracts(applicationObject.getContractInfo().size());
						}
						applicationDetails.setCreatedBy(profile.getEmailAddress());

						applicationDetails.setLogoUrl(applicationLogoEntity.getLogoUrl());
						applicationDetails.setApplicationStatus(Constant.ACTIVE);
						applicationDetails.setApplicationDescription(applicationLogoEntity.getDescription());

						applicationDetails.setProjectName(applicationObject.getApplicationInfo().getProjectName());
						DepartmentDetails details = departmentRepository.findByDepartmentName(
								applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
						List<ApplicationDetails> appDetails = details.getApplicationId();
						appDetails.add(applicationDetails);
						details.setApplicationId(appDetails);
						
						List<Departments> departmentsList = departmentsRepository
								.findByDepartmentName(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
						if (!departmentsList.isEmpty()) {
							List<ApplicationDetails> appDetailsForDepartments = details.getApplicationId();
							StringBuilder applicationIdBuilder = new StringBuilder();
							for (ApplicationDetails detail : appDetailsForDepartments) {
								if (applicationIdBuilder.length() > 0) {
									applicationIdBuilder.append(", ");
								}
								applicationIdBuilder.append(detail.getApplicationId());
							}
							String applicationIds = applicationIdBuilder.toString();
							for (Departments department : departmentsList) {
								department.setApplicationId(applicationIds);
								try {
									BeanUtils.copyProperties(department, applicationDetails);
								} catch (IllegalAccessException | InvocationTargetException e) {
									e.printStackTrace();
								}
							}
							departmentsRepository.saveAll(departmentsList);
						}

						Applications application = new Applications();
						application.setApplicationId(name);
						application.setApplicationName(applicationObject.getApplicationInfo().getApplicationName());
						application.setBuId(Constant.BUID);
						application.setCategoryName(category.getCategoryName());
						application.setStartDate(new Date());
						application.setCategoryId(category.getCategoryId());
						application.setOwnerDepartment(
								applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
						application.setCreatedOn(new Date());
						if (applicationObject.getContractInfo() != null) {
							application.setActiveContracts(applicationObject.getContractInfo().size());
						}
						application.setCreatedBy(profile.getEmailAddress());
						application.setLogoUrl(applicationLogoEntity.getLogoUrl());
						application.setApplicationStatus(Constant.ACTIVE);
						application.setApplicationDescription(applicationLogoEntity.getDescription());
						application.setProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
						application.setDepartmentId(details.getDepartmentId());
						application.setDepartmentName(details.getDepartmentName());

						departmentRepository.save(details);
						applicationDetailsRepository.save(applicationDetails);
						int i = 1;
						for (newApplicationOwnerDetailsRequest owners : applicationObject.getApplicationInfo()
								.getOwnerDetails()) {
							ApplicationOwnerDetails applicationOwnerDetails = new ApplicationOwnerDetails();
							applicationOwnerDetails.setApplicationId(name);
							applicationOwnerDetails.setCreatedOn(new Date());
							applicationOwnerDetails.setOwner(owners.getApplicaitonOwnerName());
							applicationOwnerDetails.setOwnerDepartment(
									applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
							applicationOwnerDetails.setOwnerEmail(owners.getApplicationOwnerEmail());
							applicationOwnerDetails.setPriority(i);
							applicationOwnerRepository.save(applicationOwnerDetails);
							i++;
						}

						ProjectDetails projectDetails = projectDetailsRepository
								.findByProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
						List<ApplicationDetails> aps = projectDetails.getApplicationId();
						aps.add(applicationDetails);
						projectDetails.setApplicationId(aps);
						projectDetailsRepository.save(projectDetails);

						if (applicationObject.getApplicationInfo().getSubscriptionName().length() > 0
								|| !applicationObject.getApplicationInfo().getSubscriptionName().isEmpty()) {
							String subId = Constant.SUBSCRIPTION_ID;
							Integer subIdSequence = sequenceGeneratorRepository.getApplicationSubscriptionSequence();
							subId = subId.concat(subIdSequence.toString());
							SequenceGenerator updatesubIdSequence = sequenceGeneratorRepository.getById(1);
							updatesubIdSequence.setApplicationSubscription(++subIdSequence);

							String subscriptionId = subId;
							subscriptionDetails.setSubscriptionId(subscriptionId);
							subscriptionDetails
									.setSubscriptionName(applicationObject.getApplicationInfo().getSubscriptionName());
							subscriptionDetails
									.setSubscriptionNumber(applicationObject.getApplicationInfo().getSubscriptionId());
							subscriptionDetails.setApplicationId(applicationDetails);
							subscriptionDetails.setCreatedOn(new Date());
							subscriptionDetails.setBuID(Constant.BUID);
							subscriptionDetails.setOpID(Constant.SAASPE);
							subscriptionDetails.setStartDate(new Date());
							subscriptionDetails.setSubscriptionOwner(profile.getEmailAddress());
							subscriptionDetails.setSubscriptionStatus(Constant.ACTIVE);
							subscriptionDetails.setCreatedBy(profile.getFirstName() + " " + profile.getLastName());
							subscriptionDetails
									.setSubscriptionProvider(applicationLogoEntity.getProviderId().getProviderName());

							application.setSubscriptionId(subscriptionId);
							application
									.setSubscriptionName(applicationObject.getApplicationInfo().getSubscriptionName());
							application.setSubscriptionStartDate(new Date());
							application.setSubscriptionOwner(profile.getEmailAddress());
							application
									.setSubscriptionProvider(applicationLogoEntity.getProviderId().getProviderName());

							applicationSubscriptionDetailsRepository.save(subscriptionDetails);
							sequenceGeneratorRepository.save(updatesubIdSequence);
						}

						if (applicationObject.getContractInfo() != null) {
							for (NewAppContractInfo contract : applicationObject.getContractInfo()) {
								ApplicationContractDetails applicationContractDetails = new ApplicationContractDetails();
								String contractSeqId = Constant.CONTRACT_ID;
								Integer contractSequence = sequenceGeneratorRepository.getContractSequence();
								contractSeqId = contractSeqId.concat(contractSequence.toString());
								SequenceGenerator contractupdateSequence = sequenceGeneratorRepository.getById(1);
								contractupdateSequence.setApplicationContacts(++contractSequence);

								applicationContractDetails.setContractId(contractSeqId);
								applicationContractDetails.setApplicationId(applicationDetails);
								applicationContractDetails.setContractName(contract.getContractName());
								applicationContractDetails.setContractStartDate(contract.getContractStartDate());
								applicationContractDetails.setContractEndDate(contract.getContractEndDate());
								if (Boolean.TRUE.equals(contract.getAutoRenewal())) {
									applicationContractDetails.setRenewalDate(contract.getUpcomingRenewalDate());
								} else {
									applicationContractDetails.setRenewalDate(contract.getContractEndDate());
								}
								applicationContractDetails.setStartDate(new Date());
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
								applicationContractDetails.setBuID(Constant.BUID);
								applicationContractDetails.setCreatedOn(new Date());
								applicationContractDetails.setCreatedBy(profile.getEmailAddress());
								applicationContractDetails.setAutoRenew(contract.getAutoRenewal());
								applicationContractDetails.setContractType(contract.getContractType());
								applicationContractDetails
										.setAutoRenewalCancellation(contract.getAutoRenewalCancellation());
								if (ContractType.monthToMonth(contract.getContractType())) {
									applicationContractDetails.setBillingFrequency(null);
									applicationContractDetails.setContractTenure(null);
								} else {
									applicationContractDetails.setBillingFrequency(contract.getBillingFrequency());
									applicationContractDetails
											.setContractTenure(Integer.valueOf(contract.getContractTenure()));
								}
								applicationContractDetails.setSubscriptionDetails(subscriptionDetails);
								applicationContractDetails.setContractPaymentMethod(contract.getPaymentMethod());
								applicationContractDetails.setContractCurrency(contract.getCurrencyCode());
								applicationContractDetails
										.setContractOwner(profile.getFirstName() + " " + profile.getLastName());

								String invseSeq = Constant.INVOICE_ID;
								Integer invSequence = sequenceGeneratorRepository.getPaymentReqSequence();
								invseSeq = invseSeq.concat(invSequence.toString());
								SequenceGenerator invoiceUpdateSequence = sequenceGeneratorRepository.getById(1);
								invoiceUpdateSequence.setPaymentSequenceId(++invSequence);

								PaymentDetails paymentDetails = new PaymentDetails();
								paymentDetails.setInvoiceNo(invseSeq);
								paymentDetails.setApplicationId(name);
								paymentDetails.setBuID(Constant.BUID);
								paymentDetails.setCreatedBy(profile.getEmailAddress());
								paymentDetails.setCreatedOn(new Date());
								paymentDetails.setStartDate(new Date());
								paymentDetails.setPaymentMethod(contract.getPaymentMethod());
								paymentDetails.setCardholderName(contract.getCardHolderName());
								if (contract.getCardNumber() != null) {
									String cardNo = Base64.getEncoder().encodeToString(
											contract.getCardNumber().toString().getBytes(StandardCharsets.UTF_8));
									String trimmedCardNo = cardNo.replaceAll("\\s", "").trim();
									paymentDetails.setCardNumber(trimmedCardNo);
								}
								paymentDetails.setValidThrough(contract.getValidThrough());
								paymentDetails.setWalletName(contract.getWalletName());
								paymentDetails.setContractId(contractSeqId);
								paymentDetailsRepository.save(paymentDetails);
								sequenceGeneratorRepository.save(invoiceUpdateSequence);

								contractDetailsRepository.save(applicationContractDetails);
								sequenceGeneratorRepository.save(contractupdateSequence);
							}

						}

						for (NewAppLicenseInfo license : applicationObject.getProducts()) {
							String licenseSeq = Constant.LICENSE_ID;
							Integer licenseSequence = sequenceGeneratorRepository.getLicenseSequence();
							licenseSeq = licenseSeq.concat(licenseSequence.toString());
							SequenceGenerator licenseUpdateSequence = sequenceGeneratorRepository.getById(1);
							licenseUpdateSequence.setApplicatiionLicense(++licenseSequence);
							ApplicationContractDetails contractDetails = contractDetailsRepository
									.findByContractName(license.getContractName());
							ApplicationLicenseDetails applicationLicenseDetails = licenseContractTotalCost(
									contractDetails, license);
							PaymentDetails pay = paymentDetailsRepository
									.findByContractId(contractDetails.getContractId());
							applicationLicenseDetails.setApplicationId(applicationDetails);
							applicationLicenseDetails.setContractId(contractDetails);
							applicationLicenseDetails.setCreatedBy(profile.getFirstName());
							applicationLicenseDetails.setCreatedOn(new Date());
							applicationLicenseDetails.setStartDate(new Date());
							BigDecimal afterCost = BigDecimal.valueOf(0);
							BigDecimal beforeCost = BigDecimal.valueOf(0);
							if (contractDetails != null) {
								if (contractDetails.getContractStartDate().after(new Date())) {
									afterCost = getConvertedLicenseCost(contractDetails.getStartDate(),
											contractDetails.getContractCurrency(),
											applicationLicenseDetails.getTotalCost());
									applicationLicenseDetails.setConvertedCost(afterCost);
								} else {
									beforeCost = getConvertedLicenseCost(contractDetails.getContractStartDate(),
											contractDetails.getContractCurrency(),
											applicationLicenseDetails.getTotalCost());
									applicationLicenseDetails.setConvertedCost(beforeCost);
								}
								applicationLicenseDetails.setLicenseStartDate(contractDetails.getContractStartDate());
								applicationLicenseDetails.setLicenseEndDate(contractDetails.getContractEndDate());
							}
							applicationLicenseDetails.setBuID(Constant.BUID);
							applicationLicenseDetails.setLicenseId(licenseSeq);
							applicationLicenseDetails.setProductName(license.getProductName());
							applicationLicenseDetails.setCurrency(license.getCurrencyCode());
							applicationLicenseDetails.setUnitPrice(license.getUnitPrice());
							applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
							applicationLicenseDetails.setProductCategory(license.getProductType());
							if (ProductType.professionalServices(license.getProductType())
									|| ProductType.platform(license.getProductType())) {
								applicationLicenseDetails.setQuantity(1);
								applicationLicenseDetails.setLicenseUnMapped(1);
							} else {
								applicationLicenseDetails.setQuantity(license.getQuantity());
								applicationLicenseDetails.setLicenseUnMapped(license.getQuantity());
							}
							applicationLicenseDetails.setLicenseMapped(0);

							if (application.getLicenseId() == null) {
								application.setContractId(contractDetails.getContractId());
								application.setAutoRenew(contractDetails.getAutoRenew());
								application.setContractCurrency(contractDetails.getContractCurrency());
								application.setContractDescription(contractDetails.getContractDescription());
								application.setContractEndDate(contractDetails.getContractEndDate());
								application.setContractName(contractDetails.getContractName());
								application.setContractNoticeDate(contractDetails.getContractNoticeDate());
								application.setContractOwner(contractDetails.getContractOwner());
								application.setContractOwnerEmail(contractDetails.getContractOwnerEmail());
								application.setPaymentMethod(contractDetails.getContractPaymentMethod());
								application.setPaymentTerm(contractDetails.getContractPaymentTerm());
								application.setContractProvider(contractDetails.getContractProvider());
								application.setContractStartDate(contractDetails.getContractStartDate());
								application.setContractStatus(contractDetails.getContractStatus());
								application.setReminderDate(contractDetails.getReminderDate());
								application.setRenewalTerm(contractDetails.getRenewalTerm());
								application.setContractType(contractDetails.getContractType());
								application.setBillingFrequency(contractDetails.getBillingFrequency());
								if (contractDetails.getAutoRenewalCancellation() != null)
									application
											.setAutoRenewalCancellation(contractDetails.getAutoRenewalCancellation());
								application.setContractTenure(contractDetails.getContractTenure());
								application.setLicenseId(licenseSeq);
								application.setLicenseStartDate(contractDetails.getContractStartDate());
								application.setLicenseEndDate(contractDetails.getContractEndDate());
								application.setUnitPriceType(license.getUnitPriceType());
								application.setProductName(license.getProductName());
								application.setCurrency(license.getCurrencyCode());
								application.setUnitPrice(license.getUnitPrice());
								application.setProductCategory(license.getProductType());
								application.setLicenseMapped(0);
								if (ProductType.professionalServices(license.getProductType())
										|| ProductType.platform(license.getProductType())) {
									application.setQuantity(1);
									application.setLicenseUnmapped(1);
								} else {
									application.setQuantity(license.getQuantity());
									application.setLicenseUnmapped(license.getQuantity());
								}
								application.setTotalCost(applicationLicenseDetails.getTotalCost());
								if (contractDetails.getContractStartDate().after(new Date())) {
									application.setConvertedCost(afterCost);
								} else {
									application.setConvertedCost(beforeCost);
								}
								application.setPaymentDescription(pay.getDescription());
								application.setPaymentTransactionDate(pay.getTransactionDate());
								application.setPaymentAmount(pay.getAmount());
								application.setCardholderName(pay.getCardholderName());
								application.setCardNumber(pay.getCardNumber());
								application.setValidThrough(pay.getValidThrough());
								application.setWalletName(pay.getWalletName());
								application.setPaymentStartDate(pay.getStartDate());
								application.setPaymentEndDate(pay.getEndDate());
								application.setPaymentSecretKey(pay.getSecretKey());
								applicationRepository.save(application);
							} else if (!application.getLicenseId().equals(licenseSeq)) {
								Applications app = new Applications();
								try {
									BeanUtils.copyProperties(app, application);
									app.setId(0);
								} catch (IllegalAccessException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InvocationTargetException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								app.setContractId(contractDetails.getContractId());
								app.setAutoRenew(contractDetails.getAutoRenew());
								app.setContractCurrency(contractDetails.getContractCurrency());
								app.setContractDescription(contractDetails.getContractDescription());
								app.setContractEndDate(contractDetails.getContractEndDate());
								app.setContractName(contractDetails.getContractName());
								app.setContractNoticeDate(contractDetails.getContractNoticeDate());
								app.setContractOwner(contractDetails.getContractOwner());
								app.setContractOwnerEmail(contractDetails.getContractOwnerEmail());
								app.setPaymentMethod(contractDetails.getContractPaymentMethod());
								app.setPaymentTerm(contractDetails.getContractPaymentTerm());
								app.setContractProvider(contractDetails.getContractProvider());
								app.setContractStartDate(contractDetails.getContractStartDate());
								app.setContractStatus(contractDetails.getContractStatus());
								app.setReminderDate(contractDetails.getReminderDate());
								app.setRenewalTerm(contractDetails.getRenewalTerm());
								app.setContractType(contractDetails.getContractType());
								app.setBillingFrequency(contractDetails.getBillingFrequency());
								if (contractDetails.getAutoRenewalCancellation() != null)
									app.setAutoRenewalCancellation(contractDetails.getAutoRenewalCancellation());
								app.setContractTenure(contractDetails.getContractTenure());
								app.setLicenseId(licenseSeq);
								app.setLicenseStartDate(contractDetails.getContractStartDate());
								app.setLicenseEndDate(contractDetails.getContractEndDate());
								app.setUnitPriceType(license.getUnitPriceType());
								app.setProductName(license.getProductName());
								app.setCurrency(license.getCurrencyCode());
								app.setUnitPrice(license.getUnitPrice());
								app.setProductCategory(license.getProductType());
								app.setLicenseMapped(0);
								if (ProductType.professionalServices(license.getProductType())
										|| ProductType.platform(license.getProductType())) {
									app.setQuantity(1);
									app.setLicenseUnmapped(1);
								} else {
									app.setQuantity(license.getQuantity());
									app.setLicenseUnmapped(license.getQuantity());
								}
								app.setTotalCost(applicationLicenseDetails.getTotalCost());
								if (contractDetails.getContractStartDate().after(new Date())) {
									app.setConvertedCost(afterCost);
								} else {
									app.setConvertedCost(beforeCost);
								}
								app.setPaymentDescription(pay.getDescription());
								app.setPaymentTransactionDate(pay.getTransactionDate());
								app.setPaymentAmount(pay.getAmount());
								app.setCardholderName(pay.getCardholderName());
								app.setCardNumber(pay.getCardNumber());
								app.setValidThrough(pay.getValidThrough());
								app.setWalletName(pay.getWalletName());
								app.setPaymentStartDate(pay.getStartDate());
								app.setPaymentEndDate(pay.getEndDate());
								app.setPaymentSecretKey(pay.getSecretKey());
								applicationRepository.save(app);
							}

							licenseDetailsRepository.save(applicationLicenseDetails);
							sequenceGeneratorRepository.save(licenseUpdateSequence);
						}

						List<ProjectManagerDetails> proManagers = projectOwnerRepository
								.findByProjectIdOrderByEndDate(projectDetails.getProjectId());
						List<Projects> existingProject = projectsRepository
								.findByProjectId(projectDetails.getProjectId());
						for (ProjectManagerDetails proManager : proManagers) {
							if (!existingProject.isEmpty()) {
								for (Projects project : existingProject) {
									if (project.getProjectManagerEmail().equals(proManager.getProjectManagerEmail())) {
										project.setProjectManagerEmail(proManager.getProjectManagerEmail());
										project.setPriority(proManager.getPriority());
										project.setProjectManagerStartDate(proManager.getStartDate());
										project.setProjectManagerEndDate(proManager.getEndDate());
										project.setProjectManagerCreatedOn(proManager.getCreatedOn());
										project.setProjectManagerUpdatedOn(proManager.getUpdatedOn());
										project.setApplicationId(applicationDetails.getApplicationId());
										project.setProjectStatus(
												projectDetails.getEndDate().before(new Date()) ? false : true);
										projectsRepository.save(project);
									}
								}
							} else {
								Projects project = new Projects();
								project.setProjectId(projectDetails.getProjectId());
								project.setProjectName(projectDetails.getProjectName());
								project.setProjectManagerEmail(proManager.getProjectManagerEmail());
								project.setBudget(projectDetails.getProjectBudget());
								project.setBudgetCurrency(projectDetails.getBudgetCurrency());
								project.setDescription(projectDetails.getProjectDescription());
								project.setCreatedBy(projectDetails.getCreatedBy());
								project.setUpdtaedBy(projectDetails.getUpdatedBy());
								project.setProjectCreatedOn(projectDetails.getCreatedOn());
								project.setProjectUpdatedOn(projectDetails.getUpdatedOn());
								project.setProjectStartDate(projectDetails.getStartDate());
								project.setProjectEndDate(projectDetails.getEndDate());
								project.setProjectCode(projectDetails.getProjectCode());
								project.setPriority(proManager.getPriority());
								project.setProjectManagerStartDate(proManager.getStartDate());
								project.setProjectManagerEndDate(proManager.getEndDate());
								project.setProjectManagerCreatedOn(proManager.getCreatedOn());
								project.setProjectManagerUpdatedOn(proManager.getUpdatedOn());
								project.setBuId(projectDetails.getBuID());
								project.setOpId(projectDetails.getOpID());
								project.setApplicationId(applicationDetails.getApplicationId());
								project.setProjectStatus(projectDetails.getEndDate().before(new Date()) ? false : true);
								projectsRepository.save(project);
							}
						}

						sequenceGeneratorRepository.save(updateSequence);
						applicationOnboardingRepository.save(parentRequest);
						if (applicationObject.getContractInfo() == null) {
							try {
								newApplicationEmailVerfication(parentRequest, requestId, null);
							} catch (IOException e) {
								throw new IOException(e);
							} catch (TemplateException e) {
								throw new TemplateException(e, null);
							} catch (DataValidationException e) {
								throw new DataValidationException(e.getMessage(), null, null);
							}
						}
					}
					return reviewSuccessResponse();
				} else {
					if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
						try {
							superAdminSaveData(requestId, childRequestId, profile, onboardingWorkFlowRequest);
						} catch (DataValidationException e) {
							if (e.getMessage().equalsIgnoreCase("404")) {
								throw new DataValidationException("Finance Department Does Not Exists To send Mail",
										requestId, HttpStatus.CONFLICT);
							} else {
								throw new DataValidationException("Provide Valid Id", requestId, HttpStatus.CONFLICT);
							}
						}
						return reviewSuccessResponse();
					} else {
						if (requestId != null) {
							ApplicationOnboarding rejectRequest = applicationOnboardingRepository
									.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
							if (rejectRequest == null) {
								throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null,
										HttpStatus.CONFLICT);
							}
							rejectRequest.setApprovedRejected(Constant.REJECTED);
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
							applicationOnboardingRepository.save(rejectRequest);
							return reviewFailureResponse();

						} else {
							ApplicationOnboarding rejectRequest = applicationOnboardingRepository
									.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
							if (rejectRequest == null) {
								throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null,
										HttpStatus.CONFLICT);
							}
							rejectRequest.setApprovedRejected(Constant.REJECTED);
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
							applicationOnboardingRepository.save(rejectRequest);
							return reviewFailureResponse();
						}
					}
				}
			} else {
				if (requestId != null) {
					ApplicationOnboarding rejectRequest = applicationOnboardingRepository.findByRequestNumber(requestId,
							Constant.APPROVER, Constant.REVIEW);
					if (rejectRequest == null) {
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null, HttpStatus.CONFLICT);
					}
					rejectRequest.setApprovedRejected(Constant.REJECTED);
					rejectRequest.setOnboardingStatus("Rejected by Approver");
					rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
					applicationOnboardingRepository.save(rejectRequest);
					return reviewFailureResponse();

				} else {
					ApplicationOnboarding rejectRequest = applicationOnboardingRepository
							.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
					if (rejectRequest == null) {
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null, HttpStatus.CONFLICT);
					}
					rejectRequest.setApprovedRejected(Constant.REJECTED);
					rejectRequest.setOnboardingStatus("Rejected by Approver");
					rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
					applicationOnboardingRepository.save(rejectRequest);
					return reviewFailureResponse();
				}
			}
		}
	}

	private void superAdminSaveData(String requestId, String childRequestId, UserLoginDetails profile,
			DeptOnboardingWorkFlowRequest onboardingWorkFlowRequest)
			throws DataValidationException, MessagingException, IOException, TemplateException, ParseException {
		if (requestId == null && childRequestId != null) {
			ApplicationOnboarding superAdminRequest = applicationOnboardingRepository
					.findAllBySuperAdmin(childRequestId);
			if (superAdminRequest == null) {
				throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null, HttpStatus.CONFLICT);
			}
			if (superAdminRequest.getWorkGroup().equalsIgnoreCase(Constant.REVIEWER)) {
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setWorkGroup(Constant.SUPER_ADMIN);
				superAdminRequest.setProjectName(superAdminRequest.getProjectName());
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setCreatedOn(new Date());

				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setUpdatedOn(new Date());

				ApplicationDetails applicationDetails = new ApplicationDetails();
				ApplicationSubscriptionDetails subscriptionDetails = new ApplicationSubscriptionDetails();
				PurchasedSingleAppOnboardingRequest applicationObject = applicationObjectDeserializer(
						superAdminRequest.getApplcationOnboardRequest());
				try {
					applicatoinObjectValidator(applicationObject);
				} catch (DataValidationException e) {
					throw new DataValidationException(e.getMessage(), null, null);
				}

				ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
						.findByApplicationName(applicationObject.getApplicationInfo().getApplicationName());

				String name = Constant.APPLICATION_ID;
				Integer sequence = sequenceGeneratorRepository.getApplicatiionDetailSequence();
				name = name.concat(sequence.toString());
				SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
				updateSequence.setApplicationDetails(updateSequence.getApplicationDetails() + 1);

				ApplicationCategoryMaster category = categoryMasterRepository
						.findByCategoryName(applicationObject.getApplicationInfo().getApplicationCategory());

				applicationDetails.setApplicationId(name);
				applicationDetails.setApplicationName(applicationObject.getApplicationInfo().getApplicationName());
				applicationDetails.setApplicationCategoryMaster(category);
				applicationDetails.setCategoryId(category.getCategoryId());
				applicationDetails.setBuID(Constant.BUID);
				applicationDetails.setStartDate(new Date());
				applicationDetails
						.setOwnerDepartment(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				applicationDetails.setCreatedOn(new Date());
				if (applicationObject.getContractInfo() != null) {
					applicationDetails.setActiveContracts(applicationObject.getContractInfo().size());
				}
				applicationDetails.setCreatedBy(profile.getEmailAddress());

				applicationDetails.setLogoUrl(applicationLogoEntity.getLogoUrl());
				applicationDetails.setApplicationStatus(Constant.ACTIVE);
				applicationDetails.setApplicationDescription(applicationLogoEntity.getDescription());

				applicationDetails.setProjectName(applicationObject.getApplicationInfo().getProjectName());

				DepartmentDetails details = departmentRepository
						.findByDepartmentName(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				List<ApplicationDetails> appDetails = details.getApplicationId();
				appDetails.add(applicationDetails);
				details.setApplicationId(appDetails);

				List<Departments> departmentsList = departmentsRepository
						.findByDepartmentName(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				if (!departmentsList.isEmpty()) {
					List<ApplicationDetails> appDetailsForDepartments = details.getApplicationId();
					StringBuilder applicationIdBuilder = new StringBuilder();
					for (ApplicationDetails detail : appDetailsForDepartments) {
						if (applicationIdBuilder.length() > 0) {
							applicationIdBuilder.append(", ");
						}
						applicationIdBuilder.append(detail.getApplicationId());
					}
					String applicationIds = applicationIdBuilder.toString();
					for (Departments department : departmentsList) {
						department.setApplicationId(applicationIds);
						try {
							BeanUtils.copyProperties(department, applicationDetails);
						} catch (IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
					departmentsRepository.saveAll(departmentsList);
				}

				Applications application = new Applications();
				application.setApplicationId(name);
				application.setApplicationName(applicationObject.getApplicationInfo().getApplicationName());
				application.setBuId(Constant.BUID);
				application.setCategoryName(category.getCategoryName());
				application.setStartDate(new Date());
				application.setCategoryId(category.getCategoryId());
				application.setOwnerDepartment(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				application.setCreatedOn(new Date());
				if (applicationObject.getContractInfo() != null) {
					application.setActiveContracts(applicationObject.getContractInfo().size());
				}
				application.setCreatedBy(profile.getEmailAddress());
				application.setLogoUrl(applicationLogoEntity.getLogoUrl());
				application.setApplicationStatus(Constant.ACTIVE);
				application.setApplicationDescription(applicationLogoEntity.getDescription());
				application.setProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
				application.setDepartmentId(details.getDepartmentId());
				application.setDepartmentName(details.getDepartmentName());

				departmentRepository.save(details);
				//departmentsRepository.save(departments);
				applicationDetailsRepository.save(applicationDetails);
				int i = 1;
				for (newApplicationOwnerDetailsRequest owners : applicationObject.getApplicationInfo()
						.getOwnerDetails()) {
					ApplicationOwnerDetails applicationOwnerDetails = new ApplicationOwnerDetails();
					applicationOwnerDetails.setApplicationId(name);
					applicationOwnerDetails.setCreatedOn(new Date());
					applicationOwnerDetails.setOwner(owners.getApplicaitonOwnerName());
					applicationOwnerDetails
							.setOwnerDepartment(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
					applicationOwnerDetails.setOwnerEmail(owners.getApplicationOwnerEmail());
					applicationOwnerDetails.setPriority(i);
					applicationOwnerRepository.save(applicationOwnerDetails);
					i++;
				}

				ProjectDetails projectDetails = projectDetailsRepository
						.findByProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
				List<ApplicationDetails> aps = projectDetails.getApplicationId();
				aps.add(applicationDetails);
				projectDetails.setApplicationId(aps);
				projectDetailsRepository.save(projectDetails);

				if (applicationObject.getApplicationInfo().getSubscriptionName().length() > 0
						|| !applicationObject.getApplicationInfo().getSubscriptionName().isEmpty()) {
					String subId = Constant.SUBSCRIPTION_ID;
					Integer subIdSequence = sequenceGeneratorRepository.getApplicationSubscriptionSequence();
					subId = subId.concat(subIdSequence.toString());
					SequenceGenerator updatesubIdSequence = sequenceGeneratorRepository.getById(1);
					updatesubIdSequence.setApplicationSubscription(++subIdSequence);

					String subscriptionId = subId;
					subscriptionDetails.setSubscriptionId(subscriptionId);
					subscriptionDetails
							.setSubscriptionName(applicationObject.getApplicationInfo().getSubscriptionName());
					subscriptionDetails
							.setSubscriptionNumber(applicationObject.getApplicationInfo().getSubscriptionId());
					subscriptionDetails.setApplicationId(applicationDetails);
					subscriptionDetails.setCreatedOn(new Date());
					subscriptionDetails.setBuID(Constant.BUID);
					subscriptionDetails.setOpID(Constant.SAASPE);
					subscriptionDetails.setStartDate(new Date());
					subscriptionDetails.setSubscriptionOwner(profile.getEmailAddress());
					subscriptionDetails.setSubscriptionStatus(Constant.ACTIVE);
					subscriptionDetails.setCreatedBy(profile.getFirstName() + " " + profile.getLastName());
					subscriptionDetails
							.setSubscriptionProvider(applicationLogoEntity.getProviderId().getProviderName());

					application.setSubscriptionId(subscriptionId);
					application.setSubscriptionName(applicationObject.getApplicationInfo().getSubscriptionName());
					application.setSubscriptionStartDate(new Date());
					application.setSubscriptionOwner(profile.getEmailAddress());
					application.setSubscriptionProvider(applicationLogoEntity.getProviderId().getProviderName());

					applicationSubscriptionDetailsRepository.save(subscriptionDetails);
					sequenceGeneratorRepository.save(updatesubIdSequence);
				}

				for (NewAppContractInfo contract : applicationObject.getContractInfo()) {
					ApplicationContractDetails applicationContractDetails = new ApplicationContractDetails();
					String contractSeqId = Constant.CONTRACT_ID;
					Integer contractSequence = sequenceGeneratorRepository.getContractSequence();
					contractSeqId = contractSeqId.concat(contractSequence.toString());
					SequenceGenerator contractupdateSequence = sequenceGeneratorRepository.getById(1);
					contractupdateSequence.setApplicationContacts(++contractSequence);

					applicationContractDetails.setContractId(contractSeqId);
					applicationContractDetails.setApplicationId(applicationDetails);
					applicationContractDetails.setContractName(contract.getContractName());
					applicationContractDetails.setContractStartDate(contract.getContractStartDate());
					applicationContractDetails.setContractEndDate(contract.getContractEndDate());
					if (Boolean.TRUE.equals(contract.getAutoRenewal())) {
						applicationContractDetails.setRenewalDate(contract.getUpcomingRenewalDate());
					} else {
						applicationContractDetails.setRenewalDate(contract.getContractEndDate());
					}
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
					applicationContractDetails.setBuID(Constant.BUID);
					applicationContractDetails.setCreatedOn(new Date());
					applicationContractDetails.setStartDate(new Date());
					applicationContractDetails.setContractType(contract.getContractType());
					applicationContractDetails.setAutoRenewalCancellation(contract.getAutoRenewalCancellation());
					if (ContractType.monthToMonth(contract.getContractType())) {
						applicationContractDetails.setBillingFrequency(null);
						applicationContractDetails.setContractTenure(null);
					} else {
						applicationContractDetails.setBillingFrequency(contract.getBillingFrequency());
						applicationContractDetails.setContractTenure(Integer.valueOf(contract.getContractTenure()));
					}
					applicationContractDetails.setCreatedBy(profile.getEmailAddress());
					applicationContractDetails.setAutoRenew(contract.getAutoRenewal());
					applicationContractDetails.setSubscriptionDetails(subscriptionDetails);
					applicationContractDetails.setContractPaymentMethod(contract.getPaymentMethod());
					applicationContractDetails.setContractCurrency(contract.getCurrencyCode());
					applicationContractDetails.setContractOwner(profile.getFirstName() + " " + profile.getLastName());

					String invseSeq = Constant.INVOICE_ID;
					Integer invSequence = sequenceGeneratorRepository.getPaymentReqSequence();
					invseSeq = invseSeq.concat(invSequence.toString());
					SequenceGenerator invoiceUpdateSequence = sequenceGeneratorRepository.getById(1);
					invoiceUpdateSequence.setPaymentSequenceId(++invSequence);

					PaymentDetails paymentDetails = new PaymentDetails();
					paymentDetails.setInvoiceNo(invseSeq);
					paymentDetails.setApplicationId(name);
					paymentDetails.setBuID(Constant.BUID);
					paymentDetails.setCreatedBy(profile.getEmailAddress());
					paymentDetails.setCreatedOn(new Date());
					paymentDetails.setStartDate(new Date());
					paymentDetails.setPaymentMethod(contract.getPaymentMethod());
					paymentDetails.setCardholderName(contract.getCardHolderName());
					if (contract.getCardNumber() != null) {
						String cardNo = Base64.getEncoder()
								.encodeToString(contract.getCardNumber().toString().getBytes(StandardCharsets.UTF_8));
						String trimmedCardNo = cardNo.replaceAll("\\s", "").trim();
						paymentDetails.setCardNumber(trimmedCardNo);
					}
					paymentDetails.setValidThrough(contract.getValidThrough());
					paymentDetails.setWalletName(contract.getWalletName());
					paymentDetails.setContractId(contractSeqId);
					paymentDetailsRepository.save(paymentDetails);
					sequenceGeneratorRepository.save(invoiceUpdateSequence);

					contractDetailsRepository.save(applicationContractDetails);
					sequenceGeneratorRepository.save(contractupdateSequence);
				}

				for (NewAppLicenseInfo license : applicationObject.getProducts()) {
					String licenseSeq = Constant.LICENSE_ID;
					Integer licenseSequence = sequenceGeneratorRepository.getLicenseSequence();
					licenseSeq = licenseSeq.concat(licenseSequence.toString());
					SequenceGenerator licenseUpdateSequence = sequenceGeneratorRepository.getById(1);
					licenseUpdateSequence.setApplicatiionLicense(++licenseSequence);

					ApplicationContractDetails contractDetails = contractDetailsRepository
							.findByContractName(license.getContractName());
					ApplicationLicenseDetails applicationLicenseDetails = licenseContractTotalCost(contractDetails,
							license);

					PaymentDetails pay = paymentDetailsRepository.findByContractId(contractDetails.getContractId());
					applicationLicenseDetails.setApplicationId(applicationDetails);
					applicationLicenseDetails.setContractId(contractDetails);
					applicationLicenseDetails.setCreatedBy(profile.getFirstName());
					applicationLicenseDetails.setCreatedOn(new Date());
					applicationLicenseDetails.setStartDate(new Date());
					BigDecimal afterCost = BigDecimal.valueOf(0);
					BigDecimal beforeCost = BigDecimal.valueOf(0);
					if (contractDetails != null) {
						if (contractDetails.getContractStartDate().after(new Date())) {
							afterCost = getConvertedLicenseCost(contractDetails.getStartDate(),
									contractDetails.getContractCurrency(), applicationLicenseDetails.getTotalCost());
							applicationLicenseDetails.setConvertedCost(afterCost);
						} else {
							beforeCost = getConvertedLicenseCost(contractDetails.getContractStartDate(),
									contractDetails.getContractCurrency(), applicationLicenseDetails.getTotalCost());
							applicationLicenseDetails.setConvertedCost(beforeCost);
						}
						applicationLicenseDetails.setLicenseStartDate(contractDetails.getContractStartDate());
						applicationLicenseDetails.setLicenseEndDate(contractDetails.getContractEndDate());
					}
					applicationLicenseDetails.setBuID(Constant.BUID);
					applicationLicenseDetails.setLicenseId(licenseSeq);
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setProductName(license.getProductName());
					applicationLicenseDetails.setCurrency(license.getCurrencyCode());
					applicationLicenseDetails.setUnitPrice(license.getUnitPrice());
					applicationLicenseDetails.setProductCategory(license.getProductType());
					if (ProductType.professionalServices(license.getProductType())
							|| ProductType.platform(license.getProductType())) {
						applicationLicenseDetails.setQuantity(1);
						applicationLicenseDetails.setLicenseUnMapped(1);
					} else {
						applicationLicenseDetails.setQuantity(license.getQuantity());
						applicationLicenseDetails.setLicenseUnMapped(license.getQuantity());
					}
					applicationLicenseDetails.setLicenseMapped(0);

					if (application.getLicenseId() == null) {
						application.setContractId(contractDetails.getContractId());
						application.setAutoRenew(contractDetails.getAutoRenew());
						application.setContractCurrency(contractDetails.getContractCurrency());
						application.setContractDescription(contractDetails.getContractDescription());
						application.setContractEndDate(contractDetails.getContractEndDate());
						application.setContractName(contractDetails.getContractName());
						application.setContractNoticeDate(contractDetails.getContractNoticeDate());
						application.setContractOwner(contractDetails.getContractOwner());
						application.setContractOwnerEmail(contractDetails.getContractOwnerEmail());
						application.setPaymentMethod(contractDetails.getContractPaymentMethod());
						application.setPaymentTerm(contractDetails.getContractPaymentTerm());
						application.setContractProvider(contractDetails.getContractProvider());
						application.setContractStartDate(contractDetails.getContractStartDate());
						application.setContractStatus(contractDetails.getContractStatus());
						application.setReminderDate(contractDetails.getReminderDate());
						application.setRenewalTerm(contractDetails.getRenewalTerm());
						application.setContractType(contractDetails.getContractType());
						application.setBillingFrequency(contractDetails.getBillingFrequency());
						if (contractDetails.getAutoRenewalCancellation() != null)
							application.setAutoRenewalCancellation(contractDetails.getAutoRenewalCancellation());
						application.setContractTenure(contractDetails.getContractTenure());
						application.setLicenseId(licenseSeq);
						application.setLicenseStartDate(contractDetails.getContractStartDate());
						application.setLicenseEndDate(contractDetails.getContractEndDate());
						application.setUnitPriceType(license.getUnitPriceType());
						application.setProductName(license.getProductName());
						application.setCurrency(license.getCurrencyCode());
						application.setUnitPrice(license.getUnitPrice());
						application.setProductCategory(license.getProductType());
						application.setLicenseMapped(0);
						if (ProductType.professionalServices(license.getProductType())
								|| ProductType.platform(license.getProductType())) {
							application.setQuantity(1);
							application.setLicenseUnmapped(1);
						} else {
							application.setQuantity(license.getQuantity());
							application.setLicenseUnmapped(license.getQuantity());
						}
						application.setTotalCost(applicationLicenseDetails.getTotalCost());
						if (contractDetails.getContractStartDate().after(new Date())) {
							application.setConvertedCost(afterCost);
						} else {
							application.setConvertedCost(beforeCost);
						}
						application.setPaymentDescription(pay.getDescription());
						application.setPaymentTransactionDate(pay.getTransactionDate());
						application.setPaymentAmount(pay.getAmount());
						application.setCardholderName(pay.getCardholderName());
						application.setCardNumber(pay.getCardNumber());
						application.setValidThrough(pay.getValidThrough());
						application.setWalletName(pay.getWalletName());
						application.setPaymentStartDate(pay.getStartDate());
						application.setPaymentEndDate(pay.getEndDate());
						application.setPaymentSecretKey(pay.getSecretKey());
						applicationRepository.save(application);
					} else if (!application.getLicenseId().equals(licenseSeq)) {
						Applications app = new Applications();
						try {
							BeanUtils.copyProperties(app, application);
							app.setId(0);
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						app.setContractId(contractDetails.getContractId());
						app.setAutoRenew(contractDetails.getAutoRenew());
						app.setContractCurrency(contractDetails.getContractCurrency());
						app.setContractDescription(contractDetails.getContractDescription());
						app.setContractEndDate(contractDetails.getContractEndDate());
						app.setContractName(contractDetails.getContractName());
						app.setContractNoticeDate(contractDetails.getContractNoticeDate());
						app.setContractOwner(contractDetails.getContractOwner());
						app.setContractOwnerEmail(contractDetails.getContractOwnerEmail());
						app.setPaymentMethod(contractDetails.getContractPaymentMethod());
						app.setPaymentTerm(contractDetails.getContractPaymentTerm());
						app.setContractProvider(contractDetails.getContractProvider());
						app.setContractStartDate(contractDetails.getContractStartDate());
						app.setContractStatus(contractDetails.getContractStatus());
						app.setReminderDate(contractDetails.getReminderDate());
						app.setRenewalTerm(contractDetails.getRenewalTerm());
						app.setContractType(contractDetails.getContractType());
						app.setBillingFrequency(contractDetails.getBillingFrequency());
						if (contractDetails.getAutoRenewalCancellation() != null)
							app.setAutoRenewalCancellation(contractDetails.getAutoRenewalCancellation());
						app.setContractTenure(contractDetails.getContractTenure());
						app.setLicenseId(licenseSeq);
						app.setLicenseStartDate(contractDetails.getContractStartDate());
						app.setLicenseEndDate(contractDetails.getContractEndDate());
						app.setUnitPriceType(license.getUnitPriceType());
						app.setProductName(license.getProductName());
						app.setCurrency(license.getCurrencyCode());
						app.setUnitPrice(license.getUnitPrice());
						app.setProductCategory(license.getProductType());
						app.setLicenseMapped(0);
						if (ProductType.professionalServices(license.getProductType())
								|| ProductType.platform(license.getProductType())) {
							app.setQuantity(1);
							app.setLicenseUnmapped(1);
						} else {
							app.setQuantity(license.getQuantity());
							app.setLicenseUnmapped(license.getQuantity());
						}
						app.setTotalCost(applicationLicenseDetails.getTotalCost());
						if (contractDetails.getContractStartDate().after(new Date())) {
							app.setConvertedCost(afterCost);
						} else {
							app.setConvertedCost(beforeCost);
						}
						app.setPaymentDescription(pay.getDescription());
						app.setPaymentTransactionDate(pay.getTransactionDate());
						app.setPaymentAmount(pay.getAmount());
						app.setCardholderName(pay.getCardholderName());
						app.setCardNumber(pay.getCardNumber());
						app.setValidThrough(pay.getValidThrough());
						app.setWalletName(pay.getWalletName());
						app.setPaymentStartDate(pay.getStartDate());
						app.setPaymentEndDate(pay.getEndDate());
						app.setPaymentSecretKey(pay.getSecretKey());
						app.setId(70990);
						applicationRepository.save(app);
					}

					licenseDetailsRepository.save(applicationLicenseDetails);
					sequenceGeneratorRepository.save(licenseUpdateSequence);
				}

				List<ProjectManagerDetails> proManagers = projectOwnerRepository
						.findByProjectIdOrderByEndDate(projectDetails.getProjectId());
				List<Projects> existingProject = projectsRepository.findByProjectId(projectDetails.getProjectId());
				for (ProjectManagerDetails proManager : proManagers) {
					if (!existingProject.isEmpty()) {
						for (Projects project : existingProject) {
							if (project.getProjectManagerEmail().equals(proManager.getProjectManagerEmail())) {
								project.setProjectManagerEmail(proManager.getProjectManagerEmail());
								project.setPriority(proManager.getPriority());
								project.setProjectManagerStartDate(proManager.getStartDate());
								project.setProjectManagerEndDate(proManager.getEndDate());
								project.setProjectManagerCreatedOn(proManager.getCreatedOn());
								project.setProjectManagerUpdatedOn(proManager.getUpdatedOn());
								project.setApplicationId(applicationDetails.getApplicationId());
								project.setProjectStatus(projectDetails.getEndDate().before(new Date()) ? false : true);
								projectsRepository.save(project);
							}
						}
					} else {
						Projects project = new Projects();
						project.setProjectId(projectDetails.getProjectId());
						project.setProjectName(projectDetails.getProjectName());
						project.setProjectManagerEmail(proManager.getProjectManagerEmail());
						project.setBudget(projectDetails.getProjectBudget());
						project.setBudgetCurrency(projectDetails.getBudgetCurrency());
						project.setDescription(projectDetails.getProjectDescription());
						project.setCreatedBy(projectDetails.getCreatedBy());
						project.setUpdtaedBy(projectDetails.getUpdatedBy());
						project.setProjectCreatedOn(projectDetails.getCreatedOn());
						project.setProjectUpdatedOn(projectDetails.getUpdatedOn());
						project.setProjectStartDate(projectDetails.getStartDate());
						project.setProjectEndDate(projectDetails.getEndDate());
						project.setProjectCode(projectDetails.getProjectCode());
						project.setPriority(proManager.getPriority());
						project.setProjectManagerStartDate(proManager.getStartDate());
						project.setProjectManagerEndDate(proManager.getEndDate());
						project.setProjectManagerCreatedOn(proManager.getCreatedOn());
						project.setProjectManagerUpdatedOn(proManager.getUpdatedOn());
						project.setBuId(projectDetails.getBuID());
						project.setOpId(projectDetails.getOpID());
						project.setApplicationId(applicationDetails.getApplicationId());
						project.setProjectStatus(projectDetails.getEndDate().before(new Date()) ? false : true);
						projectsRepository.save(project);
					}
				}

				sequenceGeneratorRepository.save(updateSequence);
				applicationOnboardingRepository.save(superAdminRequest);
				if (applicationObject.getContractInfo() == null) {
					try {
						newApplicationEmailVerfication(superAdminRequest, null, childRequestId);
					} catch (IOException e) {
						throw new IOException(e);
					} catch (TemplateException e) {
						throw new TemplateException(e, null);
					} catch (DataValidationException e) {
						throw new DataValidationException(e.getMessage(), null, null);
					}
				}
			} else {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setWorkGroup(Constant.SUPER_ADMIN);
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setUpdatedOn(new Date());

				ApplicationDetails applicationDetails = new ApplicationDetails();
				ApplicationSubscriptionDetails subscriptionDetails = new ApplicationSubscriptionDetails();
				PurchasedSingleAppOnboardingRequest applicationObject = applicationObjectDeserializer(
						superAdminRequest.getApplcationOnboardRequest());
				applicatoinObjectValidator(applicationObject, 0);

				ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
						.findByApplicationName(applicationObject.getApplicationInfo().getApplicationName());

				String name = Constant.APPLICATION_ID;
				Integer sequence = sequenceGeneratorRepository.getApplicatiionDetailSequence();
				name = name.concat(sequence.toString());
				SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
				updateSequence.setApplicationDetails(updateSequence.getApplicationDetails() + 1);

				ApplicationCategoryMaster category = categoryMasterRepository
						.findByCategoryName(applicationObject.getApplicationInfo().getApplicationCategory());

				applicationDetails.setApplicationId(name);
				applicationDetails.setApplicationName(applicationObject.getApplicationInfo().getApplicationName());
				applicationDetails.setBuID(Constant.BUID);
				applicationDetails.setStartDate(new Date());
				applicationDetails.setApplicationCategoryMaster(category);
				applicationDetails.setCategoryId(category.getCategoryId());
				applicationDetails
						.setOwnerDepartment(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				applicationDetails.setCreatedOn(new Date());
				if (applicationObject.getContractInfo() != null) {
					applicationDetails.setActiveContracts(applicationObject.getContractInfo().size());
				}
				applicationDetails.setCreatedBy(profile.getEmailAddress());

				applicationDetails.setLogoUrl(applicationLogoEntity.getLogoUrl());
				applicationDetails.setApplicationStatus(Constant.ACTIVE);
				applicationDetails.setApplicationDescription(applicationLogoEntity.getDescription());

				applicationDetails.setProjectName(applicationObject.getApplicationInfo().getProjectName());

				DepartmentDetails details = departmentRepository
						.findByDepartmentName(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				List<ApplicationDetails> appDetails = details.getApplicationId();
				appDetails.add(applicationDetails);
				details.setApplicationId(appDetails);

				List<Departments> departmentsList = departmentsRepository
						.findByDepartmentName(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				if (!departmentsList.isEmpty()) {
					List<ApplicationDetails> appDetailsForDepartments = details.getApplicationId();
					StringBuilder applicationIdBuilder = new StringBuilder();
					for (ApplicationDetails detail : appDetailsForDepartments) {
						if (applicationIdBuilder.length() > 0) {
							applicationIdBuilder.append(", ");
						}
						applicationIdBuilder.append(detail.getApplicationId());
					}
					String applicationIds = applicationIdBuilder.toString();
					for (Departments department : departmentsList) {
						department.setApplicationId(applicationIds);
						try {
							BeanUtils.copyProperties(department, applicationDetails);
						} catch (IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
					departmentsRepository.saveAll(departmentsList);
				}
				
				Applications application = new Applications();
				application.setApplicationId(name);
				application.setApplicationName(applicationObject.getApplicationInfo().getApplicationName());
				application.setBuId(Constant.BUID);
				application.setCategoryName(category.getCategoryName());
				application.setStartDate(new Date());
				application.setCategoryId(category.getCategoryId());
				application.setOwnerDepartment(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				application.setCreatedOn(new Date());
				if (applicationObject.getContractInfo() != null) {
					application.setActiveContracts(applicationObject.getContractInfo().size());
				}
				application.setCreatedBy(profile.getEmailAddress());
				application.setLogoUrl(applicationLogoEntity.getLogoUrl());
				application.setApplicationStatus(Constant.ACTIVE);
				application.setApplicationDescription(applicationLogoEntity.getDescription());
				application.setProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
				application.setDepartmentId(details.getDepartmentId());
				application.setDepartmentName(details.getDepartmentName());

				departmentRepository.save(details);
				applicationDetailsRepository.save(applicationDetails);
				int i = 1;
				for (newApplicationOwnerDetailsRequest owners : applicationObject.getApplicationInfo()
						.getOwnerDetails()) {
					ApplicationOwnerDetails applicationOwnerDetails = new ApplicationOwnerDetails();
					applicationOwnerDetails.setApplicationId(name);
					applicationOwnerDetails.setCreatedOn(new Date());
					applicationOwnerDetails.setOwner(owners.getApplicaitonOwnerName());
					applicationOwnerDetails
							.setOwnerDepartment(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
					applicationOwnerDetails.setOwnerEmail(owners.getApplicationOwnerEmail());
					applicationOwnerDetails.setPriority(i);
					applicationOwnerRepository.save(applicationOwnerDetails);
					i++;
				}

				ProjectDetails projectDetails = projectDetailsRepository
						.findByProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
				List<ApplicationDetails> aps = projectDetails.getApplicationId();
				aps.add(applicationDetails);
				projectDetails.setApplicationId(aps);
				projectDetailsRepository.save(projectDetails);

				if (applicationObject.getApplicationInfo().getSubscriptionName().length() > 0
						|| !applicationObject.getApplicationInfo().getSubscriptionName().isEmpty()) {
					String subId = Constant.SUBSCRIPTION_ID;
					Integer subIdSequence = sequenceGeneratorRepository.getApplicationSubscriptionSequence();
					subId = subId.concat(subIdSequence.toString());
					SequenceGenerator updatesubIdSequence = sequenceGeneratorRepository.getById(1);
					updatesubIdSequence.setApplicationSubscription(++subIdSequence);

					String subscriptionId = subId;
					subscriptionDetails.setSubscriptionId(subscriptionId);
					subscriptionDetails
							.setSubscriptionName(applicationObject.getApplicationInfo().getSubscriptionName());
					subscriptionDetails
							.setSubscriptionNumber(applicationObject.getApplicationInfo().getSubscriptionId());
					subscriptionDetails.setApplicationId(applicationDetails);
					subscriptionDetails.setCreatedOn(new Date());
					subscriptionDetails.setBuID(Constant.BUID);
					subscriptionDetails.setOpID(Constant.SAASPE);
					subscriptionDetails.setStartDate(new Date());
					subscriptionDetails.setSubscriptionOwner(profile.getEmailAddress());
					subscriptionDetails.setSubscriptionStatus(Constant.ACTIVE);
					subscriptionDetails.setCreatedBy(profile.getFirstName() + " " + profile.getLastName());
					subscriptionDetails
							.setSubscriptionProvider(applicationLogoEntity.getProviderId().getProviderName());

					application.setSubscriptionId(subscriptionId);
					application.setSubscriptionName(applicationObject.getApplicationInfo().getSubscriptionName());
					application.setSubscriptionStartDate(new Date());
					application.setSubscriptionOwner(profile.getEmailAddress());
					application.setSubscriptionProvider(applicationLogoEntity.getProviderId().getProviderName());

					applicationSubscriptionDetailsRepository.save(subscriptionDetails);
					sequenceGeneratorRepository.save(updatesubIdSequence);
				}

				for (NewAppContractInfo contract : applicationObject.getContractInfo()) {
					ApplicationContractDetails applicationContractDetails = new ApplicationContractDetails();
					String contractSeqId = Constant.CONTRACT_ID;
					Integer contractSequence = sequenceGeneratorRepository.getContractSequence();
					contractSeqId = contractSeqId.concat(contractSequence.toString());
					SequenceGenerator contractupdateSequence = sequenceGeneratorRepository.getById(1);
					contractupdateSequence.setApplicationContacts(++contractSequence);

					applicationContractDetails.setContractId(contractSeqId);
					applicationContractDetails.setApplicationId(applicationDetails);
					applicationContractDetails.setContractName(contract.getContractName());
					applicationContractDetails.setContractStartDate(contract.getContractStartDate());
					applicationContractDetails.setContractEndDate(contract.getContractEndDate());
					if (Boolean.TRUE.equals(contract.getAutoRenewal())) {
						applicationContractDetails.setRenewalDate(contract.getUpcomingRenewalDate());
					} else {
						applicationContractDetails.setRenewalDate(contract.getContractEndDate());
					}
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
					applicationContractDetails.setBuID(Constant.BUID);
					applicationContractDetails.setStartDate(new Date());
					applicationContractDetails.setContractType(contract.getContractType());
					applicationContractDetails.setAutoRenewalCancellation(contract.getAutoRenewalCancellation());
					if (ContractType.monthToMonth(contract.getContractType())) {
						applicationContractDetails.setBillingFrequency(null);
						applicationContractDetails.setContractTenure(null);
					} else {
						applicationContractDetails.setBillingFrequency(contract.getBillingFrequency());
						applicationContractDetails.setContractTenure(Integer.valueOf(contract.getContractTenure()));
					}
					applicationContractDetails.setCreatedOn(new Date());
					applicationContractDetails.setCreatedBy(profile.getEmailAddress());
					applicationContractDetails.setAutoRenew(contract.getAutoRenewal());
					applicationContractDetails.setSubscriptionDetails(subscriptionDetails);
					applicationContractDetails.setContractPaymentMethod(contract.getPaymentMethod());
					applicationContractDetails.setContractCurrency(contract.getCurrencyCode());
					applicationContractDetails.setContractOwner(profile.getFirstName() + " " + profile.getLastName());

					String invseSeq = Constant.INVOICE_ID;
					Integer invSequence = sequenceGeneratorRepository.getPaymentReqSequence();
					invseSeq = invseSeq.concat(invSequence.toString());
					SequenceGenerator invoiceUpdateSequence = sequenceGeneratorRepository.getById(1);
					invoiceUpdateSequence.setPaymentSequenceId(++invSequence);

					PaymentDetails paymentDetails = new PaymentDetails();
					paymentDetails.setInvoiceNo(invseSeq);
					paymentDetails.setApplicationId(name);
					paymentDetails.setBuID(Constant.BUID);
					paymentDetails.setCreatedBy(profile.getEmailAddress());
					paymentDetails.setCreatedOn(new Date());
					paymentDetails.setStartDate(new Date());
					paymentDetails.setPaymentMethod(contract.getPaymentMethod());
					paymentDetails.setCardholderName(contract.getCardHolderName());
					if (contract.getCardNumber() != null) {
						String cardNo = Base64.getEncoder()
								.encodeToString(contract.getCardNumber().toString().getBytes(StandardCharsets.UTF_8));
						String trimmedCardNo = cardNo.replaceAll("\\s", "").trim();
						paymentDetails.setCardNumber(trimmedCardNo);
					}
					paymentDetails.setValidThrough(contract.getValidThrough());
					paymentDetails.setWalletName(contract.getWalletName());
					paymentDetails.setContractId(contractSeqId);
					paymentDetailsRepository.save(paymentDetails);
					sequenceGeneratorRepository.save(invoiceUpdateSequence);

					contractDetailsRepository.save(applicationContractDetails);
					sequenceGeneratorRepository.save(contractupdateSequence);
				}

				for (NewAppLicenseInfo license : applicationObject.getProducts()) {
					String licenseSeq = Constant.LICENSE_ID;
					Integer licenseSequence = sequenceGeneratorRepository.getLicenseSequence();
					licenseSeq = licenseSeq.concat(licenseSequence.toString());
					SequenceGenerator licenseUpdateSequence = sequenceGeneratorRepository.getById(1);
					licenseUpdateSequence.setApplicatiionLicense(++licenseSequence);

					ApplicationContractDetails contractDetails = contractDetailsRepository
							.findByContractName(license.getContractName());
					ApplicationLicenseDetails applicationLicenseDetails = licenseContractTotalCost(contractDetails,
							license);
					PaymentDetails pay = paymentDetailsRepository.findByContractId(contractDetails.getContractId());
					applicationLicenseDetails.setApplicationId(applicationDetails);
					applicationLicenseDetails.setContractId(contractDetails);
					applicationLicenseDetails.setCreatedBy(profile.getFirstName());
					applicationLicenseDetails.setCreatedOn(new Date());
					BigDecimal afterCost = BigDecimal.valueOf(0);
					BigDecimal beforeCost = BigDecimal.valueOf(0);
					if (contractDetails != null) {
						if (contractDetails.getContractStartDate().after(new Date())) {
							afterCost = getConvertedLicenseCost(contractDetails.getStartDate(),
									contractDetails.getContractCurrency(), applicationLicenseDetails.getTotalCost());
							applicationLicenseDetails.setConvertedCost(afterCost);
						} else {
							beforeCost = getConvertedLicenseCost(contractDetails.getContractStartDate(),
									contractDetails.getContractCurrency(), applicationLicenseDetails.getTotalCost());
							applicationLicenseDetails.setConvertedCost(beforeCost);
						}
						applicationLicenseDetails.setLicenseStartDate(contractDetails.getContractStartDate());
						applicationLicenseDetails.setLicenseEndDate(contractDetails.getContractEndDate());
					}
					applicationLicenseDetails.setBuID(Constant.BUID);
					applicationLicenseDetails.setStartDate(new Date());
					applicationLicenseDetails.setLicenseId(licenseSeq);
					applicationLicenseDetails.setProductName(license.getProductName());
					applicationLicenseDetails.setCurrency(license.getCurrencyCode());
					applicationLicenseDetails.setUnitPrice(license.getUnitPrice());
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setProductCategory(license.getProductType());
					if (ProductType.professionalServices(license.getProductType())
							|| ProductType.platform(license.getProductType())) {
						applicationLicenseDetails.setQuantity(1);
						applicationLicenseDetails.setLicenseUnMapped(1);
					} else {
						applicationLicenseDetails.setQuantity(license.getQuantity());
						applicationLicenseDetails.setLicenseUnMapped(license.getQuantity());
					}
					applicationLicenseDetails.setLicenseMapped(0);

					if (application.getLicenseId() == null) {
						application.setContractId(contractDetails.getContractId());
						application.setAutoRenew(contractDetails.getAutoRenew());
						application.setContractCurrency(contractDetails.getContractCurrency());
						application.setContractDescription(contractDetails.getContractDescription());
						application.setContractEndDate(contractDetails.getContractEndDate());
						application.setContractName(contractDetails.getContractName());
						application.setContractNoticeDate(contractDetails.getContractNoticeDate());
						application.setContractOwner(contractDetails.getContractOwner());
						application.setContractOwnerEmail(contractDetails.getContractOwnerEmail());
						application.setPaymentMethod(contractDetails.getContractPaymentMethod());
						application.setPaymentTerm(contractDetails.getContractPaymentTerm());
						application.setContractProvider(contractDetails.getContractProvider());
						application.setContractStartDate(contractDetails.getContractStartDate());
						application.setContractStatus(contractDetails.getContractStatus());
						application.setReminderDate(contractDetails.getReminderDate());
						application.setRenewalTerm(contractDetails.getRenewalTerm());
						application.setContractType(contractDetails.getContractType());
						application.setBillingFrequency(contractDetails.getBillingFrequency());
						if (contractDetails.getAutoRenewalCancellation() != null)
							application.setAutoRenewalCancellation(contractDetails.getAutoRenewalCancellation());
						application.setContractTenure(contractDetails.getContractTenure());
						application.setLicenseId(licenseSeq);
						application.setLicenseStartDate(contractDetails.getContractStartDate());
						application.setLicenseEndDate(contractDetails.getContractEndDate());
						application.setUnitPriceType(license.getUnitPriceType());
						application.setProductName(license.getProductName());
						application.setCurrency(license.getCurrencyCode());
						application.setUnitPrice(license.getUnitPrice());
						application.setProductCategory(license.getProductType());
						application.setLicenseMapped(0);
						if (ProductType.professionalServices(license.getProductType())
								|| ProductType.platform(license.getProductType())) {
							application.setQuantity(1);
							application.setLicenseUnmapped(1);
						} else {
							application.setQuantity(license.getQuantity());
							application.setLicenseUnmapped(license.getQuantity());
						}
						application.setTotalCost(applicationLicenseDetails.getTotalCost());
						if (contractDetails.getContractStartDate().after(new Date())) {
							application.setConvertedCost(afterCost);
						} else {
							application.setConvertedCost(beforeCost);
						}
						application.setPaymentDescription(pay.getDescription());
						application.setPaymentTransactionDate(pay.getTransactionDate());
						application.setPaymentAmount(pay.getAmount());
						application.setCardholderName(pay.getCardholderName());
						application.setCardNumber(pay.getCardNumber());
						application.setValidThrough(pay.getValidThrough());
						application.setWalletName(pay.getWalletName());
						application.setPaymentStartDate(pay.getStartDate());
						application.setPaymentEndDate(pay.getEndDate());
						application.setPaymentSecretKey(pay.getSecretKey());
						applicationRepository.save(application);
					} else if (!application.getLicenseId().equals(licenseSeq)) {
						Applications app = new Applications();
						try {
							BeanUtils.copyProperties(app, application);
							app.setId(0);
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						app.setContractId(contractDetails.getContractId());
						app.setAutoRenew(contractDetails.getAutoRenew());
						app.setContractCurrency(contractDetails.getContractCurrency());
						app.setContractDescription(contractDetails.getContractDescription());
						app.setContractEndDate(contractDetails.getContractEndDate());
						app.setContractName(contractDetails.getContractName());
						app.setContractNoticeDate(contractDetails.getContractNoticeDate());
						app.setContractOwner(contractDetails.getContractOwner());
						app.setContractOwnerEmail(contractDetails.getContractOwnerEmail());
						app.setPaymentMethod(contractDetails.getContractPaymentMethod());
						app.setPaymentTerm(contractDetails.getContractPaymentTerm());
						app.setContractProvider(contractDetails.getContractProvider());
						app.setContractStartDate(contractDetails.getContractStartDate());
						app.setContractStatus(contractDetails.getContractStatus());
						app.setReminderDate(contractDetails.getReminderDate());
						app.setRenewalTerm(contractDetails.getRenewalTerm());
						app.setContractType(contractDetails.getContractType());
						app.setBillingFrequency(contractDetails.getBillingFrequency());
						if (contractDetails.getAutoRenewalCancellation() != null)
							app.setAutoRenewalCancellation(contractDetails.getAutoRenewalCancellation());
						app.setContractTenure(contractDetails.getContractTenure());
						app.setLicenseId(licenseSeq);
						app.setLicenseStartDate(contractDetails.getContractStartDate());
						app.setLicenseEndDate(contractDetails.getContractEndDate());
						app.setUnitPriceType(license.getUnitPriceType());
						app.setProductName(license.getProductName());
						app.setCurrency(license.getCurrencyCode());
						app.setUnitPrice(license.getUnitPrice());
						app.setProductCategory(license.getProductType());
						app.setLicenseMapped(0);
						if (ProductType.professionalServices(license.getProductType())
								|| ProductType.platform(license.getProductType())) {
							app.setQuantity(1);
							app.setLicenseUnmapped(1);
						} else {
							app.setQuantity(license.getQuantity());
							app.setLicenseUnmapped(license.getQuantity());
						}
						app.setTotalCost(applicationLicenseDetails.getTotalCost());
						if (contractDetails.getContractStartDate().after(new Date())) {
							app.setConvertedCost(afterCost);
						} else {
							app.setConvertedCost(beforeCost);
						}
						app.setPaymentDescription(pay.getDescription());
						app.setPaymentTransactionDate(pay.getTransactionDate());
						app.setPaymentAmount(pay.getAmount());
						app.setCardholderName(pay.getCardholderName());
						app.setCardNumber(pay.getCardNumber());
						app.setValidThrough(pay.getValidThrough());
						app.setWalletName(pay.getWalletName());
						app.setPaymentStartDate(pay.getStartDate());
						app.setPaymentEndDate(pay.getEndDate());
						app.setPaymentSecretKey(pay.getSecretKey());
						applicationRepository.save(app);
					}

					licenseDetailsRepository.save(applicationLicenseDetails);
					sequenceGeneratorRepository.save(licenseUpdateSequence);
				}

				List<ProjectManagerDetails> proManagers = projectOwnerRepository
						.findByProjectIdOrderByEndDate(projectDetails.getProjectId());
				List<Projects> existingProject = projectsRepository.findByProjectId(projectDetails.getProjectId());
				for (ProjectManagerDetails proManager : proManagers) {
					if (!existingProject.isEmpty()) {
						for (Projects project : existingProject) {
							if (project.getProjectManagerEmail().equals(proManager.getProjectManagerEmail())) {
								project.setProjectManagerEmail(proManager.getProjectManagerEmail());
								project.setPriority(proManager.getPriority());
								project.setProjectManagerStartDate(proManager.getStartDate());
								project.setProjectManagerEndDate(proManager.getEndDate());
								project.setProjectManagerCreatedOn(proManager.getCreatedOn());
								project.setProjectManagerUpdatedOn(proManager.getUpdatedOn());
								project.setApplicationId(applicationDetails.getApplicationId());
								project.setProjectStatus(projectDetails.getEndDate().before(new Date()) ? false : true);
								projectsRepository.save(project);
							}
						}
					} else {
						Projects project = new Projects();
						project.setProjectId(projectDetails.getProjectId());
						project.setProjectName(projectDetails.getProjectName());
						project.setProjectManagerEmail(proManager.getProjectManagerEmail());
						project.setBudget(projectDetails.getProjectBudget());
						project.setBudgetCurrency(projectDetails.getBudgetCurrency());
						project.setDescription(projectDetails.getProjectDescription());
						project.setCreatedBy(projectDetails.getCreatedBy());
						project.setUpdtaedBy(projectDetails.getUpdatedBy());
						project.setProjectCreatedOn(projectDetails.getCreatedOn());
						project.setProjectUpdatedOn(projectDetails.getUpdatedOn());
						project.setProjectStartDate(projectDetails.getStartDate());
						project.setProjectEndDate(projectDetails.getEndDate());
						project.setProjectCode(projectDetails.getProjectCode());
						project.setPriority(proManager.getPriority());
						project.setProjectManagerStartDate(proManager.getStartDate());
						project.setProjectManagerEndDate(proManager.getEndDate());
						project.setProjectManagerCreatedOn(proManager.getCreatedOn());
						project.setProjectManagerUpdatedOn(proManager.getUpdatedOn());
						project.setBuId(projectDetails.getBuID());
						project.setOpId(projectDetails.getOpID());
						project.setApplicationId(applicationDetails.getApplicationId());
						project.setProjectStatus(projectDetails.getEndDate().before(new Date()) ? false : true);
						projectsRepository.save(project);
					}
				}

				sequenceGeneratorRepository.save(updateSequence);
				applicationOnboardingRepository.save(superAdminRequest);
				if (applicationObject.getContractInfo() == null) {
					try {
						newApplicationEmailVerfication(superAdminRequest, null, childRequestId);
					} catch (IOException e) {
						throw new IOException(e);
					} catch (TemplateException e) {
						throw new TemplateException(e, null);
					} catch (DataValidationException e) {
						throw new DataValidationException(e.getMessage(), null, null);
					}
				}
			}

		}
		if (requestId != null && childRequestId == null) {
			ApplicationOnboarding superAdminRequest = applicationOnboardingRepository
					.findAllBySuperAdminRequestId(requestId);
			if (superAdminRequest == null) {
				throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, null, HttpStatus.CONFLICT);
			}
			if (superAdminRequest.getWorkGroup().equalsIgnoreCase("reviewer")) {
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setWorkGroup(Constant.SUPER_ADMIN);
				superAdminRequest.setProjectName(superAdminRequest.getProjectName());
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setCreatedOn(new Date());
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setUpdatedOn(new Date());

				ApplicationDetails applicationDetails = new ApplicationDetails();
				ApplicationSubscriptionDetails subscriptionDetails = new ApplicationSubscriptionDetails();
				PurchasedSingleAppOnboardingRequest applicationObject = applicationObjectDeserializer(
						superAdminRequest.getApplcationOnboardRequest());

				ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
						.findByApplicationName(applicationObject.getApplicationInfo().getApplicationName());

				String name = Constant.APPLICATION_ID;
				Integer sequence = sequenceGeneratorRepository.getApplicatiionDetailSequence();
				name = name.concat(sequence.toString());
				SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
				updateSequence.setApplicationDetails(updateSequence.getApplicationDetails() + 1);

				ApplicationCategoryMaster category = categoryMasterRepository
						.findByCategoryName(applicationObject.getApplicationInfo().getApplicationCategory());

				applicationDetails.setApplicationId(name);
				applicationDetails.setApplicationName(applicationObject.getApplicationInfo().getApplicationName());
				applicationDetails.setApplicationCategoryMaster(category);
				applicationDetails
						.setOwnerDepartment(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				applicationDetails.setCreatedOn(new Date());
				if (applicationObject.getContractInfo() != null) {
					applicationDetails.setActiveContracts(applicationObject.getContractInfo().size());
				}
				applicationDetails.setCategoryId(category.getCategoryId());
				applicationDetails.setCreatedBy(profile.getEmailAddress());
				applicationDetails.setBuID(Constant.BUID);
				applicationDetails.setStartDate(new Date());
				applicationDetails.setLogoUrl(applicationLogoEntity.getLogoUrl());
				applicationDetails.setApplicationStatus(Constant.ACTIVE);
				applicationDetails.setApplicationDescription(applicationLogoEntity.getDescription());
				applicationDetails.setProjectName(applicationObject.getApplicationInfo().getProjectName());
				DepartmentDetails details = departmentRepository
						.findByDepartmentName(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());

				List<ApplicationDetails> appDetails = details.getApplicationId();
				appDetails.add(applicationDetails);
				details.setApplicationId(appDetails);

				List<Departments> departmentsList = departmentsRepository
						.findByDepartmentName(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				if (!departmentsList.isEmpty()) {
					List<ApplicationDetails> appDetailsForDepartments = details.getApplicationId();
					StringBuilder applicationIdBuilder = new StringBuilder();
					for (ApplicationDetails detail : appDetailsForDepartments) {
						if (applicationIdBuilder.length() > 0) {
							applicationIdBuilder.append(", ");
						}
						applicationIdBuilder.append(detail.getApplicationId());
					}
					String applicationIds = applicationIdBuilder.toString();
					for (Departments department : departmentsList) {
						department.setApplicationId(applicationIds);
						try {
							BeanUtils.copyProperties(department, applicationDetails);
						} catch (IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
					departmentsRepository.saveAll(departmentsList);
				}


				Applications application = new Applications();
				application.setApplicationId(name);
				application.setApplicationName(applicationObject.getApplicationInfo().getApplicationName());
				application.setBuId(Constant.BUID);
				application.setCategoryName(category.getCategoryName());
				application.setStartDate(new Date());
				application.setCategoryId(category.getCategoryId());
				application.setOwnerDepartment(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				application.setCreatedOn(new Date());
				if (applicationObject.getContractInfo() != null) {
					application.setActiveContracts(applicationObject.getContractInfo().size());
				}
				application.setCreatedBy(profile.getEmailAddress());
				application.setLogoUrl(applicationLogoEntity.getLogoUrl());
				application.setApplicationStatus(Constant.ACTIVE);
				application.setApplicationDescription(applicationLogoEntity.getDescription());
				application.setProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
				application.setDepartmentId(details.getDepartmentId());
				application.setDepartmentName(details.getDepartmentName());

				departmentRepository.save(details);
				
				applicationDetailsRepository.save(applicationDetails);
				int i = 1;
				for (newApplicationOwnerDetailsRequest owners : applicationObject.getApplicationInfo()
						.getOwnerDetails()) {
					ApplicationOwnerDetails applicationOwnerDetails = new ApplicationOwnerDetails();
					applicationOwnerDetails.setApplicationId(name);
					applicationOwnerDetails.setCreatedOn(new Date());
					applicationOwnerDetails.setOwner(owners.getApplicaitonOwnerName());
					applicationOwnerDetails
							.setOwnerDepartment(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
					applicationOwnerDetails.setOwnerEmail(owners.getApplicationOwnerEmail());
					applicationOwnerDetails.setPriority(i);
					applicationOwnerRepository.save(applicationOwnerDetails);
					i++;
				}

				ProjectDetails projectDetails = projectDetailsRepository
						.findByProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
				List<ApplicationDetails> aps = projectDetails.getApplicationId();
				aps.add(applicationDetails);
				projectDetails.setApplicationId(aps);
				projectDetailsRepository.save(projectDetails);

				if (applicationObject.getApplicationInfo().getSubscriptionName().length() > 0
						|| !applicationObject.getApplicationInfo().getSubscriptionName().isEmpty()) {
					String subId = "SUB_0";
					Integer subIdSequence = sequenceGeneratorRepository.getApplicationSubscriptionSequence();
					subId = subId.concat(subIdSequence.toString());
					SequenceGenerator updatesubIdSequence = sequenceGeneratorRepository.getById(1);
					updatesubIdSequence.setApplicationSubscription(++subIdSequence);

					String subscriptionId = subId;
					subscriptionDetails.setSubscriptionId(subscriptionId);
					subscriptionDetails
							.setSubscriptionName(applicationObject.getApplicationInfo().getSubscriptionName());
					subscriptionDetails
							.setSubscriptionNumber(applicationObject.getApplicationInfo().getSubscriptionId());
					subscriptionDetails.setCreatedOn(new Date());
					subscriptionDetails.setApplicationId(applicationDetails);
					subscriptionDetails.setBuID(Constant.BUID);
					subscriptionDetails.setOpID(Constant.SAASPE);
					subscriptionDetails.setStartDate(new Date());
					subscriptionDetails.setSubscriptionOwner(profile.getEmailAddress());
					subscriptionDetails.setSubscriptionStatus(Constant.ACTIVE);
					subscriptionDetails.setCreatedBy(profile.getFirstName() + " " + profile.getLastName());
					subscriptionDetails
							.setSubscriptionProvider(applicationLogoEntity.getProviderId().getProviderName());

					application.setSubscriptionId(subscriptionId);
					application.setSubscriptionName(applicationObject.getApplicationInfo().getSubscriptionName());
					application.setSubscriptionStartDate(new Date());
					application.setSubscriptionOwner(profile.getEmailAddress());
					application.setSubscriptionProvider(applicationLogoEntity.getProviderId().getProviderName());

					applicationSubscriptionDetailsRepository.save(subscriptionDetails);
					sequenceGeneratorRepository.save(updatesubIdSequence);
				}

				if (applicationObject.getContractInfo() != null) {
					for (NewAppContractInfo contract : applicationObject.getContractInfo()) {
						ApplicationContractDetails applicationContractDetails = new ApplicationContractDetails();
						String contractSeqId = Constant.CONTRACT_ID;
						Integer contractSequence = sequenceGeneratorRepository.getContractSequence();
						contractSeqId = contractSeqId.concat(contractSequence.toString());
						SequenceGenerator contractupdateSequence = sequenceGeneratorRepository.getById(1);
						contractupdateSequence.setApplicationContacts(++contractSequence);

						applicationContractDetails.setContractId(contractSeqId);
						applicationContractDetails.setApplicationId(applicationDetails);
						applicationContractDetails.setContractName(contract.getContractName());
						applicationContractDetails.setContractStartDate(contract.getContractStartDate());
						applicationContractDetails.setStartDate(new Date());
						applicationContractDetails.setContractType(contract.getContractType());
						applicationContractDetails.setAutoRenewalCancellation(contract.getAutoRenewalCancellation());
						if (ContractType.monthToMonth(contract.getContractType())) {
							applicationContractDetails.setBillingFrequency(null);
							applicationContractDetails.setContractTenure(null);
						} else {
							applicationContractDetails.setBillingFrequency(contract.getBillingFrequency());
							applicationContractDetails.setContractTenure(Integer.valueOf(contract.getContractTenure()));
						}
						applicationContractDetails.setContractEndDate(contract.getContractEndDate());
						if (Boolean.TRUE.equals(contract.getAutoRenewal())) {
							applicationContractDetails.setRenewalDate(contract.getUpcomingRenewalDate());
						} else {
							applicationContractDetails.setRenewalDate(contract.getContractEndDate());
						}
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
						applicationContractDetails.setBuID(Constant.BUID);
						applicationContractDetails.setCreatedOn(new Date());
						applicationContractDetails.setCreatedBy(profile.getEmailAddress());
						applicationContractDetails.setAutoRenew(contract.getAutoRenewal());
						applicationContractDetails.setSubscriptionDetails(subscriptionDetails);
						applicationContractDetails.setContractPaymentMethod(contract.getPaymentMethod());
						applicationContractDetails.setContractCurrency(contract.getCurrencyCode());
						applicationContractDetails
								.setContractOwner(profile.getFirstName() + " " + profile.getLastName());

						String invseSeq = Constant.INVOICE_ID;
						Integer invSequence = sequenceGeneratorRepository.getPaymentReqSequence();
						invseSeq = invseSeq.concat(invSequence.toString());
						SequenceGenerator invoiceUpdateSequence = sequenceGeneratorRepository.getById(1);
						invoiceUpdateSequence.setPaymentSequenceId(++invSequence);

						PaymentDetails paymentDetails = new PaymentDetails();
						paymentDetails.setInvoiceNo(invseSeq);
						paymentDetails.setApplicationId(name);
						paymentDetails.setBuID(Constant.BUID);
						paymentDetails.setCreatedBy(profile.getEmailAddress());
						paymentDetails.setCreatedOn(new Date());
						paymentDetails.setStartDate(new Date());
						paymentDetails.setPaymentMethod(contract.getPaymentMethod());
						paymentDetails.setCardholderName(contract.getCardHolderName());
						if (contract.getCardNumber() != null) {
							String cardNo = Base64.getEncoder().encodeToString(
									contract.getCardNumber().toString().getBytes(StandardCharsets.UTF_8));
							String trimmedCardNo = cardNo.replaceAll("\\s", "").trim();
							paymentDetails.setCardNumber(trimmedCardNo);
						}
						paymentDetails.setValidThrough(contract.getValidThrough());
						paymentDetails.setWalletName(contract.getWalletName());
						paymentDetails.setContractId(contractSeqId);
						paymentDetailsRepository.save(paymentDetails);
						sequenceGeneratorRepository.save(invoiceUpdateSequence);

						contractDetailsRepository.save(applicationContractDetails);
						sequenceGeneratorRepository.save(contractupdateSequence);
					}

				}

				for (NewAppLicenseInfo license : applicationObject.getProducts()) {
					String licenseSeq = Constant.LICENSE_ID;
					Integer licenseSequence = sequenceGeneratorRepository.getLicenseSequence();
					licenseSeq = licenseSeq.concat(licenseSequence.toString());
					SequenceGenerator licenseUpdateSequence = sequenceGeneratorRepository.getById(1);
					licenseUpdateSequence.setApplicatiionLicense(++licenseSequence);
					ApplicationContractDetails contractDetails = contractDetailsRepository
							.findByContractName(license.getContractName());
					ApplicationLicenseDetails applicationLicenseDetails = licenseContractTotalCost(contractDetails,
							license);
					PaymentDetails pay = paymentDetailsRepository.findByContractId(contractDetails.getContractId());
					applicationLicenseDetails.setApplicationId(applicationDetails);
					applicationLicenseDetails.setContractId(contractDetails);
					applicationLicenseDetails.setCreatedBy(profile.getFirstName());
					applicationLicenseDetails.setLicenseId(licenseSeq);
					applicationLicenseDetails.setCreatedOn(new Date());
					BigDecimal afterCost = BigDecimal.valueOf(0);
					BigDecimal beforeCost = BigDecimal.valueOf(0);
					if (contractDetails != null) {
						if (contractDetails.getContractStartDate().after(new Date())) {
							afterCost = getConvertedLicenseCost(contractDetails.getStartDate(),
									contractDetails.getContractCurrency(), applicationLicenseDetails.getTotalCost());
							applicationLicenseDetails.setConvertedCost(afterCost);
						} else {
							beforeCost = getConvertedLicenseCost(contractDetails.getContractStartDate(),
									contractDetails.getContractCurrency(), applicationLicenseDetails.getTotalCost());
							applicationLicenseDetails.setConvertedCost(beforeCost);
						}
						applicationLicenseDetails.setLicenseStartDate(contractDetails.getContractStartDate());
						applicationLicenseDetails.setLicenseEndDate(contractDetails.getContractEndDate());
					}
					applicationLicenseDetails.setBuID(Constant.BUID);
					applicationLicenseDetails.setStartDate(new Date());
					applicationLicenseDetails.setProductName(license.getProductName());
					applicationLicenseDetails.setCurrency(license.getCurrencyCode());
					applicationLicenseDetails.setUnitPrice(license.getUnitPrice());
					applicationLicenseDetails.setProductCategory(license.getProductType());
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setLicenseMapped(0);
					if (ProductType.professionalServices(license.getProductType())
							|| ProductType.platform(license.getProductType())) {
						applicationLicenseDetails.setQuantity(1);
						applicationLicenseDetails.setLicenseUnMapped(1);
					} else {
						applicationLicenseDetails.setQuantity(license.getQuantity());
						applicationLicenseDetails.setLicenseUnMapped(license.getQuantity());
					}

					if (application.getLicenseId() == null) {
						application.setContractId(contractDetails.getContractId());
						application.setAutoRenew(contractDetails.getAutoRenew());
						application.setContractCurrency(contractDetails.getContractCurrency());
						application.setContractDescription(contractDetails.getContractDescription());
						application.setContractEndDate(contractDetails.getContractEndDate());
						application.setContractName(contractDetails.getContractName());
						application.setContractNoticeDate(contractDetails.getContractNoticeDate());
						application.setContractOwner(contractDetails.getContractOwner());
						application.setContractOwnerEmail(contractDetails.getContractOwnerEmail());
						application.setPaymentMethod(contractDetails.getContractPaymentMethod());
						application.setPaymentTerm(contractDetails.getContractPaymentTerm());
						application.setContractProvider(contractDetails.getContractProvider());
						application.setContractStartDate(contractDetails.getContractStartDate());
						application.setContractStatus(contractDetails.getContractStatus());
						application.setReminderDate(contractDetails.getReminderDate());
						application.setRenewalTerm(contractDetails.getRenewalTerm());
						application.setContractType(contractDetails.getContractType());
						application.setBillingFrequency(contractDetails.getBillingFrequency());
						if (contractDetails.getAutoRenewalCancellation() != null)
							application.setAutoRenewalCancellation(contractDetails.getAutoRenewalCancellation());
						application.setContractTenure(contractDetails.getContractTenure());
						application.setLicenseId(licenseSeq);
						application.setLicenseStartDate(contractDetails.getContractStartDate());
						application.setLicenseEndDate(contractDetails.getContractEndDate());
						application.setUnitPriceType(license.getUnitPriceType());
						application.setProductName(license.getProductName());
						application.setCurrency(license.getCurrencyCode());
						application.setUnitPrice(license.getUnitPrice());
						application.setProductCategory(license.getProductType());
						application.setLicenseMapped(0);
						if (ProductType.professionalServices(license.getProductType())
								|| ProductType.platform(license.getProductType())) {
							application.setQuantity(1);
							application.setLicenseUnmapped(1);
						} else {
							application.setQuantity(license.getQuantity());
							application.setLicenseUnmapped(license.getQuantity());
						}
						application.setTotalCost(applicationLicenseDetails.getTotalCost());
						if (contractDetails.getContractStartDate().after(new Date())) {
							application.setConvertedCost(afterCost);
						} else {
							application.setConvertedCost(beforeCost);
						}
						application.setPaymentDescription(pay.getDescription());
						application.setPaymentTransactionDate(pay.getTransactionDate());
						application.setPaymentAmount(pay.getAmount());
						application.setCardholderName(pay.getCardholderName());
						application.setCardNumber(pay.getCardNumber());
						application.setValidThrough(pay.getValidThrough());
						application.setWalletName(pay.getWalletName());
						application.setPaymentStartDate(pay.getStartDate());
						application.setPaymentEndDate(pay.getEndDate());
						application.setPaymentSecretKey(pay.getSecretKey());
						applicationRepository.save(application);
					} else if (!application.getLicenseId().equals(licenseSeq)) {
						Applications app = new Applications();
						try {
							BeanUtils.copyProperties(app, application);
							app.setId(0);
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						app.setContractId(contractDetails.getContractId());
						app.setAutoRenew(contractDetails.getAutoRenew());
						app.setContractCurrency(contractDetails.getContractCurrency());
						app.setContractDescription(contractDetails.getContractDescription());
						app.setContractEndDate(contractDetails.getContractEndDate());
						app.setContractName(contractDetails.getContractName());
						app.setContractNoticeDate(contractDetails.getContractNoticeDate());
						app.setContractOwner(contractDetails.getContractOwner());
						app.setContractOwnerEmail(contractDetails.getContractOwnerEmail());
						app.setPaymentMethod(contractDetails.getContractPaymentMethod());
						app.setPaymentTerm(contractDetails.getContractPaymentTerm());
						app.setContractProvider(contractDetails.getContractProvider());
						app.setContractStartDate(contractDetails.getContractStartDate());
						app.setContractStatus(contractDetails.getContractStatus());
						app.setReminderDate(contractDetails.getReminderDate());
						app.setRenewalTerm(contractDetails.getRenewalTerm());
						app.setContractType(contractDetails.getContractType());
						app.setBillingFrequency(contractDetails.getBillingFrequency());
						if (contractDetails.getAutoRenewalCancellation() != null)
							app.setAutoRenewalCancellation(contractDetails.getAutoRenewalCancellation());
						app.setContractTenure(contractDetails.getContractTenure());
						app.setLicenseId(licenseSeq);
						app.setLicenseStartDate(contractDetails.getContractStartDate());
						app.setLicenseEndDate(contractDetails.getContractEndDate());
						app.setUnitPriceType(license.getUnitPriceType());
						app.setProductName(license.getProductName());
						app.setCurrency(license.getCurrencyCode());
						app.setUnitPrice(license.getUnitPrice());
						app.setProductCategory(license.getProductType());
						app.setLicenseMapped(0);
						if (ProductType.professionalServices(license.getProductType())
								|| ProductType.platform(license.getProductType())) {
							app.setQuantity(1);
							app.setLicenseUnmapped(1);
						} else {
							app.setQuantity(license.getQuantity());
							app.setLicenseUnmapped(license.getQuantity());
						}
						app.setTotalCost(applicationLicenseDetails.getTotalCost());
						if (contractDetails.getContractStartDate().after(new Date())) {
							app.setConvertedCost(afterCost);
						} else {
							app.setConvertedCost(beforeCost);
						}
						app.setPaymentDescription(pay.getDescription());
						app.setPaymentTransactionDate(pay.getTransactionDate());
						app.setPaymentAmount(pay.getAmount());
						app.setCardholderName(pay.getCardholderName());
						app.setCardNumber(pay.getCardNumber());
						app.setValidThrough(pay.getValidThrough());
						app.setWalletName(pay.getWalletName());
						app.setPaymentStartDate(pay.getStartDate());
						app.setPaymentEndDate(pay.getEndDate());
						app.setPaymentSecretKey(pay.getSecretKey());
						applicationRepository.save(app);
					}

					licenseDetailsRepository.save(applicationLicenseDetails);
					sequenceGeneratorRepository.save(licenseUpdateSequence);
				}

				List<ProjectManagerDetails> proManagers = projectOwnerRepository
						.findByProjectIdOrderByEndDate(projectDetails.getProjectId());
				List<Projects> existingProject = projectsRepository.findByProjectId(projectDetails.getProjectId());
				for (ProjectManagerDetails proManager : proManagers) {
					if (!existingProject.isEmpty()) {
						for (Projects project : existingProject) {
							if (project.getProjectManagerEmail().equals(proManager.getProjectManagerEmail())) {
								project.setProjectManagerEmail(proManager.getProjectManagerEmail());
								project.setPriority(proManager.getPriority());
								project.setProjectManagerStartDate(proManager.getStartDate());
								project.setProjectManagerEndDate(proManager.getEndDate());
								project.setProjectManagerCreatedOn(proManager.getCreatedOn());
								project.setProjectManagerUpdatedOn(proManager.getUpdatedOn());
								project.setApplicationId(applicationDetails.getApplicationId());
								project.setProjectStatus(projectDetails.getEndDate().before(new Date()) ? false : true);
								projectsRepository.save(project);
							}
						}
					} else {
						Projects project = new Projects();
						project.setProjectId(projectDetails.getProjectId());
						project.setProjectName(projectDetails.getProjectName());
						project.setProjectManagerEmail(proManager.getProjectManagerEmail());
						project.setBudget(projectDetails.getProjectBudget());
						project.setBudgetCurrency(projectDetails.getBudgetCurrency());
						project.setDescription(projectDetails.getProjectDescription());
						project.setCreatedBy(projectDetails.getCreatedBy());
						project.setUpdtaedBy(projectDetails.getUpdatedBy());
						project.setProjectCreatedOn(projectDetails.getCreatedOn());
						project.setProjectUpdatedOn(projectDetails.getUpdatedOn());
						project.setProjectStartDate(projectDetails.getStartDate());
						project.setProjectEndDate(projectDetails.getEndDate());
						project.setProjectCode(projectDetails.getProjectCode());
						project.setPriority(proManager.getPriority());
						project.setProjectManagerStartDate(proManager.getStartDate());
						project.setProjectManagerEndDate(proManager.getEndDate());
						project.setProjectManagerCreatedOn(proManager.getCreatedOn());
						project.setProjectManagerUpdatedOn(proManager.getUpdatedOn());
						project.setBuId(projectDetails.getBuID());
						project.setOpId(projectDetails.getOpID());
						project.setApplicationId(applicationDetails.getApplicationId());
						project.setProjectStatus(projectDetails.getEndDate().before(new Date()) ? false : true);
						projectsRepository.save(project);
					}
				}

				sequenceGeneratorRepository.save(updateSequence);
				applicationOnboardingRepository.save(superAdminRequest);
				if (applicationObject.getContractInfo() == null) {
					try {
						newApplicationEmailVerfication(superAdminRequest, requestId, null);
					} catch (IOException e) {
						throw new IOException(e);
					} catch (TemplateException e) {
						throw new TemplateException(e, null);
					} catch (DataValidationException e) {
						throw new DataValidationException(e.getMessage(), null, null);
					}
				}
			} else {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setWorkGroup(Constant.SUPER_ADMIN);
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setUpdatedOn(new Date());

				ApplicationDetails applicationDetails = new ApplicationDetails();
				ApplicationSubscriptionDetails subscriptionDetails = new ApplicationSubscriptionDetails();
				PurchasedSingleAppOnboardingRequest applicationObject = applicationObjectDeserializer(
						superAdminRequest.getApplcationOnboardRequest());

				ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
						.findByApplicationName(applicationObject.getApplicationInfo().getApplicationName());

				String name = Constant.APPLICATION_ID;
				Integer sequence = sequenceGeneratorRepository.getApplicatiionDetailSequence();
				name = name.concat(sequence.toString());
				SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
				updateSequence.setApplicationDetails(updateSequence.getApplicationDetails() + 1);

				ApplicationCategoryMaster category = categoryMasterRepository
						.findByCategoryName(applicationObject.getApplicationInfo().getApplicationCategory());

				applicationDetails.setApplicationId(name);
				applicationDetails.setApplicationName(applicationObject.getApplicationInfo().getApplicationName());
				applicationDetails.setApplicationCategoryMaster(category);
				applicationDetails
						.setOwnerDepartment(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				applicationDetails.setCreatedOn(new Date());
				if (applicationObject.getContractInfo() != null) {
					applicationDetails.setActiveContracts(applicationObject.getContractInfo().size());
				}
				applicationDetails.setCategoryId(category.getCategoryId());
				applicationDetails.setStartDate(new Date());
				applicationDetails.setCreatedBy(profile.getEmailAddress());
				applicationDetails.setBuID(Constant.BUID);
				applicationDetails.setLogoUrl(applicationLogoEntity.getLogoUrl());
				applicationDetails.setApplicationStatus(Constant.ACTIVE);
				applicationDetails.setApplicationDescription(applicationLogoEntity.getDescription());
				applicationDetails.setProjectName(applicationObject.getApplicationInfo().getProjectName());
				DepartmentDetails details = departmentRepository
						.findByDepartmentName(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				List<ApplicationDetails> appDetails = details.getApplicationId();
				appDetails.add(applicationDetails);
				details.setApplicationId(appDetails);

				List<Departments> departmentsList = departmentsRepository
						.findByDepartmentName(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				if (!departmentsList.isEmpty()) {
					List<ApplicationDetails> appDetailsForDepartments = details.getApplicationId();
					StringBuilder applicationIdBuilder = new StringBuilder();
					for (ApplicationDetails detail : appDetailsForDepartments) {
						if (applicationIdBuilder.length() > 0) {
							applicationIdBuilder.append(", ");
						}
						applicationIdBuilder.append(detail.getApplicationId());
					}
					String applicationIds = applicationIdBuilder.toString();
					for (Departments department : departmentsList) {
						department.setApplicationId(applicationIds);
						try {
							BeanUtils.copyProperties(department, applicationDetails);
						} catch (IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
					departmentsRepository.saveAll(departmentsList);
				}

				Applications application = new Applications();
				application.setApplicationId(name);
				application.setApplicationName(applicationObject.getApplicationInfo().getApplicationName());
				application.setBuId(Constant.BUID);
				application.setCategoryName(category.getCategoryName());
				application.setStartDate(new Date());
				application.setCategoryId(category.getCategoryId());
				application.setOwnerDepartment(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
				application.setCreatedOn(new Date());
				if (applicationObject.getContractInfo() != null) {
					application.setActiveContracts(applicationObject.getContractInfo().size());
				}
				application.setCreatedBy(profile.getEmailAddress());
				application.setLogoUrl(applicationLogoEntity.getLogoUrl());
				application.setApplicationStatus(Constant.ACTIVE);
				application.setApplicationDescription(applicationLogoEntity.getDescription());
				application.setProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
				application.setDepartmentId(details.getDepartmentId());
				application.setDepartmentName(details.getDepartmentName());

				departmentRepository.save(details);
				applicationDetailsRepository.save(applicationDetails);
				int i = 1;
				for (newApplicationOwnerDetailsRequest owners : applicationObject.getApplicationInfo()
						.getOwnerDetails()) {
					ApplicationOwnerDetails applicationOwnerDetails = new ApplicationOwnerDetails();
					applicationOwnerDetails.setApplicationId(name);
					applicationOwnerDetails.setCreatedOn(new Date());
					applicationOwnerDetails.setOwner(owners.getApplicaitonOwnerName());
					applicationOwnerDetails
							.setOwnerDepartment(applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
					applicationOwnerDetails.setOwnerEmail(owners.getApplicationOwnerEmail());
					applicationOwnerDetails.setPriority(i);
					applicationOwnerRepository.save(applicationOwnerDetails);
					i++;
				}

				ProjectDetails projectDetails = projectDetailsRepository
						.findByProjectName(applicationObject.getApplicationInfo().getProjectName().trim());
				List<ApplicationDetails> aps = projectDetails.getApplicationId();
				aps.add(applicationDetails);
				projectDetails.setApplicationId(aps);
				projectDetailsRepository.save(projectDetails);

				if (applicationObject.getApplicationInfo().getSubscriptionName().length() > 0
						|| !applicationObject.getApplicationInfo().getSubscriptionName().isEmpty()) {
					String subId = "SUB_0";
					Integer subIdSequence = sequenceGeneratorRepository.getApplicationSubscriptionSequence();
					subId = subId.concat(subIdSequence.toString());
					SequenceGenerator updatesubIdSequence = sequenceGeneratorRepository.getById(1);
					updatesubIdSequence.setApplicationSubscription(++subIdSequence);

					String subscriptionId = subId;
					subscriptionDetails.setSubscriptionId(subscriptionId);
					subscriptionDetails
							.setSubscriptionName(applicationObject.getApplicationInfo().getSubscriptionName());
					subscriptionDetails
							.setSubscriptionNumber(applicationObject.getApplicationInfo().getSubscriptionId());
					subscriptionDetails.setApplicationId(applicationDetails);
					subscriptionDetails.setCreatedOn(new Date());
					subscriptionDetails.setBuID(Constant.BUID);
					subscriptionDetails.setStartDate(new Date());
					subscriptionDetails.setOpID(Constant.SAASPE);
					subscriptionDetails.setSubscriptionOwner(profile.getEmailAddress());
					subscriptionDetails.setSubscriptionStatus(Constant.ACTIVE);
					subscriptionDetails.setCreatedBy(profile.getFirstName() + " " + profile.getLastName());
					subscriptionDetails
							.setSubscriptionProvider(applicationLogoEntity.getProviderId().getProviderName());

					application.setSubscriptionId(subscriptionId);
					application.setSubscriptionName(applicationObject.getApplicationInfo().getSubscriptionName());
					application.setSubscriptionStartDate(new Date());
					application.setSubscriptionOwner(profile.getEmailAddress());
					application.setSubscriptionProvider(applicationLogoEntity.getProviderId().getProviderName());

					applicationSubscriptionDetailsRepository.save(subscriptionDetails);
					sequenceGeneratorRepository.save(updatesubIdSequence);
				}

				if (applicationObject.getContractInfo() != null) {
					for (NewAppContractInfo contract : applicationObject.getContractInfo()) {
						ApplicationContractDetails applicationContractDetails = new ApplicationContractDetails();
						String contractSeqId = Constant.CONTRACT_ID;
						Integer contractSequence = sequenceGeneratorRepository.getContractSequence();
						contractSeqId = contractSeqId.concat(contractSequence.toString());
						SequenceGenerator contractupdateSequence = sequenceGeneratorRepository.getById(1);
						contractupdateSequence.setApplicationContacts(++contractSequence);

						applicationContractDetails.setContractId(contractSeqId);
						applicationContractDetails.setApplicationId(applicationDetails);
						applicationContractDetails.setContractName(contract.getContractName());
						applicationContractDetails.setContractStartDate(contract.getContractStartDate());
						applicationContractDetails.setContractEndDate(contract.getContractEndDate());
						if (Boolean.TRUE.equals(contract.getAutoRenewal())) {
							applicationContractDetails.setRenewalDate(contract.getUpcomingRenewalDate());
						} else {
							applicationContractDetails.setRenewalDate(contract.getContractEndDate());
						}
						applicationContractDetails.setStartDate(new Date());
						applicationContractDetails.setBuID(Constant.BUID);
						applicationContractDetails.setCreatedOn(new Date());
						applicationContractDetails.setContractType(contract.getContractType());
						applicationContractDetails.setAutoRenewalCancellation(contract.getAutoRenewalCancellation());
						if (ContractType.monthToMonth(contract.getContractType())) {
							applicationContractDetails.setBillingFrequency(null);
							applicationContractDetails.setContractTenure(null);
						} else {
							applicationContractDetails.setBillingFrequency(contract.getBillingFrequency());
							applicationContractDetails.setContractTenure(Integer.valueOf(contract.getContractTenure()));
						}
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
						applicationContractDetails.setCreatedBy(profile.getEmailAddress());
						applicationContractDetails.setAutoRenew(contract.getAutoRenewal());
						applicationContractDetails.setSubscriptionDetails(subscriptionDetails);
						applicationContractDetails.setContractPaymentMethod(contract.getPaymentMethod());
						applicationContractDetails.setContractCurrency(contract.getCurrencyCode());
						applicationContractDetails
								.setContractOwner(profile.getFirstName() + " " + profile.getLastName());

						String invseSeq = Constant.INVOICE_ID;
						Integer invSequence = sequenceGeneratorRepository.getPaymentReqSequence();
						invseSeq = invseSeq.concat(invSequence.toString());
						SequenceGenerator invoiceUpdateSequence = sequenceGeneratorRepository.getById(1);
						invoiceUpdateSequence.setPaymentSequenceId(++invSequence);

						PaymentDetails paymentDetails = new PaymentDetails();
						paymentDetails.setInvoiceNo(invseSeq);
						paymentDetails.setApplicationId(name);
						paymentDetails.setBuID(Constant.BUID);
						paymentDetails.setCreatedBy(profile.getEmailAddress());
						paymentDetails.setCreatedOn(new Date());
						paymentDetails.setStartDate(new Date());
						paymentDetails.setPaymentMethod(contract.getPaymentMethod());
						paymentDetails.setCardholderName(contract.getCardHolderName());
						if (contract.getCardNumber() != null) {
							String cardNo = Base64.getEncoder().encodeToString(
									contract.getCardNumber().toString().getBytes(StandardCharsets.UTF_8));
							String trimmedCardNo = cardNo.replaceAll("\\s", "").trim();
							paymentDetails.setCardNumber(trimmedCardNo);
						}
						paymentDetails.setValidThrough(contract.getValidThrough());
						paymentDetails.setWalletName(contract.getWalletName());
						paymentDetails.setContractId(contractSeqId);
						paymentDetailsRepository.save(paymentDetails);
						sequenceGeneratorRepository.save(invoiceUpdateSequence);

						contractDetailsRepository.save(applicationContractDetails);
						sequenceGeneratorRepository.save(contractupdateSequence);
					}

				}

				for (NewAppLicenseInfo license : applicationObject.getProducts()) {
					String licenseSeq = Constant.LICENSE_ID;
					Integer licenseSequence = sequenceGeneratorRepository.getLicenseSequence();
					licenseSeq = licenseSeq.concat(licenseSequence.toString());
					SequenceGenerator licenseUpdateSequence = sequenceGeneratorRepository.getById(1);
					licenseUpdateSequence.setApplicatiionLicense(++licenseSequence);

					ApplicationContractDetails contractDetails = contractDetailsRepository
							.findByContractName(license.getContractName());
					ApplicationLicenseDetails applicationLicenseDetails = licenseContractTotalCost(contractDetails,
							license);
					PaymentDetails pay = paymentDetailsRepository.findByContractId(contractDetails.getContractId());
					applicationLicenseDetails.setApplicationId(applicationDetails);
					applicationLicenseDetails.setContractId(contractDetails);
					applicationLicenseDetails.setCreatedBy(profile.getFirstName());
					applicationLicenseDetails.setCreatedOn(new Date());
					BigDecimal afterCost = BigDecimal.valueOf(0);
					BigDecimal beforeCost = BigDecimal.valueOf(0);
					if (contractDetails != null) {
						if (contractDetails.getContractStartDate().after(new Date())) {
							afterCost = getConvertedLicenseCost(contractDetails.getStartDate(),
									contractDetails.getContractCurrency(), applicationLicenseDetails.getTotalCost());
							applicationLicenseDetails.setConvertedCost(afterCost);
						} else {
							beforeCost = getConvertedLicenseCost(contractDetails.getContractStartDate(),
									contractDetails.getContractCurrency(), applicationLicenseDetails.getTotalCost());
							applicationLicenseDetails.setConvertedCost(beforeCost);
						}
						applicationLicenseDetails.setLicenseStartDate(contractDetails.getContractStartDate());
						applicationLicenseDetails.setLicenseEndDate(contractDetails.getContractEndDate());
					}
					applicationLicenseDetails.setLicenseId(licenseSeq);
					applicationLicenseDetails.setBuID(Constant.BUID);
					applicationLicenseDetails.setStartDate(new Date());
					applicationLicenseDetails.setProductName(license.getProductName());
					applicationLicenseDetails.setCurrency(license.getCurrencyCode());
					applicationLicenseDetails.setUnitPrice(license.getUnitPrice());
					applicationLicenseDetails.setProductCategory(license.getProductType());
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setLicenseMapped(0);
					if (ProductType.professionalServices(license.getProductType())
							|| ProductType.platform(license.getProductType())) {
						applicationLicenseDetails.setQuantity(1);
						applicationLicenseDetails.setLicenseUnMapped(1);
					} else {
						applicationLicenseDetails.setQuantity(license.getQuantity());
						applicationLicenseDetails.setLicenseUnMapped(license.getQuantity());
					}

					if (application.getLicenseId() == null) {
						application.setContractId(contractDetails.getContractId());
						application.setAutoRenew(contractDetails.getAutoRenew());
						application.setContractCurrency(contractDetails.getContractCurrency());
						application.setContractDescription(contractDetails.getContractDescription());
						application.setContractEndDate(contractDetails.getContractEndDate());
						application.setContractName(contractDetails.getContractName());
						application.setContractNoticeDate(contractDetails.getContractNoticeDate());
						application.setContractOwner(contractDetails.getContractOwner());
						application.setContractOwnerEmail(contractDetails.getContractOwnerEmail());
						application.setPaymentMethod(contractDetails.getContractPaymentMethod());
						application.setPaymentTerm(contractDetails.getContractPaymentTerm());
						application.setContractProvider(contractDetails.getContractProvider());
						application.setContractStartDate(contractDetails.getContractStartDate());
						application.setContractStatus(contractDetails.getContractStatus());
						application.setReminderDate(contractDetails.getReminderDate());
						application.setRenewalTerm(contractDetails.getRenewalTerm());
						application.setContractType(contractDetails.getContractType());
						application.setBillingFrequency(contractDetails.getBillingFrequency());
						if (contractDetails.getAutoRenewalCancellation() != null)
							application.setAutoRenewalCancellation(contractDetails.getAutoRenewalCancellation());
						application.setContractTenure(contractDetails.getContractTenure());
						application.setLicenseId(licenseSeq);
						application.setLicenseStartDate(contractDetails.getContractStartDate());
						application.setLicenseEndDate(contractDetails.getContractEndDate());
						application.setUnitPriceType(license.getUnitPriceType());
						application.setProductName(license.getProductName());
						application.setCurrency(license.getCurrencyCode());
						application.setUnitPrice(license.getUnitPrice());
						application.setProductCategory(license.getProductType());
						application.setLicenseMapped(0);
						if (ProductType.professionalServices(license.getProductType())
								|| ProductType.platform(license.getProductType())) {
							application.setQuantity(1);
							application.setLicenseUnmapped(1);
						} else {
							application.setQuantity(license.getQuantity());
							application.setLicenseUnmapped(license.getQuantity());
						}
						application.setTotalCost(applicationLicenseDetails.getTotalCost());
						if (contractDetails.getContractStartDate().after(new Date())) {
							application.setConvertedCost(afterCost);
						} else {
							application.setConvertedCost(beforeCost);
						}
						application.setPaymentDescription(pay.getDescription());
						application.setPaymentTransactionDate(pay.getTransactionDate());
						application.setPaymentAmount(pay.getAmount());
						application.setCardholderName(pay.getCardholderName());
						application.setCardNumber(pay.getCardNumber());
						application.setValidThrough(pay.getValidThrough());
						application.setWalletName(pay.getWalletName());
						application.setPaymentStartDate(pay.getStartDate());
						application.setPaymentEndDate(pay.getEndDate());
						application.setPaymentSecretKey(pay.getSecretKey());
						applicationRepository.save(application);
					} else if (!application.getLicenseId().equals(licenseSeq)) {
						Applications app = new Applications();
						int appId = app.getId();
						try {
							BeanUtils.copyProperties(app, application);
							int applicationId = app.getId();
							app.setId(0);
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						app.setContractId(contractDetails.getContractId());
						app.setAutoRenew(contractDetails.getAutoRenew());
						app.setContractCurrency(contractDetails.getContractCurrency());
						app.setContractDescription(contractDetails.getContractDescription());
						app.setContractEndDate(contractDetails.getContractEndDate());
						app.setContractName(contractDetails.getContractName());
						app.setContractNoticeDate(contractDetails.getContractNoticeDate());
						app.setContractOwner(contractDetails.getContractOwner());
						app.setContractOwnerEmail(contractDetails.getContractOwnerEmail());
						app.setPaymentMethod(contractDetails.getContractPaymentMethod());
						app.setPaymentTerm(contractDetails.getContractPaymentTerm());
						app.setContractProvider(contractDetails.getContractProvider());
						app.setContractStartDate(contractDetails.getContractStartDate());
						app.setContractStatus(contractDetails.getContractStatus());
						app.setReminderDate(contractDetails.getReminderDate());
						app.setRenewalTerm(contractDetails.getRenewalTerm());
						app.setContractType(contractDetails.getContractType());
						app.setBillingFrequency(contractDetails.getBillingFrequency());
						if (contractDetails.getAutoRenewalCancellation() != null)
							app.setAutoRenewalCancellation(contractDetails.getAutoRenewalCancellation());
						app.setContractTenure(contractDetails.getContractTenure());
						app.setLicenseId(licenseSeq);
						app.setLicenseStartDate(contractDetails.getContractStartDate());
						app.setLicenseEndDate(contractDetails.getContractEndDate());
						app.setUnitPriceType(license.getUnitPriceType());
						app.setProductName(license.getProductName());
						app.setCurrency(license.getCurrencyCode());
						app.setUnitPrice(license.getUnitPrice());
						app.setProductCategory(license.getProductType());
						app.setLicenseMapped(0);
						if (ProductType.professionalServices(license.getProductType())
								|| ProductType.platform(license.getProductType())) {
							app.setQuantity(1);
							app.setLicenseUnmapped(1);
						} else {
							app.setQuantity(license.getQuantity());
							app.setLicenseUnmapped(license.getQuantity());
						}
						app.setTotalCost(applicationLicenseDetails.getTotalCost());
						if (contractDetails.getContractStartDate().after(new Date())) {
							app.setConvertedCost(afterCost);
						} else {
							app.setConvertedCost(beforeCost);
						}
						app.setPaymentDescription(pay.getDescription());
						app.setPaymentTransactionDate(pay.getTransactionDate());
						app.setPaymentAmount(pay.getAmount());
						app.setCardholderName(pay.getCardholderName());
						app.setCardNumber(pay.getCardNumber());
						app.setValidThrough(pay.getValidThrough());
						app.setWalletName(pay.getWalletName());
						app.setPaymentStartDate(pay.getStartDate());
						app.setPaymentEndDate(pay.getEndDate());
						app.setPaymentSecretKey(pay.getSecretKey());
						applicationRepository.save(app);
					}

					licenseDetailsRepository.save(applicationLicenseDetails);
					sequenceGeneratorRepository.save(licenseUpdateSequence);
				}

				List<ProjectManagerDetails> proManagers = projectOwnerRepository
						.findByProjectIdOrderByEndDate(projectDetails.getProjectId());
				List<Projects> existingProject = projectsRepository.findByProjectId(projectDetails.getProjectId());
				for (ProjectManagerDetails proManager : proManagers) {
					if (!existingProject.isEmpty()) {
						for (Projects project : existingProject) {
							if (project.getProjectManagerEmail().equals(proManager.getProjectManagerEmail())) {
								project.setProjectManagerEmail(proManager.getProjectManagerEmail());
								project.setPriority(proManager.getPriority());
								project.setProjectManagerStartDate(proManager.getStartDate());
								project.setProjectManagerEndDate(proManager.getEndDate());
								project.setProjectManagerCreatedOn(proManager.getCreatedOn());
								project.setProjectManagerUpdatedOn(proManager.getUpdatedOn());
								project.setApplicationId(applicationDetails.getApplicationId());
								project.setProjectStatus(projectDetails.getEndDate().before(new Date()) ? false : true);
								projectsRepository.save(project);
							}
						}
					} else {
						Projects project = new Projects();
						project.setProjectId(projectDetails.getProjectId());
						project.setProjectName(projectDetails.getProjectName());
						project.setProjectManagerEmail(proManager.getProjectManagerEmail());
						project.setBudget(projectDetails.getProjectBudget());
						project.setBudgetCurrency(projectDetails.getBudgetCurrency());
						project.setDescription(projectDetails.getProjectDescription());
						project.setCreatedBy(projectDetails.getCreatedBy());
						project.setUpdtaedBy(projectDetails.getUpdatedBy());
						project.setProjectCreatedOn(projectDetails.getCreatedOn());
						project.setProjectUpdatedOn(projectDetails.getUpdatedOn());
						project.setProjectStartDate(projectDetails.getStartDate());
						project.setProjectEndDate(projectDetails.getEndDate());
						project.setProjectCode(projectDetails.getProjectCode());
						project.setPriority(proManager.getPriority());
						project.setProjectManagerStartDate(proManager.getStartDate());
						project.setProjectManagerEndDate(proManager.getEndDate());
						project.setProjectManagerCreatedOn(proManager.getCreatedOn());
						project.setProjectManagerUpdatedOn(proManager.getUpdatedOn());
						project.setBuId(projectDetails.getBuID());
						project.setOpId(projectDetails.getOpID());
						project.setApplicationId(applicationDetails.getApplicationId());
						project.setProjectStatus(projectDetails.getEndDate().before(new Date()) ? false : true);
						projectsRepository.save(project);
					}
				}

				sequenceGeneratorRepository.save(updateSequence);
				applicationOnboardingRepository.save(superAdminRequest);
				if (applicationObject.getContractInfo() == null) {
					try {
						newApplicationEmailVerfication(superAdminRequest, requestId, null);
					} catch (IOException e) {
						throw new IOException(e);
					} catch (TemplateException e) {
						throw new TemplateException(e, null);
					} catch (DataValidationException e) {
						throw new DataValidationException(e.getMessage(), null, null);
					}
				}
			}
		}
	}

	private CommonResponse reviewSuccessResponse() {
		return new CommonResponse(HttpStatus.OK,
				new Response("OnboardingWorkflowActionResponse", "Approved Successfully"), "Workflow action completed");
	}

	private CommonResponse reviewFailureResponse() {
		return new CommonResponse(HttpStatus.OK, new Response("OnboardingWorkflowActionResponse", "Workflow rejected"),
				"Workflow action completed");
	}

	public boolean isCardNumberValid(String cardNumber) {
		PasswordValidator validator = new PasswordValidator(
				Arrays.asList(new LengthRule(14, 14), new CharacterRule(EnglishCharacterData.Digit, 1)));
		RuleResult result = validator.validate(new PasswordData(cardNumber));
		return (!result.isValid());
	}

	private List<ApplicationOnboarding> getApplicationStatus(List<ApplicationOnboarding> applicationOnboardings) {
		List<ApplicationOnboarding> list = new ArrayList<>();
		List<ApplicationOnboarding> duplicateList = new ArrayList<>();
		for (ApplicationOnboarding details : applicationOnboardings) {
			ApplicationOnboarding viewResponse = new ApplicationOnboarding();
			ApplicationOnboarding step1childReq;
			ApplicationOnboarding step1req;
			ApplicationOnboarding step2ReqReject;
			ApplicationOnboarding step2ChildReqReject;
			ApplicationOnboarding step3ReqSuperApprove;
			ApplicationOnboarding step3ChildReqSuperApprove;

			if (details.getChildRequestNumber() != null) {
				step1childReq = applicationOnboardingRepository.findAllBySuperAdmin(details.getChildRequestNumber());
				if (step1childReq != null) {
					viewResponse = step1childReq;
				} else if (step1childReq == null) {
					step2ChildReqReject = applicationOnboardingRepository
							.findAllByChildReqReject(details.getChildRequestNumber());
					if (step2ChildReqReject != null) {
						viewResponse = step2ChildReqReject;
					} else if (step1childReq == null && step2ChildReqReject == null) {
						step3ReqSuperApprove = applicationOnboardingRepository
								.findChildReqSuperApprovee(details.getChildRequestNumber());
						if (step3ReqSuperApprove != null) {
							viewResponse = step3ReqSuperApprove;
						}
					}
				}
			}
			if (details.getRequestNumber() != null && details.getChildRequestNumber() == null) {
				step1req = applicationOnboardingRepository.findAllBySuperAdminRequestId(details.getRequestNumber());
				if (step1req != null) {
					viewResponse = step1req;
				} else if (step1req == null) {
					step2ReqReject = applicationOnboardingRepository.findAllByReject(details.getRequestNumber());
					if (step2ReqReject != null) {
						viewResponse = step2ReqReject;
					} else if (step1req == null && step2ReqReject == null) {
						step3ChildReqSuperApprove = applicationOnboardingRepository
								.findReqSuperApprove(details.getRequestNumber());
						if (step3ChildReqSuperApprove != null) {
							viewResponse = step3ChildReqSuperApprove;
						}
					}
				}
			}
			list.add(viewResponse);
		}
		for (ApplicationOnboarding requestTracking : list) {
			if (!duplicateList.contains(requestTracking)) {
				duplicateList.add(requestTracking);
			}
		}
		return duplicateList;
	}

	private List<String> applicatoinObjectValidator(PurchasedSingleAppOnboardingRequest validationObejct,
			int rowNumber) {
		List<String> errors = new ArrayList<>();

		if (categoryMasterRepository
				.findByCategoryName(validationObejct.getApplicationInfo().getApplicationCategory()) == null) {
			errors.add(Constant.CATEGORY_WITH_NAME + validationObejct.getApplicationInfo().getApplicationCategory()
					+ Constant.NOT_EXIST + Constant.AT_ROW + rowNumber);
		} else if (departmentRepository
				.findByDepartmentName(validationObejct.getApplicationInfo().getApplicationOwnerDepartment()) == null) {
			errors.add(Constant.DEPARTMENT_WITH_NAME
					+ validationObejct.getApplicationInfo().getApplicationOwnerDepartment() + Constant.NOT_EXIST
					+ Constant.AT_ROW + rowNumber);
		} else if (departmentRepository
				.findByDepartmentName(validationObejct.getApplicationInfo().getApplicationOwnerDepartment()) != null) {
			DepartmentDetails departmentDetails = departmentRepository
					.findByDepartmentName(validationObejct.getApplicationInfo().getApplicationOwnerDepartment());
			for (newApplicationOwnerDetailsRequest ownerDetail : validationObejct.getApplicationInfo()
					.getOwnerDetails()) {
				if (userDetailsRepository.findByuserEmail(ownerDetail.getApplicationOwnerEmail().trim()) == null) {
					errors.add(Constant.USER_WITH_EMAIL + ownerDetail.getApplicationOwnerEmail() + Constant.NOT_EXIST
							+ Constant.AT_ROW + rowNumber);
				}
				UserDetails list = userDetailsRepository.findByDepartmentIdAndUserEmail(
						departmentDetails.getDepartmentId(), ownerDetail.getApplicationOwnerEmail().trim());
				if (list == null) {
					errors.add("User Email with department not matched at row " + rowNumber);
				}
				if (list != null
						&& !list.getUserName().trim().equalsIgnoreCase(ownerDetail.getApplicaitonOwnerName().trim())) {
					errors.add("user name does not match with user details at row " + rowNumber);
				}
			}
			Integer i = 0;
			List<String> owneremails = new ArrayList<>();
			for (UserDetails userDetails : departmentDetails.getUserDetails()) {
				for (newApplicationOwnerDetailsRequest ownerDeetails : validationObejct.getApplicationInfo()
						.getOwnerDetails()) {
					if (userDetails.getUserEmail().trim().equalsIgnoreCase(ownerDeetails.getApplicationOwnerEmail())) {
						owneremails.add(ownerDeetails.getApplicationOwnerEmail());
						i++;
					}
				}
			}
			if (i == 0) {

				errors.add(Constant.APPLICATION_EMAIL_ADDRESS + String.join(",", owneremails)
						+ Constant.FOR_DEPARTMENT_WITH_NAME
						+ validationObejct.getApplicationInfo().getApplicationOwnerDepartment()
						+ Constant.MISMATCH_ERROR + Constant.AT_ROW + rowNumber);
			}

		} else if (providerDetailsRepository
				.findByProviderName(validationObejct.getApplicationInfo().getApplicationProviderName()) == null) {
			errors.add("Provider with Name " + validationObejct.getApplicationInfo().getApplicationProviderName()
					+ " Doesn't Exist at row " + rowNumber);
		}
		return errors;

	}

	private void applicatoinObjectValidator(PurchasedSingleAppOnboardingRequest validationObejct)
			throws DataValidationException {
		List<String> categoryName = categoryMasterRepository.findCategoryName();
		if (categoryName.stream().noneMatch(validationObejct.getApplicationInfo().getApplicationCategory()::equals)) {
			throw new DataValidationException("Category name "
					+ validationObejct.getApplicationInfo().getApplicationCategory() + Constant.MISMATCH_ERROR, "400",
					HttpStatus.BAD_REQUEST);
		}
		if (categoryMasterRepository
				.findByCategoryName(validationObejct.getApplicationInfo().getApplicationCategory()) == null) {
			throw new DataValidationException(Constant.CATEGORY_WITH_NAME
					+ validationObejct.getApplicationInfo().getApplicationCategory() + Constant.NOT_EXIST, null, null);
		} else if (departmentRepository
				.findByDepartmentName(validationObejct.getApplicationInfo().getApplicationOwnerDepartment()) == null) {
			throw new DataValidationException(Constant.DEPARTMENT_WITH_NAME
					+ validationObejct.getApplicationInfo().getApplicationOwnerDepartment() + Constant.NOT_EXIST, null,
					null);
		} else if (departmentRepository
				.findByDepartmentName(validationObejct.getApplicationInfo().getApplicationOwnerDepartment()) != null) {
			DepartmentDetails departmentDetails = departmentRepository
					.findByDepartmentName(validationObejct.getApplicationInfo().getApplicationOwnerDepartment());
			for (newApplicationOwnerDetailsRequest ownerDetail : validationObejct.getApplicationInfo()
					.getOwnerDetails()) {
				List<String> userName = userDetailsRepository.getByRolesExcludeUser();
				if (userName.stream().noneMatch(ownerDetail.getApplicationOwnerEmail()::equals)) {
					throw new DataValidationException(
							"User with email " + ownerDetail.getApplicationOwnerEmail() + Constant.MISMATCH_ERROR,
							"400", HttpStatus.BAD_REQUEST);
				}
				if (userDetailsRepository.findByuserEmail(ownerDetail.getApplicationOwnerEmail().trim()) == null) {

					throw new DataValidationException(
							Constant.USER_WITH_EMAIL + ownerDetail.getApplicationOwnerEmail() + Constant.NOT_EXIST,
							null, null);
				}
				UserDetails list = userDetailsRepository.findByDepartmentIdAndUserEmail(
						departmentDetails.getDepartmentId(), ownerDetail.getApplicationOwnerEmail().trim());
				if (list == null) {
					throw new DataValidationException("User Email with department not matched", null, null);
				}
				if (!list.getUserName().trim().equalsIgnoreCase(ownerDetail.getApplicaitonOwnerName().trim())) {
					throw new DataValidationException("user name is not matched with user details", null, null);
				}
			}
			List<String> projectName = projectDetailsRepository
					.findProjectByDeptName(validationObejct.getApplicationInfo().getApplicationOwnerDepartment());
			if (projectName.stream().noneMatch(validationObejct.getApplicationInfo().getProjectName()::equals)) {
				throw new DataValidationException(
						"Project name " + validationObejct.getApplicationInfo().getProjectName()
								+ " does not match the project in the department "
								+ validationObejct.getApplicationInfo().getApplicationOwnerDepartment(),
						"400", HttpStatus.BAD_REQUEST);
			}
			Integer i = 0;
			List<String> owneremails = new ArrayList<>();
			for (UserDetails userDetails : departmentDetails.getUserDetails()) {
				for (newApplicationOwnerDetailsRequest ownerDeetails : validationObejct.getApplicationInfo()
						.getOwnerDetails()) {
					if (userDetails.getUserEmail().trim().equalsIgnoreCase(ownerDeetails.getApplicationOwnerEmail())) {
						owneremails.add(ownerDeetails.getApplicationOwnerEmail());
						i++;
					}
				}
			}
			if (i == 0) {
				throw new DataValidationException(Constant.APPLICATION_EMAIL_ADDRESS + String.join(",", owneremails)
						+ Constant.FOR_DEPARTMENT_WITH_NAME
						+ validationObejct.getApplicationInfo().getApplicationOwnerDepartment()
						+ Constant.MISMATCH_ERROR, null, null);
			}
		} else if (providerDetailsRepository
				.findByProviderName(validationObejct.getApplicationInfo().getApplicationProviderName()) == null) {
			throw new DataValidationException("Provider Name "
					+ validationObejct.getApplicationInfo().getApplicationProviderName() + Constant.NOT_EXIST, null,
					null);
		}
	}

	private void newApplicationEmailVerfication(ApplicationOnboarding applicationOnboarding, String requestId,
			String childRequestId) throws IOException, TemplateException, DataValidationException, MessagingException {
		List<String> departments = new ArrayList<>();
		DepartmentDetails department;
		departments.add("Finance");
		departments.add("finance");
		String toAddress = null;
		for (String financedepartment : departments) {
			if (departmentRepository.findByDepartmentName(financedepartment) != null) {
				department = departmentRepository.findByDepartmentName(financedepartment);
				List<DepartmentOwnerDetails> owners = departmentOwnerRepository.findByDepartmentName(financedepartment);
				DepartmentOwnerDetails primaryOwner = owners.stream().filter(owner -> owner.getPriority() == 1)
						.findFirst().orElse(null);
				toAddress = department.getDepartmentAdmin() != null ? department.getDepartmentAdmin()
						: primaryOwner.getDepartmentOwnerEmail();
				if (toAddress == null) {
					throw new DataValidationException("No Department admin is there to send mail", null, null);
				}
			}
		}
		List<ApplicationOnboarding> requestOnboardingReplace;
		if (requestId != null) {
			requestOnboardingReplace = applicationOnboardingRepository.getByRequestNumber(requestId);
		} else {
			requestOnboardingReplace = applicationOnboardingRepository.getByChildRequestNumber(childRequestId);
		}

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String subject = Constant.APPLICATION_EMAIL_SUBJECT;
		Map<String, Object> model = new HashMap<>();
		Template t = config.getTemplate("new-application-notification.html");
		Template licenseReplace = config.getTemplate("licenseIterator.html");
		Template reviewrReplace = config.getTemplate("ReviewerIterator.html");

		String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
		PurchasedSingleAppOnboardingRequest applicationObject = applicationObjectDeserializer(
				applicationOnboarding.getApplcationOnboardRequest());
		String fullLicense = null;
		String fullWorkflow = null;
		for (NewAppLicenseInfo licenseIterator : applicationObject.getProducts()) {
			BigDecimal total = licenseIterator.getUnitPrice().multiply(new BigDecimal(licenseIterator.getQuantity()));
			String licenseIterators = FreeMarkerTemplateUtils.processTemplateIntoString(licenseReplace, model);
			licenseIterators = licenseIterators.replace("{{licenceType}}", licenseIterator.getProductType());
			licenseIterators = licenseIterators.replace("{{licenceName}}", licenseIterator.getProductName());
			licenseIterators = licenseIterators.replace("{{cost}}", licenseIterator.getUnitPrice().toString() + " "
					+ licenseIterator.getCurrencyCode() + " " + licenseIterator.getUnitPriceType());
			licenseIterators = licenseIterators.replace("{{quantity}}", licenseIterator.getQuantity().toString());
			licenseIterators = licenseIterators.replace("{{costTerm}}",
					total.toString() + " " + licenseIterator.getCurrencyCode());
			if (fullLicense != null) {
				fullLicense = fullLicense.concat(licenseIterators);
			} else {
				fullLicense = licenseIterators;
			}
		}
		for (ApplicationOnboarding workflowDetails : requestOnboardingReplace) {
			String reviewerIterator = FreeMarkerTemplateUtils.processTemplateIntoString(reviewrReplace, model);
			if (workflowDetails.getWorkGroup().equalsIgnoreCase(Constant.SUPER_ADMIN)) {
				reviewerIterator = reviewerIterator.replace("{{person}}", "SuperAdmin");
			} else {
				reviewerIterator = reviewerIterator.replace("{{person}}", workflowDetails.getWorkGroup());

			}
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
			Date givenTime = workflowDetails.getEndDate();
			LocalDateTime localDateTime = givenTime.toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
			ZonedDateTime istTime = localDateTime.atZone(ZoneId.of("Asia/Kolkata"));
			reviewerIterator = reviewerIterator.replace("{{workGroup}}", workflowDetails.getWorkGroup());
			reviewerIterator = reviewerIterator.replace("{{approvedBy}}", workflowDetails.getWorkGroupUserEmail());
			reviewerIterator = reviewerIterator.replace("{{approvedOn}}", istTime.format(formatter));
			reviewerIterator = reviewerIterator.replace("{{approvedComments}}", workflowDetails.getComments());
			if (workflowDetails.getWorkGroup().equalsIgnoreCase("Reviewer")
					|| workflowDetails.getWorkGroup().equalsIgnoreCase(Constant.SUPER_ADMIN)) {
				content = content.replace("{{onboardedBy}}", workflowDetails.getOnboardedByUserEmail());
				LocalDateTime localTime = workflowDetails.getCreatedOn().toInstant().atZone(ZoneId.of("Asia/Kolkata"))
						.toLocalDateTime();
				ZonedDateTime istLocalTime = localTime.atZone(ZoneId.of("Asia/Kolkata"));
				content = content.replace("{{onboardedOn}}", istLocalTime.format(formatter));
			}
			if (fullWorkflow != null) {
				fullWorkflow = fullWorkflow.concat(reviewerIterator);
			} else {
				fullWorkflow = reviewerIterator;
			}
		}
		content = content.replace("{{licenseIterator}}", fullLicense);
		content = content.replace("{{workflowIterator}}", fullWorkflow);

		content = content.replace("{{applicationType}}", "New");
		content = content.replace("{{applicationName}}", applicationObject.getApplicationInfo().getApplicationName());
		content = content.replace("{{providerName}}",
				applicationObject.getApplicationInfo().getApplicationProviderName());
		content = content.replace("{{category}}", applicationObject.getApplicationInfo().getApplicationCategory());
		content = content.replace("{{ownerEmail}}",
				applicationObject.getApplicationInfo().getOwnerDetails().get(0).getApplicationOwnerEmail());
		content = content.replace("{{ownerName}}",
				applicationObject.getApplicationInfo().getOwnerDetails().get(0).getApplicaitonOwnerName());
		content = content.replace("{{ownerDepartment}}",
				applicationObject.getApplicationInfo().getApplicationOwnerDepartment());
		content = content.replace("{{onboardingReaason}}",
				applicationObject.getApplicationInfo().getApplicationJustification());

		content = content.replace("{{onboardedBy}}", applicationOnboarding.getOnboardedByUserEmail());
		content = content.replace("{{onboardedOn}}", applicationOnboarding.getCreatedOn().toString());

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

	private void applicatoinContractValidator(PurchasedSingleAppOnboardingRequest validationObejct)
			throws DataValidationException, ParseException {
		List<NewAppContractInfo> appContractInfos = validationObejct.getContractInfo();
		for (NewAppContractInfo contractInfo : appContractInfos) {
			Date contractStartDate = contractInfo.getContractStartDate();
			Date contractEndtDate = contractInfo.getContractEndDate();
			if (Constant.CONTRACT_TYPE.stream().noneMatch(contractInfo.getContractType()::equals)) {
				throw new DataValidationException("Contract type does not match", "400", HttpStatus.BAD_REQUEST);
			}
			if (Constant.CURRENCY.stream().noneMatch(contractInfo.getCurrencyCode()::equals)) {
				throw new DataValidationException(Constant.CURRENCY_MISMATCH, "400", HttpStatus.BAD_REQUEST);
			}
			if (ContractType.annual(contractInfo.getContractType())) {
				Integer tenure = Integer.parseInt(contractInfo.getContractTenure());
				if (tenure != null) {
					if (Constant.BILLING_FREQUENCY.stream().noneMatch(contractInfo.getBillingFrequency()::equals)) {
						throw new DataValidationException("Billing Frequency does not match", "400",
								HttpStatus.BAD_REQUEST);
					}
					Calendar contractStart = dateToCalendar(contractStartDate);
					contractStart.add(Calendar.YEAR, tenure);
					Date actualEndDate = contractStart.getTime();
					LocalDate localDateContractEndDate = CommonUtil.dateToLocalDate(actualEndDate);
					Date dateConverted = CommonUtil.convertLocalDatetoDate(localDateContractEndDate.minusDays(1));
					if (CommonUtil.simpleDateFormat(contractEndtDate).after(dateConverted)) {
						throw new DataValidationException("For " + contractInfo.getContractName()
								+ " ContractEndDate is grater than ContarctTenure = "
								+ contractInfo.getContractTenure(), null, null);
					} else if (CommonUtil.simpleDateFormat(contractEndtDate).before(dateConverted)) {
						throw new DataValidationException("For " + contractInfo.getContractName()
								+ " ContractEndDate is less than ContarctTenure = " + contractInfo.getContractTenure(),
								null, null);
					}
				}
			} else if (ContractType.monthToMonth(contractInfo.getContractType())) {
				LocalDate localDateContractEndDate = CommonUtil.dateToLocalDate(contractStartDate);
				int days = CommonUtil.getDaysBasedOnDate(localDateContractEndDate);
				Date dateConverted = CommonUtil
						.convertLocalDatetoDate(localDateContractEndDate.plusDays(days).minusDays(1));
				if (CommonUtil.simpleDateFormat(contractEndtDate).after(dateConverted)) {
					throw new DataValidationException("For " + contractInfo.getContractName()
							+ " ContractEndDate is grater than expected EndDate", null, null);
				} else if (CommonUtil.simpleDateFormat(contractEndtDate).before(dateConverted)) {
					throw new DataValidationException(
							"For " + contractInfo.getContractName() + "  ContractEndDate is less than expected EndDate",
							null, null);
				}
			}
		}
		List<NewAppLicenseInfo> appLicenseInfos = validationObejct.getProducts();
		for (NewAppLicenseInfo licenseInfo : appLicenseInfos) {
			if (Constant.PRODUCT_TYPE.stream().noneMatch(licenseInfo.getProductType()::equals)) {
				throw new DataValidationException("product type does not match", "400", HttpStatus.BAD_REQUEST);
			}
			if (Constant.UNIT_PRICE.stream().noneMatch(licenseInfo.getUnitPriceType()::equals)) {
				throw new DataValidationException(
						"Unit price " + licenseInfo.getUnitPriceType() + Constant.MISMATCH_ERROR, "400",
						HttpStatus.BAD_REQUEST);
			}
			if (Constant.CURRENCY.stream().noneMatch(licenseInfo.getCurrencyCode()::equals)) {
				throw new DataValidationException(Constant.CURRENCY_MISMATCH, "400", HttpStatus.BAD_REQUEST);
			}
		}
	}

	private ApplicationLicenseDetails licenseContractTotalCost(ApplicationContractDetails contractDetails,
			NewAppLicenseInfo license) {
		ApplicationLicenseDetails applicationLicenseDetails = new ApplicationLicenseDetails();
		if (contractDetails != null) {
			if (contractDetails.getContractTenure() != null) {
				int months = 12;
				int totalMonths = months * contractDetails.getContractTenure();
				int quanity = license.getQuantity();
				if (license.getUnitPriceType().trim().equalsIgnoreCase(Constant.PER_YEAR)) {
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
				if (UnitPriceType.perContracttenure(license.getUnitPriceType())) {
					BigDecimal lotPrice = license.getUnitPrice().multiply(BigDecimal.valueOf(quanity));
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setTotalCost(lotPrice);
				}
			} else if (ContractType.monthToMonth(contractDetails.getContractType())) {
				if (UnitPriceType.perYear(license.getUnitPriceType())) {
					BigDecimal yearPrice = license.getUnitPrice();
					yearPrice = yearPrice.multiply(new BigDecimal(license.getQuantity()), MathContext.DECIMAL32);
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setTotalCost(yearPrice);
				}
				if (UnitPriceType.perMonth(license.getUnitPriceType())) {
					BigDecimal monthPrice = license.getUnitPrice();
					monthPrice = monthPrice.multiply(new BigDecimal(license.getQuantity()), MathContext.DECIMAL32);
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setTotalCost(monthPrice);
				}
				if (UnitPriceType.perContracttenure(license.getUnitPriceType())) {
					BigDecimal lotPrice = license.getUnitPrice().multiply(BigDecimal.valueOf(license.getQuantity()));
					applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
					applicationLicenseDetails.setTotalCost(lotPrice);
				}
			}

		} else {
			int quanity = license.getQuantity();
			if (license.getUnitPriceType().trim().equalsIgnoreCase(Constant.PER_YEAR)) {
				BigDecimal yearPrice = license.getUnitPrice();
				applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
				yearPrice = yearPrice.multiply(new BigDecimal(quanity), MathContext.DECIMAL32);
				applicationLicenseDetails.setTotalCost(yearPrice);
			}
			if (license.getUnitPriceType().trim().equalsIgnoreCase("per month")) {
				BigDecimal monthPrice = license.getUnitPrice();
				monthPrice = monthPrice.multiply(new BigDecimal(quanity), MathContext.DECIMAL32);
				applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
				applicationLicenseDetails.setTotalCost(monthPrice);
			}
			if (license.getUnitPriceType().trim().equalsIgnoreCase("per contract tenure")) {
				BigDecimal lotPrice = license.getUnitPrice().multiply(BigDecimal.valueOf(quanity));
				applicationLicenseDetails.setUnitPriceType(license.getUnitPriceType());
				applicationLicenseDetails.setTotalCost(lotPrice);
			}
		}
		return applicationLicenseDetails;
	}

	public BigDecimal getConvertedLicenseCost(Date contractStartDate, String contractCurrency, BigDecimal totalCost)
			throws ParseException {

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
				String url = applicationDetailUrl;
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

	@Override
	public void storeCredentials(Credentails credentails) {

		AdaptorCredential adaptorCredential = new AdaptorCredential();
		adaptorCredential.setApplicationName(credentails.getApplicationName());
		adaptorCredential.setAdaptorKeys(new Gson().toJson(credentails.getCredential()));
		adaptorCredentialRepository.save(adaptorCredential);

	}

	@Override
	public CommonResponse provideKeys(String applicationId) throws JsonProcessingException, DataValidationException {
		ApplicationDetails application = applicationDetailsRepository.findByApplicationId(applicationId);
		AdaptorCredential adaptorCredential = new AdaptorCredential();
		AdaptorCredentialListView list = new AdaptorCredentialListView();
		AdaptorCredential existingcredential = adaptorCredentialRepository
				.findByApplicationName(application.getApplicationName());
		if (existingcredential == null) {
			throw new DataValidationException(
					application.getApplicationName() + " Application credentials not onboarded yet", "400",
					HttpStatus.BAD_REQUEST);
		}
		List<AdaptorKeyValues> keyValuesList = convertStringToKeyValues(existingcredential.getAdaptorKeys());
		list.setKeyValues(keyValuesList);
		list.setAppName(application.getApplicationName());
		list.setOauthRequired(existingcredential.getFlowType() != null
				&& !existingcredential.getFlowType().equalsIgnoreCase("OAuth2.0"));
		list.setFlowType(existingcredential.getFlowType());
		if (!list.isOauthRequired()) {
			String[] endpointArray = existingcredential.getAccessTokenEndpoint().split(",");
			String[] endpointWithDomainArray = new String[endpointArray.length];
			for (int i = 0; i < endpointArray.length; i++) {
				endpointWithDomainArray[i] = domain + endpointArray[i];
			}
			String callbackApiEndpoint = String.join(", ", endpointWithDomainArray);
			list.setCallBackApiEndPoint(callbackApiEndpoint);
		}
		adaptorCredential.setApplicationName(application.getApplicationName());
		return new CommonResponse(HttpStatus.OK, new Response("ApplicationDetailsResponce", list),
				"Keys retrived Sucessfully");
	}

	private List<AdaptorKeyValues> convertStringToKeyValues(String keyValuesString) {
		List<AdaptorKeyValues> keyValuesList = new ArrayList<>();
		if (keyValuesString != null && !keyValuesString.isEmpty()) {
			JSONArray jsonArray = new JSONArray(keyValuesString);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.optJSONObject(i);
				if (jsonObject != null) {
					AdaptorKeyValues keyValue = new AdaptorKeyValues(jsonObject);
					keyValuesList.add(keyValue);
				}
			}
		}
		return keyValuesList;
	}

	public CommonResponse saveNewAppDetails(AdaptorValue request, String applicationId)
			throws DataValidationException, JsonProcessingException {
		Map<String, String> keyValues = request.getAdaptorValues();
		mapResponseToAdaptorValue(keyValues);
		AdaptorDetails oldappdetails = adaptorDetailsRepository.findByApplicationId(applicationId);
		ApplicationDetails application = applicationDetailsRepository.findByApplicationId(applicationId);
		AdaptorCredential appCredentials = adaptorCredentialRepository
				.findByApplicationName(application.getApplicationName());

		if (appCredentials == null) {
			throw new DataValidationException(
					application.getApplicationName() + " Application or Application credentials not onboarded", "400",
					HttpStatus.BAD_REQUEST);
		} else if (oldappdetails != null) {
			throw new DataValidationException(application.getApplicationName() + " Application already onboarded with "
					+ applicationId + ". Please add new application", "400", HttpStatus.BAD_REQUEST);
		}

		AdaptorDetails details = convertAdaptorValueToDetails(request, application.getApplicationName());

		Long latestId = adaptorDetailsRepository.findLatestId();
		details.setId(latestId != null ? latestId + 1 : 1);
		details.setApplicationName(application.getApplicationName());
		details.setApplicationId(applicationId);
		adaptorDetailsRepository.save(details);

		List<AdaptorKeyValues> keyValuesList = convertStringToKeyValues(appCredentials.getAdaptorKeys());

		if (application.getApplicationName().equalsIgnoreCase("Quickbooks")) {
			int errorCount = 0;
			for (int i = 0; i < keyValuesList.size(); i++) {
				AdaptorKeyValues adaptorValue = keyValuesList.get(i);
				if (adaptorValue.getKey().equals("clientId")) {
					Pattern pattern = Pattern.compile(adaptorValue.getRegex());
					if (!pattern.matcher(details.getClientId()).matches()) {
						errorCount++;
					}
				} else if (adaptorValue.getKey().equals("clientSecret")) {
					Pattern pattern = Pattern.compile(adaptorValue.getRegex());
					if (!pattern.matcher(details.getClientSecret()).matches()) {
						errorCount++;
					}
				} else if (adaptorValue.getKey().equals("redirectUri")) {
					Pattern pattern = Pattern.compile(adaptorValue.getRegex());
					if (!pattern.matcher(details.getRedirectUrl()).matches()) {
						errorCount++;
					}
				}
			}
			if (errorCount != 0) {
				adaptorDetailsRepository.delete(details);
				return new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response(Constant.SAVE_NEW_APP_RESPONSE, Constant.INVALID_CREDENTIALS),
						"Invalid credentials to connect Quickbooks");
			} else {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.SAVE_NEW_APP_RESPONSE,
								"Quickbooks application details saved successfully"),
						Constant.ADAPTORS_DETAILS_SAVED_SUCCESSFULLY);
			}
		}

		if (application.getApplicationName().equalsIgnoreCase(Constant.MICROSOFT_365)) {
			int errorCount = 0;
			for (int i = 0; i < keyValuesList.size(); i++) {
				AdaptorKeyValues adaptorValue = keyValuesList.get(i);
				if (adaptorValue.getKey().equals("client_id")) {
					Pattern pattern = Pattern.compile(adaptorValue.getRegex());
					if (!pattern.matcher(details.getClientId()).matches()) {
						errorCount++;
					}
				} else if (adaptorValue.getKey().equals("client_secret")) {
					Pattern pattern = Pattern.compile(adaptorValue.getRegex());
					if (!pattern.matcher(details.getClientSecret()).matches()) {
						errorCount++;
					}
				} else if (adaptorValue.getKey().equals("redirect_uri")) {
					Pattern pattern = Pattern.compile(adaptorValue.getRegex());
					if (!pattern.matcher(details.getRedirectUrl()).matches()) {
						errorCount++;
					}
				} else if (adaptorValue.getKey().equals("tenant_id")) {
					Pattern pattern = Pattern.compile(adaptorValue.getRegex());
					if (!pattern.matcher(details.getTenantid()).matches()) {
						errorCount++;
					}
				}
			}
			if (errorCount != 0) {
				adaptorDetailsRepository.delete(details);
				return new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response(Constant.SAVE_NEW_APP_RESPONSE, Constant.INVALID_CREDENTIALS),
						"Invalid credentials to connect Quickbooks");
			} else {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.SAVE_NEW_APP_RESPONSE,
								"Quickbooks application details saved successfully"),
						Constant.ADAPTORS_DETAILS_SAVED_SUCCESSFULLY);
			}
		}

		if (application.getApplicationName().equalsIgnoreCase(Constant.ZOHOCRM)) {
			CommonResponse tokenResponse = zohoCRMservice.getaccessToken(applicationId, details.getGrantCode());
			if (tokenResponse.getStatus().is2xxSuccessful()) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.SAVE_NEW_APP_RESPONSE, "ZohoCRM application details saved successfully"),
						Constant.ADAPTORS_SAVE_RESPONSE);
			} else {
				adaptorDetailsRepository.delete(details);
				return tokenResponse;
			}
		}
		if (application.getApplicationName().equalsIgnoreCase("Hubspot")) {
			CommonResponse userResponse = hubSpotService.getUser(applicationId);
			if (userResponse.getStatus().is2xxSuccessful()) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.SAVE_NEW_APPLICATION_RESPONSE,
								"Hubspot application details saved successfully"),
						Constant.ADAPTORS_DETAILS_SAVED_SUCCESSFULLY);
			} else {
				adaptorDetailsRepository.delete(details);
				return userResponse;
			}
		}

		if (application.getApplicationName().equalsIgnoreCase(Constant.CONFLUENCE)) {
			CommonResponse userResponse = confluenceWrapperService.getUserList(applicationId);
			if (userResponse.getStatus().is2xxSuccessful()) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.SAVE_NEW_APPLICATION_RESPONSE,
								"Confluence application details saved successfully"),
						Constant.ADAPTORS_DETAILS_SAVED_SUCCESSFULLY);
			} else {
				adaptorDetailsRepository.delete(details);
				return userResponse;
			}
		}

		if (application.getApplicationName().equalsIgnoreCase(Constant.DATADOG)) {
			CommonResponse userResponse = datadogWrapperService.getUser(applicationId);
			if (userResponse.getStatus().is2xxSuccessful()) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.SAVE_NEW_APPLICATION_RESPONSE,
								"Datadog application details saved successfully"),
						Constant.ADAPTORS_DETAILS_SAVED_SUCCESSFULLY);
			} else {
				adaptorDetailsRepository.delete(details);
				return userResponse;
			}
		}

		if (application.getApplicationName().equalsIgnoreCase(Constant.ZOHOPEOPLE)) {
			CommonResponse response = zohoPeopleService.getToken(applicationId, details.getGrantCode());
			if (response.getStatus().is2xxSuccessful()) {
				return new CommonResponse(HttpStatus.OK, new Response(Constant.SAVE_NEW_APP_RESPONSE,
						"ZohoPeople application details saved successfully"), Constant.ADAPTORS_SAVE_RESPONSE);
			} else {
				adaptorDetailsRepository.delete(details);
				return response;
			}
		}
		if (application.getApplicationName().equalsIgnoreCase("Gitlab")) {
			CommonResponse response = gitlabService.getUsersList(applicationId);
			if (response.getStatus().is2xxSuccessful()) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.SAVE_NEW_APP_RESPONSE, "Gitlab application details saved successfully"),
						Constant.ADAPTORS_SAVE_RESPONSE);
			} else {
				adaptorDetailsRepository.delete(details);
				return new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response(Constant.SAVE_NEW_APP_RESPONSE, Constant.INVALID_CREDENTIALS),
						"Invalid credentials to connect gitlab");
			}

		}
		if (application.getApplicationName().equalsIgnoreCase("Github")) {
			CommonResponse response = githubService.getUserDetails(applicationId);
			if (response.getStatus().is2xxSuccessful()) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.SAVE_NEW_APP_RESPONSE, "Github application details saved successfully"),
						Constant.ADAPTORS_SAVE_RESPONSE);
			} else {
				adaptorDetailsRepository.delete(details);
				return new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response(Constant.SAVE_NEW_APP_RESPONSE, Constant.INVALID_CREDENTIALS),
						"Invalid credentials to connect github");
			}

		}
		if (application.getApplicationName().equalsIgnoreCase("JIRA")) {
			CommonResponse response = jiraWrapperService.getAllUser(applicationId);
			if (response.getStatus().is2xxSuccessful()) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.SAVE_NEW_APP_RESPONSE, "Jira application details saved successfully"),
						Constant.ADAPTORS_SAVE_RESPONSE);
			} else {
				adaptorDetailsRepository.delete(details);
				return new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response(Constant.SAVE_NEW_APP_RESPONSE, Constant.INVALID_CREDENTIALS),
						"Invalid credentials to connect Jira");
			}

		}
		if (application.getApplicationName().equalsIgnoreCase(Constant.ZOHOANALYTICS)) {
			CommonResponse response = zohoAnalyticsService.getToken(applicationId, details.getGrantCode());
			if (response.getStatus().is2xxSuccessful()) {

				CommonResponse saveOrgResponse = zohoAnalyticsService.saveOrgDetail(applicationId);
				if (!saveOrgResponse.getStatus().is2xxSuccessful()) {
					adaptorDetailsRepository.delete(details);
					return new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response(Constant.SAVE_NEW_APP_RESPONSE, Constant.INVALID_CREDENTIALS),
							"Invalid credentials to connect" + Constant.ZOHOANALYTICS);
				}
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.SAVE_NEW_APP_RESPONSE,
								Constant.ZOHOANALYTICS + "application details saved successfully"),
						Constant.ADAPTORS_SAVE_RESPONSE);
			} else {
				adaptorDetailsRepository.delete(details);
				return new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response(Constant.SAVE_NEW_APP_RESPONSE, Constant.INVALID_CREDENTIALS),
						"Invalid credentials to connect" + Constant.ZOHOANALYTICS);
			}

		}
		if (application.getApplicationName().equalsIgnoreCase(Constant.SALESFORCE)) {

			CommonResponse response = salesforceService.generateToken(application.getApplicationId());
			if (response.getStatus().is2xxSuccessful()) {
				return new CommonResponse(HttpStatus.OK, new Response(Constant.SAVE_NEW_APP_RESPONSE,
						"Salesforce application details saved successfully"), Constant.ADAPTORS_SAVE_RESPONSE);
			} else {
				adaptorDetailsRepository.delete(details);
				return new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response(Constant.SAVE_NEW_APP_RESPONSE, Constant.INVALID_CREDENTIALS),
						"Invalid credentials to connect salesforce");
			}
		}

		if (application.getApplicationName().equalsIgnoreCase("Freshdesk")) {
			CommonResponse response = freshdeskService.getAccountDetails(applicationId);
			if (response.getStatus().is2xxSuccessful()) {
				return new CommonResponse(HttpStatus.OK, new Response(Constant.SAVE_NEW_APP_RESPONSE,
						"Freshdesk application details saved successfully"), Constant.ADAPTORS_SAVE_RESPONSE);
			} else {
				adaptorDetailsRepository.delete(details);
				return new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response(Constant.SAVE_NEW_APP_RESPONSE, Constant.INVALID_CREDENTIALS),
						"Invalid credentials to connect Freshdesk");
			}
		}

		if (application.getApplicationName().equalsIgnoreCase("Zoom")) {
			CommonResponse userResponse = zoomService.getUser(applicationId);
			if (userResponse.getStatus().is2xxSuccessful()) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.SAVE_NEW_APPLICATION_RESPONSE,
								"Zoom application details saved successfully"),
						Constant.ADAPTORS_DETAILS_SAVED_SUCCESSFULLY);
			} else {
				adaptorDetailsRepository.delete(details);
				return userResponse;
			}
		}

		return new CommonResponse(HttpStatus.OK, new Response(Constant.SAVE_NEW_APP_RESPONSE, new ArrayList<>()),
				Constant.ADAPTORS_SAVE_RESPONSE);

	}

	private AdaptorDetails convertAdaptorValueToDetails(AdaptorValue request, String appName) {
		AdaptorDetails details = new AdaptorDetails();
		Map<String, String> keyValues = request.getAdaptorValues();

		if (appName.equalsIgnoreCase("GitLab")) {
			details.setApiToken(keyValues.get(Constant.ACCESS_TOKEN));
			details.setGroupid(Long.valueOf(keyValues.get("groupId")));

		} else if (appName.equalsIgnoreCase("JIRA")) {
			details.setEmail(keyValues.get("userEmail"));
			details.setApiToken(keyValues.get(Constant.APITOKEN));

		} else if (appName.equalsIgnoreCase(Constant.CONFLUENCE)) {
			details.setEmail(keyValues.get("apiEmail"));
			details.setApiToken(keyValues.get(Constant.APITOKEN));

		} else if (appName.equalsIgnoreCase(Constant.DATADOG)) {
			details.setApiKey(keyValues.get("apiKey"));
			details.setApiToken(keyValues.get(Constant.APITOKEN));

		} else if (appName.equalsIgnoreCase(Constant.QUICKBOOOKS)) {
			details.setClientId(keyValues.get("clientId"));
			details.setClientSecret(keyValues.get("clientSecret"));
			details.setRedirectUrl(keyValues.get("redirectUri"));

		} else if (appName.equalsIgnoreCase("GitHub")) {
			details.setOrganizationName(keyValues.get("organizationName"));
			details.setApiToken(keyValues.get(Constant.ACCESS_TOKEN));

		} else if (appName.equalsIgnoreCase("Hubspot")) {
			details.setApiToken(keyValues.get(Constant.ACCESS_TOKEN));

		} else if (appName.equalsIgnoreCase(Constant.ZOHOCRM) || (appName.equalsIgnoreCase(Constant.ZOHOPEOPLE))
				|| (appName.equalsIgnoreCase(Constant.ZOHOANALYTICS))) {
			details.setClientId(keyValues.get(Constant.CLIENT_ID));
			details.setClientSecret(keyValues.get(Constant.CLIENT_SECRET));
			details.setGrantCode(keyValues.get(Constant.CODE));

		} else if (appName.equalsIgnoreCase(Constant.MICROSOFT_365)) {
			details.setClientId(keyValues.get(Constant.CLIENT_ID));
			details.setClientSecret(keyValues.get(Constant.CLIENT_SECRET));
			details.setRedirectUrl(keyValues.get("redirect_uri"));
			details.setTenantid(keyValues.get("tenant_id"));

		} else if (appName.equalsIgnoreCase(Constant.SALESFORCE)) {
			details.setClientId(keyValues.get(Constant.CLIENT_ID));
			details.setClientSecret(keyValues.get(Constant.CLIENT_SECRET));
			details.setOrganizationName(keyValues.get("organization_name"));

		} else if (appName.equalsIgnoreCase("Freshdesk")) {
			details.setOrganizationName(keyValues.get("domainName"));
			details.setApiToken(keyValues.get("apiKey"));

		} else if (appName.equalsIgnoreCase("Zoom")) {
			details.setClientId(keyValues.get(Constant.CLIENT_ID));
			details.setClientSecret(keyValues.get(Constant.CLIENT_SECRET));
			details.setRedirectUrl(keyValues.get("redirect_uri"));
		}

		return details;
	}

	public AdaptorValue mapResponseToAdaptorValue(Map<String, String> response) {
		AdaptorValue adaptorValue = new AdaptorValue();
		adaptorValue.setAdaptorValues(response);
		return adaptorValue;
	}

	private void deleteUsersFromZohocrm(String applicationId) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		CommonResponse userListResponse = zohoCRMservice.getUserFromCRM(applicationId, "ActiveUsers");
		try {

			if (userListResponse != null) {

				List<CRMUsersResponse> mapUserResponse = objectMapper.readValue(
						objectMapper.writeValueAsString(userListResponse.getResponse().getData()),
						new TypeReference<List<CRMUsersResponse>>() {
						});
				for (CRMUsersResponse user : mapUserResponse) {
					zohoCRMservice.deleteUserInCRM(applicationId, user.getId());
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void deleteUserFromGithub(String applicationId) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		CommonResponse orgMembers = githubService.getOrganizationMembers(applicationId);
		List<GithubUser> members = objectMapper.readValue(
				objectMapper.writeValueAsString(orgMembers.getResponse().getData()),
				new TypeReference<List<GithubUser>>() {
				});
		for (GithubUser member : members) {
			RemoveUserRequest deleteUser = new RemoveUserRequest(member.getLogin());
			githubService.removeOrganizationMember(applicationId, deleteUser);
		}
	}

	private void deleteUsersFromGitlab(String applicationId) throws JsonProcessingException {

		ObjectMapper objectMapper = new ObjectMapper();
		CommonResponse groupMembers = gitlabService.getResourceMembers(applicationId);
		List<GitlabUser> members = objectMapper.readValue(
				objectMapper.writeValueAsString(groupMembers.getResponse().getData()),
				new TypeReference<List<GitlabUser>>() {
				});
		for (GitlabUser member : members) {
			if (!member.getAccessLevel().equalsIgnoreCase("Owner")) {
				GitlabDeleteUserRequest deleteUser = new GitlabDeleteUserRequest(member.getId());
				gitlabService.removeGitlabMember(deleteUser, applicationId);
			}
		}
		CommonResponse groupInvitation = gitlabService.getInvitationsList(applicationId);
		List<GitlabInvitationResponse> invitations = objectMapper.readValue(
				objectMapper.writeValueAsString(groupInvitation.getResponse().getData()),
				new TypeReference<List<GitlabInvitationResponse>>() {
				});
		for (GitlabInvitationResponse invitation : invitations) {
			gitlabService.revokeInvitation(applicationId, invitation.getInvite_email());
		}

	}

	private void deleteUsersFromQuickbooks(String appId) throws JsonProcessingException {

		ObjectMapper objectMapper = new ObjectMapper();
		CommonResponse quickbooksUsersResponse = quickbooksService.getUsers(appId);
		List<QuickBooksUsers> quickbookUsers = objectMapper.readValue(
				objectMapper.writeValueAsString(quickbooksUsersResponse.getResponse().getData()),
				new TypeReference<List<QuickBooksUsers>>() {
				});
		for (QuickBooksUsers quickbookUser : quickbookUsers) {
			quickbooksService.deleteUser(appId, quickbookUser.getUserEmail());
		}

	}

	private void deleteUsersFromHubSpot(String appId) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		CommonResponse userListResponse = hubSpotService.getUser(appId);
		try {
			if (userListResponse != null) {
				List<HubSpotGetUserlistResponse> mapUserResponse = objectMapper.readValue(
						objectMapper.writeValueAsString(userListResponse.getResponse().getData()),
						new TypeReference<List<HubSpotGetUserlistResponse>>() {
						});
				for (HubSpotGetUserlistResponse user : mapUserResponse) {
					hubSpotService.deleteUser(appId, user.getEmail());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteUsersFromConfluence(String appId) throws DataValidationException {
		List<AtlassianJiraUsers> atlassianUsers = atlassianJiraUsersRepository.findAll();
		for (AtlassianJiraUsers atlassianUser : atlassianUsers) {
			if (atlassianUser != null) {
				confluenceWrapperService.deleteUser(atlassianUser.getAccountId(), appId);
			} else {
				throw new DataValidationException("No such user found in Confluence", "404", HttpStatus.NOT_FOUND);
			}
		}
	}

	private void deleteUsersFromDatadog(String appId) {
		ObjectMapper objectMapper = new ObjectMapper();
		CommonResponse userListResponse = datadogWrapperService.getUser(appId);
		try {
			if (userListResponse != null) {
				List<DatadogGetUserResponse> mapUserResponse = objectMapper.readValue(
						objectMapper.writeValueAsString(userListResponse.getResponse().getData()),
						new TypeReference<List<DatadogGetUserResponse>>() {
						});
				for (DatadogGetUserResponse user : mapUserResponse) {
					datadogWrapperService.deleteUser(appId, user.getEmail());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteUsersFromMicrosoft365(String appId) throws JsonProcessingException, InterruptedException {
		ObjectMapper objectMapper = new ObjectMapper();
		CommonResponse userListResponse = microsoft365Service.getUser(appId);
		try {
			if (userListResponse != null) {
				List<Microsoft365getUserlistResponse> mapUserResponse = objectMapper.readValue(
						objectMapper.writeValueAsString(userListResponse.getResponse().getData()),
						new TypeReference<List<Microsoft365getUserlistResponse>>() {
						});
				for (Microsoft365getUserlistResponse user : mapUserResponse) {
					microsoft365Service.deleteUser(appId, user.getMail());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteUsersFromZohoPeople(String applicationId, @NonNull String userEmail) {
		zohoPeopleService.revokeAccess(userEmail, applicationId);
	}

	private void deleteUsersFromJira(String appId) throws DataValidationException {
		List<AtlassianJiraUsers> atlassianUsers = atlassianJiraUsersRepository.findAll();
		for (AtlassianJiraUsers atlassianUser : atlassianUsers) {
			if (atlassianUser != null) {
				jiraWrapperService.removeUserFromGroup(atlassianUser.getAccountId(), appId);
			} else {
				throw new DataValidationException("No such user found in Jira", "404", HttpStatus.NOT_FOUND);
			}
		}
	}

	private void deleteUsersFromZoom(String appId) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		CommonResponse userListResponse = zoomService.getUser(appId);
		try {
			if (userListResponse != null) {
				List<ZoomgetUserlistResponse> mapUserResponse = objectMapper.readValue(
						objectMapper.writeValueAsString(userListResponse.getResponse().getData()),
						new TypeReference<List<ZoomgetUserlistResponse>>() {
						});
				for (ZoomgetUserlistResponse user : mapUserResponse) {
					zoomService.deleteUser(appId, user.getEmail());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public CommonResponse updateAppCredentials() {

		List<AdaptorDetails> applicationList = adaptorDetailsRepository.findAll();

		for (AdaptorDetails application : applicationList) {
			if (Constant.OAUTH_APPS.contains(application.getApplicationName().toUpperCase())
					&& application.getApiToken() == null) {
				adaptorDetailsRepository.delete(application);
			}
		}

		return new CommonResponse(HttpStatus.OK, new Response("Update application credentials", null),
				"Application credentials updated successfully");
	}

	@Override
	public CommonResponse updateRefreshTokens() {
		List<AdaptorDetails> adaptors = adaptorDetailsRepository.findAll();
		LocalDate currentDate = LocalDate.now();
		RestTemplate restTemplate = new RestTemplate();
		for (AdaptorDetails adaptor : adaptors) {
			long daysSinceLastRefresh = adaptor.getRefreshtokenCreatedOn() != null ? Math.abs(ChronoUnit.DAYS.between(
					adaptor.getRefreshtokenCreatedOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
					currentDate)) : 0;
			String url = null;
			if (adaptor.getApplicationName().equalsIgnoreCase(Constant.QUICKBOOOKS) && (daysSinceLastRefresh >= 99)) {
				url = String.format(
						getQuickbooksUrls().getRefreshTokenUrl().replace(Constant.HOST, adaptorsHost) + "?appId=%s",
						adaptor.getApplicationId());
			} else if (adaptor.getApplicationName().equalsIgnoreCase(Constant.MICROSOFT_365)
					&& (daysSinceLastRefresh >= 89)) {
				url = String.format(adaptorsHost + refreshTokenUrl + "?appId=%s", adaptor.getApplicationId());
			} else if (adaptor.getApplicationName().equalsIgnoreCase(Constant.ZOOM) && (daysSinceLastRefresh >= 89)) {
				url = String.format(adaptorsHost + zoomRefreshTokenUrl + "?appId=%s", adaptor.getApplicationId());
			}

			if (url != null) {
				try {
					restTemplate.exchange(url, HttpMethod.GET, null, String.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("UpdateApplicationRefreshTokenResponse", null),
				"Application refreshtoken updated successfully");
	}

}