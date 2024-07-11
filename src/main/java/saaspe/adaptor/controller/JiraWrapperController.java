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
import saaspe.adaptor.model.JiraCreateUserRequest;
import saaspe.adaptor.service.JiraWrapperService;
import saaspe.aspect.ControllerLogging;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/jira")
public class JiraWrapperController {
	

	@Autowired
	private JiraWrapperService jiraWrapperService;
	
	private static final Logger log = LoggerFactory.getLogger(JiraWrapperController.class);

	
	@PostMapping("/createUser")
	public ResponseEntity<CommonResponse> createUser(@RequestBody JiraCreateUserRequest jiraCreateUserRequest,@RequestParam String appId) {
		log.info("Request received for createUser api");
		CommonResponse commonResponse = new CommonResponse();
		try {
		    commonResponse = jiraWrapperService.createUser(jiraCreateUserRequest,appId);
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
	
	@PostMapping("/addUserToGroup")
	public ResponseEntity<CommonResponse> addUserToGroup(@RequestParam String productName,
			@RequestBody String accountId, @RequestParam String appId) {
		log.info("Request received for addUserToGroup api");
		CommonResponse commonResponse = new CommonResponse();
		try {
		    commonResponse = jiraWrapperService.addUserToGroup(productName,accountId,appId);
			log.info("request for addUserToGroup api is successfull");
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response("createUser", null));
			commonResponse.setMessage("Failed to add User to the Group");
			log.error("exception occured while executing add User to Group api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/getAllUser")
	public ResponseEntity<CommonResponse> getAllUser(@RequestParam String appId) {
		log.info("Request received for getUser api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = jiraWrapperService.getAllUser(appId);
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
	
	@DeleteMapping("/deleteUser")
	public ResponseEntity<CommonResponse> removeUserFromGroup(@RequestParam String accountId,@RequestParam String appId) {
		log.info("Request recieved to remove user");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = jiraWrapperService.removeUserFromGroup(accountId, appId);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response(e.getLocalizedMessage(), null));
			commonResponse.setMessage("Failed to remove user");
			log.error("exception occured while executing removeUser api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}
	

}
