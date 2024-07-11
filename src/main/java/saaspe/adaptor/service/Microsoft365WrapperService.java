package saaspe.adaptor.service;


import saaspe.adaptor.model.Microsoft365getUserlistResponse;
import saaspe.model.CommonResponse;

public interface Microsoft365WrapperService {

	CommonResponse getAuthUri(String appId,String redirectUri);
	
	CommonResponse getToken(String appId, String code, Long uniqueId);

	CommonResponse getRefreshToken(String appId);

	CommonResponse getUser(String appId);

	CommonResponse createUser(String userEmail, String appId);
	
	CommonResponse deleteUser(String userEmail, String appId);
	
	CommonResponse updateUser(Microsoft365getUserlistResponse microsoft365getUserlistResponse,String userEmail, String appId);
	
	CommonResponse getSubscribedSkus(String appId);
	
	CommonResponse getUserLicenseDetails(String appId,String userEmail);

	CommonResponse assignLicense(String appId,String userEmail,String productName);

	CommonResponse unAssignLicense(String appId, String userEmail, String productName);
		

}
