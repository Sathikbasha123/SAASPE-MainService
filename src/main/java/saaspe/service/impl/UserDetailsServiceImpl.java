package saaspe.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.NonNull;
import saaspe.adaptor.model.Details;
import saaspe.adaptor.model.GitlabDeleteUserRequest;
import saaspe.adaptor.model.RemoveUserRequest;
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
import saaspe.constant.Constant;
import saaspe.constant.ContractType;
import saaspe.constant.UnitPriceType;
import saaspe.dto.MetricsDAO;
import saaspe.entity.AdaptorFields;
import saaspe.entity.ApplicationContractDetails;
import saaspe.entity.ApplicationDetails;
import saaspe.entity.ApplicationLicenseDetails;
import saaspe.entity.ApplicationOwnerDetails;
import saaspe.entity.Applications;
import saaspe.entity.AtlassianJiraUsers;
import saaspe.entity.DepartmentDetails;
import saaspe.entity.DepartmentOwnerDetails;
import saaspe.entity.Departments;
import saaspe.entity.EmailWorkFlowStatus;
import saaspe.entity.ProjectDetails;
import saaspe.entity.ProjectManagerDetails;
import saaspe.entity.Projects;
import saaspe.entity.SequenceGenerator;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLastLoginDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.entity.UserOnboarding;
import saaspe.entity.Users;
import saaspe.entity.VerificationDetails;
import saaspe.exception.AuthenticationException;
import saaspe.exception.DataValidationException;
import saaspe.model.AdaptorCustomField;
import saaspe.model.AdaptorKeyValues;
import saaspe.model.AdminListResponse;
import saaspe.model.ApplicationDetailsRes;
import saaspe.model.CommonResponse;
import saaspe.model.CreateAdminRequest;
import saaspe.model.DepartmentOwnerRes;
import saaspe.model.EmailWorkFlowResponse;
import saaspe.model.OwnerShipDetails;
import saaspe.model.OwnerShipDetailsResponse;
import saaspe.model.PermissionByRoleResponse;
import saaspe.model.ProfileData;
import saaspe.model.ProjectDetailsRes;
import saaspe.model.Response;
import saaspe.model.TopAppByUsercountResponse;
import saaspe.model.TopAppsByUserCountResponse;
import saaspe.model.UserAdaptorApplicationFields;
import saaspe.model.UserDetailsOverview;
import saaspe.model.UserDetailsOverviewResponse;
import saaspe.model.UserDetailsRequest;
import saaspe.model.UserDetailsResponse;
import saaspe.model.UserDetialViewResponse;
import saaspe.model.UserEmailsRemoveRequest;
import saaspe.model.UserLastLoginRequest;
import saaspe.model.UserLastLoginResponse;
import saaspe.model.UserListView;
import saaspe.model.UserListViewResp;
import saaspe.model.UserOnboardingRequest;
import saaspe.model.UserRemovalRequest;
import saaspe.model.UserUpdateRequest;
import saaspe.model.applicatoinUpdateOwnershipRequest;
import saaspe.model.updateUserOwnershipRequest;
import saaspe.model.userDetailsSpendAnalystics;
import saaspe.repository.AdaptorFieldsRepository;
import saaspe.repository.ApplicationDetailsRepository;
import saaspe.repository.ApplicationLicenseDetailsRepository;
import saaspe.repository.ApplicationOwnerRepository;
import saaspe.repository.ApplicationsRepository;
import saaspe.repository.AtlassianJiraUsersRepository;
import saaspe.repository.DepartmentOwnerRepository;
import saaspe.repository.DepartmentRepository;
import saaspe.repository.DepartmentsRepository;
import saaspe.repository.EmailWorkFlowRepository;
import saaspe.repository.ProjectDetailsRepository;
import saaspe.repository.ProjectOwnerRepository;
import saaspe.repository.ProjectsRepository;
import saaspe.repository.SequenceGeneratorRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.repository.UserLastLoginDetailRepository;
import saaspe.repository.UserLoginDetailsRepository;
import saaspe.repository.UserOnboardingDetailsRepository;
import saaspe.repository.UsersRepository;
import saaspe.repository.VerificationDetailsRepository;
import saaspe.service.UserDetailsService;
import saaspe.utils.CommonUtil;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserDetailsRepository userDetailsRepo;
	


	@Autowired
	private UserOnboardingDetailsRepository userOnboardingDetailsRepo;

	@Autowired
	private ApplicationDetailsRepository applicationDetailsRepo;

	private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	@Autowired
	private ApplicationLicenseDetailsRepository applicationLicenseDetailsRepo;

	@Autowired
	private SequenceGeneratorRepository generatorRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private VerificationDetailsRepository verificationDetailsRepository;

	@Autowired
	private UserLoginDetailsRepository userLoginDetailsRepository;

	@Autowired
	private SequenceGeneratorRepository sequenceGeneratorRepository;

	@Autowired
	private UserLastLoginDetailRepository userLastLoginDetailRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private EmailWorkFlowRepository emailWorkFlowRepository;

	@Autowired
	private ProjectDetailsRepository projectDetailsRepository;

	@Autowired
	private ApplicationOwnerRepository applicationOwnerRepository;

	@Autowired
	private ApplicationDetailsRepository applicationDetailsRepository;

	@Autowired
	private ProjectOwnerRepository projectOwnerRepository;

	@Autowired
	private DepartmentOwnerRepository departmentOwnerRepository;

	@Autowired
	private Configuration config;

	@Autowired
	private AtlassianJiraUsersRepository atlassianJiraUsersRepository;

	@Value("${redirecturl.path}")
	private String redirectUrl;

	@Value("${sendgrid.domain.name}")
	private String mailDomainName;

	@Value("${sendgrid.domain.sendername}")
	private String senderName;

	@Value("${logos.avatar.url}")
	private String avatarUrl;

	@Autowired
	private HubSpotWrapperService hubSpotService;

	@Autowired
	private ConfluenceWrapperService confluenceWrapperService;

	@Autowired
	private DatadogWrapperService datadogWrapperService;

	@Autowired
	private ZohoCRMWrapperService zohoCRMservice;

	@Autowired
	private QuickBookWrapperService quickbooksService;

	@Autowired
	private JiraWrapperService jiraWrapperService;

	@Autowired
	private GitlabWrapperService gitlabService;

	@Autowired
	private GithubWrapperService githubService;

	@Autowired
	private Microsoft365WrapperService microsoft365WrapperService;

	@Autowired
	private AdaptorFieldsRepository adaptorFieldsRepository;

	@Autowired
	private ZohoPeopleService zohoPeopleService;

	@Autowired
	private ZohoAnalyticsService zohoAnalyticsService;
	@Autowired
	private SalesforceService salesforceService;

	@Autowired
	private FreshdeskWrapperService freshdeskService;

	@Autowired
	private ZoomWrapperService zoomWrapperService;
	
	@Autowired
	private UsersRepository usersRepository;
	
	@Autowired
	private DepartmentsRepository departmentsRepository;
	
	@Autowired
	private ApplicationsRepository applicationRepository;
	
	@Autowired
	private ProjectsRepository projectRepository;
	
	@Override
	public CommonResponse addUserDetails(UserDetailsRequest user)
			throws DataValidationException, AuthenticationException {
		UserDetails userDetails = new UserDetails();
		Users users = new Users();
		if (!userDetailsRepo.existsById(user.getUserEmail())) {
			userDetails.setUserEmail(user.getUserEmail());
			userDetails.setDepartmentId(user.getDepartmentId());
			userDetails.setUserDesigination(user.getUserDesigination());
			userDetails.setUserId(user.getUserId());
			userDetails.setUserType(user.getUserType());
			userDetails.setUserName(user.getUserName());
			userDetails.setJobLevel(user.getJobLevel());
			userDetails.setUserStatus(user.getUserStatus());
			userDetails.setUserReportingManager(user.getUserReportingManager());
			userDetails.setApplicationId(user.getApplicationId());
			userDetails.setJoiningDate(user.getJoiningDate());
			userDetails.setTeam(user.getTeam());
			userDetails.setCreatedOn(new Date());
			userDetails.setUserStatus(Constant.ACTIVE);
			userDetails.setCreatedBy(user.getCreatedBy());
			userDetails.setBuID(user.getBuID());
			userDetails.setLogoUrl(user.getLogoUrl());
			userDetailsRepo.save(userDetails);

			
			List<ApplicationDetails> applicationIdsList = user.getApplicationId();
			String applicationIds = applicationIdsList.stream().map(ApplicationDetails::toString)
					.collect(Collectors.joining(","));
			
			DepartmentDetails departmentDetails = departmentRepository.findByDepartmentId(user.getDepartmentId().getDepartmentId());

			users.setAppUser(false);
			users.setBuID(Constant.BUID);
		    users.setDepartmentId(departmentDetails.getDepartmentId());
			users.setDepartmentName(user.getDepartmentId().getDepartmentName());
			users.setIdentityId(users.getIdentityId());
			users.setJobLevel(user.getJobLevel());
			users.setJoiningDate(user.getJoiningDate());
			users.setLastLoginTime(user.getLastLoginTime());
			users.setLogoUrl(user.getLogoUrl());
			users.setOpID(user.getOpID());	
			users.setTeam(user.getTeam());
			users.setUpdatedBy(user.getUpdatedBy());
			users.setUpdatedOn(user.getUpdatedOn());
			users.setUserApplicationId(applicationIds);
			users.setUserCreatedBy(user.getUserEmail());
			users.setUserCreatedOn(user.getCreatedOn());
			users.setUserDesignation(user.getUserDesigination());
			users.setUserEmail(user.getUserEmail());
			users.setUserId(user.getUserId());
			users.setUserName(user.getUserName());
			users.setUserReportingManager(user.getUserReportingManager());
			users.setUserRole(user.getUserRole());
			users.setUserType(user.getUserType());
			usersRepository.save(users);
			
			
			} else {
			throw new DataValidationException(Constant.USER_DETAILS_ALL_READY_REGISTERED, "400",
					HttpStatus.BAD_REQUEST);
		}
		return new CommonResponse(HttpStatus.OK, new Response("UserDetailsResponse", new ArrayList<>()),
				"UserDetials Saved Successfully");
	}

	@Override
	public CommonResponse removeUserDetailsByUserEmail(String userEmail) throws DataValidationException {
		UserLastLoginDetails lastloginbyusermail = userLastLoginDetailRepository.findByUsersEmail(userEmail);
		UserDetailsResponse response = new UserDetailsResponse();
		if (userDetailsRepo.existsById(userEmail)) {
			UserDetails userDetails = userDetailsRepo.findByuserEmail(userEmail);
			Users users = usersRepository.findByuserEmail(userEmail);
			userDetails.setEndDate(new Date());
			users.setUserEndDate(new Date());
			if (userDetails.getUserRole() != null) {
				userDetails.setUserRole(null);
				userDetails.setUserAccess(null);
				users.setUserRole(null);
			}
			userDetailsRepo.save(userDetails);
			
			if (lastloginbyusermail != null) {
				lastloginbyusermail.setEndDate(new Date());
				userLastLoginDetailRepository.save(lastloginbyusermail);
			}
			usersRepository.save(users);
			response.setMessage(Constant.USER_DATA_REMOVE);
		} else {
			throw new DataValidationException(Constant.USER_DETAILS_NOT_FOUND, "404", HttpStatus.NOT_FOUND);
		}
		return response;
	}

	@Override
	public CommonResponse getUserDetails() throws DataValidationException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		List<UserDetailsResponse> list = new ArrayList<>();
		List<UserDetails> userDetails = userDetailsRepo.getByRolesExclude();
		if (userDetails != null) {
			for (UserDetails details : userDetails) {
				UserDetailsResponse userDetailsResponse = new UserDetailsResponse();
				DepartmentDetails departmentDetails = departmentRepository
						.findByDepartmentId(details.getDepartmentId().getDepartmentId());
				userDetailsResponse.setDepartmentName(departmentDetails.getDepartmentName());
				userDetailsResponse.setDepartmentId(departmentDetails.getDepartmentId());
				userDetailsResponse.setUserName(details.getUserName());
				userDetailsResponse.setUserId(details.getUserId());
				userDetailsResponse.setUserEmail(details.getUserEmail());
				userDetailsResponse.setUserType(details.getUserType());
				list.add(userDetailsResponse);
				response.setData(list);
			}
			response.setAction("UserDetailsResponse");
			commonResponse.setStatus(HttpStatus.OK);
			commonResponse.setMessage("Details retrieved successfully");
			commonResponse.setResponse(response);
		} else {
			throw new DataValidationException("Atleast One User Should Be Avaliable to OnBoard a Application", null,
					null);
		}
		return commonResponse;
	}

	@Override
	public CommonResponse getUserDetailsByEmial(String email) {
		UserDetails details = null;
		if (userDetailsRepo.existsById(email)) {
			details = userDetailsRepo.findByuserEmail(email);
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.USER_DETAILS, details),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	@Transactional
	public CommonResponse modifyUserDetails(String userId, UserUpdateRequest userUpateRequest)
			throws DataValidationException {
		if (userDetailsRepo.findByUserId(userId) != null) {
			
			UserDetails req = userDetailsRepo.findByUserId(userId); 
			Users user = usersRepository.findByUserId(userId);
			
			if (departmentRepository.findByDepartmentName(userUpateRequest.getUserDepartment()) != null) {
				DepartmentDetails details = departmentRepository
						.findByDepartmentName(userUpateRequest.getUserDepartment());
				req.setDepartmentId(details);
				user.setDepartmentId(details.getDepartmentId());
			} else {
				throw new DataValidationException("Please given correct user-department name", null, null);
			}
			req.setJoiningDate(userUpateRequest.getUserOnboardedDate());
			req.setUserReportingManager(userUpateRequest.getUserReportingManager());
			req.setUserDesigination(userUpateRequest.getUserDesignation());
			req.setUserType(userUpateRequest.getUserType());
			userDetailsRepo.save(req);
			
			user.setJoiningDate(userUpateRequest.getUserOnboardedDate());
			user.setUserReportingManager(userUpateRequest.getUserReportingManager());
			user.setUserDesignation(userUpateRequest.getUserDesignation());
			user.setUserType(userUpateRequest.getUserType());
			usersRepository.save(user);
			
		} else {
			throw new DataValidationException(Constant.USER_DETAILS_NOT_FOUND, "404", HttpStatus.NOT_FOUND);
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.USER_DETAILS, "USER-DETAILS UPDATED!"),
				Constant.DETAILS_UPDATED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getUsersDetialView() throws DataValidationException {
		UserDetialViewResponse res = new UserDetialViewResponse();
		List<UserDetialViewResponse> resList = new ArrayList<>();
		if (!userDetailsRepo.findAll().isEmpty()) {
			List<UserDetails> user = userDetailsRepo.findAll();
			List<UserOnboarding> details = userOnboardingDetailsRepo.findAll();
			List<String> userNames = user.stream().map(UserDetails::getUserName).collect(Collectors.toList());
			List<String> logoUrl = user.stream().map(UserDetails::getLogoUrl).collect(Collectors.toList());
			List<String> status = user.stream().map(UserDetails::getUserStatus).collect(Collectors.toList());
			List<String> designation = user.stream().map(UserDetails::getUserDesigination).collect(Collectors.toList());
			List<String> type = user.stream().map(UserDetails::getUserType).collect(Collectors.toList());
			List<DepartmentDetails> department = user.stream().map(UserDetails::getDepartmentId)
					.collect(Collectors.toList());
			List<String> dept = department.stream().map(DepartmentDetails::getDepartmentId)
					.collect(Collectors.toList());
			List<Date> onboadringDate = details.stream().map(UserOnboarding::getOnboardDate)
					.collect(Collectors.toList());
			res.setUserCount(userNames.size());
			res.setUsers(userNames);
			res.setDepartment(dept);
			res.setDesignation(designation);
			res.setLogoUrl(logoUrl);
			res.setOnboardingDate(onboadringDate);
			res.setLogoUrl(logoUrl);
			res.setStatus(status);
			res.setType(type);
			resList.add(res);
		} else {
			throw new DataValidationException(Constant.USER_DETAILS_NOT_FOUND, "404", HttpStatus.NOT_FOUND);
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.USER_DETAILS, resList),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getTopAppsByUsercount() {
		List<MetricsDAO> applicationLicenseDetails = userDetailsRepo.getTopAppsByUsercount();
		TopAppsByUserCountResponse topAppsByUserCountResponse = new TopAppsByUserCountResponse();
		List<TopAppByUsercountResponse> list = new ArrayList<>();
		String applicationId = null;
		String licesneId = null;
		for (MetricsDAO dao : applicationLicenseDetails) {
			TopAppByUsercountResponse topAppByUsercountResponse = new TopAppByUsercountResponse();
			ApplicationDetails applicationDetails = applicationDetailsRepo.findByApplicationId(dao.getApplicationId());
			ApplicationLicenseDetails licenseDetails = applicationLicenseDetailsRepo
					.findByLicenseId(dao.getLicenseId());
			if (applicationDetails != null) {
				applicationId = applicationDetails.getApplicationId();
			}
			if (licenseDetails != null) {
				licesneId = licenseDetails.getLicenseId();
			}
			topAppByUsercountResponse.setApplicationId(applicationId);
			topAppByUsercountResponse.setLicenseId(licesneId);
			topAppByUsercountResponse.setUsersCount(dao.getCount());
			list.add(topAppByUsercountResponse);
		}
		topAppsByUserCountResponse.setTopAppByUserEmail(list);
		return new CommonResponse(HttpStatus.OK, new Response("TopAppsByUsercount", topAppsByUserCountResponse),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getUserListView() throws DataValidationException {
		List<UserListView> listViews = new ArrayList<>();
		List<UserDetails> user = userDetailsRepo.getByRolesExclude();
		UserListViewResp resp = new UserListViewResp();
		for (UserDetails data : user) {
			List<ApplicationDetailsRes> list = new ArrayList<>();
			UserListView userListView = new UserListView();
			userListView.setUserAvatar(data.getLogoUrl());
			userListView.setUserId(data.getUserId());
			userListView.setUserName(data.getUserName());
			userListView.setUserEmail(data.getUserEmail());
			userListView.setUserDesignation(data.getUserDesigination());
			if (data.getApplicationId() != null) {
				for (ApplicationDetails details : data.getApplicationId()) {
					ApplicationDetailsRes res = new ApplicationDetailsRes();
					res.setApplicationName(details.getApplicationName());
					res.setApplicationLogo(details.getLogoUrl());
					list.add(res);
				}
			}
			userListView.setUserApplications(list);
			listViews.add(userListView);
		}
		resp.setUserListViews(listViews);

		if (resp == null || resp.getUserListViews().isEmpty()) {

			return new CommonResponse(HttpStatus.NOT_FOUND, new Response("UserListView", resp), "Users not found");
		}
		return new CommonResponse(HttpStatus.OK, new Response("UserListView", resp), "Details Retrieved Successfully");
	}

	@Override
	@Transactional
	public CommonResponse saveUserOnboardingData(List<UserOnboardingRequest> userOnboardingRequests)
			throws DataValidationException {
		UserOnboarding user = new UserOnboarding();
		UserDetails data = new UserDetails();
		Users users = new Users();
		for (UserOnboardingRequest req : userOnboardingRequests) {
			String name = "USER_0";
			Integer sequence = generatorRepository.getUserOnboardingSequence();
			name = name.concat(sequence.toString());
			SequenceGenerator updateSequence = generatorRepository.getById(1);
			updateSequence.setUserOnboarding(++sequence);
			generatorRepository.save(updateSequence);
			user.setUserId(name);
			user.setFirstName(req.getFirstName());
			user.setLastName(req.getLastName());
			user.setUserType(req.getUserType());
			user.setCreatedOn(new Date());
			user.setUserDesignation(req.getUserDesignation());
			
			users.setUserId(name);
			users.setUserType(req.getUserType());
			users.setUserCreatedOn(new Date());
			users.setUserDesignation(req.getUserDesignation());
			
			DepartmentDetails details = departmentRepository.findByDepartmentName(req.getUserDepartment());
			
			user.setDepartmentId(details);
			user.setUserReportingManager(req.getUserReportingManager());
			user.setJoiningDate(req.getUserJoiningDate());
			userOnboardingDetailsRepo.save(user);
			data.setUserId(user.getUserId());
			data.setUserName(req.getFirstName() + req.getLastName());
			data.setUserEmail(req.getUserEmail());
			data.setUserType(req.getUserType());
			data.setCreatedOn(new Date());
			data.setUserDesigination(req.getUserDesignation());
			data.setDepartmentId(details);
			data.setApplicationId(details.getApplicationId());
			data.setUserReportingManager(req.getUserReportingManager());
			data.setJoiningDate(req.getUserJoiningDate());
			userDetailsRepo.save(data);
			
			
			users.setDepartmentId(details.getDepartmentId());
			users.setUserReportingManager(req.getUserReportingManager());
			users.setJoiningDate(req.getUserJoiningDate());
			users.setUserId(user.getUserId());
			users.setUserName(req.getFirstName()+ req.getLastName());
			users.setUserEmail(req.getUserEmail());
			users.setUserType(req.getUserType());
			users.setUserCreatedOn(new Date());
			users.setUserDesignation(req.getUserDesignation());
			users.setAppUser(false);
		
			usersRepository.save(users);
			
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.USER_DETAILS, "Users added successfully!"),
				Constant.DETAILS_UPDATED_SUCCESSFULLY);
	}

	@Transactional
	public CommonResponse deleteByUserEmails(UserEmailsRemoveRequest userEmails)
			throws DataValidationException, JsonProcessingException {
	
		for (UserRemovalRequest user : userEmails.getUserRemovalRequest()) {
			String userEmail = user.getUserEmail();
			if (userDetailsRepo.existsById(userEmail)) {
				log.info("email :{}", userEmail);
				UserDetails userDetails = userDetailsRepo.findByuserEmail(userEmail);
				Users users = usersRepository.findByuserEmail(userEmail);
				 if (userDetails.getLicenseId() != null) {
		                List<ApplicationLicenseDetails> licenseList = userDetails.getLicenseId();
		               
		                for (ApplicationLicenseDetails licenseDetails : licenseList) {
		                    licenseDetails.setLicenseUnMapped(licenseDetails.getLicenseUnMapped() + 1);
		                    licenseDetails.setLicenseMapped(licenseDetails.getLicenseMapped() - 1);
		                    applicationLicenseDetailsRepo.save(licenseDetails);
		                    Applications license = applicationRepository.findByLicenseId(licenseDetails.getLicenseId());
		                    license.setLicenseMapped(licenseDetails.getLicenseMapped());
		                    license.setLicenseUnmapped(licenseDetails.getLicenseUnMapped());
		                    applicationRepository.save(license);
		                    String productName = licenseDetails.getProductName();
		                    if (productName != null && licenseDetails.getApplicationId().getApplicationName().equalsIgnoreCase("Microsoft365")) {
		                            String applicationId = licenseList.get(0).getApplicationId().getApplicationId();
		                            unAssignMicrosoft365License(applicationId, userEmail, productName);  
		                    }
		                }
		               
		                userDetails.getLicenseId().clear();
		                
		            }
				 
					userDetails.setEndDate(new Date());
					userDetailsRepo.save(userDetails);
					
					users.setUserEndDate(new Date());
					usersRepository.save(users);

				UserLastLoginDetails lastloginbyusermail = userLastLoginDetailRepository.findByUsersEmail(userEmail);

				if (lastloginbyusermail != null) {
					lastloginbyusermail.setEndDate(new Date());
					userLastLoginDetailRepository.save(lastloginbyusermail);
				}
				
				List<ApplicationDetails> applications = userDetails.getApplicationId();
				try {
					for (ApplicationDetails applicationDetails : applications) {
						if (applicationDetails.getApplicationName().equalsIgnoreCase("Hubspot")) {
							deleteUsersFromHubSpot(applicationDetails.getApplicationId(), userEmail);
						}
						if (applicationDetails.getApplicationName().equalsIgnoreCase("ZohoCRM")) {
							deleteZohoCRMUsers(applicationDetails.getApplicationId(), userEmail);
						}
						if (applicationDetails.getApplicationName().equalsIgnoreCase("Quickbooks")) {
							deleteQuickBooksUser(applicationDetails.getApplicationId(), userEmail);
						}
						if (applicationDetails.getApplicationName().equalsIgnoreCase(Constant.GIT_LAB)) {
							deleteGitlabUser(applicationDetails.getApplicationId(), userEmail, user);
						}
						if (applicationDetails.getApplicationName().equalsIgnoreCase("GitHub")) {
							deleteGitHubUser(applicationDetails.getApplicationId(), user);
						}
						if (applicationDetails.getApplicationName().equalsIgnoreCase("Confluence")) {
							deleteUsersFromConfluence(applicationDetails.getApplicationId(), userEmail);
						}
						if (applicationDetails.getApplicationName().equalsIgnoreCase("Datadog")) {
							deleteUsersFromDatadog(applicationDetails.getApplicationId(), userEmail);
						}
						if (applicationDetails.getApplicationName().equalsIgnoreCase("Zoho People")) {
							revokeZohoPeopleLicense(applicationDetails.getApplicationId(), userEmail);
						}
						if (applicationDetails.getApplicationName().equalsIgnoreCase(Constant.ZOHOANALYTICS)) {
							zohoAnalyticsService.revokeAccess(userEmail, applicationDetails.getApplicationId());
						}
						if (applicationDetails.getApplicationName().equalsIgnoreCase("Salesforce")) {
							salesforceService.revokeAccess(applicationDetails.getApplicationId(),
									userDetails.getUserEmail(), userDetails.getUserId());
						}
			
						if (applicationDetails.getApplicationName().equalsIgnoreCase("JIRA")) {
							deleteUsersFromJira(applicationDetails.getApplicationId(), userEmail);
						}

						if (applicationDetails.getApplicationName().equalsIgnoreCase("Freshdesk")) {
							freshdeskService.revokeUserAccess(applicationDetails.getApplicationId(), userEmail);
						}
						if (applicationDetails.getApplicationName().equalsIgnoreCase("Zoom")) {
							deleteUsersFromZoom(applicationDetails.getApplicationId(), userEmail);
						}
					}
				} catch (Exception e) {
					e.getMessage();
				}
			} else

			{
				throw new DataValidationException(Constant.USER_DATA_SHOULD_HAVE, "400", HttpStatus.BAD_REQUEST);
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.USER_DETAILS, Constant.USER_DATA_REMOVE),
				Constant.DETAILS_UPDATED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getUserDetailsOverview(String userId) throws DataValidationException {
		UserDetailsOverviewResponse response = new UserDetailsOverviewResponse();
		UserDetailsOverview userOverview = new UserDetailsOverview();
		UserDetails userDetails = userDetailsRepo.findByUserId(userId);
		Integer countInteger = 0;
		String empty = "";
		if (userDetails != null) {
			response.setUserName(userDetails.getUserName());
			response.setUserLogo(userDetails.getLogoUrl());

			if (userDetails.getDepartmentId() != null) {
				response.setUserDepartmentId(userDetails.getDepartmentId().getDepartmentId());
				response.setUserDepartmentName(userDetails.getDepartmentId().getDepartmentName());
			} else {
				response.setUserDepartmentId(empty);
				response.setUserDepartmentName(empty);
			}
			response.setUserDesignation(userDetails.getUserDesigination());
			if (userDetails.getUserReportingManager() != null) {
				response.setUserReportingManager(userDetails.getUserReportingManager());
			} else {
				response.setUserReportingManager(empty);
			}
			response.setUserEmail(userDetails.getUserEmail());
			response.setUserOnboardedDate(userDetails.getJoiningDate());
			response.setUserStatus(userDetails.getUserStatus());
			response.setUserType(userDetails.getUserType());
			List<ApplicationDetails> details = userDetails.getApplicationId();
			List<ApplicationDetails> activeapp = userDetails.getApplicationId().stream()
					.filter(s -> s.getEndDate() == null && s.getActiveContracts() != null).collect(Collectors.toList());

			List<String> applicationStatus = details.stream().map(ApplicationDetails::getApplicationStatus)
					.collect(Collectors.toList());
			List<ApplicationDetails> totalactiveappsinuserdept = userDetails.getDepartmentId().getApplicationId()
					.stream()
					.filter(s -> s.getEndDate() == null
							&& (s.getSubscriptionDetails() != null || !s.getContractDetails().isEmpty()))
					.collect(Collectors.toList());
			List<ApplicationDetails> finallist = new ArrayList<>();
			for (ApplicationDetails app : activeapp) {
				if (app.getDepartmentDetails().stream().anyMatch(department -> department.getDepartmentId()
						.equals(userDetails.getDepartmentId().getDepartmentId()))) {
					finallist.add(app);
				}
			}
			if (!applicationStatus.isEmpty()) {
				for (String iterator : applicationStatus) {
					if (iterator.equalsIgnoreCase(Constant.ACTIVE)) {
						countInteger = countInteger + 1;
						response.setUserActiveApplications(countInteger);
					}
				}
			} else {
				response.setUserActiveApplications(0);
			}
			if (!userDetails.getLicenseId().isEmpty()) {
				BigDecimal avg = BigDecimal.valueOf(0);
				BigDecimal avgadminSpend = BigDecimal.valueOf(0);
				for (ApplicationLicenseDetails usersLicense : userDetails.getLicenseId()) {
					ApplicationContractDetails contract = usersLicense.getContractId();
					Map<String, BigDecimal> avgcost = getAverageMontlyAmount(contract, usersLicense);
					avg = avg.add(avgcost.get("totalCost"));
					avgadminSpend = avgadminSpend.add(avgcost.get("adminCost"));
				}
				response.setUserAvgMonthlySpend(avg);
				response.setUserAvgMonthlyAdminSpend(avgadminSpend);
			} else {
				response.setUserAvgMonthlyAdminSpend(BigDecimal.valueOf(0));
				response.setUserAvgMonthlySpend(BigDecimal.valueOf(0));
			}
			response.setUserApplicationsCount(userDetails.getApplicationId().size());
			Integer spendInteger2 = 0;
			if (!finallist.isEmpty()) {
				spendInteger2 = (int) (((double) finallist.size() / totalactiveappsinuserdept.size()) * 100);
			}
			response.setUserAvgUsage(spendInteger2);
			userOverview.setGetUserDetailsOverview(response);
		} else {
			throw new DataValidationException(Constant.USER_DETAILS_NOT_FOUND, "404", HttpStatus.NOT_FOUND);
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.USER_DETAILS, userOverview),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private Map<String, BigDecimal> getAverageMontlyAmount(ApplicationContractDetails contractDetails,
			ApplicationLicenseDetails license) {
		Map<String, BigDecimal> map = new HashMap<>();
		BigDecimal totalSpend = BigDecimal.valueOf(0.0);
		BigDecimal totalAVSpend = BigDecimal.valueOf(0.0);
		BigDecimal totalAVAdminSpend = BigDecimal.valueOf(0.0);
		totalSpend = totalSpend.add(license.getUnitPrice(), MathContext.DECIMAL32);
		BigDecimal divide = license.getTotalCost().divide(license.getUnitPrice(), 2, RoundingMode.FLOOR);
		BigDecimal convertedPerLicenseCost = license.getConvertedCost().divide(divide, 2, RoundingMode.FLOOR);
		if (ContractType.annual(contractDetails.getContractType())) {
			if (UnitPriceType.perYear(license.getUnitPriceType())) {
				totalAVSpend = totalSpend.divide(new BigDecimal(12), 2, RoundingMode.FLOOR);
				totalAVAdminSpend = convertedPerLicenseCost.divide(new BigDecimal(12), 2, RoundingMode.FLOOR);
			} else if (UnitPriceType.perContracttenure(license.getUnitPriceType())) {
				totalAVSpend = totalSpend.divide(
						BigDecimal.valueOf(contractDetails.getContractTenure()).multiply(BigDecimal.valueOf(12)), 2,
						RoundingMode.FLOOR);
				totalAVAdminSpend = convertedPerLicenseCost.divide(
						BigDecimal.valueOf(contractDetails.getContractTenure()).multiply(BigDecimal.valueOf(12)), 2,
						RoundingMode.FLOOR);
			} else {
				totalAVSpend = totalAVSpend.add(license.getUnitPrice());
				totalAVAdminSpend = totalAVAdminSpend.add(convertedPerLicenseCost);
			}
		} else {
			totalAVSpend = totalAVSpend.add(license.getUnitPrice());
			totalAVAdminSpend = totalAVAdminSpend.add(convertedPerLicenseCost);
		}
		map.put("totalCost", totalAVSpend);
		map.put("adminCost", totalAVAdminSpend);
		return map;
	}

	public CommonResponse getProfile(UserLoginDetails profile, HttpServletRequest request)
			throws DataValidationException {
		ProfileData userData = new ProfileData();
		String token = request.getHeader(Constant.HEADER_STRING);
		String provider = request.getHeader(Constant.HEADER_PROVIDER_STRING);
		if (provider == null || provider.equalsIgnoreCase(Constant.HEADER_PROVIDER_NAME)) {
			if (userDetailsRepo.findByuserEmail(profile.getEmailAddress()) != null) {
				UserDetails loginDetails = userDetailsRepo.findByuserEmail(profile.getEmailAddress());
				userData.setUserName(loginDetails.getUserName());
				userData.setUserLogo(loginDetails.getLogoUrl());
			} else {
				throw new DataValidationException("user profile not found", "404", HttpStatus.NOT_FOUND);
			}
		} else if (provider.equalsIgnoreCase("azure")) {
			DecodedJWT jwt = JWT.decode(token.replace("Bearer ", ""));
			String name = jwt.getClaim("name").asString();
			userData.setUserName(name);
			userData.setUserLogo(avatarUrl);
		}
		return new CommonResponse(HttpStatus.OK, new Response("userProfileResponse", userData),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getOwnerShipList(String userId) throws DataValidationException {
		OwnerShipDetails ownerShipDetails = new OwnerShipDetails();
		OwnerShipDetailsResponse ownerShipDetailsResponse = new OwnerShipDetailsResponse();
		Boolean isOwner = false;
		UserDetails userDetails = userDetailsRepo.findByUserId(userId);
		if (userDetails == null) {
			throw new DataValidationException("Provide valid userId", null, null);
		}
		List<ApplicationDetailsRes> appsList = new ArrayList<>();
		List<ProjectDetailsRes> projectList = new ArrayList<>();
		List<ApplicationDetails> applicationDetails = applicationDetailsRepo
				.findByApplicationOwnerEmail(userDetails.getUserEmail());
		List<ProjectDetails> projects = projectDetailsRepository.getProjectByEmail(userDetails.getUserEmail());
		List<ApplicationOwnerDetails> applicationowners = applicationOwnerRepository
				.findByEmailId(userDetails.getUserEmail());
		List<ProjectManagerDetails> projectowners = projectOwnerRepository
				.findByProjectByEmail(userDetails.getUserEmail());
		if (!applicationDetails.isEmpty()) {
			for (ApplicationDetails details : applicationDetails) {

				isOwner = true;
				ownerShipDetails.setIsOwner(isOwner);
				ApplicationDetailsRes applicationDetailsRes = new ApplicationDetailsRes();
				applicationDetailsRes.setApplicationId(details.getApplicationId());
				applicationDetailsRes.setApplicationLogo(details.getLogoUrl());
				applicationDetailsRes.setApplicationName(details.getApplicationName());
				appsList.add(applicationDetailsRes);
			}
		}
		if (!applicationowners.isEmpty()) {
			for (ApplicationOwnerDetails details : applicationowners) {
				ApplicationDetails application = applicationDetailsRepository
						.findByApplicationId(details.getApplicationId());
				if ((application != null) && (application.getActiveContracts() != null)) {
					isOwner = true;
					ownerShipDetails.setIsOwner(isOwner);
					ApplicationDetailsRes applicationDetailsRes = new ApplicationDetailsRes();
					applicationDetailsRes.setApplicationId(details.getApplicationId());
					applicationDetailsRes.setApplicationLogo(application.getLogoUrl());
					applicationDetailsRes.setApplicationName(application.getApplicationName());
					appsList.add(applicationDetailsRes);
				}

			}
		}
		if (!projects.isEmpty()) {
			for (ProjectDetails details : projects) {
				isOwner = true;
				ownerShipDetails.setIsOwner(isOwner);
				ProjectDetailsRes projectDetailsRes = new ProjectDetailsRes();
				projectDetailsRes.setProjectId(details.getProjectId());
				projectDetailsRes.setProjectName(details.getProjectName());
				projectList.add(projectDetailsRes);
			}
		}
		if (!projectowners.isEmpty()) {
			for (ProjectManagerDetails details : projectowners) {
				if (projectList.stream().filter((p -> p.getProjectId().equalsIgnoreCase(details.getProjectId())))
						.collect(Collectors.toList()).isEmpty()) {
					isOwner = true;
					ownerShipDetails.setIsOwner(isOwner);
					ProjectDetailsRes projectDetailsRes = new ProjectDetailsRes();
					projectDetailsRes.setProjectId(details.getProjectId());
					projectDetailsRes.setProjectName(details.getProjectName());
					projectList.add(projectDetailsRes);
				}
			}
		}
		DepartmentOwnerRes departmentListView = new DepartmentOwnerRes();
		if (userDetails.getDepartmentId() != null) {
			List<DepartmentOwnerDetails> owners = departmentOwnerRepository
					.findByDepartmentOwnerEmail(userDetails.getUserEmail());
			DepartmentDetails deptowner = departmentRepository.findByDeptOwnerEmail(userDetails.getUserEmail());
			if ((deptowner != null) || (!owners.isEmpty())) {
				isOwner = true;
				ownerShipDetails.setIsOwner(isOwner);
				departmentListView.setDepartmentId(userDetails.getDepartmentId().getDepartmentId());
				departmentListView.setDepartmentName(userDetails.getDepartmentId().getDepartmentName());
			}
		}
		ownerShipDetailsResponse.setDepartmentDetails(departmentListView);
		ownerShipDetailsResponse.setApplicationDetails(appsList);
		ownerShipDetailsResponse.setProjectDetails(projectList);
		ownerShipDetails.setOwnerShipDetails(ownerShipDetailsResponse);
		ownerShipDetails.setIsOwner(isOwner);
		List<AdaptorCustomField> applicationFields = new LinkedList<>();
		for (ApplicationDetails applicationDetail : userDetails.getApplicationId()) {
			if (adaptorFieldsRepository.existsByApplicationNameIgnoreCase(applicationDetail.getApplicationName())) {
				if (applicationDetail.getApplicationName().equalsIgnoreCase(Constant.GIT_LAB)) {
					boolean hasInvited = gitlabService.hasInvited(userDetails.getUserEmail(),
							applicationDetail.getApplicationId());
					if (hasInvited)
						continue;
				}
				AdaptorCustomField applicationField = new AdaptorCustomField();
				List<AdaptorKeyValues> adaptorFields = new LinkedList<>();
				applicationField.setApplicationName(applicationDetail.getApplicationName());
				AdaptorFields adaptorField = adaptorFieldsRepository.findByApplicationNameIgnoreCase(Constant.GIT_LAB);
				JSONArray jsonArray = new JSONArray(adaptorField.getAdaptorFields());
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.optJSONObject(i);
					if (jsonObject != null) {
						AdaptorKeyValues keyValue = new AdaptorKeyValues(jsonObject);
						adaptorFields.add(keyValue);
					}
				}
				applicationField.setFields(adaptorFields);
				applicationFields.add(applicationField);

			}
		}

		if (!applicationFields.isEmpty()) {
			ownerShipDetails.setHasCustomFields(true);
			ownerShipDetails.setCustomFields(applicationFields);

		} else {
			ownerShipDetails.setHasCustomFields(false);
		}
		return new CommonResponse(HttpStatus.OK, new Response("checkUserOwnershipResponse", ownerShipDetails),
				"Workflow action Completed");
	}

	@Override
	@Transactional
	public CommonResponse getOwnerShipTransfer(updateUserOwnershipRequest ownershipRequest)
			throws DataValidationException, IOException, TemplateException, MessagingException {
		if (ownershipRequest.getApplicationDetails() != null) {
			for (applicatoinUpdateOwnershipRequest request : ownershipRequest.getApplicationDetails()) {
				String applicationOwnerEmail = request.getApplicationOwnerEmail().trim();
				ApplicationDetails applicationDetails = applicationDetailsRepo
						.findByApplicationId(request.getApplicationId());
				if (applicationDetails == null) {
					throw new DataValidationException(
							"Application with ID " + request.getApplicationId() + "not  found.", null, null);
				}
				DepartmentDetails departmentDetails = departmentRepository
						.findByDepartmentName(applicationDetails.getOwnerDepartment());
				if (departmentDetails == null) {
					throw new DataValidationException(
							"Department " + applicationDetails.getOwnerDepartment() + "not found.", null, null);
				}
				UserDetails oldUser = userDetailsRepo.findByuserEmail(applicationOwnerEmail);
				if (oldUser == null) {
					throw new DataValidationException("User with email " + applicationOwnerEmail + " not found. ", null,
							null);
				}
				if (!departmentDetails.getUserDetails().stream()
						.filter(p -> p.getUserEmail().equalsIgnoreCase(oldUser.getUserEmail()))
						.collect(Collectors.toList()).isEmpty()) {
					List<ApplicationOwnerDetails> owners = applicationOwnerRepository
							.findByApplicationId(request.getApplicationId());
					if (!owners.isEmpty()) {
						ApplicationOwnerDetails owner = owners.get(0);
						if (owner.getEndDate() != null) {
							for (ApplicationOwnerDetails potentialOwner : owners) {
								if (potentialOwner.getEndDate() == null) {
									owner = potentialOwner;
									break;
								}
							}
						}
						owner.setEndDate(new Date());
						ApplicationOwnerDetails newUser = new ApplicationOwnerDetails();
						newUser.setApplicationId(owner.getApplicationId());
						newUser.setOwnerDepartment(owner.getOwnerDepartment());
						newUser.setPriority(owner.getPriority());
						newUser.setOwner(oldUser.getUserName());
						newUser.setCreatedOn(new Date());
						newUser.setOwnerEmail(applicationOwnerEmail);
						applicationOwnerRepository.save(newUser);
						applicationOwnerRepository.save(owner);
						
						
					}
				} else {
					throw new DataValidationException("User with " + applicationOwnerEmail
							+ " doesn't exist in the Department " + applicationDetails.getOwnerDepartment(), null,
							null);
				}
			}			
		}
		if (ownershipRequest.getDepartmentDetails() != null) {
			DepartmentDetails departmentDetails = departmentRepository
					.findByDepartmentId(ownershipRequest.getDepartmentDetails().getDepartmentId());
			Departments departmentsDetails = departmentsRepository
					.findByDepartmentId(ownershipRequest.getDepartmentDetails().getDepartmentId());
			List<DepartmentOwnerDetails> owners = departmentOwnerRepository
					.findByDepartmentId(ownershipRequest.getDepartmentDetails().getDepartmentId());
			List<Departments> departmentOwners = departmentsRepository
					.findByDepartmentIds(ownershipRequest.getDepartmentDetails().getDepartmentId());
			List<UserDetails> userDetails = departmentDetails.getUserDetails();
			
			UserDetails userEmail = userDetailsRepo
					.findByuserEmail(ownershipRequest.getDepartmentDetails().getDepartmentOwnerEmail());
			Users usersEmail = usersRepository.
					findByuserEmail(ownershipRequest.getDepartmentDetails().getDepartmentOwnerEmail());
			if (userEmail == null) {
				throw new DataValidationException(
						"Provided User email for Department "
								+ ownershipRequest.getDepartmentDetails().getDepartmentOwnerEmail() + " Not Found",
						null, null);
			}
			if (userDetails.contains(userEmail)) {
				if (!owners.isEmpty()) {
					DepartmentOwnerDetails owner = owners.get(0);
					if (owner.getEndDate() != null) {
						for (DepartmentOwnerDetails potentialOwner : owners) {
							if (potentialOwner.getEndDate() == null) {
								owner = potentialOwner;
								break;
							}
						}
					}		
					owner.setEndDate(new Date());
					DepartmentOwnerDetails newOwner = new DepartmentOwnerDetails();
					newOwner.setDepartmentName(departmentDetails.getDepartmentName());
					newOwner.setDepartmentOwner(userEmail.getUserName());
					newOwner.setCreatedOn(new Date());
					newOwner.setPriority(owner.getPriority());
					newOwner.setIsOnboarding(false);
					newOwner.setDeptId(departmentDetails.getDepartmentId());
					newOwner.setDepartmentOwnerEmail(ownershipRequest.getDepartmentDetails().getDepartmentOwnerEmail());
					departmentOwnerRepository.save(newOwner);
					departmentOwnerRepository.save(owner);
					

				}
				if(!departmentOwners.isEmpty()) {
					Departments departmentsOwner = departmentOwners.get(0);
					if(departmentsOwner.getEndDate()!=null) {
						for(Departments potentialOwners : departmentOwners) {
							if(potentialOwners.getEndDate()==null) {
								departmentsOwner = potentialOwners;
								break;
							}
						}
					}
					departmentsOwner.setEndDate(new Date());
					Departments newOwners = new Departments();
					newOwners.setDepartmentName(departmentsDetails.getDepartmentName());
					newOwners.setDepartmentOwner(usersEmail.getUserName());
					newOwners.setDepartmentCreatedOn(new Date());
					newOwners.setPriority(departmentsOwner.getPriority());
					newOwners.setIsOnboarding(false);
					newOwners.setDepartmentId(departmentsDetails.getDepartmentId());
					newOwners.setDepartmentOwnerEmail(ownershipRequest.getDepartmentDetails().getDepartmentOwnerEmail());
					departmentsRepository.save(newOwners);
					departmentsRepository.save(departmentsOwner);
				}

				if (userEmail.getUserAccess() == null) {
					VerificationDetails verificationdetails = new VerificationDetails();
					UserLoginDetails userProfile = new UserLoginDetails();
					Users users = new Users();
					Date emailVerificationExpiredate = DateUtils.addMinutes(Calendar.getInstance().getTime(),
							Constant.EMAIL_VERIFICATION_CODE_EXPIRE_DATE);
					int verificationcode = CommonUtil.getRandomNumber(100000, 999999);
					verificationdetails.setUserEmail(ownershipRequest.getDepartmentDetails().getDepartmentOwnerEmail());
					verificationdetails.setEmailVerificationCode(String.valueOf(verificationcode));
					verificationdetails.setEmailVerificationCodeExpiryDate(emailVerificationExpiredate);
					verificationdetails.setEmailVerified(false);
					verificationdetails.setEmailVerifiedDate(new Date());
					verificationdetails.setOpID("SAASPE");
					verificationdetails.setCreatedOn(new Date());
					verificationDetailsRepository.save(verificationdetails);
					userProfile.setEmailAddress(userEmail.getUserEmail());
					userProfile.setCreatedOn(new Date());
					userProfile.setFirstName(userEmail.getUserName());
					userLoginDetailsRepository.save(userProfile);
					userEmail.setUserAccess(Constant.ROLE_CONTRIBUTOR);
					userEmail.setUserRole("CONTRIBUTOR");
					userEmail.setUpdatedOn(new Date());
					userEmail.setUserStatus(Constant.ACTIVE);
					userDetailsRepo.save(userEmail);
				    users.setUserRole("CONTRIBUTOR");
				    users.setUpdatedOn(new Date());
				    usersRepository.save(users);
					sendDefaultPasswordEmail(userEmail.getUserEmail(), userEmail.getUserName(),
							String.valueOf(verificationcode));
				}
			} else {
				throw new DataValidationException(
						"User with " + ownershipRequest.getDepartmentDetails().getDepartmentOwnerEmail()
								+ " Doesn't Exist in the Department " + departmentDetails.getDepartmentName(),
						null, null);
			}
		}
		if (ownershipRequest.getProjectDetails() != null) {
			for (applicatoinUpdateOwnershipRequest request : ownershipRequest.getProjectDetails()) {
				String projectOwnerEmail = request.getProjectOwnerEmail().trim();

				ProjectDetails project = projectDetailsRepository.findByProjectId(request.getProjectId());
				if (project == null) {
					throw new DataValidationException("Project with ID " + request.getProjectId() + " doesn't exist",
							null, null);
				}
				List<ProjectManagerDetails> owners = projectOwnerRepository.findByProjectId(request.getProjectId());
				if (owners.isEmpty()) {
					throw new DataValidationException(
							"No project owners found for project with ID " + request.getProjectId(), null, null);
				}
				UserDetails user = userDetailsRepo.findByuserEmail(projectOwnerEmail);
				if (user == null) {
					throw new DataValidationException("User with email " + projectOwnerEmail + " doesn't exist", null,
							null);
				}
				ProjectManagerDetails owner = owners.get(0);
				if (owner.getEndDate() != null) {
					for (ProjectManagerDetails potentialOwner : owners) {
						if (potentialOwner.getEndDate() == null) {
							owner = potentialOwner;
							break;
						}
					}
				}
				owner.setEndDate(new Date());
				ProjectManagerDetails newOwner = new ProjectManagerDetails();
				newOwner.setProjectId(request.getProjectId());
				newOwner.setPriority(owner.getPriority());
				newOwner.setProjectName(project.getProjectName());
				newOwner.setCreatedOn(new Date());
				newOwner.setStartDate(new Date());
				newOwner.setProjectManagerEmail(projectOwnerEmail);
				projectOwnerRepository.save(newOwner);
				projectOwnerRepository.save(owner);
				List<Projects> projectOwner = projectRepository.findByOwnerEmail(owner.getProjectManagerEmail(), owner.getProjectId());
				for(Projects projects : projectOwner) {
					projects.setProjectManagerEndDate(new Date());
					projectRepository.save(projects);
					Projects projectNewOwner = new Projects();
					try {
						BeanUtils.copyProperties(projectNewOwner, projects);
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					projectNewOwner.setId(0);
					projectNewOwner.setProjectId(request.getProjectId());
					projectNewOwner.setPriority(owner.getPriority());
					projectNewOwner.setProjectName(project.getProjectName());
					projectNewOwner.setProjectManagerCreatedOn(new Date());
					projectNewOwner.setProjectManagerStartDate(new Date());
					projectNewOwner.setProjectManagerEmail(projectOwnerEmail);
					projectNewOwner.setProjectManagerEndDate(null);
					projectRepository.save(projectNewOwner);
				}
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("ownershipTransferResponse", new ArrayList<>()),
				"Ownership transferred successfully");
	}

	@Override
	public CommonResponse getAllAdmins() throws DataValidationException {
		List<UserDetails> adminList = userDetailsRepo.getAllAdmins();
		if (adminList.isEmpty()) {
			throw new DataValidationException("No Records Found Try to insert Some Records", null, null);
		}
		List<AdminListResponse> adminListResponses = new ArrayList<>();
		for (UserDetails userDetail : adminList) {
			AdminListResponse adminResponse = new AdminListResponse();
			adminResponse.setUserId(userDetail.getUserId());
			adminResponse.setUserName(userDetail.getUserName());
			adminResponse.setUserEmail(userDetail.getUserEmail());
			adminResponse.setRole(userDetail.getUserRole());
			adminResponse.setLastLogin(userDetail.getLastLoginTime());
			adminResponse.setUserAvatar(userDetail.getLogoUrl());
			adminResponse.setSaaspeLastLogin(userDetail.getSaaspeLastLogin());
			adminListResponses.add(adminResponse);
		}
		return new CommonResponse(HttpStatus.OK, new Response("All Admin ListView Response", adminListResponses),
				"Data Fetched Successfullty");
	}

	@Override
	public CommonResponse getPermissionByRole(@NonNull String role) throws DataValidationException {
		List<PermissionByRoleResponse> list = new ArrayList<>();
		PermissionByRoleResponse dept = new PermissionByRoleResponse();
		PermissionByRoleResponse user = new PermissionByRoleResponse();
		PermissionByRoleResponse app = new PermissionByRoleResponse();
		PermissionByRoleResponse proj = new PermissionByRoleResponse();
		PermissionByRoleResponse onboarding = new PermissionByRoleResponse();
		PermissionByRoleResponse track = new PermissionByRoleResponse();
		if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("reviewer")
				|| role.equalsIgnoreCase("approver")) {
			dept.setAdd(false);
			dept.setEdit(false);
			dept.setName(Constant.DEPARTMENT);
			dept.setDelete(false);
			dept.setReview(false);
			dept.setApprove(false);
			dept.setView(true);

			user.setAdd(false);
			user.setEdit(false);
			user.setName(Constant.USER);
			user.setDelete(false);
			user.setReview(false);
			user.setApprove(false);
			user.setView(true);

			app.setAdd(false);
			app.setEdit(false);
			app.setName(Constant.APPLICATION);
			app.setDelete(false);
			app.setReview(false);
			app.setApprove(false);
			app.setView(true);

			proj.setAdd(false);
			proj.setEdit(false);
			proj.setName(Constant.PROJECT);
			proj.setDelete(false);
			proj.setReview(false);
			proj.setApprove(false);
			proj.setView(true);

			onboarding.setAdd(false);
			onboarding.setEdit(false);
			onboarding.setName(Constant.ON_BOARDING);
			onboarding.setDelete(false);
			onboarding.setReview(true);
			onboarding.setApprove(true);
			onboarding.setView(true);

			track.setAdd(false);
			track.setEdit(false);
			track.setView(false);
			track.setName(Constant.TRACK);
			track.setDelete(false);
			track.setReview(false);
			track.setApprove(false);
		} else if (role.equalsIgnoreCase("contributor")) {
			dept.setAdd(true);
			dept.setEdit(true);
			dept.setName(Constant.DEPARTMENT);
			dept.setDelete(true);
			dept.setReview(false);
			dept.setApprove(false);
			dept.setView(true);

			user.setAdd(true);
			user.setEdit(true);
			user.setName(Constant.USER);
			user.setDelete(true);
			user.setReview(false);
			user.setApprove(false);
			user.setView(true);

			app.setAdd(true);
			app.setEdit(true);
			app.setName(Constant.APPLICATION);
			app.setDelete(true);
			app.setReview(false);
			app.setApprove(false);
			app.setView(true);

			proj.setAdd(true);
			proj.setEdit(true);
			proj.setName(Constant.PROJECT);
			proj.setDelete(true);
			proj.setReview(false);
			proj.setApprove(false);
			proj.setView(true);

			onboarding.setAdd(false);
			onboarding.setEdit(false);
			onboarding.setName(Constant.ON_BOARDING);
			onboarding.setDelete(false);
			onboarding.setReview(false);
			onboarding.setApprove(false);
			onboarding.setView(false);

			track.setAdd(false);
			track.setEdit(false);
			track.setView(true);
			track.setName(Constant.TRACK);
			track.setDelete(false);
			track.setReview(false);
			track.setApprove(false);
		} else {
			throw new DataValidationException("Provide Valid Role", null, null);
		}
		list.add(user);
		list.add(onboarding);
		list.add(track);
		list.add(proj);
		list.add(app);
		list.add(dept);
		return new CommonResponse(HttpStatus.OK, new Response("Get Permission By Role Response", list),
				"Data Fetched Successfullty");
	}

	@Override
	@Transactional
	public CommonResponse createAdminUsers(CreateAdminRequest adminRequest, UserLoginDetails profile)
			throws DataValidationException, IOException, TemplateException, MessagingException {
		if (adminRequest.getUserEmail().trim().length() == 0) {
			throw new DataValidationException("Email must not be null", null, null);
		} else if (adminRequest.getFirstName().trim().length() == 0) {
			throw new DataValidationException("User first name  must not be null", null, null);
		} else if (adminRequest.getLastName().trim().length() == 0) {
			throw new DataValidationException("User last name  must not be null", null, null);
		} else if (adminRequest.getRole().trim().length() == 0) {
			throw new DataValidationException("Role must not be null", null, null);
		} else if (userDetailsRepo.findByuserEmail(adminRequest.getUserEmail().trim()) != null) {
			throw new DataValidationException("User Already exist Try to add differenet User", null, null);
		}
		UserDetails details = new UserDetails();
		Users users = new Users();
		String namUeser = "USER_0";
		Integer sequence = sequenceGeneratorRepository.getUserOnboardingSequence();
		namUeser = namUeser.concat(sequence.toString());
		SequenceGenerator updateSequence1 = sequenceGeneratorRepository.getById(1);
		updateSequence1.setUserOnboarding(++sequence);
		details.setUserId(namUeser);
		details.setCreatedOn(new Date());
		details.setJoiningDate(new Date());
		details.setUserEmail(adminRequest.getUserEmail());
		details.setUserName(adminRequest.getFirstName().concat(" ").concat(adminRequest.getLastName()));
		details.setCreatedBy(profile.getEmailAddress());
		details.setBuID("BUID_02");
		details.setLogoUrl(avatarUrl);
		details.setUserStatus(Constant.ACTIVE);
		
		users.setUserId(namUeser);
		users.setUserCreatedOn(new Date());
		users.setJoiningDate(new Date());
		users.setUserEmail(adminRequest.getUserEmail());
		users.setUserName(adminRequest.getFirstName().concat(" ").concat(adminRequest.getLastName()));
		users.setUserCreatedBy(profile.getEmailAddress());
		users.setBuID("BUID_02");
		users.setLogoUrl(avatarUrl);
		users.setAppUser(false);
		
		
		if (Constant.USER_ROLE.stream().noneMatch(adminRequest.getRole()::equals)) {
			throw new DataValidationException("User role does not match", "400", HttpStatus.BAD_REQUEST);
		}
		if (adminRequest.getRole().trim().equalsIgnoreCase("contributor")) {
			if (adminRequest.getUserMobileNumber() == null || adminRequest.getUserMobileNumber().length() == 0) {
				throw new DataValidationException("Phone Number must not be null for Role Contributor", null, null);
			}
			details.setMobileNumber(adminRequest.getUserMobileNumber());
			details.setUserRole("CONTRIBUTOR");
			details.setUserAccess(Constant.ROLE_CONTRIBUTOR);			
			users.setMobileNumber(adminRequest.getUserMobileNumber());
			users.setUserRole("CONTRIBUTOR");
		} else if (adminRequest.getRole().trim().equalsIgnoreCase("super_admin")) {
			details.setUserRole("SUPER_ADMIN");
			details.setUserAccess(Constant.ROLE_SUPER_ADMIN);
			users.setUserRole("SUPER_ADMIN");
		} else if (adminRequest.getRole().trim().equalsIgnoreCase("reviewer")) {
			details.setUserRole("REVIEWER");
			details.setUserAccess(Constant.ROLE_REVIEWER);
			users.setUserRole("REVIEWER");
		} else if (adminRequest.getRole().trim().equalsIgnoreCase("approver")) {
			details.setUserRole("APPROVER");
			details.setUserAccess(Constant.ROLE_APPROVER);
			users.setUserRole("APPROVER");
		} else if (adminRequest.getRole().trim().equalsIgnoreCase("support")) {
			details.setUserRole("SUPPORT");
			details.setUserAccess(Constant.ROLE_SUPPORT);
			users.setUserRole("SUPPORT");
		} else if (adminRequest.getRole().trim().equalsIgnoreCase("custom")) {
			if (adminRequest.getAccess().isEmpty()) {
				throw new DataValidationException("Access must not be null", null, null);
			}
			String commaSeparated = String.join(", ", adminRequest.getAccess());
			details.setUserRole("CUSTOM");
			details.setUserAccess(commaSeparated);
			users.setUserRole("CUSTOM");
		} else {
			throw new DataValidationException("Provide Valid Role", null, null);
		}
		UserLoginDetails userProfile = new UserLoginDetails();
		sequenceGeneratorRepository.save(updateSequence1);
		userDetailsRepo.save(details);
		usersRepository.save(users);
		VerificationDetails verificationDetails = new VerificationDetails();
		Date emailVerificationExpiredate = DateUtils.addMinutes(Calendar.getInstance().getTime(),
				Constant.EMAIL_VERIFICATION_CODE_EXPIRE_DATE);
		int verificationCode = CommonUtil.getRandomNumber(100000, 999999);
		verificationDetails.setUserEmail(adminRequest.getUserEmail());
		verificationDetails.setEmailVerified(false);
		verificationDetails.setOpID("SAASPE");
		verificationDetails.setCreatedOn(new Date());
		verificationDetails.setEmailVerificationCode(String.valueOf(verificationCode));
		verificationDetails.setEmailVerificationCodeExpiryDate(emailVerificationExpiredate);
		verificationDetails.setEmailVerificationCodeSendDate(new Date());
		userProfile.setEmailAddress(adminRequest.getUserEmail());
		userProfile.setCreatedOn(new Date());
		userProfile.setFirstName(adminRequest.getFirstName());
		userProfile.setLastName(adminRequest.getLastName());
		users.setUserEmail(adminRequest.getUserEmail());
		users.setUserCreatedOn(new Date());
		usersRepository.save(users);
		userLoginDetailsRepository.save(userProfile);
		verificationDetailsRepository.save(verificationDetails);
		sendDefaultPasswordEmail(adminRequest.getUserEmail(),
				adminRequest.getFirstName().concat(" ").concat(adminRequest.getLastName()),
				String.valueOf(verificationCode));
		return new CommonResponse(HttpStatus.OK, new Response("CreateAdminResponse", new ArrayList<>()),
				"Data Created Successfullty");
	}

	@Override
	@Transactional
	public CommonResponse sendEmialToUser(UserLastLoginRequest userLastLoginRequest)
			throws DataValidationException, IOException, TemplateException, MessagingException {
		String limit = userLastLoginRequest.getDateRange();
		EmailWorkFlowStatus emailWorkFlowStatus = new EmailWorkFlowStatus();
		ApplicationDetails applicationDetails = applicationDetailsRepo
				.findByApplicationId(userLastLoginRequest.getApplicationId());
		List<UserLastLoginDetails> list = new ArrayList<>();
		List<Users> lists = new ArrayList<>();
		if (applicationDetails == null) {
			throw new DataValidationException(
					"applcaition details not found or deleted: " + userLastLoginRequest.getApplicationId(), "404",
					HttpStatus.NOT_FOUND);
		}
		if (userLastLoginDetailRepository.findApplicationId(userLastLoginRequest.getApplicationId()).isEmpty()) {
			throw new DataValidationException("does'nt have any last login user data for the given application : "
					+ userLastLoginRequest.getApplicationId(), "404", HttpStatus.NOT_FOUND);
		}
		if (userLastLoginRequest.getDateRange().equalsIgnoreCase("91")) {
			LocalDate end = LocalDate.now().minusDays(91);
			UserLastLoginDetails details = userLastLoginDetailRepository
					.getLastUserLoginDate(userLastLoginRequest.getApplicationId());
			LocalDate d1 = LocalDate.parse(end.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
			LocalDate d2 = LocalDate.parse(
					new SimpleDateFormat(Constant.DATE_FORMAT).format(details.getLastLoginTime()),
					DateTimeFormatter.ISO_LOCAL_DATE);
			Duration diff = Duration.between(d1.atStartOfDay(), d2.atStartOfDay());
			LocalDate start = LocalDate.now().minusDays(diff.toDays());
			ZoneId defaultZoneId = ZoneId.systemDefault();
			Date endDate = Date.from(end.atStartOfDay(defaultZoneId).toInstant());
			Date.from(start.atStartOfDay(defaultZoneId).toInstant());

			List<UserLastLoginDetails> lastLoginDetailsList = userLastLoginDetailRepository
					.lastLoginUsersDataafter90days(endDate, userLastLoginRequest.getApplicationId());
			for (UserLastLoginDetails userLastLoginDetails : lastLoginDetailsList) {
				UserDetails userdepartment = userDetailsRepo.findByuserEmail(userLastLoginDetails.getUserEmail());
				if (userdepartment != null && userdepartment.getDepartmentId() != null) {
					ApplicationDetails depapplicationDetails = applicationDetailsRepo
							.findByApplicationId(userLastLoginRequest.getApplicationId());
					if (depapplicationDetails.getApplicationId()
							.equalsIgnoreCase(userLastLoginDetails.getApplicationId())) {
						for (DepartmentDetails dep : depapplicationDetails.getDepartmentDetails()) {
							if (dep.getDepartmentId()
									.equalsIgnoreCase(userdepartment.getDepartmentId().getDepartmentId())
									&& userLastLoginDetails.getLastLoginTime() != null) {
								list.add(userLastLoginDetails);
								
								
							}
						}
					}
				}
			}
			if (list.isEmpty())
				throw new DataValidationException(userLastLoginRequest.getApplicationId() + Constant.NO_LAST_LOGIN,
						"403", HttpStatus.NOT_FOUND);
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet(Constant.USER_LOGIN);
			Map<String, Object[]> data = new TreeMap<>();
			data.put("1", new Object[] { Constant.USER_NAME, Constant.USER_EMAIL, Constant.USER_DESIGNATION,
					Constant.USER_LAST_LOGIN });
			Integer number = 2;
			
			
			
			for (UserLastLoginDetails userDetails : list) {
				UserDetails userDetails2 = userDetailsRepo.findByuserEmail(userDetails.getUserEmail());
				data.put(number.toString(), new Object[] { userDetails.getUserName(), userDetails.getUserEmail(),
						userDetails2.getUserDesigination(), userDetails.getLastLoginTime().toString() });
				number = number + 1;
			}
			// Iterate over data and write to sheet
			Set<String> keyset = data.keySet();
			int rownum = 0;
			for (String key : keyset) {
				Row row = sheet.createRow(rownum++);
				Object[] objArr = data.get(key);
				int cellnum = 0;
				for (Object obj : objArr) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);
				}
			}
			try {
				File tmplFile = new File(Constant.USER_LOGIN);
				FileOutputStream out = new FileOutputStream(tmplFile);
				workbook.write(out);
				out.close();
				if (sheet.getLastRowNum() != 0 && sheet.getRow(0) != null) {
					String toAddress = userLastLoginRequest.getOwnerEmail();
					MimeMessage message = mailSender.createMimeMessage();
					MimeMessageHelper helper = new MimeMessageHelper(message, true, CharEncoding.UTF_8);
					String subject = userLastLoginRequest.getSubject();
					Multipart multipart = new MimeMultipart();
					MimeBodyPart attachPart = new MimeBodyPart();
					String attachFile = Constant.USER_LOGIN;
					FileDataSource source = new FileDataSource(attachFile);
					attachPart.setDataHandler(new DataHandler(source));
					attachPart.setFileName(new File(attachFile).getName());
					multipart.addBodyPart(attachPart);
					Map<String, Object> model = new HashMap<>();
					Template t = config.getTemplate(Constant.WORK_FLOW);
					String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
					content = content.replace(Constant.APP_NAME, applicationDetails.getApplicationName());
					content = content.replace(Constant.SELECTED_RANGE,
							"Before " + LocalDate.now().minusDays(91).toString());
					content = content.replace(Constant.MESSAGE, userLastLoginRequest.getMessage());
					helper.setText(content, true);
					helper.addAttachment(attachFile, source);
					helper.setFrom(mailDomainName, senderName);
					helper.setTo(toAddress);
					helper.setSubject(subject);
					mailSender.send(message);
					if (tmplFile.delete()) {
						emailWorkFlowStatus.setCreatedOn(new Date());
					}
					emailWorkFlowStatus.setApplicationId(userLastLoginRequest.getApplicationId());
					emailWorkFlowStatus.setApplicationName(applicationDetails.getApplicationName());
					emailWorkFlowStatus.setCreatedOn(new Date());
					emailWorkFlowStatus.setEmail(userLastLoginRequest.getOwnerEmail());
					emailWorkFlowStatus.setName(userLastLoginRequest.getOwnerName());
					emailWorkFlowStatus.setRange(userLastLoginRequest.getDateRange());
					emailWorkFlowStatus.setStatus(Constant.IN_PROGRESS);
					emailWorkFlowRepository.save(emailWorkFlowStatus);
				}
				workbook.close();
			} catch (UnsupportedEncodingException e) {
				throw new UnsupportedEncodingException(e.getMessage());
			} catch (MessagingException e) {
				workbook.close();
				throw new MessagingException("There are issues with the mail service. Please try again later.");
			}
		} else if (userLastLoginRequest.getDateRange().equalsIgnoreCase("no_activity")) {
			List<UserLastLoginDetails> details = userLastLoginDetailRepository
					.findApplicationId(userLastLoginRequest.getApplicationId());
			UserLastLoginDetails endDate = userLastLoginDetailRepository
					.getLastUserLoginDate(userLastLoginRequest.getApplicationId());
			for (UserLastLoginDetails userLastLoginDetails : details) {
				UserDetails userdepartment = userDetailsRepo.findByuserEmail(userLastLoginDetails.getUserEmail());
				if ((userdepartment != null && userdepartment.getDepartmentId() != null) && (applicationDetails
						.getApplicationId().equalsIgnoreCase(userLastLoginRequest.getApplicationId()))) {
					for (DepartmentDetails dep : applicationDetails.getDepartmentDetails()) {
						if (dep.getDepartmentId().equalsIgnoreCase(userdepartment.getDepartmentId().getDepartmentId())
								&& userLastLoginDetails.getLastLoginTime() == null) {
							list.add(userLastLoginDetails);
						}
					}
				}

			}
			if (list.isEmpty())
				throw new DataValidationException(userLastLoginRequest.getApplicationId() + Constant.NO_LAST_LOGIN,
						"403", HttpStatus.NOT_FOUND);
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet(Constant.USER_LOGIN);
			Map<String, Object[]> data = new TreeMap<>();
			data.put("1", new Object[] { Constant.USER_NAME, Constant.USER_EMAIL, Constant.USER_DESIGNATION,
					Constant.USER_LAST_LOGIN });
			Integer number = 2;
			for (UserLastLoginDetails userDetails : list) {
				UserDetails userDetails2 = userDetailsRepo.findByuserEmail(userDetails.getUserEmail());
				data.put(number.toString(), new Object[] { userDetails.getUserName(), userDetails.getUserEmail(),
						userDetails2.getUserDesigination(), userDetails.getLastLoginTime().toString() });
				number = number + 1;
			}
			// Iterate over data and write to sheet
			Set<String> keyset = data.keySet();
			int rownum = 0;
			for (String key : keyset) {
				Row row = sheet.createRow(rownum++);
				Object[] objArr = data.get(key);
				int cellnum = 0;
				for (Object obj : objArr) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);
				}
			}
			try {
				File tmplFile = new File(Constant.USER_LOGIN);
				FileOutputStream out = new FileOutputStream(tmplFile);
				workbook.write(out);
				out.close();
				if (sheet.getLastRowNum() != 0 && sheet.getRow(0) != null) {
					String toAddress = userLastLoginRequest.getOwnerEmail();
					MimeMessage message = mailSender.createMimeMessage();
					MimeMessageHelper helper = new MimeMessageHelper(message, true, CharEncoding.UTF_8);
					String subject = userLastLoginRequest.getSubject();
					Multipart multipart = new MimeMultipart();
					MimeBodyPart attachPart = new MimeBodyPart();
					String attachFile = Constant.USER_LOGIN;
					FileDataSource source = new FileDataSource(attachFile);
					attachPart.setDataHandler(new DataHandler(source));
					attachPart.setFileName(new File(attachFile).getName());
					multipart.addBodyPart(attachPart);
					Map<String, Object> model = new HashMap<>();
					Template t = config.getTemplate(Constant.WORK_FLOW);
					String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
					content = content.replace(Constant.APP_NAME, applicationDetails.getApplicationName());
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
					content = content.replace(Constant.SELECTED_RANGE, simpleDateFormat.format(new Date()) + " to "
							+ simpleDateFormat.format(endDate.getLastLoginTime()));
					content = content.replace(Constant.MESSAGE, userLastLoginRequest.getMessage());
					helper.setText(content, true);
					helper.addAttachment(attachFile, source);
					helper.setFrom(mailDomainName, senderName);
					helper.setTo(toAddress);
					helper.setSubject(subject);
					mailSender.send(message);
					if (tmplFile.delete()) {
						emailWorkFlowStatus.setCreatedOn(new Date());
					}
					emailWorkFlowStatus.setApplicationId(userLastLoginRequest.getApplicationId());
					emailWorkFlowStatus.setApplicationName(applicationDetails.getApplicationName());
					emailWorkFlowStatus.setCreatedOn(new Date());
					emailWorkFlowStatus.setEmail(userLastLoginRequest.getOwnerEmail());
					emailWorkFlowStatus.setName(userLastLoginRequest.getOwnerName());
					emailWorkFlowStatus.setRange(userLastLoginRequest.getDateRange());
					emailWorkFlowStatus.setStatus(Constant.IN_PROGRESS);
					emailWorkFlowRepository.save(emailWorkFlowStatus);
				}
				workbook.close();
			} catch (UnsupportedEncodingException e) {
				throw new UnsupportedEncodingException(e.getMessage());
			} catch (MessagingException e) {
				workbook.close();
				throw new MessagingException(e.getMessage());
			}

		} else {

			String[] splitedData = limit.split("-");
			LocalDate end = LocalDate.now().minusDays(Integer.valueOf(splitedData[0]));
			LocalDate start = LocalDate.now().minusDays(Integer.valueOf(splitedData[1]));
			ZoneId defaultZoneId = ZoneId.systemDefault();
			Date endDate = Date.from(end.atStartOfDay(defaultZoneId).toInstant());
			Date startDate = Date.from(start.atStartOfDay(defaultZoneId).toInstant());
			List<UserLastLoginDetails> details = userLastLoginDetailRepository.lastLoginUsersDataByDate(startDate,
					endDate, userLastLoginRequest.getApplicationId());
			for (UserLastLoginDetails userLastLoginDetails : details) {
				UserDetails userdepartment = userDetailsRepo.findByuserEmail(userLastLoginDetails.getUserEmail());
				if (userdepartment != null && userdepartment.getDepartmentId() != null) {
					ApplicationDetails depapplicationDetails = applicationDetailsRepo
							.findByApplicationId(userLastLoginRequest.getApplicationId());
					if (depapplicationDetails.getApplicationId()
							.equalsIgnoreCase(userLastLoginDetails.getApplicationId())) {
						for (DepartmentDetails dep : depapplicationDetails.getDepartmentDetails()) {
							if (dep.getDepartmentId()
									.equalsIgnoreCase(userdepartment.getDepartmentId().getDepartmentId())
									&& userLastLoginDetails.getLastLoginTime() != null) {
								list.add(userLastLoginDetails);
							}
						}
					}
				}
			}
			if (list.isEmpty())
				throw new DataValidationException(userLastLoginRequest.getApplicationId() + Constant.NO_LAST_LOGIN,
						"403", HttpStatus.NOT_FOUND);
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet(Constant.USER_LOGIN);
			Map<String, Object[]> data = new TreeMap<>();
			data.put("1", new Object[] { Constant.USER_NAME, Constant.USER_EMAIL, Constant.USER_DESIGNATION,
					Constant.USER_LAST_LOGIN });
			Integer number = 2;
			for (UserLastLoginDetails userDetails : list) {
				UserDetails userDetails2 = userDetailsRepo.findByuserEmail(userDetails.getUserEmail());
				data.put(number.toString(), new Object[] { userDetails.getUserName(), userDetails.getUserEmail(),
						userDetails2.getUserDesigination(), userDetails.getLastLoginTime().toString() });
				number = number + 1;
			}
			// Iterate over data and write to sheet
			Set<String> keyset = data.keySet();
			int rownum = 0;
			for (String key : keyset) {
				Row row = sheet.createRow(rownum++);
				Object[] objArr = data.get(key);
				int cellnum = 0;
				for (Object obj : objArr) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);
				}
			}
			try {
				File tmplFile = new File(Constant.USER_LOGIN);
				FileOutputStream out = new FileOutputStream(tmplFile);
				workbook.write(out);
				out.close();
				if (sheet.getLastRowNum() != 0 && sheet.getRow(0) != null) {
					String toAddress = userLastLoginRequest.getOwnerEmail();
					MimeMessage message = mailSender.createMimeMessage();
					MimeMessageHelper helper = new MimeMessageHelper(message, true, CharEncoding.UTF_8);
					String subject = userLastLoginRequest.getSubject();
					Multipart multipart = new MimeMultipart();
					MimeBodyPart attachPart = new MimeBodyPart();
					String attachFile = Constant.USER_LOGIN;
					FileDataSource source = new FileDataSource(attachFile);
					attachPart.setDataHandler(new DataHandler(source));
					attachPart.setFileName(new File(attachFile).getName());
					multipart.addBodyPart(attachPart);
					Map<String, Object> model = new HashMap<>();
					Template t = config.getTemplate(Constant.WORK_FLOW);
					String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
					content = content.replace(Constant.APP_NAME, applicationDetails.getApplicationName());
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.DATE_FORMAT);
					content = content.replace(Constant.SELECTED_RANGE,
							simpleDateFormat.format(startDate) + " to " + simpleDateFormat.format(endDate));
					content = content.replace(Constant.MESSAGE, userLastLoginRequest.getMessage());
					helper.setText(content, true);
					helper.addAttachment(attachFile, source);
					helper.setFrom(mailDomainName, senderName);
					helper.setTo(toAddress);
					helper.setSubject(subject);
					mailSender.send(message);
					if (tmplFile.delete()) {
						emailWorkFlowStatus.setCreatedOn(new Date());
					}
					emailWorkFlowStatus.setApplicationId(userLastLoginRequest.getApplicationId());
					emailWorkFlowStatus.setApplicationName(applicationDetails.getApplicationName());
					emailWorkFlowStatus.setCreatedOn(new Date());
					emailWorkFlowStatus.setEmail(userLastLoginRequest.getOwnerEmail());
					emailWorkFlowStatus.setName(userLastLoginRequest.getOwnerName());
					emailWorkFlowStatus.setRange(userLastLoginRequest.getDateRange());
					emailWorkFlowStatus.setStatus(Constant.IN_PROGRESS);
					emailWorkFlowRepository.save(emailWorkFlowStatus);
				}
				workbook.close();
			} catch (Exception e) {
				throw new DataValidationException(e.getMessage(), limit, null);
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("userLastLoginResponse", ""),
				"Email triggered successfully");
	}

	@Override
	public CommonResponse lastLoginUsers(String dateRange, String applicationId) throws DataValidationException {
		ApplicationDetails applicationDetailsObj = applicationDetailsRepo.findByApplicationId(applicationId);
		if (applicationDetailsObj == null) {
			throw new DataValidationException("applcaition details not found : " + applicationId, "403",
					HttpStatus.NOT_FOUND);
		}
		if (userLastLoginDetailRepository.findApplicationId(applicationId).isEmpty()) {
			throw new DataValidationException(
					"does'nt have any last login user data for the given application : " + applicationId, "404",
					HttpStatus.NOT_FOUND);
		}
		List<UserLastLoginResponse> lastLoginResponses = new ArrayList<>();
		List<UserLastLoginDetails> list = new ArrayList<>();
		if (dateRange.equalsIgnoreCase("91")) {
			LocalDate end = LocalDate.now().minusDays(91);
			UserLastLoginDetails details = userLastLoginDetailRepository.getLastUserLoginDate(applicationId);
			if (details == null) {
				throw new DataValidationException(
						"No last login user data for the given application : " + applicationId, "204",
						HttpStatus.NO_CONTENT);
			}
			LocalDate d1 = LocalDate.parse(end.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
			LocalDate d2 = LocalDate.parse(
					new SimpleDateFormat(Constant.DATE_FORMAT).format(details.getLastLoginTime()),
					DateTimeFormatter.ISO_LOCAL_DATE);
			Duration diff = Duration.between(d1.atStartOfDay(), d2.atStartOfDay());
			LocalDate start = LocalDate.now().minusDays(diff.toDays());
			ZoneId defaultZoneId = ZoneId.systemDefault();
			Date endDate = Date.from(end.atStartOfDay(defaultZoneId).toInstant());
			Date.from(start.atStartOfDay(defaultZoneId).toInstant());

			List<UserLastLoginDetails> lastLoginDetailsList = userLastLoginDetailRepository
					.lastLoginUsersDataafter90days(endDate, applicationId);
			if (applicationDetailsRepo.findByApplicationId(applicationId) == null) {
				throw new DataValidationException("No Application Found With this Id :" + applicationId, null, null);
			}
			for (UserLastLoginDetails userLastLoginDetails : lastLoginDetailsList) {
				UserDetails userdepartment = userDetailsRepo.findByuserEmail(userLastLoginDetails.getUserEmail());
				if (userdepartment != null && userdepartment.getDepartmentId() != null) {
					ApplicationDetails applicationDetails = applicationDetailsRepo.findByApplicationId(applicationId);
					if (applicationDetails.getApplicationId()
							.equalsIgnoreCase(userLastLoginDetails.getApplicationId())) {
						for (DepartmentDetails dep : applicationDetails.getDepartmentDetails()) {
							if (dep.getDepartmentId()
									.equalsIgnoreCase(userdepartment.getDepartmentId().getDepartmentId())
									&& userLastLoginDetails.getLastLoginTime() != null) {
								list.add(userLastLoginDetails);
							}
						}
					}
				}
			}
			if (list.isEmpty())
				throw new DataValidationException(applicationId + Constant.NO_LAST_LOGIN, null, null);
			for (UserLastLoginDetails lastLoginDetails : list) {
				UserDetails userDetails = userDetailsRepo.findByuserEmail(lastLoginDetails.getUserEmail());
				UserLastLoginResponse userLastLoginResponse = new UserLastLoginResponse();
				userLastLoginResponse.setUserName(lastLoginDetails.getUserName());
				userLastLoginResponse.setUserEmail(lastLoginDetails.getUserEmail());
				userLastLoginResponse.setUserLastLogin(lastLoginDetails.getLastLoginTime());
				userLastLoginResponse.setUserLogo(userDetails.getLogoUrl());
				if (userDetails.getUserDesigination() != null) {
					userLastLoginResponse.setUserDesignation(userDetails.getUserDesigination());
				} else {
					userLastLoginResponse.setUserDesignation("");
				}
				lastLoginResponses.add(userLastLoginResponse);
			}
		} else if (dateRange.equalsIgnoreCase("no_activity")) {
			List<UserLastLoginDetails> details = userLastLoginDetailRepository.findApplicationId(applicationId);
			if (applicationDetailsRepo.findByApplicationId(applicationId) == null) {
				throw new DataValidationException("No Application Found With this Id :" + applicationId, null, null);
			}
			for (UserLastLoginDetails userLastLoginDetails : details) {
				UserDetails userdepartment = userDetailsRepo.findByuserEmail(userLastLoginDetails.getUserEmail());
				if (userdepartment != null && userdepartment.getDepartmentId() != null) {
					ApplicationDetails applicationDetails = applicationDetailsRepo.findByApplicationId(applicationId);
					if (applicationDetails.getApplicationId()
							.equalsIgnoreCase(userLastLoginDetails.getApplicationId())) {
						for (DepartmentDetails dep : applicationDetails.getDepartmentDetails()) {
							if (dep.getDepartmentId()
									.equalsIgnoreCase(userdepartment.getDepartmentId().getDepartmentId())
									&& userLastLoginDetails.getLastLoginTime() != null) {
								list.add(userLastLoginDetails);
							}
						}
					}
				}
			}
			if (list.isEmpty())
				throw new DataValidationException(applicationId + Constant.NO_LAST_LOGIN, null, null);
			for (UserLastLoginDetails lastLoginDetails : list) {
				UserDetails userDetails = userDetailsRepo.findByuserEmail(lastLoginDetails.getUserEmail());
				UserLastLoginResponse userLastLoginResponse = new UserLastLoginResponse();
				userLastLoginResponse.setUserName(lastLoginDetails.getUserName());
				userLastLoginResponse.setUserEmail(lastLoginDetails.getUserEmail());
				userLastLoginResponse.setUserLastLogin(lastLoginDetails.getLastLoginTime());
				userLastLoginResponse.setUserLogo(userDetails.getLogoUrl());
				if (userDetails.getUserDesigination() != null) {
					userLastLoginResponse.setUserDesignation(userDetails.getUserDesigination());
				} else {
					userLastLoginResponse.setUserDesignation("");
				}
				lastLoginResponses.add(userLastLoginResponse);
			}
		} else {
			String limit = dateRange;
			String[] splitedData = limit.split("-");
			LocalDate end = LocalDate.now().minusDays(Integer.valueOf(splitedData[0]));
			LocalDate start = LocalDate.now().minusDays(Integer.valueOf(splitedData[1]));
			ZoneId defaultZoneId = ZoneId.systemDefault();
			Date endDate = Date.from(end.atStartOfDay(defaultZoneId).toInstant());
			Date startDate = Date.from(start.atStartOfDay(defaultZoneId).toInstant());
			List<UserLastLoginDetails> details = userLastLoginDetailRepository.lastLoginUsersDataByDate(startDate,
					endDate, applicationId);
			if (applicationDetailsRepo.findByApplicationId(applicationId) == null) {
				throw new DataValidationException("Given application is deleted : " + applicationId, null, null);
			}
			for (UserLastLoginDetails userLastLoginDetails : details) {
				UserDetails userdepartment = userDetailsRepo.findByuserEmail(userLastLoginDetails.getUserEmail());
				if (userdepartment != null && userdepartment.getDepartmentId() != null) {
					ApplicationDetails applicationDetails = applicationDetailsRepo.findByApplicationId(applicationId);
					if (applicationDetails.getApplicationId()
							.equalsIgnoreCase(userLastLoginDetails.getApplicationId())) {
						for (DepartmentDetails dep : applicationDetails.getDepartmentDetails()) {
							if (dep.getDepartmentId()
									.equalsIgnoreCase(userdepartment.getDepartmentId().getDepartmentId())) {
								list.add(userLastLoginDetails);
							}
						}

					}
				}
			}
			if (list.isEmpty())
				throw new DataValidationException(applicationId + Constant.NO_LAST_LOGIN, null, null);
			for (UserLastLoginDetails lastLoginDetails : list) {
				UserDetails userDetails = userDetailsRepo.findByuserEmail(lastLoginDetails.getUserEmail());
				UserLastLoginResponse userLastLoginResponse = new UserLastLoginResponse();
				userLastLoginResponse.setUserName(lastLoginDetails.getUserName());
				userLastLoginResponse.setUserEmail(lastLoginDetails.getUserEmail());
				userLastLoginResponse.setUserLogo(userDetails.getLogoUrl());
				userLastLoginResponse.setUserLastLogin(lastLoginDetails.getLastLoginTime());
				if (userDetails.getUserDesigination() != null) {
					userLastLoginResponse.setUserDesignation(userDetails.getUserDesigination());
				} else {
					userLastLoginResponse.setUserDesignation("");
				}
				lastLoginResponses.add(userLastLoginResponse);
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("UserLastLoginResposne", lastLoginResponses),
				"Data Retrieved Successfullty");
	}

	private void sendDefaultPasswordEmail(String userEmail, String userName, String verificationCode)
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
		content = content.replace("{{name}}", userName);
		content = content.replace("{{code}}", verificationCode);
		try {
			helper.setFrom(mailDomainName, senderName);
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setText(content, true);
		} catch (MessagingException e) {
			throw new MessagingException(e.getMessage());
		}
		mailSender.send(message);
	}

	@Override
	public CommonResponse workflowStatus(String category) {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		List<EmailWorkFlowStatus> list = emailWorkFlowRepository.findAll();
		List<EmailWorkFlowResponse> workFlowResponses = new ArrayList<>();
		if (category.equalsIgnoreCase(Constant.IN_PROGRESS)) {
			List<EmailWorkFlowStatus> inProgrressList = list.stream()
					.filter(p -> p.getStatus().equalsIgnoreCase(Constant.IN_PROGRESS)).collect(Collectors.toList());
			for (EmailWorkFlowStatus status : inProgrressList) {
				EmailWorkFlowResponse emailWorkFlowResponse = new EmailWorkFlowResponse();
				emailWorkFlowResponse.setAccountName(status.getApplicationName());
				emailWorkFlowResponse.setCreatedOn(status.getCreatedOn());
				emailWorkFlowResponse.setLastDayToRespond(null);
				emailWorkFlowResponse.setName(status.getName());
				emailWorkFlowResponse.setStatus(status.getStatus());
				emailWorkFlowResponse.setWorkFlowNumber(status.getWorkFlowNumber());
				workFlowResponses.add(emailWorkFlowResponse);
			}
			response.setData(workFlowResponses);
		} else if (category.equalsIgnoreCase(Constant.COMPLETED)) {
			List<EmailWorkFlowStatus> completedList = list.stream()
					.filter(p -> p.getStatus().equalsIgnoreCase(Constant.COMPLETED)).collect(Collectors.toList());
			for (EmailWorkFlowStatus status : completedList) {
				EmailWorkFlowResponse emailWorkFlowResponse = new EmailWorkFlowResponse();
				emailWorkFlowResponse.setAccountName(status.getApplicationName());
				emailWorkFlowResponse.setCreatedOn(status.getCreatedOn());
				emailWorkFlowResponse.setLastDayToRespond(null);
				emailWorkFlowResponse.setName(status.getName());
				emailWorkFlowResponse.setWorkFlowNumber(status.getWorkFlowNumber());
				emailWorkFlowResponse.setStatus(status.getStatus());
				workFlowResponses.add(emailWorkFlowResponse);
			}
			response.setData(workFlowResponses);
		}
		response.setAction("WorkFlowStatusResponse");
		commonResponse.setMessage("Data Retrieved Successfullty");
		commonResponse.setStatus(HttpStatus.OK);
		commonResponse.setResponse(response);
		return commonResponse;
	}

	@Override
	public CommonResponse workflowStatusUpdate(Long workFlowNumber) throws DataValidationException {
		EmailWorkFlowStatus listFlowStatus = emailWorkFlowRepository.findByWorkFlowNumber(workFlowNumber);
		listFlowStatus.setUpdatedOn(new Date());
		listFlowStatus.setStatus(Constant.COMPLETED);
		emailWorkFlowRepository.save(listFlowStatus);
		return new CommonResponse(HttpStatus.OK, new Response("WorkFlowStatusUpdateResponse", new ArrayList<>()),
				"User workflow status updated Successfullty");
	}

	@Override
	public CommonResponse userSpendAnalytics(String userId) throws DataValidationException {
		UserDetails userDetail = userDetailsRepo.findByUserId(userId);
		List<userDetailsSpendAnalystics> list = new ArrayList<>();
		if (userDetail != null) {
			if (!userDetail.getApplicationId().isEmpty()) {
				for (ApplicationDetails application : userDetail.getApplicationId()) {
					System.out.println(application.getApplicationId());

					List<ApplicationLicenseDetails> costPerUser = userDetail.getLicenseId().stream().filter(p -> p
							.getApplicationId().getApplicationId().equalsIgnoreCase(application.getApplicationId()))
							.collect(Collectors.toList());
					if (!costPerUser.isEmpty()) {
						userDetailsSpendAnalystics analystics = new userDetailsSpendAnalystics();
						analystics.setApplicationId(application.getApplicationId());
						analystics.setApplicationName(application.getApplicationName());
						analystics.setApplicationTotalCost(BigDecimal.valueOf(application.getLicenseDetails().stream()
								.mapToInt(p -> p.getTotalCost().intValue()).sum()));
						analystics.setAdminCost(BigDecimal.valueOf(application.getLicenseDetails().stream()
								.mapToInt(p -> p.getConvertedCost().intValue()).sum()));
						analystics.setPerLicenseCost(BigDecimal
								.valueOf(costPerUser.stream().mapToInt(p -> p.getUnitPrice().intValue()).sum()));
						BigDecimal divide = BigDecimal
								.valueOf(application.getLicenseDetails().stream()
										.mapToInt(p -> p.getTotalCost().intValue()).sum())
								.divide(BigDecimal
										.valueOf(costPerUser.stream().mapToInt(p -> p.getUnitPrice().intValue()).sum()),
										2, RoundingMode.FLOOR);

						analystics.setCurrency(application.getContractDetails().get(0).getContractCurrency());
						analystics.setAdminPerLicenseCost(BigDecimal
								.valueOf(application.getLicenseDetails().stream()
										.mapToInt(p -> p.getConvertedCost().intValue()).sum())
								.divide(divide, 2, RoundingMode.FLOOR));
						list.add(analystics);
					}
				}
			}
		} else {
			throw new DataValidationException("User Not Found for the Given Id " + userId, null, null);
		}
		list.sort(Comparator.comparing(userDetailsSpendAnalystics::getApplicationTotalCost).reversed());
		return new CommonResponse(HttpStatus.OK, new Response("UserAnalysticsResponse", list),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getDepartmentUsers(String depId) throws DataValidationException {
		DepartmentDetails department = departmentRepository.findByDepartmentId(depId);
		List<String> listofusers = new ArrayList<>();
		if (department != null) {
			if (!department.getUserDetails().isEmpty()) {
				List<UserDetails> usersinsingledepartment = userDetailsRepo.getAllUsersByDepartmentId(depId);
				for (UserDetails userdetails : usersinsingledepartment) {
					listofusers.add(userdetails.getUserEmail());
				}
			}
		} else {
			throw new DataValidationException("Department Not Found for Given Id " + depId, "200", HttpStatus.OK);
		}
		return new CommonResponse(HttpStatus.OK, new Response("ListofUsersInDepartmentResponse", listofusers),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private void deleteZohoCRMUsers(String applicationId, String userEmail) throws JsonProcessingException {

		ObjectMapper objectMapper = new ObjectMapper();
		CommonResponse userResponse = zohoCRMservice.getUserId(userEmail, "ActiveUsers", applicationId);
		Details userId = objectMapper.readValue(objectMapper.writeValueAsString(userResponse.getResponse().getData()),
				Details.class);
		zohoCRMservice.deleteUserInCRM(applicationId, userId.getId());

	}

	private void deleteQuickBooksUser(String appId, String userEmail) {
		quickbooksService.deleteUser(appId, userEmail);
	}

	private void deleteUsersFromHubSpot(String applicationId, String userEmail) {
		hubSpotService.deleteUser(applicationId, userEmail);
	}

	private void deleteUsersFromDatadog(String applicationId, String userEmail) {
		datadogWrapperService.deleteUser(applicationId, userEmail);
	}

	private void deleteUsersFromConfluence(String applicationId, String userEmail) {
		AtlassianJiraUsers atlassianJiraUsers = atlassianJiraUsersRepository.findByUserEmail(userEmail);
		if (atlassianJiraUsers != null) {
			confluenceWrapperService.deleteUser(atlassianJiraUsers.getAccountId(), applicationId);
		}
	}

	private void deleteGitlabUser(String applicationId, String userEmail, UserRemovalRequest userRemovalRequest) {

		if (userRemovalRequest.isHasCustomFields()) {
			for (UserAdaptorApplicationFields adaptorApplication : userRemovalRequest.getCustomFields()) {
				if (adaptorApplication.getApplicationName().equalsIgnoreCase(Constant.GIT_LAB)) {
					JSONObject jsonObject = new JSONObject(adaptorApplication.getFields());
					if (jsonObject.get("userId").toString() != null) {
						Integer userId = Integer.valueOf(jsonObject.get("userId").toString());
						GitlabDeleteUserRequest deleteUserRequest = new GitlabDeleteUserRequest(userId);
						gitlabService.removeGitlabMember(deleteUserRequest, applicationId);
					}
				}
			}
		}
		gitlabService.revokeInvitation(applicationId, userEmail);

	}

	private void deleteGitHubUser(String applicationId, UserRemovalRequest userRemovalRequest) {

		if (userRemovalRequest.isHasCustomFields()) {
			for (UserAdaptorApplicationFields adaptorApplication : userRemovalRequest.getCustomFields()) {
				if (adaptorApplication.getApplicationName().equalsIgnoreCase("GitHub")) {
					JSONObject jsonObject = new JSONObject(adaptorApplication.getFields());
					if (jsonObject.get("userName").toString() != null) {
						String userName = jsonObject.get("userName").toString();
						RemoveUserRequest userDeleteRequest = new RemoveUserRequest(userName);
						githubService.removeOrganizationMember(applicationId, userDeleteRequest);
					}
				}
			}
		}
	}

	private void revokeZohoPeopleLicense(String applicationId, String userEmail) {
		zohoPeopleService.revokeAccess(userEmail, applicationId);
	}

	private void unAssignMicrosoft365License(String applicationId,String userEmail,String productName) {
		microsoft365WrapperService.unAssignLicense(applicationId, userEmail,productName);
	}


	private void deleteUsersFromJira(String applicationId, String userEmail) {
		AtlassianJiraUsers atlassianJiraUsers = atlassianJiraUsersRepository.findByUserEmail(userEmail);
		if (atlassianJiraUsers != null) {
			jiraWrapperService.removeUserFromGroup(atlassianJiraUsers.getAccountId(), applicationId);
		}
	}
	
	private void deleteUsersFromZoom(String applicationId, String userEmail) {
		zoomWrapperService.deleteUser(applicationId, userEmail);
	}

}
