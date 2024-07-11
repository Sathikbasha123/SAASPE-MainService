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

import saaspe.adaptor.service.SalesforceService;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@RestController
@RequestMapping("/v1/salesforce")
public class SalesforceWrapperController {

	@Autowired
	SalesforceService salesforceService;

	private static final Logger log = LoggerFactory.getLogger(SalesforceWrapperController.class);

	@PostMapping("/generateToken")
	ResponseEntity<CommonResponse> generateToken(@RequestParam String appId, @RequestParam String clientId,
			@RequestParam String clientSecret, @RequestParam String organizationDomain) {
		log.info("Request received for generate token api");
		try {
			CommonResponse commonResponse = salesforceService.generateToken(appId);
			log.info("request for generate token api is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			log.error("Exception occured while executing generate token api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Generate token response", null), "Failed to generate token"), HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/createUser")
	ResponseEntity<CommonResponse> createUser(@RequestParam String appId, @RequestParam String userEmail,
			@RequestParam String userId,@RequestParam String firstName) {
		log.info("Request received for create user api");
		try {
			CommonResponse commonResponse = salesforceService.createUser(appId, userEmail, userId,firstName);
			log.info("request for create user api is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			log.error("Exception occured while executing generate token api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Creat User response", null), "Failed to create user"), HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/revokeAccess")
	public ResponseEntity<CommonResponse> revokeAccess(@RequestParam String appId, @RequestParam String userEmail,
			@RequestParam String userId) {
		log.info("Request received for revoke user access api");
		try {
			CommonResponse commonResponse = salesforceService.revokeAccess(appId, userEmail, userId);
			log.info("request for revoke user access api is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			log.error("Exception occured while executing revoke user access api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Revoke user access response", null), "Failed to revoke user access"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/user/list")
	public ResponseEntity<CommonResponse> getUsersList(@RequestParam String appId) {
		log.info("Request received for user list api");
		try {
			CommonResponse commonResponse = salesforceService.getUserList(appId);
			log.info("request for user list api is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			log.error("Exception occured while executing user list api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Revoke user access response", null), "Failed to get user list"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/licenseDetails")
	public ResponseEntity<CommonResponse> licenseDetails(@RequestParam String appId) {
		log.info("Request received for license details api");
		try {
			CommonResponse commonResponse = salesforceService.getLicenseDetails(appId);
			log.info("request for get license details is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			log.error("Exception occured while executing license details api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get license details response", null), "Failed to get license details"),
					HttpStatus.BAD_REQUEST);
		}
	}
}
