package saaspe.adaptor.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import saaspe.adaptor.model.CommonZohoCRMRequest;
import saaspe.adaptor.service.ZohoCRMWrapperService;
import saaspe.aspect.ControllerLogging;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/zohoCRMAdaptor")
public class ZohoCRMWrapperController {

	@Autowired
	ZohoCRMWrapperService zohoCRMWrapperservice;

	@GetMapping("/oauth/getGrantToken")
	public void getGrantToken(HttpServletResponse response, @RequestParam("appId") String appId) {
		try {
			zohoCRMWrapperservice.getGrantToken(response, appId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@PostMapping("/oauth/getAccessToken")
	public ResponseEntity<CommonResponse> getaccessToken(@RequestParam("appId") String appId,
			@RequestParam("code") String code) throws JsonProcessingException {

		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = zohoCRMWrapperservice.getaccessToken(appId, code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
	}

	@PostMapping("/oauth/getRefreshToken")
	public ResponseEntity<CommonResponse> generateToken(@RequestParam("appId") String appId)
			throws JsonProcessingException {

		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = zohoCRMWrapperservice.generateRefreshToken(appId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
	}

	@PostMapping("/user/addUser")
	public ResponseEntity<CommonResponse> addUser(@RequestHeader("Authorize") String accesstoken,
			@RequestBody CommonZohoCRMRequest request) {
		CommonResponse commonResponse = new CommonResponse();
		Response r = new Response();
		try {
			commonResponse = zohoCRMWrapperservice.addUserToCRM(accesstoken, request);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			r.setData(e.getMessage());
			commonResponse.setResponse(r);
			commonResponse.setMessage("Failed to add user");
		}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());

	}

	@GetMapping("/user/getUser/{userId}")
	public ResponseEntity<CommonResponse> getUserById(@RequestParam("appId") String appId,
			@PathVariable String userId) {
		CommonResponse commonResponse = new CommonResponse();
		Response r = new Response();
		try {
			commonResponse = zohoCRMWrapperservice.getUserFromCRMById(appId, userId);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			r.setData(e.getMessage());
			commonResponse.setResponse(r);
		}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());

	}

	@GetMapping("/user/getOrganizationUser")
	public ResponseEntity<CommonResponse> getUser(@RequestParam("appId") String appId,
			@RequestParam("type") String userType) {
		CommonResponse commonResponse = new CommonResponse();

		try {
			commonResponse = zohoCRMWrapperservice.getUserFromCRM(appId, userType);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			e.printStackTrace();
		}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());

	}

	@PutMapping("/user/updateUser")
	public ResponseEntity<CommonResponse> updateUser(@RequestParam("appId") String appId,
			@RequestBody CommonZohoCRMRequest request) {
		CommonResponse commonResponse = new CommonResponse();

		try {
			commonResponse = zohoCRMWrapperservice.updateUserInCRM(appId, request);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			e.printStackTrace();
		}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());

	}

	@DeleteMapping("/user/deleteUser/{userId}")
	public ResponseEntity<CommonResponse> deleteUser(@RequestParam("appId") String appId,
			@PathVariable String userId) {
		CommonResponse commonResponse = new CommonResponse();

		try {
			commonResponse = zohoCRMWrapperservice.deleteUserInCRM(appId, userId);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			e.printStackTrace();
		}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());

	}

	@GetMapping("/organization/getOrganization")
	public ResponseEntity<CommonResponse> getOrganization(@RequestParam("appId") String appId) {
		CommonResponse commonResponse = new CommonResponse();

		try {
			commonResponse = zohoCRMWrapperservice.getOrganizationInCRM(appId);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			e.printStackTrace();
		}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
	}

	@GetMapping("/user/profiles")
	public ResponseEntity<CommonResponse> getProfiles(@RequestParam("appId") String appId) {
		CommonResponse commonResponse = new CommonResponse();

		try {
			commonResponse = zohoCRMWrapperservice.getUserProfiles(appId);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			e.printStackTrace();
		}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
	}

	@GetMapping("/user/roles")
	public ResponseEntity<CommonResponse> getRoles(@RequestParam("appId") String appId) {
		CommonResponse commonResponse = new CommonResponse();

		try {
			commonResponse = zohoCRMWrapperservice.getUserRoles(appId);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			e.printStackTrace();
		}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
	}

	@GetMapping("/user/getLicenseDetails")
	public ResponseEntity<CommonResponse> getLicense(@RequestParam("appId") String appId) {
		CommonResponse commonResponse = new CommonResponse();

		try {
			commonResponse = zohoCRMWrapperservice.getLicenseDetails(appId);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			e.printStackTrace();
		}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
	}

	@GetMapping("/user/getUserId")
	public ResponseEntity<CommonResponse> getUserId(@RequestParam("email") String email,
			@RequestParam("userType") String userType, @RequestParam("appId") String appId) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = zohoCRMWrapperservice.getUserId(email, userType, appId);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			e.printStackTrace();
		}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());

	}
	
	@GetMapping("/user/createUser")
	public ResponseEntity<CommonResponse> constructURL(@RequestParam("appId") String appId) throws IOException {
      CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse= zohoCRMWrapperservice.constructURL(appId);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			e.printStackTrace();
				}
		return new ResponseEntity<>(commonResponse, commonResponse.getStatus());

		}
}
