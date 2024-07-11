package saaspe.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.MarketPlaceEmailRequest;
import saaspe.model.Response;
import saaspe.service.MarketPlaceService;

@ControllerLogging
@RestController
@RequestMapping("/api/v1/marketplace")
public class MarketPlaceController {

	@Autowired
	private MarketPlaceService marketPlaceService;

	private static final Logger log = LoggerFactory.getLogger(MarketPlaceController.class);

	@GetMapping("/products")
	@PreAuthorize("hasAuthority('VIEW_MARKETPLACE')")
	public ResponseEntity<CommonResponse> getMarketPlace(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID,
			@RequestParam(name = "range", required = false) String range) {
		try {
			log.info("*** Enter into getRequestTracking method ***");
			CommonResponse request = marketPlaceService.getMarketPlace(range);
			return ResponseEntity.ok(request);
		} catch (DataValidationException e) {
			log.error("*** Ending getRequestTracking method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("MarketPlace ", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending error with MarketPlace ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(" MarketPlace", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/product/reviews")
	@PreAuthorize("hasAuthority('VIEW_MARKETPLACE')")
	public ResponseEntity<CommonResponse> getProductReviews(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID,
			@RequestParam(name = "UUID", required = false) String uUID) {
		try {
			log.info("*** Enter into getRequestTracking method ***");
			CommonResponse request = marketPlaceService.getProductReviews(uUID);
			return ResponseEntity.ok(request);
		} catch (DataValidationException e) {
			log.error("*** Ending getRequestTracking method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(" MarketPlace ", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending error with MarketPlace ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("IntegrationsListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/email")
	public ResponseEntity<CommonResponse> sendMailMarketEnquiry(
			@RequestBody MarketPlaceEmailRequest marketPlaceEmailRequest, Authentication authentication) {
		try {
			log.info("*** Enter into setPasswordToDeptUser method with request : {} ***", marketPlaceEmailRequest);
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonresponse = marketPlaceService.marketPlaceEmailTrigger(marketPlaceEmailRequest,
					profile);
			return ResponseEntity.ok(commonresponse);
		} catch (Exception e) {
			log.error("*** Ending setPasswordToDeptUser method with an error ***", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("MarketPlaceResponse", new ArrayList<>()), e.getMessage()));
		}
	}

}
