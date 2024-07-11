package saaspe.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import saaspe.cloud.repository.AwsBudgetDocumentRepository;
import saaspe.cloud.repository.AwsCostAndUsageDocumentRepository;
import saaspe.cloud.repository.AwsCostAndUsageMonthlyDocumentRepository;
import saaspe.cloud.repository.AwsCostForecastDocumentRepository;
import saaspe.cloud.repository.AwsResourcesDocumentRepository;
import saaspe.cloud.repository.AzureAdvisorRecommendationsRepository;
import saaspe.cloud.repository.AzureForecastRepository;
import saaspe.cloud.repository.AzureSpendHistoryRepository;
import saaspe.cloud.repository.AzureSubscriptionsRepository;
import saaspe.cloud.repository.BudgetRepository;
import saaspe.cloud.repository.CostManagementQueryMonthlyRepository;
import saaspe.cloud.repository.CostManagementQueryQuaterlyRepository;
import saaspe.cloud.repository.CostManagementQueryYearlyRepository;
import saaspe.cloud.repository.CostManagementUsageByServiceNameRepository;
import saaspe.cloud.repository.MulticloudBudgetRepository;
import saaspe.cloud.repository.ResourceGroupRepository;
import saaspe.cloud.repository.ResourceGroupsActualCostRepository;
import saaspe.cloud.repository.ResourcesRepository;
import saaspe.cloud.repository.RightsizingRecommendationDocumentRepository;
import saaspe.configuration.DateParser;
import saaspe.constant.CloudVendors;
import saaspe.constant.Constant;
import saaspe.document.AdvisorDocument;
import saaspe.document.AwsBudgetDocument;
import saaspe.document.AwsCostAndUsageDocument;
import saaspe.document.AwsCostAndUsageMonthlyDocument;
import saaspe.document.AwsCostForecastDocument;
import saaspe.document.AwsResourcesDocument;
import saaspe.document.AwsRightsizingRecommendationDocument;
import saaspe.document.AzureForecastDocument;
import saaspe.document.AzureSpendingHistoryDocument;
import saaspe.document.AzureSubscriptions;
import saaspe.document.BudgetsDocument;
import saaspe.document.CostManagementQueryMonthlyDocument;
import saaspe.document.CostManagementQueryQuaterlyDocument;
import saaspe.document.CostManagementQueryYearlyDocument;
import saaspe.document.CostManagementUsageByServiceNameDocument;
import saaspe.document.ResourceGroupDocument;
import saaspe.document.ResourceGroupsActualCostDocument;
import saaspe.document.ResourcesDocument;
import saaspe.dto.SubscriptionDao;
import saaspe.entity.ApplicationLogoEntity;
import saaspe.entity.CloudServiceDetails;
import saaspe.entity.MultiCloudDetails;
import saaspe.entity.SequenceGenerator;
import saaspe.entity.UserLoginDetails;
import saaspe.entity.UserSubscription;
import saaspe.exception.DataValidationException;
import saaspe.model.AwsResourcesResponse;
import saaspe.model.AzureForecastData;
import saaspe.model.AzureForecastResponse;
import saaspe.model.AzureRecommendationResponse;
import saaspe.model.AzureResourceListResponse;
import saaspe.model.AzureSubscriptionsResponse;
import saaspe.model.AzureTotalSpendResponse;
import saaspe.model.BudgetAlertDetailRequest;
import saaspe.model.BudgetRequest;
import saaspe.model.CloudOnboardRequest;
import saaspe.model.CloudProvider;
import saaspe.model.CommonResponse;
import saaspe.model.CostInfo;
import saaspe.model.ForecastDataListResponse;
import saaspe.model.ForecastMonthAndCost;
import saaspe.model.IntegratedVendorsResponse;
import saaspe.model.MonthlyCost;
import saaspe.model.MonthlySpendingResponse;
import saaspe.model.MultiCloudCountResponse;
import saaspe.model.MultiCloudOverviewResponse;
import saaspe.model.MultiCloudResponse;
import saaspe.model.MultiCloudSpendResponse;
import saaspe.model.MulticloudBudgetResponse;
import saaspe.model.Notification;
import saaspe.model.OptimizeRequest;
import saaspe.model.OptimizeRequestAws;
import saaspe.model.Response;
import saaspe.model.SpendHistory;
import saaspe.model.SpendHistoryResponse;
import saaspe.model.Subscriber;
import saaspe.model.UserSubscriptionRequest;
import saaspe.model.VendorsResponse;
import saaspe.repository.ApplicationLogoRepository;
import saaspe.repository.CloudDetailsRepository;
import saaspe.repository.MultiCloudDetailRepository;
import saaspe.repository.SequenceGeneratorRepository;
import saaspe.repository.UserSubscriptionRepository;
import saaspe.service.MultiCloudService;
import saaspe.utils.CommonUtil;

@Service
public class MultiCloudServiceImpl implements MultiCloudService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private AzureForecastRepository azureForecastRepository;

	@Autowired
	private AzureAdvisorRecommendationsRepository advisorRecommendationsRepository;

	@Autowired
	private UserSubscriptionRepository subscriptionRepo;

	@Autowired
	private AwsCostForecastDocumentRepository awsCostForecastDocumentRepository;

	@Autowired
	private CloudDetailsRepository detailsRespo;

	@Autowired
	private MulticloudBudgetRepository multicloudBudgetRepository;

	@Autowired
	private CostManagementQueryMonthlyRepository managementQueryMonthlyRepository;

	@Autowired
	private CostManagementQueryQuaterlyRepository managementQueryQuaterlyRepository;

	@Autowired
	private CostManagementQueryYearlyRepository managementQueryYearlyRepository;

	@Autowired
	private AwsResourcesDocumentRepository awsResourcesDocumentRepository;

	@Autowired
	private AzureSubscriptionsRepository azureSubscriptionsRepository;

	@Autowired
	private CostManagementUsageByServiceNameRepository costManagementUsageByServiceNameRepository;

	@Autowired
	private ResourceGroupsActualCostRepository resourceGroupsActualCostRepository;

	@Autowired
	private AwsCostAndUsageMonthlyDocumentRepository awsCostAndUsageMonthlyDocumentRepository;

	@Autowired
	private ResourceGroupRepository resourceGroupRepository;

	@Autowired
	private ResourcesRepository resourcesRepository;

	@Autowired
	private AwsBudgetDocumentRepository awsBudgetDocumentRepository;

	@Autowired
	private AwsCostAndUsageDocumentRepository awsCostAndUsageDocumentRepository;

	@Autowired
	private RightsizingRecommendationDocumentRepository rightsizingRecommendationDocumentRepository;

	@Autowired
	private ApplicationLogoRepository applicationLogoRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private SequenceGeneratorRepository sequenceGeneratorRepository;

	@Autowired
	private MultiCloudDetailRepository multiCloudDetailRepository;

	@Autowired
	private BudgetRepository budgetRepository;
	
	@Autowired
	private AzureSpendHistoryRepository azureSpendHistoryRepository;

	@Autowired
	private Configuration config;

	@Value("${sendgrid.domain.name}")
	private String sendgridDomain;

	@Value("${sendgrid.domain.sendername}")
	private String domainName;

	@Value("${demo.mutlicloud.data}")
	private Boolean demoMultiCloudData;

	@Value("${budget-mail.trigger.dev}")
	private boolean budgetMailTrigger;

	@Value("${budget.create.url}")
	private String createBudgetUrl;

	@Value("${azure.host.url}")
	private String azureHost;

	@Value("${logos.aws.url}")
	private String awsLogoUrl;

	@Value("${logos.azure.url}")
	private String azureLogoUrl;

	@Transactional
	@Override
	public CommonResponse addUserSubscriptionDetails(UserSubscriptionRequest request) throws DataValidationException {
		UserSubscription subscription = new UserSubscription();
		subscription.setAmountSpent(request.getAmountSpent());
		subscription.setAccountName(request.getAccountName());
		subscription.setRenewalDate(request.getRenewalDate());
		subscription.setRenewalType(request.getRenewalType());
		subscription.setSubscriptionStartDate(new Date());
		CloudServiceDetails cloudDetails = detailsRespo.findByServiceName(request.getAccountName());
		if (cloudDetails != null) {
			subscription.setVendorId(cloudDetails.getVendorId());
			subscription.setSubscriptionName(cloudDetails.getSubscriptionType());
		} else {
			throw new DataValidationException("service name not found", null, HttpStatus.NOT_FOUND);
		}

		subscriptionRepo.save(subscription);
		return new CommonResponse(HttpStatus.OK, new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()),
				"Multicloud data saved successfully");
	}

	@Override
	public CommonResponse getDetails() throws DataValidationException {
		List<MultiCloudResponse> response = new ArrayList<>();
		List<UserSubscription> subscription = subscriptionRepo.findAll();
		if (subscription.isEmpty()) {
			throw new DataValidationException(Constant.CLOUD_DETAILS_NOT_FOUND, null, HttpStatus.NOT_FOUND);
		}
		for (UserSubscription us : subscription) {
			Optional<CloudServiceDetails> cloudDetails = detailsRespo.findById(us.getVendorId());
			if (cloudDetails.isPresent()) {
				MultiCloudResponse cloudResponse = new MultiCloudResponse();
				cloudResponse.setCategory(cloudDetails.get().getCategory());
				cloudResponse.setServiceName(cloudDetails.get().getServiceName());
				cloudResponse.setPrice(cloudDetails.get().getPrice());
				cloudResponse.setSubscriptionType(cloudDetails.get().getVendor());
				cloudResponse.setVendorName(cloudDetails.get().getSubscriptionType());
				cloudResponse.setResourceId(cloudDetails.get().getResourceId());
				cloudResponse.setCurrency(cloudDetails.get().getCurrency());
				cloudResponse.setLogo(cloudDetails.get().getLogo());
				cloudResponse.setRenewalDate(us.getRenewalDate());
				cloudResponse.setRenewalType(us.getRenewalType());
				cloudResponse.setAmountSpent(us.getAmountSpent());
				response.add(cloudResponse);
			} else {
				throw new DataValidationException(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.MULTI_CLOUD_RESPONSE, response),
				Constant.DATA_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getSpendingHistory() throws DataValidationException {
		List<MultiCloudResponse> response = new ArrayList<>();
		List<UserSubscription> subscription = subscriptionRepo.findByAccountNameOrderedByDate();
		if (subscription.isEmpty()) {
			throw new DataValidationException(Constant.CLOUD_DETAILS_NOT_FOUND, null, HttpStatus.NOT_FOUND);
		}
		if (Boolean.TRUE.equals(demoMultiCloudData)) {
			for (UserSubscription us : subscription) {
				Optional<CloudServiceDetails> cloudDetails = detailsRespo.findById(us.getVendorId());
				if (cloudDetails.isPresent()) {
					MultiCloudResponse cloudResponse = new MultiCloudResponse();
					cloudResponse.setServiceName(cloudDetails.get().getServiceName());
					cloudResponse.setVendorName(cloudDetails.get().getSubscriptionType());
					cloudResponse.setCurrency(cloudDetails.get().getCurrency());
					cloudResponse.setLogo(cloudDetails.get().getLogo());
					cloudResponse.setRenewalDate(us.getRenewalDate());
					cloudResponse.setSubscriptionStartDate(us.getSubscriptionStartDate());
					cloudResponse.setRenewalType(us.getRenewalType());
					cloudResponse.setResourceId(cloudDetails.get().getResourceId());
					cloudResponse.setAmountSpent(us.getAmountSpent());
					response.add(cloudResponse);
				} else {
					throw new DataValidationException(Constant.CLOUD_DETAILS_NOT_FOUND, null, HttpStatus.NOT_FOUND);
				}
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.MULTI_CLOUD_RESPONSE, response),
				Constant.DATA_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getRecentSpendingHistory() throws DataValidationException {
		List<MultiCloudResponse> response = new ArrayList<>();
		List<UserSubscription> subscription = subscriptionRepo.findByAccountOrderedByDate();
		if (subscription.isEmpty()) {
			throw new DataValidationException(Constant.CLOUD_DETAILS_NOT_FOUND, null, HttpStatus.NOT_FOUND);
		}
		for (UserSubscription us : subscription) {
			Optional<CloudServiceDetails> cloudDetails = detailsRespo.findById(us.getVendorId());
			if (cloudDetails.isPresent()) {
				MultiCloudResponse cloudResponse = new MultiCloudResponse();
				cloudResponse.setServiceName(cloudDetails.get().getServiceName());
				cloudResponse.setVendorName(cloudDetails.get().getSubscriptionType());
				cloudResponse.setCurrency("INR");
				cloudResponse.setLogo(cloudDetails.get().getLogo());
				cloudResponse.setRenewalDate(us.getRenewalDate());
				cloudResponse.setSubscriptionStartDate(us.getSubscriptionStartDate());
				cloudResponse.setRenewalType(us.getRenewalType());
				cloudResponse.setResourceId(cloudDetails.get().getResourceId());
				cloudResponse.setAmountSpent(us.getAmountSpent());
				response.add(cloudResponse);
			} else {
				throw new DataValidationException(Constant.CLOUD_DETAILS_NOT_FOUND, null, HttpStatus.NOT_FOUND);
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.MULTI_CLOUD_RESPONSE, response),
				Constant.DATA_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getTotalSpendBasedOnServiceName() throws DataValidationException, ParseException {
		List<MultiCloudSpendResponse> spendResponses = new ArrayList<>();
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
		List<AwsCostAndUsageDocument> awsCostAndUsageDocument = awsCostAndUsageDocumentRepository.findAll();
		if (awsCostAndUsageDocument != null) {
			for (AwsCostAndUsageDocument costAndUsageDocument : awsCostAndUsageDocument) {
				Integer j;
				for (j = 0; j < costAndUsageDocument.getGroups().size(); j++) {
					MultiCloudSpendResponse multiCloudSpendResponse = new MultiCloudSpendResponse();
					multiCloudSpendResponse.setTotalAmountSpent(new BigDecimal(costAndUsageDocument.getGroups().get(j)
							.getMetrics().get(Constant.UNBLENDED_COST).getAmount()));
					multiCloudSpendResponse.setServiceName(costAndUsageDocument.getGroups().get(j).getKeys().get(0));
					multiCloudSpendResponse.setCurrency("INR");
					multiCloudSpendResponse.setLogo(awsLogoUrl);
					multiCloudSpendResponse.setVendorName("AWS");
					spendResponses.add(multiCloudSpendResponse);
				}
			}
		}
		for (AzureSubscriptions subscriptions : azureSubscriptions) {
			CostManagementUsageByServiceNameDocument costManagementUsageByServiceNameDocument = costManagementUsageByServiceNameRepository
					.findBySubscriptionId(subscriptions.getSubscriptionId());
			Integer i;
			if (costManagementUsageByServiceNameDocument != null) {
				for (i = 0; i < costManagementUsageByServiceNameDocument.getRows().size(); i++) {
					MultiCloudSpendResponse multiCloudSpendResponse = new MultiCloudSpendResponse();
					multiCloudSpendResponse.setTotalAmountSpent(new BigDecimal(
							costManagementUsageByServiceNameDocument.getRows().get(i).get(0).toString()));
					multiCloudSpendResponse.setServiceName(
							costManagementUsageByServiceNameDocument.getRows().get(i).get(2).toString());
					multiCloudSpendResponse.setCurrency("INR");
					multiCloudSpendResponse.setLogo(azureLogoUrl);
					multiCloudSpendResponse.setVendorName(Constant.AZURE);
					spendResponses.add(multiCloudSpendResponse);
				}
			}
		}
		spendResponses.sort(Comparator.comparing(MultiCloudSpendResponse::getTotalAmountSpent).reversed());
		if (spendResponses.size() <= 10) {
			spendResponses = spendResponses.subList(0, spendResponses.size());
		} else {
			spendResponses = spendResponses.subList(0, 10);
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.MULTI_CLOUD_RESPONSE, spendResponses),
				Constant.DATA_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getCountBasedOnSubscriptionType(HttpServletRequest request) throws DataValidationException {
		Response response = new Response();
		List<MultiCloudCountResponse> countBasedOnVendor = new ArrayList<>();
		List<MultiCloudDetails> allClouds = multiCloudDetailRepository.findAll();
		if (allClouds.isEmpty()) {
			throw new DataValidationException(Constant.CLOUD_DETAILS_NOT_FOUND, null, HttpStatus.NOT_FOUND);
		}
		for (MultiCloudDetails cloudDetails : allClouds) {
			String providerName = cloudDetails.getProviderName();
			if (providerName.equalsIgnoreCase("Azure")) {
				countBasedOnVendor.add(getAzureCount());
			} else if (providerName.equalsIgnoreCase("AWS")) {
				countBasedOnVendor.add(getAwsCount());
			} else if (providerName.equalsIgnoreCase("GCP")) {
				countBasedOnVendor.add(getGcpCount());
			}
		}
		response.setData(countBasedOnVendor);
		return new CommonResponse(HttpStatus.OK, response, Constant.DATA_RETRIEVED_SUCCESSFULLY);
	}

	private MultiCloudCountResponse getAzureCount() {
		List<String> azureList = new ArrayList<>();
		List<MultiCloudOverviewResponse> azureOverviewResponses = new ArrayList<>();
		MultiCloudCountResponse azureResponse = new MultiCloudCountResponse();
		for (ResourceGroupDocument document : resourceGroupRepository.findAll()) {
			List<ResourcesDocument> resourcesDocuments = resourcesRepository
					.getresoucresByResourcesGroup(document.getName(), document.getSubscriptionId());
			for (ResourcesDocument resource : resourcesDocuments) {
				LocalDate localDate = CommonUtil.dateToLocalDate(resource.getUpdatedOn());
				if (localDate.equals(LocalDate.now())) {
					if (resource != null) {
						azureList.add(resource.getType().substring(resource.getType().indexOf("/") + 1));
					}
				}
			}
		}
		Set<String> azSet = new HashSet<>(azureList);
		for (String az : azSet) {
			MultiCloudOverviewResponse azureOverview = new MultiCloudOverviewResponse();
			azureOverview.setServiceName(az.substring(0, 1).toUpperCase() + az.substring(1).toLowerCase());
			azureOverview.setCount(Collections.frequency(azureList, az));
			azureOverviewResponses.add(azureOverview);
		}
		azureResponse.setVendorName(Constant.AZURE);
		azureResponse.setLogo(azureLogoUrl);
		azureResponse.setService(azureOverviewResponses);
		return azureResponse;
	}

	private MultiCloudCountResponse getAwsCount() {
		List<String> awsList = new ArrayList<>();
		List<MultiCloudOverviewResponse> awsOverviewResponses = new ArrayList<>();
		MultiCloudCountResponse awsResponse = new MultiCloudCountResponse();

		List<AwsResourcesDocument> resourcesDocuments = awsResourcesDocumentRepository.findAll();
		for (AwsResourcesDocument resource : resourcesDocuments) {
			if (resource != null && !resource.getResourceARN().equalsIgnoreCase("arn:aws:s3:::mgpocbucket")) {
				String[] str = resource.getResourceARN().split(":", 4);
				for (int i = 0; i < str.length; i++) {
					if (i % 2 == 0 && !str[i].equalsIgnoreCase("arn")) {
						awsList.add(str[i].substring(0, 1).toUpperCase() + str[i].substring(1).toLowerCase());
					}
				}
			}
		}

		Set<String> awsSet = new HashSet<>(awsList);
		for (String aws : awsSet) {
			MultiCloudOverviewResponse awsOverview = new MultiCloudOverviewResponse();
			awsOverview.setServiceName(aws.substring(0, 1).toUpperCase() + aws.substring(1).toLowerCase());
			awsOverview.setCount(Collections.frequency(awsList, aws));
			awsOverviewResponses.add(awsOverview);
		}

		awsResponse.setVendorName("AWS");
		awsResponse.setLogo(awsLogoUrl);
		awsResponse.setService(awsOverviewResponses);

		return awsResponse;
	}

	private MultiCloudCountResponse getGcpCount() {
		List<MultiCloudOverviewResponse> gcpOverviewResponses = new ArrayList<>();
		MultiCloudCountResponse gcpResponse = new MultiCloudCountResponse();
		if (Boolean.TRUE.equals(demoMultiCloudData)) {
			List<SubscriptionDao> gcpSubscriptions = subscriptionRepo.getCountBasedOnSubscriptionType("Google");
			if (!gcpSubscriptions.isEmpty()) {
				CloudServiceDetails cloudServiceDetails = detailsRespo
						.findByServiceName(gcpSubscriptions.get(0).getAccountname());
				gcpResponse.setLogo(cloudServiceDetails.getLogo());
				for (SubscriptionDao dao : gcpSubscriptions) {
					MultiCloudOverviewResponse gcpOverview = new MultiCloudOverviewResponse();
					gcpOverview.setServiceName(dao.getAccountname());
					gcpOverview.setCount(dao.getCount());
					gcpOverviewResponses.add(gcpOverview);
				}
			}
			gcpResponse.setVendorName("GCP");
			gcpResponse.setService(gcpOverviewResponses);
		}
		return gcpResponse;
	}

	@Override
	public CommonResponse getQuaterlyAmountByService() throws DataValidationException, ParseException {
		List<MultiCloudSpendResponse> list = new ArrayList<>();
		List<String> vendors = Arrays.asList("Google", "Digital Ocean", "Oracle Cloud");
		for (String s : vendors) {
			List<SubscriptionDao> intData = subscriptionRepo.getTotalSpendBasedOnServiceName(s);
			if (Boolean.TRUE.equals(demoMultiCloudData)) {
				for (SubscriptionDao sc : intData) {
					MultiCloudSpendResponse cloudResponse = new MultiCloudSpendResponse();
					cloudResponse.setTotalAmountSpent(new BigDecimal(sc.getCount()));
					cloudResponse.setVendorName(sc.getSubscriptionname());
					cloudResponse.setCurrency("INR");
					list.add(cloudResponse);
				}
			}
		}

		List<AwsCostAndUsageMonthlyDocument> listDocuments = awsCostAndUsageMonthlyDocumentRepository.findAll();
		if (!listDocuments.isEmpty()) {
			BigDecimal awsTotal = BigDecimal.valueOf(0);
			MultiCloudSpendResponse multiCloudSpendResponse = new MultiCloudSpendResponse();
			for (AwsCostAndUsageMonthlyDocument document : listDocuments) {
				SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATE_FORMAT);
				Date startOfMonth = new Date();
				Date quarterEnd = new Date();
				Date qStart = new Date();
				try {
					startOfMonth = sdf.parse(document.getTimePeriod().getStart());
					LocalDate quarterStartDate = LocalDate.parse(getFirstDateOfQuarter());
					quarterStartDate = quarterStartDate.minusDays(1);
					qStart = sdf.parse(quarterStartDate.toString());
					quarterEnd = sdf.parse(getLastDateOfQuarter());
				} catch (ParseException e) {
					throw new ParseException(e.getMessage(), 0);
				}
				if (startOfMonth.after(qStart) && startOfMonth.before(quarterEnd)) {
					String amount = document.getTotal().get(Constant.UNBLENDED_COST).getAmount();
					BigDecimal decimal = new BigDecimal(amount);
					awsTotal = awsTotal.add(decimal, MathContext.DECIMAL64);
				}
				multiCloudSpendResponse.setTotalAmountSpent(awsTotal);
				multiCloudSpendResponse.setVendorName("AWS");
				multiCloudSpendResponse.setCurrency("INR");
			}
			list.add(multiCloudSpendResponse);
		}

		MultiCloudSpendResponse multiCloudSpendResponse = new MultiCloudSpendResponse();
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
		for (AzureSubscriptions subscription : azureSubscriptions) {
			List<CostManagementQueryQuaterlyDocument> costManagementQueryDocument = managementQueryQuaterlyRepository
					.getBySubscriptionId(subscription.getSubscriptionId());
			for (CostManagementQueryQuaterlyDocument costManagementQuDocument : costManagementQueryDocument) {
				multiCloudSpendResponse.setVendorName(Constant.AZURE);
				if (!costManagementQuDocument.getRows().isEmpty()) {
					BigDecimal quarter = new BigDecimal(costManagementQuDocument.getRows().get(0).get(0).toString());
					multiCloudSpendResponse.setTotalAmountSpent(quarter);
					multiCloudSpendResponse.setCurrency(costManagementQuDocument.getRows().get(0).get(2).toString());
					list.add(multiCloudSpendResponse);
				}
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.MULTI_CLOUD_RESPONSE, list),
				Constant.DATA_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse spendingHistory(String renewalType) throws DataValidationException {
		Response resp = new Response();
		List<SpendHistoryResponse> response = new ArrayList<>();
		List<SubscriptionDao> subscription1 = subscriptionRepo.getVendorDetails();
		if (subscription1.isEmpty()) {
			throw new DataValidationException(Constant.CLOUD_DETAILS_NOT_FOUND, null, HttpStatus.NOT_FOUND);
		}
		for (SubscriptionDao subdata : subscription1) {
			List<SubscriptionDao> intData = subscriptionRepo.getSpendBasedOnServiceName(subdata.getSubscriptionname(),
					renewalType);
			SpendHistoryResponse cloudResponse = new SpendHistoryResponse();
			List<SpendHistory> reSpendHistories = new ArrayList<>();
			cloudResponse.setVendorName(subdata.getSubscriptionname());
			cloudResponse.setRenewaltype(renewalType);
			for (SubscriptionDao sc : intData) {
				SpendHistory histroryHistory = new SpendHistory();
				histroryHistory.setTotalAmountSpent(sc.getCount());
				reSpendHistories.add(histroryHistory);
			}
			cloudResponse.setSpendHistory(reSpendHistories);
			response.add(cloudResponse);
			resp.setData(response);
		}
		return new CommonResponse(HttpStatus.OK, resp, Constant.DATA_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getBudgetsByVendor(String category) throws ParseException {
		List<MulticloudBudgetResponse> list = new ArrayList<>();
		List<BudgetsDocument> budgets = multicloudBudgetRepository.findAll();
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
		BigDecimal percentageConsumed = BigDecimal.ZERO;
		if (CloudVendors.azure(category)) {
			for (BudgetsDocument budget : budgets) {
				LocalDate localDate = CommonUtil.dateToLocalDate(budget.getUpdatedOn());
				if (localDate.equals(LocalDate.now())) {
					MulticloudBudgetResponse multicloudBudgetResponse = new MulticloudBudgetResponse();
					multicloudBudgetResponse.setBudgetName(budget.getName());
					for (AzureSubscriptions subscriptions : azureSubscriptions) {
						multicloudBudgetResponse.setScope(subscriptions.getDisplayName());
					}
					if (budget.getCurrentSpendAmount().intValue() != 0) {
						percentageConsumed = (budget.getCurrentSpendAmount()
								.divide(budget.getAmount(), MathContext.DECIMAL32).multiply(new BigDecimal(100)));
					}
					multicloudBudgetResponse.setPercentageConsumed(percentageConsumed);
					multicloudBudgetResponse.setCurrency(budget.getUnit());
					multicloudBudgetResponse.setResetPeriod(budget.getTimeGrain());
					Date start;
					Date end;
					try {
						start = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'").parse(budget.getStartDate());
						end = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'").parse(budget.getEndDate());
						DateFormat formatter = new SimpleDateFormat(Constant.DATE_FORMAT);
						String startDate = formatter.format(start);
						String endDate = formatter.format(end);
						multicloudBudgetResponse.setCreationDate(startDate);
						multicloudBudgetResponse.setExpirationDate(endDate);
					} catch (ParseException e) {
						throw new ParseException(e.getMessage(), 0);
					}
					multicloudBudgetResponse.setBudget(budget.getAmount());
					list.add(multicloudBudgetResponse);
				}

			}
		} else if (CloudVendors.aWS(category)) {
			List<AwsBudgetDocument> awsBudgets = awsBudgetDocumentRepository.findAll();
			for (AwsBudgetDocument budgetDocument : awsBudgets) {
				MulticloudBudgetResponse multicloudBudgetResponse = new MulticloudBudgetResponse();
				multicloudBudgetResponse.setBudgetName(budgetDocument.getBudgetName());
				multicloudBudgetResponse.setScope("MINDITOT");
				if (budgetDocument.getCalculatedSpend().getActualSpend().getAmount().intValue() != 0) {
					percentageConsumed = (budgetDocument.getCalculatedSpend().getActualSpend().getAmount()
							.divide(budgetDocument.getBudgetLimit().getAmount(), MathContext.DECIMAL32)
							.multiply(new BigDecimal(100)));
				}
				multicloudBudgetResponse.setPercentageConsumed(percentageConsumed);
				multicloudBudgetResponse.setCurrency(budgetDocument.getBudgetLimit().getUnit());
				multicloudBudgetResponse.setResetPeriod(budgetDocument.getTimeUnit());
				DateFormat formatter = new SimpleDateFormat(Constant.DATE_FORMAT);
				String start = formatter.format(budgetDocument.getTimePeriod().getStart());
				String end = formatter.format(budgetDocument.getTimePeriod().getEnd());
				multicloudBudgetResponse.setCreationDate(start);
				multicloudBudgetResponse.setExpirationDate(end);
				multicloudBudgetResponse.setBudget(budgetDocument.getBudgetLimit().getAmount());
				list.add(multicloudBudgetResponse);
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.MULTI_CLOUD_RESPONSE, list),
				Constant.DATA_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getSubscriptions() throws DataValidationException {
		List<AzureSubscriptionsResponse> azureSubscriptionsResponses = new ArrayList<>();
		List<AzureSubscriptions> list = azureSubscriptionsRepository.findAll();
		if (list.isEmpty()) {
			throw new DataValidationException("No Subscriptions Found!", null, HttpStatus.NO_CONTENT);
		}
		for (AzureSubscriptions subscriptions : list) {
			AzureSubscriptionsResponse azureSubscriptionsResponse = new AzureSubscriptionsResponse();
			CostManagementUsageByServiceNameDocument costManagementUsageByServiceNameDocument = costManagementUsageByServiceNameRepository
					.findBySubscriptionId(subscriptions.getSubscriptionId());
			if (costManagementUsageByServiceNameDocument != null
					&& costManagementUsageByServiceNameDocument.getRows() != null
					&& !costManagementUsageByServiceNameDocument.getRows().isEmpty()) {
				azureSubscriptionsResponse.setDefaultCurrency(
						costManagementUsageByServiceNameDocument.getRows().get(0).get(3).toString());
			} else {
				azureSubscriptionsResponse.setDefaultCurrency("USD");
			}
			azureSubscriptionsResponse.setSubscriptionId(subscriptions.getSubscriptionId());
			azureSubscriptionsResponse.setSubscriptionName(subscriptions.getDisplayName());
			azureSubscriptionsResponses.add(azureSubscriptionsResponse);
		}
		return new CommonResponse(HttpStatus.OK,
				new Response("AzureSubscriptionsResponse", azureSubscriptionsResponses),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getRecommendations(String category) throws DataValidationException {
		List<AzureRecommendationResponse> list = new ArrayList<>();
		List<AdvisorDocument> advisorDocuments = advisorRecommendationsRepository.findAll();
		if (advisorDocuments.isEmpty()) {
			throw new DataValidationException("No Recommendations", null, HttpStatus.NO_CONTENT);
		}
		BigDecimal savingsAmmount = BigDecimal.valueOf(0.0);
		BigDecimal annualSavingsAmount = BigDecimal.valueOf(0.0);
		if (CloudVendors.azure(category)) {
			for (AdvisorDocument advisorDocument : advisorDocuments) {
				if (advisorDocument.getUpdatedOn() != null) {
					LocalDate localDate = CommonUtil.dateToLocalDate(advisorDocument.getUpdatedOn());
					if (localDate.equals(LocalDate.now())) {
						AzureRecommendationResponse azureRecommendationResponse = new AzureRecommendationResponse();
						azureRecommendationResponse.setCurrency("INR");
						azureRecommendationResponse.setImpact(advisorDocument.getImpact());
						azureRecommendationResponse.setCategroy(advisorDocument.getCategory());
						if (advisorDocument.getResourceId().contains("subscriptions")
								&& advisorDocument.getResourceId().contains("resourcegroups")) {
							azureRecommendationResponse.setResourceId(advisorDocument.getResourceId()
									.substring(advisorDocument.getResourceId().lastIndexOf("/") + 1));
						} else {
							azureRecommendationResponse.setResourceId(null);
						}
						azureRecommendationResponse.setSubscriptionId(advisorDocument.getSubscriptionId());
						azureRecommendationResponse.setResourceName(advisorDocument.getImpactedField()
								.substring(advisorDocument.getImpactedField().indexOf("/") + 1));
						azureRecommendationResponse.setAction(advisorDocument.getShortDescription().getSolution());
						if (advisorDocument.getExtendedProperties().get("savingsAmount") == null) {
							azureRecommendationResponse.setSavingsAmount(savingsAmmount);
						} else {
							azureRecommendationResponse.setSavingsAmount(
									new BigDecimal(advisorDocument.getExtendedProperties().get("savingsAmount")));
						}
						if (advisorDocument.getExtendedProperties().get("annualSavingsAmount") == null) {
							azureRecommendationResponse.setAnnualSavingsAmount(annualSavingsAmount);
						} else {
							azureRecommendationResponse.setAnnualSavingsAmount(
									new BigDecimal(advisorDocument.getExtendedProperties().get("annualSavingsAmount")));
						}
						list.add(azureRecommendationResponse);
					}
				}
			}
		} else if (CloudVendors.aWS(category)) {
			List<AwsRightsizingRecommendationDocument> awsRightsizingRecommendationDocuments = rightsizingRecommendationDocumentRepository
					.findAll();
			if (awsRightsizingRecommendationDocuments.isEmpty()) {
				throw new DataValidationException("No Recommendations", null, HttpStatus.NO_CONTENT);
			}
			for (AwsRightsizingRecommendationDocument awsRightsizingRecommendationDocumentadvisorDocument : awsRightsizingRecommendationDocuments) {
				AzureRecommendationResponse azureRecommendationResponse = new AzureRecommendationResponse();
				azureRecommendationResponse
						.setAction(awsRightsizingRecommendationDocumentadvisorDocument.getAccountId());
				list.add(azureRecommendationResponse);
			}
		} else {
			throw new UnsupportedOperationException("Unsupported cloud vendor: " + category);
		}

		list.sort(Comparator.comparing(AzureRecommendationResponse::getAnnualSavingsAmount).reversed());
		return new CommonResponse(HttpStatus.OK, new Response("RecommendationResponse", list),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getAzureForecastData(String subscriptionId, Date startDate, Date endDate, String category)
			throws DataValidationException, ParseException {
		Response response = new Response();
		List<AzureForecastResponse> azureForecastResponse = new ArrayList<>();
		List<ForecastDataListResponse> forecastDataListResponses = new ArrayList<>();
		if (CloudVendors.azure(category)) {
			List<AzureForecastDocument> forecastDocuments = azureForecastRepository.getBySubscriptionId(subscriptionId);
			ResourceGroupsActualCostDocument actualCostDocuments = resourceGroupsActualCostRepository
					.findBySubscriptionId(subscriptionId);
			List<String> allResourcedName = new ArrayList<>();
			Map<String, String> keyValue = new HashMap<>();
			for (int i = 0; i < actualCostDocuments.getRows().size(); i++) {
				keyValue.put(actualCostDocuments.getRows().get(i).get(2).toString(),
						actualCostDocuments.getRows().get(i).get(0).toString().toLowerCase());
			}
			for (AzureForecastDocument azureForecastDocument : forecastDocuments) {
				for (int i = 0; i < azureForecastDocument.getRows().size(); i++) {
					AzureForecastResponse forecastResponse = new AzureForecastResponse();
					AzureForecastData azureForecastData = new AzureForecastData();
					azureForecastData.setCost(new BigDecimal(azureForecastDocument.getRows().get(i).get(0).toString()));
					Date s = DateParser.parse(azureForecastDocument.getRows().get(i).get(1).toString());
					azureForecastData.setMonth(s);
					forecastResponse.setForecastData(azureForecastData);
					forecastResponse
							.setActualCost(new BigDecimal(azureForecastDocument.getRows().get(i).get(0).toString()));
					forecastResponse.setCurrency(azureForecastDocument.getRows().get(i).get(3).toString());
					forecastResponse.setName(azureForecastDocument.getResourceName());
					allResourcedName.add(azureForecastDocument.getResourceName());
					azureForecastResponse.add(forecastResponse);
				}
			}
			LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			end = end.plusDays(1);
			Date eDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());
			Set<String> distinct = new HashSet<>(allResourcedName);
			LocalDate paramDate = start;
			LocalDate startMonthFirstDay = start.withDayOfMonth(1);
			LocalDate startMonthLastDay = startMonthFirstDay.plusMonths(1).minusDays(1);
			LocalDate startMonthNextMonthFirstDay = startMonthFirstDay.plusMonths(1);

			for (String str : distinct) {
				ForecastDataListResponse forecastDataListResponse = new ForecastDataListResponse();
				List<ForecastMonthAndCost> monthAndCosts = new ArrayList<>();
				List<AzureForecastResponse> initialMonth = azureForecastResponse.stream()
						.filter(p -> p.getForecastData().getMonth().before(eDate) && p.getName().equalsIgnoreCase(str))
						.collect(Collectors.toList());
				BigDecimal acutalCost = BigDecimal.valueOf(0.0);
				if (keyValue.get(str.toLowerCase()) != null) {
					acutalCost = new BigDecimal(keyValue.get(str.toLowerCase()));
				}
				BigDecimal costplus = BigDecimal.valueOf(0.0);
				costplus = costplus.add(acutalCost);
				int i = 0;
				while (!initialMonth.isEmpty() && i == 0) {
					BigDecimal cost = BigDecimal.valueOf(0.0);
					String month = null;
					Date startMonthEnd = Date.from(startMonthLastDay.atStartOfDay(ZoneId.systemDefault()).toInstant());
					Date startParam = Date.from(paramDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
					List<AzureForecastResponse> resourceFiltered = azureForecastResponse.stream()
							.filter(p -> p.getForecastData().getMonth().after(startParam)
									&& p.getForecastData().getMonth().before(startMonthEnd)
									&& p.getName().equalsIgnoreCase(str))
							.collect(Collectors.toList());
					LocalDate loopConEndate = eDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					loopConEndate = loopConEndate.minusDays(2);
					Date loopConDateFormat = Date.from(loopConEndate.atStartOfDay(ZoneId.systemDefault()).toInstant());
					boolean loopcon = loopConDateFormat.after(startParam) && loopConDateFormat.before(startMonthEnd);
					List<AzureForecastResponse> endResources = azureForecastResponse.stream()
							.filter(p -> p.getForecastData().getMonth().after(startParam)
									&& p.getForecastData().getMonth().before(eDate)
									&& p.getName().equalsIgnoreCase(str))
							.collect(Collectors.toList());
					initialMonth = resourceFiltered.stream().filter(
							p -> p.getForecastData().getMonth().before(eDate) && p.getName().equalsIgnoreCase(str))
							.collect(Collectors.toList());
					ForecastMonthAndCost forecastMonthAndCost = new ForecastMonthAndCost();
					if (!initialMonth.isEmpty() && !loopcon) {
						for (AzureForecastResponse loop : resourceFiltered) {
							cost = cost.add(loop.getForecastData().getCost());
							int s = loop.getForecastData().getMonth().getMonth();
							int year = loop.getForecastData().getMonth().getYear() + 1900;
							month = Month.of(s + 1).name() + " " + year;
						}
						costplus = costplus.add(cost);
						if (month != null) {
							forecastMonthAndCost.setCost(costplus);
							forecastMonthAndCost.setMonth(month);
							monthAndCosts.add(forecastMonthAndCost);
						}
					} else {
						for (AzureForecastResponse loop : endResources) {
							cost = cost.add(loop.getForecastData().getCost());
							int s = loop.getForecastData().getMonth().getMonth();
							int year = loop.getForecastData().getMonth().getYear() + 1900;
							month = Month.of(s + 1).name() + " " + year;
						}
						costplus = costplus.add(cost);
						if (month != null) {
							forecastMonthAndCost.setCost(costplus);
							forecastMonthAndCost.setMonth(month);
							monthAndCosts.add(forecastMonthAndCost);
						}
						i++;
					}
					paramDate = startMonthNextMonthFirstDay;
					startMonthLastDay = startMonthNextMonthFirstDay.plusMonths(1).minusDays(1);
					startMonthNextMonthFirstDay = paramDate.plusMonths(1);
				}
				forecastDataListResponse.setName(str);
				forecastDataListResponse.setActualCost(acutalCost);
				forecastDataListResponse.setForecastData(monthAndCosts);
				forecastDataListResponse.setCurrency("INR");
				forecastDataListResponses.add(forecastDataListResponse);
				paramDate = start;
				startMonthFirstDay = start.withDayOfMonth(1);
				startMonthLastDay = startMonthFirstDay.plusMonths(1).minusDays(1);
				startMonthNextMonthFirstDay = startMonthFirstDay.plusMonths(1);
			}
			response.setAction("AzureForecastResponse");
		} else if (category.equalsIgnoreCase("aws")) {
			SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATE_FORMAT);
			List<AwsCostForecastDocument> awsCostForecastDocuments = awsCostForecastDocumentRepository.findAll();
			LocalDate givenStartDateMonthFirstDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate convertGivenEndDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate givenEndDateMonthLastDate = convertGivenEndDate.withDayOfMonth(1).plusMonths(1).minusDays(1);
			List<AwsCostForecastDocument> filteredAwsForecastDocumentList = new ArrayList<>();

			for (AwsCostForecastDocument document : awsCostForecastDocuments) {
				Date documentSatrt = sdf.parse(document.getTimePeriod().getStart());
				Date documentEnd = sdf.parse(document.getTimePeriod().getEnd());
				Date gSDMFD = sdf.parse(givenStartDateMonthFirstDate.withDayOfMonth(1).toString());
				Date gEDMLD = sdf.parse(givenEndDateMonthLastDate.plusDays(1).toString());
				if ((documentSatrt.after(gSDMFD) || documentSatrt.compareTo(gSDMFD) == 0)
						&& (documentEnd.before(gEDMLD) || documentEnd.compareTo(gEDMLD) == 0)) {
					filteredAwsForecastDocumentList.add(document);

				}
			}
			List<ForecastMonthAndCost> forecastMonthAndcostList = new ArrayList<>();
			ForecastDataListResponse forecastDataListResponse = new ForecastDataListResponse();
			for (AwsCostForecastDocument fliteredDoc : filteredAwsForecastDocumentList) {
				ForecastMonthAndCost forecastMonthAndCost = new ForecastMonthAndCost();
				BigDecimal totalCost = new BigDecimal(fliteredDoc.getTotalCost().getAmount());
				forecastMonthAndCost.setCost(totalCost);
				Date dTs = sdf.parse(fliteredDoc.getTimePeriod().getStart());
				int s = dTs.getMonth();
				int year = dTs.getYear() + 1900;
				forecastMonthAndCost.setMonth(Month.of(s + 1).name() + " " + year);
				forecastMonthAndcostList.add(forecastMonthAndCost);
			}

			List<AwsCostAndUsageMonthlyDocument> listDocuments = awsCostAndUsageMonthlyDocumentRepository.findAll();
			BigDecimal awsTotal = BigDecimal.valueOf(0);
			for (AwsCostAndUsageMonthlyDocument document : listDocuments) {
				String amount = document.getTotal().get(Constant.UNBLENDED_COST).getAmount();
				BigDecimal decimal = new BigDecimal(amount);
				awsTotal = awsTotal.add(decimal, MathContext.DECIMAL32);
			}

			forecastDataListResponse.setActualCost(awsTotal);
			forecastDataListResponse.setCurrency("USD");
			forecastDataListResponse.setForecastData(forecastMonthAndcostList);
			forecastDataListResponses.add(forecastDataListResponse);
			response.setAction("AwsForecastResponse");
		}
		return new CommonResponse(HttpStatus.OK, new Response(response.getAction(), forecastDataListResponses),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getResources(String category) throws DataValidationException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		List<AzureResourceListResponse> azurelist = new ArrayList<>();
		List<AwsResourcesResponse> awsList = new ArrayList<>();
		List<AzureSubscriptions> list = azureSubscriptionsRepository.findAll();
		List<ResourcesDocument> resourcesDocuments = resourcesRepository.findAll();
		List<AwsResourcesDocument> awsResourcesDocuments = awsResourcesDocumentRepository.findAll();
		if (CloudVendors.aWS(category)) {
			for (AwsResourcesDocument resourcesDocument : awsResourcesDocuments) {
				AwsResourcesResponse awsResourcesResponse = new AwsResourcesResponse();
				awsResourcesResponse.setAccountId(resourcesDocument.getAccountId());
				awsResourcesResponse.setRegion(resourcesDocument.getRegion());
				if (!resourcesDocument.getResourceARN().equalsIgnoreCase("arn:aws:s3:::mgpocbucket")) {
					String[] str = resourcesDocument.getResourceARN().split(":", 4);
					for (int i = 0; i < str.length; i++) {
						if (i % 2 == 0 && (!str[i].equalsIgnoreCase("arn"))) {
							awsResourcesResponse.setResourceType(
									str[i].substring(0, 1).toUpperCase() + str[i].substring(1).toLowerCase());

						}
					}
					String resourceId = resourcesDocument.getResourceARN()
							.substring(resourcesDocument.getResourceARN().indexOf("/") + 1);
					awsResourcesResponse.setResourceId(resourceId);
				}
				if (resourcesDocument.getTags() != null) {
					awsResourcesResponse.setResourceTags(resourcesDocument.getTags());
				}
				awsList.add(awsResourcesResponse);
				response.setData(awsList);
			}
		} else if (CloudVendors.azure(category)) {
			for (ResourcesDocument document : resourcesDocuments) {
				LocalDate localDate = CommonUtil.dateToLocalDate(document.getUpdatedOn());
				if (localDate.equals(LocalDate.now())) {
					AzureResourceListResponse azureResourceListResponse = new AzureResourceListResponse();
					for (AzureSubscriptions azureSubscriptions : list) {
						azureResourceListResponse.setSubscriptionName(azureSubscriptions.getDisplayName());
					}
					azureResourceListResponse.setResourceName(document.getName());
					azureResourceListResponse.setResourceLocation(document.getLocation());
					azureResourceListResponse
							.setResourceType(document.getType().substring(document.getType().indexOf("/") + 1));
					if (document.getTags() != null) {
						azureResourceListResponse.setResourceTags(document.getTags());
					}
					azurelist.add(azureResourceListResponse);
					response.setData(azurelist);
				}
			}
		}
		response.setAction("azureResourceListResponse");
		commonResponse.setMessage("Details retrieved successfully");
		commonResponse.setResponse(response);
		commonResponse.setStatus(HttpStatus.OK);
		return commonResponse;
	}

	@Override
	public CommonResponse getCostSpendPerYearlyAndMonthly() throws DataValidationException, ParseException {
		AzureTotalSpendResponse spendResponse = new AzureTotalSpendResponse();
		BigDecimal yearSpend = BigDecimal.valueOf(0);
		BigDecimal monthSpend = BigDecimal.valueOf(0);
		List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
		for (AzureSubscriptions subscription : azureSubscriptions) {
			BigDecimal azureYearSpend = BigDecimal.valueOf(0);
			BigDecimal azureMonthSpend = BigDecimal.valueOf(0);
			List<CostManagementQueryMonthlyDocument> costManagementQueryMonthlyDocuments = managementQueryMonthlyRepository
					.findBySubscriptionId(subscription.getSubscriptionId());
			for (CostManagementQueryMonthlyDocument queryDocument : costManagementQueryMonthlyDocuments) {
				Integer i;
				for (i = 0; i < queryDocument.getRows().size(); i++) {
					azureMonthSpend = new BigDecimal(queryDocument.getRows().get(i).get(0).toString());
					spendResponse.setCurrency(queryDocument.getRows().get(i).get(2).toString());
				}
			}
			List<CostManagementQueryYearlyDocument> costManagementQueryYearlyDocuments = managementQueryYearlyRepository
					.getBySubscriptionId(subscription.getSubscriptionId());
			for (CostManagementQueryYearlyDocument queryDocument : costManagementQueryYearlyDocuments) {
				Integer i;
				for (i = 0; i < queryDocument.getRows().size(); i++) {
					azureYearSpend = new BigDecimal(queryDocument.getRows().get(i).get(0).toString());
					spendResponse.setCurrency(queryDocument.getRows().get(i).get(2).toString());
				}
			}
			yearSpend = yearSpend.add(azureYearSpend);
			monthSpend = monthSpend.add(azureMonthSpend);
		}

		List<AwsCostAndUsageMonthlyDocument> listDocuments = awsCostAndUsageMonthlyDocumentRepository.findAll();
		BigDecimal awsYearSpend = BigDecimal.valueOf(0);
		BigDecimal awsMonthSpend = BigDecimal.valueOf(0);
		for (AwsCostAndUsageMonthlyDocument document : listDocuments) {
			SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATE_FORMAT);
			Date documentStartYear = new Date();
			Date documentCurrentMonthStart = new Date();
			Date documentCurrentMonthEnd = new Date();
			int sYear = 0;
			try {
				documentStartYear = sdf.parse(document.getTimePeriod().getStart());
				documentCurrentMonthStart = sdf.parse(document.getTimePeriod().getStart());
				documentCurrentMonthEnd = sdf.parse(document.getTimePeriod().getEnd());
				int startYear = documentStartYear.getYear();
				sYear = startYear + 1900;
			} catch (ParseException e) {
				throw new ParseException(e.getMessage(), 0);
			}
			if (LocalDate.now().getYear() == sYear) {
				String amount = document.getTotal().get(Constant.UNBLENDED_COST).getAmount();
				BigDecimal decimal = new BigDecimal(amount);
				awsYearSpend = awsYearSpend.add(decimal, MathContext.DECIMAL32);
			}
			LocalDate currentMonthStart = LocalDate.now();
			int lengthOfCurrentMonth = currentMonthStart.lengthOfMonth();
			Date startDate = Date
					.from(currentMonthStart.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
			Date endDate = Date.from(currentMonthStart.withDayOfMonth(lengthOfCurrentMonth)
					.atStartOfDay(ZoneId.systemDefault()).toInstant());

			if (documentCurrentMonthStart.compareTo(startDate) == 0 && documentCurrentMonthEnd.before(endDate)) {
				String amount = document.getTotal().get(Constant.UNBLENDED_COST).getAmount();
				BigDecimal decimal = new BigDecimal(amount);
				awsMonthSpend = awsMonthSpend.add(decimal, MathContext.DECIMAL32);
			}
		}

		spendResponse.setThisYearSpend(yearSpend);
		spendResponse.setThisMonthSpend(monthSpend);
		return new CommonResponse(HttpStatus.OK, new Response("CostSpendPerYearlyAndMonthlyResponse", spendResponse),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse optimizeEmailTriggerAzure(OptimizeRequest optimizeRequest)
			throws IOException, TemplateException, MessagingException {
		List<BudgetsDocument> budgetsDocuments = multicloudBudgetRepository.findAll();
		for (BudgetsDocument document : budgetsDocuments) {
			document.getThreshold();
			document.getContactEmails();
		}

		List<String> toAddress = optimizeRequest.getEmailAddress();
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String fromAddress = sendgridDomain;
		String senderName = domainName;
		String subject = Constant.MULTICLOUD_AZURE_EMAIL_SUBJECT;
		Map<String, Object> model = new HashMap<>();
		Template t = config.getTemplate("Azure.html");
		Template t1 = config.getTemplate("recomndation_amount.html");
		String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
		String content1 = FreeMarkerTemplateUtils.processTemplateIntoString(t1, model);
		content = content.replace("{{subscription}}", optimizeRequest.getSubscriptionName());
		content = content.replace("{{recommendationType}}", optimizeRequest.getCategroy());
		if (optimizeRequest.getResourceId() != null) {
			content = content.replace("{{resourceId}}", optimizeRequest.getResourceId());
		} else {
			content = content.replace("{{resourceId}}", "");
		}
		content = content.replace("{{resourceName}}", optimizeRequest.getResourceName());
		content = content.replace("{{action}}", optimizeRequest.getAction());
		content = content.replace("{{impact}}", optimizeRequest.getImpact());
		if (optimizeRequest.getCategroy().equalsIgnoreCase("Cost")) {
			content1 = content1.replace("{{monthlySavings}}",
					("INR ").concat(optimizeRequest.getSavingsAmount().toString()));
			content1 = content1.replace("{{anualSavings}}",
					("INR ").concat(optimizeRequest.getAnnualSavingsAmount().toString()));
		}
		content = content.replace("{{OptimizeAmount}}", content1);
		try {
			helper.setFrom(fromAddress, senderName);
			helper.setTo(toAddress.toArray(new String[0]));
			helper.setSubject(subject);
			helper.setText(content, true);
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedEncodingException(e.getMessage());
		} catch (MessagingException e) {
			throw new MessagingException(e.getMessage());
		}
		mailSender.send(message);
		return new CommonResponse(HttpStatus.OK,
				new Response(Constant.OPTIMIZE_EMAIL_TRIGGER_RESPONSE, new ArrayList<>()),
				"Email sent successfully : " + optimizeRequest.getEmailAddress());
	}

	@Override
	public CommonResponse optimizeEmailTriggerAws(OptimizeRequestAws optimizeRequest)
			throws IOException, TemplateException, MessagingException {
		String toAddress = optimizeRequest.getEmailAddress();
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String fromAddress = sendgridDomain;
		String senderName = domainName;
		String subject = Constant.MULTICLOUD_AWS_EMAIL_SUBJECT;
		Map<String, Object> model = new HashMap<>();
		Template t = config.getTemplate("AWS.html");
		String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
		content = content.replace("{{accountId}}", optimizeRequest.getAccountId());
		content = content.replace("{{instanceId}}", optimizeRequest.getInstanceId());
		content = content.replace("{{resourceType}}", optimizeRequest.getResourceType());
		content = content.replace("{{recommendationType}}", optimizeRequest.getRecommendationType());
		content = content.replace("{{cpu}}", optimizeRequest.getCpu());
		content = content.replace("{{monthlySavings}}", ("$ ").concat(optimizeRequest.getMonthlySavings().toString()));
		content = content.replace("{{annualSavings}}", ("$ ").concat(optimizeRequest.getAnnualSavings().toString()));
		try {
			helper.setFrom(fromAddress, senderName);
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setText(content, true);
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedEncodingException(e.getMessage());
		} catch (MessagingException e) {
			throw new MessagingException(e.getMessage());
		}
		if (budgetMailTrigger) {
			mailSender.send(message);
		}
		return new CommonResponse(HttpStatus.OK,
				new Response(Constant.OPTIMIZE_EMAIL_TRIGGER_RESPONSE, new ArrayList<>()),
				"Email sent successfully : " + optimizeRequest.getEmailAddress());
	}

	@Override
	public CommonResponse budgetEmailTriggerAzure()
			throws IOException, TemplateException, InterruptedException, MessagingException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		List<BudgetsDocument> budgetsDocuments = multicloudBudgetRepository.findAll();
		for (BudgetsDocument document : budgetsDocuments) {
			BigDecimal percentage = document.getCurrentSpendAmount().multiply(BigDecimal.valueOf(100))
					.divide(document.getAmount(), 2, RoundingMode.HALF_UP);
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message);
			String fromAddress = sendgridDomain;
			String senderName = domainName;
			String subject = Constant.BUDGET_EMAIL_SUBJECT;
			Map<String, Object> model = new HashMap<>();
			Template t = config.getTemplate("Budget.html");
			String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
			content = content.replace("{{budgetName}}", document.getName());
			content = content.replace("{{provider}}", "AZURE");
			content = content.replace("{{scope}}", "SaaSPe Microsoft Azure Sponsorship");
			content = content.replace("{{category}}", document.getCategory());
			content = content.replace("{{resetPeriod}}", document.getTimeGrain());
			content = content.replace("{{creationDate}}", document.getStartDate());
			content = content.replace("{{expirationDate}}", document.getEndDate());
			content = content.replace("{{budgetAmount}}",
					document.getUnit().concat(" ").concat(document.getAmount().toString()));
			content = content.replace("{{consumedPercentage}}", percentage.toString());
			content = content.replace("{{consumedAmount}}",
					document.getUnit().concat(" ").concat(document.getCurrentSpendAmount().toString()));
			if (percentage.compareTo(document.getThreshold()) >= 0) {
				for (String email : document.getContactEmails()) {
					String toAddress = email;
					try {
						helper.setFrom(fromAddress, senderName);
						helper.setTo(toAddress);
						helper.setSubject(subject);
						helper.setText(content, true);
					} catch (UnsupportedEncodingException e) {
						throw new UnsupportedEncodingException(e.getMessage());
					} catch (MessagingException e) {
						throw new MessagingException(e.getMessage());
					}
					if (budgetMailTrigger) {
						mailSender.send(message);
					}
					Thread.sleep(30000);
				}

			}
		}
		response.setAction(Constant.OPTIMIZE_EMAIL_TRIGGER_RESPONSE);
		response.setData("");
		commonResponse.setMessage("Emails sent successfully");
		commonResponse.setStatus(HttpStatus.OK);
		commonResponse.setResponse(response);
		return commonResponse;
	}

	@Override
	public CommonResponse getAllSupportedVendors() throws DataValidationException {
		List<ApplicationLogoEntity> cloduVendors = applicationLogoRepository.findCloduVendors();
		List<VendorsResponse> vendors = new ArrayList<>();
		for (ApplicationLogoEntity vendor : cloduVendors) {
			VendorsResponse vendorsResponse = new VendorsResponse();
			vendorsResponse.setId(vendor.getNumber());
			vendorsResponse.setVendorName(vendor.getApplicationName());
			vendorsResponse.setVendorLogo(vendor.getLogoUrl());
			vendors.add(vendorsResponse);
		}
		return new CommonResponse(HttpStatus.OK, new Response("CloudVendorsResponse", vendors),
				"Details modify successfully");
	}

	@Override
	public CommonResponse cloudOnboard(CloudOnboardRequest cloudOnboardRequest, UserLoginDetails profile)
			throws DataValidationException {
		MultiCloudDetails multiCloudDetails = new MultiCloudDetails();
		MultiCloudDetails details = multiCloudDetailRepository.findByProviderName(cloudOnboardRequest.getProvider());
		if (details != null) {
			throw new DataValidationException("Provider Already Integrated " + cloudOnboardRequest.getProvider(), null,
					HttpStatus.BAD_REQUEST);
		}
		String cloudId = null;
		String cloduIdSeqId = "CLOUD_0";
		Integer cloudSequence = sequenceGeneratorRepository.getCloudSequence();
		cloduIdSeqId = cloduIdSeqId.concat(cloudSequence.toString());
		SequenceGenerator cloudupdateSequence = sequenceGeneratorRepository.getById(1);
		cloudupdateSequence.setCloudSequenceId(++cloudSequence);
		cloudId = cloduIdSeqId;
		if (Constant.CLOUD_PROVIDER.stream().noneMatch(cloudOnboardRequest.getProvider()::equals)) {
			throw new DataValidationException("Cloud vendor does not match", "400", HttpStatus.BAD_REQUEST);
		}
		if (CloudVendors.azure(cloudOnboardRequest.getProvider())) {
			List<String> str = Arrays.asList(cloudOnboardRequest.getProvider(), cloudOnboardRequest.getClientId(),
					cloudOnboardRequest.getClientSecret(), cloudOnboardRequest.getTenantId());
			for (String val : str) {
				if (val.isEmpty()) {
					throw new DataValidationException("Please Provide Value for key " + val, null,
							HttpStatus.BAD_REQUEST);
				}
			}
			multiCloudDetails.setProviderName(cloudOnboardRequest.getProvider());
			multiCloudDetails.setClientId(cloudOnboardRequest.getClientId());
			multiCloudDetails.setClientSecret(cloudOnboardRequest.getClientSecret());
			multiCloudDetails.setTenantId(cloudOnboardRequest.getTenantId());
			multiCloudDetails.setCreatedOn(new Date());
			multiCloudDetails.setCreatedBy(profile.getEmailAddress());
			multiCloudDetails.setCloudId(cloudId);
			multiCloudDetailRepository.save(multiCloudDetails);
			sequenceGeneratorRepository.save(cloudupdateSequence);
		}
		if (CloudVendors.aWS(cloudOnboardRequest.getProvider())) {
			List<String> str = Arrays.asList(cloudOnboardRequest.getProvider(), cloudOnboardRequest.getAccessKeyId(),
					cloudOnboardRequest.getSecretAccessKey());
			for (String val : str) {
				if (val.isEmpty()) {
					throw new DataValidationException("Please Provide Value for key " + val, null,
							HttpStatus.BAD_REQUEST);
				}
			}
			multiCloudDetails.setProviderName(cloudOnboardRequest.getProvider());
			multiCloudDetails.setTenantId(cloudOnboardRequest.getTenantId());
			multiCloudDetails.setAccessKeyId(cloudOnboardRequest.getAccessKeyId());
			multiCloudDetails.setSecretAccessKey(cloudOnboardRequest.getSecretAccessKey());
			multiCloudDetails.setCreatedOn(new Date());
			multiCloudDetails.setCreatedBy(profile.getEmailAddress());
			multiCloudDetails.setCloudId(cloudId);
			multiCloudDetailRepository.save(multiCloudDetails);
			sequenceGeneratorRepository.save(cloudupdateSequence);
		}
		return new CommonResponse(HttpStatus.CREATED, new Response("MultiCoudOnbaordResponse", cloudId), "Success");
	}

	@Override
	public CommonResponse getIntegratedClouds() throws DataValidationException {
		Response response = new Response();
		List<MultiCloudDetails> multiCloudDetails = multiCloudDetailRepository.findAll();
		List<IntegratedVendorsResponse> clouds = new ArrayList<>();
		if (!multiCloudDetails.isEmpty()) {
			for (MultiCloudDetails details : multiCloudDetails) {
				IntegratedVendorsResponse integratedVendorsResponse = new IntegratedVendorsResponse();
				if (CloudVendors.azure(details.getProviderName())) {
					integratedVendorsResponse.setClientId(details.getClientId());
					integratedVendorsResponse.setCloudProviderName(details.getProviderName());
					ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
							.findByApplicationName(details.getProviderName());
					integratedVendorsResponse.setCloudProviderLogo(applicationLogoEntity.getLogoUrl());
					integratedVendorsResponse.setOnboardedDate(details.getCreatedOn());
					integratedVendorsResponse.setTenantId(details.getTenantId());
				}
				if (CloudVendors.aWS(details.getProviderName())) {
					integratedVendorsResponse.setClientId(details.getAccessKeyId());
					integratedVendorsResponse.setCloudProviderName(details.getProviderName());
					ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
							.findByApplicationName(details.getProviderName());
					integratedVendorsResponse.setCloudProviderLogo(applicationLogoEntity.getLogoUrl());
					integratedVendorsResponse.setOnboardedDate(details.getCreatedOn());
					integratedVendorsResponse.setTenantId(details.getTenantId());
				}
				clouds.add(integratedVendorsResponse);
				response.setData(clouds);
			}
		} else {
			response.setData(clouds);
		}
		response.setData(clouds);
		response.setAction("IntegratedVendors");
		return new CommonResponse(HttpStatus.OK, response, Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private static String getFirstDateOfQuarter() {
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3);
		DateFormat formatter = new SimpleDateFormat(Constant.DATE_FORMAT);
		return formatter.format(cal.getTime());
	}

	private static String getLastDateOfQuarter() {
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3 + 2);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		DateFormat formatter = new SimpleDateFormat(Constant.DATE_FORMAT);
		return formatter.format(cal.getTime());
	}

	@Override
	public CommonResponse budgetEmailTriggerAws()
			throws IOException, TemplateException, InterruptedException, MessagingException {
		List<AwsBudgetDocument> budgetsDocuments = awsBudgetDocumentRepository.findAll();
		BigDecimal percentageAmt = new BigDecimal(0);
		BigDecimal percentage = new BigDecimal(0);
		for (AwsBudgetDocument document : budgetsDocuments) {
			for (Notification notification : document.getNotifications()) {
				percentageAmt = document.getCalculatedSpend().getActualSpend().getAmount();
				percentage = document.getCalculatedSpend().getActualSpend().getAmount()
						.multiply(BigDecimal.valueOf(100))
						.divide(document.getBudgetLimit().getAmount(), 2, RoundingMode.HALF_UP);
				notification.getNotificationType();
			}
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message);
			String fromAddress = sendgridDomain;
			String senderName = domainName;
			String subject = Constant.BUDGET_EMAIL_SUBJECT;
			Map<String, Object> model = new HashMap<>();
			Template t = config.getTemplate("Budget.html");
			String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
			content = content.replace("{{budgetName}}", document.getBudgetName());
			content = content.replace("{{provider}}", "AWS");
			content = content.replace("{{scope}}", document.getBudgetType());
			content = content.replace("{{resetPeriod}}", document.getTimeUnit());
			content = content.replace("{{creationDate}}", document.getTimePeriod().getStart().toString());
			content = content.replace("{{expirationDate}}", document.getTimePeriod().getEnd().toString());
			content = content.replace("{{budgetAmount}}", document.getBudgetLimit().getUnit().concat(" ")
					.concat(document.getBudgetLimit().getAmount().toString()));
			content = content.replace("{{consumedPercentage}}", percentage.toString());
			content = content.replace("{{consumedAmount}}",
					document.getBudgetLimit().getUnit().concat(" ").concat(percentageAmt.toString())

			);
			if (percentage.compareTo(new BigDecimal(75)) >= 0) {
				for (Subscriber email : document.getSubscriber()) {
					String toAddress = email.getAddress();
					try {
						helper.setFrom(fromAddress, senderName);
						helper.setTo(toAddress);
						helper.setSubject(subject);
						helper.setText(content, true);
					} catch (UnsupportedEncodingException e) {
						throw new UnsupportedEncodingException(e.getMessage());
					} catch (MessagingException e) {
						throw new MessagingException(e.getMessage());
					}
					if (budgetMailTrigger) {
						mailSender.send(message);
					}
				}
				Thread.sleep(20000);
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("AwsOptimizeEmailTriggerResponse", new ArrayList<>()),
				"Emails sent successfully");
	}

	@Override
	public CommonResponse removeCloudVendor(String cloudId) throws DataValidationException {
		if (cloudId == null) {
			throw new DataValidationException("Cloud Id is Null", null, HttpStatus.BAD_REQUEST);
		}
		MultiCloudDetails cloudDetails = multiCloudDetailRepository.findByCloudId(cloudId);
		if (cloudDetails == null) {
			throw new DataValidationException("Invalid Cloud Id", null, HttpStatus.BAD_REQUEST);
		}
		if (CloudVendors.azure(cloudDetails.getProviderName())) {
			MultiCloudDetails multicloudOptional = multiCloudDetailRepository.findByCloudId(cloudId);
			multiCloudDetailRepository.delete(multicloudOptional);
			List<AzureSubscriptions> azureSubscriptions = azureSubscriptionsRepository.findAll();
			for (AzureSubscriptions subscription : azureSubscriptions) {
				List<AdvisorDocument> advisorDocuments = advisorRecommendationsRepository
						.findBySubscriptionId(subscription.getSubscriptionId());
				for (AdvisorDocument advisorDocument : advisorDocuments) {
					advisorRecommendationsRepository.delete(advisorDocument);
				}
				List<AzureForecastDocument> azureForecastDocuments = azureForecastRepository
						.getBySubscriptionId(subscription.getSubscriptionId());
				for (AzureForecastDocument forecastDocument : azureForecastDocuments) {
					azureForecastRepository.delete(forecastDocument);
				}
				List<BudgetsDocument> budgetsDocuments = budgetRepository
						.findBySubscriptionId(subscription.getSubscriptionId());
				for (BudgetsDocument budgetsDocument : budgetsDocuments) {
					budgetRepository.delete(budgetsDocument);
				}
				List<CostManagementQueryMonthlyDocument> costManagementQueryMonthlyDocuments = managementQueryMonthlyRepository
						.getBySubscriptionId(subscription.getSubscriptionId());
				for (CostManagementQueryMonthlyDocument costManagementQueryDocument : costManagementQueryMonthlyDocuments) {
					managementQueryMonthlyRepository.delete(costManagementQueryDocument);
				}
				List<CostManagementQueryQuaterlyDocument> costManagementQueryQuaterlyDocuments = managementQueryQuaterlyRepository
						.getBySubscriptionId(subscription.getSubscriptionId());
				for (CostManagementQueryQuaterlyDocument costManagementQueryDocument : costManagementQueryQuaterlyDocuments) {
					managementQueryQuaterlyRepository.delete(costManagementQueryDocument);
				}
				List<CostManagementQueryYearlyDocument> costManagementQueryYearlyDocuments = managementQueryYearlyRepository
						.getBySubscriptionId(subscription.getSubscriptionId());
				for (CostManagementQueryYearlyDocument costManagementQueryDocument : costManagementQueryYearlyDocuments) {
					managementQueryYearlyRepository.delete(costManagementQueryDocument);
				}
				List<CostManagementUsageByServiceNameDocument> costManagementUsageByServiceNameDocuments = costManagementUsageByServiceNameRepository
						.getBySubscriptionId(subscription.getSubscriptionId());
				for (CostManagementUsageByServiceNameDocument costManagementUsageByServiceNameDocument : costManagementUsageByServiceNameDocuments) {
					costManagementUsageByServiceNameRepository.delete(costManagementUsageByServiceNameDocument);
				}
				List<ResourceGroupDocument> resourceGroupDocuments = resourceGroupRepository
						.getResourceBySubscriptionId(subscription.getSubscriptionId());
				for (ResourceGroupDocument resourceGroupDocument : resourceGroupDocuments) {
					resourceGroupRepository.delete(resourceGroupDocument);
				}
				List<ResourceGroupsActualCostDocument> resourceGroupsActualCostDocuments = resourceGroupsActualCostRepository
						.getBySubscriptionId(subscription.getSubscriptionId());
				for (ResourceGroupsActualCostDocument resourceGroupsActualCostDocument : resourceGroupsActualCostDocuments) {
					resourceGroupsActualCostRepository.delete(resourceGroupsActualCostDocument);
				}
				List<ResourcesDocument> resourcesDocuments = resourcesRepository
						.findBySubscriptionId(subscription.getSubscriptionId());
				for (ResourcesDocument resourcesDocument : resourcesDocuments) {
					resourcesRepository.delete(resourcesDocument);
				}
			}
		}
		if (CloudVendors.aWS(cloudDetails.getProviderName())) {
			MultiCloudDetails multicloudOptional = multiCloudDetailRepository.findByCloudId(cloudId);
			List<AwsBudgetDocument> awsBudgetDocuments = awsBudgetDocumentRepository
					.getByBudgetsAccountId(multicloudOptional.getTenantId());
			for (AwsBudgetDocument awsBudgetDocument : awsBudgetDocuments) {
				awsBudgetDocumentRepository.delete(awsBudgetDocument);
			}
			List<AwsCostAndUsageDocument> awsCostAndUsageDocuments = awsCostAndUsageDocumentRepository
					.getRecordsByAccountId(multicloudOptional.getTenantId());
			for (AwsCostAndUsageDocument awsCostAndUsageDocument : awsCostAndUsageDocuments) {
				awsCostAndUsageDocumentRepository.delete(awsCostAndUsageDocument);
			}
			List<AwsCostAndUsageMonthlyDocument> andUsageMonthlyDocuments = awsCostAndUsageMonthlyDocumentRepository
					.getByAccountId(multicloudOptional.getTenantId());
			for (AwsCostAndUsageMonthlyDocument andUsageMonthlyDocument : andUsageMonthlyDocuments) {
				awsCostAndUsageMonthlyDocumentRepository.delete(andUsageMonthlyDocument);
			}
			List<AwsCostForecastDocument> awsCostForecastDocuments = awsCostForecastDocumentRepository
					.getByAccountId(multicloudOptional.getTenantId());
			for (AwsCostForecastDocument awsCostForecastDocument : awsCostForecastDocuments) {
				awsCostForecastDocumentRepository.delete(awsCostForecastDocument);
			}
			List<AwsResourcesDocument> awsResourcesDocuments = awsResourcesDocumentRepository
					.getresoucresByResourcesGroup(multicloudOptional.getTenantId());
			for (AwsResourcesDocument awsResourcesDocument : awsResourcesDocuments) {
				awsResourcesDocumentRepository.delete(awsResourcesDocument);
			}
			List<AwsRightsizingRecommendationDocument> awsRightsizingRecommendationDocuments = rightsizingRecommendationDocumentRepository
					.getByAccountId(multicloudOptional.getTenantId());
			for (AwsRightsizingRecommendationDocument awsRightsizingRecommendationDocument : awsRightsizingRecommendationDocuments) {
				rightsizingRecommendationDocumentRepository.delete(awsRightsizingRecommendationDocument);
			}
			multiCloudDetailRepository.delete(multicloudOptional);
		}
		return new CommonResponse(HttpStatus.OK, new Response("cloudDeleteResponse", ""), cloudId + " removed!");
	}

	@Override
	public CommonResponse getCreateBudget(BudgetRequest budgetRequest) throws DataValidationException {
		CommonResponse response = new CommonResponse();
		List<String> errors = new ArrayList<>();
		List<String> budgetPeriods = Constant.AZURE_BUDGET_PERIOD;
		List<String> thersholdType = Constant.AZURE_BUDGET_THERSHOLD_TYPE;
		BudgetsDocument document = budgetRepository.findByBudgetName(budgetRequest.getBudgetName());
		if (document != null) {
			errors.add("Budget with name " + budgetRequest.getBudgetName() + " already exists");
			throw new DataValidationException(
					"Budget with name " + budgetRequest.getBudgetName() + " already exists,Please create new budget",
					null, HttpStatus.BAD_REQUEST);
		}
		String[] createDate = budgetRequest.getCreationDate().split("-");
		if (!createDate[2].equals("01")) {
			errors.add("Creation date sholud be the 1st date of month");
		}
		if (Boolean.FALSE.equals(checkBigdecimalString(budgetRequest.getBudgetAmount().getValue()))) {
			errors.add("Please provide valid BudgetAmount value");
		}
		for (BudgetAlertDetailRequest alertDetails : budgetRequest.getAlertDetails()) {
			if (Boolean.FALSE.equals(checkBigdecimalString(alertDetails.getThreshold()))) {
				errors.add("Please provide valid Threshold value");
			}
			if (!thersholdType.contains(alertDetails.getType())) {
				errors.add("Please provide valid Thershold Type value");
			}
		}
		if (!budgetPeriods.contains(budgetRequest.getResetPeriod())) {
			errors.add("Please provide valid ResetPeriod value");
		}
		if (document == null) {
			checkBigdecimalString(budgetRequest.getBudgetAmount().getValue());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<BudgetRequest> requestEntity = new HttpEntity<>(budgetRequest, headers);
			ResponseEntity<CommonResponse> responseEntity = restTemplate.exchange(azureHost + createBudgetUrl,
					HttpMethod.POST, requestEntity, CommonResponse.class);
			if (errors.isEmpty()) {
				CommonResponse resp = responseEntity.getBody();
				Response res = new Response();
				if (resp != null) {
					res = resp.getResponse();
					resp.setResponse(res);
				}
				res.setData(new ArrayList<>());
				return resp;
			} else {
				response.setMessage("Budget Creation Failed");
				response.setResponse(new Response("CreateBudgetResponse", errors));
				response.setStatus(HttpStatus.BAD_REQUEST);
			}
		}
		return response;
	}

	private Boolean checkBigdecimalString(String value) {
		boolean flag = true;
		try {
			new BigDecimal(value);
		} catch (NumberFormatException e) {
			flag = false;
		}
		return flag;
	}
	
	@Override
	public CommonResponse getMonthlySpendingHistory() throws DataValidationException {
	   
	        List<AzureSpendingHistoryDocument> azureSpendingHistoryDocuments = azureSpendHistoryRepository.findAll();
	     
	        MonthlySpendingResponse monthlySpendingResponse = new MonthlySpendingResponse();
            List<Map<String, Object>> dataList = new ArrayList<>();

	        for (AzureSpendingHistoryDocument document : azureSpendingHistoryDocuments) {
	        	  
	        	Map<String, Object> monthlyData = new LinkedHashMap<>();
	            monthlyData.put("month", document.getMonth() + " " + document.getYear());
	            
	            CloudProvider cloudProvider = new CloudProvider();
	            cloudProvider.setName("Azure");
	            List<CostInfo> costInfos = new ArrayList<>();

	            CostInfo costInfoINR = new CostInfo();
	            costInfoINR.setValueInINR(document.getTotalCostINR());
	            costInfoINR.setCurrency("INR");

	            CostInfo costInfoUSD = new CostInfo();
	            costInfoUSD.setValueInUSD(document.getTotalCostUSD());
	            costInfoUSD.setCurrency("USD");

	            costInfos.add(costInfoINR);
	            costInfos.add(costInfoUSD);

	            Map<String, String> costMap = new LinkedHashMap<>();
	            for (CostInfo costInfo : costInfos) {
	                costMap.put(costInfo.getCurrency(), costInfo.getValue().toString());
	            }

	            Map<String, Object> serviceMap = new LinkedHashMap<>();
	            serviceMap.put("name", "Azure");
	            serviceMap.put("cost", costMap);

	            monthlyData.put("CloudProvider", Collections.singletonList(serviceMap));
	            dataList.add(monthlyData);

	            monthlySpendingResponse.setMonthlySpendingHistory(dataList);

	        }

	        return new CommonResponse(HttpStatus.OK,
	                new Response("AzureMonthlySpendingHistory", monthlySpendingResponse),
	                "Azure Monthly Spending History details retrieved successfully");
	    } 


}
