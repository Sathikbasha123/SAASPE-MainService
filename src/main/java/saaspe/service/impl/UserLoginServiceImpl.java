package saaspe.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import saaspe.constant.Constant;
import saaspe.docusign.model.DocusignUrls;
import saaspe.entity.RefreshToken;
import saaspe.entity.SequenceGenerator;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.entity.UserOnboarding;
import saaspe.entity.Users;
import saaspe.entity.VerificationDetails;
import saaspe.exception.AuthenticationException;
import saaspe.exception.DataValidationException;
import saaspe.exception.UserDetailsNotFoundException;
import saaspe.model.ChangePasswordRequest;
import saaspe.model.Clm;
import saaspe.model.CommonResponse;
import saaspe.model.DocusignUserCache;
import saaspe.model.LoginRequest;
import saaspe.model.RefreshTokenRequest;
import saaspe.model.RefreshTokenResponse;
import saaspe.model.ResetPasswordRequest;
import saaspe.model.Response;
import saaspe.model.SignupRequest;
import saaspe.model.UserAccessRoleResponse;
import saaspe.model.UserProfileResponse;
import saaspe.model.UserSignUpResponse;
import saaspe.model.VerificationRequest;
import saaspe.repository.RefreshTokenRepository;
import saaspe.repository.SequenceGeneratorRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.repository.UserLoginDetailsRepository;
import saaspe.repository.UserOnboardingDetailsRepository;
import saaspe.repository.UsersRepository;
import saaspe.repository.VerificationDetailsRepository;
import saaspe.service.UserLoginService;
import saaspe.utils.CommonUtil;
import saaspe.utils.EncryptionHelper;
import saaspe.utils.RedisUtility;
import saaspe.utils.SecureUtils;

@Service
public class UserLoginServiceImpl implements UserLoginService {

	@Autowired
	private RedisUtility redisUtility;

	@Autowired
	private UserLoginDetailsRepository userLoginDetailsRepository;

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	private VerificationDetailsRepository verificationDetailsRepository;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private Configuration config;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private UserOnboardingDetailsRepository userOnboardingDetailsRepository;

	@Autowired
	private SequenceGeneratorRepository sequenceGeneratorRepository;

	@Autowired
	private UsersRepository usersRepository;
	
	@Value("${app.encryption.key}")
	private String encryptionKey;

	@Value("${app.jwt.key}")
	private String jwtKey;

	@Value("${app.jwt.issuer}")
	private String jwtIssuer;

	@Value("${app.jwt.expiration.min}")
	private int jwtExpirationInMin;

	@Value("${spring.mail.username}")
	private String fromMail;

	@Value("${sendgrid.domain.name}")
	private String mailDomainName;

	@Value("${sendgrid.domain.sendername}")
	private String senderName;

	@Value("${spring.login.verify}")
	private String loginVerify;

	@Value("${spring.media.host}")
	private String mediaHost;

	@Value("${spring.image.key}")
	private String imageKey;

	@Value("${sendgrid.domain.support}")
	private String supportEmail;

	@Value("${logos.avatar.url}")
	private String avatarUrl;

	@Value("${userlogin.consent.url}")
	private String consentUrl;

	@Value("${userlogin.user.url}")
	private String userUrl;

	@Value("${docusign-urls-file}")
	private String docusignUrls;

	@Value("${docusign.host.url}")
	private String docusignHost;
	
	@Value("${docusign.prefix}")
	private String redisPrefix;

	private Random random = new Random();

	private static final Logger log = LoggerFactory.getLogger(UserLoginServiceImpl.class);

	public DocusignUrls getDousignUrl() {
		ClassPathResource resource = new ClassPathResource(docusignUrls);
		ObjectMapper objectMapper = new ObjectMapper();
		DocusignUrls docusignUrl = null;
		try {
			docusignUrl = objectMapper.readValue(resource.getInputStream(), DocusignUrls.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return docusignUrl;
	}

	@Override
	@Transactional
	public CommonResponse createUserProfile(SignupRequest signupRequest) throws DataValidationException {
		
		if (Constant.DESIGNATIONFORSIGNUPUSER.stream().noneMatch(signupRequest.getDesignation()::equalsIgnoreCase)) {
			throw new DataValidationException("Designation Not Match", "400", HttpStatus.BAD_REQUEST);
		}
		
		try {
			UserDetails userdetails = userDetailsRepository.findByuserEmail(signupRequest.getEmailAddress());
			if (userOnboardingDetailsRepository.existByEmailAddress(signupRequest.getEmailAddress())) {
				throw new DataValidationException(Constant.USER_DETAILS_ALL_READY_REGISTERED, "400",
						HttpStatus.BAD_REQUEST);
			}
			if (userDetailsRepository.findByuserEmail(signupRequest.getEmailAddress()) != null) {
				throw new DataValidationException(Constant.USER_DETAILS_ALL_READY_REGISTERED, "400",
						HttpStatus.BAD_REQUEST);
			}
			if (userDetailsRepository.findByUserMobileNumber(signupRequest.getUserMobileNumber()) != null) {
				throw new DataValidationException("User Mobile Number Already Registered", "400",
						HttpStatus.BAD_REQUEST);
			}

			List<UserLoginDetails> userProfileList = userLoginDetailsRepository
					.getUserProfile(signupRequest.getEmailAddress());
			if (!userProfileList.isEmpty() && userdetails != null) {
				throw new DataValidationException(Constant.USER_DETAILS_ALL_READY_REGISTERED, "400",
						HttpStatus.BAD_REQUEST);
			}
			boolean status = isValid(signupRequest.getPassword());
			if (status) {
				saveUserProfile(signupRequest);
			} else {
				throw new DataValidationException("password is week", "400", HttpStatus.BAD_REQUEST);
			}
			UserSignUpResponse response = new UserSignUpResponse();
			response.setMessage("Waiting For Reviewer to Approve");
			response.setSuccess(true);
			return new CommonResponse(HttpStatus.OK, new Response(Constant.USER_PROFILE, response),
					"Data updated successfully");
		} catch (DataValidationException e) {
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(Constant.USER_PROFILE, null),
					e.getMessage());
		}
	}

	public String saveUserProfile(SignupRequest signupRequest) {
		try {
			String name = "USER_0";
			Integer sequence = sequenceGeneratorRepository.getUserOnboardingSequence();
			name = name.concat(sequence.toString());
			SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
			updateSequence.setUserOnboarding(++sequence);
			sequenceGeneratorRepository.save(updateSequence);

			String request = "REQ_USR_0";
			Integer sequence1 = sequenceGeneratorRepository.getUserReqSequence();
			request = request.concat(sequence1.toString());
			SequenceGenerator updateSequence1 = sequenceGeneratorRepository.getById(1);
			updateSequence.setUserRequestId(++sequence1);
			sequenceGeneratorRepository.save(updateSequence1);

			String salt = SecureUtils.getSalt();
			String password = CommonUtil.createHash(signupRequest.getPassword(), salt);
			UUID uuid = UUID.randomUUID();
			VerificationDetails verificationDetails = new VerificationDetails();
			UserLoginDetails userProfiles = new UserLoginDetails();
			UserOnboarding userOnboarding = new UserOnboarding();
            Users users = new Users();
            
			userProfiles.setPassword(password);
			userProfiles.setEmailAddress(signupRequest.getEmailAddress());
			userProfiles.setFirstName(signupRequest.getFirstName());
			userProfiles.setLastName(signupRequest.getLastName());
			userProfiles.setDesignation(signupRequest.getDesignation());
			verificationDetails.setSalt(salt);
			verificationDetails.setUserEmail(signupRequest.getEmailAddress());
			verificationDetails.setIsMfaEnabled(true);

			verificationDetails.setEmailVerified(false);
			verificationDetails.setOpID("SAASPE");
			verificationDetails.setCreatedOn(new Date());

			userOnboarding.setFirstName(signupRequest.getFirstName());
			userOnboarding.setUserId(name);
			userOnboarding.setPassWord(password);

			userOnboarding.setRequestNumber(request);
			userOnboarding.setLastName(signupRequest.getLastName());
			userOnboarding.setUserEmail(signupRequest.getEmailAddress());
			userOnboarding.setUserDesignation(signupRequest.getDesignation());
			userOnboarding.setOpID("SAASPE");
			userOnboarding.setOnboardedByUserEmail("self");
			userOnboarding.setVerificationUrl(signupRequest.getVerifyUrl());
			userOnboarding.setSignUp(true);
			userOnboarding.setLogoUrl(avatarUrl);
			userOnboarding.setApprovedRejected("Review");
			userOnboarding.setWorkGroup("Reviewer");
			userOnboarding.setOnboardingStatus("pending with reviewer");
			userOnboarding.setBuID("BUID_".concat(String.valueOf(CommonUtil.getRandomNumber(1, 99))));
			userOnboarding.setCreatedOn(new Date());
			userOnboarding.setMobileNumber(signupRequest.getUserMobileNumber());
			
			users.setAppUser(false);
			users.setBuID(Constant.BUID);
			users.setUserEmail(signupRequest.getEmailAddress());
			users.setOpID("SAASPE");
			users.setUserDesignation(signupRequest.getDesignation());
			users.setUserCreatedOn(new Date());
            users.setUserId(name);
            users.setMobileNumber(signupRequest.getUserMobileNumber());
            users.setLogoUrl(avatarUrl);
            users.setUserName(signupRequest.getFirstName()+ " " +signupRequest.getLastName());
            usersRepository.save(users);
            
			userOnboardingDetailsRepository.save(userOnboarding);
			verificationDetailsRepository.save(verificationDetails);
			userLoginDetailsRepository.save(userProfiles);
			return uuid.toString();
		} catch (Exception e) {
			log.error("There are some error in saveUserProfile ", e);
		}
		return null;
	}

	private void sendVerificationEmail(String emailAddress, int verificationCode, String userName, String redirectUrl)
			throws IOException, TemplateException, MessagingException {
		String toAddress = emailAddress;
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String url = "{{host}}?verificationToken={{verificationCode}}&emailAddress={{emailAddress}}";
		url = url.replace("{{host}}", redirectUrl);
		url = url.replace("{{verificationCode}}", String.valueOf(verificationCode));
		String emailString = URLEncoder.encode(emailAddress, Constant.UTF);
		url = url.replace("{{emailAddress}}", emailString);
		String subject = Constant.USER_VERIFY_SUBJECT;
		Map<String, Object> model = new HashMap<>();
		model.put("name", userName);
		Template t = config.getTemplate("signup-verification-v1.html");
		String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
		content = content.replace("{{verifyURL}}", url);
		content = content.replace("{{code}}", String.valueOf(verificationCode));
		content = content.replace(Constant.NAME, userName);
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

	private void sendResetInitiateEmail(String emailAddress, int verificationCode, String redirectUrl)
			throws IOException, TemplateException, MessagingException {
		String toAddress = emailAddress;
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String url = "{{host}}?emailAddress={{emailAddress}}&verificationToken={{verificationCode}}";
		url = url.replace("{{host}}", redirectUrl);
		url = url.replace("{{verificationCode}}", String.valueOf(verificationCode));
		String emailString = URLEncoder.encode(emailAddress, Constant.UTF);
		url = url.replace("{{emailAddress}}", emailString);
		String subject = Constant.USER_PWDRESET_EMAIL_SUBJECT;
		Map<String, Object> model = new HashMap<>();
		Template t = config.getTemplate("reset-password.html");
		String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
		content = content.replace("{{url}}", url);
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
	public CommonResponse login(LoginRequest request)
			throws AuthenticationException, DataValidationException, UnsupportedEncodingException, MessagingException {
		RefreshToken rToken = new RefreshToken();
		if (StringUtils.isBlank(request.getEmailAddress()) || StringUtils.isBlank(request.getPassword())) {
			throw new DataValidationException(Constant.USER_NAME_OR_EMAIL_REQUIRED_ERROR_MESSAGE, "400",
					HttpStatus.BAD_REQUEST);
		}
		String email = request.getEmailAddress() != null ? request.getEmailAddress().toLowerCase()
				: request.getEmailAddress();
		UserLoginDetails userProfile = userLoginDetailsRepository.findByUserEmail(email);
		if (userProfile == null) {
			throw new DataValidationException(Constant.USER_NOT_FOUND, "403", HttpStatus.FORBIDDEN);
		}
		VerificationDetails verificationDetails = verificationDetailsRepository.findByUserEmail(email);
		if (verificationDetails.getFailedCount() != null && verificationDetails.getFailedCount() == 3) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(verificationDetails.getUpdatedOn());
			cal.add(Calendar.MINUTE, 10);
			if (new Date().before(cal.getTime())) {
				throw new DataValidationException("Exceeded maximum attempts(3), Please try again after 10 mins", "400",
						HttpStatus.BAD_REQUEST);
			} else if (new Date().after(cal.getTime())) {
				verificationDetails.setFailedCount(0);
				verificationDetails.setUpdatedOn(null);
				verificationDetailsRepository.save(verificationDetails);
			}
		}
		if (Boolean.FALSE.equals(verificationDetails.getEmailVerified())) {
			throw new AuthenticationException("Email Not Verified Yet", email, HttpStatus.FORBIDDEN);
		}
		String passwordHash = CommonUtil.createHash(request.getPassword(), verificationDetails.getSalt());
		if (!userProfile.getPassword().equalsIgnoreCase(passwordHash)) {
			if (verificationDetails != null && verificationDetails.getFailedCount() == 2) {
				verificationDetails.setUpdatedOn(new Date());
			}
			verificationDetails.setFailedCount(
					verificationDetails.getFailedCount() != null ? verificationDetails.getFailedCount() + 1 : 1);
			verificationDetailsRepository.save(verificationDetails);
			throw new DataValidationException("Invalid username or password", null, HttpStatus.FORBIDDEN);
		}
		verificationDetails.setIsMfaEnabled(true);
		verificationDetailsRepository.save(verificationDetails);

		if (Boolean.TRUE.equals(verificationDetails.getIsMfaEnabled())) {
			String oTP = generateOtp();
			if ((loginVerify.compareTo("true") == 0)) {
				sendAlertMail(email, oTP,
						userProfile.getLastName() != null ? userProfile.getFirstName() + " " + userProfile.getLastName()
								: userProfile.getFirstName());
			}
			verificationDetails.setLoginOTP(oTP);
			verificationDetails.setLoginOtpGeneratedTime(new Timestamp(System.currentTimeMillis()));
			verificationDetails.setLoginOtpExpiryTime(new Timestamp(System.currentTimeMillis() + 60000));
			verificationDetails.setFailedCount(0);
			verificationDetails.setUpdatedOn(null);
			verificationDetailsRepository.save(verificationDetails);

			Map<String, Object> data = new HashMap<>();
			data.put("mfaEnabled", verificationDetails.getIsMfaEnabled());
			return new CommonResponse(HttpStatus.OK, new Response(null, data),
					"Login OTP Sent to " + request.getEmailAddress());
		}

		UserDetails user = userDetailsRepository.findByuserEmail(request.getEmailAddress());
		String token = buildToken(userProfile);
		UserProfileResponse response = new UserProfileResponse();
		response.setToken(token);
		DecodedJWT jwt = JWT.require(Algorithm.HMAC256(EncryptionHelper.decrypt(encryptionKey, jwtKey).getBytes()))
				.build().verify(token);
		String refreshToken = UUID.randomUUID().toString();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, 60);
		response.setAccessTokenExpiry(jwt.getExpiresAt());
		response.setRefreshToken(refreshToken);
		response.setRefreshTokenExpiry(calendar.getTime());
		verificationDetails.setFailedCount(0);
		verificationDetails.setUpdatedOn(null);
		verificationDetails.setRefreshToken(refreshToken);
		verificationDetails.setRefreshTokenExpiry(calendar.getTime());
		rToken.setAccessToken(token);
		rToken.setAccessTokenExpiry(jwt.getExpiresAt());
		rToken.setRefreshToken(refreshToken);
		rToken.setRefreshTokenExpiry(calendar.getTime());
		rToken.setUserEmail(email);
		rToken.setCreatedOn(new Date());
		refreshTokenRepository.save(rToken);
		verificationDetailsRepository.save(verificationDetails);
		user.setSaaspeLastLogin(new Date());
		userDetailsRepository.save(user);
		return new CommonResponse(HttpStatus.OK, new Response(Constant.USER_PROFILE, response),
				Constant.DATA_RETRIEVED_SUCCESSFULLY);
	}

	private void sendAlertMail(String email, String OTP, String userName)
			throws MessagingException, UnsupportedEncodingException {
		Map<String, Object> model = new HashMap<>();
		model.put("OTP", OTP);
		MimeMessage message = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			String subject = "Verify OTP";
			Template t = null;
			t = config.getTemplate("verify-otp.html");
			String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
			content = content.replace("{{name}}", userName);
			content = content.replace("{{code}}", OTP);
			helper.setTo(email);
			helper.setText(content, true);
			helper.setFrom(mailDomainName, senderName);
			helper.setSubject(subject);
			mailSender.send(message);

		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedEncodingException(e.getMessage());
		} catch (MessagingException | IOException | TemplateException ex) {
			throw new MessagingException(ex.getMessage());
		}

	}

	private String generateOtp() {
		Random random = new Random();
		int randomNumber = random.nextInt(999999);
		String output = Integer.toString(randomNumber);

		while (output.length() < 6) {
			output = "0" + output;
		}
		return output;
	}

	private String buildToken(UserLoginDetails userProfile) {
		String access = null;
		UserDetails user = userDetailsRepository.findByuserEmail(userProfile.getEmailAddress());
		if (user.getUserRole().equalsIgnoreCase("approver")) {
			access = Constant.ROLE_APPROVER;
		} else if (user.getUserRole().equalsIgnoreCase("reviewer")) {
			access = Constant.ROLE_REVIEWER;
		} else if (user.getUserRole().equalsIgnoreCase("super_admin")) {
			access = Constant.ROLE_SUPER_ADMIN;
		} else if (user.getUserRole().equalsIgnoreCase("contributor")) {
			access = Constant.ROLE_CONTRIBUTOR;
		} else if (user.getUserRole().equalsIgnoreCase("support")) {
			access = Constant.ROLE_SUPPORT;
		} else if (user.getUserRole().equalsIgnoreCase("custom")) {
			access = user.getUserAccess();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, jwtExpirationInMin);
		String key = EncryptionHelper.decrypt(encryptionKey, jwtKey);
		return JWT.create().withSubject(userProfile.getEmailAddress()).withExpiresAt(calendar.getTime())
				.withIssuer(jwtIssuer).withClaim("sub", userProfile.getEmailAddress()).withClaim("scopes", access)
				.withClaim(Constant.EMAIL, userProfile.getEmailAddress()).withClaim("username", user.getUserName())
				.withClaim("role", user.getUserRole()).sign(Algorithm.HMAC256(key));
	}

	@Override
	public CommonResponse verifyOTP(@Valid VerificationRequest request) throws DataValidationException {

		VerificationDetails verificationdetails = verificationDetailsRepository.findByUserEmail(request.getEmail());
		if (request.getCode() == null)
			throw new UserDetailsNotFoundException("Code cannot be null", HttpStatus.BAD_REQUEST.toString());
		if (verificationdetails == null)
			throw new UserDetailsNotFoundException("User not found with provided email",
					HttpStatus.BAD_REQUEST.toString());
		if (verificationdetails.getFailedCount() != null && verificationdetails.getFailedCount() == 3) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(verificationdetails.getUpdatedOn());
			cal.add(Calendar.MINUTE, 10);
			if (new Date().before(cal.getTime())) {
				throw new DataValidationException("Exceeded maximum attempts(3), Please try again after 10 mins", "400",
						HttpStatus.BAD_REQUEST);
			} else if (new Date().after(cal.getTime())) {
				verificationdetails.setFailedCount(0);
				verificationdetails.setUpdatedOn(null);
				verificationDetailsRepository.save(verificationdetails);
			}
		}
		if ((loginVerify.compareTo("false") == 0)) {
			if (!request.getCode().matches("\\d{6}")) {
				if (verificationdetails != null && verificationdetails.getFailedCount() == 2) {
					verificationdetails.setUpdatedOn(new Date());
				}
				verificationdetails.setFailedCount(
						verificationdetails.getFailedCount() != null ? verificationdetails.getFailedCount() + 1 : 1);
				verificationDetailsRepository.save(verificationdetails);
				throw new BadCredentialsException("OTP Invalid or less than 6 digits");
			}
		} else if (!(verificationdetails.getLoginOTP().equals(request.getCode()))
				|| !new Timestamp(System.currentTimeMillis()).before(verificationdetails.getLoginOtpExpiryTime())) {
			if (verificationdetails != null && verificationdetails.getFailedCount() == 2) {
				verificationdetails.setUpdatedOn(new Date());
			}
			verificationdetails.setFailedCount(
					verificationdetails.getFailedCount() != null ? verificationdetails.getFailedCount() + 1 : 1);
			verificationDetailsRepository.save(verificationdetails);
			throw new BadCredentialsException("OTP Invalid Please check");
		}

		RefreshToken rToken = new RefreshToken();
		UserLoginDetails userProfile = userLoginDetailsRepository.findByUserEmail(request.getEmail());
		UserDetails user = userDetailsRepository.findByuserEmail(request.getEmail());
		String token = buildToken(userProfile);
		UserProfileResponse response = new UserProfileResponse();
		response.setToken(token);
		DecodedJWT jwt = JWT.require(Algorithm.HMAC256(EncryptionHelper.decrypt(encryptionKey, jwtKey).getBytes()))
				.build().verify(token);
		String refreshToken = UUID.randomUUID().toString();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, 60);
		response.setAccessTokenExpiry(jwt.getExpiresAt());
		response.setRefreshToken(refreshToken);
		response.setRefreshTokenExpiry(calendar.getTime());
		response.setMfaEnabled(verificationdetails.getIsMfaEnabled());
		verificationdetails.setRefreshToken(refreshToken);
		verificationdetails.setRefreshTokenExpiry(calendar.getTime());
		verificationdetails.setFailedCount(0);
		verificationdetails.setUpdatedOn(null);
		rToken.setAccessToken(token);
		rToken.setAccessTokenExpiry(jwt.getExpiresAt());
		rToken.setRefreshToken(refreshToken);
		rToken.setRefreshTokenExpiry(calendar.getTime());
		rToken.setUserEmail(request.getEmail());
		rToken.setCreatedOn(new Date());
		refreshTokenRepository.save(rToken);
		verificationDetailsRepository.save(verificationdetails);
		user.setSaaspeLastLogin(new Date());
		userDetailsRepository.save(user);
		return new CommonResponse(HttpStatus.OK, new Response(Constant.USER_PROFILE, response),
				Constant.DATA_RETRIEVED_SUCCESSFULLY);

	}

	@Override
	@Transactional
	public CommonResponse changePassword(ChangePasswordRequest changePassword, UserLoginDetails profile)
			throws NoSuchMessageException, DataValidationException {
		UserLoginDetails userProfile;
		VerificationDetails verificationDetails;
		if (!changePassword.getNewPassword().equals(changePassword.getConfirmNewPassword())) {
			throw new DataValidationException(Constant.CONFIRM_PASSWORD_ERROR_MESSAGE, "403", HttpStatus.FORBIDDEN);
		}
		if (changePassword.getOldPassword().equals(changePassword.getNewPassword())) {
			throw new DataValidationException(Constant.NEW_PASSWORD_EQUALS_OLD_PASSWORD_ERROR_MESSAGE, "403",
					HttpStatus.FORBIDDEN);
		}
		if (StringUtils.isEmpty(profile.getEmailAddress())) {
			throw new DataValidationException(Constant.USER_ID_ERROR_MESSAGE, "403", HttpStatus.FORBIDDEN);
		}
		userProfile = userLoginDetailsRepository.findByEmail(profile.getEmailAddress());
		verificationDetails = verificationDetailsRepository.findByUserEmail(profile.getEmailAddress());
		if (userProfile == null) {
			throw new DataValidationException(Constant.USER_NOT_FOUND, null, HttpStatus.FORBIDDEN);
		}
		String passwordHash = CommonUtil.createHash(changePassword.getOldPassword(), verificationDetails.getSalt());
		if (!userProfile.getPassword().equalsIgnoreCase(passwordHash)) {
			throw new DataValidationException("Wrong Password!", null, HttpStatus.FORBIDDEN);
		}
		String salt = SecureUtils.getSalt();
		String password = CommonUtil.createHash(changePassword.getNewPassword(), salt);
		userProfile.setPassword(password);
		verificationDetails.setSalt(salt);
		userLoginDetailsRepository.save(userProfile);
		verificationDetailsRepository.save(verificationDetails);
		return new CommonResponse(HttpStatus.OK, new Response("changePasswordResponse", profile.getEmailAddress()),
				"Password Updated successfully");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CommonResponse verifyEmailInitiate(String emailAddress, String redirectUrl)
			throws AuthenticationException, IOException, TemplateException, MessagingException {

		UserLoginDetails userProfile;
		VerificationDetails verificationDetails;
		Date current = new Date();
		if (emailAddress != null) {
			userProfile = userLoginDetailsRepository.findByUserEmail(emailAddress);
			verificationDetails = verificationDetailsRepository.findByUserEmail(emailAddress);
		} else {
			throw new AuthenticationException(Constant.USER_ID_ERROR_MESSAGE, Constant.USER_ID_ERROR_KEY,
					HttpStatus.FORBIDDEN);
		}
		if (userProfile == null) {
			throw new AuthenticationException(Constant.USER_NOT_FOUND, null, HttpStatus.NOT_FOUND);
		} else if (Boolean.TRUE.equals(verificationDetails.getEmailVerified())) {
			throw new AuthenticationException(Constant.VERIFY_EMAIL_ERROR_MESSAGE, Constant.VERIFY_EMAIL_ERROR_KEY,
					HttpStatus.FORBIDDEN);
		}

		Date emailVerificationExpiredate = DateUtils.addMinutes(Calendar.getInstance().getTime(),
				Constant.EMAIL_VERIFICATION_CODE_EXPIRE_DATE);
		int verificationCode = CommonUtil.getRandomNumber(100000, 999999);
		sendVerificationEmail(userProfile.getEmailAddress(), verificationCode,
				userProfile.getFirstName().concat(" ").concat(userProfile.getLastName()), redirectUrl);
		verificationDetails.setEmailVerificationCode(String.valueOf(verificationCode));
		verificationDetails.setEmailVerificationCodeExpiryDate(emailVerificationExpiredate);
		verificationDetails.setEmailVerificationCodeSendDate(current);
		verificationDetailsRepository.save(verificationDetails);
		userLoginDetailsRepository.save(userProfile);
		return new CommonResponse(HttpStatus.OK, new Response("verifyEmailInitiateResponse", emailAddress),
				"Email Sent successfully");

	}

	@Override
	@Transactional
	public Optional<UserLoginDetails> loadUserByUsername(String username) throws UsernameNotFoundException {
		return userLoginDetailsRepository.findById(username);
	}

	@Override
	public CommonResponse verifyEmail(String userId, String emailAddress, String verificationToken)
			throws AuthenticationException, UnsupportedEncodingException {
		UserLoginDetails userProfile;
		VerificationDetails verificationDetails;
		Date current = new Date();
		String decodedEmail = URLDecoder.decode(emailAddress, Constant.UTF);
		if (decodedEmail != null) {
			userProfile = userLoginDetailsRepository.findByUserEmail(decodedEmail);
			verificationDetails = verificationDetailsRepository.findByUserEmail(decodedEmail);
		} else if (!StringUtils.isBlank(decodedEmail)) {
			userProfile = userLoginDetailsRepository.findByEmail(decodedEmail);
			verificationDetails = verificationDetailsRepository.findByUserEmail(decodedEmail);
		} else {
			throw new AuthenticationException(messageSource.getMessage("USER_ID_ERROR_MESSAGE", null, null),
					messageSource.getMessage("USER_ID_ERROR_KEY", null, null), HttpStatus.FORBIDDEN);
		}
		if (userProfile == null) {
			throw new AuthenticationException(Constant.USER_NOT_FOUND, null, HttpStatus.NOT_FOUND);

		} else if (Boolean.TRUE
				.equals(StringUtils.isBlank(verificationToken)
						|| StringUtils.isBlank(verificationDetails.getEmailVerificationCode())
						|| !verificationDetails.getEmailVerificationCode().equals(verificationToken)
						|| verificationDetails.getEmailVerified())
				|| verificationDetails.getEmailVerificationCodeExpiryDate().compareTo(current) < 0) {
			throw new AuthenticationException("Email Already Verified", emailAddress, HttpStatus.FORBIDDEN);
		}
		verificationDetails.setEmailVerificationCode(null);
		verificationDetails.setEmailVerified(true);
		verificationDetails.setEmailVerificationCodeExpiryDate(null);
		verificationDetails.setEmailVerifiedDate(current);
		verificationDetailsRepository.save(verificationDetails);
		return new CommonResponse(HttpStatus.OK, new Response("verifyEmailInitiateResponse", emailAddress),
				"Email Verified Successfully");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CommonResponse resetInitiate(String emailAddress, String redirectUrl)
			throws DataValidationException, IOException, TemplateException, MessagingException {
		UserLoginDetails userProfile;
		VerificationDetails verificationDetails;
		Date current = new Date();
		if (!StringUtils.isBlank(emailAddress)) {
			userProfile = userLoginDetailsRepository.findByEmail(emailAddress);
			verificationDetails = verificationDetailsRepository.findByUserEmail(emailAddress);
		} else {
			throw new DataValidationException(messageSource.getMessage("USER_ID_ERROR_MESSAGE", null, null),
					messageSource.getMessage("USER_ID_ERROR_KEY", null, null), HttpStatus.FORBIDDEN);
		}
		if (userProfile == null) {
			throw new DataValidationException(Constant.USER_NOT_FOUND, emailAddress, HttpStatus.NOT_FOUND);
		} else if (Boolean.FALSE.equals(verificationDetails.getEmailVerified())) {
			throw new DataValidationException("Email Not Verified", emailAddress, HttpStatus.FORBIDDEN);
		}
		int resetCode = CommonUtil.getRandomNumber(100000, 999999);

		sendResetInitiateEmail(userProfile.getEmailAddress(), resetCode, redirectUrl);
		verificationDetails.setResetVerificationCode(String.valueOf(resetCode));
		verificationDetails.setResetVerificationCodeExpiryDate(current);
		verificationDetailsRepository.save(verificationDetails);
		return new CommonResponse(HttpStatus.OK, new Response("resetInitiateResponse", emailAddress),
				"Initiated successfully");
	}

	@Override
	@Transactional
	public CommonResponse resetPassword(ResetPasswordRequest resetPassword) throws DataValidationException {
		UserLoginDetails userProfile;
		VerificationDetails verificationDetails;
		if (!StringUtils.isBlank(resetPassword.getNewPassword())
				&& !StringUtils.isBlank(resetPassword.getConfirmNewPassword())
				&& !resetPassword.getNewPassword().equals(resetPassword.getConfirmNewPassword())) {
			throw new DataValidationException(messageSource.getMessage("CONFIRM_PASSWORD_ERROR_MESSAGE", null, null),
					messageSource.getMessage("CONFIRM_PASSWORD_ERROR_KEY", null, null), HttpStatus.FORBIDDEN);
		}
		if (!resetPassword.getEmailAddress().isEmpty()) {
			userProfile = userLoginDetailsRepository.findByEmail(resetPassword.getEmailAddress());
			verificationDetails = verificationDetailsRepository.findByUserEmail(resetPassword.getEmailAddress());
		} else {
			throw new DataValidationException(messageSource.getMessage("VALIDATION_ERROR_MESSAGE", null, null),
					messageSource.getMessage("CONFIRM_PASSWORD_ERROR_KEY", null, null), HttpStatus.FORBIDDEN);
		}
		if (userProfile == null) {
			throw new DataValidationException(null, null, HttpStatus.NOT_FOUND);
		}
		Date current = new Date();
		if (!resetPassword.getPasswordResetCode().equals(verificationDetails.getResetVerificationCode())
				|| verificationDetails.getResetVerificationCodeExpiryDate().compareTo(current) > 0) {
			throw new DataValidationException(
					messageSource.getMessage("PASSWORD_RESET_CODE_EXPIRY_ERROR_MESSAGE", null, null), null,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		String salt = SecureUtils.getSalt();
		String password = CommonUtil.createHash(resetPassword.getNewPassword(), salt);
		userProfile.setPassword(password);
		verificationDetails.setResetVerificationCode(null);
		verificationDetails.setResetVerificationCodeExpiryDate(null);
		verificationDetails.setSalt(salt);
		userLoginDetailsRepository.save(userProfile);
		verificationDetailsRepository.save(verificationDetails);
		return new CommonResponse(HttpStatus.OK, new Response("resetInitiateResponse", resetPassword.getEmailAddress()),
				"Password Reset Successfull");
	}

	public boolean isValid(String password) {
		PasswordValidator validator = new PasswordValidator(Arrays.asList(new LengthRule(8, 30),
				new CharacterRule(EnglishCharacterData.UpperCase, 1),
				new CharacterRule(EnglishCharacterData.LowerCase, 1), new CharacterRule(EnglishCharacterData.Digit, 1),
				new CharacterRule(EnglishCharacterData.Special, 1), new WhitespaceRule()));
		RuleResult result = validator.validate(new PasswordData(password));
		return (result.isValid());
	}

	@Override
	public CommonResponse getUserAccessAndRole(UserLoginDetails profile) {
		CommonResponse commonResponse = new CommonResponse();
		UserAccessRoleResponse response = new UserAccessRoleResponse();
		UserDetails user = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		Response resp = new Response();
		if (user != null) {
			response.setRole(user.getUserRole());
			String[] list = user.getUserAccess().split(", ");
			response.setAccess(list);
			response.setAccess(list);
			resp.setData(response);
			resp.setAction(Constant.USER_ACCESS_AND_ROLES_RESPONSE);
			commonResponse.setResponse(resp);
			commonResponse.setMessage(Constant.DATA_RETRIEVED_SUCCESSFULLY);
			commonResponse.setStatus(HttpStatus.OK);
		} else {
			resp.setAction(Constant.USER_ACCESS_AND_ROLES_RESPONSE);
			resp.setData("");
			commonResponse.setResponse(resp);
			commonResponse.setMessage(Constant.USER_NOT_FOUND);
			commonResponse.setStatus(HttpStatus.NOT_FOUND);
		}
		return commonResponse;
	}

	@Override
	public CommonResponse getUserAccessAndRole(HttpServletRequest request, UserLoginDetails profile,
			String xAuthProvider) {
		CommonResponse commonResponse = new CommonResponse();
		UserAccessRoleResponse accessRoleResponse = new UserAccessRoleResponse();
		UserDetails user = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		accessRoleResponse.setCurrency(userDetailsRepository.getCurrency().getCurrency());
		Response response = new Response();

		String token = request.getHeader(Constant.HEADER_STRING);
		if (xAuthProvider == null || xAuthProvider.equalsIgnoreCase("internal")) {
			accessRoleResponse.setRole(user.getUserRole());
			if (user.getUserRole().equalsIgnoreCase("approver")) {
				String[] list = Constant.ROLE_APPROVER.split(", ");
				accessRoleResponse.setAccess(list);
			} else if (user.getUserRole().equalsIgnoreCase("reviewer")) {
				String[] list = Constant.ROLE_REVIEWER.split(", ");
				accessRoleResponse.setAccess(list);
			} else if (user.getUserRole().equalsIgnoreCase("super_admin")) {
				String[] list = Constant.ROLE_SUPER_ADMIN.split(", ");
				accessRoleResponse.setAccess(list);
			} else if (user.getUserRole().equalsIgnoreCase("contributor")) {
				String[] list = Constant.ROLE_CONTRIBUTOR.split(", ");
				accessRoleResponse.setAccess(list);
			} else if (user.getUserRole().equalsIgnoreCase("support")) {
				String[] list = Constant.ROLE_SUPPORT.split(", ");
				accessRoleResponse.setAccess(list);
			} else if (user.getUserRole().equalsIgnoreCase("custom")) {
				String[] list = user.getUserAccess().split(", ");
				accessRoleResponse.setAccess(list);
			}
			response.setData(accessRoleResponse);
			response.setAction(Constant.USER_ACCESS_AND_ROLES_RESPONSE);
			commonResponse.setResponse(response);
			commonResponse.setMessage("Data retrieved successfully");
			commonResponse.setStatus(HttpStatus.OK);
		} else {
			DecodedJWT jwt = JWT.decode(token.replace("Bearer ", ""));
			String email = jwt.getClaim("upn").asString();
			DocusignUserCache userId = redisUtility.getDocusignValue(redisPrefix + email);
			Clm clm = new Clm();
			ResponseEntity<Clm> responseEntity = null;
			URI uri = UriComponentsBuilder
					.fromUriString(getDousignUrl().getGetConsent().replace(Constant.HOST, docusignHost))
					.queryParam(Constant.EMAIL, email).queryParam("userId", userId.getUserId()).build().toUri();
			try {
				responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.GET, null, Clm.class);
			} catch (HttpServerErrorException ex) {
				return new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response(Constant.USER_ACCESS_AND_ROLES_RESPONSE, new ArrayList<>()),
						"UserAccessroles failed");
			}
			if (responseEntity.getBody().getError() == null) {
				clm.setEnabled(true);
				clm.setConsentGiven(true);
			} else if (responseEntity.getBody().getError() != null
					&& responseEntity.getBody().getError().equalsIgnoreCase("consent_required")) {
				clm.setEnabled(true);
				clm.setConsentGiven(false);
				clm.setConsentUrl(responseEntity.getBody().getConsentUrl());
			}
			accessRoleResponse.setClm(clm);
			accessRoleResponse.setRole("CLM_USER");
			String[] list = Constant.ROLE_CLM.split(", ");
			accessRoleResponse.setAccess(list);
			response.setData(accessRoleResponse);
			response.setAction(Constant.USER_ACCESS_AND_ROLES_RESPONSE);
			commonResponse.setResponse(response);
			commonResponse.setMessage("Data retrieved successfully");
			commonResponse.setStatus(HttpStatus.OK);
		}
		return commonResponse;
	}

	@Override
	public CommonResponse getRefreshToken(RefreshTokenRequest refreshTokenRequest) throws DataValidationException {
		RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(refreshTokenRequest.getRefreshToken());
		RefreshTokenResponse refereshTokenResponse = new RefreshTokenResponse();
		Response response = new Response();
		if (refreshToken != null) {
			if (refreshToken.getRefreshTokenExpiry().compareTo(new Date()) > 0) {
				UserLoginDetails userDetails = userLoginDetailsRepository.findByEmail(refreshToken.getUserEmail());
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());
				calendar.add(Calendar.MINUTE, 60);
				refreshToken.setRefreshToken(UUID.randomUUID().toString());
				refreshToken.setRefreshTokenExpiry(calendar.getTime());
				refreshToken.setUserEmail(userDetails.getEmailAddress());
				refreshToken.setUpdatedOn(new Date());
				String token = buildToken(userDetails);
				DecodedJWT jwt = JWT
						.require(Algorithm.HMAC256(EncryptionHelper.decrypt(encryptionKey, jwtKey).getBytes())).build()
						.verify(token);
				refereshTokenResponse.setToken(token);
				refereshTokenResponse.setAccessTokenExpiry(jwt.getExpiresAt());
				refereshTokenResponse.setRefreshToken(refreshToken.getRefreshToken());
				refereshTokenResponse.setRefreshTokenExpiry(calendar.getTime());
				refreshToken.setAccessToken(token);
				refreshTokenRepository.save(refreshToken);
				response.setData(refereshTokenResponse);
			} else {
				refreshTokenRepository.delete(refreshToken);
				throw new DataValidationException("Refresh token expired. Please make a new signin request", null,
						null);
			}
		} else {
			throw new DataValidationException("Provide Valid Refresh Token", null, null);
		}
		return new CommonResponse(HttpStatus.OK, response, "RefereshTokenResponse");
	}

	@Override
	public CommonResponse sendConsentToUser(HttpServletRequest request, String xAuthProvider)
			throws MessagingException, DataValidationException, IOException, TemplateException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		if (xAuthProvider == null || xAuthProvider.equalsIgnoreCase("azure")) {
			String token = request.getHeader(Constant.HEADER_STRING);
			DecodedJWT jwt = JWT.decode(token.replace("Bearer ", ""));
			String email = jwt.getClaim("upn").asString();
			String name = jwt.getClaim("name").asString();
			String toAddress = email;
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message);
			String subject = "View Consent";
			String consentMailContent;
			Map<String, Object> model = new HashMap<>();
			DocusignUserCache userId = redisUtility.getDocusignValue(redisPrefix + email);
			URI uri = UriComponentsBuilder
					.fromUriString(getDousignUrl().getGetConsent().replace(Constant.HOST, docusignHost))
					.queryParam(Constant.EMAIL, email).queryParam("userId", userId.getUserId()).build().toUri();
			ResponseEntity<Clm> responseEntity;
			try {
				responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.GET, null, Clm.class);
			} catch (HttpServerErrorException ex) {
				return new CommonResponse(HttpStatus.BAD_REQUEST, new Response("UserConsent", new ArrayList<>()),
						"UserConsent failed");
			}
			Clm responseBody = responseEntity.getBody();
			if (responseBody != null && "consent_required".equalsIgnoreCase(responseBody.getError())) {
				model.put("name", name);
				model.put("consentUrl", responseBody.getConsentUrl());
				Template consentMailTemplate = config.getTemplate("user-consent.html");
				consentMailContent = FreeMarkerTemplateUtils.processTemplateIntoString(consentMailTemplate, model);
				consentMailContent = consentMailContent.replace(Constant.NAME, name);
				consentMailContent = consentMailContent.replace("{{consentUrl}}", responseBody.getConsentUrl());
				consentMailContent = consentMailContent.replace("{{supportEmail}}", supportEmail);
				consentMailContent = consentMailContent.replace("{{orgName}}", senderName);
				consentMailContent = consentMailContent.replace("{{mediaHost}}", mediaHost);
				consentMailContent = consentMailContent.replace("{{imageKey}}", imageKey);
			} else {
				response.setData("User with email " + email + " has already given consent");
				response.setAction("UserConsentResponse");
				commonResponse.setResponse(response);
				commonResponse.setMessage("User has already given consent");
				commonResponse.setStatus(HttpStatus.BAD_REQUEST);
				return commonResponse;
			}
			helper.setFrom(mailDomainName, senderName);
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setText(consentMailContent, true);
			mailSender.send(helper.getMimeMessage());
			response.setData("Consent URL Sent to " + toAddress);
			response.setAction("UserConsentResponse");
			commonResponse.setResponse(response);
			commonResponse.setMessage("Email sent successfully");
			commonResponse.setStatus(HttpStatus.OK);
			return commonResponse;
		} else
			throw new DataValidationException("Headers should be azure", "400", HttpStatus.BAD_REQUEST);
	}

	@Override
	public CommonResponse resetFailedCounts() {
		List<VerificationDetails> verificationDetailsList = verificationDetailsRepository.findAll();
		for (VerificationDetails details : verificationDetailsList) {
			if (details.getFailedCount() == null || details.getFailedCount() > 0) {
				details.setFailedCount(0);
				verificationDetailsRepository.save(details);
			}
		}
		return null;
	}

	@Override
	public String deleteUserinRedis(String email) {
		redisUtility.deleteKeyFromRedis(redisPrefix + email);
		redisUtility.deleteKeyFromRedis("token" + email);
		redisUtility.deleteKeyFromRedis(email);
		return "deleted" + email + "Successfully";
	}

}