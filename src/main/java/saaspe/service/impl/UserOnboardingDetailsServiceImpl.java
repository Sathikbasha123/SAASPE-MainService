package saaspe.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import saaspe.configuration.DateParser;
import saaspe.constant.Constant;
import saaspe.dto.ExcelUserOnboardingUploadDTO;
import saaspe.entity.ApplicationDetails;
import saaspe.entity.DepartmentDetails;
import saaspe.entity.SequenceGenerator;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.entity.UserOnboarding;
import saaspe.entity.Users;
import saaspe.entity.VerificationDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.CreateUserDetails;
import saaspe.model.NewApplicationOnboardingResposne;
import saaspe.model.Response;
import saaspe.model.SingleOnBoardingResponse;
import saaspe.model.UserDetailsResponse;
import saaspe.model.UserOnboardingDetailsRequest;
import saaspe.model.UserOnboardingListView;
import saaspe.model.UserOnboardingRequestDetailViewResponse;
import saaspe.model.UserOnboardingWorkFlowRequest;
import saaspe.model.UserReviewerDetails;
import saaspe.model.UserSingleOnboardingRequest;
import saaspe.repository.ApplicationDetailsRepository;
import saaspe.repository.DepartmentRepository;
import saaspe.repository.SequenceGeneratorRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.repository.UserOnboardingDetailsRepository;
import saaspe.repository.UsersRepository;
import saaspe.repository.VerificationDetailsRepository;
import saaspe.service.UserOnboardingDetailsService;
import saaspe.utils.CommonUtil;

@Service
public class UserOnboardingDetailsServiceImpl implements UserOnboardingDetailsService {

	@Autowired
	private UserOnboardingDetailsRepository userOnboardingDetailsRepo;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	private SequenceGeneratorRepository sequenceGeneratorRepository;

	@Autowired
	private ApplicationDetailsRepository applicationDetailsRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private VerificationDetailsRepository verificationDetailsRepository;

	@Autowired
	private Configuration config;
	
	@Autowired
	private UsersRepository usersRepository;

	@Value("${sendgrid.domain.name}")
	private String mailDomainName;

	@Value("${sendgrid.domain.sendername}")
	private String senderName;

	@Value("${logos.user.onboarding.url}")
	private String onboardingUrl;

	@Override
	@Transactional
	public CommonResponse userSingleOnboarding(UserSingleOnboardingRequest userOnboardingDetails,
			UserLoginDetails profile) throws DataValidationException {
		String extractedCountryCode = userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserMobileNumber()
				.split(" ")[0];
		UserOnboarding userOnboarding = new UserOnboarding();
		UserOnboarding lists = userOnboardingDetailsRepo
				.findByEmailAddresss(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserEmailAddress());
		List<UserOnboarding> apps = new ArrayList<>();
		if (Constant.DESIGNATIONFORONBOARDUSER.stream().noneMatch(
				userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserDesignation()::equalsIgnoreCase)) {
			throw new DataValidationException("Designation is not match", "400", HttpStatus.BAD_REQUEST);
		} else if (Constant.EMPLOYEMENT.stream().noneMatch(
				userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserType()::equalsIgnoreCase)) {
			throw new DataValidationException("Type of Employement is not match", "400", HttpStatus.BAD_REQUEST);
		} else if (Constant.GENDER.stream().noneMatch(
				userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserGender()::equalsIgnoreCase)) {
			throw new DataValidationException("Gender is not match", "400", HttpStatus.BAD_REQUEST);
		} else if (!Constant.COUNTRY_CODE.contains(extractedCountryCode)) {
			throw new DataValidationException("Country code is not matching", "400", HttpStatus.BAD_REQUEST);
		}
		if (userDetailsRepository.findByUserMobileNumber(
				userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserMobileNumber()) != null) {
			throw new DataValidationException("User Mobile Number Already Registered", "400", HttpStatus.BAD_REQUEST);
		}
		UserDetails deleteChecking = null;
		if (lists != null) {
			List<UserOnboarding> loop = new ArrayList<>();
			loop.add(lists);
			List<UserOnboarding> userOnboardings = getUserStatus(loop);
			apps = userOnboardings.stream().filter(p -> p.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW))
					.collect(Collectors.toList());
		}
		deleteChecking = userDetailsRepository.getDeletedUserByUserEmail(
				userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserEmailAddress());
		if (apps.isEmpty() && deleteChecking == null) {
			DepartmentDetails departmentDetails = departmentRepository.findByDepartmentName(
					userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserDepartment());
			if (departmentDetails == null) {
				throw new DataValidationException(
						"Department not found for name: "
								+ userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserDepartment(),
						"400", HttpStatus.BAD_REQUEST);
			}
			String name = Constant.USER_ID;
			Integer sequence = sequenceGeneratorRepository.getUserOnboardingSequence();
			name = name.concat(sequence.toString());
			SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
			updateSequence.setUserOnboarding(++sequence);
			sequenceGeneratorRepository.save(updateSequence);

			userOnboarding.setUserId(name);
			String request = "REQ_USR_0";
			Integer sequence1 = sequenceGeneratorRepository.getUserReqSequence();
			request = request.concat(sequence1.toString());
			SequenceGenerator updateSequence1 = sequenceGeneratorRepository.getById(1);
			updateSequence1.setUserRequestId(++sequence1);
			userOnboarding
					.setUserEmail(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserEmailAddress());
			userOnboarding.setFirstName(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserFirstName());
			userOnboarding.setCreatedBy(profile.getEmailAddress());
			userOnboarding.setCreatedOn(new Date());
			userOnboarding.setLastName(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserLastName());
			userOnboarding.setDepartmentId(departmentDetails);
			userOnboarding.setUserReportingManager(
					userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserReportingManager());
			userOnboarding
					.setJoiningDate(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserJoiningDate());
			userOnboarding.setUserType(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserType());
			userOnboarding.setUserDesignation(
					userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserDesignation());
			userOnboarding.setGender(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserGender());
			userOnboarding
					.setMobileNumber(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserMobileNumber());
			if (userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserGender().equalsIgnoreCase("Male")) {
				userOnboarding.setLogoUrl(Constant.AVATAR_13);
			}
			if (userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserGender().equalsIgnoreCase("Female")) {
				userOnboarding.setLogoUrl(onboardingUrl);
			}
			if (userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserGender().equalsIgnoreCase("Others")) {
				userOnboarding.setLogoUrl(Constant.AVATAR_13);
			}
			userOnboarding.setRequestNumber(request);
			userOnboarding.setOnboardDate(new Date());
			userOnboarding.setApprovedRejected(Constant.REVIEW);
			userOnboarding.setBuID(Constant.BUID);
			userOnboarding.setWorkGroup(Constant.REVIEWER);
			userOnboarding.setOnboardingStatus(Constant.PENDING_WITH_REVIEWER);
			userOnboarding.setWorkGroupUserEmail(profile.getEmailAddress());
			userOnboarding.setOnboardedByUserEmail(profile.getEmailAddress());
			userOnboarding.setCreatedOn(new Date());
			userOnboarding.setSignUp(false);
			userOnboardingDetailsRepo.save(userOnboarding);
			
			Users users = new Users();
			users.setUserId(name);
			users.setUserEmail(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserEmailAddress());
			users.setUserName(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserFirstName()+" "+userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserLastName());
			users.setBuID(Constant.BUID);
			users.setUserCreatedOn(new Date());
			users.setUserCreatedBy(profile.getEmailAddress());
			users.setDepartmentId(departmentDetails.getDepartmentId());
			users.setUserReportingManager(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserReportingManager());
			users.setJoiningDate(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserJoiningDate());
			users.setUserType(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserType());
			users.setUserDesignation(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserDesignation());
			users.setMobileNumber(userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserMobileNumber());
			if (userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserGender().equalsIgnoreCase("Male")) {
				users.setLogoUrl(Constant.AVATAR_13);
			}
			if (userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserGender().equalsIgnoreCase("Female")) {
				users.setLogoUrl(onboardingUrl);
			}
			if (userOnboardingDetails.getCreateUserRequest().getUserInfo().getUserGender().equalsIgnoreCase("Others")) {
				users.setLogoUrl(Constant.AVATAR_13);
			}
			usersRepository.save(users);
			
			sequenceGeneratorRepository.save(updateSequence1);
			SingleOnBoardingResponse responseBoardingResponse = new SingleOnBoardingResponse();
			responseBoardingResponse.setRequestId(request);
			return new CommonResponse(HttpStatus.CREATED, new Response("createUserResponse", responseBoardingResponse),
					"Onboarding request submitted!");
		} else {
			throw new DataValidationException(Constant.USER_DETAILS_ALL_READY_REGISTERED, null, null);
		}
	}

	@Override
	@Transactional
	public UserDetailsResponse modifyUserOnboardingDetails(UserOnboardingDetailsRequest userOnboardingDetails)
			throws DataValidationException {
		UserOnboarding userOnboarding = userOnboardingDetailsRepo.findByUserId(userOnboardingDetails.getUserId());
		if (!Constant.DESIGNATIONFORONBOARDUSER.contains(userOnboardingDetails.getUserDesigination())) {
			throw new DataValidationException("Designation is not match", "400", HttpStatus.BAD_REQUEST);
		} else if (!Constant.EMPLOYEMENT.contains(userOnboardingDetails.getUserType())) {
			throw new DataValidationException("Type of Employement is not match", "400", HttpStatus.BAD_REQUEST);
		}
		UserDetailsResponse response = new UserDetailsResponse();
		if (userOnboardingDetailsRepo.existsById(Integer.valueOf(userOnboardingDetails.getUserId()))) {
			userOnboarding.setBuID(userOnboardingDetails.getBuID());
			userOnboarding.setCreatedBy(userOnboardingDetails.getCreatedBy());
			userOnboarding.setCreatedOn(userOnboardingDetails.getCreatedOn());
			userOnboarding.setJoiningDate(userOnboardingDetails.getJoiningDate());
			userOnboarding.setDepartmentId(userOnboardingDetails.getDepartmentId());
			userOnboarding.setLogoUrl(userOnboardingDetails.getLogoUrl());
			userOnboarding.setOnboardDate(userOnboardingDetails.getOnboardDate());
			userOnboarding.setUserEmail(userOnboardingDetails.getUserEmail());
			userOnboarding.setFirstName(userOnboardingDetails.getFirstName());
			userOnboarding.setLastName(userOnboardingDetails.getLastName());
			userOnboarding.setUserReportingManager(userOnboardingDetails.getUserReportingManager());
			userOnboarding.setUserType(userOnboardingDetails.getUserType());
			userOnboarding.setUpdatedOn(new Date());
			userOnboarding.setUpdatedBy(userOnboardingDetails.getUpdatedBy());
			userOnboardingDetailsRepo.save(userOnboarding);
			
			Users user = new Users();
			user.setBuID(userOnboardingDetails.getBuID());
			user.setDepartmentId(userOnboardingDetails.getDepartmentId().getDepartmentId());
			user.setUserCreatedBy(userOnboardingDetails.getCreatedBy());
			user.setUserCreatedOn(userOnboardingDetails.getCreatedOn());
			user.setUserType(userOnboardingDetails.getUserType());
			user.setUserName(userOnboardingDetails.getFirstName()+" "+userOnboardingDetails.getLastName());
			user.setJoiningDate(userOnboardingDetails.getJoiningDate());
			user.setLogoUrl(userOnboardingDetails.getLogoUrl());
			user.setAppUser(true);
			user.setUserEmail(userOnboardingDetails.getUserEmail());
			user.setUpdatedOn(new Date());
			user.setUpdatedBy(userOnboardingDetails.getUpdatedBy());
			user.setUserReportingManager(userOnboardingDetails.getUserReportingManager());
			usersRepository.save(user);
			
			response.setUserId(userOnboardingDetails.getUserId());
			response.setMessage("USER ONBOARDING " + userOnboardingDetails.getUserId() + "DATA UPDATED!");
		} else {
			throw new DataValidationException(Constant.USER_DETAILS_NOT_FOUND, "404", HttpStatus.NOT_FOUND);
		}
		return response;
	}

	@Override
	public CommonResponse getUserOnboardingDetailsByUserId(String userId) throws DataValidationException {
		if (userOnboardingDetailsRepo.existsById(Integer.valueOf(userId))) {
			return null;
		} else {
			throw new DataValidationException(Constant.USER_DETAILS_NOT_FOUND, "404", HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public UserDetailsResponse removeUserOnBoardingDetailsByUserId(String userId) throws DataValidationException {
		UserDetailsResponse response = new UserDetailsResponse();
		if (userOnboardingDetailsRepo.existsById(Integer.valueOf(userId))) {
			userOnboardingDetailsRepo.deleteById(Integer.valueOf(userId));
			response.setMessage("USER ONBOARDING DEATAILS REMOVED!");
			response.setUserId(userId);
			return response;
		} else {
			throw new DataValidationException(Constant.USER_DETAILS_NOT_FOUND, "404", HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public UserDetailsResponse userOnboardingDetails() throws DataValidationException {
		UserDetailsResponse response = new UserDetailsResponse();
		List<UserOnboarding> userData = userOnboardingDetailsRepo.findAll(Sort.by(Sort.Direction.DESC, "createdOn"));
		response.setSize(userData.size());
		return response;
	}

	@Override
	public CommonResponse userReviewerApproverListView(UserLoginDetails profile) {
		UserDetails userDetails = userDetailsRepository.getByEmail(profile.getEmailAddress());
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
			List<UserOnboardingListView> listOfUsers = getListOfUserOnBoarding(Constant.REVIEWER, Constant.REVIEW);
			return new CommonResponse(HttpStatus.OK, new Response("reviewerListViewResponse", listOfUsers),
					Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER)) {
			List<UserOnboardingListView> listOfDept = getListOfUserOnBoarding(Constant.APPROVER, Constant.REVIEW);
			return new CommonResponse(HttpStatus.OK, new Response("approverListViewResponse", listOfDept),
					Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
		}
		List<UserOnboarding> onboarding = userOnboardingDetailsRepo.findAllByListView();
		List<UserOnboardingListView> list = new ArrayList<>();
		for (UserOnboarding onBoarding : onboarding) {
			UserOnboardingListView listViewResponse = new UserOnboardingListView();
			listViewResponse.setUserName(onBoarding.getFirstName().concat(" ").concat(onBoarding.getLastName()));
			listViewResponse.setOnboardedByEmail(onBoarding.getOnboardedByUserEmail());
			listViewResponse.setRequestId(onBoarding.getRequestNumber());
			listViewResponse.setChildRequestId(onBoarding.getChildRequestNumber());
			listViewResponse.setUserAvatar(onBoarding.getLogoUrl());
			listViewResponse.setOnboardingStatus(onBoarding.getOnboardingStatus());
			listViewResponse.setUserAvatar(onBoarding.getLogoUrl());
			list.add(listViewResponse);
		}
		return new CommonResponse(HttpStatus.OK, new Response("userOnboardingListViewResponse", list),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private List<UserOnboardingListView> getListOfUserOnBoarding(String role, String key) {
		List<UserOnboarding> userOnboarding = userOnboardingDetailsRepo.getAllByName(role, key);
		List<UserOnboardingListView> list = new ArrayList<>();
		for (UserOnboarding userOnBoarding : userOnboarding) {
			UserOnboardingListView listViewResponse = new UserOnboardingListView();
			listViewResponse
					.setUserName(userOnBoarding.getFirstName().concat(" ").concat(userOnBoarding.getLastName()));
			listViewResponse.setOnboardedByEmail(userOnBoarding.getOnboardedByUserEmail());
			listViewResponse.setRequestId(userOnBoarding.getRequestNumber());
			listViewResponse.setChildRequestId(userOnBoarding.getChildRequestNumber());
			listViewResponse.setUserAvatar(userOnBoarding.getLogoUrl());
			if (role.equalsIgnoreCase(Constant.APPROVER)) {
				listViewResponse.setReviewedByEmail(userOnBoarding.getWorkGroupUserEmail());
				listViewResponse.setOnboardingStatus(userOnBoarding.getOnboardingStatus());
			}
			list.add(listViewResponse);
		}
		return list;
	}

	@Override
	@Transactional
	public CommonResponse userOnboardReview(String childRequestId, String requestId, UserLoginDetails profile,
			UserOnboardingWorkFlowRequest onboardingWorkFlowRequest)
			throws DataValidationException, IOException, TemplateException, MessagingException {
		CommonResponse commonResponse = new CommonResponse();
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
		UserDetails userDetails = userDetailsRepository.getByEmail(profile.getEmailAddress());
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)
				|| userDetails.getUserRole().equalsIgnoreCase("super_admin")) {
			if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
				if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
					if (requestId == null && childRequestId != null) {
						UserOnboarding childRequest = userOnboardingDetailsRepo.findByChildRequestNumber(childRequestId,
								Constant.REVIEWER, Constant.REVIEW);
						if (childRequest == null) {
							throw new DataValidationException(Constant.VALID_ID, requestId, HttpStatus.CONFLICT);
						}
						UserOnboarding onboarding = new UserOnboarding();
						onboarding.setChildRequestNumber(childRequestId);
						onboarding.setWorkGroup(Constant.APPROVER);
						onboarding.setApprovedRejected(Constant.REVIEW);
						onboarding.setBuID(Constant.BUID);
						onboarding.setCreatedOn(new Date());
						onboarding.setCreatedBy(childRequest.getCreatedBy());
						onboarding.setOpID(childRequest.getOpID());
						onboarding.setDepartmentId(childRequest.getDepartmentId());
						onboarding.setUpdatedOn(new Date());
						onboarding.setRequestNumber(childRequest.getRequestNumber());
						onboarding.setOnboardedByUserEmail(childRequest.getOnboardedByUserEmail());
						onboarding.setLogoUrl(childRequest.getLogoUrl());
						onboarding.setWorkGroupUserEmail(profile.getEmailAddress());
						onboarding.setUserType(childRequest.getUserType());
						onboarding.setMobileNumber(childRequest.getMobileNumber());
						onboarding.setUserEmail(childRequest.getUserEmail());
						onboarding.setEndDate(childRequest.getEndDate());
						onboarding.setJoiningDate(childRequest.getJoiningDate());
						onboarding.setComments(onboardingWorkFlowRequest.getComments());
						onboarding.setUserId(childRequest.getUserId());
						onboarding.setUserDesignation(childRequest.getUserDesignation());
						onboarding.setFirstName(childRequest.getFirstName());
						onboarding.setLastName(childRequest.getLastName());
						onboarding.setGender(childRequest.getGender());
						onboarding.setUserReportingManager(childRequest.getUserReportingManager());
						onboarding.setOnboardingStatus(Constant.PENDING_WITH_APPROVER);
						childRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						childRequest.setApprovedRejected(Constant.APPROVE);
						childRequest.setOnboardingStatus("Approved By Reviewer");
						childRequest.setComments(onboardingWorkFlowRequest.getComments());
						childRequest.setEndDate(new Date());
						childRequest.setUpdatedBy(profile.getFirstName() + " " + profile.getLastName());
						childRequest.setUpdatedOn(new Date());
						userOnboardingDetailsRepo.save(onboarding);
						userOnboardingDetailsRepo.save(childRequest);
					}
					if (requestId != null && childRequestId == null) {
						UserOnboarding parentRequest = userOnboardingDetailsRepo.findByRequestNumber(requestId,
								Constant.REVIEWER, Constant.REVIEW);
						if (parentRequest == null) {
							throw new DataValidationException(Constant.VALID_ID, requestId, HttpStatus.CONFLICT);
						}
						if (parentRequest.isSignUp()) {
							UserOnboarding onboarding = new UserOnboarding();
							onboarding.setChildRequestNumber(childRequestId);
							onboarding.setWorkGroup(Constant.APPROVER);
							onboarding.setApprovedRejected(Constant.REVIEW);
							onboarding.setBuID(Constant.BUID);
							onboarding.setCreatedOn(new Date());
							onboarding.setCreatedBy(parentRequest.getCreatedBy());
							onboarding.setOpID(parentRequest.getOpID());
							onboarding.setDepartmentId(parentRequest.getDepartmentId());
							onboarding.setUpdatedOn(new Date());
							onboarding.setRequestNumber(parentRequest.getRequestNumber());
							onboarding.setOnboardedByUserEmail(parentRequest.getOnboardedByUserEmail());
							onboarding.setLogoUrl(parentRequest.getLogoUrl());
							onboarding.setWorkGroupUserEmail(profile.getEmailAddress());
							onboarding.setUserType(parentRequest.getUserType());
							onboarding.setMobileNumber(parentRequest.getMobileNumber());
							onboarding.setUserEmail(parentRequest.getUserEmail());
							onboarding.setEndDate(parentRequest.getEndDate());
							onboarding.setJoiningDate(parentRequest.getJoiningDate());
							onboarding.setComments(onboardingWorkFlowRequest.getComments());
							onboarding.setUserId(parentRequest.getUserId());
							onboarding.setUserDesignation(parentRequest.getUserDesignation());
							onboarding.setFirstName(parentRequest.getFirstName());
							onboarding.setVerificationUrl(parentRequest.getVerificationUrl());
							onboarding.setSignUp(true);
							onboarding.setPassWord(parentRequest.getPassWord());
							onboarding.setLastName(parentRequest.getLastName());
							onboarding.setGender(parentRequest.getGender());
							onboarding.setUserReportingManager(parentRequest.getUserReportingManager());
							onboarding.setOnboardingStatus("Pending With Approver");
							parentRequest.setWorkGroupUserEmail(profile.getEmailAddress());
							parentRequest.setApprovedRejected(Constant.APPROVE);
							parentRequest.setEndDate(new Date());
							parentRequest.setComments(onboardingWorkFlowRequest.getComments());
							parentRequest.setUpdatedBy(profile.getFirstName());
							parentRequest.setUpdatedOn(new Date());
							userOnboardingDetailsRepo.save(onboarding);
							userOnboardingDetailsRepo.save(parentRequest);
							return reviewSuccessResponse();
						}
						UserOnboarding onboarding = new UserOnboarding();
						onboarding.setChildRequestNumber(childRequestId);
						onboarding.setWorkGroup(Constant.APPROVER);
						onboarding.setApprovedRejected(Constant.REVIEW);
						onboarding.setBuID(Constant.BUID);
						onboarding.setCreatedOn(new Date());
						onboarding.setCreatedBy(parentRequest.getCreatedBy());
						onboarding.setOpID(parentRequest.getOpID());
						onboarding.setDepartmentId(parentRequest.getDepartmentId());
						onboarding.setUpdatedOn(new Date());
						onboarding.setRequestNumber(parentRequest.getRequestNumber());
						onboarding.setOnboardedByUserEmail(parentRequest.getOnboardedByUserEmail());
						onboarding.setLogoUrl(parentRequest.getLogoUrl());
						onboarding.setWorkGroupUserEmail(profile.getEmailAddress());
						onboarding.setUserType(parentRequest.getUserType());
						onboarding.setMobileNumber(parentRequest.getMobileNumber());
						onboarding.setUserEmail(parentRequest.getUserEmail());
						onboarding.setEndDate(parentRequest.getEndDate());
						onboarding.setJoiningDate(parentRequest.getJoiningDate());
						onboarding.setComments(onboardingWorkFlowRequest.getComments());
						onboarding.setUserId(parentRequest.getUserId());
						onboarding.setUserDesignation(parentRequest.getUserDesignation());
						onboarding.setFirstName(parentRequest.getFirstName());
						onboarding.setLastName(parentRequest.getLastName());
						onboarding.setGender(parentRequest.getGender());
						onboarding.setUserReportingManager(parentRequest.getUserReportingManager());
						onboarding.setOnboardingStatus("Pending With Approver");
						parentRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						parentRequest.setApprovedRejected(Constant.APPROVE);
						parentRequest.setEndDate(new Date());
						parentRequest.setComments(onboardingWorkFlowRequest.getComments());
						parentRequest.setUpdatedBy(profile.getFirstName());
						parentRequest.setUpdatedOn(new Date());
						userOnboardingDetailsRepo.save(onboarding);
						userOnboardingDetailsRepo.save(parentRequest);
					}
					return reviewSuccessResponse();
				} else {
					if (requestId != null) {
						UserOnboarding rejectRequest = userOnboardingDetailsRepo.findByRequestNumber(requestId,
								Constant.REVIEWER, Constant.REVIEW);
						if (rejectRequest == null) {
							throw new DataValidationException(Constant.VALID_ID, requestId, HttpStatus.CONFLICT);
						}

						if (rejectRequest.isSignUp()) {
							rejectRequest.setApprovedRejected(Constant.REJECTED);
							if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
								rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_REVIEWER);
							} else {
								rejectRequest.setWorkGroup(Constant.SUPERADMIN);
								rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
								if (rejectRequest.isSignUp()) {
									sendRejectedEmail(rejectRequest.getUserEmail(),
											rejectRequest.getFirstName() + " " + rejectRequest.getLastName());
								}
							}
							rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
							userOnboardingDetailsRepo.save(rejectRequest);
							if (rejectRequest.isSignUp()) {
								sendRejectedEmail(rejectRequest.getUserEmail(),
										rejectRequest.getFirstName() + " " + rejectRequest.getLastName());
							}
							return reviewFailureResponse();
						}
						rejectRequest.setApprovedRejected(Constant.REJECTED);
						if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_REVIEWER);
						} else {
							rejectRequest.setWorkGroup(Constant.SUPERADMIN);
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							if (rejectRequest.isSignUp()) {
								sendRejectedEmail(rejectRequest.getUserEmail(),
										rejectRequest.getFirstName() + " " + rejectRequest.getLastName());
							}
						}
						rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
						userOnboardingDetailsRepo.save(rejectRequest);
						return reviewFailureResponse();

					} else {
						UserOnboarding rejectRequest = userOnboardingDetailsRepo
								.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
						if (rejectRequest == null) {
							throw new DataValidationException(Constant.VALID_ID, requestId, HttpStatus.CONFLICT);
						}
						if (rejectRequest.isSignUp()) {
							rejectRequest.setApprovedRejected(Constant.REJECTED);
							if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
								rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_REVIEWER);
							} else {
								rejectRequest.setWorkGroup(Constant.SUPERADMIN);
								rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
								if (rejectRequest.isSignUp()) {
									sendRejectedEmail(rejectRequest.getUserEmail(),
											rejectRequest.getFirstName() + " " + rejectRequest.getLastName());
								}
							}
							if (rejectRequest.isSignUp()) {
								sendRejectedEmail(rejectRequest.getUserEmail(),
										rejectRequest.getFirstName() + " " + rejectRequest.getLastName());
							}
							rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
							userOnboardingDetailsRepo.save(rejectRequest);
							return reviewFailureResponse();
						}
						rejectRequest.setApprovedRejected(Constant.REJECTED);
						if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_REVIEWER);
						} else {
							rejectRequest.setWorkGroup(Constant.SUPERADMIN);
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							if (rejectRequest.isSignUp()) {
								sendRejectedEmail(rejectRequest.getUserEmail(),
										rejectRequest.getFirstName() + " " + rejectRequest.getLastName());
							}
						}
						rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
						userOnboardingDetailsRepo.save(rejectRequest);
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
						UserOnboarding rejectRequestForReviewer = userOnboardingDetailsRepo
								.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
						UserOnboarding rejectRequestForApprover = userOnboardingDetailsRepo
								.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
						if (rejectRequestForReviewer != null) {
							rejectRequestForReviewer.setApprovedRejected(Constant.REJECTED);
							rejectRequestForReviewer.setComments(onboardingWorkFlowRequest.getComments());
							rejectRequestForReviewer.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							userOnboardingDetailsRepo.save(rejectRequestForReviewer);
							if (rejectRequestForReviewer.isSignUp()) {
								sendRejectedEmail(rejectRequestForReviewer.getUserEmail(),
										rejectRequestForReviewer.getFirstName() + " "
												+ rejectRequestForReviewer.getLastName());
							}
							return reviewFailureResponse();
						}
						if (rejectRequestForApprover != null) {
							rejectRequestForApprover.setApprovedRejected(Constant.REJECTED);
							rejectRequestForApprover.setComments(onboardingWorkFlowRequest.getComments());
							rejectRequestForApprover.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							if (rejectRequestForApprover.isSignUp()) {
								sendRejectedEmail(rejectRequestForApprover.getUserEmail(),
										rejectRequestForApprover.getFirstName() + " "
												+ rejectRequestForApprover.getLastName());
							}
							userOnboardingDetailsRepo.save(rejectRequestForApprover);
							return reviewFailureResponse();
						}
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);

					} else {
						UserOnboarding rejectRequestForReviewer = userOnboardingDetailsRepo
								.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
						UserOnboarding rejectRequestForApprover = userOnboardingDetailsRepo
								.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
						if (rejectRequestForReviewer != null) {
							rejectRequestForReviewer.setApprovedRejected(Constant.REJECTED);
							rejectRequestForReviewer.setComments(onboardingWorkFlowRequest.getComments());
							rejectRequestForReviewer.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							userOnboardingDetailsRepo.save(rejectRequestForReviewer);
							return reviewFailureResponse();
						}
						if (rejectRequestForApprover != null) {
							rejectRequestForApprover.setApprovedRejected(Constant.REJECTED);
							rejectRequestForApprover.setComments(onboardingWorkFlowRequest.getComments());
							rejectRequestForApprover.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							userOnboardingDetailsRepo.save(rejectRequestForApprover);

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
						UserOnboarding childRequest = userOnboardingDetailsRepo.findByChildRequestNumber(childRequestId,
								Constant.APPROVER, Constant.REVIEW);
						if (childRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
									HttpStatus.CONFLICT);
						}
						childRequest.setComments(onboardingWorkFlowRequest.getComments());
						childRequest.setOnboardingStatus(Constant.APPROVED_BY_APPROVER);
						childRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						childRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						childRequest.setEndDate(new Date());
						childRequest.setCreatedOn(new Date());
						childRequest.setUpdatedBy(profile.getFirstName());
						childRequest.setUpdatedOn(new Date());
						UserDetails details = new UserDetails();
						details.setBuID(Constant.BUID_02);
						details.setCreatedBy(profile.getEmailAddress());
						details.setLogoUrl(childRequest.getLogoUrl());
						details.setUserEmail(childRequest.getUserEmail());
						details.setDepartmentId(childRequest.getDepartmentId());
						details.setJoiningDate(childRequest.getJoiningDate());
						details.setUpdatedOn(childRequest.getUpdatedOn());
						details.setUserDesigination(childRequest.getUserDesignation());
						details.setUserId(childRequest.getUserId());
						details.setUserReportingManager(childRequest.getUserReportingManager());
						details.setUserType(childRequest.getUserType());
						details.setUserStatus(Constant.ACTIVE);
						if (childRequest.isSignUp()) {
							details.setUserRole(Constant.CONTRIBUTOR);
							details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
							details.setMobileNumber(childRequest.getMobileNumber());
						}
						details.setUserId(childRequest.getUserId());
						details.setUserName(childRequest.getFirstName().concat(" ").concat(childRequest.getLastName()));
						userDetailsRepository.save(details);
						userOnboardingDetailsRepo.save(childRequest);
					}
					if (requestId != null && childRequestId == null) {
						UserOnboarding parentRequest = userOnboardingDetailsRepo.findByRequestNumber(requestId,
								Constant.APPROVER, Constant.REVIEW);
						if (parentRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
									HttpStatus.CONFLICT);
						}
						if (parentRequest.isSignUp()) {
							parentRequest.setOnboardingStatus(Constant.APPROVED_BY_APPROVER);
							parentRequest.setWorkGroupUserEmail(profile.getEmailAddress());
							parentRequest.setComments(onboardingWorkFlowRequest.getComments());
							parentRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
							parentRequest.setWorkGroup(Constant.APPROVER);
							parentRequest.setEndDate(new Date());
							parentRequest.setCreatedOn(new Date());
							parentRequest.setUpdatedBy(profile.getFirstName());
							parentRequest.setUpdatedOn(new Date());
							UserDetails details = new UserDetails();
							if (parentRequest.getDepartmentId() != null) {
								DepartmentDetails listDept = departmentRepository
										.findByDepartmentId(parentRequest.getDepartmentId().getDepartmentId());
								details.setApplicationId(listDept.getApplicationId());
							}
							details.setBuID(Constant.BUID_02);
							details.setCreatedBy(profile.getEmailAddress());
							details.setLogoUrl(Constant.AVATAR_13);
							details.setUserEmail(parentRequest.getUserEmail());
							details.setDepartmentId(parentRequest.getDepartmentId());
							details.setCreatedOn(new Date());
							details.setJoiningDate(new Date());

							details.setUpdatedOn(parentRequest.getUpdatedOn());
							details.setUserDesigination(parentRequest.getUserDesignation());
							if (parentRequest.isSignUp()) {
								details.setUserRole(Constant.CONTRIBUTOR);
								details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
							}
							details.setUserId(parentRequest.getUserId());
							details.setUserStatus(Constant.ACTIVE);
							details.setUserReportingManager("self");
							details.setUserType("self");
							details.setUserName(
									parentRequest.getFirstName().concat(" ").concat(parentRequest.getLastName()));
							userDetailsRepository.save(details);
							userOnboardingDetailsRepo.save(parentRequest);
							sendVerificationEmail(parentRequest.getUserEmail(),
									parentRequest.getFirstName() + " " + parentRequest.getLastName(),
									parentRequest.getVerificationUrl());
							return reviewSuccessResponse();
						}
						parentRequest.setOnboardingStatus(Constant.APPROVED_BY_APPROVER);
						parentRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						parentRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						parentRequest.setWorkGroup(Constant.APPROVER);
						parentRequest.setComments(onboardingWorkFlowRequest.getComments());
						parentRequest.setEndDate(new Date());
						parentRequest.setUpdatedBy(profile.getFirstName());
						parentRequest.setUpdatedOn(new Date());
						parentRequest.setCreatedOn(new Date());
						UserDetails details = new UserDetails();
						details.setBuID(Constant.BUID_02);
						details.setCreatedBy(profile.getEmailAddress());
						details.setLogoUrl(parentRequest.getLogoUrl());
						details.setUserEmail(parentRequest.getUserEmail());
						details.setDepartmentId(parentRequest.getDepartmentId());
						details.setJoiningDate(parentRequest.getJoiningDate());
						details.setUpdatedOn(parentRequest.getUpdatedOn());
						details.setCreatedOn(parentRequest.getCreatedOn());
						details.setUserDesigination(parentRequest.getUserDesignation());
						details.setUserId(parentRequest.getUserId());
						details.setUserStatus(Constant.ACTIVE);
						if (parentRequest.isSignUp()) {
							details.setUserRole(Constant.CONTRIBUTOR);
							details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
							details.setMobileNumber(parentRequest.getMobileNumber());
						}
						details.setUserReportingManager(parentRequest.getUserReportingManager());
						details.setUserType(parentRequest.getUserType());
						details.setUserName(
								parentRequest.getFirstName().concat(" ").concat(parentRequest.getLastName()));
						userDetailsRepository.save(details);
						userOnboardingDetailsRepo.save(parentRequest);
					}
					return reviewSuccessResponse();

				} else {
					if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
						try {
							superAdminsaveData(requestId, childRequestId, profile, onboardingWorkFlowRequest);
						} catch (DataValidationException e) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
									HttpStatus.CONFLICT);
						}
						return reviewSuccessResponse();
					} else {
						if (requestId != null) {
							UserOnboarding rejectRequest = userOnboardingDetailsRepo.findByRequestNumber(requestId,
									Constant.APPROVER, Constant.REVIEW);
							if (rejectRequest == null) {
								throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
										HttpStatus.CONFLICT);
							}
							rejectRequest.setApprovedRejected(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequest.setOnboardingStatus(onboardingWorkFlowRequest.getComments());
							rejectRequest.setComments(rejectRequest.getComments());
							userOnboardingDetailsRepo.save(rejectRequest);
							return reviewFailureResponse();
						} else {
							UserOnboarding rejectRequest = userOnboardingDetailsRepo
									.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
							if (rejectRequest == null) {
								throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
										HttpStatus.CONFLICT);
							}
							rejectRequest.setApprovedRejected(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequest.setOnboardingStatus(onboardingWorkFlowRequest.getComments());
							rejectRequest.setComments(rejectRequest.getComments());
							userOnboardingDetailsRepo.save(rejectRequest);
							return reviewFailureResponse();
						}
					}

				}
			} else {
				if (requestId != null) {
					UserOnboarding rejectRequest = userOnboardingDetailsRepo.findByRequestNumber(requestId,
							Constant.APPROVER, Constant.REVIEW);
					if (rejectRequest == null) {
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
					}
					rejectRequest.setApprovedRejected(Constant.REJECTED);
					rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_APPROVER);
					rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
					userOnboardingDetailsRepo.save(rejectRequest);
					sendRejectedEmail(rejectRequest.getUserEmail(),
							rejectRequest.getFirstName() + " " + rejectRequest.getLastName());
					return reviewFailureResponse();

				} else {
					UserOnboarding rejectRequest = userOnboardingDetailsRepo.findByChildRequestNumber(childRequestId,
							Constant.APPROVER, Constant.REVIEW);
					if (rejectRequest == null) {
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
					}
					rejectRequest.setApprovedRejected(Constant.REJECTED);
					rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_APPROVER);
					rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
					userOnboardingDetailsRepo.save(rejectRequest);
					sendRejectedEmail(rejectRequest.getUserEmail(),
							rejectRequest.getFirstName() + " " + rejectRequest.getLastName());

					return reviewFailureResponse();
				}
			}
		}
	}

	private void superAdminsaveData(String requestId, String childRequestId, UserLoginDetails profile,
			UserOnboardingWorkFlowRequest onboardingWorkFlowRequest)
			throws DataValidationException, IOException, TemplateException, MessagingException {
		UserDetails details = new UserDetails();
		if (requestId == null && childRequestId != null) {
			UserOnboarding superAdminRequest = userOnboardingDetailsRepo.findAllBySuperAdmin(childRequestId);
			if (superAdminRequest == null) {
				throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
			}
			if (superAdminRequest.getWorkGroup().equalsIgnoreCase(Constant.REVIEWER)) {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setChildRequestNumber(childRequestId);
				superAdminRequest.setWorkGroup(Constant.SUPERADMIN);
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setUpdatedOn(new Date());
				userOnboardingDetailsRepo.save(superAdminRequest);
				details.setBuID(Constant.BUID_02);
				details.setCreatedBy(profile.getEmailAddress());
				details.setLogoUrl(superAdminRequest.getLogoUrl());
				details.setUserEmail(superAdminRequest.getUserEmail());
				details.setDepartmentId(superAdminRequest.getDepartmentId());
				details.setJoiningDate(superAdminRequest.getJoiningDate());
				details.setUpdatedOn(superAdminRequest.getUpdatedOn());
				details.setUserDesigination(superAdminRequest.getUserDesignation());
				details.setUserId(superAdminRequest.getUserId());
				if (superAdminRequest.isSignUp()) {
					details.setUserRole(Constant.CONTRIBUTOR);
					details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
					details.setMobileNumber(superAdminRequest.getMobileNumber());
				}
				details.setUserStatus(Constant.ACTIVE);
				details.setUserReportingManager(superAdminRequest.getUserReportingManager());
				details.setUserType(superAdminRequest.getUserType());
				details.setUserName(
						superAdminRequest.getFirstName().concat(" ").concat(superAdminRequest.getLastName()));
				userDetailsRepository.save(details);
			} else {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setWorkGroup(Constant.SUPERADMIN);
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setChildRequestNumber(childRequestId);
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setUpdatedOn(new Date());
				userOnboardingDetailsRepo.save(superAdminRequest);
				details.setBuID(Constant.BUID_02);
				details.setCreatedBy(profile.getEmailAddress());
				details.setLogoUrl(superAdminRequest.getLogoUrl());
				details.setUserEmail(superAdminRequest.getUserEmail());
				details.setDepartmentId(superAdminRequest.getDepartmentId());
				details.setJoiningDate(superAdminRequest.getJoiningDate());
				details.setUpdatedOn(superAdminRequest.getUpdatedOn());
				details.setUserDesigination(superAdminRequest.getUserDesignation());
				details.setUserId(superAdminRequest.getUserId());
				details.setUserStatus(Constant.ACTIVE);
				details.setUserReportingManager(superAdminRequest.getUserReportingManager());
				details.setUserType(superAdminRequest.getUserType());
				details.setUserName(
						superAdminRequest.getFirstName().concat(" ").concat(superAdminRequest.getLastName()));
				userDetailsRepository.save(details);
			}

		}
		if (requestId != null && childRequestId == null) {
			UserOnboarding superAdminRequest = userOnboardingDetailsRepo.findAllBySuperAdminRequestId(requestId);
			if (superAdminRequest == null) {
				throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
			}
			if (superAdminRequest.getWorkGroup().equalsIgnoreCase(Constant.REVIEWER)) {
				if (superAdminRequest.isSignUp()) {
					superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
					superAdminRequest.setApprovedRejected(Constant.APPROVE);
					superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
					superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
					superAdminRequest.setChildRequestNumber(childRequestId);
					superAdminRequest.setWorkGroup(Constant.SUPERADMIN);
					superAdminRequest.setEndDate(new Date());
					superAdminRequest.setUpdatedBy(profile.getFirstName());
					superAdminRequest.setUpdatedOn(new Date());
					userOnboardingDetailsRepo.save(superAdminRequest);

					details.setBuID(Constant.BUID_02);

					if (superAdminRequest.getDepartmentId() != null) {
						String string = superAdminRequest.getDepartmentId().getDepartmentId();
						DepartmentDetails listDept = departmentRepository.findByDepartmentId(string);
						List<ApplicationDetails> appList = new ArrayList<>();
						for (ApplicationDetails app : listDept.getApplicationId()) {
							ApplicationDetails applicationDetails = applicationDetailsRepository
									.findByApplicationId(app.getApplicationId());
							appList.add(applicationDetails);
						}
						details.setApplicationId(appList);
					}
					if (superAdminRequest.isSignUp()) {
						details.setUserRole(Constant.CONTRIBUTOR);
						details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
						details.setMobileNumber(superAdminRequest.getMobileNumber());
					}
					details.setCreatedBy(profile.getEmailAddress());
					details.setLogoUrl(Constant.AVATAR_13);
					details.setUserEmail(superAdminRequest.getUserEmail());
					details.setDepartmentId(superAdminRequest.getDepartmentId());
					details.setJoiningDate(new Date());
					details.setUpdatedOn(superAdminRequest.getUpdatedOn());
					details.setUserDesigination(superAdminRequest.getUserDesignation());
					details.setUserId(superAdminRequest.getUserId());
					details.setUserStatus(Constant.ACTIVE);
					details.setUserReportingManager("self");
					details.setUserType("self");
					details.setUserName(
							superAdminRequest.getFirstName().concat(" ").concat(superAdminRequest.getLastName()));
					sendVerificationEmail(superAdminRequest.getUserEmail(),
							superAdminRequest.getFirstName() + " " + superAdminRequest.getLastName(),
							superAdminRequest.getVerificationUrl());
					userDetailsRepository.save(details);

				} else {
					superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
					superAdminRequest.setApprovedRejected(Constant.APPROVE);
					superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
					superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
					superAdminRequest.setChildRequestNumber(childRequestId);
					superAdminRequest.setEndDate(new Date());
					superAdminRequest.setWorkGroup(Constant.SUPERADMIN);
					superAdminRequest.setUpdatedBy(profile.getFirstName());
					superAdminRequest.setUpdatedOn(new Date());
					userOnboardingDetailsRepo.save(superAdminRequest);
					details.setBuID(Constant.BUID);
					details.setCreatedBy(profile.getEmailAddress());
					details.setLogoUrl(superAdminRequest.getLogoUrl());
					details.setUserEmail(superAdminRequest.getUserEmail());
					details.setDepartmentId(superAdminRequest.getDepartmentId());
					details.setJoiningDate(superAdminRequest.getJoiningDate());
					details.setUpdatedOn(superAdminRequest.getUpdatedOn());
					details.setUserDesigination(superAdminRequest.getUserDesignation());
					details.setUserId(superAdminRequest.getUserId());
					details.setUserStatus(Constant.ACTIVE);
					details.setUserReportingManager(superAdminRequest.getUserReportingManager());
					details.setUserType(superAdminRequest.getUserType());
					details.setUserName(
							superAdminRequest.getFirstName().concat(" ").concat(superAdminRequest.getLastName()));
					userDetailsRepository.save(details);
				}

			} else {
				if (superAdminRequest.isSignUp()) {
					superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
					superAdminRequest.setApprovedRejected(Constant.APPROVE);
					superAdminRequest.setWorkGroup(Constant.SUPERADMIN);
					superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
					superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
					superAdminRequest.setChildRequestNumber(childRequestId);
					superAdminRequest.setEndDate(new Date());
					superAdminRequest.setSignUp(true);
					superAdminRequest.setUpdatedBy(profile.getFirstName());
					superAdminRequest.setUpdatedOn(new Date());
					userOnboardingDetailsRepo.save(superAdminRequest);
					details.setBuID(Constant.BUID_02);
					details.setCreatedBy(profile.getEmailAddress());
					details.setLogoUrl(Constant.AVATAR_13);
					details.setUserEmail(superAdminRequest.getUserEmail());
					details.setDepartmentId(superAdminRequest.getDepartmentId());
					details.setJoiningDate(new Date());
					details.setUpdatedOn(new Date());
					details.setUserDesigination(superAdminRequest.getUserDesignation());
					details.setUserId(superAdminRequest.getUserId());
					details.setUserStatus(Constant.ACTIVE);
					if (superAdminRequest.isSignUp()) {
						details.setUserRole(Constant.CONTRIBUTOR);
						details.setUserAccess(Constant.ROLE_CONTRIBUTOR);
						details.setMobileNumber(superAdminRequest.getMobileNumber());
					}
					details.setUserReportingManager("self");
					details.setUserType("self");
					details.setUserName(
							superAdminRequest.getFirstName().concat(" ").concat(superAdminRequest.getLastName()));
					sendVerificationEmail(superAdminRequest.getUserEmail(),
							superAdminRequest.getFirstName().concat(" ").concat(superAdminRequest.getLastName()),
							superAdminRequest.getVerificationUrl());
					userDetailsRepository.save(details);

				} else {
					superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
					superAdminRequest.setApprovedRejected(Constant.APPROVE);
					superAdminRequest.setWorkGroup(Constant.SUPERADMIN);
					superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
					superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
					superAdminRequest.setChildRequestNumber(childRequestId);
					superAdminRequest.setEndDate(new Date());
					superAdminRequest.setUpdatedBy(profile.getFirstName());
					superAdminRequest.setUpdatedOn(new Date());
					userOnboardingDetailsRepo.save(superAdminRequest);
					details.setBuID(Constant.BUID_02);
					details.setCreatedBy(profile.getEmailAddress());
					details.setLogoUrl(superAdminRequest.getLogoUrl());
					details.setUserEmail(superAdminRequest.getUserEmail());
					details.setDepartmentId(superAdminRequest.getDepartmentId());
					details.setJoiningDate(superAdminRequest.getJoiningDate());
					details.setUpdatedOn(superAdminRequest.getUpdatedOn());
					details.setUserDesigination(superAdminRequest.getUserDesignation());
					details.setUserId(superAdminRequest.getUserId());
					details.setUserStatus(Constant.ACTIVE);
					details.setUserReportingManager(superAdminRequest.getUserReportingManager());
					details.setUserType(superAdminRequest.getUserType());
					details.setUserName(
							superAdminRequest.getFirstName().concat(" ").concat(superAdminRequest.getLastName()));
					userDetailsRepository.save(details);
				}
			}
		}
	}

	private CommonResponse reviewSuccessResponse() {
		return new CommonResponse(HttpStatus.OK,
				new Response("onboardingWorkflowActionResponse", "Workflow approved Successfully"),
				"Workflow action completed");
	}

	private CommonResponse reviewFailureResponse() {
		return new CommonResponse(HttpStatus.OK, new Response("onboardingWorkflowActionResponse", "Workflow rejected"),
				"Workflow action completed");
	}

	@Override
	public CommonResponse userReviewerApproverDetailsView(String childRequestId, String requestId,
			UserLoginDetails profile) throws DataValidationException {
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		UserOnboardingRequestDetailViewResponse detailViewResponse = new UserOnboardingRequestDetailViewResponse();
		CreateUserDetails user = new CreateUserDetails();
		UserReviewerDetails reviewerDetails = new UserReviewerDetails();
		UserOnboarding userDetail = userOnboardingDetailsRepo.findByRequest(requestId, childRequestId);
		if ( (userDetail.getApprovedRejected().equalsIgnoreCase(Constant.APPROVE)
				|| userDetail.getApprovedRejected().equalsIgnoreCase(Constant.REJECTED))
				|| (userDetail.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW)
						&& userDetail.getWorkGroup().equalsIgnoreCase(Constant.APPROVER)
						&& (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)))) {
			throw new DataValidationException("Onboarding flow for the requested user is completed already", null,
					HttpStatus.NO_CONTENT);
		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
			if (requestId != null && childRequestId == null) {
				UserOnboarding userReviewer = userOnboardingDetailsRepo.findByRequestNumber(requestId,
						Constant.REVIEWER, Constant.REVIEW);
				if (userReviewer.getDepartmentId() != null) {
					user.setUserDepartment(userReviewer.getDepartmentId().getDepartmentId());
					user.setUserDepartmentName(userReviewer.getDepartmentId().getDepartmentName());
				}

				user.setUserDesignation(userReviewer.getUserDesignation());
				user.setUserEmailAddress(userReviewer.getUserEmail());
				user.setUserFirstName(userReviewer.getFirstName());
				user.setUserLastName(userReviewer.getLastName());
				user.setUserGender(userReviewer.getGender());
				user.setUserJoiningDate(userReviewer.getJoiningDate());
				user.setUserMobileNumber(userReviewer.getMobileNumber());
				user.setUserReportingManager(userReviewer.getUserReportingManager());
				user.setUserType(userReviewer.getUserType());
				user.setLogoUrl(userReviewer.getLogoUrl());
				detailViewResponse.setUserInfo(user);
			}
			if (requestId == null && childRequestId != null) {
				UserOnboarding userReviewer = userOnboardingDetailsRepo.findByChildRequestNumber(childRequestId,
						Constant.REVIEWER, Constant.REVIEW);

				user.setUserDepartment(userReviewer.getDepartmentId().getDepartmentId());
				user.setUserDepartmentName(userReviewer.getDepartmentId().getDepartmentName());
				user.setUserDesignation(userReviewer.getUserDesignation());
				user.setUserEmailAddress(userReviewer.getUserEmail());
				user.setUserFirstName(userReviewer.getFirstName());
				user.setUserLastName(userReviewer.getLastName());
				user.setUserGender(userReviewer.getGender());
				user.setUserJoiningDate(userReviewer.getJoiningDate());
				user.setUserMobileNumber(userReviewer.getMobileNumber());
				user.setUserReportingManager(userReviewer.getUserReportingManager());
				user.setUserType(userReviewer.getUserType());
				user.setLogoUrl(userReviewer.getLogoUrl());
				detailViewResponse.setUserInfo(user);
			}

		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER)) {
			if (requestId != null && childRequestId == null) {
				UserOnboarding userApprover = userOnboardingDetailsRepo.findByRequestNumber(requestId,
						Constant.APPROVER, Constant.REVIEW);
				UserOnboarding userReviewer = userOnboardingDetailsRepo.findByRequestNumber(requestId,
						Constant.REVIEWER, Constant.APPROVE);
				if (userApprover.getDepartmentId() != null) {
					user.setUserDepartment(userApprover.getDepartmentId().getDepartmentId());
					user.setUserDepartmentName(userReviewer.getDepartmentId().getDepartmentName());
				}

				user.setUserDesignation(userApprover.getUserDesignation());
				user.setUserEmailAddress(userApprover.getUserEmail());
				user.setUserFirstName(userApprover.getFirstName());
				user.setUserLastName(userApprover.getLastName());
				user.setUserGender(userApprover.getGender());
				user.setUserJoiningDate(userApprover.getJoiningDate());
				user.setUserMobileNumber(userApprover.getMobileNumber());
				user.setUserReportingManager(userApprover.getUserReportingManager());
				user.setUserType(userApprover.getUserType());
				user.setLogoUrl(userApprover.getLogoUrl());

				reviewerDetails.setApprovedByEmail(userReviewer.getWorkGroupUserEmail());
				reviewerDetails.setWorkGroupName(userReviewer.getWorkGroup());
				reviewerDetails.setComments(userReviewer.getComments());
				reviewerDetails.setApprovalTimeStamp(userReviewer.getEndDate());

				detailViewResponse.setUserInfo(user);
				detailViewResponse.setReviewerDetails(reviewerDetails);
			}
			if (requestId == null && childRequestId != null) {

				UserOnboarding userApprover = userOnboardingDetailsRepo.findByChildRequestNumber(childRequestId,
						Constant.APPROVER, Constant.REVIEW);
				UserOnboarding userReviewer = userOnboardingDetailsRepo.findByChildRequestNumber(childRequestId,
						Constant.REVIEWER, Constant.APPROVE);
				user.setUserDepartment(userApprover.getDepartmentId().getDepartmentId());
				user.setUserDepartmentName(userReviewer.getDepartmentId().getDepartmentName());
				user.setUserDesignation(userApprover.getUserDesignation());
				user.setUserEmailAddress(userApprover.getUserEmail());
				user.setUserFirstName(userApprover.getFirstName());
				user.setUserLastName(userApprover.getLastName());
				user.setUserGender(userApprover.getGender());
				user.setUserJoiningDate(userApprover.getJoiningDate());
				user.setUserMobileNumber(userApprover.getMobileNumber());
				user.setUserReportingManager(userApprover.getUserReportingManager());
				user.setUserType(userApprover.getUserType());
				user.setLogoUrl(userApprover.getLogoUrl());

				reviewerDetails.setApprovedByEmail(userReviewer.getWorkGroupUserEmail());
				reviewerDetails.setWorkGroupName(userReviewer.getWorkGroup());
				reviewerDetails.setComments(userReviewer.getComments());
				reviewerDetails.setApprovalTimeStamp(userReviewer.getEndDate());

				detailViewResponse.setUserInfo(user);
				detailViewResponse.setReviewerDetails(reviewerDetails);
			}
		}
		if (userDetails.getUserRole().equalsIgnoreCase("super_admin")) {
			if (requestId != null && childRequestId == null) {
				UserOnboarding userApprover = userOnboardingDetailsRepo.findByRequestNumber(requestId,
						Constant.APPROVER, Constant.REVIEW);
				UserOnboarding userReviewerApproved = userOnboardingDetailsRepo.findByRequestNumber(requestId,
						Constant.REVIEWER, Constant.APPROVE);
				UserOnboarding userReviewer = userOnboardingDetailsRepo.findByRequestNumber(requestId,
						Constant.REVIEWER, Constant.REVIEW);
				if (userApprover != null) {

					if (userApprover.getDepartmentId() != null) {
						user.setUserDepartment(userApprover.getDepartmentId().getDepartmentId());
						user.setUserDepartmentName(userApprover.getDepartmentId().getDepartmentName());
					}

					user.setUserDesignation(userApprover.getUserDesignation());
					user.setUserEmailAddress(userApprover.getUserEmail());
					user.setUserFirstName(userApprover.getFirstName());
					user.setUserLastName(userApprover.getLastName());
					user.setUserGender(userApprover.getGender());
					user.setUserJoiningDate(userApprover.getJoiningDate());
					user.setUserMobileNumber(userApprover.getMobileNumber());
					user.setUserReportingManager(userApprover.getUserReportingManager());
					user.setUserType(userApprover.getUserType());
					user.setLogoUrl(userApprover.getLogoUrl());

					reviewerDetails.setApprovedByEmail(userReviewerApproved.getWorkGroupUserEmail());
					reviewerDetails.setWorkGroupName(userReviewerApproved.getWorkGroup());
					reviewerDetails.setComments(userReviewerApproved.getComments());
					reviewerDetails.setApprovalTimeStamp(userReviewerApproved.getEndDate());

					detailViewResponse.setUserInfo(user);
					detailViewResponse.setReviewerDetails(reviewerDetails);

				} else {
					if (userReviewer.getDepartmentId() != null) {
						user.setUserDepartment(userReviewer.getDepartmentId().getDepartmentId());
						user.setUserDepartmentName(userReviewer.getDepartmentId().getDepartmentName());
					}

					user.setUserDesignation(userReviewer.getUserDesignation());
					user.setUserEmailAddress(userReviewer.getUserEmail());
					user.setUserFirstName(userReviewer.getFirstName());
					user.setUserLastName(userReviewer.getLastName());
					user.setUserGender(userReviewer.getGender());
					user.setUserJoiningDate(userReviewer.getJoiningDate());
					user.setUserMobileNumber(userReviewer.getMobileNumber());
					user.setUserReportingManager(userReviewer.getUserReportingManager());
					user.setUserType(userReviewer.getUserType());
					user.setLogoUrl(userReviewer.getLogoUrl());
					detailViewResponse.setUserInfo(user);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				}

			}
			if (requestId == null && childRequestId != null) {
				UserOnboarding userApprover = userOnboardingDetailsRepo.findByChildRequestNumber(childRequestId,
						Constant.APPROVER, Constant.REVIEW);
				UserOnboarding userReviewerApproved = userOnboardingDetailsRepo.findByChildRequestNumber(childRequestId,
						Constant.REVIEWER, Constant.APPROVE);
				UserOnboarding userReviewer = userOnboardingDetailsRepo.findByChildRequestNumber(childRequestId,
						Constant.REVIEWER, Constant.REVIEW);
				if (userApprover != null) {
					user.setUserDepartment(userApprover.getDepartmentId().getDepartmentId());
					user.setUserDepartmentName(userApprover.getDepartmentId().getDepartmentName());
					user.setUserDesignation(userApprover.getUserDesignation());
					user.setUserEmailAddress(userApprover.getUserEmail());
					user.setUserFirstName(userApprover.getFirstName());
					user.setUserLastName(userApprover.getLastName());
					user.setUserGender(userApprover.getGender());
					user.setUserJoiningDate(userApprover.getJoiningDate());
					user.setUserMobileNumber(userApprover.getMobileNumber());
					user.setUserReportingManager(userApprover.getUserReportingManager());
					user.setUserType(userApprover.getUserType());
					user.setLogoUrl(userApprover.getLogoUrl());

					reviewerDetails.setApprovedByEmail(userReviewerApproved.getWorkGroupUserEmail());
					reviewerDetails.setWorkGroupName(userReviewerApproved.getWorkGroup());
					reviewerDetails.setComments(userReviewerApproved.getComments());
					reviewerDetails.setApprovalTimeStamp(userReviewerApproved.getEndDate());

					detailViewResponse.setUserInfo(user);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				} else {
					user.setUserDepartment(userReviewer.getDepartmentId().getDepartmentId());
					user.setUserDepartmentName(userReviewer.getDepartmentId().getDepartmentName());
					user.setUserDesignation(userReviewer.getUserDesignation());
					user.setUserEmailAddress(userReviewer.getUserEmail());
					user.setUserFirstName(userReviewer.getFirstName());
					user.setUserLastName(userReviewer.getLastName());
					user.setUserGender(userReviewer.getGender());
					user.setUserJoiningDate(userReviewer.getJoiningDate());
					user.setUserMobileNumber(userReviewer.getMobileNumber());
					user.setUserReportingManager(userReviewer.getUserReportingManager());
					user.setUserType(userReviewer.getUserType());
					user.setLogoUrl(userReviewer.getLogoUrl());

					detailViewResponse.setUserInfo(user);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				}
			}
		}
		return new CommonResponse(HttpStatus.OK,
				new Response("UserOnboardingRequestDetailViewResponse", detailViewResponse),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private List<String> userExcelValidation(MultipartFile usersFile) throws IOException {
		XSSFWorkbook workbook = null;
		List<String> erros = new ArrayList<>();
		try {
			workbook = new XSSFWorkbook(usersFile.getInputStream());
		} catch (IOException e) {
			throw new IOException(e.getMessage());
		}
		XSSFSheet worksheet = workbook.getSheetAt(0);
		int index = 0;
		int cellConstant = 0;
		List<String> columnNames = new ArrayList<>();
		for (Row cellRow : worksheet) {
			if (cellRow.getPhysicalNumberOfCells() >= 1 && hasDataInRow(cellRow)) {
				if (cellRow.getRowNum() == 0) {
					cellConstant = cellRow.getPhysicalNumberOfCells();
				}
				if (cellRow.getRowNum() == 0
						&& !cellRow.getCell(0).getStringCellValue().trim().equalsIgnoreCase("First Name")) {
					erros.add("Seems Your Uploading Wrong Excel file Please Check");
				}
				for (int cell = 0; cell < cellConstant; cell++) {
					if (cellRow.getRowNum() == 0) {
						columnNames.add(cellRow.getCell(cell).getStringCellValue().trim());
					}
					
					if (cellRow.getCell(cell) == null || cellRow.getCell(cell).getStringCellValue().length() == 0) {
					
						erros.add("Null value in " + columnNames.get(cell) + " at Row " + cellRow.getRowNum());
					}
				}
				index++;
			}
		}
		if (index == 1) {
			erros.add("Please Enter the Data");
		}
		workbook.close();
		return erros;
	}

	@SuppressWarnings("resource")
	@Transactional
	@Override
	public CommonResponse saveUserOnboarding(MultipartFile usersFile, UserLoginDetails profile)
			throws IOException, ParseException, DataValidationException {
		NewApplicationOnboardingResposne applicationOnboardingResposne = new NewApplicationOnboardingResposne();
		List<String> errors = new ArrayList<>();
		Integer childNum = 1;
		String request = "REQ_USR_0";
		Integer userReqSequence = sequenceGeneratorRepository.getUserReqSequence();
		SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
		request = request.concat(userReqSequence.toString());
		XSSFWorkbook workbook = new XSSFWorkbook(usersFile.getInputStream());
		XSSFSheet worksheet = workbook.getSheetAt(0);
		List<UserOnboarding> list = new ArrayList<>();
		int index = 0;
		List<String> userEmails = new ArrayList<>();
		List<String> mobileNumbers = new ArrayList<>();
		errors.addAll(userExcelValidation(usersFile));
		for (Row cellRow : worksheet) {
			if (cellRow.getPhysicalNumberOfCells() > 1 && hasDataInRow(cellRow)) {
				userEmails.add(cellRow.getCell(2).getStringCellValue().trim());
				mobileNumbers.add(cellRow.getCell(9).getStringCellValue().trim());
			}
		}
		Set<String> set = new HashSet<>(userEmails);
		Set<String> numberSet = new HashSet<>(mobileNumbers);
		List<String> newlist = new ArrayList<>(set);
		List<String> newNumber = new ArrayList<>(numberSet);
		if (userEmails.size() != newlist.size()) {
			errors.add("Duplicate Users Emails Found in Bulk Upload");
		}
		if (mobileNumbers.size() != newNumber.size()) {
			errors.add("Duplicate mobile numbers found in Bulk Upload");
		}
		if (!errors.isEmpty()) {
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response("Excels Upload Users", errors),
					"Users Excel Upload Failed");
		}
		for (Row cellRow : worksheet) {
			if ((cellRow.getPhysicalNumberOfCells() > 1 && hasDataInRow(cellRow)) && (index > 0)) {
				String childRequestNum = null;
				childRequestNum = request.concat("_0" + childNum);
				childNum++;
				Integer deptseq = sequenceGeneratorRepository.getUserOnboardingSequence();
				SequenceGenerator upate = sequenceGeneratorRepository.getById(1);
				ExcelUserOnboardingUploadDTO onboarding = new ExcelUserOnboardingUploadDTO();
				UserOnboarding userOnboarding = new UserOnboarding();
				upate.setApplicationDetails(++deptseq);
				XSSFRow row = worksheet.getRow(index);
				Integer i = 0;
				UserOnboarding lists = userOnboardingDetailsRepo
						.findByEmailAddresss(row.getCell(2).getStringCellValue().trim());
				List<UserOnboarding> apps = new ArrayList<>();
				UserDetails deleteChecking = null;
				if (lists != null) {
					List<UserOnboarding> loop = new ArrayList<>();
					loop.add(lists);
					List<UserOnboarding> userOnboardings = getUserStatus(loop);
					apps = userOnboardings.stream()
							.filter(p -> p.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW))
							.collect(Collectors.toList());
					deleteChecking = userDetailsRepository
							.getDeletedUserByUserEmail(row.getCell(2).getStringCellValue().trim());
				}
				if (apps.isEmpty() && deleteChecking == null) {
					onboarding.setFirstName(row.getCell(0).getStringCellValue().trim());
					onboarding.setLastName(row.getCell(1).getStringCellValue().trim());
					onboarding.setEmailAddress(row.getCell(2).getStringCellValue().trim());
					if (departmentRepository.findByDepartmentName(row.getCell(3).getStringCellValue().trim()) == null) {
						errors.add("Department name with " + row.getCell(3).getStringCellValue().trim()
								+ "Not Found For user " + row.getCell(2).getStringCellValue().trim());
					}
					String countryCode = "+" + row.getCell(10).getStringCellValue().trim();
					String mobileNumber = countryCode + " " + row.getCell(9).getStringCellValue().trim();
					if (userDetailsRepository.findByUserMobileNumber(mobileNumber) != null) {
						errors.add("User Mobile Number Already Registered at Row " + row.getRowNum());
					}
					if (userOnboardingDetailsRepo.findByUserMobileNumber(mobileNumber) != null) {
						errors.add("User Mobile Number Already Registered at Row " + row.getRowNum());
					}
					if (Constant.COUNTRY_CODE.stream().noneMatch(countryCode::equals)) {
						errors.add("Invalid country code at Row " + row.getRowNum());
					}
					onboarding.setDepartment(row.getCell(3).getStringCellValue().trim());
					onboarding.setReportingManager(row.getCell(4).getStringCellValue().trim());
					String joiningDate = row.getCell(5).getStringCellValue().trim();
					String validatedDate = DateParser.validateAndConvertToDDMMYYYY(joiningDate);
					if (validatedDate.contains("Invalid") || validatedDate.contains("Unsupported")) {
						errors.add(validatedDate + " at Row " + row.getRowNum());
					}
					Date joiningDate1 = DateParser.parse(validatedDate);
					onboarding.setJoiningDate(joiningDate1);
					onboarding.setTypeOfEmployment(row.getCell(6).getStringCellValue().trim());
					onboarding.setDesignation(row.getCell(7).getStringCellValue().trim());
					onboarding.setGender(row.getCell(8).getStringCellValue().trim());
					onboarding.setContactNumber(mobileNumber);
					userOnboarding.setFirstName(onboarding.getFirstName());
					userOnboarding.setLastName(onboarding.getLastName());
					userOnboarding.setUserEmail(onboarding.getEmailAddress());
					DepartmentDetails departmentDetails = departmentRepository
							.findByDepartmentName(onboarding.getDepartment());
					userOnboarding.setDepartmentId(departmentDetails);
					userOnboarding.setUserReportingManager(onboarding.getReportingManager());
					userOnboarding.setUserType(onboarding.getTypeOfEmployment());
					userOnboarding.setUserDesignation(onboarding.getDesignation());
					userOnboarding.setGender(onboarding.getGender());
					userOnboarding.setJoiningDate(joiningDate1);
					userOnboarding.setOnboardDate(new Date());
					userOnboarding.setMobileNumber(onboarding.getContactNumber());
					if (row.getCell(8).getStringCellValue().equalsIgnoreCase("Male")) {
						userOnboarding.setLogoUrl(Constant.AVATAR_13);
					}
					if (row.getCell(8).getStringCellValue().equalsIgnoreCase("Female")) {
						userOnboarding.setLogoUrl(onboardingUrl);
					}
					if (row.getCell(8).getStringCellValue().equalsIgnoreCase("Prefer not to disclose")) {
						userOnboarding.setLogoUrl(Constant.AVATAR_13);
					}
					userOnboarding.setApprovedRejected(Constant.REVIEW);
					userOnboarding.setCreatedOn(new Date());
					userOnboarding.setBuID(Constant.BUID);
					userOnboarding.setOnboardedByUserEmail(profile.getEmailAddress());
					userOnboarding.setOpID(Constant.SAASPE);
					userOnboarding.setWorkGroup(Constant.REVIEWER);
					userOnboarding.setWorkGroupUserEmail(profile.getEmailAddress());
					userOnboarding.setOnboardingStatus("Pending With Reviewer");
					userOnboarding.setRequestNumber(request);
					if (worksheet.getPhysicalNumberOfRows() != 1) {
						userOnboarding.setChildRequestNumber(childRequestNum);
					}
					list.add(userOnboarding);
					i = i + 1;
					
					    Users user = new Users();
		                user.setUserEmail(onboarding.getEmailAddress());
		                user.setOpID(Constant.SAASPE);
		                user.setBuID(Constant.BUID);
		                user.setUserCreatedBy(profile.getEmailAddress());
		                user.setUserCreatedOn(new Date());
		                user.setJoiningDate(joiningDate1);
		                user.setLogoUrl(userOnboarding.getLogoUrl());
		                user.setStartDate(new Date());
		                user.setTeam(Constant.REVIEWER);
		                user.setUserDesignation(onboarding.getDesignation());
		                user.setUserId(userOnboarding.getUserId());
		                user.setUserName(onboarding.getFirstName() + " " + onboarding.getLastName());
		                user.setUserReportingManager(onboarding.getReportingManager());
		                user.setUserType(onboarding.getTypeOfEmployment());
		                user.setDepartmentId(departmentDetails.getDepartmentId());
		                user.setDepartmentName(departmentDetails.getDepartmentName());
		                user.setMobileNumber(onboarding.getContactNumber());
		                usersRepository.save(user);
				}
				if (i == 0) {
					errors.add("Users with Email " + row.getCell(2).getStringCellValue() + " Already Exists");
				}
			}

			if (worksheet.getPhysicalNumberOfRows() - 1 == index && errors.isEmpty()) {
				for (UserOnboarding userOnboarding1 : list) {
					String name1 = "USER_0";
					Integer sequence1 = sequenceGeneratorRepository.getUserOnboardingSequence();
					name1 = name1.concat(sequence1.toString());
					SequenceGenerator updateSequence1 = sequenceGeneratorRepository.getById(1);
					updateSequence.setUserOnboarding(++sequence1);
					userOnboarding1.setUserId(name1);
					sequenceGeneratorRepository.save(updateSequence1);
					userOnboardingDetailsRepo.save(userOnboarding1);
				}
				updateSequence.setUserRequestId(++userReqSequence);
				sequenceGeneratorRepository.save(updateSequence);
			}
			index++;
		}
		if (errors.isEmpty()) {
			applicationOnboardingResposne.setRequestId(request);
			return new CommonResponse(HttpStatus.CREATED,
					new Response("Excel Uploaded Users", applicationOnboardingResposne), "Users Excel Upload Success");
		}
		return new CommonResponse(HttpStatus.BAD_REQUEST, new Response("Excel Uploads Users", errors),
				"Users Excel Upload Failed");
	}

	private boolean hasDataInRow(Row row) {
		DataFormatter formatter = new DataFormatter(Locale.US);
		for (int cell = 0; cell < row.getLastCellNum(); cell++) {
			if (formatter.formatCellValue(row.getCell(cell)) != null
					&& !formatter.formatCellValue(row.getCell(cell)).trim().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private void sendRejectedEmail(String emailAddress, String userName)
			throws IOException, TemplateException, MessagingException {
		String toAddress = emailAddress;
		String subject = Constant.USER_SIGNUP_EMAIL_SUBJECT;
		Map<String, Object> model = new HashMap<>();
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		Template t = config.getTemplate("signup-reject.html");
		String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
		content = content.replace("{{name}}", userName);
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

	private void sendVerificationEmail(String emailAddress, String userName, String redirectUrl)
			throws IOException, TemplateException, MessagingException {
		int verificationCode = CommonUtil.getRandomNumber(100000, 999999);
		String toAddress = emailAddress;
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String url = "{{host}}?verificationToken={{verificationCode}}&emailAddress={{emailAddress}}";
		url = url.replace("{{host}}", redirectUrl);
		url = url.replace("{{verificationCode}}", String.valueOf(verificationCode));
		String emailString = URLEncoder.encode(emailAddress, "UTF-8");
		url = url.replace("{{emailAddress}}", emailString);
		String subject = Constant.USER_VERIFY_SUBJECT;
		Map<String, Object> model = new HashMap<>();
		model.put("name", userName);
		Template t = config.getTemplate("signup-verification-v1.html");
		String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
		content = content.replace("{{verifyURL}}", url);
		content = content.replace("{{code}}", String.valueOf(verificationCode));
		content = content.replace("{{name}}", userName);
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
		VerificationDetails verificationDetails;
		Date current = new Date();
		verificationDetails = verificationDetailsRepository.findByUserEmail(emailAddress);
		Date emailVerificationExpiredate = DateUtils.addMinutes(Calendar.getInstance().getTime(),
				Constant.EMAIL_VERIFICATION_CODE_EXPIRE_DATE);
		verificationDetails.setEmailVerificationCode(String.valueOf(verificationCode));
		verificationDetails.setEmailVerificationCodeExpiryDate(emailVerificationExpiredate);
		verificationDetails.setEmailVerificationCodeSendDate(current);
		verificationDetailsRepository.save(verificationDetails);

	}

	private List<UserOnboarding> getUserStatus(List<UserOnboarding> onboardings) {
		List<UserOnboarding> list = new ArrayList<>();
		List<UserOnboarding> duplicateList = new ArrayList<>();
		for (UserOnboarding details : onboardings) {
			UserOnboarding viewResponse = new UserOnboarding();
			UserOnboarding step1childReq;
			UserOnboarding step1req;
			UserOnboarding step2ReqReject;
			UserOnboarding step2ChildReqReject;
			UserOnboarding step3ReqSuperApprove;
			UserOnboarding step3ChildReqSuperApprove;

			if (details.getChildRequestNumber() != null) {
				step1childReq = userOnboardingDetailsRepo.findAllBySuperAdmin(details.getChildRequestNumber());
				if (step1childReq != null) {
					viewResponse = step1childReq;
				} else if (step1childReq == null) {
					step2ChildReqReject = userOnboardingDetailsRepo
							.findAllByChildReqReject(details.getChildRequestNumber());
					if (step2ChildReqReject != null) {
						viewResponse = step2ChildReqReject;
					} else if (step1childReq == null && step2ChildReqReject == null) {
						step3ReqSuperApprove = userOnboardingDetailsRepo
								.findChildReqSuperApprovee(details.getChildRequestNumber());
						viewResponse = step3ReqSuperApprove;
					}
				}
			}
			if (details.getRequestNumber() != null && details.getChildRequestNumber() == null) {
				step1req = userOnboardingDetailsRepo.requestTrackingStepOneReq(details.getRequestNumber());
				if (step1req != null) {
					viewResponse = step1req;
				} else if (step1req == null) {
					step2ReqReject = userOnboardingDetailsRepo.requestTrackingStepTwoReq(details.getRequestNumber());
					if (step2ReqReject != null) {
						viewResponse = step2ReqReject;
					} else if (step1req == null && step2ReqReject == null) {
						step3ChildReqSuperApprove = userOnboardingDetailsRepo
								.requestTrackingStepThreeReq(details.getRequestNumber());
						if (step3ChildReqSuperApprove != null) {
							viewResponse = step3ChildReqSuperApprove;
						}
					}
				}
			}
			list.add(viewResponse);
		}
		for (UserOnboarding requestTracking : list) {
			if (!duplicateList.contains(requestTracking)) {
				duplicateList.add(requestTracking);
			}
		}
		return duplicateList;
	}

}