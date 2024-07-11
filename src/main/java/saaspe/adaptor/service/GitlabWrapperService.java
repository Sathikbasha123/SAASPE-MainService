package saaspe.adaptor.service;

import saaspe.adaptor.model.AddGitlabMemberRequest;
import saaspe.adaptor.model.GitlabDeleteUserRequest;
import saaspe.model.CommonResponse;

public interface GitlabWrapperService {

	CommonResponse getAuthUri(String appId);

	CommonResponse getToken(String appId);

	CommonResponse generateToken(String appId);

	CommonResponse getUserProfile(String appId);

	CommonResponse addGitlabMember(AddGitlabMemberRequest gitlabMemberRequest, String appId);

	CommonResponse removeGitlabMember(GitlabDeleteUserRequest removeGitlabMember, String appId);

	CommonResponse getUserGroups(String appId);

	CommonResponse getGroupProjects(String appId);

	CommonResponse findUserId(String appId, String userName);

	CommonResponse getAccessRoles();

	CommonResponse getSubscriptionInfo(String appId);

	CommonResponse getUsersList(String appId);

	CommonResponse getResourceMembers(String appId);

	CommonResponse getInvitationsList(String appId);

	CommonResponse revokeInvitation(String appId, String inviteEmail);
	
	boolean hasInvited(String email,String appId);

	
}
