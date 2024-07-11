package saaspe.controller;

import java.util.ArrayList;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.service.SubscriptionService;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/application/subscription")
public class SubscriptionController {

	@Autowired
	private SubscriptionService subscriptionService;

	private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

	@GetMapping("/list")
	@PreAuthorize("hasAuthority('VIEW_SUBSCRIPTION')")
	public ResponseEntity<CommonResponse> getApplicationSubscriptionDetails(
			@Valid @RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			log.info("*** Enter into getApplicationSubscriptionDetails method ***");
			CommonResponse commonResponse = subscriptionService.getApplicationSubscriptionDetails();
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending getApplicationSubscriptionDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("subscriptionListResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getApplicationSubscriptionDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("subscriptionListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/list-view")
	@PreAuthorize("hasAuthority('VIEW_SUBSCRIPTION')")
	public ResponseEntity<CommonResponse> getApplicationSubscriptionDetailsListview(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			log.info("*** Enter into getApplicationSubscriptionDetailsListview method ***");
			CommonResponse applicationSubscriptionDetailsResponse = subscriptionService
					.getApplicationSubscriptionDetailsListview();
			return new ResponseEntity<>(applicationSubscriptionDetailsResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending getApplicationSubscriptionDetailsListview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("RenewalsListResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getApplicationSubscriptionDetailsBySubscriptionEmail method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("RenewalsListResponse", new ArrayList<>()), e.getMessage()));
		}
	}
	
	@GetMapping("/lookup")
	@PreAuthorize("hasAuthority('VIEW_SUBSCRIPTION')")
	public ResponseEntity<CommonResponse> getListOfSubscriptions(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			log.info("*** Enter into getListOfSubscriptions method ***");
			CommonResponse subscriptionLookUpResponse = subscriptionService
					.getListOfSubscriptions();
			return new ResponseEntity<>(subscriptionLookUpResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending getListOfSubscriptions method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("applicationSubscriptionLookupResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getListOfSubscriptions method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("applicationSubscriptionLookupResponse", new ArrayList<>()), e.getMessage()));
		}
	}

}
