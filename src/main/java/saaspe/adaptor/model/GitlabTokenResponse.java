package saaspe.adaptor.model;

import lombok.Data;

@Data
public class GitlabTokenResponse {
	private String access_token;
	private String refresh_token;
	private String expires_in;
	private String scope;
}
