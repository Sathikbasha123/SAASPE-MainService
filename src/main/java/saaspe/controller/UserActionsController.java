package saaspe.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.service.UserActionService;

@ControllerLogging
@RequestMapping("api/v1/user/auditlogs")
@RestController
public class UserActionsController {

	private static final Logger log = LoggerFactory.getLogger(UserActionsController.class);

	@Autowired
	private UserActionService userActionService;

	@GetMapping("/actions")
	// @PreAuthorize("hasAuthority('VIEW_DEPARTMENT')")
	public ResponseEntity<CommonResponse> getUsersAllActionsListBasedOnrole(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse listView = userActionService.getUsersAllActionsListBasedOnrole();
			return ResponseEntity.ok(listView);
		} catch (Exception e) {
			log.error("*** Ending getUsersAllActionsListBasedOnrole method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST, new Response("UsersAllActionsList", ""),
					e.getMessage()));
		}
	}

}
