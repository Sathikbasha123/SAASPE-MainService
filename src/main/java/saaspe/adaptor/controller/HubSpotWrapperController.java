package saaspe.adaptor.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import saaspe.adaptor.service.HubSpotWrapperService;
import saaspe.aspect.ControllerLogging;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.adaptor.model.CreateHubSpotUserRequest;
import saaspe.adaptor.model.HubSpotSubscriptionRequest;
import saaspe.model.Response;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/hubspot")
public class HubSpotWrapperController {
	@Autowired
	private HubSpotWrapperService adaptorService;
	private static final Logger log = LoggerFactory.getLogger(HubSpotWrapperController.class);


	@Value("${hubspot.getLicense.count.api.url}")
	private String licenseCountUrl;

	@Value("${hubspot.subscription.info.api.url}")
	private String subscriptionUrl;


	@GetMapping("/AuthUri")
	public ResponseEntity<CommonResponse> authUri(@RequestParam String appId) {
		log.info("Request received for getAuthUri api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = adaptorService.getAuthUri(appId);
			log.info("request for getAuthUri api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("getAuthUri", null));
			commonResponse.setMessage("Failed to get AuthUri");
			log.error("exception occured while executing getAuthUri api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getToken")
	public ResponseEntity<CommonResponse> getToken(@RequestParam String appId) {
		log.info("Request received for getToken api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = adaptorService.getToken(appId);
			log.info("request for getToken api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("getToken", null));
			commonResponse.setMessage("Failed to get token");
			log.error("exception occured while executing getToken api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/refreshToken")
	public ResponseEntity<CommonResponse> generateRefreshToken(@RequestParam String appId) {
		log.info("Request received for refreshToken api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = adaptorService.getRefreshToken(appId);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response(e.getLocalizedMessage(), null));
			commonResponse.setMessage("Failed to refresh token");
			log.error("exception occured while executing refreshToken api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getUser")
	public ResponseEntity<CommonResponse> getUser(@RequestParam String appId) {
		log.info("Request received for getUser api");
		CommonResponse commonResponse = new CommonResponse();
		try {

			commonResponse = adaptorService.getUser(appId);
			log.info("request for getUser api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("getUser", null));
			commonResponse.setMessage("Failed to get user");
			log.error("exception occured while executing getUser api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/createUser")
	public ResponseEntity<CommonResponse> createUser(@RequestBody CreateHubSpotUserRequest hubSpotUserRequest,
			@RequestParam String appId) {
		log.info("Request received for createUser api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = adaptorService.createUser(hubSpotUserRequest, appId);
			log.info("request for createUser api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("createUser", null));
			commonResponse.setMessage("Failed to create User");
			log.error("exception occured while executing create User api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/deleteUser")
	public ResponseEntity<CommonResponse> deleteUser(@RequestParam String userEmail, @RequestParam String appId) {
		log.info("Request recieved to delete user");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = adaptorService.deleteUser(userEmail, appId);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response(e.getLocalizedMessage(), null));
			commonResponse.setMessage("Failed to delete user");
			log.error("exception occured while executing deleteUser api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getLicenseCount")
	public ResponseEntity<CommonResponse> getUserCount(@RequestParam String appId) {

		log.info("Request received for getLicenseCount api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = adaptorService.getByAccesssCode(licenseCountUrl, appId);
			log.info("request for getLicenseCount api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("getLicenseCount", null));
			commonResponse.setMessage("Failed to getLicenseCount");
			log.error("exception occured while executing getLicenseCount api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getLoginAuditLogs")
	public ResponseEntity<CommonResponse> getLoginAuditLogs(@RequestParam String userEmail,
			@RequestParam String appId) {
		log.info("Request received for getLoginAuditLogs api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = adaptorService.getLoginAuditLogs(userEmail, appId);
			log.info("request for getLoginAuditLogs api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("getLoginAuditLogs", null));
			commonResponse.setMessage("Failed to getLoginAuditLogs");
			log.error("exception occured while executing getLoginAuditLogs api {}", e.getLocalizedMessage());
			e.printStackTrace();
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getSecurityAuditLogs")
	public ResponseEntity<CommonResponse> getSecurityAuditLogs(@RequestParam String userEmail,
			@RequestParam String appId) {
		log.info("Request received for getSecurityAuditLogs api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = adaptorService.getSecurityAuditLogs(userEmail, appId);
			log.info("request for getSecurity api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("getSecurityAuditLogs", null));
			commonResponse.setMessage("Failed to getSecurityAuditLogs");
			log.error("exception occured while executing getSecurityAuditLogs api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getAccountInfoAuditLogs")
	public ResponseEntity<CommonResponse> getAccountInfoAuditLogs(@RequestParam String appId) {

		log.info("Request received for getAccountInfoAuditLogs api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = adaptorService.getByAccountInfoAuditlogs(appId);
			log.info("request for getAccountInfoAuditLogs api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("getAccountInfoAuditLogs", null));
			commonResponse.setMessage("Failed to getAccountInfoAuditLogs");
			log.error("exception occured while executing getAccountInfoAuditLogs api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/Subscription")
	public ResponseEntity<CommonResponse> getAllSubDefinition(@RequestParam String appId) {
		log.info("Request received for getSubscriptiondetails api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = adaptorService.getSubscription(subscriptionUrl, appId);
			log.info("request for getSubscriptionDetails api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("getSubscriptionDetails", null));
			commonResponse.setMessage("Failed to getSubscriptionDetails");
			log.error("exception occured while executing getSubscriptionDetails api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/createSubscription")
	public ResponseEntity<CommonResponse> createSubscription(
			@RequestBody HubSpotSubscriptionRequest hubSpotSubscriptionRequest, @RequestParam String appId) {
		log.info("Request received for createSubscription api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = adaptorService.createsubscription(hubSpotSubscriptionRequest, appId);
			log.info("request for create subscription api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("create subscription", null));
			commonResponse.setMessage("Failed to create subscription");
			log.error("exception occured while executing create subscription api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/createUnSubscription")
	public ResponseEntity<CommonResponse> createUnSubscription(
			@RequestBody HubSpotSubscriptionRequest hubSpotSubscriptionRequest, @RequestParam String appId) {
		log.info("Request received for createUnSubscription api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = adaptorService.createunsubscription(hubSpotSubscriptionRequest, appId);
			log.info("request for createUnsubscription api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("createUnSubscription", null));
			commonResponse.setMessage("Failed to createUnsubscription");
			log.error("exception occured while executing createUnSubscription api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getUserIds")
	public ResponseEntity<List<String>> getUserIds(@RequestParam String appId) throws JsonProcessingException {
		log.info("Request received for getUserIds api");
		try {
			List<String> userIds = adaptorService.getUserIds(appId);
			log.info("Request for getUserIds api is successful");
			return new ResponseEntity<>(userIds, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("Exception occurred while executing getUserIds api: {}", e.getLocalizedMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

}
