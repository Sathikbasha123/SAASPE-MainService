package saaspe.model;

import lombok.Data;

@Data
public class ZohoAnalyticsUrl {
	private String accessToken;
	private String generateToken;
	private String assignResource;
	private String deassginResource;
	private String usersList;
	private String organizationList;
	private String subscriptionDetails;
	private String saveOrganizationDetails; 
}
