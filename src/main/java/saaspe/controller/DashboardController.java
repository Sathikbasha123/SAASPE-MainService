package saaspe.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.model.SimilarApplicationsRequest;
import saaspe.service.DashboardService;

@RestController
@RequestMapping(value = "/api/v1/dashboard")
@ControllerLogging
public class DashboardController {

	@Autowired
	DashboardService dashboardService;

	private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

	@GetMapping("/topapps/byspend")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> getTopAppsBySpend(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse resp = dashboardService.getTopAppsBySpend();
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			log.error("**getTopAppsBySpend method ending with error**", e);
			return ResponseEntity.badRequest().body(
					new CommonResponse(HttpStatus.BAD_REQUEST, new Response(" DashBoardResponse", ""), e.getMessage()));
		}

	}

	@GetMapping("/topapps/bymetrics")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> getTopAppsByMetrics(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse resp = dashboardService.getTopAppsByMetrics();
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			log.error("**getTopAppsByMetrics method ending with error**", e);
			return ResponseEntity.badRequest().body(
					new CommonResponse(HttpStatus.BAD_REQUEST, new Response("DashBoardResponse ", ""), e.getMessage()));
		}
	}

	@GetMapping("/get/invoices")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> getInvoices(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse resp = dashboardService.getInvoices();
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			log.error("**getInvoices method ending with error**", e);
			return ResponseEntity.badRequest().body(
					new CommonResponse(HttpStatus.BAD_REQUEST, new Response(" DashBoardResponse ", ""), e.getMessage()));
		}
	}

	@GetMapping("/topapps/recent")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> getTopAppsRecentlyAdded() {
		try {
			CommonResponse resp = dashboardService.getTopAppsRecentlyAdded();
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			log.error("**getTopAppsRecentlyAdded method ending with error**", e);
			return ResponseEntity.badRequest().body(
					new CommonResponse(HttpStatus.BAD_REQUEST, new Response("  DashBoardResponse", ""), e.getMessage()));
		}
	}

	@GetMapping("/contract/details")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> getContractDetails() {
		try {
			CommonResponse resp = dashboardService.getContractDetails();
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			log.error("**getContractDetails method ending with error**", e);
			return ResponseEntity.badRequest().body(
					new CommonResponse(HttpStatus.BAD_REQUEST, new Response("DashBoardResponse  ", ""), e.getMessage()));
		}
	}

	@GetMapping("/upcoming/renewals")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> getUpcomingRenewals() {
		try {
			CommonResponse resp = dashboardService.getUpcomingRenewals();
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			log.error("**getUpcomingRenewals method ending with error**", e);
			return ResponseEntity.badRequest().body(
					new CommonResponse(HttpStatus.BAD_REQUEST, new Response(" DashBoard Response", ""), e.getMessage()));
		}
	}

	@GetMapping("/view")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> getDashboardView(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse response = dashboardService.getDashboardView();
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("*** ending error with getDashboardView ***", e);
			return ResponseEntity.badRequest().body(
					new CommonResponse(HttpStatus.BAD_REQUEST, new Response("DashBoard Response", ""), e.getMessage()));
		}
	}

	@PostMapping("/similar-apps")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> getSimilarApplications(
			@RequestBody SimilarApplicationsRequest similarapplicationsrequest,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse response = dashboardService.getSimilarApplications(similarapplicationsrequest);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("*** ending error with getSimilarApplications ***", e);
			return ResponseEntity.badRequest().body(
					new CommonResponse(HttpStatus.BAD_REQUEST, new Response("DashBoard Response ", ""), e.getMessage()));
		}
	}

	@GetMapping("/request-tracking/list-view")
	@PreAuthorize("hasAuthority('VIEW_REQUESTMGMT')")
	public ResponseEntity<CommonResponse> getRequestTracking(
			@RequestParam(name = "category", required = true) String category,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse request = dashboardService.getRequestTracking(category);
			return ResponseEntity.ok(request);
		} catch (DataValidationException e) {
			log.error("*** Ending getRequestTracking method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("requestTrackingResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending error with getRequestTracking ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("requestTrackingResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/analytics/view")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> dashBoardTopCardAnalystics() {
		try {
			CommonResponse request = dashboardService.dashBoardTopCardAnalystics();
			return ResponseEntity.ok(request);
		} catch (DataValidationException e) {
			log.error("*** Ending dashBoardTopCardAnalystics method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DashBoardAnalystics Response", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending error with dashBoardTopCardAnalystics ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DashBoard AnalysticsResponse", ""), e.getMessage()));
		}
	}

	@GetMapping("/analytics/department/budget")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> dashBoardDepartmentBudgetAnalystics() {
		try {
			CommonResponse request = dashboardService.dashBoardDepartmentBudgetAnalystics();
			return ResponseEntity.ok(request);
		} catch (DataValidationException e) {
			log.error("*** Ending dashBoardDepartmentBudgetAnalystics method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(" DashBoardAnalysticsResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending error with dashBoardDepartmentBudgetAnalystics ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DashBoardAnalysticsResponse ", ""), e.getMessage()));
		}
	}

	@GetMapping("/analytics/department/expense")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> dashBoardExpenseByDepartmentAnalystics() {
		try {
			CommonResponse request = dashboardService.dashBoardExpenseByDepartmentAnalystics();
			return ResponseEntity.ok(request);
		} catch (DataValidationException e) {
			log.error("*** Ending dashBoardExpenseByDepartmentAnalystics method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("  DashBoardAnalysticsResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending error with dashBoardExpenseByDepartmentAnalystics ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(" DashBoard AnalysticsResponse", ""), e.getMessage()));
		}
	}

	@GetMapping("/analytics/spend")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> dashboardSpendHistory() {
		try {
			CommonResponse request = dashboardService.dashboardSpendHistory();
			return ResponseEntity.ok(request);
		} catch (DataValidationException e) {
			log.error("*** Ending dashboardSpendHistory method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DashBoardAnalysticsResponse  ", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending error with dashboardSpendHistory ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(" DashBoard Analystics Response", ""), e.getMessage()));
		}
	}

	@GetMapping("/usage/trend")
	@PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
	public ResponseEntity<CommonResponse> usageTrends() {
		try {
			CommonResponse request = dashboardService.usageTrends();
			return ResponseEntity.ok(request);
		} catch (DataValidationException e) {
			log.error("*** Ending usageTrends method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST, new Response("usageTrendsResponse", ""),
					e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending error with dashboardSpendHistory ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST, new Response("usageTrendsResponse", ""),
					e.getMessage()));
		}
	}

}
