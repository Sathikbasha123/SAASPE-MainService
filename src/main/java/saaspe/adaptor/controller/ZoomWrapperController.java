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

import saaspe.adaptor.model.CreateZoomUserRequest;
import saaspe.adaptor.service.ZoomWrapperService;
import saaspe.aspect.ControllerLogging;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/zoom")
public class ZoomWrapperController {
	
	@Autowired
	private ZoomWrapperService zoomService;
	
	private static final Logger log = LoggerFactory.getLogger(ZoomWrapperController.class);

	@GetMapping("/AuthUri")
	public ResponseEntity<CommonResponse> authUri(@RequestParam String appId, @RequestParam String redirectUri) {
		log.info("Request received for getAuthUri api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = zoomService.getAuthUri(appId, redirectUri);
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
	public ResponseEntity<CommonResponse> getToken(@RequestParam String appId, @RequestParam String code,
			@RequestParam Long uniqueId) {
		log.info("Request received for getToken api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = zoomService.getToken(appId, code, uniqueId);
			log.info("request for getToken api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("getToken", null));
			commonResponse.setMessage("Failed to get token");
			e.printStackTrace();
			log.error("exception occured while executing getToken api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/refreshToken")
	public ResponseEntity<CommonResponse> generateRefreshToken(@RequestParam String appId) {
		log.info("Request received for refreshToken api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = zoomService.getRefreshToken(appId);
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
			commonResponse = zoomService.getUser(appId);
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
	public ResponseEntity<CommonResponse> createUser(
			@RequestBody CreateZoomUserRequest createZoomUserRequest, @RequestParam String appId) {
		log.info("Request received for createUser api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = zoomService.createUser(createZoomUserRequest, appId);
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
			commonResponse = zoomService.deleteUser(userEmail, appId);
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
	public ResponseEntity<CommonResponse> getLicenseCount(@RequestParam String appId) {
		log.info("Request received for getUser api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = zoomService.getLicenseCount(appId);
			log.info("request for getUser api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("getUser", null));
			commonResponse.setMessage("Failed to get user");
			log.error("exception occured while executing getLicenseCount api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}

}
