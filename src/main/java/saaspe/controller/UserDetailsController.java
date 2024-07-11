package saaspe.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.NonNull;
import saaspe.aspect.ControllerLogging;
import saaspe.constant.Constant;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.CreateAdminRequest;
import saaspe.model.Response;
import saaspe.model.UserDetailsRequest;
import saaspe.model.UserEmailsRemoveRequest;
import saaspe.model.UserLastLoginRequest;
import saaspe.model.UserOnboardingRequest;
import saaspe.model.UserUpdateRequest;
import saaspe.model.updateUserOwnershipRequest;
import saaspe.service.UserDetailsService;

@RestController
@RequestMapping("api/v1/user/details")
@ControllerLogging
public class UserDetailsController {

	@Autowired
	private UserDetailsService userDetailsService;

	private static final Logger log = LoggerFactory.getLogger(UserDetailsController.class);

	@PostMapping("/add-user")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('ADD_USER')")
	public ResponseEntity<CommonResponse> addUserDetails(@Valid @RequestBody UserDetailsRequest user,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse commonResponse = userDetailsService.addUserDetails(user);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** ending addUserDetails with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.CONFLICT,
					new Response("addUserDetailsResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending addUserDetails with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserAddResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/users")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('VIEW_USER')")
	public ResponseEntity<CommonResponse> getUserDetails(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse commonResponse = userDetailsService.getUserDetails();
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** ending getUserDetails with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response(Constant.USER_DETAILS_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending getUserDetails with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.USER_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@DeleteMapping("/remove-user/{email}")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('DELETE_USER')")
	public ResponseEntity<CommonResponse> removeUserDetails(@Valid @PathVariable String email,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse userData = userDetailsService.removeUserDetailsByUserEmail(email);
			return ResponseEntity.ok(userData);
		} catch (DataValidationException e) {
			log.error("*** ending removeUserDetails with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("removeUserDetailsResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending removeUserDetails with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserDeleteResponse", new ArrayList<>()), e.getMessage()));
		}
	}


	@PutMapping("/modify-user")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('EDIT_USER')")
	public ResponseEntity<CommonResponse> modifyUserDetails(@Valid @RequestParam("userId") String userId,
			@RequestBody UserUpdateRequest userUpateRequest,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse userJson = userDetailsService.modifyUserDetails(userId, userUpateRequest);
			return ResponseEntity.ok(userJson);
		} catch (DataValidationException e) {
			log.error("*** ending modifyUserDetails method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("PaymentDetails", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending modifyUserDetails method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.USER_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/detail-view")
	@PreAuthorize("hasAuthority('VIEW_USER')")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	public ResponseEntity<CommonResponse> usersDetialView(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse userDetails = userDetailsService.getUsersDetialView();
			return ResponseEntity.ok(userDetails);
		} catch (DataValidationException e) {
			log.error("*** ending getUserDetailView method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("UsersDetialView", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending getUserDetailView method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.USER_DETAILS_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/topapps/byuser")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('VIEW_USER')")
	public ResponseEntity<CommonResponse> getTopAppsByUserCount(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse resp = userDetailsService.getTopAppsByUsercount();
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			log.error("*** ending topAppsByUserCount method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.USER_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/user-list-view")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('VIEW_USER')")
	public ResponseEntity<CommonResponse> userListView(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse resp = userDetailsService.getUserListView();
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			log.error("*** ending userListView method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/users/import")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('ADD_USER')")
	public ResponseEntity<CommonResponse> usersOnboardingBulk(@Valid @RequestBody List<UserOnboardingRequest> request,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse response = userDetailsService.saveUserOnboardingData(request);
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** ending usersOnboardingBulk method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("UsersOnboardingBulk", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending usersOnboardingBulk method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserOnboardingResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@DeleteMapping("/multiple-remove")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('DELETE_USER')")
	public ResponseEntity<CommonResponse> deleteByUserIds(@RequestBody UserEmailsRemoveRequest userEmails) {
		try {
			CommonResponse response = userDetailsService.deleteByUserEmails(userEmails);
			return ResponseEntity.ok(response);
		} catch (DataValidationException e) {
			log.error("*** ending deleteByUserIds method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("UsersMultipleRemove", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending deleteByUserIds method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DeleteUserResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/user-details-overview")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('VIEW_USER')")
	public ResponseEntity<CommonResponse> getUserDetailsOverview(@RequestParam String userId) {
		try {
			CommonResponse commonResponse = userDetailsService.getUserDetailsOverview(userId);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** ending getUserDetailsOverview method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("UserDetailsOverview", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending getUserDetailsOverview method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.USER_DETAILS_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/profile")
	// @PreAuthorize("hasAuthority('VIEW_USER')")
	public ResponseEntity<CommonResponse> getProfile(HttpServletRequest request, Authentication authentication) {
		try {
			UserLoginDetails profile = new UserLoginDetails();
			if (authentication != null) {
				profile = (UserLoginDetails) authentication.getPrincipal();
			}
			CommonResponse userData = userDetailsService.getProfile(profile, request);
			return ResponseEntity.ok(userData);
		} catch (DataValidationException dve) {
			log.error("*** ending getProfile method with error ***", dve);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		} catch (Exception e) {
			log.error("*** ending getProfile method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ProfileResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/ownership/list")
	@PreAuthorize("hasAuthority('VIEW_USER')")
	public ResponseEntity<CommonResponse> getOwnerShipList(
			@RequestParam(value = "userId", required = true) String userId) {
		try {
			CommonResponse commonResponse = userDetailsService.getOwnerShipList(userId);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** ending getOwnerShipList method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("checkUserOwnershipResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending getOwnerShipList method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("OwnerListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PutMapping("/ownership/transfer")
	@PreAuthorize("hasAuthority('EDIT_USER')")
	public ResponseEntity<CommonResponse> getOwnerShipTransfer(
			@RequestBody updateUserOwnershipRequest ownershipRequest) {
		try {
			CommonResponse commonResponse = userDetailsService.getOwnerShipTransfer(ownershipRequest);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** ending getOwnerShipTransfer method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("ownershipTransferResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending getOwnerShipTransfer method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("OwnershipTransferResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/admin/list")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('VIEW_ADMINUSER')")
	public ResponseEntity<CommonResponse> getAllRoles() {
		try {
			CommonResponse commonResponse = userDetailsService.getAllAdmins();
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** ending getAllRoles method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("Admin ListView Response", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending getAllRoles method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("AdminListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/get/permission")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('VIEW_USER')")
	public ResponseEntity<CommonResponse> getPermissionByRole(@NonNull @RequestParam("role") String role) {
		try {
			CommonResponse commonResponse = userDetailsService.getPermissionByRole(role);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** ending getPermissionByRole method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("Get Permission By Role Response", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending getPermissionByRole method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("PermissionResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/create/admin")
	@ApiOperation(value = "", authorizations = { @Authorization(value = "jwtToken") })
	@PreAuthorize("hasAuthority('ADD_ADMINUSER')")
	public ResponseEntity<CommonResponse> createAdminUsers(@RequestBody CreateAdminRequest adminRequest,
			Authentication authentication) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse commonResponse = userDetailsService.createAdminUsers(adminRequest, profile);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** ending createAdminUsers method with errors ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.CREATE_ADMIN_RESPONSE, new ArrayList<>()), e.getMessage()));
		} catch (MailSendException e) {
			log.error("*** createAdminUsers method with errors ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.CREATE_ADMIN_RESPONSE, new ArrayList<>()), "Please unblock the mail service to send OTP."));
		}catch (Exception e) {
			log.error("*** ending createAdminUsers method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.CREATE_ADMIN_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/login/excel")
	@PreAuthorize("hasAuthority('ADD_WORKFLOW')")
	public ResponseEntity<CommonResponse> sendEmialToUser(@RequestBody UserLastLoginRequest userLastLoginRequest) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = userDetailsService.sendEmialToUser(userLastLoginRequest);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException dve) {
			log.error(Constant.SEND_EMAIL_TO_USER_EROR, dve);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST, new Response("Email Trigger", new ArrayList<>()),
					dve.getMessage()));
		} catch (MailSendException e) {
			log.error(Constant.SEND_EMAIL_TO_USER_EROR, e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Email Trigger", new ArrayList<>()), "Please unblock the mail service to send mail."));
		}catch (Exception e) {
			log.error(Constant.SEND_EMAIL_TO_USER_EROR, e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserEmailResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/lastlogin")
	@PreAuthorize("hasAuthority('VIEW_USER')")
	public ResponseEntity<CommonResponse> lastLoginUsers(@RequestParam String dateRange,
			@RequestParam String applicationId) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = userDetailsService.lastLoginUsers(dateRange, applicationId);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException dve) {
			return ((BodyBuilder) ResponseEntity.noContent()).body(new CommonResponse(HttpStatus.NO_CONTENT,
					new Response("userLastLoginResponse", new ArrayList<>()), dve.getMessage()));
		} catch (Exception e) {
			log.error("*** ending lastLoginUsers method with errors ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("LastLoginResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/workflow/status")
	@PreAuthorize("hasAuthority('VIEW_WORKFLOW')")
	public ResponseEntity<CommonResponse> workflowStatus(@RequestParam String category) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = userDetailsService.workflowStatus(category);
			return ResponseEntity.ok(commonResponse);
		} catch (Exception e) {
			log.error("*** ending sendEmialToUser method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserWorkflowResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PutMapping("/workflow/status")
	@PreAuthorize("hasAuthority('EDIT_WORKFLOW')")
	public ResponseEntity<CommonResponse> workflowStatusUpdate(@RequestParam Long workFlowNumber) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = userDetailsService.workflowStatusUpdate(workFlowNumber);
			return ResponseEntity.ok(commonResponse);
		} catch (Exception e) {
			log.error("*** ending sendEmialToUser method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.USER_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/spend/analytics")
	public ResponseEntity<CommonResponse> userSpendAnalytics(@RequestParam String userId) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = userDetailsService.userSpendAnalytics(userId);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException dve) {
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserSpendAnalyticsResponse", new ArrayList<>()), dve.getMessage()));
		} catch (Exception e) {
			log.error("*** ending userSpendAnalytics method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UserAnalyticsResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/department/users")
	public ResponseEntity<CommonResponse> getDepartmentUsers(@RequestParam String depId)
			throws DataValidationException {
		CommonResponse responce = new CommonResponse();
		try {
			responce = userDetailsService.getDepartmentUsers(depId);
			return ResponseEntity.ok(responce);

		} catch (DataValidationException exception) {
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ListofUsersinDepartmentResponse", new ArrayList<>()), exception.getMessage()));
		} catch (Exception exception) {
			log.error("*** ending ListofUsersinDepartmentResponse method with error ***", exception);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ListofUsersinDepartment", new ArrayList<>()), exception.getMessage()));
		}

	}
}