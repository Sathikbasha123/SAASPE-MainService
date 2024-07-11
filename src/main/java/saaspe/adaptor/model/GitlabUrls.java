package saaspe.adaptor.model;

import lombok.Data;

@Data
public class GitlabUrls {
	private String authURL;
	private String accessToken;
	private String generateToken;
	private String userProfile;
	private String assignResource;
	private String deassginResource;
	private String userGroups;
	private String groupProject;
	private String findUser;
	private String accessRoles;
	private String subscriptionInfo;
	private String usersList;
	private String resourceMembers;
	private String invitationList;
	private String revokeInvitation;
}
