package saaspe.model;

import lombok.Data;

@Data
public class GraphServicePrincipalResponse {

	private String appDescription;

	private String appDisplayName;

	private String appId;

	private String servicePrincipalType;

	private String signInAudience;

}
