package saaspe.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import saaspe.aspect.ControllerLogging;
import saaspe.configuration.AzureBlobAdapter;
import saaspe.constant.Constant;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.AdaptorValue;
import saaspe.model.ApplicationDetailsUpdateRequest;
import saaspe.model.ApplicationIdsRemoveRequest;
import saaspe.model.CommonResponse;
import saaspe.model.Credentails;
import saaspe.model.DeptOnboardingWorkFlowRequest;
import saaspe.model.PurchasedApplcationRequest;
import saaspe.model.Response;
import saaspe.model.SingleNewApplicationOnboardingRequest;
import saaspe.service.ApplicationDetailService;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/application")
public class ApplicationDetailController {

	@Autowired
	private ApplicationDetailService applicationService;

	@Autowired
	private AzureBlobAdapter azureBlobAdapter;

	public static final String APPLICATION_RESPONSE = "ApplicationResponse";

	private static final Logger log = LoggerFactory.getLogger(ApplicationDetailController.class);

	@PutMapping("/update-by-applicationid")
	@PreAuthorize("hasAuthority('EDIT_APPLICATION')")
	public ResponseEntity<CommonResponse> modifyApplicationDetails(@RequestParam String applicationId,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID,
			@RequestBody ApplicationDetailsUpdateRequest updateRquest) {
		try {
			CommonResponse applicationDetailsUpdateRequest = applicationService.modifyApplicationDetails(applicationId,
					updateRquest);
			return new ResponseEntity<>(applicationDetailsUpdateRequest, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending modifyApplicationDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(APPLICATION_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending modifyApplicationDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(APPLICATION_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@DeleteMapping("/remove-details/{applicationId}")
	@PreAuthorize("hasAuthority('DELETE_APPLICATION')")
	public ResponseEntity<CommonResponse> removeApplicationDetails(
			@Valid @PathVariable("applicationId") String applicationId,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse applicationDetailsResponse = applicationService.removeApplicationDetails(applicationId);
			return new ResponseEntity<>(applicationDetailsResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending removeApplicationDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("ApplicationDetailsResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending removeApplicationDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ApplicationRemoveResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/list-view")
	@PreAuthorize("hasAuthority('VIEW_APPLICATION')")
	public ResponseEntity<CommonResponse> getApplicationListView() {
		try {
			CommonResponse response = applicationService.getApplicationListView();
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("*** Ending getApplicationListView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ApplicationListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@DeleteMapping("/multiple-remove")
	@PreAuthorize("hasAuthority('DELETE_APPLICATION')")
	public ResponseEntity<CommonResponse> deleteByApplicationIds(
			@RequestBody ApplicationIdsRemoveRequest applicationIds) {
		try {
			CommonResponse commonResponse = applicationService.deleteAllByApplicationIds(applicationIds);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending deleteByApplicationIds method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("ApplicationDetailsResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("**ApplicationDetailsController ending with error @deleteByApplicationIds()**");
			return ResponseEntity.internalServerError().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ApplicationDeleteResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/overview")
	@PreAuthorize("hasAuthority('VIEW_APPLICATION')")
	public ResponseEntity<CommonResponse> getApplicationOverview(
			@RequestParam(value = "applicationId", required = true) String applicationId,
			@RequestParam(value = "category", required = false) String category, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = applicationService.getApplicationOverview(applicationId, category, profile);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("*** Ending getApplicationOverview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ApplicationOverviewResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/new/onboard")
	@PreAuthorize("hasAuthority('ADD_APPLICATION')")
	public ResponseEntity<CommonResponse> newApplicationOnBoarding(
			@RequestBody SingleNewApplicationOnboardingRequest onboardingRequest, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonResponse = applicationService.newApplicationOnboarding(onboardingRequest, profile);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending newApplicationOnBoarding method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.ONBOARDING_WORK_FLOW_ACTION_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending newApplicationOnBoarding method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ApplicationOnboardingResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/purchase/onboard")
	@PreAuthorize("hasAuthority('ADD_APPLICATION')")
	public ResponseEntity<CommonResponse> purchasedApplicationOnboard(
			@RequestBody PurchasedApplcationRequest onboardingRequest, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonResponse = applicationService.purchasedApplicationOnboard(onboardingRequest, profile);
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (DataValidationException e) {
			log.error("*** Ending purchasedApplicationOnboard method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.ONBOARDING_WORK_FLOW_ACTION_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending purchasedApplicationOnboard method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ApplicationPurchasedOnboardingResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping(value = "/file", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
			MediaType.APPLICATION_PDF_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_PDF_VALUE })
	@PreAuthorize("hasAuthority('ADD_APPLICATION')")
	public ResponseEntity<CommonResponse> upload(@RequestPart List<MultipartFile> multipartFiles,
			@RequestParam(value = "fileName", required = false) String fileName, Authentication authentication) {
		try {
			CommonResponse response = azureBlobAdapter.upload(multipartFiles, fileName);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("*** Ending upload method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(APPLICATION_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("excel")
	@PreAuthorize("hasAuthority('ADD_APPLICATION')")
	public ResponseEntity<CommonResponse> saveApplicatoinOnboarding(
			@RequestParam("applicationFile") MultipartFile applicationFile, Authentication authentication)
			throws IOException {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonResponse = applicationService.saveApplicatoinOnboarding(applicationFile, profile);
			return ResponseEntity.status(commonResponse.getStatus()).body(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending saveApplicatoinOnboarding method with an error ***", e);
			return ResponseEntity.badRequest()
					.body(new CommonResponse(HttpStatus.CONFLICT,
							new Response(Constant.ONBOARDING_WORK_FLOW_ACTION_RESPONSE, e.getMessage()),
							"Excel updation failed"));
		} catch (Exception e) {
			log.error("*** Ending saveApplicatoinOnboarding method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Excel Upload Application", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/request/list-view")
	@PreAuthorize("hasAnyAuthority('REVIEW_ONBOARDINGMGMT','APPROVE_ONBOARDINGMGMT','VIEW_ONBOARDINGMGMT')")
	public ResponseEntity<CommonResponse> applicatoinReviewerApproverListView(Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = applicationService.applicatoinReviewerApproverListView(profile);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("*** Ending applicatoinReviewerApproverListView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ApplicationARSListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/request/detail-view")
	@PreAuthorize("hasAnyAuthority('REVIEW_ONBOARDINGMGMT','APPROVE_ONBOARDINGMGMT','VIEW_ONBOARDINGMGMT','REVIEW_APPLICATION')")
	public ResponseEntity<CommonResponse> applicationOnboardDetailView(
			@RequestParam(value = "childRequestId", required = false) String childRequestId,
			@RequestParam(value = "requestId", required = false) String requestId, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = applicationService.applicatoinReviewerApproverDetailsView(childRequestId,
					requestId, profile);
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending applicationOnboardDetailView method with an error ***", e);
			return ResponseEntity.status(e.getStatusCode()).body(new CommonResponse(e.getStatusCode(),
					new Response("ApplicationARSDetailsResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending applicationOnboardDetailView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ApplicationARSDetailsResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/review")
	@PreAuthorize("hasAnyAuthority('REVIEW_ONBOARDINGMGMT','APPROVE_ONBOARDINGMGMT','VIEW_ONBOARDINGMGMT','APPROVE_APPLICATION')")
	public ResponseEntity<CommonResponse> applicaitonOnboardReview(
			@RequestParam(value = "childRequestId", required = false) String childRequestId,
			@RequestParam(value = "requestId", required = false) String requestId,
			@RequestBody DeptOnboardingWorkFlowRequest onboardingWorkFlowRequest, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response1 = applicationService.applicationOnboardReview(childRequestId, requestId, profile,
					onboardingWorkFlowRequest);
			return ResponseEntity.ok(response1);
		} catch (DataValidationException e) {
			log.error("*** Ending applicaitonOnboardReview method with an  error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.ONBOARDING_WORK_FLOW_ACTION_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (MailSendException e) {
			log.error("*** Ending ApplicaitonOnboardReview method with an error ***", e);
			return ResponseEntity.badRequest()
					.body(new CommonResponse(HttpStatus.CONFLICT,
							new Response(Constant.ONBOARDING_WORK_FLOW_ACTION_RESPONSE, new ArrayList<>()),
							"Issue with sending mail"));
		} catch (Exception e) {
			log.error("*** Ending applicaitonOnboardReview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ApplicationReviewResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/StoreCredentials")
	public ResponseEntity<CommonResponse> storeCredentials(@RequestBody Credentails credentails) {
		try {
			log.info("request received for storeCredentials api");
			applicationService.storeCredentials(credentails);
			log.info("credentials stored successfully");
			return ResponseEntity.ok(new CommonResponse(HttpStatus.OK,
					new Response("CredentialStoredResponse", "Credentials Stored successfully!"), null));

		} catch (Exception e) {
			e.printStackTrace();
			log.error("error occured while storing the credentials");
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("CredentialStoredResponse", "error occured while storing the credentials!"), null));
		}
	}

	@GetMapping("/adaptor/keys")
	public ResponseEntity<CommonResponse> provideKeys(@RequestParam String applicationId) {
		try {
			CommonResponse response = applicationService.provideKeys(applicationId);
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (DataValidationException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ProvideKeysResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ProvideKeysResponse", "error occured while providing the keys!"), null));
		}
	}

	@PostMapping("/adaptor/save/details")
	public ResponseEntity<CommonResponse> saveNewAppDetails(@RequestBody AdaptorValue request,
			@RequestParam String applicationId) {
		try {
			CommonResponse response = applicationService.saveNewAppDetails(request, applicationId);
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (DataValidationException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.SAVE_NEW_APP_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			log.error("Error occured while storing the credentials");
			return ResponseEntity.badRequest()
					.body(new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response(Constant.SAVE_NEW_APP_RESPONSE,
									"Error occured while storing the credentials!"),
							"Invalid data format for key values"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest()
					.body(new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response(Constant.SAVE_NEW_APP_RESPONSE, "error occured while providing the values!"),
							"error occured while providing the values!"));

		}
	}

	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
	@GetMapping("/updateApplicationCredentials")
	public ResponseEntity<CommonResponse> updateAppCredentials() {
		try {
			log.info("==== updateAppCredentials method started====" + LocalDate.now(ZoneId.systemDefault()));
			CommonResponse response = applicationService.updateAppCredentials();
			log.info("==== updateAppCredentials method success====" + LocalDate.now(ZoneId.systemDefault()));
			return new ResponseEntity<>(response, response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest()
					.body(new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response("Update app credentials",
									"error occured while updating application credentials"),
							"error occured while updating application credentials"));
		}
	}

	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
	@GetMapping("/updateApplicationRefreshToken")
	public ResponseEntity<CommonResponse> updateRefreshToken() {
		try {
			log.info("==== updateRefreshToken method started====" + LocalDate.now(ZoneId.systemDefault()));
			CommonResponse response1 = applicationService.updateRefreshTokens();
			log.info("==== updateRefreshToken method success====" + LocalDate.now(ZoneId.systemDefault()));
			return ResponseEntity.ok(response1);
		} catch (Exception e) {
			log.error("*** Ending applicaitonOnboardReview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ApplicationReviewResponse", new ArrayList<>()), e.getMessage()));
		}
	}

}
