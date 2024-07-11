package saaspe.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.constant.Constant;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.AzureCredentials;
import saaspe.model.CommonResponse;
import saaspe.model.GraphApplicationLinkRequest;
import saaspe.model.Response;
import saaspe.service.AuthenticaionService;
import saaspe.service.impl.AsyncServiceImpl;

@RestController
@RequestMapping("/api/admin/")
@ControllerLogging
public class AuthenticationController {

	@Autowired
	private AuthenticaionService authenticaionService;
	
	@Autowired
	private AsyncServiceImpl asyncServiceImpl;

	private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

	@GetMapping("/integrations")
	@PreAuthorize("hasAuthority('VIEW_INTEGRATION')")
	public ResponseEntity<CommonResponse> getIntegrations(Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonResponse = authenticaionService.getIntegrations(profile);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error(Constant.AUTH_USER_CRED_ERROR, e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.INTEGRATION_LIST_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error(Constant.AUTH_USER_CRED_ERROR, e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.INTEGRATION_LIST_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/consent")
	@PreAuthorize("hasAnyAuthority('ENABLE_INTEGRATION','MAP_INTEGRATION')")
	public ResponseEntity<CommonResponse> addAdminConsent(@RequestBody AzureCredentials azureCredentials,
			Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonResponse = authenticaionService.addAdminConsent(azureCredentials, profile);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error(Constant.AUTH_USER_CRED_ERROR, e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("Admin Consent", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.INTEGRATION_LIST_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/code")
	@PreAuthorize("hasAnyAuthority('ENABLE_INTEGRATION','MAP_INTEGRATION')")
	public ResponseEntity<CommonResponse> addAuthCode(@RequestBody AzureCredentials azureCredentials,
			Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonResponse = authenticaionService.addAuthCode(azureCredentials, profile);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error(Constant.AUTH_USER_CRED_ERROR, e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.AUTH_CODE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error(Constant.AUTH_USER_CRED_ERROR, e);
			Thread.currentThread().interrupt();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.AUTH_CODE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/application")
	@PreAuthorize("hasAuthority('MAP_INTEGRATION')")
	public ResponseEntity<CommonResponse> getGraphServicePrincipals(Authentication authentication) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			commonResponse = authenticaionService.getGraphServicePrincipals(profile);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			log.error(Constant.AUTH_USER_CRED_ERROR, e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.INTEGRATION_LIST_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@PutMapping("/application/link")
	@PreAuthorize("hasAuthority('MAP_INTEGRATION')")
	public ResponseEntity<CommonResponse> addGraphApplication(
			@RequestBody GraphApplicationLinkRequest applicationLinkRequest, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonResponse = authenticaionService.addGraphApplication(applicationLinkRequest, profile);
			asyncServiceImpl.getUsersId();
			asyncServiceImpl.getUsersUnderApplicationLogin();
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error(Constant.AUTH_USER_CRED_ERROR, e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("ApplicationMappingFailed", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error(Constant.AUTH_USER_CRED_ERROR, e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.INTEGRATION_LIST_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@Scheduled(cron = "0 0 7 * * ?")
	public void getUsersUnderApplicationLogin() {
		try {
			log.info("==== getUsersUnderApplicationLogin method started====" + LocalDate.now(ZoneId.systemDefault()));
			asyncServiceImpl.getUsersUnderApplicationLogin();
			log.info("==== getUsersUnderApplicationLogin method started====" + LocalDate.now(ZoneId.systemDefault()));
			return;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
	}

	@Scheduled(cron = "0 0 6 * * ?")
	public void getUsersId() {
		try {
			log.info("==== getUsersId method started====" + LocalDate.now(ZoneId.systemDefault()));
			asyncServiceImpl.getUsersId();
			log.info("==== getUsersId method success====" + LocalDate.now(ZoneId.systemDefault()));
			return;
		} catch (Exception e) {
			return;
		}
	}

	@Scheduled(cron = "0 */30 * ? * *")
	public ResponseEntity<CommonResponse> getNewAccessToken() {
		CommonResponse commonResponse = new CommonResponse();
		try {
			log.info("Ready to get token");
			commonResponse = authenticaionService.getNewAccessToken();
			log.info("Success in geting token");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			log.error("failed in geting token");
			Thread.currentThread().interrupt();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}

}
