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

import saaspe.adaptor.model.ConfluenceCreateUser;
import saaspe.adaptor.service.ConfluenceWrapperService;
import saaspe.aspect.ControllerLogging;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/confluence")
public class ConfluenceWrapperController {
	
	@Autowired
	private ConfluenceWrapperService confluenceWrapperService;
	
	private static final Logger log = LoggerFactory.getLogger(ConfluenceWrapperController.class);

	@PostMapping("/createUser")
	public ResponseEntity<CommonResponse> createUser(@RequestBody ConfluenceCreateUser confluenceCreateUser,@RequestParam String appId) {
		log.info("Request received for createUser api");
		CommonResponse commonResponse = new CommonResponse();
		try {
		    commonResponse = confluenceWrapperService.createUser(confluenceCreateUser,appId);
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

	@GetMapping("/getUser")
	public ResponseEntity<CommonResponse> getUserList(@RequestParam String appId) {
		log.info("Request received for getUser api");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = confluenceWrapperService.getUserList(appId);
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
	public ResponseEntity<CommonResponse> deleteUser(@RequestParam String accountId, @RequestParam String appId) {
		log.info("Request recieved to delete user");
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = confluenceWrapperService.deleteUser(accountId, appId);
			return new ResponseEntity<>(commonResponse, HttpStatus.OK);
		} catch (Exception e) {
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			commonResponse.setResponse(new Response(e.getLocalizedMessage(), null));
			commonResponse.setMessage("Failed to delete user");
			log.error("exception occured while executing deleteUser api {}", e.getLocalizedMessage());
			return new ResponseEntity<>(commonResponse, HttpStatus.BAD_REQUEST);
		}
	}
}
