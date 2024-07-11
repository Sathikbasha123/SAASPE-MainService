package saaspe.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;
import saaspe.adaptor.model.AddGitlabMemberRequest;
import saaspe.adaptor.model.CommonCRMModel;
import saaspe.adaptor.model.CommonZohoCRMRequest;
import saaspe.adaptor.model.ConfluenceCreateUser;
import saaspe.adaptor.model.CreateHubSpotUserRequest;
import saaspe.adaptor.model.CreateZoomUserRequest;
import saaspe.adaptor.model.GitHubInviteRequestBody;
import saaspe.adaptor.model.JiraCreateUserRequest;
import saaspe.adaptor.model.QuickBooksUserRequest;
import saaspe.adaptor.model.QuickBooksUserRequest.Email;
import saaspe.adaptor.model.QuickBooksUserRequest.PhoneNumber;
import saaspe.adaptor.model.ZohoCRMLicenseResponse;
import saaspe.adaptor.model.ZohoPeopleInviteRequest;
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
import saaspe.dto.UserLicenseAssignDto;
import saaspe.entity.AdaptorDetails;
import saaspe.entity.ApplicationContractDetails;
import saaspe.entity.ApplicationDetails;
import saaspe.entity.ApplicationLicenseDetails;
import saaspe.entity.Applications;
import saaspe.entity.DepartmentDetails;
import saaspe.entity.UserDetails;
import saaspe.entity.UserOnboarding;
import saaspe.entity.Users;
import saaspe.exception.DataValidationException;
import saaspe.model.ApplicationLicenseCountResponse;
import saaspe.model.ApplicationsLicenseCountRequest;
import saaspe.model.AssignLicenseResponse;
import saaspe.model.CommonResponse;
import saaspe.model.LicenseUsersDetailsResponse;
import saaspe.model.Response;
import saaspe.repository.AdaptorCredentialRepository;
import saaspe.repository.AdaptorDetailsRepsitory;
import saaspe.repository.ApplicationContractDetailsRepository;
import saaspe.repository.ApplicationDetailsRepository;
import saaspe.repository.ApplicationLicenseDetailsRepository;
import saaspe.repository.ApplicationsRepository;
import saaspe.repository.DepartmentRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.repository.UserOnboardingDetailsRepository;
import saaspe.repository.UsersRepository;
import saaspe.service.LicenseService;

@Service
public class LicenseServiceImpl implements LicenseService {

	@Autowired
	private ApplicationLicenseDetailsRepository applicationLicenseDetailsRepository;

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	ApplicationDetailsRepository applicationDetailsRepository;

	@Autowired
	ApplicationContractDetailsRepository applicationContractDetailsRepository;

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	JiraWrapperService jiraWrapperService;

	@Autowired
	HubSpotWrapperService hubSpotService;

	@Autowired
	DatadogWrapperService datadogWrapperService;

	@Autowired
	ZohoCRMWrapperService zohoCRMservice;

	@Autowired
	GithubWrapperService githubService;

	@Autowired
	GitlabWrapperService gitlabService;

	@Autowired
	ConfluenceWrapperService confluenceWrapperService;

	@Autowired
	QuickBookWrapperService quickbooksService;

	@Autowired
	Microsoft365WrapperService microsoft365WrapperService;

	@Autowired
	AdaptorDetailsRepsitory adaptorDetailsRepository;

	@Autowired
	UserOnboardingDetailsRepository userOnboardingDetailsRepository;

	@Autowired
	ZohoPeopleService zohoPeopleService;

	@Autowired
	ZohoAnalyticsService zohoAnalyticsService;

	@Autowired
	SalesforceService salesforceService;

	@Autowired
	FreshdeskWrapperService freshdeskService;
	
	@Autowired
	ZoomWrapperService zoomWrapperService;

	@Autowired
	private AdaptorCredentialRepository adaptorCredentialRepository;
	
	@Autowired
	private ApplicationsRepository applicationRepository;
	
	@Autowired 
	private UsersRepository usersRepository;

	@Override
	public CommonResponse getUsersDetailsByLicebseId(String licenseId) throws DataValidationException {
		List<LicenseUsersDetailsResponse> list = new ArrayList<>();
		ApplicationLicenseDetails applicationLicenseDetails = applicationLicenseDetailsRepository
				.getUsersDetailsByLicenseId(licenseId);
		if (applicationLicenseDetails == null) {
			throw new DataValidationException("License id not found: " + licenseId, null, null);
		}
		if (applicationLicenseDetails.getUserId().isEmpty()) {
			throw new DataValidationException("License is not mapped to users: " + licenseId, null, null);
		}
		for (UserDetails details : applicationLicenseDetails.getUserId()) {
			if (details.getEndDate() == null) {
				LicenseUsersDetailsResponse licenseUsersDetailsResponse = new LicenseUsersDetailsResponse();
				licenseUsersDetailsResponse.setUserId(details.getUserId());
				licenseUsersDetailsResponse.setUserAvatar(details.getLogoUrl());
				licenseUsersDetailsResponse.setUserName(details.getUserName());
				licenseUsersDetailsResponse.setUserEmail(details.getUserEmail());
				list.add(licenseUsersDetailsResponse);
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("licenseUsersResponse", list),
				"Data retrieved successfully");
	}

	@Override
	@Transactional
	public CommonResponse linkUserLicense(UserLicenseAssignDto licenseAssignDto)
			throws DataValidationException, JsonProcessingException {
		CommonResponse resp = new CommonResponse();
		ApplicationLicenseDetails licenseDetials = applicationLicenseDetailsRepository
				.getUsersDetailsByLicenseId(licenseAssignDto.getLicenseId());
		if (licenseAssignDto.getUserId().size() != licenseAssignDto.getUserId().stream().distinct().count()) {
			throw new DataValidationException("User Id Should not repeat ", null, HttpStatus.BAD_REQUEST);
		}

		if (licenseDetials != null) {
			ApplicationDetails application = licenseDetials.getApplicationId();

			boolean isAdaptorConnected = true;
			
			if (adaptorCredentialRepository.existsByApplicationName(application.getApplicationName())) {
				if (adaptorDetailsRepository.existsByApplicationId(application.getApplicationId())) {
					AdaptorDetails adaptorDetails = adaptorDetailsRepository
							.findByApplicationId(application.getApplicationId());
					if (Constant.OAUTH_APPS.contains(application.getApplicationName().toUpperCase())
							&& adaptorDetails.getApiToken() == null) {
						isAdaptorConnected = false;
					}
				} else {
					isAdaptorConnected = false;
				}
			}
			AssignLicenseResponse licenseResp = new AssignLicenseResponse();
			if (licenseDetials.getLicenseUnMapped() != 0) {
				List<UserDetails> userDetails = licenseDetials.getUserId();
				List<UserDetails> users = new ArrayList<>();
				List<String> errors = new ArrayList<>();
				List<String> success = new ArrayList<>();
				for (String userId : licenseAssignDto.getUserId()) {
					UserDetails user = userDetailsRepository.findByUserId(userId);
                    Users userss = usersRepository.findByUserId(userId);
					CommonResponse adaptorResponse = null;
					if(isAdaptorConnected) {
					if (application.getApplicationName().equalsIgnoreCase("Hubspot")) {
						adaptorResponse = createUserHubSpot(application.getApplicationId(), user.getUserEmail());
					}

					if (application.getApplicationName().equalsIgnoreCase("Zohocrm")) {
						ObjectMapper mapper = new ObjectMapper();
						CommonResponse zohoLicenseResp = zohoCRMservice
								.getLicenseDetails(application.getApplicationId());
						ZohoCRMLicenseResponse licenseResponse = mapper.readValue(
								mapper.writeValueAsString(zohoLicenseResp.getResponse().getData()),
								ZohoCRMLicenseResponse.class);
						if (licenseResponse.getAvailableCount() > 0) {
							adaptorResponse = createZohoCRMUser(application.getApplicationId(), user.getUserEmail());
						} else {
							adaptorResponse = new CommonResponse(HttpStatus.BAD_REQUEST,
									new Response("ZohoCRMLicenseRequest", null),
									"Not enough license in Zohocrm application");
							resp.setMessage(adaptorResponse.getMessage());
						}
					}
					if (application.getApplicationName().equalsIgnoreCase("Confluence")) {
						adaptorResponse = createConfluenceUser(application.getApplicationId(), user.getUserEmail());
					}
					if (application.getApplicationName().equalsIgnoreCase("Datadog")) {
						adaptorResponse = createUserDatadog(application.getApplicationId(), user.getUserEmail());
					}
					if (application.getApplicationName().equalsIgnoreCase("Gitlab")) {
						adaptorResponse = createGitlabUser(application.getApplicationId(), user.getUserEmail());
					}
					if (application.getApplicationName().equalsIgnoreCase("Quickbooks")) {
						adaptorResponse = createQuickBooksUser(application.getApplicationId(), user.getUserEmail());
					}
					if (application.getApplicationName().equalsIgnoreCase("Github")) {
						adaptorResponse = createGithubUser(application.getApplicationId(), user.getUserEmail());
					}
					if (application.getApplicationName().equalsIgnoreCase("Zoho People")) {
						adaptorResponse = createZohoPeopleUser(application.getApplicationId(), user.getUserEmail());
					}
					if (application.getApplicationName().equalsIgnoreCase(Constant.ZOHOANALYTICS)) {
						adaptorResponse = createZohoAnalyticsUser(application.getApplicationId(), user.getUserEmail());
					}
					if (application.getApplicationName().equalsIgnoreCase("Salesforce")) {
						adaptorResponse = createSalesforceUser(application.getApplicationId(), user.getUserEmail(),
								user.getUserId(), user.getUserName());
					}
					if (application.getApplicationName().equalsIgnoreCase("Microsoft365")) {
	                    adaptorResponse = assignMicrosoft365License(application.getApplicationId(), user.getUserEmail(),licenseDetials.getProductName());
	                }
					if (application.getApplicationName().equalsIgnoreCase("JIRA")) {
						adaptorResponse = createUserJira(application.getApplicationId(), user.getUserEmail());
					}
					if (application.getApplicationName().equalsIgnoreCase("Freshdesk")) {
						adaptorResponse = createFreshdeskUser(application.getApplicationId(), user.getUserEmail(),
								user.getUserName());
					}
					if (application.getApplicationName().equalsIgnoreCase("Zoom")) {
						adaptorResponse = createUserZoom(application.getApplicationId(), user.getUserEmail());
					}
					}

					if ((adaptorResponse == null || adaptorResponse.getStatus().is2xxSuccessful())
							&& !users.contains(user)) {
						userDetails.add(user);
						users.add(user);
						userss.setUserLicenseId(licenseAssignDto.getLicenseId());
						userss.setUserApplicationId(application.getApplicationId().toString());
						userss.setAppUser(true);
	                    usersRepository.save(userss);
						success.add(user.getUserEmail());
					} else {
						errors.add(user.getUserEmail());
					}
				}
				if (errors.size() < licenseAssignDto.getUserId().size()) {
					Integer mapped = licenseDetials.getLicenseMapped();
					Integer unmapped = licenseDetials.getLicenseUnMapped();
					licenseDetials.setUserId(userDetails);
					licenseDetials.setLicenseMapped(mapped + users.size());
					licenseDetials.setLicenseUnMapped(unmapped - users.size());
					applicationLicenseDetailsRepository.save(licenseDetials);
					Applications license = applicationRepository.findByLicenseId(licenseAssignDto.getLicenseId());
					license.setLicenseMapped(mapped + users.size());
					license.setLicenseUnmapped(unmapped - users.size());
					applicationRepository.save(license);
					for (UserDetails details : users) {
						details.getApplicationId().add(application);
						 Users userEntity = usersRepository.findByUserId(details.getUserId());
						    if (userEntity != null) {
						        userEntity.setUserLicenseId(licenseAssignDto.getLicenseId());
						        userEntity.setUserApplicationId(application.getApplicationId().toString());
						        userEntity.setAppUser(true);
						        usersRepository.save(userEntity);
						    }
						userDetailsRepository.save(details);
					}
					
					licenseResp.setAssigned(success);
					licenseResp.setUnassigned(errors);
					return new CommonResponse(HttpStatus.OK, new Response("assignLicenseRequest", licenseResp),
							"Licenses assigned successfully");
				} else
					if(resp.getMessage()!=null) {
						return new CommonResponse(HttpStatus.BAD_REQUEST, new Response("assignLicenseRequest", null),
								resp.getMessage());
					}
					return new CommonResponse(HttpStatus.BAD_REQUEST, new Response("assignLicenseRequest", null),
							"License assign Failed for all the users");
			} else {
				throw new DataValidationException("Not Enough License to Assign", null, HttpStatus.BAD_REQUEST);
			}
		} else {
			throw new DataValidationException("License Id " + licenseAssignDto.getLicenseId() + " Not Valid", null,
					HttpStatus.BAD_REQUEST);
		}

	}

	@Override
	public CommonResponse getAppplicationsLicenseCount(ApplicationsLicenseCountRequest applicationLicenseDetails)
			throws DataValidationException {
		ApplicationLicenseCountResponse applicationLicenseCountResponse = new ApplicationLicenseCountResponse();
		Integer licenseCount = 0;
		DepartmentDetails departmentDetails = departmentRepository
				.findByDepartmentId(applicationLicenseDetails.getDepartmentId());
		if (departmentDetails == null) {
			throw new DataValidationException("Selected department is not found, please select other department ", null,
					null);
		}
		List<ApplicationDetails> filteredApplication = departmentDetails.getApplicationId().stream()
				.filter(p -> p.getApplicationName().trim().equals(applicationLicenseDetails.getApplicationName().trim())
						&& p.getActiveContracts() != null)
				.collect(Collectors.toList());
		if (filteredApplication.isEmpty()) {
			throw new DataValidationException("Department with " + departmentDetails.getDepartmentName()
					+ " Doesn't have Any Application Like " + applicationLicenseDetails.getApplicationName(), null,
					null);
		}
		for (ApplicationDetails application : filteredApplication) {
			List<ApplicationContractDetails> applicationContractDetails = applicationContractDetailsRepository
					.findActiveContractsByAppId(application.getApplicationId());
			if (!applicationContractDetails.stream().filter(s -> s.getContractStatus().equalsIgnoreCase("Active"))
					.collect(Collectors.toList()).isEmpty()) {
				for (ApplicationContractDetails contractDetails : applicationContractDetails) {
					List<ApplicationLicenseDetails> licenseDetails = applicationLicenseDetailsRepository
							.getdByContractId(contractDetails.getContractId());
					int applicationLicenseCount = licenseDetails.stream().mapToInt(i -> i.getLicenseUnMapped()).sum();
					licenseCount = licenseCount + applicationLicenseCount;
				}
			} else
				throw new DataValidationException("No contract is active in this application", null, null);
		}
		applicationLicenseCountResponse.setApplicationName(applicationLicenseDetails.getApplicationName());
		applicationLicenseCountResponse.setLicenseCount(licenseCount);
		return new CommonResponse(HttpStatus.OK,
				new Response("ApplicationLicenseCountResponse", applicationLicenseCountResponse),
				"Data retrieved successfully");
	}

	private CommonResponse createGithubUser(String applicationId, String userEmail) {
		CommonResponse resp = new CommonResponse();
		int count = 0;
		while (count < 3) {
			GitHubInviteRequestBody gitHubInviteRequestBody = new GitHubInviteRequestBody();
			gitHubInviteRequestBody.setEmail(userEmail);
			gitHubInviteRequestBody.setRole("direct_member");
			resp = githubService.inviteUser(gitHubInviteRequestBody, applicationId);
			if (resp.getStatus().is2xxSuccessful()) {
				break;
			} else {
				count++;
			}

		}
		if (count == 3) {
			resp.setStatus(HttpStatus.BAD_REQUEST);
			resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
			resp.setResponse(new Response("GitHubLicenseAssignRequest", null));
		}
		return resp;
	}

	private CommonResponse createZohoCRMUser(@NonNull String applicationId, @NonNull String userEmail) {
		CommonResponse resp = new CommonResponse();
		int count = 0;
		while (count < 3) {
			CommonZohoCRMRequest addrequest = new CommonZohoCRMRequest();
			List<CommonCRMModel> cRMbodyList = new ArrayList<>();
			UserDetails userDetails = userDetailsRepository.getByEmail(userEmail);
			CommonCRMModel userbody = new CommonCRMModel(userDetails.getUserName(), userEmail.trim(),
					"621068000000031154", "621068000000031160");
			cRMbodyList.add(userbody);
			addrequest.setUsers(cRMbodyList);
			resp = zohoCRMservice.addUserToCRM(applicationId, addrequest);
			if (resp.getStatus().is2xxSuccessful()) {
				break;
			} else {
				count++;
			}
		}
		if (count == 3) {
			resp.setStatus(HttpStatus.BAD_REQUEST);
			resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
			resp.setResponse(new Response("ZohoCRMLicenseAssignRequest", null));
		}
		return resp;
	}

	private CommonResponse createGitlabUser(String applicationId, String userEmail) {
		CommonResponse resp = new CommonResponse();
		int count = 0;
		while (count < 3) {
			AddGitlabMemberRequest gitlabMember = new AddGitlabMemberRequest(userEmail, 30);
			resp = gitlabService.addGitlabMember(gitlabMember, applicationId);
			if (resp.getStatus().is2xxSuccessful()) {
				break;
			} else {
				count++;
			}
		}
		if (count == 3) {
			resp.setStatus(HttpStatus.BAD_REQUEST);
			resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
			resp.setResponse(new Response("GitLabLicenseAssignRequest", null));
		}
		return resp;
	}

	private CommonResponse createQuickBooksUser(String applicationId, String userEmail) {
		CommonResponse resp = new CommonResponse();
		int count = 0;
		while (count < 3) {
			QuickBooksUserRequest quickbooksUser = new QuickBooksUserRequest();
			UserDetails userDetails = userDetailsRepository.getByEmail(userEmail);
			quickbooksUser.setGivenName(userDetails.getUserName());
			quickbooksUser.setFamilyName("ㅤ");
			Email email = quickbooksUser.new Email(userDetails.getUserEmail());
			quickbooksUser.setPrimaryEmailAddr(email);
			PhoneNumber phoneNumber = quickbooksUser.new PhoneNumber(userDetails.getMobileNumber());
			quickbooksUser.setPrimaryPhone(phoneNumber);
			resp = quickbooksService.addUser(applicationId, quickbooksUser);
			if (resp.getStatus().is2xxSuccessful()) {
				break;
			} else {
				count++;
			}
		}
		if (count == 3) {
			resp.setStatus(HttpStatus.BAD_REQUEST);
			resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
			resp.setResponse(new Response("QuickbooksLicenseAssignRequest", null));
		}
		return resp;
	}

	private CommonResponse createUserHubSpot(String applicationId, String userEmail) {
		CommonResponse resp = new CommonResponse();
		int count = 0;
		while (count < 3) {
			CreateHubSpotUserRequest hubSpotUserRequest = new CreateHubSpotUserRequest();
			hubSpotUserRequest.setSendWelcomeEmail(true);
			hubSpotUserRequest.setEmail(userEmail);
			resp = hubSpotService.createUser(hubSpotUserRequest, applicationId);
			if (resp.getStatus().is2xxSuccessful()) {
				break;
			} else {
				count++;
			}
		}
		if (count == 3) {
			resp.setStatus(HttpStatus.BAD_REQUEST);
			resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
			resp.setResponse(new Response("HubSpotLicenseAssignRequest", null));
		}
		return resp;
	}

	private CommonResponse createConfluenceUser(String applicationId, String userEmail) {
		CommonResponse resp = new CommonResponse();
		int count = 0;
		while (count < 3) {
			ConfluenceCreateUser confluenceCreateUser = new ConfluenceCreateUser();
			confluenceCreateUser.setEmailAddress(userEmail);
			resp = confluenceWrapperService.createUser(confluenceCreateUser, applicationId);
			if (resp.getStatus().is2xxSuccessful()) {
				break;
			} else {
				count++;
			}
		}
		if (count == 3) {
			resp.setStatus(HttpStatus.BAD_REQUEST);
			resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
			resp.setResponse(new Response("ConfluenceLicenseAssignRequest", null));
		}
		return resp;
	}

	private CommonResponse createUserDatadog(String applicationId, String userEmail) {
		CommonResponse resp = new CommonResponse();
		int count = 0;
		while (count < 3) {
			resp = datadogWrapperService.createUser(userEmail, applicationId);
			if (resp.getStatus().is2xxSuccessful()) {
				break;
			} else {
				count++;
			}
		}
		if (count == 3) {
			resp.setStatus(HttpStatus.BAD_REQUEST);
			resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
			resp.setResponse(new Response("DatadogLicenseAssignRequest", null));
		}
		return resp;
	}

	private CommonResponse createZohoPeopleUser(String applicationId, String userEmail) {
		CommonResponse resp = new CommonResponse();
		int count = 0;
		while (count < 3) {
			UserOnboarding userDetail = userOnboardingDetailsRepository.findByEmailAddresss(userEmail);
			ZohoPeopleInviteRequest inviteRequest = new ZohoPeopleInviteRequest();
			inviteRequest.setEmailID(userEmail);
			inviteRequest.setFirstName(userDetail.getFirstName());
			inviteRequest.setLastName(userDetail.getLastName());
			inviteRequest.setEmployeeID(userDetail.getUserId());
			resp = zohoPeopleService.addUser(inviteRequest, applicationId);
			if (resp.getStatus().is2xxSuccessful()) {
				break;
			} else {
				count++;
			}
		}
		if (count == 3) {
			resp.setStatus(HttpStatus.BAD_REQUEST);
			resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
			resp.setResponse(new Response("ZohoPeopleLicenseAssignRequest", null));
		}
		return resp;
	}

	private CommonResponse assignMicrosoft365License(String applicationId, String userEmail, String productName) {
	    CommonResponse resp = new CommonResponse();
	        int count = 0;
	        while (count < 3) {
	            resp = microsoft365WrapperService.assignLicense(applicationId, userEmail, productName);
	            if (resp.getStatus().is2xxSuccessful()) {
	                break;
	            } else {
	                count++;
	            }
	        }
	        if (count == 3) {
	            resp.setStatus(HttpStatus.BAD_REQUEST);
	            resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
	            resp.setResponse(new Response("Microsoft365LicenseAssignRequest", null));
	        }
	     
	    return resp;
	}





	private CommonResponse createZohoAnalyticsUser(String applicationId, String userEmail) {
		CommonResponse resp = new CommonResponse();
		int count = 0;
		while (count < 3) {
			resp = zohoAnalyticsService.inviteUser(applicationId, userEmail);
			if (resp.getStatus().is2xxSuccessful()) {
				break;
			} else {
				count++;
			}
		}
		if (count == 3) {
			resp.setStatus(HttpStatus.BAD_REQUEST);
			resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
			resp.setResponse(new Response("GitLabLicenseAssignRequest", null));
		}
		return resp;
	}

	private CommonResponse createSalesforceUser(String applicationId, String userEmail, String userId,
			String firstName) {
		CommonResponse resp = new CommonResponse();
		int count = 0;
		while (count < 3) {
			resp = salesforceService.createUser(applicationId, userEmail, userId, firstName);
			if (resp.getStatus().is2xxSuccessful()) {
				break;
			} else {
				count++;
			}
		}
		if (count == 3) {
			resp.setStatus(HttpStatus.BAD_REQUEST);
			resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
			resp.setResponse(new Response("SalesforceLicenseAssignRequest", null));
		}
		return resp;
	}

	private CommonResponse createUserJira(String applicationId, String userEmail) {
		CommonResponse resp = new CommonResponse();
		int count = 0;
		while (count < 3) {
			JiraCreateUserRequest jiraCreateUserRequest = new JiraCreateUserRequest();
			jiraCreateUserRequest.setEmailAddress(userEmail);
			jiraCreateUserRequest.setProducts(Collections.singletonList("jira-software"));
			resp = jiraWrapperService.createUser(jiraCreateUserRequest, applicationId);
			if (resp.getStatus().is2xxSuccessful()) {
				break;
			} else {
				count++;
			}
		}
		if (count == 3) {
			resp.setStatus(HttpStatus.BAD_REQUEST);
			resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
			resp.setResponse(new Response("ConfluenceLicenseAssignRequest", null));
		}
		return resp;
	}

	private CommonResponse createFreshdeskUser(String applicationId, @NonNull String userEmail,
			@NonNull String userName) {
		CommonResponse resp = new CommonResponse();
		int count = 0;
		while (count < 3) {
			resp = freshdeskService.inviteUser(applicationId, userEmail, userName);
			if (resp.getStatus().is2xxSuccessful()) {
				break;
			} else {
				count++;
			}
		}
		if (count == 3) {
			resp.setStatus(HttpStatus.BAD_REQUEST);
			resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
			resp.setResponse(new Response("FreshdeskAssignLicenseRequest", null));
		}
		return resp;
	}
	
	
	private CommonResponse createUserZoom(String applicationId, String userEmail) {
	    CommonResponse resp = new CommonResponse();
	    int count = 0;
	    while (count < 3) {
	    	UserOnboarding userDetail = userOnboardingDetailsRepository.findByEmailAddresss(userEmail);
	        CreateZoomUserRequest createZoomUserRequest = new CreateZoomUserRequest();
	        CreateZoomUserRequest.UserInfo userInfo = new CreateZoomUserRequest.UserInfo();
	        userInfo.setEmail(userEmail);
	        userInfo.setFirst_name(userDetail.getFirstName());
	        userInfo.setLast_name(userDetail.getLastName());
	        userInfo.setDisplay_name(userDetail.getFirstName()); 
	        userInfo.setPassword(userDetail.getPassWord());
	        userInfo.setType(2); 
	        
	        createZoomUserRequest.setAction("create"); 
	        createZoomUserRequest.setUser_info(userInfo);
	        
	        resp = zoomWrapperService.createUser(createZoomUserRequest, applicationId);
	        if (resp.getStatus().is2xxSuccessful()) {
	            break;
	        } else {
	            count++;
	        }
	    }
	    if (count == 3) {
	        resp.setStatus(HttpStatus.BAD_REQUEST);
	        resp.setMessage(Constant.LICENSE_NOT_ASSIGNED + userEmail);
	        resp.setResponse(new Response("ZoomLicenseAssignRequest", null));
	    }
	    return resp;
	}

}
