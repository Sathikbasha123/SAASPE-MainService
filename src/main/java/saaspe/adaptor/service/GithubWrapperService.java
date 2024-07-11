package saaspe.adaptor.service;

import saaspe.adaptor.model.GitHubInviteRequestBody;
import saaspe.adaptor.model.RemoveUserRequest;
import saaspe.adaptor.model.UserUpdateRequest;
import saaspe.model.CommonResponse;

public interface GithubWrapperService {

	CommonResponse getAuthUri(String appId);

	CommonResponse getToken(String appId);

	CommonResponse getUserDetails(String appId);

	CommonResponse inviteUser(GitHubInviteRequestBody gitHubInviteRequestBody, String appId);

	CommonResponse getOrganizationMembers(String appId);

	CommonResponse getActionsBilling(String appId);

	CommonResponse getPackagesBilling(String appId);

	CommonResponse getSharedStorageBilling(String appId);

	CommonResponse removeOrganizationMember(String appId,RemoveUserRequest removeUserRequest);

	CommonResponse updateMemberRole(String appId, UserUpdateRequest userUpdateRequest);

	CommonResponse getOrganizationDetails(String appId);

}


