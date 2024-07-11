package saaspe.adaptor.model;

import lombok.Data;

@Data
public class HubSpotTokenResponse {
	private String token_type;
	private String access_token;
	private String refresh_token;
	private Long expires_in;
}
