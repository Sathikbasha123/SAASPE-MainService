package saaspe.adaptor.model;

import lombok.Data;

@Data
public class AdaptorValue {

	private String appId;

	private String applicationName;

	private String username;

	private String email;

	private String authType;

	private String apiToken;

	private String role;

	private String clientId;

	private String clientSecret;

	private String domainUrl;

	private String redirectUrl;

	private String apiKey;
}
