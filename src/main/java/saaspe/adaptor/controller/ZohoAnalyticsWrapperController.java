package saaspe.adaptor.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saaspe.adaptor.service.ZohoAnalyticsService;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@RestController
@RequestMapping("/api/v1/zohoAnalytics")
public class ZohoAnalyticsWrapperController {

	@Autowired
	ZohoAnalyticsService zohoAnalyticsService;

	private static final Logger log = LoggerFactory.getLogger(ZohoAnalyticsWrapperController.class);

	@GetMapping("/getAccessToken")
	public ResponseEntity<CommonResponse> getAccessToken(@RequestParam String appId,
			@RequestParam("code") String code) {
		log.info("Request received for get Access Token api");
		try {
			CommonResponse commonResponse = zohoAnalyticsService.getToken(appId, code);
			log.info("request for get Access Token api is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing Acsess toekn api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get Access Token", null), "Failed to get Access Token"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/generateToken")
	public ResponseEntity<CommonResponse> generateToken(@RequestParam String appId) {
		log.info("Request received for generate Token api");
		try {
			CommonResponse commonResponse = zohoAnalyticsService.generateToken(appId);
			log.info("request for generate Token api is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing generate Token api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST, new Response("Get Token", null),
					"Failed to generate Access Token"), HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/saveOrganizationDetails")
	public ResponseEntity<CommonResponse> saveOrganizationDetail(@RequestParam String appId) {
		log.info("Request received for Save organization details API");
		try {
			CommonResponse commonResponse = zohoAnalyticsService.saveOrgDetail(appId);
			log.info("request for save ZohoAnalytics org detail is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing  Save organization details API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Save organization details response", null), "Failed to Save organization details"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/inviteUser")
	public ResponseEntity<CommonResponse> inviteUser(@RequestParam String appId, @RequestParam String userEmail) {
		log.info("Request received for Invite ZohoAnalytics User API");
		try {
			CommonResponse commonResponse = zohoAnalyticsService.inviteUser(appId, userEmail);
			log.info("request for invite user is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing  Add member API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Invite Zoho Analytics Member", null), "Failed to Invite Zoho Analytics member"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/user-list")
	public ResponseEntity<CommonResponse> getUsersList(@RequestParam String appId) {
		log.info("Request received for ZohoAnalytics Users list api");
		try {
			CommonResponse commonResponse = zohoAnalyticsService.getUsersList(appId);
			log.info("request for get ZohoAnalytics user list is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing  get ZohoAnalytics users list API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get Users list response", null), "Failed to get users list"), HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/revokeAccess")
	public ResponseEntity<CommonResponse> revokeAccess(@RequestParam String appId, @RequestParam String userEmail) {
		log.info("Request received for ZohoAnalytics revoke Member api");
		try {
			CommonResponse commonResponse = zohoAnalyticsService.revokeAccess(userEmail, appId);
			log.info("request for  revoke ZohoAnalytics member is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing  revoke ZohoAnalytics user access API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Revoke ZohoPeople Member", null), "Failed to revoke Zoho Analytics Access"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/organizationList")
	public ResponseEntity<CommonResponse> getOrganizationList(@RequestParam String appId) {
		log.info("Request received for ZohoAnalytics organization list api");
		try {
			CommonResponse commonResponse = zohoAnalyticsService.getOrgList(appId);
			log.info("request for get ZohoAnalytics organization list is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing  revoke ZohoAnalytics user access API {}", e.getMessage());
			return new ResponseEntity<>(
					new CommonResponse(HttpStatus.BAD_REQUEST, new Response("Get organization list response", null),
							"Failed to get ZohoAnalytics organization list"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/subscription-details")
	public ResponseEntity<CommonResponse> getSubscriptionList(@RequestParam String appId) {
		log.info("Request received for ZohoAnalytics Subscription List api");
		try {
			CommonResponse commonResponse = zohoAnalyticsService.getSubscriptionList(appId);
			log.info("request for get ZohoAnalytics subscription list is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing  get ZohoAnalytics subscription list API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get Subscription list response", null), "Failed to get subscription list"),
					HttpStatus.BAD_REQUEST);
		}
	}
}
