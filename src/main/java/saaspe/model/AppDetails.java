package saaspe.model;

import lombok.Data;

@Data
public class AppDetails {

	private String clientId;
	private String clientSecret;
	private String redirectUri;
}
