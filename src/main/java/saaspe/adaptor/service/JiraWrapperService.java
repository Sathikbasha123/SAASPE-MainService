package saaspe.adaptor.service;

import saaspe.adaptor.model.JiraCreateUserRequest;
import saaspe.model.CommonResponse;

public interface JiraWrapperService {

	CommonResponse createUser(JiraCreateUserRequest jiraCreateUserRequest, String appId);

	CommonResponse addUserToGroup(String productName, String accountId, String appId);

	CommonResponse getAllUser(String appId);

	CommonResponse removeUserFromGroup(String accountId, String appId);
		
	
}
