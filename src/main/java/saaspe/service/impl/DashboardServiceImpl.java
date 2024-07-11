package saaspe.service.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import saaspe.constant.Constant;
import saaspe.constant.ContractType;
import saaspe.dto.TopAppsBySpendDAO;
import saaspe.entity.ApplicationCategoryMaster;
import saaspe.entity.ApplicationContractDetails;
import saaspe.entity.ApplicationDetails;
import saaspe.entity.ApplicationLicenseDetails;
import saaspe.entity.ApplicationLogoEntity;
import saaspe.entity.ApplicationOnboarding;
import saaspe.entity.ApplicationSubscriptionDetails;
import saaspe.entity.ContractOnboardingDetails;
import saaspe.entity.DepartmentDetails;
import saaspe.entity.DepartmentOnboarding;
import saaspe.entity.InvoiceDetails;
import saaspe.entity.ProjectDetails;
import saaspe.entity.ProjectOnboardingDetails;
import saaspe.entity.UserDetails;
import saaspe.entity.UserOnboarding;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.ContractDashBoardResponse;
import saaspe.model.DashBoardDepartmentExpenseResponse;
import saaspe.model.DashBoardInvoiceResponse;
import saaspe.model.DashboardDepartmentBudgetResponse;
import saaspe.model.DashboardInvoicesResponse;
import saaspe.model.DashboardSpendHistoryResponse;
import saaspe.model.DashboardTopCardAnalysticsResponse;
import saaspe.model.DashboardTopCardResponse;
import saaspe.model.DashboardUsageTrendResponse;
import saaspe.model.DashboardViewResponse;
import saaspe.model.DepartmentBudgetResponse;
import saaspe.model.RequestTrackingListViewResponse;
import saaspe.model.Response;
import saaspe.model.SimilarApplicationResponse;
import saaspe.model.SimilarApplicationsRequest;
import saaspe.model.TopAppsBySpend;
import saaspe.model.TopAppsBySpendResponse;
import saaspe.model.TopAppsRecentResponse;
import saaspe.model.UpcomingRenewalsResponse;
import saaspe.model.getTopAppByMetricsResponse;
import saaspe.model.getTopAppsByMetricsResponse;
import saaspe.repository.ApplicationCategoryMasterRepository;
import saaspe.repository.ApplicationContractDetailsRepository;
import saaspe.repository.ApplicationDetailsRepository;
import saaspe.repository.ApplicationLicenseDetailsRepository;
import saaspe.repository.ApplicationLogoRepository;
import saaspe.repository.ApplicationOnboardingRepository;
import saaspe.repository.ApplicationSubscriptionDetailsRepository;
import saaspe.repository.ContractsOnboardingRespository;
import saaspe.repository.DepartmentOnboardingRepository;
import saaspe.repository.DepartmentRepository;
import saaspe.repository.InvoiceRepository;
import saaspe.repository.PaymentDetailsRepository;
import saaspe.repository.ProjectDetailsRepository;
import saaspe.repository.ProjectOnboardingDetailsRepository;
import saaspe.repository.TenantRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.repository.UserOnboardingDetailsRepository;
import saaspe.service.DashboardService;
import saaspe.utils.CommonUtil;
import saaspe.utils.CustomComparator;

@Service
public class DashboardServiceImpl implements DashboardService {

	@Autowired
	ApplicationDetailsRepository applicationDetailsRepository;

	@Autowired
	ApplicationContractDetailsRepository applicationContractDetailsRepository;

	@Autowired
	ApplicationLicenseDetailsRepository applicationLicenseDetailsRepository;

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	WebClient webClient;

	@Autowired
	ApplicationCategoryMasterRepository applicationCategoryMasterRepository;

	@Autowired
	TenantRepository tenantRepository;

	@Autowired
	UserDetailsRepository userDetailsRepository;

	@Autowired
	InvoiceRepository invoiceRepository;

	@Autowired
	PaymentDetailsRepository paymentDetailsRepository;

	@Autowired
	private ApplicationDetailsRepository applicationDetailsRepo;

	@Autowired
	private ApplicationSubscriptionDetailsRepository applicationSubscriptionDetailsRepo;

	@Autowired
	private ApplicationOnboardingRepository applicationOnboardingRepository;

	@Autowired
	private UserOnboardingDetailsRepository userOnboardingDetailsRepository;

	@Autowired
	private DepartmentOnboardingRepository departmentOnboardingRepository;

	@Autowired
	private ApplicationLogoRepository applicationLogoRepository;

	@Autowired
	private ProjectOnboardingDetailsRepository projectOnboardingDetailsRepository;

	@Autowired
	private ProjectDetailsRepository projectDetailsRepository;

	@Autowired
	private ContractsOnboardingRespository contractsOnboardingRespository;

	@Override
	public CommonResponse getTopAppsBySpend() {
		List<TopAppsBySpendDAO> topAppsBySpend = applicationLicenseDetailsRepository.getTopAppsBySpend();
		TopAppsBySpendResponse appsBySpendResponses = new TopAppsBySpendResponse();
		List<TopAppsBySpend> appsBySpends = new ArrayList<>();
		for (TopAppsBySpendDAO applicationLicenseDetails : topAppsBySpend) {
			TopAppsBySpend appsBySpend = new TopAppsBySpend();
			ApplicationDetails details = applicationDetailsRepository
					.findByApplicationId(applicationLicenseDetails.getAppId());
			appsBySpend.setApplicatonId(details.getApplicationId());
			appsBySpend.setApplicaitonName(details.getApplicationName());
			appsBySpend.setLogUrl(details.getLogoUrl());
			appsBySpend.setCost(applicationLicenseDetails.getCost());
			appsBySpend.setCurrency(applicationLicenseDetails.getCurrency());
			appsBySpends.add(appsBySpend);
		}
		appsBySpendResponses.setTopAppsBySpendRespone(appsBySpends);
		return new CommonResponse(HttpStatus.OK, new Response("TopAppsBySpendResponse", appsBySpendResponses),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getTopAppsByMetrics() {
		getTopAppsByMetricsResponse getTopAppsByMetricsResponse = new getTopAppsByMetricsResponse();
		List<getTopAppByMetricsResponse> list = new ArrayList<>();
		List<ApplicationDetails> details = applicationDetailsRepository.findRemainingApplications();
		for (ApplicationDetails dao : details) {
			getTopAppByMetricsResponse appsByMetricsResponse = new getTopAppByMetricsResponse();
			ApplicationDetails applicationDetails = applicationDetailsRepository
					.findByApplicationId(dao.getApplicationId());
			List<ApplicationContractDetails> applicationContractDetails = applicationContractDetailsRepository
					.findActiveContracts(dao.getApplicationId());
			BigDecimal totalCost = BigDecimal.valueOf(0);
			BigDecimal adminCost = BigDecimal.valueOf(0);
			String currency = "USD";
			String applicationName = null;
			String applicationId = null;
			String logoUrl = null;
			Integer userCount = 0;
			List<String> userList = new ArrayList<>();
			for (UserDetails userDetails : applicationDetails.getUserDetails()) {
				if (userDetails.getEndDate() == null) {
					userList.add(userDetails.getUserId());
				}
			}
			for (ApplicationContractDetails contracts : applicationContractDetails) {
				for (ApplicationLicenseDetails license : contracts.getLicenseDetails()) {
					if (license != null) {
						totalCost = totalCost.add(license.getTotalCost());
						adminCost = adminCost.add(license.getConvertedCost());
						currency = license.getCurrency();
					}
				}
			}
			appsByMetricsResponse.setCurrencyCode(currency);
			if (applicationDetails != null) {
				userCount = userList.size();
				applicationName = applicationDetails.getApplicationName();
				applicationId = applicationDetails.getApplicationId();
				logoUrl = applicationDetails.getLogoUrl();
			}
			appsByMetricsResponse.setApplicationName(applicationName);
			appsByMetricsResponse.setApplicationId(applicationId);
			appsByMetricsResponse.setLogoUrl(logoUrl);
			appsByMetricsResponse.setUserCount(userCount);
			appsByMetricsResponse.setCost(totalCost);
			appsByMetricsResponse.setAdminCost(adminCost);
			ProjectDetails project = projectDetailsRepository.findByProjectName(dao.getProjectName());
			appsByMetricsResponse.setProjectCode(project.getProjectCode());
			appsByMetricsResponse.setProjectName(dao.getProjectName());
			appsByMetricsResponse.setProjectId(project.getProjectId());
			if ((applicationDetails.getActiveContracts() != null) 
				&& (applicationDetails.getActiveContracts() > 0)) {
					list.add(appsByMetricsResponse);	
			}
		}
		list.sort(Comparator.comparing(getTopAppByMetricsResponse::getUserCount)
				.thenComparing(getTopAppByMetricsResponse::getCost).reversed());
		if (list.size() <= 10) {
			list = list.subList(0, list.size());
		} else {
			list = list.subList(0, 10);
		}
		for (int i = 0; i < list.size() - 1; i++) {
			for (int j = i + 1; j < list.size(); j++) {
				if (list.get(i).getApplicationName().equalsIgnoreCase(list.get(j).getApplicationName())
						&& list.get(i).getUserCount().equals(list.get(j).getUserCount())
						&& (list.get(i).getCost().compareTo(list.get(j).getCost()) < 0)) {
					Collections.swap(list, i, j);

				}
			}
		}
		getTopAppsByMetricsResponse.setGetTopAppResponse(list);
		return new CommonResponse(HttpStatus.OK, new Response("TopAppsByMetrics", getTopAppsByMetricsResponse),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getInvoices() {
		List<InvoiceDetails> invoices = invoiceRepository.getInvoceDetailsListview();
		DashboardInvoicesResponse dashboardInvoicesResponse = new DashboardInvoicesResponse();
		DashBoardInvoiceResponse invoiceResponse = new DashBoardInvoiceResponse();
		Integer paidInvoice = 0;
		Integer peningInvoice = 0;
		BigDecimal totalInvoiceCost = BigDecimal.valueOf(0.0);
		BigDecimal paidInvoiceCost = BigDecimal.valueOf(0.0);
		BigDecimal pendingInvoiceCost = BigDecimal.valueOf(0.0);
		for (InvoiceDetails details : invoices) {
			totalInvoiceCost = totalInvoiceCost.add(details.getInvoiceAmount());
			if (details.getAmountDue() == null || details.getAmountDue().intValue() == 0) {
				paidInvoiceCost = paidInvoiceCost.add(details.getInvoiceAmount());
				paidInvoice = paidInvoice + 1;
			} else {
				pendingInvoiceCost = pendingInvoiceCost.add(details.getInvoiceAmount());
				peningInvoice = peningInvoice + 1;
			}
		}
		invoiceResponse.setTotalInvoices(invoices.size());
		invoiceResponse.setTotalInvoicesCost(totalInvoiceCost);
		invoiceResponse.setPaidInvoices(paidInvoice);
		invoiceResponse.setPendingInvoicesCost(pendingInvoiceCost);
		invoiceResponse.setPaidInvoicesCost(paidInvoiceCost);
		invoiceResponse.setPendingInvoices(peningInvoice);
		dashboardInvoicesResponse.setGetInvoicesResponse(invoiceResponse);
		return new CommonResponse(HttpStatus.OK, new Response("GetInvoicesResponse", dashboardInvoicesResponse),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getDashboardView() {
		BigDecimal totalCost = BigDecimal.valueOf(0);
		BigDecimal adminCost = BigDecimal.valueOf(0);
		LocalDate firstdayOfYear = LocalDate.now().withDayOfYear(1);
		LocalDate lastdayOfYear = LocalDate.now();
		DashboardViewResponse response = new DashboardViewResponse();
		List<ApplicationDetails> appList = applicationDetailsRepo.findRemainingApplications();
		List<ApplicationSubscriptionDetails> subList = applicationSubscriptionDetailsRepo.findRemainingubscriptions();
		List<ApplicationContractDetails> applicationContractDetails = applicationContractDetailsRepository.findAll();
		for (ApplicationContractDetails contractDetails : applicationContractDetails) {
			ApplicationDetails appDetail = applicationDetailsRepository.findByAppId(contractDetails.getApplicationId().getApplicationId());
			if(appDetail.getEndDate() == null || appDetail.getEndDate().after(contractDetails.getContractStartDate()) || appDetail.getEndDate().compareTo(contractDetails.getContractStartDate())==0) {
				if (contractDetails.getContractStatus().equalsIgnoreCase(Constant.ACTIVE)
						|| contractDetails.getContractStatus().equalsIgnoreCase(Constant.EXPIRED)) {
					for (ApplicationLicenseDetails licenseDetails : contractDetails.getLicenseDetails()) {
						totalCost = totalCost.add(licenseDetails.getTotalCost(), MathContext.DECIMAL32);
						adminCost = adminCost.add(licenseDetails.getConvertedCost(), MathContext.DECIMAL32);

					}
				}
			}
		}
		Date currentDate = CommonUtil.convertLocalDatetoDate(lastdayOfYear);
		List<ApplicationContractDetails> renewalsSize = applicationContractDetailsRepository
				.findContractsAndRenewals(currentDate);
		response.setRenewals(renewalsSize.size());
		response.setTotalSpend(totalCost);
		response.setAdminCost(adminCost);
		response.setTotalSpendYTD(getTotalCostYTD(firstdayOfYear, lastdayOfYear).get("total"));
		response.setAdminCostYTD(getTotalCostYTD(firstdayOfYear, lastdayOfYear).get("admin"));
		response.setApplications(appList.size());
		response.setSubscriptions(subList.size());
		return new CommonResponse(HttpStatus.OK, new Response("DashboardViewResponse", response),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private Map<String, BigDecimal> getTotalCostYTD(LocalDate firstDayOfQuarter, LocalDate lastDayOfQuarter) {
		Map<String, BigDecimal> spend = new HashMap<>();
		BigDecimal totalSpend = new BigDecimal(0);
		BigDecimal adminSpend = new BigDecimal(0);
		List<ApplicationContractDetails> applicationContractDetails = applicationContractDetailsRepository
				.findActiveExpiredContracts();
		for (ApplicationContractDetails contract : applicationContractDetails) {
			ApplicationDetails appDetail = applicationDetailsRepository.findByAppId(contract.getApplicationId().getApplicationId());
			if(appDetail.getEndDate() == null || appDetail.getEndDate().after(contract.getContractStartDate()) || appDetail.getEndDate().compareTo(contract.getContractStartDate())==0) {

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
					
					if (contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) &&  (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)))) {
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
												|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
														CommonUtil.convertLocalDatetoDate(localContractEnd)) == 0) {
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
												|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
														CommonUtil.convertLocalDatetoDate(localContractEnd)) == 0) {
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
												|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
														CommonUtil.convertLocalDatetoDate(localContractEnd)) == 0) {
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
												|| CommonUtil.convertLocalDatetoDate(plusYears).compareTo(
														CommonUtil.convertLocalDatetoDate(localContractEnd)) == 0) {
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
		}
		spend.put("total", totalSpend);
		spend.put("admin", adminSpend);
		return spend;
	}

	@Override
	public CommonResponse getTopAppsRecentlyAdded() {
		List<ApplicationDetails> list = applicationDetailsRepository.getDataDesc();
		List<TopAppsRecentResponse> appsRecentResponses = new ArrayList<>();
		for (ApplicationDetails details : list) {
			if (appsRecentResponses.stream()
					.filter(s -> s.getApplicationName().equalsIgnoreCase(details.getApplicationName()))
					.collect(Collectors.toList()).isEmpty()) {
				TopAppsRecentResponse appsRecentResponse = new TopAppsRecentResponse();
				appsRecentResponse.setApplicationCreatedDate(details.getCreatedOn());
				appsRecentResponse.setApplicationID(details.getApplicationId());
				appsRecentResponse.setApplicationLogo(details.getLogoUrl());
				appsRecentResponse.setApplicationName(details.getApplicationName());
				appsRecentResponse.setApplicationShortDescription(details.getApplicationDescription());
				appsRecentResponses.add(appsRecentResponse);
			}
		}
		List<TopAppsRecentResponse> listoftenapplications = appsRecentResponses.stream().limit(10)
				.collect(Collectors.toList());
		return new CommonResponse(HttpStatus.OK, new Response("TopAppsRecentlyAdded", listoftenapplications),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getContractDetails() {
		ContractDashBoardResponse boardResponse = new ContractDashBoardResponse();
		ZoneId defaultZoneId = ZoneId.systemDefault();
		LocalDate date = LocalDate.now();
		LocalDate sixMonthFromToday = date.plusMonths(6);
		Date oneMonthDate = Date.from(sixMonthFromToday.atStartOfDay(defaultZoneId).toInstant());
		BigDecimal totalContractSpend = BigDecimal.valueOf(0);
		BigDecimal adminSpend = BigDecimal.valueOf(0);
		String currency = "USD";
		List<ApplicationContractDetails> listOfActiveContracts = applicationContractDetailsRepository
				.findActiveContracts();
		List<ApplicationContractDetails> listOfExpired = applicationContractDetailsRepository
				.getByContractEndDate(new Date(), oneMonthDate);
		for (ApplicationContractDetails contractDetails : listOfActiveContracts) {
			if (contractDetails.getApplicationId() != null) {
				for (ApplicationLicenseDetails license : contractDetails.getLicenseDetails()) {
					if (license != null) {
						totalContractSpend = totalContractSpend.add(license.getTotalCost());
						adminSpend = adminSpend.add(license.getConvertedCost());
						currency = license.getCurrency();
					}
				}
			}
		}
		boardResponse.setCurrency(currency);
		boardResponse.setTotalExpiringContracts(listOfExpired.size());
		boardResponse.setTotalActiveContracts(listOfActiveContracts.size());
		boardResponse.setTotalContractsSpend(totalContractSpend);
		boardResponse.setTotalContractSpendAdminCost(adminSpend);
		return new CommonResponse(HttpStatus.OK, new Response("ContractDetails", boardResponse),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getUpcomingRenewals() {
		ZoneId defaultZoneId = ZoneId.systemDefault();
		LocalDate today = LocalDate.now();
		LocalDate afterSixMonthDate = today.plusMonths(6);
		Date to = Date.from(today.atStartOfDay(defaultZoneId).toInstant());
		Date sixMonth = Date.from(afterSixMonthDate.atStartOfDay(defaultZoneId).toInstant());
		List<ApplicationContractDetails> contractDetails = applicationContractDetailsRepository
				.getByContractRenewalDateAndContratEndDate(to, sixMonth);
		List<UpcomingRenewalsResponse> list = new ArrayList<>();
		if (contractDetails != null) {
			for (ApplicationContractDetails details : contractDetails) {
				UpcomingRenewalsResponse renewalsResponse = new UpcomingRenewalsResponse();
				renewalsResponse.setApplicationID(details.getApplicationId().getApplicationId());
				renewalsResponse.setApplicationLogo(details.getApplicationId().getLogoUrl());
				renewalsResponse.setApplicationName(details.getApplicationId().getApplicationName());
				renewalsResponse.setContractID(details.getContractId());
				if (details.getApplicationId() != null) {
					BigDecimal costPerLicense = BigDecimal.valueOf(0);
					BigDecimal adminCostPerLicense = BigDecimal.valueOf(0);
					for (ApplicationLicenseDetails license : details.getLicenseDetails()) {
						if (license != null) {
							costPerLicense = costPerLicense.add(license.getTotalCost());
							adminCostPerLicense = adminCostPerLicense.add(license.getConvertedCost());
						}
					}
					renewalsResponse.setRenewalAmount(costPerLicense);
					renewalsResponse.setAdminCost(adminCostPerLicense);
				}
				renewalsResponse.setCurrency(details.getContractCurrency());
				if (Boolean.TRUE.equals(details.getAutoRenew())) {
					renewalsResponse.setRenewalDate(details.getRenewalDate());
				} else {
					renewalsResponse.setRenewalDate(details.getContractEndDate());
				}
				list.add(renewalsResponse);
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("UpcomingRenewals", list),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getSimilarApplications(SimilarApplicationsRequest similarapplicationsrequest) {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		List<SimilarApplicationResponse> list = new ArrayList<>();
		ApplicationCategoryMaster categoryMaster = applicationCategoryMasterRepository
				.findByCategoryName(similarapplicationsrequest.getCategoryName());
		List<ApplicationDetails> applicationDetails = applicationDetailsRepo
				.findByCategoryId(categoryMaster.getCategoryId());
		for (ApplicationDetails details : applicationDetails) {
			if (list.stream().filter(p -> p.getApplicationName().equalsIgnoreCase(details.getApplicationName()))
					.collect(Collectors.toList()).isEmpty()) {
				SimilarApplicationResponse similarApplicationResponse = new SimilarApplicationResponse();
				similarApplicationResponse.setApplicationName(details.getApplicationName());
				similarApplicationResponse.setApplicationLogo(details.getLogoUrl());
				list.add(similarApplicationResponse);
			}
		}
		response.setData(list);
		response.setAction("SimilarApplicationResponse");
		commonResponse.setStatus(HttpStatus.OK);
		commonResponse.setMessage(Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
		commonResponse.setResponse(response);
		return commonResponse;
	}

	@Override
	public CommonResponse getRequestTracking(String category) throws DataValidationException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		List<RequestTrackingListViewResponse> list = new ArrayList<>();

		List<RequestTrackingListViewResponse> duplicateChildList = new ArrayList<>();
		List<RequestTrackingListViewResponse> duplicateParentList = new ArrayList<>();

		List<ApplicationOnboarding> applicationOnboardingList = new ArrayList<>();
		List<UserOnboarding> userOnboardinglist = new ArrayList<>();
		List<ProjectOnboardingDetails> projectOnboardinglist = new ArrayList<>();
		List<ContractOnboardingDetails> contractOnboardingList = new ArrayList<>();

		List<ApplicationOnboarding> duplicateapplicationOnboardingList = new ArrayList<>();
		List<UserOnboarding> duplicateuserOnboardingList = new ArrayList<>();
		List<ProjectOnboardingDetails> duplicateProjectOnboardingList = new ArrayList<>();
		List<ContractOnboardingDetails> duplicateContractOnboardingList = new ArrayList<>();

		List<ContractOnboardingDetails> contractOnboardingDetails = contractsOnboardingRespository.findAll();
		List<DepartmentOnboarding> departmentOnboardings = departmentOnboardingRepository.findAll();
		List<ApplicationOnboarding> applicationOnboardings = applicationOnboardingRepository.findAll();
		List<ProjectOnboardingDetails> projectOnboardingDetails = projectOnboardingDetailsRepository.findAll();
		List<UserOnboarding> userOnboardings = userOnboardingDetailsRepository.findAllBySignup();
		String[] strArray = { "user", "project", "application", "department", "contract" };
		List<String> categorytest = Arrays.asList(strArray);
		if (!categorytest.contains(category)) {
			throw new DataValidationException("Provide Valid Category", category, null);
		}
		if (category.equalsIgnoreCase("user")) {
			for (UserOnboarding details : userOnboardings) {
				UserOnboarding userOnboarding = userOnboardingDetailsRepository.getByUserEmail(details.getUserEmail());
				userOnboardinglist.add(userOnboarding);
			}
			for (UserOnboarding requestTracking : userOnboardinglist) {
				if (!duplicateuserOnboardingList.contains(requestTracking)) {
					duplicateuserOnboardingList.add(requestTracking);
				}
			}
			List<UserOnboarding> userslastFiltered = duplicateuserOnboardingList.stream()
					.filter(p -> p.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW)
							|| p.getApprovedRejected().equalsIgnoreCase(Constant.REJECTED))
					.collect(Collectors.toList());
			for (UserOnboarding stream : userslastFiltered) {
				RequestTrackingListViewResponse viewResponse = new RequestTrackingListViewResponse();
				duplicateuserOnboardingList.remove(stream);
				viewResponse.setRequestId(stream.getRequestNumber());
				viewResponse.setChildRequestId(stream.getChildRequestNumber());
				viewResponse.setOnboardingRequestName(stream.getFirstName() + " " + stream.getLastName());
				viewResponse.setOnboardingStatus(stream.getOnboardingStatus());
				viewResponse.setOnboardingComments(stream.getComments());
				viewResponse.setOnboardingRequestAvatar(stream.getLogoUrl());
				list.add(viewResponse);
			}
			for (UserOnboarding stream : duplicateuserOnboardingList) {
				UserDetails userDetails = userDetailsRepository.getDeleteduserEmail(stream.getUserEmail());
				RequestTrackingListViewResponse viewResponse = new RequestTrackingListViewResponse();
				if (userDetails != null) {
					viewResponse.setRequestId(stream.getRequestNumber());
					viewResponse.setChildRequestId(stream.getChildRequestNumber());
					viewResponse.setOnboardingRequestName(stream.getFirstName() + " " + stream.getLastName());
					viewResponse.setOnboardingStatus(stream.getOnboardingStatus());
					viewResponse.setOnboardingComments(stream.getComments());
					viewResponse.setOnboardingRequestAvatar(stream.getLogoUrl());
					list.add(viewResponse);
				}
			}
		}
		if (category.equalsIgnoreCase("department")) {
			for (DepartmentOnboarding details : departmentOnboardings) {
				RequestTrackingListViewResponse viewResponse = new RequestTrackingListViewResponse();
				DepartmentOnboarding step1childReq;
				DepartmentOnboarding step1req;
				DepartmentOnboarding step2ReqReject;
				DepartmentOnboarding step2ChildReqReject;
				DepartmentOnboarding step3ReqSuperApprove;
				DepartmentOnboarding step3ChildReqSuperApprove;

				if (details.getChildRequestNumber() != null) {
					step1childReq = departmentOnboardingRepository.findAllBySuperAdmin(details.getChildRequestNumber());
					if (step1childReq != null) {
						viewResponse.setRequestId(step1childReq.getRequestNumber());
						viewResponse.setChildRequestId(step1childReq.getChildRequestNumber());
						viewResponse.setOnboardingRequestName(step1childReq.getDepartmentName());
						viewResponse.setOnboardingStatus(step1childReq.getOnboardingStatus());
						viewResponse.setOnboardingComments(step1childReq.getComments());
					} else if (step1childReq == null) {
						step2ChildReqReject = departmentOnboardingRepository
								.findAllByChildReqReject(details.getChildRequestNumber());
						if (step2ChildReqReject != null) {
							viewResponse.setRequestId(step2ChildReqReject.getRequestNumber());
							viewResponse.setChildRequestId(step2ChildReqReject.getChildRequestNumber());
							viewResponse.setOnboardingRequestName(step2ChildReqReject.getDepartmentName());
							viewResponse.setOnboardingStatus(step2ChildReqReject.getOnboardingStatus());
							viewResponse.setOnboardingComments(step2ChildReqReject.getComments());
						} else if (step1childReq == null && step2ChildReqReject == null ) {
							step3ReqSuperApprove = departmentOnboardingRepository
									.findChildReqSuperApprovee(details.getChildRequestNumber());
							if(step3ReqSuperApprove!=null) {
							viewResponse.setRequestId(step3ReqSuperApprove.getRequestNumber());
							viewResponse.setChildRequestId(step3ReqSuperApprove.getChildRequestNumber());
							viewResponse.setOnboardingRequestName(step3ReqSuperApprove.getDepartmentName());
							viewResponse.setOnboardingStatus(step3ReqSuperApprove.getOnboardingStatus());
							viewResponse.setOnboardingComments(step3ReqSuperApprove.getComments());
							}
						}
					}
				}
				if (details.getRequestNumber() != null && details.getChildRequestNumber() == null) {
					step1req = departmentOnboardingRepository.findAllBySuperAdminRequestId(details.getRequestNumber());
					if (step1req != null) {
						viewResponse.setRequestId(step1req.getRequestNumber());
						viewResponse.setChildRequestId(step1req.getChildRequestNumber());
						viewResponse.setOnboardingRequestName(step1req.getDepartmentName());
						viewResponse.setOnboardingStatus(step1req.getOnboardingStatus());
						viewResponse.setOnboardingComments(step1req.getComments());
					} else if (step1req == null) {
						step2ReqReject = departmentOnboardingRepository.findAllByReject(details.getRequestNumber());
						if (step2ReqReject != null) {
							viewResponse.setRequestId(step2ReqReject.getRequestNumber());
							viewResponse.setChildRequestId(step2ReqReject.getChildRequestNumber());
							viewResponse.setOnboardingRequestName(step2ReqReject.getDepartmentName());
							viewResponse.setOnboardingStatus(step2ReqReject.getOnboardingStatus());
							viewResponse.setOnboardingComments(step2ReqReject.getComments());
						} else if (step1req == null && step2ReqReject == null) {
							step3ChildReqSuperApprove = departmentOnboardingRepository
									.findReqSuperApprove(details.getRequestNumber());
							viewResponse.setRequestId(step3ChildReqSuperApprove.getRequestNumber());
							viewResponse.setChildRequestId(step3ChildReqSuperApprove.getChildRequestNumber());
							viewResponse.setOnboardingRequestName(step3ChildReqSuperApprove.getDepartmentName());
							viewResponse.setOnboardingStatus(step3ChildReqSuperApprove.getOnboardingStatus());
							viewResponse.setOnboardingComments(step3ChildReqSuperApprove.getComments());
						}
					}

				}
				list.add(viewResponse);
			}
		}
		if (category.equalsIgnoreCase("application")) {
			for (ApplicationOnboarding details : applicationOnboardings) {
				ApplicationOnboarding applicationOnboarding = applicationOnboardingRepository
						.getDataByApplicationName(details.getApplicationName(), details.getOwnerDepartment());
				applicationOnboardingList.add(applicationOnboarding);
			}
			for (ApplicationOnboarding requestTracking : applicationOnboardingList) {
				if (!duplicateapplicationOnboardingList.contains(requestTracking)) {
					duplicateapplicationOnboardingList.add(requestTracking);
				}
			}
			List<ApplicationOnboarding> lastFiltered = duplicateapplicationOnboardingList.stream()
					.filter(p -> p.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW)
							|| p.getApprovedRejected().equalsIgnoreCase(Constant.REJECTED))
					.collect(Collectors.toList());
			for (ApplicationOnboarding stream : lastFiltered) {
				RequestTrackingListViewResponse viewResponse = new RequestTrackingListViewResponse();
				duplicateapplicationOnboardingList.remove(stream);
				viewResponse.setRequestId(stream.getRequestNumber());
				viewResponse.setChildRequestId(stream.getChildRequestNumber());
				viewResponse.setOnboardingRequestName(stream.getApplicationName());
				viewResponse.setOnboardingStatus(stream.getOnboardingStatus());
				viewResponse.setOnboardingComments(stream.getComments());
				ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
						.findByApplicationName(stream.getApplicationName());
				if (applicationLogoEntity != null) {
					viewResponse.setOnboardingRequestAvatar(applicationLogoEntity.getLogoUrl());
				}
				list.add(viewResponse);
			}
			for (ApplicationOnboarding stream : duplicateapplicationOnboardingList) {
				ApplicationDetails application = applicationDetailsRepository
						.getDeletedApplicationByAppName(stream.getApplicationName(), stream.getOwnerDepartment());
				RequestTrackingListViewResponse viewResponse = new RequestTrackingListViewResponse();
				if (application != null) {
					viewResponse.setRequestId(stream.getRequestNumber());
					viewResponse.setChildRequestId(stream.getChildRequestNumber());
					viewResponse.setOnboardingRequestName(stream.getApplicationName());
					viewResponse.setOnboardingStatus(stream.getOnboardingStatus());
					viewResponse.setOnboardingComments(stream.getComments());
					ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
							.findByApplicationName(stream.getApplicationName());
					if (applicationLogoEntity != null) {
						viewResponse.setOnboardingRequestAvatar(applicationLogoEntity.getLogoUrl());
					}
					list.add(viewResponse);
				}
			}
		}
		if (category.equalsIgnoreCase("project")) {
			for (ProjectOnboardingDetails details : projectOnboardingDetails) {
				ProjectOnboardingDetails projectOnboarding = projectOnboardingDetailsRepository
						.findAllByProjectName(details.getProjectName());
				projectOnboardinglist.add(projectOnboarding);
			}
			for (ProjectOnboardingDetails requestTracking : projectOnboardinglist) {
				if (!duplicateProjectOnboardingList.contains(requestTracking)) {
					duplicateProjectOnboardingList.add(requestTracking);
				}
			}
			List<ProjectOnboardingDetails> projectlastFiltered = duplicateProjectOnboardingList.stream()
					.filter(p -> p.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW)
							|| p.getApprovedRejected().equalsIgnoreCase(Constant.REJECTED))
					.collect(Collectors.toList());
			for (ProjectOnboardingDetails stream : projectlastFiltered) {
				RequestTrackingListViewResponse viewResponse = new RequestTrackingListViewResponse();
				duplicateProjectOnboardingList.remove(stream);
				viewResponse.setRequestId(stream.getRequestNumber());
				viewResponse.setChildRequestId(stream.getChildRequestNumber());
				viewResponse.setOnboardingRequestName(stream.getProjectName());
				viewResponse.setOnboardingStatus(stream.getOnboardingStatus());
				viewResponse.setOnboardingComments(stream.getComments());
				list.add(viewResponse);
			}
			for (ProjectOnboardingDetails stream : duplicateProjectOnboardingList) {
				ProjectDetails application = projectDetailsRepository.findByProjName(stream.getProjectName());
				RequestTrackingListViewResponse viewResponse = new RequestTrackingListViewResponse();
				if (application != null) {
					viewResponse.setRequestId(stream.getRequestNumber());
					viewResponse.setChildRequestId(stream.getChildRequestNumber());
					viewResponse.setOnboardingRequestName(stream.getProjectName());
					viewResponse.setOnboardingStatus(stream.getOnboardingStatus());
					viewResponse.setOnboardingComments(stream.getComments());
					list.add(viewResponse);
				}
			}
		}

		if (category.equalsIgnoreCase("contract")) {
			for (ContractOnboardingDetails details : contractOnboardingDetails) {
				ContractOnboardingDetails contractOnboarding = contractsOnboardingRespository
						.findAllByContractName(details.getContractName());
				contractOnboardingList.add(contractOnboarding);
			}
			for (ContractOnboardingDetails requestTracking : contractOnboardingList) {
				if (!duplicateContractOnboardingList.contains(requestTracking)) {
					duplicateContractOnboardingList.add(requestTracking);
				}
			}
			List<ContractOnboardingDetails> contractsLastFiltered = duplicateContractOnboardingList.stream()
					.filter(p -> p.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW)
							|| p.getApprovedRejected().equalsIgnoreCase(Constant.REJECTED))
					.collect(Collectors.toList());
			for (ContractOnboardingDetails stream : contractsLastFiltered) {
				RequestTrackingListViewResponse viewResponse = new RequestTrackingListViewResponse();
				duplicateContractOnboardingList.remove(stream);
				viewResponse.setRequestId(stream.getRequestNumber());
				viewResponse.setChildRequestId(stream.getChildRequestNumber());
				viewResponse.setOnboardingRequestName(stream.getContractName());
				viewResponse.setApplicationName(stream.getApplicationName());
				viewResponse.setOnboardingStatus(stream.getOnboardingStatus());
				viewResponse.setOnboardingComments(stream.getComments());
				ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
						.findByApplicationName(stream.getApplicationName());
				if (applicationLogoEntity != null) {
					viewResponse.setOnboardingRequestAvatar(applicationLogoEntity.getLogoUrl());
				}
				list.add(viewResponse);
			}
			for (ContractOnboardingDetails stream : duplicateContractOnboardingList) {
				ApplicationContractDetails applicationContractDetails = applicationContractDetailsRepository
						.findByContractName(stream.getContractName());
				RequestTrackingListViewResponse viewResponse = new RequestTrackingListViewResponse();
				if (applicationContractDetails != null) {
					viewResponse.setRequestId(stream.getRequestNumber());
					viewResponse.setApplicationName(stream.getApplicationName());
					viewResponse.setChildRequestId(stream.getChildRequestNumber());
					viewResponse.setOnboardingRequestName(stream.getContractName());
					viewResponse.setOnboardingStatus(stream.getOnboardingStatus());
					viewResponse.setOnboardingComments(stream.getComments());
					ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
							.findByApplicationName(stream.getApplicationName());
					if (applicationLogoEntity != null) {
						viewResponse.setOnboardingRequestAvatar(applicationLogoEntity.getLogoUrl());
					}
					list.add(viewResponse);
				}
			}
		}

		for (RequestTrackingListViewResponse requestTracking : list) {
			if (requestTracking.getChildRequestId() != null) {
				List<RequestTrackingListViewResponse> duplicateChecking = duplicateChildList.stream()
						.filter(p -> p.getChildRequestId().equals(requestTracking.getChildRequestId()))
						.collect(Collectors.toList());
				if (duplicateChecking.isEmpty()) {
					duplicateChildList.add(requestTracking);
				}
			}
			if (requestTracking.getRequestId() != null && requestTracking.getChildRequestId() == null) {
				List<RequestTrackingListViewResponse> duplicateChecking = duplicateParentList.stream()
						.filter(p -> p.getRequestId().equals(requestTracking.getRequestId()))
						.collect(Collectors.toList());
				if (duplicateChecking.isEmpty()) {
					duplicateParentList.add(requestTracking);
				}
			}
		}
		duplicateChildList.addAll(duplicateParentList);
		Collections.sort(duplicateChildList, new CustomComparator(category));
		response.setData(duplicateChildList);
		response.setAction("requestTrackingResponse");
		commonResponse.setStatus(HttpStatus.OK);
		commonResponse.setMessage(Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
		commonResponse.setResponse(response);
		return commonResponse;
	}

	@Override
	public CommonResponse dashBoardTopCardAnalystics() throws DataValidationException {
		DashboardTopCardResponse response = new DashboardTopCardResponse();
		DashboardTopCardAnalysticsResponse analysticsResponse = new DashboardTopCardAnalysticsResponse();
		LocalDate localToday = LocalDate.now();
		Date firstMonthFirstDate = CommonUtil.convertLocalDatetoDate(localToday.withDayOfMonth(1).minusMonths(3));
		Date currentMonthLast = CommonUtil
				.convertLocalDatetoDate(localToday.withDayOfMonth(1).plusMonths(1).minusDays(1));
		List<ApplicationDetails> applicationDetailsList = applicationDetailsRepo
				.getAllApplicationBtwDates(firstMonthFirstDate, currentMonthLast);
		List<ApplicationContractDetails> contractEndDateList = applicationContractDetailsRepository
				.getAllContractsBtwDates(firstMonthFirstDate, currentMonthLast);
		List<ApplicationContractDetails> renewalsDetailsList = applicationContractDetailsRepository
				.getAllRenewalsBtwDates(firstMonthFirstDate, currentMonthLast);
		List<ApplicationContractDetails> finalContractsRenewalsList = new ArrayList<>();
		List<ApplicationContractDetails> nonduplicatefinalContractsRenewalsList = new ArrayList<>();
		finalContractsRenewalsList.addAll(contractEndDateList);
		finalContractsRenewalsList.addAll(renewalsDetailsList);

		for (ApplicationContractDetails details : finalContractsRenewalsList) {
			if (nonduplicatefinalContractsRenewalsList.stream()
					.filter(s -> s.getContractId().equalsIgnoreCase(details.getContractId()))
					.collect(Collectors.toList()).isEmpty()) {
				nonduplicatefinalContractsRenewalsList.add(details);
			}
		}

		if (applicationDetailsList.isEmpty()) {
			response.setApplication(analysticsResponse);
		} else {
			response.setApplication(topCardAnalystics(applicationDetailsList, null, null));
		}
		if (nonduplicatefinalContractsRenewalsList.isEmpty()) {
			response.setRenewal(analysticsResponse);
		} else {
			response.setRenewal(topCardAnalystics(null, null, nonduplicatefinalContractsRenewalsList));
		}
		LocalDate now = LocalDate.now();
		List<String> list = new ArrayList<>();
		for (int j = 0; j < 4; j++) {
			list.add(now.minusMonths(j).getMonth().toString().substring(0, 3) + " "
					+ String.valueOf(now.minusMonths(j).getYear()).substring(2, 4));
		}
		response.setMonths(list);
		return new CommonResponse(HttpStatus.OK, new Response("DashBoardAnalysticsResponse", response),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private DashboardTopCardAnalysticsResponse topCardAnalystics(List<ApplicationDetails> applicationDetailsList,
			List<ApplicationSubscriptionDetails> subscriptionDetailsList,
			List<ApplicationContractDetails> nonduplicatefinalContractsRenewalsList) {
		DashboardTopCardAnalysticsResponse applicationAnalysticsResponse = new DashboardTopCardAnalysticsResponse();
		LocalDate tempToday = LocalDate.now();
		Map<Integer, Integer> monthToSizeMap = new HashMap<>();

		for (int minusmonth = 1; minusmonth <= 4; minusmonth++) {
			LocalDate tempCurrentMonthFirst = tempToday.withDayOfMonth(1);
			LocalDate tempCurrentMonthLast = tempToday.withDayOfMonth(1).plusMonths(1).minusDays(1);
			int size = 0;

			if (applicationDetailsList != null) {
				size = (int) applicationDetailsList.stream()
						.filter(application -> application.getCreatedOn().toInstant().atZone(ZoneId.systemDefault())
								.toLocalDate().isAfter(tempCurrentMonthFirst.minusDays(1))
								&& application.getCreatedOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
										.isBefore(tempCurrentMonthLast.plusDays(1)))
						.count();
			} else if (subscriptionDetailsList != null) {
				size = (int) subscriptionDetailsList.stream()
						.filter(subscription -> subscription.getCreatedOn().toInstant().atZone(ZoneId.systemDefault())
								.toLocalDate().isAfter(tempCurrentMonthFirst.minusDays(1))
								&& subscription.getCreatedOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
										.isBefore(tempCurrentMonthLast.plusDays(1)))
						.count();
			} else if (nonduplicatefinalContractsRenewalsList != null) {
				size = (int) nonduplicatefinalContractsRenewalsList.stream()
						.filter(contract -> (contract.getAutoRenew()
								&& contract.getRenewalDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
										.isAfter(tempCurrentMonthFirst.minusDays(1))
								&& contract.getRenewalDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
										.isBefore(tempCurrentMonthLast.plusDays(1)))
								|| (!contract.getAutoRenew()
										&& contract.getContractEndDate().toInstant().atZone(ZoneId.systemDefault())
												.toLocalDate().isAfter(tempCurrentMonthFirst.minusDays(1))
										&& contract.getContractEndDate().toInstant().atZone(ZoneId.systemDefault())
												.toLocalDate().isBefore(tempCurrentMonthLast.plusDays(1))))
						.count();
			}
			if (minusmonth == 1) {
				applicationAnalysticsResponse.setFirstMonth(size);
			} else if (minusmonth == 2) {
				applicationAnalysticsResponse.setSecondMonth(size);
			} else if (minusmonth == 3) {
				applicationAnalysticsResponse.setThirdMonth(size);
			} else if (minusmonth == 4) {
				applicationAnalysticsResponse.setFourthmonth(size);
			}
			monthToSizeMap.put(minusmonth, size);
			tempToday = tempToday.minusMonths(1);
		}
		return applicationAnalysticsResponse;

	}

	@Override
	public CommonResponse dashBoardDepartmentBudgetAnalystics() throws DataValidationException {
		DashboardDepartmentBudgetResponse budgetResponse = new DashboardDepartmentBudgetResponse();
		List<DepartmentBudgetResponse> list = new ArrayList<>();
		DepartmentBudgetResponse pastQuarter = new DepartmentBudgetResponse();
		DepartmentBudgetResponse currentQuarter = new DepartmentBudgetResponse();
		DepartmentBudgetResponse futureQuarter = new DepartmentBudgetResponse();
		LocalDate inputDate = LocalDate.now();
		LocalDate firstDayOfQuarter = inputDate.withMonth(inputDate.get(IsoFields.QUARTER_OF_YEAR) * 3 - 2)
				.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate lastDayOfQuarter = inputDate.withMonth(inputDate.get(IsoFields.QUARTER_OF_YEAR) * 3)
				.with(TemporalAdjusters.lastDayOfMonth());
		LocalDate firstDayOfPastQuarter = firstDayOfQuarter.minusMonths(3).withDayOfMonth(1);
		LocalDate lastDayOfPastQuarter = firstDayOfQuarter.minusDays(1);
		LocalDate firstDayOfFutureQuarter = lastDayOfQuarter.plusDays(1);
		LocalDate lastDayOfFutureQuarter = firstDayOfFutureQuarter.plusMonths(3).minusDays(1);
		List<DepartmentDetails> deptDetails = departmentRepository.findAll();
		int totalBudget = deptDetails.stream().mapToInt(p -> p.getBudget().intValue()).sum();
		BigDecimal currentSpent = new BigDecimal("0.0");
		BigDecimal pastSpent = new BigDecimal("0.0");
		BigDecimal futureSpent = new BigDecimal("0.0");
		BigDecimal currentAdminSpent = new BigDecimal("0.0");
		BigDecimal pastAdminSpent = new BigDecimal("0.0");
		BigDecimal futureAdminSpent = new BigDecimal("0.0");
		for (DepartmentDetails departmentDetails : deptDetails) {
			Map<String, BigDecimal> current = getDepartmentTotalSpend(departmentDetails, firstDayOfQuarter,
					lastDayOfQuarter);
			Map<String, BigDecimal> past = getDepartmentTotalSpend(departmentDetails, firstDayOfPastQuarter,
					lastDayOfPastQuarter);
			Map<String, BigDecimal> future = getDepartmentTotalSpend(departmentDetails, firstDayOfFutureQuarter,
					lastDayOfFutureQuarter);
			currentSpent = currentSpent.add(current.get(Constant.TOTAL_SPEND));
			pastSpent = pastSpent.add(past.get(Constant.TOTAL_SPEND));
			futureSpent = futureSpent.add(future.get(Constant.TOTAL_SPEND));
			currentAdminSpent = currentAdminSpent.add(current.get(Constant.ADMIN_SPEND));
			pastAdminSpent = pastAdminSpent.add(past.get(Constant.ADMIN_SPEND));
			futureAdminSpent = futureAdminSpent.add(future.get(Constant.ADMIN_SPEND));
		}
		pastQuarter.setTotalSpend(pastSpent);
		pastQuarter.setAdminCost(pastAdminSpent);
		pastQuarter.setCurrency("USD");
		pastQuarter.setName("Past Quarter");
		currentQuarter.setTotalSpend(currentSpent);
		currentQuarter.setAdminCost(currentAdminSpent);
		currentQuarter.setCurrency("USD");
		currentQuarter.setName("Current Quarter");
		futureQuarter.setName("Future Quarter");
		futureQuarter.setTotalSpend(futureSpent);
		futureQuarter.setAdminCost(futureAdminSpent);
		futureQuarter.setCurrency("USD");
		list.add(pastQuarter);
		list.add(currentQuarter);
		list.add(futureQuarter);
		budgetResponse.setTotalBudget(BigDecimal.valueOf(totalBudget));
		budgetResponse.setQuarter(list);
		return new CommonResponse(HttpStatus.OK, new Response("budgetResponse", budgetResponse),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse dashBoardExpenseByDepartmentAnalystics() throws DataValidationException {
		List<DashBoardDepartmentExpenseResponse> list = new ArrayList<>();
		List<DepartmentDetails> deptDetails = departmentRepository.findAll();
		String currency = "MYR";
		for (DepartmentDetails department : deptDetails) {
			DashBoardDepartmentExpenseResponse departmentExpenseResponse = new DashBoardDepartmentExpenseResponse();
			BigDecimal totalSpend = new BigDecimal("0.0");
			BigDecimal adminSpend = new BigDecimal("0.0");
			List<ApplicationDetails> applications = applicationDetailsRepo
					.findByDepartmentName(department.getDepartmentName());
			for (ApplicationDetails application : applications) {
				for (ApplicationContractDetails contract : application.getContractDetails()) {
					if (contract.getContractStatus().equalsIgnoreCase(Constant.ACTIVE)
							|| contract.getContractStatus().equalsIgnoreCase(Constant.EXPIRED)) {
						totalSpend = totalSpend.add(BigDecimal.valueOf(contract.getLicenseDetails().stream()
								.mapToInt(p -> p.getTotalCost().intValue()).sum()));
						adminSpend = adminSpend.add(BigDecimal.valueOf(contract.getLicenseDetails().stream()
								.mapToInt(p -> p.getConvertedCost().intValue()).sum()));
						if (!contract.getLicenseDetails().isEmpty()) {
							contract.getLicenseDetails().forEach(p -> 
								departmentExpenseResponse.setCurrency("MYR")
							);
						} else {
							departmentExpenseResponse.setCurrency("MYR");
						}
					}
				}

			}
			departmentExpenseResponse.setValue(totalSpend);
			departmentExpenseResponse.setAdminCost(adminSpend);
			departmentExpenseResponse.setCategory(department.getDepartmentName());
			departmentExpenseResponse.setCurrency(currency);
			list.add(departmentExpenseResponse);
		}
		list.sort(Comparator.comparing(DashBoardDepartmentExpenseResponse::getAdminCost).reversed());
		return new CommonResponse(HttpStatus.OK, new Response("budgetResponse", list),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private Map<String, BigDecimal> getDepartmentTotalSpend(DepartmentDetails departmentDetails,
			LocalDate firstDayOfQuarter, LocalDate lastDayOfQuarter) {
		Map<String, BigDecimal> spends = new HashMap<>();
		BigDecimal totalSpend = new BigDecimal("0.0");
		BigDecimal adminSpend = new BigDecimal("0.0");
		List<ApplicationDetails> applicationDetails = applicationDetailsRepository
				.findByDepartmentName(departmentDetails.getDepartmentName());
		for (ApplicationDetails application : applicationDetails) {
			List<ApplicationContractDetails> contractDetails = applicationContractDetailsRepository
					.findActiveContractsBtwConEndDate(application.getApplicationId(),
							CommonUtil.convertLocalDatetoDate(firstDayOfQuarter),
							CommonUtil.convertLocalDatetoDate(lastDayOfQuarter));
			List<ApplicationContractDetails> activeAndExpiredContracts = contractDetails.stream()
					.filter(c -> (c.getContractStatus().equalsIgnoreCase(Constant.ACTIVE)
							|| c.getContractStatus().equalsIgnoreCase(Constant.EXPIRED))
							&&(c.getApplicationId().getEndDate()==null || 
							c.getApplicationId().getEndDate().after(c.getContractStartDate())
							||c.getApplicationId().getEndDate().compareTo(c.getContractStartDate())==0))
					.collect(Collectors.toList());
			for (ApplicationContractDetails contract : activeAndExpiredContracts) {
				BigDecimal totalContractCost = BigDecimal.valueOf(
						contract.getLicenseDetails().stream().mapToLong(p -> p.getTotalCost().longValue()).sum());
				BigDecimal totalAdminCost = BigDecimal.valueOf(
						contract.getLicenseDetails().stream().mapToLong(p -> p.getConvertedCost().longValue()).sum());
				if (ContractType.annual(contract.getContractType())) {
					BigDecimal monthlyCost = totalContractCost.divide(
							BigDecimal.valueOf(contract.getContractTenure()).multiply(BigDecimal.valueOf(12)), 2,
							RoundingMode.FLOOR);
					BigDecimal monthlyAdminCost = totalAdminCost.divide(
							BigDecimal.valueOf(contract.getContractTenure()).multiply(BigDecimal.valueOf(12)), 2,
							RoundingMode.FLOOR);
					if (contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
							|| contract.getContractStartDate()
									.compareTo(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
						if ((contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) 
							&&(contract.getContractEndDate()
									.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
									|| contract.getContractEndDate()
											.compareTo(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0)) {
								LocalDate enddate = contract.getContractEndDate().toInstant()
										.atZone(ZoneId.systemDefault()).toLocalDate();
								long monthsBetweenTwoDates = ChronoUnit.MONTHS.between(firstDayOfQuarter, enddate);
								if (contract.getContractStartDate()
										.before(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) &&  ((int) monthsBetweenTwoDates > 0)) {
										LocalDate localContractEnd = contract.getContractEndDate().toInstant()
												.atZone(ZoneId.systemDefault()).toLocalDate();
										if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
											int i = 1;
											while (i != 0) {
												LocalDate minusYears = localContractEnd.minusYears(i);
												if (CommonUtil.convertLocalDatetoDate(minusYears)
														.before(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))) {
													i = 0;
												} else {
													totalSpend = totalSpend
															.add(monthlyCost.multiply(new BigDecimal(12)));
													adminSpend = adminSpend
															.add(monthlyAdminCost.multiply(new BigDecimal(12)));
													i++;
												}

											}

										}
										if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
											int i = 6;
											while (i != 0) {
												LocalDate minusYears = localContractEnd.minusMonths(i);
												if (CommonUtil.convertLocalDatetoDate(minusYears)
														.before(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))) {
													i = 0;
												} else {
													totalSpend = totalSpend
															.add(monthlyCost.multiply(new BigDecimal(6)));
													adminSpend = adminSpend
															.add(monthlyAdminCost.multiply(new BigDecimal(6)));
													i = i + 6;
												}
											}
										}
										if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
											int i = 3;
											while (i != 0) {
												LocalDate minusYears = localContractEnd.minusMonths(i);
												if (CommonUtil.convertLocalDatetoDate(minusYears)
														.before(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))) {
													i = 0;
												} else {
													totalSpend = totalSpend
															.add(monthlyCost.multiply(new BigDecimal(3)));
													adminSpend = adminSpend
															.add(monthlyAdminCost.multiply(new BigDecimal(3)));
													i = i + 3;
												}
											}
										}
										if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
											LocalDate contractStartDate = contract.getContractStartDate().toInstant()
							                        .atZone(ZoneId.systemDefault()).toLocalDate();
							                if (contractStartDate.isBefore(firstDayOfQuarter)) {
							                    contractStartDate = firstDayOfQuarter;
							                }
							                if (enddate.isAfter(lastDayOfQuarter)) {
							                	enddate = lastDayOfQuarter;
							                }
 
							                long monthsInQuarter = ChronoUnit.MONTHS.between(contractStartDate, enddate.plusDays(1));
 
							                totalSpend = totalSpend.add(monthlyCost.multiply(BigDecimal.valueOf(monthsInQuarter)));
							                adminSpend = adminSpend.add(monthlyAdminCost.multiply(BigDecimal.valueOf(monthsInQuarter)));
										}
									 								}
								if ((contract.getContractStartDate()
										.compareTo(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0
										&& contract.getContractEndDate()
												.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))) &&
									((int) monthsBetweenTwoDates > 0)){
										LocalDate localContractStart = contract.getContractStartDate().toInstant()
												.atZone(ZoneId.systemDefault()).toLocalDate();
										if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
											totalSpend = totalSpend.add(totalContractCost);
											adminSpend = adminSpend.add(totalAdminCost);
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
													totalSpend = totalSpend
															.add(monthlyCost.multiply(new BigDecimal(12)));
													adminSpend = adminSpend
															.add(monthlyAdminCost.multiply(new BigDecimal(12)));
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
													totalSpend = totalSpend
															.add(monthlyCost.multiply(new BigDecimal(6)));
													adminSpend = adminSpend
															.add(monthlyAdminCost.multiply(new BigDecimal(6)));
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
													totalSpend = totalSpend
															.add(monthlyCost.multiply(new BigDecimal(3)));
													adminSpend = adminSpend
															.add(monthlyAdminCost.multiply(new BigDecimal(3)));
													i = i + 3;
												}
											}
										}
										if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
							                if (localContractStart.isBefore(firstDayOfQuarter)) {
							                    localContractStart = firstDayOfQuarter;
							                }
							                if (enddate.isAfter(lastDayOfQuarter)) {
							                	enddate = lastDayOfQuarter;
							                }
 
							                long monthsInQuarter = ChronoUnit.MONTHS.between(localContractStart, enddate.plusDays(1));
 
							                totalSpend = totalSpend.add(monthlyCost.multiply(BigDecimal.valueOf(monthsInQuarter)));
							                adminSpend = adminSpend.add(monthlyAdminCost.multiply(BigDecimal.valueOf(monthsInQuarter)));
										}
									}
						}
						if (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))
								|| contract.getContractEndDate()
										.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
							LocalDate startdate = contract.getContractStartDate().toInstant()
									.atZone(ZoneId.systemDefault()).toLocalDate();
							long monthsBetweenTwoDates = ChronoUnit.MONTHS.between(firstDayOfQuarter, lastDayOfQuarter);
							if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency()) && contract.getContractStartDate()
									.compareTo(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
								totalSpend = totalSpend.add(totalContractCost);
								adminSpend = adminSpend.add(totalAdminCost);
							}
							if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								int i = 1;
								if(CommonUtil.convertLocalDatetoDate(startdate)
												.compareTo(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) ==0) {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
								}
								while (i != 0) {
									LocalDate plusYears = startdate.plusYears(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										if (CommonUtil.convertLocalDatetoDate(plusYears)
												.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))) {
											totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
											adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
										}

										i++;
									} else {
										i = 0;
									}
								}
							}
							if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								int i = 6;
								if(CommonUtil.convertLocalDatetoDate(startdate)
										.compareTo(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) ==0) {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
								}
								while (i != 0) {
									LocalDate minusYears = startdate.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(minusYears)
											.before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										if (CommonUtil.convertLocalDatetoDate(minusYears)
												.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))) {
											totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
											adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(6)));
										}
										i = i + 6;
									} else {
										i = 0;
									}
								}
							}
							if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								int i = 3;
								if(CommonUtil.convertLocalDatetoDate(startdate)
										.compareTo(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) ==0) {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
								}
								while (i != 0) {
									LocalDate minusYears = startdate.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(minusYears)
											.before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										if (CommonUtil.convertLocalDatetoDate(minusYears)
												.after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))) {
											totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
											adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
										}
										i = i + 3;
									} else {
										i = 0;
									}
								}
							}
							if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend
										.add(monthlyCost.multiply(BigDecimal.valueOf(monthsBetweenTwoDates + 1)));
								adminSpend = adminSpend
										.add(monthlyAdminCost.multiply(BigDecimal.valueOf(monthsBetweenTwoDates + 1)));
							}
						}
					}
					if (contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
							&& contract.getContractStartDate()
									.before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
						if (contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
							LocalDate startDate = contract.getContractStartDate().toInstant()
									.atZone(ZoneId.systemDefault()).toLocalDate();
							LocalDate enddate = contract.getContractEndDate().toInstant().atZone(ZoneId.systemDefault())
									.toLocalDate();
							long monthsBetweenTwoDates = ChronoUnit.MONTHS.between(startDate, enddate);
							LocalDate localContractStart = contract.getContractStartDate().toInstant()
									.atZone(ZoneId.systemDefault()).toLocalDate();
							if ((int) monthsBetweenTwoDates > 0) {
								if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
									totalSpend = totalSpend.add(totalContractCost);
									adminSpend = adminSpend.add(totalAdminCost);
								}
								if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
									int i = 1;
									while (i != 0) {
										LocalDate plusYears = localContractStart.plusYears(i);
										if (CommonUtil.convertLocalDatetoDate(plusYears)
												.after(CommonUtil.convertLocalDatetoDate(enddate))) {
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
										LocalDate plusMonths = localContractStart.plusMonths(i);
										if (CommonUtil.convertLocalDatetoDate(plusMonths)
												.after(CommonUtil.convertLocalDatetoDate(enddate))) {
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
										LocalDate plusMonths = localContractStart.plusMonths(i);
										if (CommonUtil.convertLocalDatetoDate(plusMonths)
												.after(CommonUtil.convertLocalDatetoDate(enddate))) {
											i = 0;
										} else {
											totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
											adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
											i = i + 3;
										}
									}
								}
								if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
					                if (startDate.isBefore(firstDayOfQuarter)) {
					                    startDate = firstDayOfQuarter;
					                }
					                if (enddate.isAfter(lastDayOfQuarter)) {
					                	enddate = lastDayOfQuarter;
					                }

					                long monthsInQuarter = ChronoUnit.MONTHS.between(startDate, enddate.plusDays(1));

					                totalSpend = totalSpend.add(monthlyCost.multiply(BigDecimal.valueOf(monthsInQuarter)));
					                adminSpend = adminSpend.add(monthlyAdminCost.multiply(BigDecimal.valueOf(monthsInQuarter)));
								}

							} else {
								if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
									totalSpend = totalSpend.add(totalContractCost);
									adminSpend = adminSpend.add(totalAdminCost);
								}
								if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
									totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
									adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(12)));
									int i = 1;
									while (i != 0) {
										LocalDate plusYears = localContractStart.plusYears(i);
										if (CommonUtil.convertLocalDatetoDate(plusYears)
												.after(CommonUtil.convertLocalDatetoDate(enddate))) {
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
										LocalDate plusMonths = localContractStart.plusMonths(i);
										if (CommonUtil.convertLocalDatetoDate(plusMonths)
												.after(CommonUtil.convertLocalDatetoDate(enddate))) {
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
										LocalDate plusMonths = localContractStart.plusMonths(i);
										if (CommonUtil.convertLocalDatetoDate(plusMonths)
												.after(CommonUtil.convertLocalDatetoDate(enddate))) {
											i = 0;
										} else {
											totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
											adminSpend = adminSpend.add(monthlyAdminCost.multiply(new BigDecimal(3)));
											i = i + 3;
										}
									}
								}

								if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
					                if (startDate.isBefore(firstDayOfQuarter)) {
					                    startDate = firstDayOfQuarter;
					                }
					                if (enddate.isAfter(lastDayOfQuarter)) {
					                	enddate = lastDayOfQuarter;
					                }

					                long monthsInQuarter = ChronoUnit.MONTHS.between(startDate, enddate.plusDays(1));

					                totalSpend = totalSpend.add(monthlyCost.multiply(BigDecimal.valueOf(monthsInQuarter)));
					                adminSpend = adminSpend.add(monthlyAdminCost.multiply(BigDecimal.valueOf(monthsInQuarter)));
								}
							}
						}
						if (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
							LocalDate startDate = contract.getContractStartDate().toInstant()
									.atZone(ZoneId.systemDefault()).toLocalDate();
							long monthsBetweenTwoDates = ChronoUnit.MONTHS.between(startDate, lastDayOfQuarter);
							LocalDate localContractStart = contract.getContractStartDate().toInstant()
									.atZone(ZoneId.systemDefault()).toLocalDate();
							if ((int) monthsBetweenTwoDates > 0) {
								if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
									totalSpend = totalSpend.add(totalContractCost);
									adminSpend = adminSpend.add(totalAdminCost);
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
										LocalDate plusMonths = localContractStart.plusMonths(i);
										if (CommonUtil.convertLocalDatetoDate(plusMonths)
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
										LocalDate plusMonths = localContractStart.plusMonths(i);
										if (CommonUtil.convertLocalDatetoDate(plusMonths)
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
									totalSpend = totalSpend
											.add(monthlyCost.multiply(BigDecimal.valueOf(monthsBetweenTwoDates + 1)));
									adminSpend = adminSpend.add(
											monthlyAdminCost.multiply(BigDecimal.valueOf(monthsBetweenTwoDates + 1)));
								}

							} else {
								if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
									totalSpend = totalSpend.add(totalContractCost);
									adminSpend = adminSpend.add(totalAdminCost);
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
										LocalDate plusMonths = localContractStart.plusMonths(i);
										if (CommonUtil.convertLocalDatetoDate(plusMonths)
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
										LocalDate plusMonths = localContractStart.plusMonths(i);
										if (CommonUtil.convertLocalDatetoDate(plusMonths)
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
									totalSpend = totalSpend.add(monthlyCost);
									adminSpend = adminSpend.add(monthlyAdminCost);
								}

							}

						}

					}
				}
				if (ContractType.monthToMonth(contract.getContractType())) {
					if (contract.getContractStartDate()
							.compareTo(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)) == 0) {
						totalSpend = totalSpend.add(totalContractCost);
						adminSpend = adminSpend.add(totalAdminCost);
					}
					if (contract.getContractStartDate()
							.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
						totalSpend = totalSpend.add(totalContractCost);
						adminSpend = adminSpend.add(totalAdminCost);
					}
					if (contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
							&& contract.getContractStartDate()
									.before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
						totalSpend = totalSpend.add(totalContractCost);
						adminSpend = adminSpend.add(totalAdminCost);
					}
				}
			}
		}
		spends.put(Constant.TOTAL_SPEND, totalSpend);
		spends.put(Constant.ADMIN_SPEND, adminSpend);
		return spends;
	}

	@Override
	public CommonResponse dashboardSpendHistory() throws DataValidationException {
		List<DashboardSpendHistoryResponse> list = new ArrayList<>();
		LocalDate today = LocalDate.now();
		LocalDate yearstartDate = today.withDayOfMonth(1).minusMonths(11);
		LocalDate yearlastDate = today.withDayOfMonth(1).plusMonths(1).minusDays(1);
		List<ApplicationContractDetails> contractListBetweenDates = applicationContractDetailsRepository
				.findActiveConBtwConEndDate(CommonUtil.convertLocalDatetoDate(yearstartDate),
						CommonUtil.convertLocalDatetoDate(yearlastDate));
		for (ApplicationContractDetails contract : contractListBetweenDates) {
			BigDecimal totalContractCost = BigDecimal
					.valueOf(contract.getLicenseDetails().stream().mapToLong(p -> p.getTotalCost().longValue()).sum());
			BigDecimal totalAdminCost = BigDecimal.valueOf(
					contract.getLicenseDetails().stream().mapToLong(p -> p.getConvertedCost().longValue()).sum());
			if (ContractType.annual(contract.getContractType())) {
				BigDecimal monthlyCost = totalContractCost.divide(
						BigDecimal.valueOf(contract.getContractTenure()).multiply(BigDecimal.valueOf(12)), 2,
						RoundingMode.FLOOR);
				BigDecimal monthlyAdminCost = totalAdminCost.divide(
						BigDecimal.valueOf(contract.getContractTenure()).multiply(BigDecimal.valueOf(12)), 2,
						RoundingMode.FLOOR);
				LocalDate start = yearstartDate;
				LocalDate end = yearstartDate.plusMonths(1).minusDays(1);
				for (int i = 0; i < 12; i++) {
					int j = i;
					if (list.stream().filter(p -> p.getRefId() == j).collect(Collectors.toList()).isEmpty()) {
						List<String> app = new ArrayList<>();
						DashboardSpendHistoryResponse dashboardSpendHistoryResponse = new DashboardSpendHistoryResponse();
						Month month = start.getMonth();
						dashboardSpendHistoryResponse.setRefId(i);
						dashboardSpendHistoryResponse.setMonth(month.toString().substring(0, 3) + " "
								+ String.valueOf(start.getYear()).substring(2, 4));
						if (contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(start)) || contract
								.getContractStartDate().compareTo(CommonUtil.convertLocalDatetoDate(start)) == 0) {
							if ((contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(end))) &&
								 (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(start))
										|| contract.getContractEndDate()
												.compareTo(CommonUtil.convertLocalDatetoDate(start)) == 0)) {
									dashboardSpendHistoryResponse.setApplicationSpend(monthlyCost);
									dashboardSpendHistoryResponse.setAdminCost(monthlyAdminCost);
									app.add(contract.getApplicationId().getApplicationId());
								}
							
							if (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(end)) || contract
									.getContractEndDate().compareTo(CommonUtil.convertLocalDatetoDate(end)) == 0) {
								dashboardSpendHistoryResponse.setApplicationSpend(monthlyCost);
								dashboardSpendHistoryResponse.setAdminCost(monthlyAdminCost);
								app.add(contract.getApplicationId().getApplicationId());
							}
						}
						if ((contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(start))
								&& contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(end))) &&
							 (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(end)))) {
								dashboardSpendHistoryResponse.setApplicationSpend(monthlyCost);
								dashboardSpendHistoryResponse.setAdminCost(monthlyAdminCost);
								app.add(contract.getApplicationId().getApplicationId());
							}
						
						dashboardSpendHistoryResponse.setAppCount(app);
						dashboardSpendHistoryResponse.setApplicationCount(app.size());
						list.add(dashboardSpendHistoryResponse);
					} else {
						if (contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(start)) || contract
								.getContractStartDate().compareTo(CommonUtil.convertLocalDatetoDate(start)) == 0||(contract.getContractStartDate().compareTo(CommonUtil.convertLocalDatetoDate(end)) == 0)) {
							if ((contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(end))) &&
								 (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(start))
										|| contract.getContractEndDate()
												.compareTo(CommonUtil.convertLocalDatetoDate(start)) == 0)) {
									List<DashboardSpendHistoryResponse> listUpdate = list.stream()
											.filter(p -> p.getRefId() == j).collect(Collectors.toList());
									listUpdate.forEach(p -> {
										p.setApplicationSpend(p.getApplicationSpend().add(monthlyCost));
										p.setAdminCost(p.getAdminCost().add(monthlyAdminCost));
										List<String> app = p.getAppCount();
										if (!app.contains(contract.getApplicationId().getApplicationId())) {
											app.add(contract.getApplicationId().getApplicationId());
											p.setApplicationCount(app.size());
										}
									});
								}
							
							if (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(end)) || contract
									.getContractEndDate().compareTo(CommonUtil.convertLocalDatetoDate(end)) == 0) {
								List<DashboardSpendHistoryResponse> listUpdate = list.stream()
										.filter(p -> p.getRefId() == j).collect(Collectors.toList());
								listUpdate.forEach(p -> {
									List<String> app = p.getAppCount();
									p.setApplicationSpend(p.getApplicationSpend().add(monthlyCost));
									p.setAdminCost(p.getAdminCost().add(monthlyAdminCost));
									if (!app.contains(contract.getApplicationId().getApplicationId())) {
										app.add(contract.getApplicationId().getApplicationId());
										p.setApplicationCount(app.size());
									}
								});
							}
						}
						if (contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(start))
								&& contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(end))
								&& (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(end)))) {
							List<DashboardSpendHistoryResponse> listUpdate = list.stream()
									.filter(p -> p.getRefId() == j).collect(Collectors.toList());
							listUpdate.forEach(p -> {
								List<String> app = p.getAppCount();
								p.setApplicationSpend(p.getApplicationSpend().add(monthlyCost));
								p.setAdminCost(p.getAdminCost().add(monthlyAdminCost));
								if (!app.contains(contract.getApplicationId().getApplicationId())) {
									app.add(contract.getApplicationId().getApplicationId());
									p.setApplicationCount(app.size());
								}
							});

						}
					}
					start = start.plusMonths(1);
					end = start.plusMonths(1).minusDays(1);
				}

			}
			if (ContractType.monthToMonth(contract.getContractType())) {
				LocalDate start = yearstartDate;
				LocalDate end = yearstartDate.plusMonths(1).minusDays(1);
				for (int i = 0; i < 12; i++) {
					int j = i;
					if (list.stream().filter(p -> p.getRefId() == j).collect(Collectors.toList()).isEmpty()) {
						List<String> app = new ArrayList<>();
						DashboardSpendHistoryResponse dashboardSpendHistoryResponse = new DashboardSpendHistoryResponse();
						Month month = start.getMonth();
						dashboardSpendHistoryResponse.setRefId(i);
						dashboardSpendHistoryResponse.setMonth(month.toString().substring(0, 3) + " "
								+ String.valueOf(start.getYear()).substring(2, 4));
						if (contract.getContractStartDate().compareTo(CommonUtil.convertLocalDatetoDate(start)) == 0) {
							dashboardSpendHistoryResponse.setApplicationSpend(totalContractCost);
							dashboardSpendHistoryResponse.setAdminCost(totalAdminCost);
							app.add(contract.getApplicationId().getApplicationId());
						}
						if (contract.getContractStartDate().compareTo(CommonUtil.convertLocalDatetoDate(end)) == 0) {
							dashboardSpendHistoryResponse.setApplicationSpend(totalContractCost);
							dashboardSpendHistoryResponse.setAdminCost(totalAdminCost);
							app.add(contract.getApplicationId().getApplicationId());
						}
						if (contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(start))
								&& contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(end))) {
							dashboardSpendHistoryResponse.setApplicationSpend(totalContractCost);
							dashboardSpendHistoryResponse.setAdminCost(totalAdminCost);
							app.add(contract.getApplicationId().getApplicationId());
						}
						dashboardSpendHistoryResponse.setAppCount(app);
						dashboardSpendHistoryResponse.setApplicationCount(app.size());
						list.add(dashboardSpendHistoryResponse);
					} else {
						if (contract.getContractStartDate().compareTo(CommonUtil.convertLocalDatetoDate(start)) == 0) {
							List<DashboardSpendHistoryResponse> listUpdate = list.stream()
									.filter(p -> p.getRefId() == j).collect(Collectors.toList());
							listUpdate.forEach(p -> {
								p.setApplicationSpend(p.getApplicationSpend().add(totalContractCost));
								p.setAdminCost(p.getAdminCost().add(totalAdminCost));
								List<String> app = p.getAppCount();
								if (!app.contains(contract.getApplicationId().getApplicationId())) {
									app.add(contract.getApplicationId().getApplicationId());
									p.setApplicationCount(app.size());
								}
							});
						}
						if (contract.getContractStartDate().compareTo(CommonUtil.convertLocalDatetoDate(end)) == 0) {
							List<DashboardSpendHistoryResponse> listUpdate = list.stream()
									.filter(p -> p.getRefId() == j).collect(Collectors.toList());
							listUpdate.forEach(p -> {
								p.setApplicationSpend(p.getApplicationSpend().add(totalContractCost));
								p.setAdminCost(p.getAdminCost().add(totalAdminCost));
								List<String> app = p.getAppCount();
								if (!app.contains(contract.getApplicationId().getApplicationId())) {
									app.add(contract.getApplicationId().getApplicationId());
									p.setApplicationCount(app.size());
								}
							});
						}
						if (contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(start))
								&& contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(end))
								&& (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(end)))) {
							List<DashboardSpendHistoryResponse> listUpdate = list.stream()
									.filter(p -> p.getRefId() == j).collect(Collectors.toList());
							listUpdate.forEach(p -> {
								p.setApplicationSpend(p.getApplicationSpend().add(totalContractCost));
								p.setAdminCost(p.getAdminCost().add(totalAdminCost));
								List<String> app = p.getAppCount();
								if (!app.contains(contract.getApplicationId().getApplicationId())) {
									app.add(contract.getApplicationId().getApplicationId());
									p.setApplicationCount(app.size());
								}
							});

						}
					}
					start = start.plusMonths(1);
					end = start.plusMonths(1).minusDays(1);
				}
			}
		}
		for (DashboardSpendHistoryResponse lists : list) {
			List<String> strs = new ArrayList<>();
			lists.getAppCount().forEach(p -> {
				if (applicationDetailsRepo.findByApplicationId(p) != null) {
					strs.add(p);
				}
			});
			lists.setAppCount(strs);
			lists.setApplicationCount(strs.size());
		}
		return new CommonResponse(HttpStatus.OK, new Response("spendHistoryResponse", list),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse usageTrends() throws DataValidationException {
		List<DashboardUsageTrendResponse> usageTrendResponses = new ArrayList<>();
		LocalDate today = LocalDate.now();
		LocalDate yearstartDate = today.withDayOfMonth(1).minusMonths(11);
		LocalDate yearlastDate = today.withDayOfMonth(1).plusMonths(1).minusDays(1);
		List<ApplicationContractDetails> activeContracts = applicationContractDetailsRepository
				.getActiveConBtwConEndDate(CommonUtil.convertLocalDatetoDate(yearstartDate),
						CommonUtil.convertLocalDatetoDate(yearlastDate));
		for (ApplicationContractDetails contract : activeContracts) {
			LocalDate start = yearstartDate;
			LocalDate end = yearstartDate.plusMonths(1).minusDays(1);
			for (int i = 0; i < 12; i++) {
				int j = i;
				if (usageTrendResponses.stream().filter(p -> p.getRefId() == j).collect(Collectors.toList())
						.isEmpty()) {
					DashboardUsageTrendResponse dashboardSpendHistoryResponse = new DashboardUsageTrendResponse();
					List<String> app = new ArrayList<>();
					Month month = start.getMonth();
					dashboardSpendHistoryResponse.setRefId(i);
					dashboardSpendHistoryResponse.setMonth(
							month.toString().substring(0, 3) + " " + String.valueOf(start.getYear()).substring(2, 4));
					if (applicationDetailsRepo
							.findByApplicationId(contract.getApplicationId().getApplicationId()) != null) {
						if (contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(start)) || contract
								.getContractStartDate().compareTo(CommonUtil.convertLocalDatetoDate(start)) == 0) {
							if ((contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(end)))
								&& (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(start))
										|| contract.getContractEndDate()
												.compareTo(CommonUtil.convertLocalDatetoDate(start)) == 0)) {
									dashboardSpendHistoryResponse.setApplicationCount(1);
									dashboardSpendHistoryResponse
											.setUserCount(contract.getApplicationId().getUserDetails().size());
									app.add(contract.getApplicationId().getApplicationId());

								}
							
							if (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(end)) || contract
									.getContractEndDate().compareTo(CommonUtil.convertLocalDatetoDate(end)) == 0) {
								dashboardSpendHistoryResponse.setApplicationCount(1);
								dashboardSpendHistoryResponse
										.setUserCount(contract.getApplicationId().getUserDetails().size());
								app.add(contract.getApplicationId().getApplicationId());

							}
						}
						if (contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(start))
								&& contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(end))
								&& contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(end))) {
							dashboardSpendHistoryResponse.setApplicationCount(1);
							dashboardSpendHistoryResponse
									.setUserCount(contract.getApplicationId().getUserDetails().size());
							app.add(contract.getApplicationId().getApplicationId());
						}
					}
					dashboardSpendHistoryResponse.setAppCount(app);
					usageTrendResponses.add(dashboardSpendHistoryResponse);
				} else {
					if (applicationDetailsRepo
							.findByApplicationId(contract.getApplicationId().getApplicationId()) != null) {
						if (contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(start)) || contract
								.getContractStartDate().compareTo(CommonUtil.convertLocalDatetoDate(start)) == 0) {
							if ((contract.getContractEndDate().before(CommonUtil.convertLocalDatetoDate(end))) 
								&& (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(start))
										|| contract.getContractEndDate()
												.compareTo(CommonUtil.convertLocalDatetoDate(start)) == 0)) {
									List<DashboardUsageTrendResponse> listUpdate = usageTrendResponses.stream()
											.filter(p -> p.getRefId() == j).collect(Collectors.toList());
									listUpdate.forEach(p -> {
										List<String> app = p.getAppCount();
										if (!app.contains(contract.getApplicationId().getApplicationId())) {
											app.add(contract.getApplicationId().getApplicationId());
											p.setApplicationCount(app.size());
											p.setUserCount(p.getUserCount()
													+ contract.getApplicationId().getUserDetails().size());
										}
									});
								}
							
							if (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(end)) || contract
									.getContractEndDate().compareTo(CommonUtil.convertLocalDatetoDate(end)) == 0) {

								List<DashboardUsageTrendResponse> listUpdate = usageTrendResponses.stream()
										.filter(p -> p.getRefId() == j).collect(Collectors.toList());
								listUpdate.forEach(p -> {
									List<String> app = p.getAppCount();
									if (!app.contains(contract.getApplicationId().getApplicationId())) {
										app.add(contract.getApplicationId().getApplicationId());
										p.setApplicationCount(app.size());
										p.setUserCount(
												p.getUserCount() + contract.getApplicationId().getUserDetails().size());
									}
								});
							}
						}
						if (contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(start))
								&& contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(end))
								&& contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(end))) {
							List<DashboardUsageTrendResponse> listUpdate = usageTrendResponses.stream()
									.filter(p -> p.getRefId() == j).collect(Collectors.toList());
							listUpdate.forEach(p -> {
								List<String> app = p.getAppCount();
								if (!app.contains(contract.getApplicationId().getApplicationId())) {
									app.add(contract.getApplicationId().getApplicationId());
									p.setApplicationCount(app.size());
									p.setUserCount(
											p.getUserCount() + contract.getApplicationId().getUserDetails().size());
								}
							});
						}
					}
				}
				start = start.plusMonths(1);
				end = start.plusMonths(1).minusDays(1);
			}

		}
		return new CommonResponse(HttpStatus.OK, new Response("spendHistoryResponse", usageTrendResponses),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}
}