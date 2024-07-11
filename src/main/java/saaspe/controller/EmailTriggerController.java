package saaspe.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.DeptUserPasswordRequest;
import saaspe.model.Response;
import saaspe.service.DepartmentService;

@RestController
@RequestMapping("/api/auth")
@ControllerLogging
public class EmailTriggerController {

	@Autowired
	private DepartmentService departmentService;

	private static final Logger log = LoggerFactory.getLogger(EmailTriggerController.class);

	@PostMapping("/create-password")
	public ResponseEntity<CommonResponse> setPasswordToDeptUser(@RequestBody DeptUserPasswordRequest pwdRequest) {
		try {
			CommonResponse commonresponse = departmentService.createPassword(pwdRequest);
			return ResponseEntity.ok(commonresponse);
		} catch (DataValidationException e) {
			log.error("*** Ending setPasswordToDeptUser method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("departmentUsersResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending setPasswordToDeptUser method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("SetPasswordResponse", new ArrayList<>()), e.getMessage()));
		}
	}
}
