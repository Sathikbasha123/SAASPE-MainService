package saaspe.adaptor.model;

import lombok.Data;

@Data
public class QuickbooksUrls {
	
	private String authUrl;
	private String accessTokenUrl;
	private String refreshTokenUrl;
	private String usersListUrl;
	private String companyInfoUrl;
	private String licenseUrl;
	private String addUserUrl;
	private String deleteUserUrl;
	private String userDetailsByEmailUrl;

}
