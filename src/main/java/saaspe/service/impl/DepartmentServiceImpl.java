package saaspe.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import saaspe.constant.Constant;
import saaspe.constant.ContractStatus;
import saaspe.constant.ContractType;
import saaspe.dto.MetricsDAO;
import saaspe.entity.ApplicationContractDetails;
import saaspe.entity.ApplicationDetails;
import saaspe.entity.ApplicationLicenseDetails;
import saaspe.entity.DepartmentDetails;
import saaspe.entity.DepartmentOnboarding;
import saaspe.entity.DepartmentOwnerDetails;
import saaspe.entity.Departments;
import saaspe.entity.ProjectDetails;
import saaspe.entity.ProjectManagerDetails;
import saaspe.entity.SequenceGenerator;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.entity.VerificationDetails;
import saaspe.exception.AuthenticationException;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.CreateDepartmentDetails;
import saaspe.model.CreateDepartmentOwnerDetails;
import saaspe.model.CreateDepartmentRequest;
import saaspe.model.DepartmentBudgetAnalytics;
import saaspe.model.DepartmentBudgetAnalyticsResponse;
import saaspe.model.DepartmentDetailsApplicationsResponse;
import saaspe.model.DepartmentDetailsOverviewResponse;
import saaspe.model.DepartmentDetailsUsersResponse;
import saaspe.model.DepartmentListView;
import saaspe.model.DepartmentOnboardingRequestDetailViewResponse;
import saaspe.model.DepartmentOnboardingReviewerListViewResponse;
import saaspe.model.DepartmentOverViewResponse;
import saaspe.model.DepartmentReviewerDetails;
import saaspe.model.DeptApplicationUsageAnalystics;
import saaspe.model.DeptOnboardingWorkFlowRequest;
import saaspe.model.DeptUserPasswordRequest;
import saaspe.model.LicenseUsersDetailsResponse;
import saaspe.model.ListOfCreateDeptartmentRequest;
import saaspe.model.NewApplicationOnboardingResposne;
import saaspe.model.ProjectListViewResponse;
import saaspe.model.Response;
import saaspe.model.departmentUpdateOwnershipRequest;
import saaspe.model.updateUserOwnershipRequest;
import saaspe.repository.ApplicationContractDetailsRepository;
import saaspe.repository.ApplicationDetailsRepository;
import saaspe.repository.ApplicationLicenseDetailsRepository;
import saaspe.repository.DepartmentOnboardingRepository;
import saaspe.repository.DepartmentOwnerRepository;
import saaspe.repository.DepartmentRepository;
import saaspe.repository.DepartmentsRepository;
import saaspe.repository.ProjectDetailsRepository;
import saaspe.repository.ProjectOwnerRepository;
import saaspe.repository.SequenceGeneratorRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.repository.UserLoginDetailsRepository;
import saaspe.repository.VerificationDetailsRepository;
import saaspe.service.DepartmentService;
import saaspe.service.UserDetailsService;
import saaspe.utils.CommonUtil;
import saaspe.utils.SecureUtils;

@Service
public class DepartmentServiceImpl implements DepartmentService {

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	ApplicationContractDetailsRepository applicationContractDetailsRepository;

	@Autowired
	SequenceGeneratorRepository sequenceGeneratorRepository;

	@Autowired
	private ApplicationContractDetailsRepository contractDetailsRepository;

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	private ApplicationLicenseDetailsRepository licenseDetailsRepository;

	@Autowired
	private DepartmentOnboardingRepository departmentOnboardingRepository;

	@Autowired
	private ApplicationDetailsRepository applicationDetailsRepository;

	@Autowired
	private UserLoginDetailsRepository userLoginDetailsRepository;

	@Autowired
	private VerificationDetailsRepository verificationDetailsRepository;

	@Autowired
	private ProjectDetailsRepository projectDetailsRepository;

	@Autowired
	private DepartmentOwnerRepository departmentOwnerRepository;

	@Autowired
	private ProjectOwnerRepository projectOwnerRepository;

	@Autowired
	private DepartmentsRepository departmentsRepository;
	
	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private Configuration config;
	

	@Value("${redirecturl.path}")
	private String redirectUrl;

	@Value("${sendgrid.domain.name}")
	private String mailDomainName;

	@Value("${sendgrid.domain.sendername}")
	private String senderName;

	@Value("${budget-mail.trigger.dev}")
	private boolean budgetMailTrigger;

	@Value("${logos.avatar.url}")
	private String avatarUrl;

	@Autowired
	UserDetailsService userDetailsService;

	public static final String SUCCESS_RESPONSE = "Details Retrieved successfully";

	@Override
	public CommonResponse getDepartmentListView() {
		List<DepartmentDetails> listofdept = departmentRepository.findAll();
		List<DepartmentListView> list = new ArrayList<>();
		for (DepartmentDetails details : listofdept) {
			BigDecimal totalCostYTD = BigDecimal.valueOf(0.0);
			BigDecimal adminTotalCostYTD = BigDecimal.valueOf(0.0);

			Integer count = 0;
			Integer size = 0;
			DepartmentListView departmentListView = new DepartmentListView();
			departmentListView.setDepartmentId(details.getDepartmentId());
			List<DepartmentOwnerDetails> deptOwners = departmentOwnerRepository
					.findByDepartmentIdAndEnddate(details.getDepartmentId());

			departmentListView.setDepartmentName(details.getDepartmentName());
			List<CreateDepartmentOwnerDetails> dept = new ArrayList<>();
			if (deptOwners.isEmpty()) {
				List<UserDetails> deptUsers = userDetailsRepository
						.getAllUsersByDepartmentId(details.getDepartmentId());
				deptUsers.stream().filter(q -> q.getEndDate() == null).limit(1).forEach(q -> {
					CreateDepartmentOwnerDetails departmentOwnerDetails = new CreateDepartmentOwnerDetails();
					departmentUpdateOwnershipRequest deptOwner = new departmentUpdateOwnershipRequest();
					deptOwner.setDepartmentId(details.getDepartmentId());
					deptOwner.setDepartmentOwnerEmail(q.getUserEmail());
					updateUserOwnershipRequest ownership = new updateUserOwnershipRequest();
					ownership.setDepartmentDetails(deptOwner);
					try {
						userDetailsService.getOwnerShipTransfer(ownership);
					} catch (DataValidationException | IOException | TemplateException | MessagingException e) {
						e.printStackTrace();
					}
					departmentOwnerDetails.setDepartmentOwnerEmailAddress(q.getUserEmail());
					departmentOwnerDetails.setDepartmentOwnerName(q.getUserName());
					dept.add(departmentOwnerDetails);
				});
			}
			deptOwners.stream().forEach(p -> {
				CreateDepartmentOwnerDetails departmentOwnerDetails = new CreateDepartmentOwnerDetails();
				departmentOwnerDetails.setDepartmentOwnerEmailAddress(p.getDepartmentOwnerEmail());
				departmentOwnerDetails.setDepartmentOwnerName(p.getDepartmentOwner());
				dept.add(departmentOwnerDetails);
			});

			if (details.getDepartmentAdmin() == null) {
				departmentListView.setOwnerDetails(dept);
			} else {
				List<CreateDepartmentOwnerDetails> deptO = new ArrayList<>();
				CreateDepartmentOwnerDetails createDepartmentOwnerDetails = new CreateDepartmentOwnerDetails();
				createDepartmentOwnerDetails.setDepartmentOwnerEmailAddress(details.getDepartmentAdmin());
				createDepartmentOwnerDetails.setDepartmentOwnerName(details.getDepartmentOwner());
				deptO.add(createDepartmentOwnerDetails);
				departmentListView.setOwnerDetails(deptO);
			}

			departmentListView.setUserLogo(avatarUrl);
			departmentListView.setBudgets(details.getBudget());
			if (details.getUserDetails() != null) {
				for (UserDetails userDetails : details.getUserDetails()) {
					if (userDetails.getEndDate() == null) {
						count = count + 1;
					}
				}
			}
			departmentListView.setProjectsCount(details.getProjectDetails().size());
			departmentListView.setNoOfUsers(count);
			BigDecimal totalCost = BigDecimal.valueOf(0);
			BigDecimal adminTotalCost = BigDecimal.valueOf(0);
			if (details.getApplicationId() != null) {
				for (ApplicationDetails applicationDetails : details.getApplicationId()) {
					for (ApplicationContractDetails contractDetails : applicationDetails.getContractDetails()) {
						if (ContractStatus.active(contractDetails.getContractStatus())
								|| ContractStatus.expired(contractDetails.getContractStatus())) {
							for (ApplicationLicenseDetails licenseDetails : contractDetails.getLicenseDetails()) {
								totalCost = totalCost.add(licenseDetails.getTotalCost(), MathContext.DECIMAL32);
								adminTotalCost = adminTotalCost.add(licenseDetails.getConvertedCost(),
										MathContext.DECIMAL32);
							}
						}
					}
					if (applicationDetails.getActiveContracts() != null) {
						size = size + 1;
					}
					totalCostYTD = totalCostYTD.add(getTotalCostYTD(LocalDate.now().withDayOfYear(1), LocalDate.now(),
							applicationDetails.getApplicationId()).get(Constant.TOTAL));
					adminTotalCostYTD = adminTotalCostYTD.add(getTotalCostYTD(LocalDate.now().withDayOfYear(1),
							LocalDate.now(), applicationDetails.getApplicationId()).get(Constant.ADMIN));
				}
			}
			departmentListView.setTotalSpendYTD(totalCostYTD);
			departmentListView.setAdminCostYTD(adminTotalCostYTD);
			departmentListView.setBudgetCurrency(details.getBudgetCurrency());
			departmentListView.setTotalSpend(totalCost);
			departmentListView.setAdminCost(adminTotalCost);
			departmentListView.setNoOfApps(size);
			list.add(departmentListView);
		}
		if (list == null || list.isEmpty()) {
			return new CommonResponse(HttpStatus.NOT_FOUND, new Response("DepartmentListView", list),
					"No departments found");

		}
		return new CommonResponse(HttpStatus.OK, new Response("DepartmentListView", list), SUCCESS_RESPONSE);
	}

	@Override
	public CommonResponse getDepartmentOverview(String departmentId, String request) throws DataValidationException {
		ZoneId defaultZoneId = ZoneId.systemDefault();
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfYear = today.with(TemporalAdjusters.firstDayOfYear());
		Date yearStartDate = Date.from(firstDayOfYear.atStartOfDay(defaultZoneId).toInstant());
		Date currentDate = Date.from(today.atStartOfDay(defaultZoneId).toInstant());
		DepartmentDetails departmentDetails = departmentRepository.findByDepartmentId(departmentId);
		List<ApplicationDetails> departmentApplicatoins = applicationDetailsRepository
				.findByDepartmentName(departmentDetails.getDepartmentName());
		MetricsDAO deptUserDetails = userDetailsRepository.findByDeparmentId(departmentId);
		DepartmentOverViewResponse response = new DepartmentOverViewResponse();
		if (departmentDetails != null) {
			if (StringUtils.isEmpty(request)) {
				Integer applicationCount = 0;
				Integer userCount = 0;
				String currency = null;
				if (deptUserDetails != null && deptUserDetails.getCount() != null) {
					userCount = deptUserDetails.getCount();
				}
				DepartmentDetailsOverviewResponse detailsOverviewResponse = new DepartmentDetailsOverviewResponse();
				detailsOverviewResponse.setDepartmentName(departmentDetails.getDepartmentName());
				detailsOverviewResponse.setDepartmentUserCount(userCount);
				BigDecimal depTotalAVSpend = BigDecimal.valueOf(0.0);
				BigDecimal depTotalAVSpendAdminCost = BigDecimal.valueOf(0.0);
				if (departmentDetails.getApplicationId() != null) {
					for (ApplicationDetails applicationDetails : departmentDetails.getApplicationId()) {
						if (applicationDetails.getActiveContracts() != null) {
							applicationCount = applicationCount + 1;
						}
					}
					for (ApplicationDetails app : departmentApplicatoins) {
						List<ApplicationContractDetails> listofactivecontracts = contractDetailsRepository
								.findActiveContractsByAppId(app.getApplicationId());
						for (ApplicationContractDetails contract : listofactivecontracts) {
							BigDecimal adminSpend = BigDecimal.valueOf(0.0);
							BigDecimal totalSpend = BigDecimal.valueOf(0.0);
							currency = contract.getContractCurrency();
							for (ApplicationLicenseDetails license : contract.getLicenseDetails()) {
								totalSpend = totalSpend.add(license.getTotalCost(), MathContext.DECIMAL32);
								adminSpend = adminSpend.add(license.getConvertedCost(), MathContext.DECIMAL32);
							}
							if (ContractType.annual(contract.getContractType())) {
								depTotalAVSpend = depTotalAVSpend
										.add(totalSpend.divide(BigDecimal.valueOf(contract.getContractTenure())
												.multiply(BigDecimal.valueOf(12)), 2, RoundingMode.FLOOR));
								depTotalAVSpendAdminCost = depTotalAVSpendAdminCost
										.add(adminSpend.divide(BigDecimal.valueOf(contract.getContractTenure())
												.multiply(BigDecimal.valueOf(12)), 2, RoundingMode.FLOOR));
							} else {
								depTotalAVSpend = depTotalAVSpend.add(totalSpend);
								depTotalAVSpendAdminCost = depTotalAVSpendAdminCost.add(adminSpend);
							}
						}
					}
				}
				detailsOverviewResponse.setDepartmentAvgMonthlySpend(depTotalAVSpend);
				detailsOverviewResponse.setDepartmentAvgMonthlyAdminCost(depTotalAVSpendAdminCost);
				detailsOverviewResponse.setDepartmentApplicationCount(applicationCount);
				detailsOverviewResponse.setCurrencySymbol(currency);
				detailsOverviewResponse.setProjectsCount(departmentDetails.getProjectDetails().size());
				response.setDepartmentOverviewResponse(detailsOverviewResponse);
			}
			if (!StringUtils.isEmpty(request) && request.equalsIgnoreCase("user")) {
				List<DepartmentDetailsUsersResponse> list = new ArrayList<>();
				List<DepartmentOwnerDetails> deptOwners = departmentOwnerRepository.findByDepartmentId(departmentId);
				List<UserDetails> userDetails = userDetailsRepository.getAllUsersByDepartmentId(departmentId);
				for (DepartmentOwnerDetails deptOwner : deptOwners) {
					if (userDetails.stream().filter(p -> p.getUserEmail().equals(deptOwner.getDepartmentOwnerEmail()))
							.collect(Collectors.toList()).isEmpty()) {
						UserDetails details = new UserDetails();
						details.setDepartmentId(departmentDetails);
						String nameUser = Constant.USER_0;
						Integer sequence = sequenceGeneratorRepository.getUserOnboardingSequence();
						nameUser = nameUser.concat(sequence.toString());
						SequenceGenerator updateSequence1 = sequenceGeneratorRepository.getById(1);
						updateSequence1.setUserOnboarding(++sequence);
						sequenceGeneratorRepository.save(updateSequence1);
						details.setUserId(nameUser);
						details.setCreatedOn(deptOwner.getCreatedOn());
						details.setJoiningDate(deptOwner.getCreatedOn());
						details.setUserEmail(deptOwner.getDepartmentOwnerEmail());
						details.setUserName(deptOwner.getDepartmentOwner());
						details.setCreatedBy(departmentDetails.getCreatedBy());
						details.setBuID(Constant.BUID_02);
						details.setLogoUrl(avatarUrl);
						details.setUserStatus(Constant.ACTIVE);
						details.setUserRole(Constant.CONTRIBUTOR);
						details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
						details.setEndDate(deptOwner.getEndDate());
						userDetailsRepository.save(details);
					}
				}
				for (UserDetails details : userDetails) {
					DepartmentDetailsUsersResponse detailsUsersResponse = new DepartmentDetailsUsersResponse();
					detailsUsersResponse.setUserApplicationCount(details.getApplicationId().size());
					detailsUsersResponse.setUserDesignation(details.getUserDesigination());
					detailsUsersResponse.setUserEmail(details.getUserEmail());
					detailsUsersResponse.setUserLogo(details.getLogoUrl());
					detailsUsersResponse.setUserName(details.getUserName());
					detailsUsersResponse.setUserStatus(details.getUserStatus());
					list.add(detailsUsersResponse);
				}
				response.setDepartmentusersResponse(list);
			}
			if (!StringUtils.isEmpty(request) && request.equalsIgnoreCase("application")) {
				List<DepartmentDetailsApplicationsResponse> list = new ArrayList<>();
				for (ApplicationDetails details : departmentDetails.getApplicationId()) {
					DepartmentDetailsApplicationsResponse applicationsResponse = new DepartmentDetailsApplicationsResponse();
					ApplicationDetails applicationDetails = applicationDetailsRepository
							.findByApplicationId(details.getApplicationId());
					if (applicationDetails.getActiveContracts() != null) {
						String currency = null;
						Integer userCount = 0;
						List<ApplicationContractDetails> applicationContractDetails = contractDetailsRepository
								.findContractsInCurrentYear(details.getApplicationId(), yearStartDate, currentDate);
						for (ApplicationContractDetails contract : applicationContractDetails) {
							for (ApplicationLicenseDetails license : contract.getLicenseDetails()) {
								currency = license.getCurrency();
							}
						}
						userCount = applicationDetails.getUserDetails().stream().filter(p -> p.getEndDate() == null)
								.collect(Collectors.toList()).size();
						applicationsResponse.setApplicationLogo(details.getLogoUrl());
						applicationsResponse.setApplicationName(details.getApplicationName());
						applicationsResponse.setApplicationStatus(details.getApplicationStatus());
						applicationsResponse.setCurrencySymbol(currency);
						applicationsResponse
								.setApplicationSpend(updatedGetTotalCostYTD(LocalDate.now().withDayOfYear(1),
										LocalDate.now(), details.getApplicationId()).get(Constant.TOTAL));
						applicationsResponse.setApplicationUserCount(userCount);
						applicationsResponse
								.setAdminApplicationSpend(updatedGetTotalCostYTD(LocalDate.now().withDayOfYear(1),
										LocalDate.now(), details.getApplicationId()).get(Constant.ADMIN));
						list.add(applicationsResponse);
					}
				}
				response.setDepartmentapplicationsResponse(list);
			}
			if (!StringUtils.isEmpty(request) && request.equalsIgnoreCase("project")) {
				List<ProjectListViewResponse> projectListView = new ArrayList<>();
				List<ProjectDetails> projectDetails = projectDetailsRepository.getProjectsByDeptId(departmentId);
				for (ProjectDetails details : projectDetails) {
					List<ProjectManagerDetails> ownerDetails = projectOwnerRepository
							.findByProjectIdandEndDate(details.getProjectId());
					ProjectListViewResponse listViewResponse = new ProjectListViewResponse();
					BigDecimal costPerLicense = BigDecimal.valueOf(0);
					BigDecimal adminCostPerLicense = BigDecimal.valueOf(0);
					String currency = "USD";
					for (ApplicationDetails applicationDetails : details.getApplicationId()) {
						if (applicationDetails.getActiveContracts() != null) {
							List<ApplicationContractDetails> contractDetails = contractDetailsRepository
									.findByApplicationId(applicationDetails.getApplicationId());
							for (ApplicationContractDetails contract : contractDetails) {
								for (ApplicationLicenseDetails license : contract.getLicenseDetails()) {
									if (license != null) {
										costPerLicense = costPerLicense.add(license.getTotalCost(),
												MathContext.DECIMAL32);
										adminCostPerLicense = adminCostPerLicense.add(license.getConvertedCost(),
												MathContext.DECIMAL32);
										currency = license.getCurrency();
									}
								}
							}
						}
					}

					if (details.getProjectManager() != null) {
						listViewResponse.setProjectManagerEmail(Arrays.asList(details.getProjectManager()));
					} else {
						List<String> owners = ownerDetails.stream().map(ProjectManagerDetails::getProjectManagerEmail)
								.collect(Collectors.toList());
						listViewResponse.setProjectManagerEmail(owners);
					}
					listViewResponse.setProjectId(details.getProjectId());
					listViewResponse.setProjectName(details.getProjectName());
					listViewResponse.setProjectStartDate(details.getStartDate());
					listViewResponse.setProjectEndDate(details.getEndDate());
					listViewResponse.setProjectStartDate(details.getStartDate());
					listViewResponse.setProjctBudget(details.getProjectBudget());
					listViewResponse.setProjctBudgetCurrency(details.getBudgetCurrency());
					listViewResponse.setProjectCost(costPerLicense);
					listViewResponse.setAdminCost(adminCostPerLicense);
					listViewResponse.setCurrency(currency);
					projectListView.add(listViewResponse);
				}
				response.setProjectDepartmentResponse(projectListView);
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("DepartmentOverview", response), SUCCESS_RESPONSE);
	}

	private Map<String, BigDecimal> getTotalCostYTD(LocalDate firstDayOfQuarter, LocalDate lastDayOfQuarter,
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

				if ((contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)))
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

	private Map<String, BigDecimal> updatedGetTotalCostYTD(LocalDate firstDayOfQuarter, LocalDate lastDayOfQuarter,
			String applicationId) {
		Map<String, BigDecimal> spend = new HashMap<>();
		BigDecimal totalSpend = new BigDecimal(0);
		BigDecimal adminSpend = new BigDecimal(0);
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
				if ((contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)))
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
		spend.put(Constant.TOTAL, totalSpend);
		spend.put(Constant.ADMIN, adminSpend);
		return spend;
	}

	private List<String> departmentExcelValidation(MultipartFile departmentFile)
			throws DataValidationException, IOException {
		XSSFWorkbook workbook = null;
		List<String> errors = new ArrayList<>();
		List<String> data = new ArrayList<>();
		try {
			workbook = new XSSFWorkbook(departmentFile.getInputStream());
		} catch (IOException e) {
			throw new IOException(e.getMessage());
		}
		XSSFSheet worksheet = workbook.getSheetAt(0);
		int index = 0;
		int cellConstant = 0;
		List<String> columnNames = new ArrayList<>();
		for (Row cellRow : worksheet) {
			if (cellRow.getPhysicalNumberOfCells() >= 1) {
				if (cellRow.getRowNum() == 0) {
					cellConstant = cellRow.getPhysicalNumberOfCells();
				}
				if (cellRow.getRowNum() == 0
						&& !cellRow.getCell(0).getStringCellValue().trim().equalsIgnoreCase("Department Name")) {
					workbook.close();
					throw new DataValidationException("Seems Your Uploading Wrong Excel file Please Check", null, null);
				}
				for (int cell = 0; cell < cellConstant; cell++) {
					if (cell < 7) {
						if (cellRow.getRowNum() == 0) {
							columnNames.add(cellRow.getCell(cell).getStringCellValue().trim());
						}
						if ((cellRow.getCell(cell) == null || cellRow.getCell(cell).getStringCellValue().length() == 0)
								&& ((cell != 3 && cell != 4) || (cell == 3
										&& !columnNames.get(cell).trim().equalsIgnoreCase("Secondary Owner Name"))
										|| (cell == 4 && !columnNames.get(cell).trim()
												.equalsIgnoreCase("Secondary Owner Email Address")))) {
							errors.add("Null value in " + columnNames.get(cell) + " at " + cellRow.getRowNum());

						}
					}
				}
				index++;
			}
		}
		data.add("Please Enter the Data!");
		if (index == 1) {
			return data;
		}
		return errors;
	}

	@SuppressWarnings("resource")
	@Override
	@Transactional
	public CommonResponse saveDepartmentOnboarding(MultipartFile departmentFile, UserLoginDetails profile)
			throws IOException, DataValidationException {
		List<String> erros = new ArrayList<>();
		List<String> depatmentEmails = new ArrayList<>();
		NewApplicationOnboardingResposne applicationOnboardingResposne = new NewApplicationOnboardingResposne();
		Integer childNum = 1;
		String request = "REQ_DPT_0";
		Integer deptReqSequence = sequenceGeneratorRepository.getDeptReqSequence();
		SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
		request = request.concat(deptReqSequence.toString());

		XSSFWorkbook workbook = new XSSFWorkbook(departmentFile.getInputStream());
		XSSFSheet worksheet = workbook.getSheetAt(0);
		List<DepartmentOnboarding> list = new ArrayList<>();
		List<DepartmentOwnerDetails> departmentOwnerDetails = new ArrayList<>();
		List<String> departmentNames = new ArrayList<>();
		try {
			erros.addAll(departmentExcelValidation(departmentFile));
		} catch (DataValidationException e) {
			throw new DataValidationException(e.getMessage(), request, null);
		}
		if (!erros.isEmpty()) {
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response("ExcelsUploadDepartmentResponse", erros),
					"Excel Upload Failed");
		}
		Set<String> uniqueOwnerEmails = new HashSet<>();
		Set<String> secondrayOwnerEmails = new HashSet<>();
		String previousDepartmentSecOwnerEmail = null;
		int rowNumber = 0;
		for (Row cellRow : worksheet) {
			if (cellRow.getPhysicalNumberOfCells() > 1 && rowNumber > 0) {
				departmentNames.add(cellRow.getCell(0).getStringCellValue().trim());
				depatmentEmails.add(cellRow.getCell(2).getStringCellValue().trim());
				String departmentOwnerEmail = cellRow.getCell(2).getStringCellValue().trim();
				if (uniqueOwnerEmails.contains(departmentOwnerEmail)
						|| secondrayOwnerEmails != null && secondrayOwnerEmails.contains(departmentOwnerEmail)) {
					erros.add("Duplicate Department Owner Email found in Row " + cellRow.getRowNum());

				} else {
					uniqueOwnerEmails.add(departmentOwnerEmail);
				}
				if (previousDepartmentSecOwnerEmail != null
						&& previousDepartmentSecOwnerEmail.equalsIgnoreCase(departmentOwnerEmail)) {
					erros.add(
							"Previous Row's Secondary Owner email matches current Row's Department Owner email in Row "
									+ cellRow.getRowNum());
				}
				Set<Integer> processedRows = new HashSet<>();

				if (cellRow.getCell(1) != null && cellRow.getCell(1).getCellType() == CellType.STRING) {
					String name = cellRow.getCell(1).getStringCellValue().trim();
					if (!isValidName(name) && processedRows.add(cellRow.getRowNum())) {
						erros.add("Invalid Name format in Row " + cellRow.getRowNum());
					}
				}

				if (cellRow.getCell(2) != null && cellRow.getCell(2).getCellType() == CellType.STRING) {
					String email = cellRow.getCell(2).getStringCellValue().trim();
					if (!isValidEmail(email) && processedRows.add(cellRow.getRowNum())) {
						erros.add("Invalid Email format in Row " + cellRow.getRowNum());
					}
				}

				if (cellRow.getCell(3) != null && cellRow.getCell(3).getCellType() == CellType.STRING) {
					String name = cellRow.getCell(3).getStringCellValue().trim();
					if (!isValidName(name) && processedRows.add(cellRow.getRowNum())) {
						erros.add("Invalid Name format in Row " + cellRow.getRowNum());
					}
				}

				if (cellRow.getCell(4) != null && cellRow.getCell(4).getCellType() == CellType.STRING) {
					String email = cellRow.getCell(4).getStringCellValue().trim();
					if (!isValidEmail(email) && processedRows.add(cellRow.getRowNum())) {
						erros.add("Invalid Email format in Row " + cellRow.getRowNum());
					}
				}
				if (cellRow.getCell(4) != null && cellRow.getCell(4).getCellType() == CellType.STRING) {
					String departmentSecOwnerEmail = cellRow.getCell(4).getStringCellValue().trim();
					if (cellRow.getCell(2).getStringCellValue().trim().equalsIgnoreCase(departmentSecOwnerEmail)) {
						erros.add("Department Owner email should be different in Row " + cellRow.getRowNum());
					}
					if (secondrayOwnerEmails.contains(departmentSecOwnerEmail)) {
						erros.add("Duplicate Department Secondray Owner Email found in Row " + cellRow.getRowNum());
					} else {
						secondrayOwnerEmails.add(departmentSecOwnerEmail);
					}
					previousDepartmentSecOwnerEmail = departmentSecOwnerEmail;
				}
				rowNumber++;
			}
		}
		Set<String> set = new HashSet<>(departmentNames);
		List<String> newlist = new ArrayList<>(set);
		if (departmentNames.size() != newlist.size()) {
			erros.add("Duplicate Department Names Found in Bulk Upload");
		}
		depatmentEmails.forEach(p -> {
			DepartmentDetails departmentDetails = departmentRepository.findByDeptOwnerEmail(p);
			UserDetails userDetails = userDetailsRepository.findByuserEmailAndDepartment(p);
			UserDetails adminCheck = userDetailsRepository.findByuserEmailAndRole(p);
			if (adminCheck != null) {
				erros.add("Please enter valid user detail for the department owner.");
			}
			if (userDetails != null) {
				DepartmentDetails dept = departmentRepository
						.findByDepartmentId(userDetails.getDepartmentId().getDepartmentId());
				erros.add(" The Person " + p + " is already part of " + dept.getDepartmentName()
						+ " Department Please provide different details for the owner.");
			}
			if (departmentDetails != null) {
				erros.add("The person " + p + " is already an owner for the " + departmentDetails.getDepartmentName()
						+ " Department. Please provide different details for the owners.");
			}

		});

		Set<String> emailsSet = new HashSet<>(depatmentEmails);
		List<String> newEmailSet = new ArrayList<>(emailsSet);
		if (depatmentEmails.size() != newEmailSet.size()) {
			erros.add("Duplicate Department Owner Email Found User will be part of only one Department");
		}
		if (!erros.isEmpty()) {
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response("ExcelUploadsDepartmentResponse", erros),
					"Excel Upload Failed");
		}
		int index = 0;
		for (Row cellRow : worksheet) {
			if ((cellRow.getPhysicalNumberOfCells() > 1) && (index > 0)) {
				String childRequestNum = null;
				DepartmentOnboarding onboarding = new DepartmentOnboarding();
				childRequestNum = request.concat("_0" + childNum);
				childNum++;
				XSSFRow row = worksheet.getRow(index);
				Integer i = 0;
				List<DepartmentOnboarding> lists = departmentOnboardingRepository
						.findByDepartmentName(row.getCell(0).getStringCellValue().trim());
				List<DepartmentOnboarding> departmentOnboardings = getDepartmentStatus(lists);
				List<DepartmentOnboarding> apps = departmentOnboardings.stream()
						.filter(p -> p.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW)
								|| p.getApprovedRejected().equalsIgnoreCase(Constant.APPROVE))
						.collect(Collectors.toList());
				if (apps.isEmpty()) {
					DepartmentOnboarding depOwnerDesc = departmentOnboardingRepository
							.findByOwnerEmailDAndApprovedReject(row.getCell(2).getStringCellValue().trim(),
									Constant.REVIEW);

					if (depOwnerDesc == null) {
						onboarding.setDepartmentName(row.getCell(0).getStringCellValue().trim());
						onboarding.setDepartmentOwner(row.getCell(1).getStringCellValue().trim());
						onboarding.setDepartmentOwnerEmail(row.getCell(2).getStringCellValue().trim());
						BigDecimal budget = new BigDecimal(row.getCell(5).getStringCellValue().trim());
						DepartmentOwnerDetails departmentOwnerDetail = new DepartmentOwnerDetails();
						departmentOwnerDetail.setDepartmentName(row.getCell(0).getStringCellValue().trim());
						departmentOwnerDetail.setDepartmentOwner(row.getCell(1).getStringCellValue().trim());
						departmentOwnerDetail.setDepartmentOwnerEmail(row.getCell(2).getStringCellValue().trim());
						departmentOwnerDetail.setIsOnboarding(true);
						departmentOwnerDetail.setPriority(1);
						departmentOwnerDetail.setCreatedOn(new Date());
						departmentOwnerDetails.add(departmentOwnerDetail);

						if (cellRow.getCell(3) != null && cellRow.getCell(3).getCellType() == CellType.STRING
								|| cellRow.getCell(4) != null && cellRow.getCell(4).getCellType() == CellType.STRING) {
							DepartmentOwnerDetails secondaryOwnerDetail = new DepartmentOwnerDetails();
							secondaryOwnerDetail.setDepartmentName(row.getCell(0).getStringCellValue().trim());
							secondaryOwnerDetail.setDepartmentOwner(row.getCell(3).getStringCellValue().trim());
							secondaryOwnerDetail.setDepartmentOwnerEmail(row.getCell(4).getStringCellValue().trim());
							secondaryOwnerDetail.setIsOnboarding(true);
							secondaryOwnerDetail.setPriority(2);
							secondaryOwnerDetail.setCreatedOn(new Date());
							departmentOwnerDetails.add(secondaryOwnerDetail);
						}

						onboarding.setBudgetCurrency(row.getCell(6).getStringCellValue().trim());
						onboarding.setBudget(budget);
						onboarding.setCreatedOn(new Date());
						onboarding.setBuID(Constant.BUID);
						onboarding.setOnboardByUserEmail(profile.getEmailAddress());
						onboarding.setOpID(Constant.SAASPE);
						onboarding.setApprovedRejected(Constant.REVIEW);
						onboarding.setOnboardingStatus(Constant.PENDING_WITH_REVIEWER);
						onboarding.setWorkGroup(Constant.REVIEWER);
						onboarding.setWorkGroupUserEmail(profile.getEmailAddress());
						onboarding.setRequestNumber(request);
						if (worksheet.getPhysicalNumberOfRows() != 1) {
							onboarding.setChildRequestNumber(childRequestNum);
						}
						list.add(onboarding);
						i = i + 1;
					} else {
						return new CommonResponse(HttpStatus.CONFLICT,
								new Response("ExcelUploadDepartmentsResponse", Arrays.asList(
										"Department owner already having another Department It is in Review or Approved")),
								"Department Excel Upload Failed");
					}
				}
				if (i == 0) {
					return new CommonResponse(HttpStatus.OK,
							new Response("ExcelUploadsDepartmentsResponse", Arrays.asList(
									"Department with Name " + row.getCell(0).getStringCellValue() + " Already Exists")),
							"Department Excel Upload Failed");
				}
			}

			if (worksheet.getPhysicalNumberOfRows() - 1 == index && erros.isEmpty()) {
				for (DepartmentOnboarding departmentOnboarding : list) {
					String name = Constant.DEPARTMENT_ID;
					Integer deptseq = sequenceGeneratorRepository.getDepartmentSequence();
					SequenceGenerator upate = sequenceGeneratorRepository.getById(1);
					name = name.concat(deptseq.toString());
					upate.setDepartmentSequence(++deptseq);
					departmentOnboarding.setDepartmentId(name);

					List<DepartmentOwnerDetails> idset = departmentOwnerDetails.stream().filter(
							p -> p.getDepartmentName().equalsIgnoreCase(departmentOnboarding.getDepartmentName()))
							.collect(Collectors.toList());
					for (DepartmentOwnerDetails id : idset) {
						id.setDeptId(name);
						departmentOwnerRepository.save(id);
					}
					sequenceGeneratorRepository.save(upate); 
					departmentOnboardingRepository.save(departmentOnboarding);
					
					Departments department = new Departments();
					department.setDepartmentId(departmentOnboarding.getDepartmentId());
					department.setDepartmentName(departmentOnboarding.getDepartmentName());
					department.setDepartmentOwner(departmentOnboarding.getDepartmentOwner());
					department.setDepartmentOwnerEmail(departmentOnboarding.getDepartmentOwnerEmail());
					department.setBudget(departmentOnboarding.getBudget());
					department.setBudgetCurrency(departmentOnboarding.getBudgetCurrency());
					department.setIsOnboarding(true);
					department.setDepartmentCreatedOn(new Date());
					department.setDepartmentCreatedBy(profile.getEmailAddress());
					department.setDepartmentOwnerCreatedOn(new Date());
					department.setBuID(Constant.BUID);
					department.setOpID(Constant.SAASPE);
					department.setPriority(1);
					departmentsRepository.save(department);        
				        
				}
				updateSequence.setDeptRequestId(++deptReqSequence);
				sequenceGeneratorRepository.save(updateSequence);
			}
			index++;
		}
		applicationOnboardingResposne.setRequestId(request);
		return new CommonResponse(HttpStatus.CREATED,
				new Response("ExcelsUploadDepartmentResponses", applicationOnboardingResposne),
				"Deparment Excel Upload successfully");
	}

	private boolean isValidName(String name) {
		return name.matches("^[a-zA-Z\\s]+$");
	}

	private boolean isValidEmail(String email) {
		return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
	}

	@Override
	@Transactional
	public CommonResponse departmentSingleOnboarding(ListOfCreateDeptartmentRequest createDeptartmentRequest,
			UserLoginDetails profile) throws DataValidationException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		Integer childNum = 1;
		String request = "REQ_DPT_0";
		String name = Constant.DEPARTMENT_ID;
		Integer deptReqsequence = sequenceGeneratorRepository.getDeptReqSequence();
		NewApplicationOnboardingResposne onboardingResposne = new NewApplicationOnboardingResposne();
		SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
		request = request.concat(deptReqsequence.toString());
		Integer i = 0;
		for (CreateDepartmentRequest createDepartmentRequest : createDeptartmentRequest.getCreateDepartmentRequest()) {

			List<DepartmentOnboarding> list = departmentOnboardingRepository
					.findByDepartmentName(createDepartmentRequest.getDepartmentInfo().getDepartmentName());
			List<DepartmentOnboarding> departmentOnboardings = getDepartmentStatus(list);
			List<DepartmentOnboarding> apps = departmentOnboardings.stream()
					.filter(p -> p.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW)
							|| p.getApprovedRejected().equalsIgnoreCase(Constant.APPROVE))
					.collect(Collectors.toList());
			if (departmentRepository
					.findByDepartmentName(createDepartmentRequest.getDepartmentInfo().getDepartmentName()) != null) {
				throw new DataValidationException("Department with "
						+ createDepartmentRequest.getDepartmentInfo().getDepartmentName() + " Already exists", null,
						null);
			}
			if (!Constant.CURRENCY.contains(createDepartmentRequest.getDepartmentInfo().getCurrencyCode())) {
				throw new DataValidationException("Currency code is not matching with the existing list", null, null);
			}
			List<String> departmentOwnerEmailAddress = createDepartmentRequest.getDepartmentInfo().getOwnerDetails()
					.stream().map(CreateDepartmentOwnerDetails::getDepartmentOwnerEmailAddress)
					.collect(Collectors.toList());

			List<UserDetails> duplicateUsers = userDetailsRepository
					.findByDepartmentOwnerEmailAddressIn(departmentOwnerEmailAddress);
			if (!duplicateUsers.isEmpty()) {
				throw new DataValidationException("The given user already exists", null, null);
			}

			Set<String> nonduplicate = new HashSet<>(departmentOwnerEmailAddress);

			if (departmentOwnerEmailAddress.size() != nonduplicate.size()) {
				throw new DataValidationException("duplicate owner email address found ", null, null);
			}
			int adminCheck = 0;
			int depOwnerDesc = 0;
			for (CreateDepartmentOwnerDetails ownerDetails : createDepartmentRequest.getDepartmentInfo()
					.getOwnerDetails()) {
				if (departmentOnboardingRepository.findByOwnerEmailDAndApprovedReject(
						ownerDetails.getDepartmentOwnerEmailAddress(), Constant.REVIEW) == null) {
					depOwnerDesc++;
				}
				DepartmentDetails departmentDetails = departmentRepository
						.findByDeptOwnerEmail(ownerDetails.getDepartmentOwnerEmailAddress());

				UserDetails userDetailswithRoleanDeptId = userDetailsRepository
						.findByuserEmailAndDepartment(ownerDetails.getDepartmentOwnerEmailAddress());
				if (userDetailsRepository
						.findByuserEmailAndRole(ownerDetails.getDepartmentOwnerEmailAddress()) != null) {
					adminCheck++;
				}
				if (departmentDetails != null) {
					throw new DataValidationException("The  person " + ownerDetails.getDepartmentOwnerEmailAddress()
							+ " is already an owner for the " + departmentDetails.getDepartmentName()
							+ " Department. Please provide different details for the owner.", null, null);
				}
				if (userDetailswithRoleanDeptId != null) {
					DepartmentDetails dept = departmentRepository
							.findByDepartmentId(userDetailswithRoleanDeptId.getDepartmentId().getDepartmentId());
					throw new DataValidationException("The person  " + ownerDetails.getDepartmentOwnerEmailAddress()
							+ " is already part of " + dept.getDepartmentName()
							+ " Department. Please provide different details for the owner.", null, null);
				}
			}

			if (adminCheck > 0) {
				throw new DataValidationException("Please enter valid user details for the department owner.", null,
						null);
			}

			if (apps.isEmpty()) {
				if (depOwnerDesc > 0) {
					DepartmentOnboarding departmentOnboarding = new DepartmentOnboarding();
					Integer deptseq = sequenceGeneratorRepository.getDepartmentSequence();
					name = name.concat(deptseq.toString());
					String childRequestNum = null;
					childRequestNum = request.concat("_0" + childNum);
					childNum++;
					departmentOnboarding
							.setDepartmentName(createDepartmentRequest.getDepartmentInfo().getDepartmentName().trim());
					departmentOnboarding.setRequestNumber(request);
					departmentOnboarding.setCreatedOn(new Date());
					departmentOnboarding.setBuID(Constant.BUID);
					departmentOnboarding
							.setBudgetCurrency(createDepartmentRequest.getDepartmentInfo().getCurrencyCode());
					departmentOnboarding.setCreatedBy(profile.getEmailAddress().trim());
					departmentOnboarding.setBudget(createDepartmentRequest.getDepartmentInfo().getDepartmentBudget());
					departmentOnboarding.setOpID(Constant.SAASPE);
					departmentOnboarding.setWorkGroup(Constant.REVIEWER);
					departmentOnboarding.setApprovedRejected(Constant.REVIEW);
					departmentOnboarding.setWorkGroupUserEmail(profile.getEmailAddress().trim());
					departmentOnboarding.setOnboardByUserEmail(profile.getEmailAddress().trim());
					departmentOnboarding.setOnBoardDate(new Date());
					departmentOnboarding.setOnboardingStatus(Constant.PENDING_WITH_REVIEWER);
					departmentOnboarding.setDepartmentId(name);

					int j = 1;
					for (CreateDepartmentOwnerDetails ownerDetail : createDepartmentRequest.getDepartmentInfo()
							.getOwnerDetails()) {
						DepartmentOwnerDetails ownerDetails = new DepartmentOwnerDetails();
						ownerDetails.setCreatedOn(new Date());
						ownerDetails.setDepartmentName(createDepartmentRequest.getDepartmentInfo().getDepartmentName());
						ownerDetails.setDepartmentOwnerEmail(ownerDetail.getDepartmentOwnerEmailAddress());
						ownerDetails.setDepartmentOwner(ownerDetail.getDepartmentOwnerName());
						ownerDetails.setDeptId(name);
						ownerDetails.setIsOnboarding(true);
						ownerDetails.setPriority(j);
						departmentOwnerRepository.save(ownerDetails);
						
						
						
						Departments department = new Departments();
						department.setDepartmentId(name);
						department.setDepartmentName(createDepartmentRequest.getDepartmentInfo().getDepartmentName().trim());
						department.setDepartmentOwner(createDepartmentRequest.getDepartmentInfo().getOwnerDetails().get(0).getDepartmentOwnerName());
						department.setDepartmentOwnerEmail(createDepartmentRequest.getDepartmentInfo().getOwnerDetails().get(0).getDepartmentOwnerEmailAddress());
						department.setBudget(createDepartmentRequest.getDepartmentInfo().getDepartmentBudget());
						department.setBudgetCurrency(createDepartmentRequest.getDepartmentInfo().getCurrencyCode());
						department.setIsOnboarding(true); 
						department.setDepartmentCreatedOn(new Date());
						department.setDepartmentCreatedBy(profile.getEmailAddress().trim()); 
						department.setPriority(j);
						department.setBuID(Constant.BUID);
						department.setOpID(Constant.SAASPE);
						department.setDepartmentOwnerCreatedOn(new Date());
						departmentsRepository.save(department);
						
						j++;
					}
					departmentOnboardingRepository.save(departmentOnboarding);
					updateSequence.setDepartmentSequence(++deptseq);
					sequenceGeneratorRepository.save(updateSequence);
					
					


					if (createDeptartmentRequest.getCreateDepartmentRequest().size() != 1) {
						departmentOnboarding.setChildRequestNumber(childRequestNum);
					}
					++i;
					Integer size = createDeptartmentRequest.getCreateDepartmentRequest().size();
					if (Objects.equals(size, i)) {
						updateSequence.setDeptRequestId(++deptReqsequence);
						sequenceGeneratorRepository.save(updateSequence);
					}
					onboardingResposne.setRequestId(request);
					response.setData(onboardingResposne);
					response.setAction("createDepartmentsResponse");
					commonResponse.setStatus(HttpStatus.CREATED);
					commonResponse.setMessage("Onboarding request submitted");
					commonResponse.setResponse(response);
				} else {
					response.setData("");
					response.setAction("createsDepartmentResponse");
					commonResponse.setStatus(HttpStatus.CONFLICT);
					commonResponse.setMessage(
							"Department owner already having another Department It is in Review or Approved");
					commonResponse.setResponse(response);
				}
			} else {
				response.setData("");
				response.setAction("createDepartmentResponses");
				commonResponse.setStatus(HttpStatus.CONFLICT);
				commonResponse.setMessage(
						"Department are in the Stage of Review Or Already Exists in Department Please Check");
				commonResponse.setResponse(response);
			}
		}
		return commonResponse;
	}

	@Override
	public CommonResponse departmentReviewerApproverListView(UserLoginDetails profile) {
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
			List<DepartmentOnboardingReviewerListViewResponse> listOfDept = getListOfDepartment(Constant.REVIEWER,
					Constant.REVIEW);
			response.setData(listOfDept);
			response.setAction("Reviewer List view Response");
			commonResponse.setStatus(HttpStatus.OK);
			commonResponse.setMessage(SUCCESS_RESPONSE);
			commonResponse.setResponse(response);
			return commonResponse;
		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER)) {
			List<DepartmentOnboardingReviewerListViewResponse> listOfDept = getListOfDepartment(Constant.APPROVER,
					Constant.REVIEW);
			response.setData(listOfDept);
			response.setAction("Approver List View Response");
			commonResponse.setStatus(HttpStatus.OK);
			commonResponse.setMessage(SUCCESS_RESPONSE);
			commonResponse.setResponse(response);
			return commonResponse;
		}
		List<DepartmentOnboarding> departmentOnboarding = departmentOnboardingRepository.findAllBySuperAdminListView();
		List<DepartmentOnboardingReviewerListViewResponse> list = new ArrayList<>();
		for (DepartmentOnboarding deptonboarding : departmentOnboarding) {
			DepartmentOnboardingReviewerListViewResponse listViewResponse = new DepartmentOnboardingReviewerListViewResponse();
			listViewResponse.setDepartmentName(deptonboarding.getDepartmentName());
			listViewResponse.setOnBoardedByEmail(deptonboarding.getOnboardByUserEmail());
			listViewResponse.setRequestId(deptonboarding.getRequestNumber());
			listViewResponse.setOnboardingStatus(deptonboarding.getOnboardingStatus());
			listViewResponse.setChildRequestId(deptonboarding.getChildRequestNumber());
			listViewResponse.setReviewedByEmail(deptonboarding.getWorkGroupUserEmail());
			if (deptonboarding.getChildRequestNumber() != null) {
				listViewResponse.setChildRequestId(deptonboarding.getChildRequestNumber());
			}
			list.add(listViewResponse);
		}
		return new CommonResponse(HttpStatus.OK, new Response("DepartmentListViewResponse", list),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private List<DepartmentOnboardingReviewerListViewResponse> getListOfDepartment(String role, String key) {
		List<DepartmentOnboarding> departmentOnboarding = departmentOnboardingRepository.getAllByName(role, key);
		List<DepartmentOnboardingReviewerListViewResponse> list = new ArrayList<>();
		for (DepartmentOnboarding deptonboarding : departmentOnboarding) {
			DepartmentOnboardingReviewerListViewResponse listViewResponse = new DepartmentOnboardingReviewerListViewResponse();
			listViewResponse.setDepartmentName(deptonboarding.getDepartmentName());
			listViewResponse.setOnBoardedByEmail(deptonboarding.getOnboardByUserEmail());
			listViewResponse.setRequestId(deptonboarding.getRequestNumber());
			listViewResponse.setOnboardingStatus(deptonboarding.getOnboardingStatus());
			listViewResponse.setChildRequestId(deptonboarding.getChildRequestNumber());
			listViewResponse.setReviewedByEmail(deptonboarding.getWorkGroupUserEmail());
			if (role.equalsIgnoreCase(Constant.APPROVER)) {
				listViewResponse.setReviewedByEmail(deptonboarding.getWorkGroupUserEmail());
			}
			if (deptonboarding.getChildRequestNumber() != null) {
				listViewResponse.setChildRequestId(deptonboarding.getChildRequestNumber());
			}
			list.add(listViewResponse);
		}
		return list;
	}

	@Override
	@Transactional
	public CommonResponse departmentOnboardReview(String childRequestId, String requestId, UserLoginDetails profile,
			DeptOnboardingWorkFlowRequest onboardingWorkFlowRequest)
			throws DataValidationException, IOException, TemplateException, MessagingException {
		CommonResponse commonResponse = new CommonResponse();
		UserDetails details = new UserDetails();

		Response response = new Response();
		if (childRequestId == null && requestId == null) {
			response.setData("Check Parameters");
			response.setAction("Onboarding Work flow Action Response");
			commonResponse.setMessage("Wrong Param or Null values in param");
			commonResponse.setResponse(response);
			commonResponse.setStatus(HttpStatus.CONFLICT);
			return commonResponse;
		}
		if (childRequestId != null && requestId != null) {
			response.setData("Check Parameters");
			response.setAction("Onboarding Work flow Action Response");
			commonResponse.setMessage("Provide Either ChildRequestId or RequestId");
			commonResponse.setResponse(response);
			commonResponse.setStatus(HttpStatus.CONFLICT);
			return commonResponse;
		}
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)
				|| userDetails.getUserRole().equalsIgnoreCase(Constant.SUPERADMIN)) {
			if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
				if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
					if (requestId == null && childRequestId != null) {
						DepartmentOnboarding childRequest = departmentOnboardingRepository
								.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
						if (childRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
									HttpStatus.CONFLICT);
						}
						DepartmentOnboarding departmentOnboarding = new DepartmentOnboarding();
						departmentOnboarding.setChildRequestNumber(childRequestId);
						departmentOnboarding.setWorkGroup(Constant.APPROVER);
						departmentOnboarding.setApprovedRejected(Constant.REVIEW);
						departmentOnboarding.setBudget(childRequest.getBudget());
						departmentOnboarding.setOnboardingStatus(Constant.PENDING_WITH_APPROVER);
						departmentOnboarding.setComments(onboardingWorkFlowRequest.getComments());
						departmentOnboarding.setCreatedOn(new Date());
						departmentOnboarding.setCreatedBy(childRequest.getCreatedBy());
						departmentOnboarding.setOpID(childRequest.getOpID());
						departmentOnboarding.setBuID(childRequest.getBuID());
						departmentOnboarding.setDepartmentId(childRequest.getDepartmentId());
						departmentOnboarding.setDepartmentName(childRequest.getDepartmentName());
						departmentOnboarding.setWorkGroupUserEmail(profile.getEmailAddress());
						departmentOnboarding.setUpdatedOn(new Date());
						departmentOnboarding.setComments(onboardingWorkFlowRequest.getComments());
						departmentOnboarding.setRequestNumber(childRequest.getRequestNumber());
						departmentOnboarding.setOnboardByUserEmail(childRequest.getOnboardByUserEmail());
						departmentOnboarding.setDepartmentOwner(childRequest.getDepartmentOwner());
						departmentOnboarding.setDepartmentOwnerEmail(childRequest.getDepartmentOwnerEmail());
						departmentOnboarding.setBudgetCurrency(childRequest.getBudgetCurrency());

						childRequest.setOnboardingStatus("Approved By Reviewer");
						childRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						childRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						childRequest.setEndDate(new Date());
						childRequest.setComments(onboardingWorkFlowRequest.getComments());
						childRequest.setUpdatedBy(profile.getFirstName());
						childRequest.setUpdatedOn(new Date());
						departmentOnboardingRepository.save(departmentOnboarding);
						departmentOnboardingRepository.save(childRequest);
					}
					if (requestId != null && childRequestId == null) {
						DepartmentOnboarding parentRequest = departmentOnboardingRepository
								.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
						if (parentRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
									HttpStatus.CONFLICT);
						}
						DepartmentOnboarding departmentOnboarding = new DepartmentOnboarding();
						departmentOnboarding.setWorkGroup(Constant.APPROVER);
						departmentOnboarding.setApprovedRejected(Constant.REVIEW);
						departmentOnboarding.setBudget(parentRequest.getBudget());
						departmentOnboarding.setCreatedOn(new Date());
						departmentOnboarding.setCreatedBy(parentRequest.getCreatedBy());
						departmentOnboarding.setOpID(parentRequest.getOpID());
						departmentOnboarding.setComments(onboardingWorkFlowRequest.getComments());
						departmentOnboarding.setBuID(parentRequest.getBuID());
						departmentOnboarding.setOnboardingStatus(Constant.PENDING_WITH_APPROVER);
						departmentOnboarding.setDepartmentId(parentRequest.getDepartmentId());
						departmentOnboarding.setDepartmentName(parentRequest.getDepartmentName());
						departmentOnboarding.setWorkGroupUserEmail(profile.getEmailAddress());
						departmentOnboarding.setUpdatedOn(new Date());
						departmentOnboarding.setComments(onboardingWorkFlowRequest.getComments());
						departmentOnboarding.setRequestNumber(parentRequest.getRequestNumber());
						departmentOnboarding.setOnboardByUserEmail(parentRequest.getOnboardByUserEmail());
						departmentOnboarding.setDepartmentOwner(parentRequest.getDepartmentOwner());
						departmentOnboarding.setDepartmentOwnerEmail(parentRequest.getDepartmentOwnerEmail());
						departmentOnboarding.setBudgetCurrency(parentRequest.getBudgetCurrency());
						parentRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						parentRequest.setOnboardingStatus("Approved By Reviewer");
						parentRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						parentRequest.setEndDate(new Date());
						parentRequest.setComments(onboardingWorkFlowRequest.getComments());
						parentRequest.setUpdatedBy(profile.getFirstName());
						parentRequest.setUpdatedOn(new Date());

						departmentOnboardingRepository.save(departmentOnboarding);
						departmentOnboardingRepository.save(parentRequest);
					}
					return reviewSuccessResponse();
				} else {
					if (requestId != null) {
						DepartmentOnboarding rejectRequest = departmentOnboardingRepository
								.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
						if (rejectRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
									HttpStatus.CONFLICT);
						}
						rejectRequest.setApprovedRejected(Constant.REJECTED);
						if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
							rejectRequest.setOnboardingStatus("Rejected by Reviewer");
						} else {
							rejectRequest.setWorkGroup(Constant.SUPERADMIN);
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
						}
						rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
						departmentOnboardingRepository.save(rejectRequest);
						return reviewFailureResponse();

					} else {
						DepartmentOnboarding rejectRequest = departmentOnboardingRepository
								.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
						if (rejectRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
									HttpStatus.CONFLICT);
						}
						rejectRequest.setApprovedRejected(Constant.REJECTED);
						if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
							rejectRequest.setOnboardingStatus("Rejected by Reviewer");
						} else {
							rejectRequest.setWorkGroup(Constant.SUPERADMIN);
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
						}
						rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
						departmentOnboardingRepository.save(rejectRequest);
						return reviewFailureResponse();
					}
				}
			} else {
				if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
					try {
						superAdminsaveData(requestId, childRequestId, profile, onboardingWorkFlowRequest);
					} catch (DataValidationException e) {
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
					}
					return reviewSuccessResponse();
				} else {
					if (requestId != null) {
						DepartmentOnboarding rejectRequestForReviewer = departmentOnboardingRepository
								.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
						DepartmentOnboarding rejectRequestForApprover = departmentOnboardingRepository
								.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
						if (rejectRequestForReviewer != null) {
							rejectRequestForReviewer.setApprovedRejected(Constant.REJECTED);
							rejectRequestForReviewer.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequestForReviewer.setComments(onboardingWorkFlowRequest.getComments());
							departmentOnboardingRepository.save(rejectRequestForReviewer);
							return reviewFailureResponse();
						}
						if (rejectRequestForApprover != null) {
							rejectRequestForApprover.setApprovedRejected(Constant.REJECTED);
							rejectRequestForApprover.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequestForApprover.setComments(onboardingWorkFlowRequest.getComments());
							departmentOnboardingRepository.save(rejectRequestForApprover);
							return reviewFailureResponse();
						}
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
					} else {
						DepartmentOnboarding rejectRequestForReviewer = departmentOnboardingRepository
								.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
						DepartmentOnboarding rejectRequestForApprover = departmentOnboardingRepository
								.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
						if (rejectRequestForReviewer != null) {
							rejectRequestForReviewer.setApprovedRejected(Constant.REJECTED);
							rejectRequestForReviewer.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequestForReviewer.setComments(onboardingWorkFlowRequest.getComments());
							departmentOnboardingRepository.save(rejectRequestForReviewer);
							return reviewFailureResponse();
						}
						if (rejectRequestForApprover != null) {
							rejectRequestForApprover.setApprovedRejected(Constant.REJECTED);
							rejectRequestForApprover.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequestForApprover.setComments(onboardingWorkFlowRequest.getComments());
							departmentOnboardingRepository.save(rejectRequestForApprover);
							return reviewFailureResponse();
						}
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
					}
				}
			}
		} else {
			if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
				if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER)) {
					if (requestId == null && childRequestId != null) {
						DepartmentOnboarding childRequest = departmentOnboardingRepository
								.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
						if (childRequest == null) {
							throw new DataValidationException(Constant.VALID_ID, requestId, HttpStatus.CONFLICT);
						}
						childRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						childRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						childRequest.setOnboardingStatus("Approved By Approver");
						childRequest.setEndDate(new Date());
						childRequest.setUpdatedBy(profile.getFirstName());
						childRequest.setComments(onboardingWorkFlowRequest.getComments());
						childRequest.setUpdatedOn(new Date());
						departmentOnboardingRepository.save(childRequest);

						DepartmentDetails departmentDetails = new DepartmentDetails();
						departmentDetails.setBudget(childRequest.getBudget());
						departmentDetails.setBudgetCurrency(childRequest.getBudgetCurrency());
						departmentDetails.setCreatedOn(new Date());
						departmentDetails.setDepartmentName(childRequest.getDepartmentName());
						departmentDetails.setDepartmentId(childRequest.getDepartmentId());
						departmentDetails.setBuID(Constant.BUID);
						departmentDetails.setCreatedBy(profile.getEmailAddress());
						departmentRepository.save(departmentDetails);
						
						
					        
						List<DepartmentOwnerDetails> detOwnerDetails = departmentOwnerRepository
								.findByDepartmentId(childRequest.getDepartmentId());
						for (DepartmentOwnerDetails owner : detOwnerDetails) {
							details.setDepartmentId(departmentDetails);
							String nameUser = Constant.USER_0;
							Integer sequence = sequenceGeneratorRepository.getUserOnboardingSequence();
							nameUser = nameUser.concat(sequence.toString());
							SequenceGenerator updateSequence1 = sequenceGeneratorRepository.getById(1);
							updateSequence1.setUserOnboarding(++sequence);
							sequenceGeneratorRepository.save(updateSequence1);
							details.setUserId(nameUser);
							details.setCreatedOn(new Date());
							details.setJoiningDate(new Date());
							details.setUserEmail(owner.getDepartmentOwnerEmail());
							details.setUserName(owner.getDepartmentOwner());
							details.setCreatedBy(profile.getEmailAddress().trim());
							details.setBuID(Constant.BUID_02);
							details.setLogoUrl(avatarUrl);
							details.setUserStatus(Constant.ACTIVE);
							details.setUserRole(Constant.CONTRIBUTOR);
							details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
							UserLoginDetails userProfile = new UserLoginDetails();
							VerificationDetails verificationDetails = new VerificationDetails();
							Date emailVerificationExpiredate = DateUtils.addMinutes(Calendar.getInstance().getTime(),
									Constant.EMAIL_VERIFICATION_CODE_EXPIRE_DATE);
							int verificationCode = CommonUtil.getRandomNumber(100000, 999999);
							verificationDetails.setUserEmail(owner.getDepartmentOwnerEmail());
							verificationDetails.setEmailVerified(false);
							verificationDetails.setOpID(Constant.SAASPE);
							verificationDetails.setCreatedOn(new Date());
							verificationDetails.setEmailVerificationCode(String.valueOf(verificationCode));
							verificationDetails.setEmailVerificationCodeExpiryDate(emailVerificationExpiredate);
							verificationDetails.setEmailVerificationCodeSendDate(new Date());
							userProfile.setEmailAddress(owner.getDepartmentOwnerEmail());
							userProfile.setCreatedOn(new Date());
							userProfile.setFirstName(owner.getDepartmentOwner());
							UserDetails mailSendCheck = userDetailsRepository
									.findByuserEmail(owner.getDepartmentOwnerEmail());
							if (mailSendCheck == null) {
								sequenceGeneratorRepository.save(updateSequence1);
								userDetailsRepository.save(details);
								userLoginDetailsRepository.save(userProfile);
								verificationDetailsRepository.save(verificationDetails);
								sendDefaultPasswordEmail(owner.getDepartmentOwnerEmail(), owner.getDepartmentOwner(),
										String.valueOf(verificationCode));
							} else {
								mailSendCheck.setDepartmentId(departmentDetails);
								userDetailsRepository.save(mailSendCheck);
							}
						}

					}
					if (requestId != null && childRequestId == null) {
						DepartmentOnboarding parentRequest = departmentOnboardingRepository
								.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
						if (parentRequest == null) {
							throw new DataValidationException(Constant.VALID_ID, requestId, HttpStatus.CONFLICT);
						}
						parentRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						parentRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						parentRequest.setComments(onboardingWorkFlowRequest.getComments());
						parentRequest.setWorkGroup(Constant.APPROVER);
						parentRequest.setOnboardingStatus("Approved By Approver");
						parentRequest.setEndDate(new Date());
						parentRequest.setUpdatedBy(profile.getFirstName());
						parentRequest.setUpdatedOn(new Date());
						departmentOnboardingRepository.save(parentRequest);

						DepartmentDetails departmentDetails = new DepartmentDetails();
						departmentDetails.setBudget(parentRequest.getBudget());
						departmentDetails.setDepartmentName(parentRequest.getDepartmentName());
						departmentDetails.setDepartmentOwner(parentRequest.getDepartmentOwner());
						departmentDetails.setDepartmentId(parentRequest.getDepartmentId());
						departmentDetails.setBudgetCurrency(parentRequest.getBudgetCurrency());
						departmentDetails.setBuID(Constant.BUID);
						departmentDetails.setCreatedOn(new Date());
						departmentDetails.setCreatedBy(profile.getEmailAddress());
						departmentDetails.setDepartmentAdmin(parentRequest.getDepartmentOwnerEmail());
						departmentRepository.save(departmentDetails);
						
						
						List<DepartmentOwnerDetails> detOwnerDetails = departmentOwnerRepository
								.findByDepartmentId(parentRequest.getDepartmentId());
						for (DepartmentOwnerDetails owner : detOwnerDetails) {
							details.setDepartmentId(departmentDetails);
							String nameUser = Constant.USER_0;
							Integer sequence = sequenceGeneratorRepository.getUserOnboardingSequence();
							nameUser = nameUser.concat(sequence.toString());
							SequenceGenerator updateSequence1 = sequenceGeneratorRepository.getById(1);
							updateSequence1.setUserOnboarding(++sequence);
							sequenceGeneratorRepository.save(updateSequence1);
							details.setUserId(nameUser);
							details.setCreatedOn(new Date());
							details.setJoiningDate(new Date());
							details.setUserEmail(owner.getDepartmentOwnerEmail());
							details.setUserName(owner.getDepartmentOwner());
							details.setCreatedBy(profile.getEmailAddress().trim());
							details.setBuID(Constant.BUID_02);
							details.setLogoUrl(avatarUrl);
							details.setUserStatus(Constant.ACTIVE);
							details.setUserRole(Constant.CONTRIBUTOR);
							details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
							UserLoginDetails userProfile = new UserLoginDetails();
							VerificationDetails verificationDetails = new VerificationDetails();
							Date emailVerificationExpiredate = DateUtils.addMinutes(Calendar.getInstance().getTime(),
									Constant.EMAIL_VERIFICATION_CODE_EXPIRE_DATE);
							int verificationCode = CommonUtil.getRandomNumber(100000, 999999);
							verificationDetails.setUserEmail(owner.getDepartmentOwnerEmail());
							verificationDetails.setEmailVerified(false);
							verificationDetails.setOpID(Constant.SAASPE);
							verificationDetails.setCreatedOn(new Date());
							verificationDetails.setEmailVerificationCode(String.valueOf(verificationCode));
							verificationDetails.setEmailVerificationCodeExpiryDate(emailVerificationExpiredate);
							verificationDetails.setEmailVerificationCodeSendDate(new Date());
							userProfile.setEmailAddress(owner.getDepartmentOwnerEmail());
							userProfile.setCreatedOn(new Date());
							userProfile.setFirstName(owner.getDepartmentOwner());
							UserDetails mailSendCheck = userDetailsRepository
									.findByuserEmail(owner.getDepartmentOwnerEmail());
							if (mailSendCheck == null) {
								sequenceGeneratorRepository.save(updateSequence1);
								userDetailsRepository.save(details);
								userLoginDetailsRepository.save(userProfile);
								verificationDetailsRepository.save(verificationDetails);
								sendDefaultPasswordEmail(owner.getDepartmentOwnerEmail(), owner.getDepartmentOwner(),
										String.valueOf(verificationCode));
							} else {
								mailSendCheck.setDepartmentId(departmentDetails);
								userDetailsRepository.save(mailSendCheck);
							}
						}
						return reviewSuccessResponse();
					}
					return reviewSuccessResponse();
				} else {
					if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
						try {
							superAdminsaveData(requestId, childRequestId, profile, onboardingWorkFlowRequest);
						} catch (DataValidationException e) {
							throw new DataValidationException(Constant.VALID_ID, requestId, HttpStatus.CONFLICT);
						}
						return reviewSuccessResponse();
					} else {
						if (requestId != null) {
							DepartmentOnboarding rejectRequest = departmentOnboardingRepository
									.findByRequestNumber(requestId);
							rejectRequest.setApprovedRejected(Constant.REJECTED);
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
							departmentOnboardingRepository.save(rejectRequest);
							return reviewFailureResponse();

						} else {
							DepartmentOnboarding rejectRequest = departmentOnboardingRepository
									.findByChildRequestNumber(childRequestId);
							rejectRequest.setApprovedRejected(Constant.REJECTED);
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
							departmentOnboardingRepository.save(rejectRequest);
							return reviewFailureResponse();
						}
					}
				}

			} else {
				if (requestId != null) {
					DepartmentOnboarding rejectRequest = departmentOnboardingRepository.findByRequestNumber(requestId,
							Constant.APPROVER, Constant.REVIEW);
					if (rejectRequest == null) {
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
					}
					rejectRequest.setOnboardingStatus("Rejected by Approver");
					rejectRequest.setApprovedRejected(Constant.REJECTED);
					rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
					departmentOnboardingRepository.save(rejectRequest);
					return reviewFailureResponse();

				} else {
					DepartmentOnboarding rejectRequest = departmentOnboardingRepository
							.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
					if (rejectRequest == null) {
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
					}
					rejectRequest.setApprovedRejected(Constant.REJECTED);
					rejectRequest.setOnboardingStatus("Rejected by Approver");
					rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
					departmentOnboardingRepository.save(rejectRequest);
					return reviewFailureResponse();
				}
			}
		}
	}

	private void superAdminsaveData(String requestId, String childRequestId, UserLoginDetails profile,
			DeptOnboardingWorkFlowRequest onboardingWorkFlowRequest)
			throws DataValidationException, IOException, TemplateException, MessagingException {
		DepartmentDetails departmentDetails = new DepartmentDetails();
		UserDetails details = new UserDetails();

		if (requestId == null && childRequestId != null) {
			DepartmentOnboarding superAdminRequest = departmentOnboardingRepository.findAllBySuperAdmin(childRequestId);
			if (superAdminRequest == null) {
				throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
			}
			DepartmentOnboarding onboarding = new DepartmentOnboarding();
			if (superAdminRequest.getWorkGroup().equalsIgnoreCase(Constant.REVIEWER)) {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setUpdatedOn(new Date());

				onboarding.setOnboardingStatus(superAdminRequest.getOnboardingStatus());
				onboarding.setComments(onboardingWorkFlowRequest.getComments());
				onboarding.setBudget(superAdminRequest.getBudget());
				onboarding.setCreatedOn(new Date());
				onboarding.setOnBoardDate(new Date());
				onboarding.setCreatedBy(superAdminRequest.getCreatedBy());
				onboarding.setOpID(superAdminRequest.getOpID());
				onboarding.setBuID(superAdminRequest.getBuID());
				onboarding.setChildRequestNumber(superAdminRequest.getChildRequestNumber());
				onboarding.setDepartmentId(superAdminRequest.getDepartmentId());
				onboarding.setBudgetCurrency(superAdminRequest.getBudgetCurrency());
				onboarding.setDepartmentName(superAdminRequest.getDepartmentName());
				onboarding.setWorkGroup(Constant.SUPERADMIN);
				onboarding.setApprovedRejected(Constant.APPROVE);
				onboarding.setWorkGroupUserEmail(profile.getEmailAddress());
				onboarding.setUpdatedOn(new Date());
				onboarding.setRequestNumber(superAdminRequest.getRequestNumber());
				onboarding.setOnboardByUserEmail(superAdminRequest.getOnboardByUserEmail());
				onboarding.setDepartmentOwner(superAdminRequest.getDepartmentOwner());
				onboarding.setDepartmentOwnerEmail(superAdminRequest.getDepartmentOwnerEmail());
				departmentOnboardingRepository.save(onboarding);
				departmentOnboardingRepository.save(superAdminRequest);
				departmentDetails.setBudget(superAdminRequest.getBudget());
				departmentDetails.setCreatedOn(new Date());
				departmentDetails.setDepartmentName(superAdminRequest.getDepartmentName());
				departmentDetails.setDepartmentOwner(superAdminRequest.getDepartmentOwner());
				departmentDetails.setDepartmentId(superAdminRequest.getDepartmentId());
				departmentDetails.setBudgetCurrency(superAdminRequest.getBudgetCurrency());
				departmentDetails.setBuID(Constant.BUID);
				departmentDetails.setCreatedBy(profile.getEmailAddress());
				departmentDetails.setDepartmentAdmin(superAdminRequest.getDepartmentOwnerEmail());
				departmentRepository.save(departmentDetails);
				
				
				// start - department owner(user) onboarding
				List<DepartmentOwnerDetails> detOwnerDetails = departmentOwnerRepository
						.findByDepartmentId(superAdminRequest.getDepartmentId());
				for (DepartmentOwnerDetails owner : detOwnerDetails) {
					details.setDepartmentId(departmentDetails);
					String nameUser = Constant.USER_0;
					Integer sequence = sequenceGeneratorRepository.getUserOnboardingSequence();
					nameUser = nameUser.concat(sequence.toString());
					SequenceGenerator updateSequence1 = sequenceGeneratorRepository.getById(1);
					updateSequence1.setUserOnboarding(++sequence);
					sequenceGeneratorRepository.save(updateSequence1);
					details.setUserId(nameUser);
					details.setCreatedOn(new Date());
					details.setJoiningDate(new Date());
					details.setUserEmail(owner.getDepartmentOwnerEmail());
					details.setUserName(owner.getDepartmentOwner());
					details.setCreatedBy(profile.getEmailAddress().trim());
					details.setBuID(Constant.BUID_02);
					details.setLogoUrl(avatarUrl);
					details.setUserStatus(Constant.ACTIVE);
					details.setUserRole(Constant.CONTRIBUTOR);
					details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
					UserLoginDetails userProfile = new UserLoginDetails();
					VerificationDetails verificationDetails = new VerificationDetails();
					Date emailVerificationExpiredate = DateUtils.addMinutes(Calendar.getInstance().getTime(),
							Constant.EMAIL_VERIFICATION_CODE_EXPIRE_DATE);
					int verificationCode = CommonUtil.getRandomNumber(100000, 999999);
					verificationDetails.setUserEmail(owner.getDepartmentOwnerEmail());
					verificationDetails.setEmailVerified(false);
					verificationDetails.setOpID(Constant.SAASPE);
					verificationDetails.setCreatedOn(new Date());
					verificationDetails.setEmailVerificationCode(String.valueOf(verificationCode));
					verificationDetails.setEmailVerificationCodeExpiryDate(emailVerificationExpiredate);
					verificationDetails.setEmailVerificationCodeSendDate(new Date());
					userProfile.setEmailAddress(owner.getDepartmentOwnerEmail());
					userProfile.setCreatedOn(new Date());
					userProfile.setFirstName(owner.getDepartmentOwner());
					UserDetails mailSendCheck = userDetailsRepository.findByuserEmail(owner.getDepartmentOwnerEmail());
					if (mailSendCheck == null) {
						sequenceGeneratorRepository.save(updateSequence1);
						userDetailsRepository.save(details);
						userLoginDetailsRepository.save(userProfile);
						verificationDetailsRepository.save(verificationDetails);
						sendDefaultPasswordEmail(owner.getDepartmentOwnerEmail(), owner.getDepartmentOwner(),
								String.valueOf(verificationCode));
					} else {
						mailSendCheck.setDepartmentId(departmentDetails);
						userDetailsRepository.save(mailSendCheck);
					}
				}

			} else {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setOnboardingStatus("Approved By SuperAdmin");
				superAdminRequest.setWorkGroup(Constant.SUPERADMIN);
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setOnBoardDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setUpdatedOn(new Date());
				departmentOnboardingRepository.save(superAdminRequest);
				departmentDetails.setBudget(superAdminRequest.getBudget());
				departmentDetails.setDepartmentName(superAdminRequest.getDepartmentName());
				departmentDetails.setDepartmentOwner(superAdminRequest.getDepartmentOwner());
				departmentDetails.setDepartmentId(superAdminRequest.getDepartmentId());
				departmentDetails.setBudgetCurrency(superAdminRequest.getBudgetCurrency());
				departmentDetails.setCreatedOn(new Date());
				departmentDetails.setBuID(Constant.BUID);
				departmentDetails.setCreatedBy(profile.getEmailAddress());
				departmentDetails.setDepartmentAdmin(superAdminRequest.getDepartmentOwnerEmail());
				departmentRepository.save(departmentDetails);
				
				

				// start - department owner(user) onboarding
				List<DepartmentOwnerDetails> detOwnerDetails = departmentOwnerRepository
						.findByDepartmentId(superAdminRequest.getDepartmentId());
				for (DepartmentOwnerDetails owner : detOwnerDetails) {
					details.setDepartmentId(departmentDetails);
					String nameUser = Constant.USER_0;
					Integer sequence = sequenceGeneratorRepository.getUserOnboardingSequence();
					nameUser = nameUser.concat(sequence.toString());
					SequenceGenerator updateSequence1 = sequenceGeneratorRepository.getById(1);
					updateSequence1.setUserOnboarding(++sequence);
					sequenceGeneratorRepository.save(updateSequence1);
					details.setUserId(nameUser);
					details.setCreatedOn(new Date());
					details.setJoiningDate(new Date());
					details.setUserEmail(owner.getDepartmentOwnerEmail());
					details.setUserName(owner.getDepartmentOwner());
					details.setCreatedBy(profile.getEmailAddress().trim());
					details.setBuID(Constant.BUID_02);
					details.setLogoUrl(avatarUrl);
					details.setUserStatus(Constant.ACTIVE);
					details.setUserRole(Constant.CONTRIBUTOR);
					details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
					UserLoginDetails userProfile = new UserLoginDetails();
					VerificationDetails verificationDetails = new VerificationDetails();
					Date emailVerificationExpiredate = DateUtils.addMinutes(Calendar.getInstance().getTime(),
							Constant.EMAIL_VERIFICATION_CODE_EXPIRE_DATE);
					int verificationCode = CommonUtil.getRandomNumber(100000, 999999);
					verificationDetails.setUserEmail(owner.getDepartmentOwnerEmail());
					verificationDetails.setEmailVerified(false);
					verificationDetails.setOpID(Constant.SAASPE);
					verificationDetails.setCreatedOn(new Date());
					verificationDetails.setEmailVerificationCode(String.valueOf(verificationCode));
					verificationDetails.setEmailVerificationCodeExpiryDate(emailVerificationExpiredate);
					verificationDetails.setEmailVerificationCodeSendDate(new Date());
					userProfile.setEmailAddress(owner.getDepartmentOwnerEmail());
					userProfile.setCreatedOn(new Date());
					userProfile.setFirstName(owner.getDepartmentOwner());
					UserDetails mailSendCheck = userDetailsRepository.findByuserEmail(owner.getDepartmentOwnerEmail());
					if (mailSendCheck == null) {
						sequenceGeneratorRepository.save(updateSequence1);
						userDetailsRepository.save(details);
						userLoginDetailsRepository.save(userProfile);
						verificationDetailsRepository.save(verificationDetails);
						sendDefaultPasswordEmail(owner.getDepartmentOwnerEmail(), owner.getDepartmentOwner(),
								String.valueOf(verificationCode));
					} else {
						mailSendCheck.setDepartmentId(departmentDetails);
						userDetailsRepository.save(mailSendCheck);
					}
				}
				// end - department owner(user) onboarding

			}
		}
		if (requestId != null && childRequestId == null) {
			DepartmentOnboarding onboarding = new DepartmentOnboarding();
			DepartmentOnboarding superAdminRequest = departmentOnboardingRepository
					.findAllBySuperAdminRequestId(requestId);
			if (superAdminRequest == null) {
				throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
			}
			if (superAdminRequest.getWorkGroup().equalsIgnoreCase(Constant.REVIEWER)) {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setUpdatedOn(new Date());
				onboarding.setOnboardingStatus(superAdminRequest.getOnboardingStatus());
				onboarding.setComments(onboardingWorkFlowRequest.getComments());
				onboarding.setBudget(superAdminRequest.getBudget());
				onboarding.setBudgetCurrency(superAdminRequest.getBudgetCurrency());
				onboarding.setCreatedOn(new Date());
				onboarding.setOnBoardDate(new Date());
				onboarding.setCreatedBy(superAdminRequest.getCreatedBy());
				onboarding.setOpID(superAdminRequest.getOpID());
				onboarding.setBuID(superAdminRequest.getBuID());
				onboarding.setDepartmentId(superAdminRequest.getDepartmentId());
				onboarding.setDepartmentName(superAdminRequest.getDepartmentName());
				onboarding.setWorkGroup(Constant.SUPERADMIN);
				onboarding.setApprovedRejected(Constant.APPROVE);
				onboarding.setWorkGroupUserEmail(profile.getEmailAddress());
				onboarding.setUpdatedOn(new Date());
				onboarding.setRequestNumber(superAdminRequest.getRequestNumber());
				onboarding.setOnboardByUserEmail(superAdminRequest.getOnboardByUserEmail());
				onboarding.setDepartmentOwner(superAdminRequest.getDepartmentOwner());
				onboarding.setDepartmentOwnerEmail(superAdminRequest.getDepartmentOwnerEmail());
				departmentOnboardingRepository.save(onboarding);
				departmentOnboardingRepository.save(superAdminRequest);
				departmentDetails.setBudget(superAdminRequest.getBudget());
				departmentDetails.setDepartmentName(superAdminRequest.getDepartmentName());
				departmentDetails.setDepartmentOwner(superAdminRequest.getDepartmentOwner());
				departmentDetails.setDepartmentId(superAdminRequest.getDepartmentId());
				departmentDetails.setBudgetCurrency(superAdminRequest.getBudgetCurrency());
				departmentDetails.setBuID(Constant.BUID);
				departmentDetails.setCreatedOn(new Date());
				departmentDetails.setCreatedBy(profile.getEmailAddress());
				departmentDetails.setDepartmentAdmin(superAdminRequest.getDepartmentOwnerEmail());
				departmentRepository.save(departmentDetails);
				
				
				// start - department owner(user) onboarding

				List<DepartmentOwnerDetails> detOwnerDetails = departmentOwnerRepository
						.findByDepartmentId(superAdminRequest.getDepartmentId());
				for (DepartmentOwnerDetails owner : detOwnerDetails) {
					details.setDepartmentId(departmentDetails);
					String nameUser = Constant.USER_0;
					Integer sequence = sequenceGeneratorRepository.getUserOnboardingSequence();
					nameUser = nameUser.concat(sequence.toString());
					SequenceGenerator updateSequence1 = sequenceGeneratorRepository.getById(1);
					updateSequence1.setUserOnboarding(++sequence);
					sequenceGeneratorRepository.save(updateSequence1);
					details.setUserId(nameUser);
					details.setCreatedOn(new Date());
					details.setJoiningDate(new Date());
					details.setUserEmail(owner.getDepartmentOwnerEmail());
					details.setUserName(owner.getDepartmentOwner());
					details.setCreatedBy(profile.getEmailAddress().trim());
					details.setBuID(Constant.BUID_02);
					details.setLogoUrl(avatarUrl);
					details.setUserStatus(Constant.ACTIVE);
					details.setUserRole(Constant.CONTRIBUTOR);
					details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
					UserLoginDetails userProfile = new UserLoginDetails();
					VerificationDetails verificationDetails = new VerificationDetails();
					Date emailVerificationExpiredate = DateUtils.addMinutes(Calendar.getInstance().getTime(),
							Constant.EMAIL_VERIFICATION_CODE_EXPIRE_DATE);
					int verificationCode = CommonUtil.getRandomNumber(100000, 999999);
					verificationDetails.setUserEmail(owner.getDepartmentOwnerEmail());
					verificationDetails.setEmailVerified(false);
					verificationDetails.setOpID(Constant.SAASPE);
					verificationDetails.setCreatedOn(new Date());
					verificationDetails.setEmailVerificationCode(String.valueOf(verificationCode));
					verificationDetails.setEmailVerificationCodeExpiryDate(emailVerificationExpiredate);
					verificationDetails.setEmailVerificationCodeSendDate(new Date());
					userProfile.setEmailAddress(owner.getDepartmentOwnerEmail());
					userProfile.setCreatedOn(new Date());
					userProfile.setFirstName(owner.getDepartmentOwner());
					UserDetails mailSendCheck = userDetailsRepository.findByuserEmail(owner.getDepartmentOwnerEmail());
					if (mailSendCheck == null) {
						sequenceGeneratorRepository.save(updateSequence1);
						userDetailsRepository.save(details);
						userLoginDetailsRepository.save(userProfile);
						verificationDetailsRepository.save(verificationDetails);
						sendDefaultPasswordEmail(owner.getDepartmentOwnerEmail(), owner.getDepartmentOwner(),
								String.valueOf(verificationCode));
					} else {
						mailSendCheck.setDepartmentId(departmentDetails);
						userDetailsRepository.save(mailSendCheck);
					}
				}
			} else {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setWorkGroup(Constant.SUPERADMIN);
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setOnBoardDate(new Date());
				superAdminRequest.setUpdatedOn(new Date());
				departmentOnboardingRepository.save(superAdminRequest);
				departmentDetails.setBudget(superAdminRequest.getBudget());
				departmentDetails.setDepartmentName(superAdminRequest.getDepartmentName());
				departmentDetails.setDepartmentOwner(superAdminRequest.getDepartmentOwner());
				departmentDetails.setDepartmentId(superAdminRequest.getDepartmentId());
				departmentDetails.setBudgetCurrency(superAdminRequest.getBudgetCurrency());
				departmentDetails.setBuID(Constant.BUID);
				departmentDetails.setCreatedOn(new Date());
				departmentDetails.setCreatedBy(profile.getEmailAddress());
				departmentDetails.setDepartmentAdmin(superAdminRequest.getDepartmentOwnerEmail());
				departmentRepository.save(departmentDetails);
				
				
				

				// start - department owner(user) onboarding
				List<DepartmentOwnerDetails> detOwnerDetails = departmentOwnerRepository
						.findByDepartmentId(superAdminRequest.getDepartmentId());
				for (DepartmentOwnerDetails owner : detOwnerDetails) {
					details.setDepartmentId(departmentDetails);
					String nameUser = Constant.USER_0;
					Integer sequence = sequenceGeneratorRepository.getUserOnboardingSequence();
					nameUser = nameUser.concat(sequence.toString());
					SequenceGenerator updateSequence1 = sequenceGeneratorRepository.getById(1);
					updateSequence1.setUserOnboarding(++sequence);
					sequenceGeneratorRepository.save(updateSequence1);
					details.setUserId(nameUser);
					details.setCreatedOn(new Date());
					details.setJoiningDate(new Date());
					details.setUserEmail(owner.getDepartmentOwnerEmail());
					details.setUserName(owner.getDepartmentOwner());
					details.setCreatedBy(profile.getEmailAddress().trim());
					details.setBuID(Constant.BUID_02);
					details.setLogoUrl(avatarUrl);
					details.setUserStatus(Constant.ACTIVE);
					details.setUserRole(Constant.CONTRIBUTOR);
					details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
					UserLoginDetails userProfile = new UserLoginDetails();
					VerificationDetails verificationDetails = new VerificationDetails();
					Date emailVerificationExpiredate = DateUtils.addMinutes(Calendar.getInstance().getTime(),
							Constant.EMAIL_VERIFICATION_CODE_EXPIRE_DATE);
					int verificationCode = CommonUtil.getRandomNumber(100000, 999999);
					verificationDetails.setUserEmail(owner.getDepartmentOwnerEmail());
					verificationDetails.setEmailVerified(false);
					verificationDetails.setOpID(Constant.SAASPE);
					verificationDetails.setCreatedOn(new Date());
					verificationDetails.setEmailVerificationCode(String.valueOf(verificationCode));
					verificationDetails.setEmailVerificationCodeExpiryDate(emailVerificationExpiredate);
					verificationDetails.setEmailVerificationCodeSendDate(new Date());
					userProfile.setEmailAddress(owner.getDepartmentOwnerEmail());
					userProfile.setCreatedOn(new Date());
					userProfile.setFirstName(owner.getDepartmentOwner());
					UserDetails mailSendCheck = userDetailsRepository.findByuserEmail(owner.getDepartmentOwnerEmail());
					if (mailSendCheck == null) {
						sequenceGeneratorRepository.save(updateSequence1);
						userDetailsRepository.save(details);
						userLoginDetailsRepository.save(userProfile);
						verificationDetailsRepository.save(verificationDetails);
						sendDefaultPasswordEmail(owner.getDepartmentOwnerEmail(), owner.getDepartmentOwner(),
								String.valueOf(verificationCode));
					} else {
						mailSendCheck.setDepartmentId(departmentDetails);
						userDetailsRepository.save(mailSendCheck);
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

	@Override
	public CommonResponse departmentReviewerApproverDetailsView(String childRequestId, String requestId,
			UserLoginDetails profile) throws DataValidationException {
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		DepartmentOnboardingRequestDetailViewResponse detailViewResponse = new DepartmentOnboardingRequestDetailViewResponse();
		CreateDepartmentDetails departmentDetails = new CreateDepartmentDetails();
		DepartmentReviewerDetails reviewerDetails = new DepartmentReviewerDetails();
		DepartmentOnboarding department = departmentOnboardingRepository.findByRequest(requestId,childRequestId);
		if ((department.getApprovedRejected().equalsIgnoreCase(Constant.APPROVE)
				|| department.getApprovedRejected().equalsIgnoreCase(Constant.REJECTED))
				|| (department.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW)
						&& department.getWorkGroup().equalsIgnoreCase(Constant.APPROVER)
						&& (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)))) {
			throw new DataValidationException("Onboarding flow for the requested department is completed already", null, HttpStatus.NO_CONTENT);
		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
			if (requestId != null && childRequestId == null) {
				DepartmentOnboarding departmentReviewer = departmentOnboardingRepository.findByRequestNumber(requestId,
						Constant.REVIEWER, Constant.REVIEW);
				List<DepartmentOwnerDetails> departmentDetail = departmentOwnerRepository
						.findByDepartmentId(departmentReviewer.getDepartmentId());
				departmentDetails.setDepartmentBudget(departmentReviewer.getBudget());
				departmentDetails.setDepartmentName(departmentReviewer.getDepartmentName());
				departmentDetails.setCurrencyCode(departmentReviewer.getBudgetCurrency());
				List<CreateDepartmentOwnerDetails> ownerDetails = new ArrayList<>();
				for (DepartmentOwnerDetails deptDetails : departmentDetail) {
					CreateDepartmentOwnerDetails ownerDetail = new CreateDepartmentOwnerDetails();
					ownerDetail.setDepartmentOwnerEmailAddress(deptDetails.getDepartmentOwnerEmail());
					ownerDetail.setDepartmentOwnerName(deptDetails.getDepartmentOwner());
					ownerDetail.setPriority(deptDetails.getPriority());
					ownerDetails.add(ownerDetail);
				}
				departmentDetails.setOwnerDetails(ownerDetails);
				detailViewResponse.setDepartmentDetailsInfo(departmentDetails);
			}
			if (requestId == null && childRequestId != null) {
				DepartmentOnboarding departmentReviewer = departmentOnboardingRepository
						.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
				departmentDetails.setDepartmentBudget(departmentReviewer.getBudget());
				departmentDetails.setDepartmentName(departmentReviewer.getDepartmentName());

				departmentDetails.setCurrencyCode(departmentReviewer.getBudgetCurrency());
				List<DepartmentOwnerDetails> departmentDetail = departmentOwnerRepository
						.findByDepartmentId(departmentReviewer.getDepartmentId());
				List<CreateDepartmentOwnerDetails> ownerDetails = new ArrayList<>();
				for (DepartmentOwnerDetails deptDetails : departmentDetail) {
					CreateDepartmentOwnerDetails ownerDetail = new CreateDepartmentOwnerDetails();
					ownerDetail.setDepartmentOwnerEmailAddress(deptDetails.getDepartmentOwnerEmail());
					ownerDetail.setDepartmentOwnerName(deptDetails.getDepartmentOwner());
					ownerDetail.setPriority(deptDetails.getPriority());
					ownerDetails.add(ownerDetail);
				}
				departmentDetails.setOwnerDetails(ownerDetails);
				detailViewResponse.setDepartmentDetailsInfo(departmentDetails);
			}

		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER)) {
			if (requestId != null && childRequestId == null) {
				DepartmentOnboarding departmentApprover = departmentOnboardingRepository.findByRequestNumber(requestId,
						Constant.APPROVER, Constant.REVIEW);
				DepartmentOnboarding departmentReviewer = departmentOnboardingRepository.findByRequestNumber(requestId,
						Constant.REVIEWER, Constant.APPROVE);
				departmentDetails.setDepartmentBudget(departmentApprover.getBudget());
				departmentDetails.setDepartmentName(departmentApprover.getDepartmentName());

				departmentDetails.setCurrencyCode(departmentApprover.getBudgetCurrency());
				List<DepartmentOwnerDetails> departmentDetail = departmentOwnerRepository
						.findByDepartmentId(departmentApprover.getDepartmentId());
				List<CreateDepartmentOwnerDetails> ownerDetails = new ArrayList<>();
				for (DepartmentOwnerDetails deptDetails : departmentDetail) {
					CreateDepartmentOwnerDetails ownerDetail = new CreateDepartmentOwnerDetails();
					ownerDetail.setDepartmentOwnerEmailAddress(deptDetails.getDepartmentOwnerEmail());
					ownerDetail.setDepartmentOwnerName(deptDetails.getDepartmentOwner());
					ownerDetail.setPriority(deptDetails.getPriority());
					ownerDetails.add(ownerDetail);
				}
				departmentDetails.setOwnerDetails(ownerDetails);

				reviewerDetails.setApprovedByEmail(departmentReviewer.getWorkGroupUserEmail());
				reviewerDetails.setWorkGroupName(departmentReviewer.getWorkGroup());
				reviewerDetails.setComments(departmentReviewer.getComments());
				reviewerDetails.setApprovalTimeStamp(departmentReviewer.getEndDate());
				detailViewResponse.setDepartmentDetailsInfo(departmentDetails);
				detailViewResponse.setReviewerDetails(reviewerDetails);
			}
			if (requestId == null && childRequestId != null) {
				DepartmentOnboarding departmentApprover = departmentOnboardingRepository
						.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
				DepartmentOnboarding departmentReviewer = departmentOnboardingRepository
						.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.APPROVE);
				departmentDetails.setDepartmentBudget(departmentApprover.getBudget());
				departmentDetails.setDepartmentName(departmentApprover.getDepartmentName());

				departmentDetails.setCurrencyCode(departmentApprover.getBudgetCurrency());
				List<DepartmentOwnerDetails> departmentDetail = departmentOwnerRepository
						.findByDepartmentId(departmentApprover.getDepartmentId());
				List<CreateDepartmentOwnerDetails> ownerDetails = new ArrayList<>();
				for (DepartmentOwnerDetails deptDetails : departmentDetail) {
					CreateDepartmentOwnerDetails ownerDetail = new CreateDepartmentOwnerDetails();
					ownerDetail.setDepartmentOwnerEmailAddress(deptDetails.getDepartmentOwnerEmail());
					ownerDetail.setDepartmentOwnerName(deptDetails.getDepartmentOwner());
					ownerDetail.setPriority(deptDetails.getPriority());
					ownerDetails.add(ownerDetail);
				}
				departmentDetails.setOwnerDetails(ownerDetails);

				reviewerDetails.setApprovedByEmail(departmentReviewer.getWorkGroupUserEmail());
				reviewerDetails.setWorkGroupName(departmentReviewer.getWorkGroup());
				reviewerDetails.setComments(departmentReviewer.getComments());
				reviewerDetails.setApprovalTimeStamp(departmentReviewer.getEndDate());
				detailViewResponse.setDepartmentDetailsInfo(departmentDetails);
				detailViewResponse.setReviewerDetails(reviewerDetails);
			}
		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.SUPERADMIN)) {
			if (requestId != null && childRequestId == null) {
				DepartmentOnboarding departmentApprover = departmentOnboardingRepository.findByRequestNumber(requestId,
						Constant.APPROVER, Constant.REVIEW);
				DepartmentOnboarding departmentReviewerApproved = departmentOnboardingRepository
						.findByRequestNumber(requestId, Constant.REVIEWER, Constant.APPROVE);
				DepartmentOnboarding departmentReviewer = departmentOnboardingRepository.findByRequestNumber(requestId,
						Constant.REVIEWER, Constant.REVIEW);
				if (departmentApprover != null) {
					departmentDetails.setDepartmentBudget(departmentApprover.getBudget());
					departmentDetails.setDepartmentName(departmentApprover.getDepartmentName());

					departmentDetails.setCurrencyCode(departmentApprover.getBudgetCurrency());
					List<DepartmentOwnerDetails> departmentDetail = departmentOwnerRepository
							.findByDepartmentId(departmentApprover.getDepartmentId());
					List<CreateDepartmentOwnerDetails> ownerDetails = new ArrayList<>();
					for (DepartmentOwnerDetails deptDetails : departmentDetail) {
						CreateDepartmentOwnerDetails ownerDetail = new CreateDepartmentOwnerDetails();
						ownerDetail.setDepartmentOwnerEmailAddress(deptDetails.getDepartmentOwnerEmail());
						ownerDetail.setDepartmentOwnerName(deptDetails.getDepartmentOwner());
						ownerDetail.setPriority(deptDetails.getPriority());
						ownerDetails.add(ownerDetail);
					}
					departmentDetails.setOwnerDetails(ownerDetails);

					reviewerDetails.setApprovedByEmail(departmentReviewerApproved.getWorkGroupUserEmail());
					reviewerDetails.setWorkGroupName(departmentReviewerApproved.getWorkGroup());
					reviewerDetails.setComments(departmentReviewerApproved.getComments());
					reviewerDetails.setApprovalTimeStamp(departmentReviewerApproved.getEndDate());
					detailViewResponse.setDepartmentDetailsInfo(departmentDetails);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				} else {
					departmentDetails.setDepartmentBudget(departmentReviewer.getBudget());
					departmentDetails.setDepartmentName(departmentReviewer.getDepartmentName());
					departmentDetails.setCurrencyCode(departmentReviewer.getBudgetCurrency());
					List<DepartmentOwnerDetails> departmentDetail = departmentOwnerRepository
							.findByDepartmentId(departmentReviewer.getDepartmentId());
					List<CreateDepartmentOwnerDetails> ownerDetails = new ArrayList<>();
					for (DepartmentOwnerDetails deptDetails : departmentDetail) {
						CreateDepartmentOwnerDetails ownerDetail = new CreateDepartmentOwnerDetails();
						ownerDetail.setDepartmentOwnerEmailAddress(deptDetails.getDepartmentOwnerEmail());
						ownerDetail.setDepartmentOwnerName(deptDetails.getDepartmentOwner());
						ownerDetail.setPriority(deptDetails.getPriority());
						ownerDetails.add(ownerDetail);
					}
					departmentDetails.setOwnerDetails(ownerDetails);
					detailViewResponse.setDepartmentDetailsInfo(departmentDetails);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				}

			}
			if (requestId == null && childRequestId != null) {
				DepartmentOnboarding departmentApprover = departmentOnboardingRepository
						.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
				DepartmentOnboarding departmentReviewerApproved = departmentOnboardingRepository
						.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.APPROVE);
				DepartmentOnboarding departmentReviewer = departmentOnboardingRepository
						.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
				if (departmentApprover != null) {
					departmentDetails.setDepartmentBudget(departmentApprover.getBudget());
					departmentDetails.setDepartmentName(departmentApprover.getDepartmentName());
					departmentDetails.setCurrencyCode(departmentApprover.getBudgetCurrency());
					List<DepartmentOwnerDetails> departmentDetail = departmentOwnerRepository
							.findByDepartmentId(departmentApprover.getDepartmentId());
					List<CreateDepartmentOwnerDetails> ownerDetails = new ArrayList<>();
					for (DepartmentOwnerDetails deptDetails : departmentDetail) {
						CreateDepartmentOwnerDetails ownerDetail = new CreateDepartmentOwnerDetails();
						ownerDetail.setDepartmentOwnerEmailAddress(deptDetails.getDepartmentOwnerEmail());
						ownerDetail.setDepartmentOwnerName(deptDetails.getDepartmentOwner());
						ownerDetail.setPriority(deptDetails.getPriority());
						ownerDetails.add(ownerDetail);
					}
					departmentDetails.setOwnerDetails(ownerDetails);
					reviewerDetails.setApprovedByEmail(departmentReviewerApproved.getWorkGroupUserEmail());
					reviewerDetails.setWorkGroupName(departmentReviewerApproved.getWorkGroup());
					reviewerDetails.setComments(departmentReviewerApproved.getComments());
					reviewerDetails.setApprovalTimeStamp(departmentReviewerApproved.getEndDate());
					detailViewResponse.setDepartmentDetailsInfo(departmentDetails);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				} else {
					departmentDetails.setDepartmentBudget(departmentReviewer.getBudget());
					departmentDetails.setDepartmentName(departmentReviewer.getDepartmentName());
					departmentDetails.setCurrencyCode(departmentReviewer.getBudgetCurrency());
					List<DepartmentOwnerDetails> departmentDetail = departmentOwnerRepository
							.findByDepartmentId(departmentReviewer.getDepartmentId());
					List<CreateDepartmentOwnerDetails> ownerDetails = new ArrayList<>();
					for (DepartmentOwnerDetails deptDetails : departmentDetail) {
						CreateDepartmentOwnerDetails ownerDetail = new CreateDepartmentOwnerDetails();
						ownerDetail.setDepartmentOwnerEmailAddress(deptDetails.getDepartmentOwnerEmail());
						ownerDetail.setDepartmentOwnerName(deptDetails.getDepartmentOwner());
						ownerDetail.setPriority(deptDetails.getPriority());
						ownerDetails.add(ownerDetail);
					}
					departmentDetails.setOwnerDetails(ownerDetails);
					detailViewResponse.setDepartmentDetailsInfo(departmentDetails);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				}
			}
		}
		return new CommonResponse(HttpStatus.OK,
				new Response("departmentOnboardingRequestDetailViewResponse", detailViewResponse),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private List<DepartmentOnboarding> getDepartmentStatus(List<DepartmentOnboarding> departmentOnboardings) {
		List<DepartmentOnboarding> list = new ArrayList<>();
		List<DepartmentOnboarding> duplicateList = new ArrayList<>();
		for (DepartmentOnboarding details : departmentOnboardings) {
			DepartmentOnboarding viewResponse = new DepartmentOnboarding();
			DepartmentOnboarding step1childReq;
			DepartmentOnboarding step1req;
			DepartmentOnboarding step2ReqReject;
			DepartmentOnboarding step2ChildReqReject;
			DepartmentOnboarding step3ReqSuperApprove;
			DepartmentOnboarding step3ChildReqSuperApprove;

			if (details.getChildRequestNumber() != null) {
				step1childReq = departmentOnboardingRepository.findAllBySuperAdmin(details.getChildRequestNumber());
				if (step1childReq != null) {
					viewResponse = step1childReq;
				} else if (step1childReq == null) {
					step2ChildReqReject = departmentOnboardingRepository
							.findAllByChildReqReject(details.getChildRequestNumber());
					if (step2ChildReqReject != null) {
						viewResponse = step2ChildReqReject;
					} else if (step1childReq == null && step2ChildReqReject == null) {
						step3ReqSuperApprove = departmentOnboardingRepository
								.findChildReqSuperApprovee(details.getChildRequestNumber());
						viewResponse = step3ReqSuperApprove;
					}
				}
			}
			if (details.getRequestNumber() != null && details.getChildRequestNumber() == null) {
				step1req = departmentOnboardingRepository.findAllBySuperAdminRequestId(details.getRequestNumber());
				if (step1req != null) {
					viewResponse = step1req;
				} else if (step1req == null) {
					step2ReqReject = departmentOnboardingRepository.findAllByReject(details.getRequestNumber());
					if (step2ReqReject != null) {
						viewResponse = step2ReqReject;
					} else if (step1req == null && step2ReqReject == null) {
						step3ChildReqSuperApprove = departmentOnboardingRepository
								.findReqSuperApprove(details.getRequestNumber());
						viewResponse = step3ChildReqSuperApprove;
					}
				}

			}
			list.add(viewResponse);
		}
		for (DepartmentOnboarding requestTracking : list) {
			if (!duplicateList.contains(requestTracking)) {
				duplicateList.add(requestTracking);
			}
		}
		return duplicateList;
	}

	@Override
	public CommonResponse departmentUserListWithoutLicenseMapped(String licenseId, String departmentId)
			throws DataValidationException {
		List<LicenseUsersDetailsResponse> listOfUsers = new ArrayList<>();
		DepartmentDetails departmentDetails = departmentRepository.findByDepartmentId(departmentId);
		ApplicationLicenseDetails licenseDetails = licenseDetailsRepository.getUsersDetailsByLicenseId(licenseId);
		if (licenseDetails == null) {
			throw new DataValidationException("License ID " + licenseId + " Does not Exist", null, null);
		}
		if (departmentDetails != null) {
			List<UserDetails> userDetails = userDetailsRepository.getAllUsersByDepartmentId(departmentId);
			for (UserDetails user : userDetails) {
				LicenseUsersDetailsResponse detailsResponse = new LicenseUsersDetailsResponse();
				if ((!user.getLicenseId().contains(licenseDetails))
						&& (!user.getApplicationId().contains(licenseDetails.getApplicationId()))) {
					detailsResponse.setUserAvatar(user.getLogoUrl());
					detailsResponse.setUserEmail(user.getUserEmail());
					detailsResponse.setUserId(user.getUserId());
					detailsResponse.setUserName(user.getUserName());
					listOfUsers.add(detailsResponse);
				}
			}
		} else {
			throw new DataValidationException("Department ID " + departmentId + " Does not Exist", null, null);
		}
		return new CommonResponse(HttpStatus.OK, new Response("departmentUsersResponse", listOfUsers),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private void sendDefaultPasswordEmail(String userEmail, String ownerName, String verificationCode)
			throws IOException, TemplateException, MessagingException {
		String toAddress = userEmail;
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String url = "{{host}}/auth/create-password?userEmail={{userEmail}}&verificationCode={{verificationCode}}";
		url = url.replace("{{host}}", redirectUrl);
		url = url.replace("{{verificationCode}}", verificationCode);
		url = url.replace("{{userEmail}}", URLEncoder.encode(userEmail, StandardCharsets.UTF_8.toString()));
		String subject = Constant.USER_EMAIL_SUBJECT;
		Map<String, Object> model = new HashMap<>();
		Template t = config.getTemplate("set-password.html");
		String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
		content = content.replace("{{url}}", url);
		content = content.replace("{{name}}", ownerName);
		content = content.replace("{{code}}", verificationCode);
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
	public CommonResponse createPassword(DeptUserPasswordRequest pwdRequest)
			throws DataValidationException, IOException, TemplateException, AuthenticationException {
		VerificationDetails verificationDetails;
		UserLoginDetails userProfile;
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		if (!pwdRequest.getPassword().equals(pwdRequest.getConfirmPassword())) {
			throw new DataValidationException("Password and confirm password is doesn't match", null, null);
		}
		boolean status = isValid(pwdRequest.getPassword());
		userProfile = userLoginDetailsRepository.findByUserEmail(pwdRequest.getUserEmail());
		verificationDetails = verificationDetailsRepository.findByUserEmail(pwdRequest.getUserEmail());
		if (status) {
			String salt = SecureUtils.getSalt();
			String password = CommonUtil.createHash(pwdRequest.getPassword(), salt);
			userProfile.setPassword(password);
			verificationDetails.setSalt(salt);
			verificationDetails.setUserEmail(pwdRequest.getUserEmail());
			verificationDetails.setOpID(Constant.SAASPE);
			verificationDetails.setCreatedOn(new Date());
			verificationDetailsRepository.save(verificationDetails);
			if (Boolean.TRUE
					.equals(StringUtils.isBlank(verificationDetails.getEmailVerificationCode())
							|| Boolean.TRUE.equals(StringUtils.isBlank(pwdRequest.getUserEmail()))
							|| !verificationDetails.getEmailVerificationCode().equals(pwdRequest.getVerificationCode())
							|| verificationDetails.getEmailVerified())
					|| verificationDetails.getEmailVerificationCodeExpiryDate().compareTo(new Date()) < 0) {
				throw new DataValidationException(
						"Email Already Verified OR Check The Verification Code : " + pwdRequest.getUserEmail(), null,
						HttpStatus.CONFLICT);
			}
			verificationDetails.setEmailVerificationCode(null);
			verificationDetails.setFailedCount(0);
			verificationDetails.setEmailVerified(true);
			verificationDetails.setEmailVerificationCodeExpiryDate(null);
			verificationDetails.setEmailVerifiedDate(new Date());
			userLoginDetailsRepository.save(userProfile);
			verificationDetailsRepository.save(verificationDetails);
			commonResponse.setMessage("you can login by using your email with password");
			response.setAction("CreatePasswordResponse");
			response.setData(new ArrayList<>());
			commonResponse.setResponse(response);
			commonResponse.setStatus(HttpStatus.OK);
		}
		return commonResponse;
	}

	public boolean isValid(String password) {
		PasswordValidator validator = new PasswordValidator(Arrays.asList(new LengthRule(8, 30),
				new CharacterRule(EnglishCharacterData.UpperCase, 1),
				new CharacterRule(EnglishCharacterData.LowerCase, 1), new CharacterRule(EnglishCharacterData.Digit, 1),
				new CharacterRule(EnglishCharacterData.Special, 1), new WhitespaceRule()));
		RuleResult result = validator.validate(new PasswordData(password));
		return result.isValid();
	}

	@Override
	public CommonResponse deptApplicationUsage(String deptId) throws DataValidationException {
		List<DeptApplicationUsageAnalystics> analystics = new ArrayList<>();
		DepartmentDetails departmentDetail = departmentRepository.findByDepartmentId(deptId);
		if (departmentDetail != null) {
			if (!departmentDetail.getApplicationId().isEmpty()) {
				departmentDetail.getApplicationId().forEach(p -> {
					if (p.getActiveContracts() != null) {
						DeptApplicationUsageAnalystics analystic = new DeptApplicationUsageAnalystics();
						analystic.setApplicationId(p.getApplicationId());
						analystic.setApplicationName(p.getApplicationName());
						analystic.setUserCount(p.getUserDetails().stream().filter(c -> c.getEndDate() == null)
								.collect(Collectors.toList()).size());
						analystics.add(analystic);
					}
				});
			}
		} else {
			throw new DataValidationException("Department Not Found for the Given Id" + deptId, null, null);
		}
		analystics.sort(Comparator.comparing(DeptApplicationUsageAnalystics::getUserCount).reversed());
		return new CommonResponse(HttpStatus.OK, new Response(" DeptAnalysticsResponse ", analystics),
				SUCCESS_RESPONSE);
	}

	@Override
	public CommonResponse deptSpendAnalytics(String deptId) throws DataValidationException {
		List<DeptApplicationUsageAnalystics> analystics = new ArrayList<>();
		DepartmentDetails departmentDetail = departmentRepository.findByDepartmentId(deptId);
		if (departmentDetail != null) {
			if (!departmentDetail.getApplicationId().isEmpty()) {
				departmentDetail.getApplicationId().forEach(p -> {
					if (p.getActiveContracts() != null) {
						DeptApplicationUsageAnalystics analystic = new DeptApplicationUsageAnalystics();
						analystic.setApplicationId(p.getApplicationId());
						analystic.setApplicationName(p.getApplicationName());
						analystic.setUserCount(p.getUserDetails().stream().filter(c -> c.getEndDate() == null)
								.collect(Collectors.toList()).size());
						List<ApplicationContractDetails> applicationContractDetails = p.getContractDetails();
						BigDecimal cost = BigDecimal.valueOf(0.0);
						BigDecimal adminCost = BigDecimal.valueOf(0.0);
						for (ApplicationContractDetails contractDetails : applicationContractDetails) {
							if (contractDetails.getContractStatus().equalsIgnoreCase(Constant.ACTIVE)
									|| contractDetails.getContractStatus().equalsIgnoreCase("Expired")) {
								for (ApplicationLicenseDetails licenseDetails : contractDetails.getLicenseDetails()) {
									cost = cost.add(licenseDetails.getTotalCost(), MathContext.DECIMAL32);
									adminCost = adminCost.add(licenseDetails.getConvertedCost(), MathContext.DECIMAL32);
								}
								analystic.setTotalApplicationCost(cost);
								analystic.setTotalApplicationAdminCost(adminCost);
							}
							analystic.setCurrency(p.getContractDetails().get(0).getContractCurrency());
						}
						analystics.add(analystic);
					}
				});
			}
		} else {
			throw new DataValidationException(" Department Not Found for the Given Id " + deptId, null, null);
		}
		analystics.sort(Comparator.comparing(DeptApplicationUsageAnalystics::getTotalApplicationCost).reversed());
		return new CommonResponse(HttpStatus.OK, new Response(" DeptAnalysticsResponse", analystics), SUCCESS_RESPONSE);
	}

	@Override
	public CommonResponse deptBudgetAnalytics(String deptId) throws DataValidationException {
		DepartmentBudgetAnalyticsResponse analyticsResponse = new DepartmentBudgetAnalyticsResponse();
		DepartmentDetails departmentDetail = departmentRepository.findByDepartmentId(deptId);
		if (departmentDetail != null) {
			BigDecimal remainaing = departmentDetail.getBudget();
			List<DepartmentBudgetAnalytics> analyticsList = new ArrayList<>();
			analyticsResponse.setAllocatedAmount(departmentDetail.getBudget());
			analyticsResponse.setCurrency(departmentDetail.getBudgetCurrency());
			LocalDate today = LocalDate.now();
			LocalDate yearstartDate = today.withDayOfMonth(1).minusMonths(11);
			LocalDate start = yearstartDate;
			LocalDate end = yearstartDate.plusMonths(1).minusDays(1);
			for (int i = 0; i < 12; i++) {
				DepartmentBudgetAnalytics analytics = new DepartmentBudgetAnalytics();
				Month month = start.getMonth();
				BigDecimal spend = getDepartmentTotalSpend(departmentDetail, start, end);
				analytics.setSpend(spend);
				remainaing = remainaing.subtract(spend);
				if (remainaing.compareTo(new BigDecimal(0)) <= 0) {
					analytics.setRemaining(BigDecimal.ZERO);
				} else {
					analytics.setRemaining(remainaing);
				}
				analytics.setCurrency(userDetailsRepository.getCurrency().getCurrency());
				analytics.setMonth(
						month.toString().substring(0, 3) + " " + String.valueOf(start.getYear()).substring(2, 4));
				start = start.plusMonths(1);
				end = start.plusMonths(1).minusDays(1);
				analyticsList.add(analytics);
			}
			analyticsResponse.setData(analyticsList);
		} else {
			throw new DataValidationException(" Department Not Found for the Given Id  " + deptId, null, null);
		}
		return new CommonResponse(HttpStatus.OK, new Response("DeptAnalysticsResponse ", analyticsResponse),
				SUCCESS_RESPONSE);
	}

	public BigDecimal getDepartmentTotalSpend(DepartmentDetails departmentDetails, LocalDate firstDayOfQuarter,
			LocalDate lastDayOfQuarter) {
		BigDecimal totalSpend = new BigDecimal("0.0");
		List<ApplicationDetails> applicationDetails = applicationDetailsRepository
				.findByDepartmentName(departmentDetails.getDepartmentName());
		for (ApplicationDetails application : applicationDetails) {
			List<ApplicationContractDetails> contractDetails = applicationContractDetailsRepository
					.findActiveContractsBtwConEndDate(application.getApplicationId(),
							CommonUtil.convertLocalDatetoDate(firstDayOfQuarter),
							CommonUtil.convertLocalDatetoDate(lastDayOfQuarter));
			for (ApplicationContractDetails contract : contractDetails) {
				LocalDate localContractStart = contract.getContractStartDate().toInstant()
						.atZone(ZoneId.systemDefault()).toLocalDate();
				LocalDate localContractEnd = contract.getContractEndDate().toInstant().atZone(ZoneId.systemDefault())
						.toLocalDate();
				BigDecimal totalContractCost = BigDecimal.valueOf(
						contract.getLicenseDetails().stream().mapToLong(p -> p.getTotalCost().longValue()).sum());
				if (ContractType.annual(contract.getContractType())) {
					BigDecimal monthlyCost = totalContractCost.divide(
							BigDecimal.valueOf(contract.getContractTenure()).multiply(BigDecimal.valueOf(12)), 2,
							RoundingMode.FLOOR);
					if ((contract.getContractStartDate().before(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter)))
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
										}
										i++;
									}
								}
							}
							if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								int i = 6;
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
											totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
										}
										i = i + 6;
									}
								}
							}
							if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								int i = 3;
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
											totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
										}
										i = i + 3;
									}
								}
							}
							if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
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
											totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
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
										}
										i++;
									}
								}
							}
							if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								int i = 6;
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
											totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
										}
										i = i + 6;
									}
								}
							}
							if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								int i = 3;
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
											totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
										}
										i = i + 3;
									}
								}
							}
							if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
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
											totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
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
							}
							if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
								int i = 1;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusYears(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
									}
									i++;
								}
							}
							if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								int i = 6;
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
									}
									i = i + 6;
								}
							}
							if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								int i = 3;
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
									}
									i = i + 3;
								}
							}
							if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
								int i = 1;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
									}
									i++;
								}
							}

						}
						if (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
							if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(totalContractCost);
							}
							if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
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
										}
										i++;
									}
								}
							}
							if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
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
										}
										i = i + 6;
									}
								}
							}
							if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
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
										}
										i = i + 3;
									}
								}
							}
							if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
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
							}
							if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
								int i = 1;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusYears(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
										i++;
									}
								}
							}
							if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
								int i = 6;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
										i = i + 6;
									}
								}
							}
							if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
								int i = 3;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
										i = i + 3;
									}
								}
							}
							if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
								int i = 1;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
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
							}
							if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
								int i = 1;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusYears(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
										i++;
									}
								}
							}
							if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
								int i = 6;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
										i = i + 6;
									}
								}
							}
							if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
								int i = 3;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
										i = i + 3;
									}
								}
							}
							if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
								int i = 1;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
										i++;
									}
								}
							}

						}
						if (contract.getContractEndDate()
								.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
							if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(totalContractCost);
							}
							if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
								int i = 1;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusYears(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
										i++;
									}
								}
							}
							if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
								int i = 6;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
										i = i + 6;
									}
								}
							}
							if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
								int i = 3;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
										i = i + 3;
									}
								}
							}
							if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
								int i = 1;
								while (i != 0) {
									LocalDate plusYears = localContractStart.plusMonths(i);
									if (CommonUtil.convertLocalDatetoDate(plusYears)
											.after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
										i = 0;
									} else {
										totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
										i++;
									}
								}
							}

						}
						if (contract.getContractEndDate().after(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
							if (Constant.ONE_TIME.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(totalContractCost);
							}
							if (Constant.ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(12)));
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
										}
										i++;
									}
								}
							}
							if (Constant.SEMI_ANNUALLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(6)));
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
										}
										i = i + 6;
									}
								}
							}
							if (Constant.QUARTERLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(3)));
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
										}
										i = i + 3;
									}
								}
							}
							if (Constant.MONTHLY.equalsIgnoreCase(contract.getBillingFrequency())) {
								totalSpend = totalSpend.add(monthlyCost.multiply(new BigDecimal(1)));
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
					}
					if (contract.getContractStartDate()
							.compareTo(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter)) == 0) {
						totalSpend = totalSpend.add(totalContractCost);
					}
					if (contract.getContractStartDate().after(CommonUtil.convertLocalDatetoDate(firstDayOfQuarter))
							&& contract.getContractStartDate()
									.before(CommonUtil.convertLocalDatetoDate(lastDayOfQuarter))) {
						totalSpend = totalSpend.add(totalContractCost);
					}
				}

			}
		}
		return totalSpend;

	}

	@Override
	public CommonResponse sendBudgetEmail()
			throws IOException, MessagingException, TemplateException, InterruptedException {
		String toAddress = null;
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String subject = Constant.DEPARTMENT_BUDGET_ALERT;
		List<DepartmentDetails> departmentList = departmentRepository.findAll();
		for (DepartmentDetails department : departmentList) {
			Map<String, Object> model = new HashMap<>();
			List<DepartmentOwnerDetails> owners = departmentOwnerRepository
					.findByDepartmentId(department.getDepartmentId());
			DepartmentOwnerDetails primaryOwner = owners.stream().filter(owner -> owner.getPriority() == 1).findFirst()
					.orElse(null);
			if (primaryOwner != null) {
				toAddress = primaryOwner.getDepartmentOwnerEmail();
			} else {
				throw new NullPointerException("Primary Owner is null");
			}
			BigDecimal cost = BigDecimal.valueOf(0);
			BigDecimal adminCost = BigDecimal.valueOf(0);
			BigDecimal emailPercentage = BigDecimal.valueOf(100);
			for (ApplicationDetails application : department.getApplicationId()) {
				if (application.getActiveContracts() != null) {
					List<ApplicationContractDetails> applicationContractDetails = application.getContractDetails();
					for (ApplicationContractDetails contractDetails : applicationContractDetails) {
						if (contractDetails.getContractStatus().equalsIgnoreCase(Constant.ACTIVE)
								|| contractDetails.getContractStatus().equalsIgnoreCase("Expired")) {
							for (ApplicationLicenseDetails licenseDetails : contractDetails.getLicenseDetails()) {
								cost = cost.add(licenseDetails.getTotalCost(), MathContext.DECIMAL32);
								adminCost = adminCost.add(licenseDetails.getConvertedCost(), MathContext.DECIMAL32);
							}
						}
					}
				}
			}
			BigDecimal remainingBudget = department.getBudget().subtract(cost);
			Template t = config.getTemplate("department-budget-analysis.html");
			String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
			if (cost.compareTo(department.getBudget()) > 0) {
				content = content.replace("{{marketing}}", department.getDepartmentName());
				content = content.replace("{{allocatedBudget}}", department.getBudget().toString());
				content = content.replace("{{spend}}", cost.toString());
				content = content.replace("{{remaining}}", BigDecimal.valueOf(0).toString());
				content = content.replace("{{threshold}}", BigDecimal.valueOf(75).toString());
				content = content.replace("{{currentConsumed}}", BigDecimal.valueOf(100).toString());
			} else if (cost.compareTo(department.getBudget()) < 0 || cost.compareTo(department.getBudget()) == 0) {
				BigDecimal percentSpent = cost.divide(department.getBudget(), 2, RoundingMode.HALF_UP)
						.multiply(new BigDecimal(100));
				BigDecimal currentPercentage = BigDecimal.valueOf(100).max(percentSpent);
				emailPercentage = currentPercentage;
				if (currentPercentage.compareTo(BigDecimal.valueOf(75)) == 0
						|| currentPercentage.compareTo(BigDecimal.valueOf(75)) > 0) {
					content = content.replace("{{marketing}}", department.getDepartmentName());
					content = content.replace("{{allocatedBudget}}", department.getBudget().toString());
					content = content.replace("{{spend}}", cost.toString());
					content = content.replace("{{remaining}}", remainingBudget.toString());
					content = content.replace("{{threshold}}", BigDecimal.valueOf(75).toString());
					content = content.replace("{{currentConsumed}}", percentSpent.toString());
				}
			}
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
			if ((emailPercentage.compareTo(BigDecimal.valueOf(75)) == 0
					|| emailPercentage.compareTo(BigDecimal.valueOf(75)) > 0) && (budgetMailTrigger)) {
				mailSender.send(message);

			}
			Thread.sleep(30000);
		}
		return null;
	}
}