package saaspe.adaptor.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saaspe.adaptor.model.GitHubInviteRequestBody;
import saaspe.adaptor.model.RemoveUserRequest;
import saaspe.adaptor.service.GithubWrapperService;
import saaspe.aspect.ControllerLogging;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.adaptor.model.UserUpdateRequest;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/githubAdaptor")
public class GithubWrapperController {

	@Autowired
	private GithubWrapperService githubService;
	private static final Logger log = LoggerFactory.getLogger(GithubWrapperController.class);

	@GetMapping("/authURI")
	public ResponseEntity<CommonResponse> authURI(@RequestParam String appId) {
		log.info("Request received for Auth URI api");
		try {
			CommonResponse commonResponse = githubService.getAuthUri(appId);
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
			CommonResponse commonResponse = githubService.getToken(appId);
			log.info("request for get Access Token api is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing Acsess toekn api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get Access Token", null), "Failed to get Access Token"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getUserDetails")
	public ResponseEntity<CommonResponse> getUserDetails(@RequestParam String appId) {
		log.info("Request received for User details api");
		try {
			CommonResponse commonResponse = githubService.getUserDetails(appId);
			log.info("request for User details is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing User details api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get User details", null), "Failed to get User details"), HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/inviteUser")
	public ResponseEntity<CommonResponse> inviteUser(@RequestBody GitHubInviteRequestBody gitHubInviteRequestBody,
			@RequestParam String appId) {
		log.info("Request received for invite user api");
		try {
			CommonResponse commonResponse = githubService.inviteUser(gitHubInviteRequestBody, appId);
			log.info("request for  invite user is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing invite user API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("invite github user", null), "Failed to add Github user"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/getOrganizationMembers")
	public ResponseEntity<CommonResponse> getOrganizationMembers(@RequestParam String appId) {
		log.info("Request received for Get organization members api");
		try {
			CommonResponse commonResponse = githubService.getOrganizationMembers(appId);
			log.info("request for get organization members is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing Get organization members API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get User Groups", null), "Failed to Get User Groups"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/actionsBilling")
	public ResponseEntity<CommonResponse> getActionsBilling(@RequestParam String appId) {
		log.info("Request received for Get actions billing api");
		try {
			CommonResponse commonResponse = githubService.getActionsBilling(appId);
			log.info("request for get actions billing is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing actions billing API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get actions billing", null), "Failed to Get actions billing"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/packagesBilling")
	public ResponseEntity<CommonResponse> getPackagesBilling(@RequestParam String appId) {
		log.info("Request received for Get packages billing api");
		try {
			CommonResponse commonResponse = githubService.getPackagesBilling(appId);
			log.info("request for get packages billing is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing packagaes billing API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get packages billing", null), "Failed to Get packages billing"),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/sharedStorageBilling")
	public ResponseEntity<CommonResponse> getSharedStorageBilling(@RequestParam String appId) {
		log.info("Request received for get shared storage billing api");
		try {
			CommonResponse commonResponse = githubService.getSharedStorageBilling(appId);
			log.info("request for get shared storage billing is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing shared storage billing API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get shared storage billing", null), "Failed to Get shared storage billing"),
					HttpStatus.BAD_REQUEST);
		}
	}
	
	@DeleteMapping("/removeOrganizationMember")
	public ResponseEntity<CommonResponse> removeOrganizationMemeber(@RequestParam String appId, @RequestBody RemoveUserRequest removeUserRequest) {
		log.info("Request received for remove Github organization member api ");
		try {
			CommonResponse commonResponse = githubService.removeOrganizationMember( appId, removeUserRequest);
			log.info("request for  remove member is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing  remove organization member API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Remove github organization Member", null), "Failed to remove github organization Member"), HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping("/updateMemberRole")
	public ResponseEntity<CommonResponse> updateMemberRole(@RequestParam String appId, UserUpdateRequest userUpdateRequest) {
		log.info("Request received for update organization member role api");
		try {
			CommonResponse commonResponse = githubService.updateMemberRole(appId, userUpdateRequest);
			log.info("Request received for update organization member role api is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception occured while executing update member role API {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Update organization member role", null), "Failed to update organization member role"),
					HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/getOrganizationDetails")
	public ResponseEntity<CommonResponse> getOrganizationDetails(@RequestParam String appId) {
		log.info("Request received for organizations details api");
		try {
			CommonResponse commonResponse = githubService.getOrganizationDetails(appId);
			log.info("request for organization details is successfull");
			return new ResponseEntity<>(commonResponse, commonResponse.getStatus());
		} catch (Exception e) {

			log.error("Exception occured while executing organizations details api {}", e.getMessage());
			return new ResponseEntity<>(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Get organization details", null), "Failed to get organization details"), HttpStatus.BAD_REQUEST);
		}
	}

	
	
}
