package saaspe.adaptor.model;

import lombok.Data;

@Data
public class GitHubInviteRequestBody {

	private String role;
	private String email;
	private int[] teamIds;
	private int inviteeId;
}
