package saaspe.sso;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import saaspe.configuration.AzureConfig;
import saaspe.constant.Constant;
import saaspe.model.CommonResponse;
import saaspe.model.GraphGroupsResponse;
import saaspe.model.Response;
import saaspe.model.UserAccessRoleResponse;
import saaspe.model.Value;

@Service
public class SingleSignOnServiceImpl implements SingleSignOnService {

	@Autowired
	WebClient webClient;

	@Autowired
	RestTemplate restTemplate;

	@Override
	public CommonResponse getAzureUserAccessAndRoles(String token) {
		UserAccessRoleResponse accessRoleResponse = new UserAccessRoleResponse();
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		boolean valid = AzureConfig.isValidToken("Bearer " + token);
		String role = null;
		String access = null;
		if (valid) {
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.setBearerAuth(token);
			HttpEntity<String> entity = new HttpEntity<>(headers);
			GraphGroupsResponse groupsResponse = restTemplate
					.exchange(Constant.GRAPH_GROUP_URL, HttpMethod.GET, entity, GraphGroupsResponse.class).getBody();
			if (groupsResponse == null) {
				throw new NullPointerException("No Data In Groups!");
			}
			for (Value value : groupsResponse.getValue()) {
				role = value.getDisplayName();
			}
			if (role != null) {
				if (role.equalsIgnoreCase("approver")) {
					access = Constant.ROLE_APPROVER;
				} else if (role.equalsIgnoreCase("reviewer")) {
					access = Constant.ROLE_REVIEWER;
				} else if (role.equalsIgnoreCase("superadmin")) {
					access = Constant.ROLE_SUPER_ADMIN;
				} else if (role.equalsIgnoreCase("contributor")) {
					access = Constant.ROLE_CONTRIBUTOR;
				} else if (role.equalsIgnoreCase("support")) {
					access = Constant.ROLE_SUPPORT;
				}
			} else {
				commonResponse.setMessage("Role is Null OR Not Found");
				commonResponse.setStatus(HttpStatus.CONFLICT);
				response.setAction(Constant.USER_ACCESS_AND_ROLES_RESPONSE);
				response.setData(accessRoleResponse);
				commonResponse.setResponse(response);
				return commonResponse;
			}
			if (access == null) {
				throw new NullPointerException("Access has No Data!");
			}
			accessRoleResponse.setRole(role);
			accessRoleResponse.setAccess(access.split(", "));
			commonResponse.setMessage(Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
			commonResponse.setStatus(HttpStatus.OK);
			response.setAction(Constant.USER_ACCESS_AND_ROLES_RESPONSE);
			response.setData(accessRoleResponse);
			commonResponse.setResponse(response);
		} else {
			commonResponse.setMessage("Token Already Expired");
			commonResponse.setStatus(HttpStatus.CONFLICT);
			response.setAction(Constant.USER_ACCESS_AND_ROLES_RESPONSE);
			response.setData(accessRoleResponse);
			commonResponse.setResponse(response);
		}
		return commonResponse;
	}

}
