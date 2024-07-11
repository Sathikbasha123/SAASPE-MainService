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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import saaspe.adaptor.model.AddGitlabMemberRequest;
import saaspe.adaptor.model.GitlabDeleteUserRequest;
import saaspe.adaptor.model.GitlabTokenResponse;
import saaspe.adaptor.service.GitlabWrapperService;
import saaspe.aspect.ControllerLogging;
import saaspe.entity.AdaptorDetails;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.repository.AdaptorDetailsRepsitory;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/gitlabAdaptor")
public class GitlabWrapperController {

	@Autowired
	private GitlabWrapperService gitlabService;

	@Autowired
	private AdaptorDetailsRepsitory adaptorDetailsRepository;

	private static final Logger log = LoggerFactory.getLogger(GitlabWrapperController.class);

	@GetMapping("/authURI")
	public ResponseEntity<CommonResponse> authURI(@RequestParam String appId) {
		log.info("Request received for Auth URI api");
		try {
			CommonResponse commonResponse = gitlabService.getAuthUri(appId);
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
	public ResponseEntity<CommonResponse> getAccessToken(@RequestParam String appId) {
		log.info("Request received for get Access Token api");
		try {
			CommonResponse commonResponse = gitlabService.getToken(appId);
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
			CommonResponse commonResponse = gitlabService.generateToken(appId);
			log.info("request for generate Token api is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing generate Token api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST, new Response("Get Token", null),
					"Failed to generate Access Token"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/userProfile")
	public ResponseEntity<CommonResponse> userProfile(@RequestParam String appId) {
		log.info("Request received for User Profile api");
		try {
			CommonResponse commonResponse = gitlabService.getUserProfile(appId);
			log.info("request for User Profile is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing User Profile {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get User Profile", null), "Failed to get User Profile"), HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/addGitlabMember")
	public ResponseEntity<CommonResponse> addGitlabMember(@RequestBody AddGitlabMemberRequest gitlabMemberRequest,
			@RequestParam String appId) {
		log.info("Request received for Add member api");
		try {
			CommonResponse commonResponse = gitlabService.addGitlabMember(gitlabMemberRequest, appId);
			log.info("request for  Add member is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing  Add member API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Add Gitlab Member", null), "Failed to add Gitlab member"), HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/removeGitlabMember")
	public ResponseEntity<CommonResponse> removeGitlabMemeber(@RequestBody GitlabDeleteUserRequest removeGitlabMember,
			@RequestParam String appId) {
		log.info("Request received for Gitlab revoke Member api");
		try {
			CommonResponse commonResponse = gitlabService.removeGitlabMember(removeGitlabMember, appId);
			log.info("request for  revoke member is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing  revoke gitlab member API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Revoke Gitlab Member", null), "Failed to revoke Gitlab Member"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getUserGroups")
	public ResponseEntity<CommonResponse> getUserGroup(@RequestParam String appId) {
		log.info("Request received for Get user groups api");
		try {
			CommonResponse commonResponse = gitlabService.getUserGroups(appId);
			log.info("request for get user group is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing Get user groups API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get User Groups", null), "Failed to Get User Groups"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getProjects")
	public ResponseEntity<CommonResponse> getGroupProjects(@RequestParam String appId) {
		log.info("Request received for get projects api");
		try {
			CommonResponse commonResponse = gitlabService.getGroupProjects(appId);
			log.info("request for get projects by group Id is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing get projects by group Id API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get projects by groupId", null), "Failed to get projects by group Id"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getUserId")
	public ResponseEntity<CommonResponse> findUserId(@RequestParam String appId, @RequestParam String userName) {
		log.info("Request received for find user ID api");
		try {
			CommonResponse commonResponse = gitlabService.findUserId(appId, userName);
			log.info("request for find user ID is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing Find user ID api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST, new Response("Find User ID", null),
					"Failed to Find User Id"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getAccessRoles")
	public ResponseEntity<CommonResponse> getAccessRoles() {
		log.info("Request received for get access roles api");
		try {
			CommonResponse commonResponse = gitlabService.getAccessRoles();
			log.info("request for get access roles is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing get access roles api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get access roles", null), "Failed to get access roles"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getGroupSubscriptionInfo")
	public ResponseEntity<CommonResponse> getGroupSubsInfo(@RequestParam String appId) {
		log.info("Request received for group subscription api");
		try {
			CommonResponse commonResponse = gitlabService.getSubscriptionInfo(appId);
			log.info("request for group subscription is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing group subscription api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get subscription info", null), "Failed to get group's subscription details"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/listAllUsers")
	public ResponseEntity<CommonResponse> getAllUsers(@RequestParam String appId) {
		try {
			CommonResponse response = gitlabService.getUsersList(appId);
			return new ResponseEntity<>(response, response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing get all users api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST, new Response("Get All users", null),
					"Failed to get users list"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getResourceMembers")
	public ResponseEntity<CommonResponse> getResourceMembers(@RequestParam String appId) {
		try {
			CommonResponse response = gitlabService.getResourceMembers(appId);
			return new ResponseEntity<>(response, response.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing get resource members api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get Resource members", null), "Failed to get resource members"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/token")
	public String getToken(@RequestParam String appId) throws JsonProcessingException {
		AdaptorDetails adaptorDetails = adaptorDetailsRepository.findByApplicationName("Gitlab");
		CommonResponse commonResponse = gitlabService.generateToken(appId);
		ObjectMapper objectMapper = new ObjectMapper();
		GitlabTokenResponse tokenResponse = objectMapper.readValue(
				objectMapper.writeValueAsString(commonResponse.getResponse().getData()), GitlabTokenResponse.class);
		adaptorDetails.setApiToken(tokenResponse.getRefresh_token());
		adaptorDetailsRepository.save(adaptorDetails);
		return tokenResponse.getAccess_token();

	}
	
	
}
