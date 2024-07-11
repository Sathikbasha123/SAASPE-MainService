package saaspe.controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import saaspe.aspect.ControllerLogging;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.model.UserOnboardingDetailsRequest;
import saaspe.model.UserOnboardingWorkFlowRequest;
import saaspe.model.UserSingleOnboardingRequest;
import saaspe.service.UserOnboardingDetailsService;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/user/onboarding")
public class UserOnboardingDetailsController {

	@Autowired
	private UserOnboardingDetailsService userOnboardingDetailsService;

	private static final Logger log = LoggerFactory.getLogger(UserOnboardingDetailsController.class);

	@PostMapping("/single-user")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('ADD_USER')")
	public ResponseEntity<CommonResponse> addUserOnboardingDetails(
			@Valid @RequestBody UserSingleOnboardingRequest userOnboardingDetails, Authentication authentication,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
		try {
			CommonResponse commonResponse = userOnboardingDetailsService.userSingleOnboarding(userOnboardingDetails,
					profile);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending addUserOnboardingDetails method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("createUserResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending addUserOnboardingDetails method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("createUserResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PutMapping("/modify-user")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('EDIT_USER')")
	public ResponseEntity<CommonResponse> modifyUserOnboardingDetails(
			@Valid @RequestBody UserOnboardingDetailsRequest userOnboardingDetails,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse response = userOnboardingDetailsService.modifyUserOnboardingDetails(userOnboardingDetails);
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending modifyUserOnboardingDetails method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("UserOnboardingResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending modifyUserOnboardingDetails method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserOnboardingResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/user/{userEmail}")
	@PreAuthorize("hasAuthority('VIEW_USER')")
	public ResponseEntity<CommonResponse> fetchUserOnboardingDetailsByUserEmail(@Valid @PathVariable String userEmail,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse response = userOnboardingDetailsService.getUserOnboardingDetailsByUserId(userEmail);
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending fetchUserOnboardingDetailsByUserEmail method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("FetchUserOnboardingDetailsByUserEmailResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending fetchUserOnboardingDetailsByUserEmail method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("FetchUserOnboardingDetailsByUserEmailResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@DeleteMapping("/remove-user/{userId}")
	@PreAuthorize("hasAuthority('DELETE_USER')")
	public ResponseEntity<CommonResponse> removeUserOnboardingDetailsByUserId(@Valid @PathVariable String userId,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse response = userOnboardingDetailsService.removeUserOnBoardingDetailsByUserId(userId);
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending removeUserOnboardingDetailsByUserId method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserProfile", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending removeUserOnboardingDetailsByUserId method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DeleteUserResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/onboarded/users")
	@PreAuthorize("hasAuthority('VIEW_USER')")
	public ResponseEntity<CommonResponse> userOnboardingDetails(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse response = userOnboardingDetailsService.userOnboardingDetails();
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending userOnboardingDetails method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserProfile", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending userOnboardingDetails method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("USerOnboardingResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/request/list-view")
	@PreAuthorize("hasAnyAuthority('VIEW_ONBOARDINGMGMT','REVIEW_ONBOARDINGMGMT','APPROVE_ONBOARDINGMGMT')")
	public ResponseEntity<CommonResponse> userReviewerApproverListView(Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = userOnboardingDetailsService.userReviewerApproverListView(profile);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("*** Ending userReviewerApproverListView method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserARSListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/request/detail-view")
	@PreAuthorize("hasAnyAuthority('VIEW_ONBOARDINGMGMT','REVIEW_ONBOARDINGMGMT','APPROVE_ONBOARDINGMGMT','REVIEW_USER')")
	public ResponseEntity<CommonResponse> userReviewerApproverDetailsView(
			@RequestParam(value = "childRequestId", required = false) String childRequestId,
			@RequestParam(value = "requestId", required = false) String requestId, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = userOnboardingDetailsService.userReviewerApproverDetailsView(childRequestId,
					requestId, profile);
			return ResponseEntity.ok(response);
		} 
		catch (DataValidationException e) {
			log.error("*** Ending userReviewerApproverDetailsView method with an error *** ", e);
			return ResponseEntity.status(e.getStatusCode()).body(new CommonResponse(e.getStatusCode(),
					new Response("UserARSDetailsResponse", new ArrayList<>()), e.getMessage()));
		}
		catch (Exception e) {
			log.error("*** Ending userReviewerApproverDetailsView method with an error *** ", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserARSDetailsResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/review")
	@PreAuthorize("hasAnyAuthority('VIEW_ONBOARDINGMGMT','REVIEW_ONBOARDINGMGMT','APPROVE_ONBOARDINGMGMT','APPROVE_USER')")
	public ResponseEntity<CommonResponse> userOnboardReview(
			@RequestParam(value = "childRequestId", required = false) String childRequestId,
			@RequestParam(value = "requestId", required = false) String requestId,
			@RequestBody UserOnboardingWorkFlowRequest onboardingWorkFlowRequest, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response1 = userOnboardingDetailsService.userOnboardReview(childRequestId, requestId,
					profile, onboardingWorkFlowRequest);
			return ResponseEntity.ok(response1);
		} catch (DataValidationException e) {
			log.error("*** Ending departmentOnboardReview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("Onboarding Work flow Action Response", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending userOnboardReview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserReviewResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("excel")
	@PreAuthorize("hasAuthority('ADD_USER')")
	public ResponseEntity<CommonResponse> saveUserOnboarding(@RequestParam("usersFile") MultipartFile usersFile,
			Authentication authentication) throws IOException {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse applicaitonOnbard = userOnboardingDetailsService.saveUserOnboarding(usersFile, profile);
			return ResponseEntity.status(applicaitonOnbard.getStatus()).body(applicaitonOnbard);
		} catch (DataValidationException e) {
			log.error("*** Ending Excel saveUserOnboarding method with an error ***", e);
			return new ResponseEntity<>(
					new CommonResponse(HttpStatus.CONFLICT,
							new Response("Onboarding Work flow Action Response", new ArrayList<>()), e.getMessage()),
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			log.error("*** Ending Excel saveUserOnboarding method with an error *** ", e);
			return new ResponseEntity<>(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("Excel Upload Users", new ArrayList<>()), e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}

}
