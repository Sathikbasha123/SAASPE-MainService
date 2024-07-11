package saaspe.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import saaspe.aspect.ControllerLogging;
import saaspe.constant.Constant;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.BudgetRequest;
import saaspe.model.CloudOnboardRequest;
import saaspe.model.CommonResponse;
import saaspe.model.OptimizeRequest;
import saaspe.model.OptimizeRequestAws;
import saaspe.model.Response;
import saaspe.model.UserSubscriptionRequest;
import saaspe.service.MultiCloudService;

@RestController
@RequestMapping("api/cloud")
@ControllerLogging
public class MultiCloudController {

	@Autowired
	private MultiCloudService multiCloudService;

	private static final Logger log = LoggerFactory.getLogger(MultiCloudController.class);

	@PostMapping("/subscribe")
	@PreAuthorize("hasAuthority('ADD_MULTICLOUD')")
	public ResponseEntity<CommonResponse> cloudSubscription(@RequestBody UserSubscriptionRequest request) {
		try {
			CommonResponse response = multiCloudService.addUserSubscriptionDetails(request);
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending addCloudService method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending addCloudService method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/details")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getDetails() {
		try {
			CommonResponse response = multiCloudService.getDetails();
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending getDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("top/spend/history")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getSpendingDetails() {
		try {
			CommonResponse response = multiCloudService.getSpendingHistory();
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending getSpendingDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getSpendingDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/recent/spend/history")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getRecentSpendingDetails() {
		try {
			CommonResponse response = multiCloudService.getRecentSpendingHistory();
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending getRecentSpendingDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getRecentSpendingDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/top/expensive/services")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getTotalSpendBasedOnServiceName() {
		try {
			CommonResponse response = multiCloudService.getTotalSpendBasedOnServiceName();
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending getTotalSpendBasedOnServiceName method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getTotalSpendBasedOnServiceName method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/overview")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getCountBasedOnSubscriptionType(HttpServletRequest request) {
		try {
			CommonResponse response = multiCloudService.getCountBasedOnSubscriptionType(request);
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending getCountBasedOnSubscriptionType method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getCountBasedOnSubscriptionType method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/cost/bycloud/vendor")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getQuaterlyAmountByService() {
		try {
			CommonResponse response = multiCloudService.getQuaterlyAmountByService();
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending getTotalAmountBasedOnServiceName method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getTotalAmountBasedOnServiceName method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/spend/history")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getGraphSpendingDetails(@RequestParam String renewalType) {
		try {
			CommonResponse response = multiCloudService.spendingHistory(renewalType);
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending getGraphSpendingDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getGraphSpendingDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/budget")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getMulticloudBudgetDetails(@RequestParam String category) {
		try {
			CommonResponse response = multiCloudService.getBudgetsByVendor(category);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("*** Ending getMulticloudBudgetDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/subscriptions")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getSubscriptions() {
		try {
			CommonResponse commonResponse = multiCloudService.getSubscriptions();
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending getSubscriptions method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getSubscriptions method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/forecast")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getAzureForecastData(
			@RequestParam(value = "subscriptionId", required = false) String subscriptionId,
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
			@RequestParam(value = "category", required = false) String category) {
		try {
			CommonResponse commonResponse = multiCloudService.getAzureForecastData(subscriptionId, startDate, endDate,
					category);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending getAzureForecastData method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getAzureForecastData method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/recommendation")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getRecommendations(@RequestParam String category) {
		try {
			CommonResponse commonResponse = multiCloudService.getRecommendations(category);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending getRecommendations method with an error ***", e);
			return ResponseEntity.status(e.getStatusCode()).body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getRecommendations method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/resources")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getResources(@RequestParam String category) {
		try {
			CommonResponse commonResponse = multiCloudService.getResources(category);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending getResources method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("azureResourceListResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getResources method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("azureResourceListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/cost/yearly/monthly")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getCostSpendPerYearlyAndMonthly() {
		try {
			CommonResponse commonResponse = multiCloudService.getCostSpendPerYearlyAndMonthly();
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending getCostSpendPerYearlyAndMonthly method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("CostSpendResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getCostSpendPerYearlyAndMonthly method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("CostSpendResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/optimize/azure/email")
	@PreAuthorize("hasAnyAuthority('SUPER_ADMIN','REVIEWER','APPROVER','CONTRIBUTOR')")
	public ResponseEntity<CommonResponse> optimizeEmailTriggerAzure(@RequestBody OptimizeRequest optimizeRequest) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = multiCloudService.optimizeEmailTriggerAzure(optimizeRequest);
			return ResponseEntity.ok(commonResponse);
		} catch (Exception e) {
			commonResponse.setMessage(e.getMessage());
			log.error("*** Ending optimizeEmailTriggerAazure method with an error ***", e);
			return ResponseEntity.badRequest().body(commonResponse);
		}
	}

	@PostMapping("/optimize/aws/email")
	@PreAuthorize("hasAnyAuthority('SUPER_ADMIN','REVIEWER','APPROVER','CONTRIBUTOR')")
	public ResponseEntity<CommonResponse> optimizeEmailTriggerAws(@RequestBody OptimizeRequestAws optimizeRequest) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = multiCloudService.optimizeEmailTriggerAws(optimizeRequest);
			return ResponseEntity.ok(commonResponse);
		} catch (Exception e) {
			commonResponse.setMessage(e.getMessage());
			log.error("*** Ending optimizeEmailTriggerAws method with an error ***", e);
			return ResponseEntity.badRequest().body(commonResponse);
		}
	}

	@Scheduled(cron = "0 30 8 * * *", zone = "Asia/Kuala_Lumpur")
	@GetMapping("/a")
	public ResponseEntity<CommonResponse> budjetEmailTriggerAzure() {
		CommonResponse commonResponse = new CommonResponse();
		try {
			log.info("==== budjetEmailTriggerAzure method started====" + LocalDate.now(ZoneId.systemDefault()));
			commonResponse = multiCloudService.budgetEmailTriggerAzure();
			log.info("==== budjetEmailTriggerAzure method started====" + LocalDate.now(ZoneId.systemDefault()));
			return ResponseEntity.ok(commonResponse);
		} catch (Exception e) {
			commonResponse.setMessage(e.getMessage());
			log.error("*** Ending budjetEmailTriggerAzure method with an error ***", e);
			Thread.currentThread().interrupt();
			return ResponseEntity.badRequest().body(commonResponse);
		}
	}

	@Scheduled(cron = "0 30 8 * * *", zone = "Asia/Kuala_Lumpur")
	@GetMapping("/b")
	public ResponseEntity<CommonResponse> budjetEmailTriggerAws() {
		CommonResponse commonResponse = new CommonResponse();
		try {
			log.info("==== budjetEmailTriggerAws method started====" + LocalDate.now(ZoneId.systemDefault()));
			commonResponse = multiCloudService.budgetEmailTriggerAws();
			log.info("==== budjetEmailTriggerAws method started====" + LocalDate.now(ZoneId.systemDefault()));
			return ResponseEntity.ok(commonResponse);
		} catch (Exception e) {
			commonResponse.setMessage(e.getMessage());
			log.error("*** Ending budjetEmailTriggerAws method with an error ***", e);
			Thread.currentThread().interrupt();
			return ResponseEntity.badRequest().body(commonResponse);
		}
	}

	@GetMapping("/vendors")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getAllSupportedVendors() {
		try {
			CommonResponse commonResponse = multiCloudService.getAllSupportedVendors();
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending getAllSupportedVendors method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.CLOUD_VENDORS_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getAllSupportedVendors method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.CLOUD_VENDORS_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/onboard")
	@PreAuthorize("hasAuthority('ADD_MULTICLOUD')")
	public ResponseEntity<CommonResponse> cloudOnboard(@RequestBody CloudOnboardRequest cloudOnboardRequest,
			Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonResponse = multiCloudService.cloudOnboard(cloudOnboardRequest, profile);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending cloudOnboard method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.ONBOARDING_WORK_FLOW_ACTION_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending cloudOnboard method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}

	@GetMapping("/integrated")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getIntegratedClouds() {
		try {
			CommonResponse commonResponse = multiCloudService.getIntegratedClouds();
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending getIntegratedClouds method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.CLOUD_VENDORS_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getIntegratedClouds method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response(Constant.CLOUD_VENDORS_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@DeleteMapping("/remove/{cloudId}")
	@PreAuthorize("hasAuthority('DELETE_MULTICLOUD')")
	public ResponseEntity<CommonResponse> removeCloudVendor(@PathVariable String cloudId) {
		try {
			CommonResponse commonResponse = multiCloudService.removeCloudVendor(cloudId);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("cloudDeleteResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("cloudDeleteResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/budget/create")
	@PreAuthorize("hasAuthority('CREATE_BUDGET')")
	public ResponseEntity<CommonResponse> createAzureBudget(@RequestBody BudgetRequest budgetRequest) {
		try {
			CommonResponse commonResponse = multiCloudService.getCreateBudget(budgetRequest);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending createAzureBudget method with an error  ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("createAzureBudget Response", new ArrayList<>()), e.getMessage()));
		} catch (RestClientException e) {
			log.error("*** Ending createAzureBudget method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response("createAzureBudgetResponse", new ArrayList<>()),
							"Budget cannot be created at the moment"));
		} catch (Exception e) {
			log.error("*** Ending createAzureBudget method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("createAzureBudgetResponse", new ArrayList<>()), e.getMessage()));
		}
	}
	
	@GetMapping("/monthly/spendinghistory")
	@PreAuthorize("hasAuthority('VIEW_MULTICLOUD')")
	public ResponseEntity<CommonResponse> getMonthlySpendingHistory() {
		try {
			CommonResponse response = multiCloudService.getMonthlySpendingHistory();
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** Ending getMonthlySpendingHistory method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getMonthlySpendingHistory method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.MULTI_CLOUD_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

}