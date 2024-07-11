package saaspe.adaptor.model;

import lombok.Data;

@Data
public class GithubUrls {
    private String authURL;
    private String token;
    private String getUser;
    private String inviteUser;
    private String getMembers;
    private String actionsBilling;
    private String packagesBilling;
    private String sharedStorageBilling;
    private String removeMember;
    private String updateMembership;
    private String getOrgDetails;
}