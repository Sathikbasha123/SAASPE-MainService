package saaspe.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.constant.Constant;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.ApplicationContractDetailsUpdateRequest;
import saaspe.model.CommonResponse;
import saaspe.model.ContractOnboardingRequest;
import saaspe.model.DeptOnboardingWorkFlowRequest;
import saaspe.model.Response;
import saaspe.service.ContractService;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/application/contract")
public class ContractController {

	@Autowired
	private ContractService contractService;

	private static final Logger log = LoggerFactory.getLogger(ContractController.class);

	@GetMapping("/list-view")
	@PreAuthorize("hasAuthority('VIEW_CONTRACT')")
	public ResponseEntity<CommonResponse> getContractsListView() {
		try {
			CommonResponse applicationDetailsResponse = contractService.getContractsListView();
			return new ResponseEntity<>(applicationDetailsResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending getContractsListView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("contractsListResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getContractsListView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ContractListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PutMapping("/modify-contract")
	@PreAuthorize("hasAuthority('EDIT_CONTRACT')")
	public ResponseEntity<CommonResponse> modifyApplicationContractDetails(@Valid @RequestParam String contractId,
			@RequestBody ApplicationContractDetailsUpdateRequest updateRequest,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse applicationDetailsResponse = contractService.modifyApplicationContractDetails(contractId,
					updateRequest);
			return new ResponseEntity<>(applicationDetailsResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending modifyApplicationContractDetails method with an error ***", e);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} catch (Exception e) {
			log.error("*** Ending modifyApplicationContractDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UpdateContractResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/overview")
	@PreAuthorize("hasAuthority('VIEW_CONTRACT')")
	public ResponseEntity<CommonResponse> applicationContractDetailView(
			@RequestParam(value = "contractId", required = true) String contractId,
			@RequestParam(value = "category", required = false) String category,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse applicationDetailsResponse = contractService.getApplicationContractDetailView(contractId,
					category);
			return ResponseEntity.status(HttpStatus.OK).body(applicationDetailsResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending applicationContractDetailView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.CONTRACT_DETAILS_OVERVIEW_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending applicationContractDetailView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ContractDetailsResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kuala_Lumpur")
	public ResponseEntity<CommonResponse> upCommingContractRenewalReminderEmail() {
		try {
			log.info("==== upCommingContractRenewalReminderEmail method started====" + LocalDate.now(ZoneId.systemDefault()));
			CommonResponse commonResponse = contractService.upCommingContractRenewalReminderEmail();
			log.info("==== upCommingContractRenewalReminderEmail method success====" + LocalDate.now(ZoneId.systemDefault()));
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending upCommingContractRenewalReminderEmail method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.CONTRACT_DETAILS_OVERVIEW_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending upCommingContractRenewalReminderEmail method with an error ***", e);
			Thread.currentThread().interrupt();
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ContractRenewalResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/{applicationId}/addcontract")
	@PreAuthorize("hasAuthority('ADD_CONTRACT')")
	public ResponseEntity<CommonResponse> addApplicationContract(
			@PathVariable(value = "applicationId") String applicationId,
			@RequestBody ContractOnboardingRequest contractOnboardingRequest, Authentication authentication,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse applicationDetailsResponse = contractService
					.addApplicationContract(contractOnboardingRequest, applicationId, profile);
			return ResponseEntity.status(HttpStatus.OK).body(applicationDetailsResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending addApplicationContract method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("addApplicationContractResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending addApplicationContract method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("AddContractResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/list")
	@PreAuthorize("hasAuthority('VIEW_CONTRACT')")
	public ResponseEntity<CommonResponse> getContractsByApplicationId(
			@RequestParam(required = true) String applicationId) {
		try {
			CommonResponse commonResponse = contractService.getContractsByApplicationId(applicationId);
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending getContractsByApplicationId method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.CONTRACT_DETAILS_OVERVIEW_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getContractsByApplicationId method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ViewContractResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/request/list-view")
	@PreAuthorize("hasAnyAuthority('REVIEW_ONBOARDINGMGMT','APPROVE_ONBOARDINGMGMT','VIEW_ONBOARDINGMGMT')")
	public ResponseEntity<CommonResponse> contractReviewerApproverListView(Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonResponse = contractService.contractReviewerApproverListView(profile);
			return ResponseEntity.ok(commonResponse);
		} catch (Exception e) {
			log.error("*** Ending contractReviewerApproverListView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ContractARSListResponse", new ArrayList<>()), e.getMessage()));
		}

	}

	@PostMapping("/review")
	@PreAuthorize("hasAnyAuthority('REVIEW_ONBOARDINGMGMT','APPROVE_ONBOARDINGMGMT','VIEW_ONBOARDINGMGMT')")
	public ResponseEntity<CommonResponse> contractOnboardReview(
			@RequestParam(value = "requestId", required = true) String requestId,
			@RequestBody DeptOnboardingWorkFlowRequest onboardingWorkFlowRequest, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonresponse = contractService.contractOnboardReview(requestId, profile,
					onboardingWorkFlowRequest);
			return ResponseEntity.ok(commonresponse);
		} catch (DataValidationException e) {
			log.error("*** Ending applicaitonOnboardReview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.ONBOARDING_WORK_FLOW_ACTION_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending applicaitonOnboardReview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ContractReviewResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/request/detail-view")
	@PreAuthorize("hasAnyAuthority('REVIEW_ONBOARDINGMGMT','APPROVE_ONBOARDINGMGMT','VIEW_ONBOARDINGMGMT')")
	public ResponseEntity<CommonResponse> applicationOnboardDetailView(
			@RequestParam(value = "requestId", required = true) String requestId, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = contractService.contractReviewerApproverDetailsView(requestId, profile);
			return ResponseEntity.ok(response);
		}
		catch (DataValidationException e) {
			log.error("*** Ending applicationOnboardDetailView method with an error ***", e);
			return ResponseEntity.status(e.getStatusCode()).body(new CommonResponse(e.getStatusCode(),
					new Response("ContractARSDetailsResponse", new ArrayList<>()), e.getMessage()));
		}
		catch (Exception e) {
			log.error("*** Ending applicationOnboardDetailView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ContractARSDetailsResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@Scheduled(cron = "0 */30 * ? * *")
	public ResponseEntity<CommonResponse> updateContractStatus() {
		try {
			log.info("==== updateContractStatus method started====" + LocalDate.now(ZoneId.systemDefault()));
			CommonResponse commonResponse = contractService.updateContractStatus();
			log.info("==== updateContractStatus method success====" + LocalDate.now(ZoneId.systemDefault()));
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending updateContractStatus method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.CONTRACT_DETAILS_OVERVIEW_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending updateContractStatus method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UpdateContractResponse", new ArrayList<>()), e.getMessage()));
		}
	}
}
