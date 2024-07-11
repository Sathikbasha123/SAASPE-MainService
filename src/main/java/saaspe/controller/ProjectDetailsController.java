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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import freemarker.template.TemplateException;
import saaspe.aspect.ControllerLogging;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.ProjectDetailsUpdateRequest;
import saaspe.model.ProjectWorkflowReviewRequest;
import saaspe.model.Response;
import saaspe.model.SingleProjectOnboardingRequest;
import saaspe.service.ProjectDetailsService;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/project")
public class ProjectDetailsController {

	@Autowired
	private ProjectDetailsService projectDetailsService;

	private static final Logger log = LoggerFactory.getLogger(ProjectDetailsController.class);

	@PostMapping("/single/onboard")
	@PreAuthorize("hasAuthority('ADD_PROJECT')")
	public ResponseEntity<CommonResponse> singleProjectgOnboarding(
			@RequestBody SingleProjectOnboardingRequest projectOnboardingRequest, Authentication authentication) {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			commonResponse = projectDetailsService.projectSingleOnboarding(projectOnboardingRequest, profile);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			commonResponse.setMessage(e.getMessage());
			response.setAction("project OnboardingRequest");
			response.setData(new ArrayList<>());
			commonResponse.setResponse(response);
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			log.error("***  Ending singleProjectgOnboarding method with an error ***", e);
			return ResponseEntity.badRequest().body(commonResponse);
		} catch (Exception e) {
			log.error("*** Ending  singleProjectgOnboarding method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Project Response", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("excel")
	@PreAuthorize("hasAuthority('ADD_DEPARTMENT')")
	public ResponseEntity<CommonResponse> saveDepartmentOnboarding(@RequestParam("departmentId") String departmentId,
			@RequestParam("projectFile") MultipartFile projectFile, Authentication authentication) throws IOException {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse deptListView = projectDetailsService.projectMultipleOnboaring(projectFile, profile,
					departmentId);
			return ResponseEntity.status(deptListView.getStatus()).body(deptListView);
		} catch (DataValidationException e) {
			log.error("*** Ending saveProjectOnboaring method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("Onboarding Work flow Action Response", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending saveProjectOnboarding method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("Excel Upload Departments", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/request/list-view")
	@PreAuthorize("hasAuthority('VIEW_PROJECT')")
	public ResponseEntity<CommonResponse> projectReviewerApproverListView(Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonResponse = projectDetailsService.projectReviewerApproverListView(profile);
			return ResponseEntity.ok(commonResponse);
		} catch (Exception e) {
			log.error("*** Ending departmentReviewerApproverListView method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("projectOnboarding Request", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/request/detail-view")
	@PreAuthorize("hasAnyAuthority('VIEW_PROJECT','REVIEW_PROJECT')")
	public ResponseEntity<CommonResponse> projectReviewerApproverDetailsView(
			@RequestParam(value = "childRequestId", required = false) String childRequestId,
			@RequestParam(value = "requestId", required = false) String requestId, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonResponse = projectDetailsService.projectReviewerApproverDetailsView(childRequestId,
					requestId, profile);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending departmentReviewerApproverDetailsView method with an error ***", e);
			return ResponseEntity.status(e.getStatusCode()).body(new CommonResponse(e.getStatusCode(),
					new Response("projectOnboardingResponse", new ArrayList<>()), e.getMessage()));
		}
		catch (Exception e) {
			log.error("*** Ending departmentReviewerApproverDetailsView method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("projectOnboardingResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/review")
	@PreAuthorize("hasAnyAuthority('REVIEW_ONBOARDINGMGMT','APPROVE_ONBOARDINGMGMT','VIEW_ONBOARDINGMGMT','APPROVE_PROJECT')")
	public ResponseEntity<CommonResponse> projectOnboardReview(
			@RequestParam(value = "childRequestId", required = false) String childRequestId,
			@RequestParam(value = "requestId", required = false) String requestId,
			@RequestBody ProjectWorkflowReviewRequest onboardingWorkFlowRequest, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonresponse = projectDetailsService.projectOnboardReview(childRequestId, requestId,
					profile, onboardingWorkFlowRequest);
			return ResponseEntity.ok(commonresponse);
		} catch (DataValidationException e) {
			log.error("*** Ending projectOnboardReview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("Onboarding Work flow Action Response", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending projectOnboardReview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(" ProjectResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/detailview")
	@PreAuthorize("hasAuthority('VIEW_PROJECT')")
	public ResponseEntity<CommonResponse> projectDetailsView(
			@RequestParam(value = "projectId", required = true) String projectId, Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonresponse = projectDetailsService.projectDetailsView(projectId, profile);
			return ResponseEntity.ok(commonresponse);
		} catch (DataValidationException e) {
			log.error("***  Ending projectDetailsView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(" projectDetailViewResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending  projectDetailsView method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("projectDetailViewResponse ", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/projectdetails")
	@PreAuthorize("hasAuthority('VIEW_PROJECT')")
	public ResponseEntity<CommonResponse> projectDetailsByDeptId(
			@RequestParam(value = "departmentId", required = true) String departmentId) {
		try {
			CommonResponse commonresponse = projectDetailsService.getProjectDetailsByDeptId(departmentId);
			return ResponseEntity.ok(commonresponse);
		} catch (DataValidationException e) {
			log.error("*** Ending projectDetailsView method with an error  ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("project DetailViewResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending projectDetailsByDeptId method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("project Detail ViewResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/update")
	@PreAuthorize("hasAuthority('EDIT_PROJECT')")
	public ResponseEntity<CommonResponse> projectUpdate(@RequestBody ProjectDetailsUpdateRequest projectUpdateRequest,
			Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse dettailsResponse = projectDetailsService.projectApplicationUpdate(projectUpdateRequest,
					profile);
			return new ResponseEntity<>(dettailsResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending singleProjectgOnboarding method with an error  ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("projectApplicationsAddResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending  singleProjectgOnboarding method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("projectApplicationsAddResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/list")
	@PreAuthorize("hasAuthority('VIEW_PROJECT')")
	public ResponseEntity<CommonResponse> getProjectDetails() {
		try {
			log.info("*** Enter into getProjectDetails method ***");
			CommonResponse dettailsResponse = projectDetailsService.getProjectDetails();
			return ResponseEntity.ok(dettailsResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending getProjectDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("ProjectDetailsListResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getProjectDetails method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(" projectOnboardingRequest", new ArrayList<>()), e.getMessage()));

		}
	}

	@GetMapping("/spend/analytics")
	public ResponseEntity<CommonResponse> projectSpendAnalytics(@RequestParam String projectId) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = projectDetailsService.projectSpendAnalytics(projectId);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException dve) {
			log.error("*** ending deptApplicationUsage method with error ***", dve);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.OK,
					new Response("ProjectSpendAnalyticsResponse", ""), dve.getMessage()));
		} catch (Exception e) {
			log.error("*** ending projectSpendAnalytics method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ProjectResponse ", new ArrayList<>()), e.getMessage()));
		}
	}

	@Scheduled(cron = "0 0 1 * * *", zone = "Asia/Kuala_Lumpur")
	public ResponseEntity<CommonResponse> sendBudgetEmail() throws IOException, TemplateException, MessagingException {
		CommonResponse commonResponse = new CommonResponse();
		try {
			log.info("==== sendBudgetEmail method started====" + LocalDate.now(ZoneId.systemDefault()));
			commonResponse = projectDetailsService.sendBudgetEmail();
			log.info("==== sendBudgetEmail method started====" + LocalDate.now(ZoneId.systemDefault()));
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException dve) {
			log.error("*** ending sendBudgetEmail method with error ***", dve);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.OK,
					new Response("ProjectbudgetEmailResponse", ""), dve.getMessage()));
		} catch (InterruptedException e) {
			log.error("*** ending sendBudgetEmail method with error ***", e);
			Thread.currentThread().interrupt();
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ProjectbudgetEmailResponse", new ArrayList<>()), e.getMessage()));
		}
	}
	
	@Scheduled(cron = "0 0 3 * * *", zone = "Asia/Kuala_Lumpur")
	public ResponseEntity<CommonResponse> updateProjectStatus() {
		CommonResponse commonResponse = new CommonResponse();
		try {
			log.info("==== UpdateProjectStatus method started====  {}" , LocalDate.now(ZoneId.systemDefault()));
			commonResponse = projectDetailsService.updateProjectStatus();
			log.info("==== UpdateProjectStatus method Ended====  {}" , LocalDate.now(ZoneId.systemDefault()));
			return ResponseEntity.ok(commonResponse);
		} catch (Exception exe) {
			log.error("*** ending UpdateProjectStatus method with error *** {}", exe.getMessage());
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.OK,
					new Response("UpdateProjectStatusResponse", ""), exe.getMessage()));
		}
	}
	
	

}
