package saaspe.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import javax.mail.MessagingException;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import freemarker.template.TemplateException;
import saaspe.aspect.ControllerLogging;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.DeptOnboardingWorkFlowRequest;
import saaspe.model.ListOfCreateDeptartmentRequest;
import saaspe.model.Response;
import saaspe.repository.DepartmentOnboardingRepository;
import saaspe.repository.SequenceGeneratorRepository;
import saaspe.service.DepartmentService;

@ControllerLogging
@RestController
@RequestMapping("/api/v1/department")
public class DepartmentController {

	@Autowired
	DepartmentService departmentService;

	@Autowired
	DepartmentOnboardingRepository departmentOnboardingRepository;

	@Autowired
	SequenceGeneratorRepository sequenceGeneratorRepository;

	private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);

	@GetMapping("/list")
	@PreAuthorize("hasAuthority('VIEW_DEPARTMENT')")
	public ResponseEntity<CommonResponse> getDepartmentListView(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse deptListView = departmentService.getDepartmentListView();
			return ResponseEntity.ok(deptListView);
		} catch (Exception e) {
			log.error("*** Ending getDepartmentListView method with an error ***", e);
			return ResponseEntity.badRequest().body(
					new CommonResponse(HttpStatus.BAD_REQUEST, new Response("DepartmentListView", ""), e.getMessage()));
		}
	}

	@GetMapping("/overview")
	@PreAuthorize("hasAuthority('VIEW_DEPARTMENT')")
	public ResponseEntity<CommonResponse> getDepartmentOverview(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID,
			@RequestParam(value = "departmentId", required = true) String departmentId,
			@RequestParam(value = "category", required = false) String request) {
		try {
			CommonResponse deptListView = departmentService.getDepartmentOverview(departmentId, request);
			return ResponseEntity.ok(deptListView);
		} catch (DataValidationException e) {
			log.error("*** Ending getDepartmentOverview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DepartmentOverview", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getDepartmentOverview method with an error ***", e);
			return ResponseEntity.badRequest().body(
					new CommonResponse(HttpStatus.BAD_REQUEST, new Response("DepartmentListView", ""), e.getMessage()));
		}
	}

	@PostMapping("excel")
	@PreAuthorize("hasAuthority('ADD_DEPARTMENT')")
	public ResponseEntity<CommonResponse> saveDepartmentOnboarding(
			@RequestParam("departmentFile") MultipartFile departmentFile, Authentication authentication)
			throws IOException {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse deptListView = departmentService.saveDepartmentOnboarding(departmentFile, profile);
			return ResponseEntity.ok(deptListView);
		} catch (DataValidationException e) {
			log.error("*** Ending save Department Onboarding method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("Onboarding Workflow Action Response", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending saveDepartmentOnboarding method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("Excel Upload Departments", new ArrayList<>()), e.getLocalizedMessage()));
		}
	}

	@PostMapping("/single/onboard")
	@PreAuthorize("hasAuthority('ADD_DEPARTMENT')")
	public ResponseEntity<CommonResponse> departmentSingleOnboarding(
			@RequestBody ListOfCreateDeptartmentRequest createDeptartmentRequest, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse requestId = departmentService.departmentSingleOnboarding(createDeptartmentRequest, profile);
			return ResponseEntity.ok(requestId);
		} catch (DataValidationException e) {
			log.error("*** Ending saveDepartmentOnboarding method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("Onboarding Work flow Action Response", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending departmentSingleOnboarding method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DepartmentOnboardingResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/request/list-view")
	@PreAuthorize("hasAuthority('VIEW_ONBOARDINGMGMT')")
	public ResponseEntity<CommonResponse> departmentReviewerApproverListView(Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = departmentService.departmentReviewerApproverListView(profile);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("*** Ending departmentReviewerApproverListView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DepartmentARSListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/request/detail-view")
	@PreAuthorize("hasAnyAuthority('VIEW_ONBOARDINGMGMT','REVIEW_DEPARTMENT')")
	public ResponseEntity<CommonResponse> departmentReviewerApproverDetailsView(
			@RequestParam(value = "childRequestId", required = false) String childRequestId,
			@RequestParam(value = "requestId", required = false) String requestId, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = departmentService.departmentReviewerApproverDetailsView(childRequestId, requestId,
					profile);
			return ResponseEntity.ok(response);
		} 
		catch (DataValidationException e) {
			log.error("*** Ending departmentReviewerApproverDetailsView method with an error ***", e);
			return ResponseEntity.status(e.getStatusCode()).body(new CommonResponse(e.getStatusCode(),
					new Response("DepartmentARSDetailsResponse", new ArrayList<>()), e.getMessage()));
		}
		catch (Exception e) {
			log.error("*** Ending departmentReviewerApproverDetailsView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DepartmentARSDetailsResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/review")
	@PreAuthorize("hasAnyAuthority('REVIEW_ONBOARDINGMGMT','APPROVE_ONBOARDINGMGMT','APPROVE_DEPARTMENT')")
	public ResponseEntity<CommonResponse> departmentOnboardReview(
			@RequestParam(value = "childRequestId", required = false) String childRequestId,
			@RequestParam(value = "requestId", required = false) String requestId,
			@RequestBody DeptOnboardingWorkFlowRequest onboardingWorkFlowRequest, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonresponse = departmentService.departmentOnboardReview(childRequestId, requestId,
					profile, onboardingWorkFlowRequest);
			return ResponseEntity.ok(commonresponse);
		} catch (DataValidationException e) {
			log.error("*** Ending departmentOnboardReview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("Onboarding Work flow Action Response", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending departmentOnboardReview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DepartmentReviewResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/user/unmapped")
	@PreAuthorize("hasAuthority('VIEW_USER')")
	public ResponseEntity<CommonResponse> departmentUserListWithoutLicenseMapped(
			@RequestParam(value = "licenseId") String licenseId,
			@RequestParam(value = "departmentId") String departmentId) {
		try {
			CommonResponse commonresponse = departmentService.departmentUserListWithoutLicenseMapped(licenseId,
					departmentId);
			return ResponseEntity.ok(commonresponse);
		} catch (DataValidationException e) {
			log.error("*** Ending saveApplicatoinOnboarding method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("departmentUsersResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending departmentReviewerApproverDetailsView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DepartmentListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/usage/analytics")
	public ResponseEntity<CommonResponse> deptApplicationUsage(@RequestParam String deptId) {
		try {
			CommonResponse commonResponse = departmentService.deptApplicationUsage(deptId);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException dve) {
			log.error("*** ending deptApplication Usage method with error ***", dve);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.OK,
					new Response("DeptApplicationUsageRespone", new ArrayList<>()), dve.getMessage()));
		} catch (Exception e) {
			log.error("*** ending deptApplicationUsage method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DepartmentResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/spend/analytics")
	public ResponseEntity<CommonResponse> deptSpendAnalytics(@RequestParam String deptId) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = departmentService.deptSpendAnalytics(deptId);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException dve) {
			log.error("*** ending deptApplicationUsage method with error ***", dve);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.OK,
					new Response("DeptSpendAnalyticsResponse", new ArrayList<>()), dve.getMessage()));
		} catch (Exception e) {
			log.error("*** ending deptSpendAnalytics method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DepartmentAnalyticsResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/budget/analytics")
	public ResponseEntity<CommonResponse> deptBudgetAnalytics(@RequestParam String deptId) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = departmentService.deptBudgetAnalytics(deptId);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException dve) {
			log.error("*** ending deptBudgetAnalytics method with error ***", dve);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.OK,
					new Response("DeptbudgetAnalyticsResponse", ""), dve.getMessage()));
		} catch (Exception e) {
			log.error("*** ending deptBudgetAnalytics method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DepartmentAnalyticsResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@Scheduled(cron = "0 0 1 * * *", zone = "Asia/Kuala_Lumpur")
	@GetMapping("/d")
	public ResponseEntity<CommonResponse> sendBudgetEmail() throws IOException, MessagingException, TemplateException {
		CommonResponse commonResponse = new CommonResponse();
		try {
			log.info("==== sendBudgetEmail method started====" + LocalDate.now(ZoneId.systemDefault()));
			commonResponse = departmentService.sendBudgetEmail();
			log.info("==== sendBudgetEmail method started====" + LocalDate.now(ZoneId.systemDefault()));
			return ResponseEntity.ok(commonResponse);
		} catch (InterruptedException e) {
			log.error("*** ending sendBudgetEmail method with error ***", e);
			Thread.currentThread().interrupt();
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DepartmentEmailResponse", new ArrayList<>()), e.getMessage()));
		}
	}
}
