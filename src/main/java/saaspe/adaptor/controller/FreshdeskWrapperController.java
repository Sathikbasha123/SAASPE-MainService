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

import saaspe.adaptor.service.FreshdeskWrapperService;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@RestController
@RequestMapping("/freshdesk")
public class FreshdeskWrapperController {

	@Autowired
	private FreshdeskWrapperService freshdeskService;

	private static final Logger log = LoggerFactory.getLogger(FreshdeskWrapperController.class);

	@GetMapping("/account/details")
	public ResponseEntity<CommonResponse> getAccountDetails(@RequestParam String appId) {
		try {
			CommonResponse response = freshdeskService.getAccountDetails(appId);
			log.info("request for get account details api is successfull");
			return new ResponseEntity<>(response, response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing get account details api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get account details response", null), "Failed to get Accoutn details"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/user/invite")
	public ResponseEntity<CommonResponse> inviteUser(@RequestParam String appId, @RequestParam String userName,
			@RequestParam String userEmail) {
		try {
			CommonResponse response = freshdeskService.inviteUser(appId, userEmail, userName);
			log.info("request for invite user api is successfull");
			return new ResponseEntity<>(response, response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing invite user api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get account details response", null), "Failed to invite user"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/user/revokeAccess")
	public ResponseEntity<CommonResponse> revokeUserAccess(@RequestParam String appId, @RequestParam String userEmail) {
		try {
			CommonResponse response = freshdeskService.revokeUserAccess(appId, userEmail);
			log.info("request for revoke user access api is successfull");
			return new ResponseEntity<>(response, response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing revoke user access api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get account details response", null), "Failed to revoke user access"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/user/list")
	public ResponseEntity<CommonResponse> getUserList(@RequestParam String appId) {
		try {
			CommonResponse response = freshdeskService.getUserList(appId);
			log.info("request for user list api is successfull");
			return new ResponseEntity<>(response, response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing user list api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get account details response", null), "Failed to get user list"),
					HttpStatus.BAD_REQUEST);
		}
	}

}
