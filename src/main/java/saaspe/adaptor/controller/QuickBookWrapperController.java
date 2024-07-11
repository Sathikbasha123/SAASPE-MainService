package saaspe.adaptor.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saaspe.adaptor.model.QuickBooksUserRequest;
import saaspe.adaptor.service.QuickBookWrapperService;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@RestController
@RequestMapping("/api/v1/quickbook")
public class QuickBookWrapperController {

	private static final Logger log = LoggerFactory.getLogger(QuickBookWrapperController.class);

	@Autowired
	private QuickBookWrapperService quickBookService;

	@GetMapping("/authCodeUrl")
	public ResponseEntity<CommonResponse> getUrl(@RequestParam String appId, @RequestParam String redirectUri) {
		log.info("Request received for authCodeUrl api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = quickBookService.getUrl(appId, redirectUri);
			log.info("request for getToken api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response(e.getLocalizedMessage(), null));
			commonResponse.setMessage("Request failed");
			log.error("exception occured while executing authCodeUrl api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/accessToken")
	public ResponseEntity<CommonResponse> accessToken(@RequestParam String appId,
			@RequestParam(value = "code") String authCode, @RequestParam String realmId,
			@RequestParam Long uniqueId) {
		log.info("Request received for access token api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = quickBookService.getAccessToken(appId, authCode, realmId,uniqueId);
			log.info("request for accessToken api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response(e.getLocalizedMessage(), null));
			commonResponse.setMessage("Request failed");
			log.error("exception occured while executing accessToken api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getUsers")
	public ResponseEntity<CommonResponse> getUsers(@RequestParam String appId) {
		log.info("Request received for getUser api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = quickBookService.getUsers(appId);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response(e.getLocalizedMessage(), null));
			commonResponse.setMessage("Failed to get user");
			log.error("exception occured while executing getUsers api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getInfo")
	public ResponseEntity<CommonResponse> getCompanyInfo(@RequestParam String appId) {
		log.info("Request received for getInfo api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = quickBookService.getCompanyInfo(appId);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response(e.getLocalizedMessage(), null));
			commonResponse.setMessage("Failed to get company info");
			log.error("exception occured while executing getInfo api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/licenseCount")
	public ResponseEntity<CommonResponse> getLicenseCount(@RequestParam String appId) {
		log.info("Request received for getLicense api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = quickBookService.getLicenseCount(appId);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response(e.getLocalizedMessage(), null));
			commonResponse.setMessage("Failed to get licence count");
			log.error("exception occured while executing getLicenceCount  api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/refreshToken")
	public ResponseEntity<CommonResponse> generateRefreshToken(@RequestParam String appId) {
		log.info("Request received for refreshToken api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = quickBookService.generateRefreshToken(appId);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response(e.getLocalizedMessage(), null));
			commonResponse.setMessage("Failed to refresh token");
			log.error("exception occured while executing refreshToken api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/addUser")
	public ResponseEntity<CommonResponse> addUser(@RequestParam String appId,
			@RequestBody QuickBooksUserRequest userRequest) {
		log.info("Request recieved to add user");
		CommonResponse response = new CommonResponse();
		try {
			response = quickBookService.addUser(appId, userRequest);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.setStatus(HttpStatus.BAD_REQUEST);
			response.setResponse(new Response(e.getLocalizedMessage(), null));
			response.setMessage("Failed to add user");
			log.error("exception occured while executing addUser api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/deleteUser")
	public ResponseEntity<CommonResponse> deleteUser(@RequestParam String appId, @RequestParam String userEmail) {
		log.info("Request recieved to delete user");
		CommonResponse response = new CommonResponse();
		try {
			response = quickBookService.deleteUser(appId, userEmail);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.setStatus(HttpStatus.BAD_REQUEST);
			response.setResponse(new Response(e.getLocalizedMessage(), null));
			response.setMessage("Failed to delete user");
			log.error("exception occured while executing deleteUser api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/userDetail")
	public ResponseEntity<CommonResponse> getUserDetails(@RequestParam String appId, @RequestParam String userEmail) {
		log.info("Request received for getUserInfoByEmail api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = quickBookService.getUserInfoByEmail(appId, userEmail);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response(e.getLocalizedMessage(), null));
			commonResponse.setMessage("Failed to get user details");
			log.error("exception occured while fetching user Details  {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

}
