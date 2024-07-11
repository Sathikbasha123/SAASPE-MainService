package saaspe.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import saaspe.constant.Constant;
import saaspe.entity.UserActions;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.model.UserActionListView;
import saaspe.repository.UserActionsRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.service.UserActionService;

@Service
public class UserActionsServiceImpl implements UserActionService {

	@Autowired
	private UserActionsRepository userActionRepo;

	@Autowired
	private UserDetailsRepository userDetailsRepo;

	@Override
	public CommonResponse getUsersAllActionsListBasedOnrole() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication != null && authentication.getPrincipal() instanceof UserLoginDetails)) {
			return new CommonResponse(HttpStatus.UNAUTHORIZED, new Response("Unauthorized", null),
					"User is not authenticated.");
		}
		UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
		UserDetails userRole = userDetailsRepo.findByuserEmail(profile.getEmailAddress());
		if (userRole == null) {
			return new CommonResponse(HttpStatus.NOT_FOUND, new Response("UserDetails not found", null),
					"User details not found.");
		}

		List<UserActions> userActions;
		if ("SUPER_ADMIN".equalsIgnoreCase(userRole.getUserRole())) {
			userActions = userActionRepo.findAll().stream().filter(
					action -> action.getRole() != null && Constant.USER_ROLES.contains(action.getRole().toLowerCase()))
					.collect(Collectors.toList());
		} else {
			userActions = userActionRepo.findByUserEmail(profile.getEmailAddress());
		}

		List<UserActionListView> userActionList = userActions.stream().map(action -> {
			UserActionListView user = new UserActionListView();
			user.setUserEmail(action.getUserEmail());
			user.setRole(action.getRole());
			user.setCreatedOn(action.getCreatedOn());
			user.setStatusCode(action.getStatusCode());
			user.setActionSummary(action.getActionSummary());
			user.setRequest(action.getEndpoint());
			user.setActionCategory(action.getActionCategory());
			user.setAction(action.getAction());
			user.setTraceId(action.getTraceId());
			user.setStatus(action.getStatus());
			return user;
		}).collect(Collectors.toList());

		Collections.sort(userActionList,
				(action1, action2) -> action2.getCreatedOn().compareTo(action1.getCreatedOn()));

		return new CommonResponse(HttpStatus.OK, new Response("UsersAllActionsList", userActionList),
				"UsersActionsDetails Retrieved Successfully");
	}
}
