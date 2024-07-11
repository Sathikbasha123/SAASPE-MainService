package saaspe.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.constant.Constant;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.AuthenticationException;
import saaspe.exception.DataValidationException;
import saaspe.model.ChangePasswordRequest;
import saaspe.model.CommonResponse;
import saaspe.model.LoginRequest;
import saaspe.model.RefreshTokenRequest;
import saaspe.model.ResetPasswordRequest;
import saaspe.model.Response;
import saaspe.model.SignupRequest;
import saaspe.model.VerificationRequest;
import saaspe.service.UserLoginService;

@RestController
@ControllerLogging
@RequestMapping("api/userprofile")
public class UserLoginController {

	@Autowired
	private UserLoginService userLoginService;

	private static final Logger log = LoggerFactory.getLogger(UserLoginController.class);

	@PostMapping("/signup")
	public ResponseEntity<CommonResponse> createProfile(@Valid @RequestBody SignupRequest signupRequest) {
		try {
			CommonResponse commonResponse = userLoginService.createUserProfile(signupRequest);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending createProfile method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.USER_PROFILE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending createProfile method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("CreateProfileResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/login")
	public ResponseEntity<CommonResponse> login(@Valid @RequestBody LoginRequest request) {
		try {
			CommonResponse commonResponse = userLoginService.login(request);
			return ResponseEntity.ok(commonResponse);
		} catch (AuthenticationException e) {
			log.error("*** Ending login method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.USER_PROFILE, request.getEmailAddress()), e.getMessage()));
		} catch (MailSendException e) {
			log.error("*** Ending login method with mail exeception ***", e);
			return ResponseEntity.badRequest()
					.body(new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response(Constant.LOGIN_RESPONSE, new ArrayList<>()),
							"Please unblock the mail service to send OTP."));
		} catch (DataValidationException e) {
			log.error("*** Ending  DataValidationException login method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.USER_PROFILE, request.getEmailAddress()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending Exception login method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.LOGIN_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<CommonResponse> verifyOTP(@Valid @RequestBody VerificationRequest request) {
		try {
			CommonResponse commonResponse = userLoginService.verifyOTP(request);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending login method with an error ***", e);
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
					.body(new CommonResponse(HttpStatus.TOO_MANY_REQUESTS,
							new Response(Constant.LOGIN_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending Exception login method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.LOGIN_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/changePassword")
	public ResponseEntity<CommonResponse> changePassword(@RequestBody ChangePasswordRequest change,
			Authentication authentication) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			commonResponse = userLoginService.changePassword(change, profile);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending changePassword method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("changePasswordResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending changePassword method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Respo", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/verify-initiate")
	public ResponseEntity<CommonResponse> verifyEmailInitiate(@RequestParam(required = false) String emailAddress,
			String redirectUrl) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = userLoginService.verifyEmailInitiate(emailAddress, redirectUrl);
			return ResponseEntity.ok(commonResponse);
		} catch (AuthenticationException e) {
			log.error("***Ending of verify_initiate with an error", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.FORBIDDEN,
					new Response("verifyEmailResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("**Ending the verify_initiate with an error**", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("EmailInitiateResponse", new ArrayList<>()), e.getMessage()));
		}

	}

	@PostMapping("/verify-email")
	public ResponseEntity<CommonResponse> verifyEmail(@RequestParam(required = false) String userId,
			@RequestParam(required = false) String emailAddress,
			@RequestParam(required = true) String verificationToken) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = userLoginService.verifyEmail(userId, emailAddress, verificationToken);
			return ResponseEntity.ok(commonResponse);
		} catch (AuthenticationException e) {
			log.error("**Ending the verify_email with an error**", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("verifyEmailResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("**Ending the verify_email with an error**", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("VerifyEmailResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/reset-initiate")
	public ResponseEntity<CommonResponse> resetInitiate(@RequestParam(required = true) String emailAddress,
			String redirectUrl) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = userLoginService.resetInitiate(emailAddress, redirectUrl);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("***Ending of reset_initiate with an error", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("resetInitiateResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("**Ending the reset_initiate with an error**", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ResetInitiateResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/reset-password")
	public ResponseEntity<CommonResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest reset) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = userLoginService.resetPassword(reset);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error(Constant.ENDING_RESET_PASSWORD_ERROR, e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("resetPasswordResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error(Constant.ENDING_RESET_PASSWORD_ERROR, e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ResetPasswordResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/access")
	public ResponseEntity<CommonResponse> userAccessAndRoles(HttpServletRequest request,
			Authentication authentication) {
		try {
			UserLoginDetails profile = new UserLoginDetails();
			if (authentication != null) {
				profile = (UserLoginDetails) authentication.getPrincipal();
			}
			CommonResponse response = userLoginService.getUserAccessAndRole(request, profile,
					request.getHeader("X-Auth-Provider"));
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("*** Ending resetPassword method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("AccessResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/refresh/token")
	public ResponseEntity<CommonResponse> getRefreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
		CommonResponse response = new CommonResponse();
		try {
			response = userLoginService.getRefreshToken(refreshTokenRequest);
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** ending getPermissionByRole method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("RefreshTokenResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending getPermissionByRole method with error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("RefreshTokenResponse", new ArrayList<>()), e.getMessage()));

		}
	}

	@GetMapping("/consent-email/to-user")
	public ResponseEntity<CommonResponse> sendConsentToUser(HttpServletRequest request) {
		CommonResponse response = new CommonResponse();
		try {
			response = userLoginService.sendConsentToUser(request, request.getHeader("X-Auth-Provider"));
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** ending UserConsentResponse method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserConsentResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending UserConsentResponse method with error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserConsentResponse", new ArrayList<>()), e.getMessage()));

		}
	}

	// @Scheduled(cron = "0 */10 * * * *")
	@GetMapping("/reset")
	public ResponseEntity<CommonResponse> resetFailedCounts() {
		try {
			CommonResponse commonResponse = userLoginService.resetFailedCounts();
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);

		} catch (Exception e) {
			log.error("*** Ending resetFailedCounts method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ResetFailedCountsResponce", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/delete/user-redis")
	public String deleteUserinRedis(HttpServletRequest request, @RequestParam String email) {
		return userLoginService.deleteUserinRedis(email);
	}

}