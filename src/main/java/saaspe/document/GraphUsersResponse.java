package saaspe.document;

import lombok.Data;

@Data
public class GraphUsersResponse {

	private String id;

	private String userId;

	private String mail;

	private String displayName;

	private String userPrincipalName;


}
