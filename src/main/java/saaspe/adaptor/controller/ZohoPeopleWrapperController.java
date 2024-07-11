package saaspe.adaptor.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saaspe.adaptor.model.ZohoPeopleInviteRequest;
import saaspe.adaptor.service.ZohoPeopleService;
import saaspe.aspect.ControllerLogging;
import saaspe.entity.UserOnboarding;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.repository.UserOnboardingDetailsRepository;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/zohoPeople")
public class ZohoPeopleWrapperController {

	@Autowired
	ZohoPeopleService zohoPeopleService;

	@Autowired
	UserOnboardingDetailsRepository userOnboardingDetailsRepository;

	private static final Logger log = LoggerFactory.getLogger(ZohoPeopleWrapperController.class);

	@GetMapping("/authURI")
	public ResponseEntity<CommonResponse> authURI(@RequestParam String appId) {
		log.info("Request received for Auth URI api");
		try {
			CommonResponse commonResponse = zohoPeopleService.getAuthUri(appId);
			log.info("request for Auth URI api is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			log.error("Exception occured while executing authURI api {}", e.getMessage());
			return new ResponseEntity<>(
					new CommonResponse(HttpStatus.BAD_REQUEST, new Response("Get URI", null), "Failed to get Auth URI"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getAccessToken")
	public ResponseEntity<CommonResponse> getAccessToken(@RequestParam String appId,
			@RequestParam("code") String code) {
		log.info("Request received for get Access Token api");
		try {
			CommonResponse commonResponse = zohoPeopleService.getToken(appId, code);
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
			CommonResponse commonResponse = zohoPeopleService.generateToken(appId);
			log.info("request for generate Token api is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing generate Token api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST, new Response("Get Token", null),
					"Failed to generate Access Token"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/user/list")
	public ResponseEntity<CommonResponse> getAllUsers(@RequestParam String appId) {
		log.info("Request received for user list api");

		try {
			CommonResponse response = zohoPeopleService.getUsersList(appId);
			return new ResponseEntity<>(response, response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing get all users api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST, new Response("Get All users", null),
					"Failed to get users list"), HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/user/addUser")
	public ResponseEntity<CommonResponse> addUser(@RequestBody ZohoPeopleInviteRequest zohoPeopleInviteRequest,
			@RequestParam String appId) {
		log.info("Request received for Add member api");
		try {
			CommonResponse commonResponse = zohoPeopleService.addUser(zohoPeopleInviteRequest, appId);
			log.info("request for  Add member is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing  Add member API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Add ZohoPeople Member", null), "Failed to add ZohoPeople member"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/user/revokeAccess")
	public ResponseEntity<CommonResponse> revokeAccess(@RequestParam String userEmail, @RequestParam String appId) {
		log.info("Request received for ZohoPeople revoke Member api");
		try {
			CommonResponse commonResponse = zohoPeopleService.revokeAccess(userEmail, appId);
			log.info("request for  revoke member is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing  revoke gitlab member API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Revoke ZohoPeople Member", null), "Failed to revoke ZohoPeople Member"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/user/findUser")
	public ResponseEntity<CommonResponse> findUserId(@RequestParam String appId, @RequestParam String email) {
		log.info("Request received for find user ID api");
		try {
			CommonResponse commonResponse = zohoPeopleService.findUserByEmail(appId, email);
			log.info("request for find user ID is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing Find user ID api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST, new Response("Find User ID", null),
					"Failed to Find User Id"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/demo")
	public CommonResponse demo() {
		UserOnboarding userDetail = userOnboardingDetailsRepository.findByEmailAddresss("hupimo@socam.me");

		ZohoPeopleInviteRequest inviteRequest = new ZohoPeopleInviteRequest();
		inviteRequest.setEmailID(userDetail.getUserEmail());
		inviteRequest.setFirstName(userDetail.getFirstName());
		inviteRequest.setLastName(userDetail.getLastName());
		inviteRequest.setEmployeeID(userDetail.getUserId());
		return zohoPeopleService.addUser(inviteRequest, "APP_02");

	}
}
