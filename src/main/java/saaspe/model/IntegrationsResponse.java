package saaspe.model;

import lombok.Data;

@Data
public class IntegrationsResponse {

	private Integer id;

	private String appId;

	private Boolean hasAdminConsent;

	private Boolean isConnected;

	private String companyId;

	private String applicationName;

	private String logo;

	private String category;

	private String discription;

}
