package saaspe.adaptor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZohoCRMUrls {

	@NonNull
	private String getGrantCode;
	@NonNull
	private String getToken;
	@NonNull
	private String generateAccessToken;
	@NonNull
	private String createUser;
	@NonNull
	private String updateUser;
	@NonNull
	private String getUser;
	@NonNull
	private String getOrganizationUser;
	@NonNull
	private String deleteUser;
	@NonNull
	private String getProfiles;
	@NonNull
	private String getRoles;
	@NonNull
	private String getLicenseDetails;
	@NonNull
	private String getOrganizationDetails;
	@NonNull
	private String getUserId;
	@NonNull
	private String createUserByURL;
}
