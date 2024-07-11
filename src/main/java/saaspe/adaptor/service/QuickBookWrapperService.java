package saaspe.adaptor.service;

import saaspe.adaptor.model.QuickBooksUserRequest;
import saaspe.model.CommonResponse;

public interface QuickBookWrapperService {

	CommonResponse getUrl(String appId,String redirectUri);

	CommonResponse getAccessToken(String appId,String authCode,String realmId, Long uniqueId);
	
	CommonResponse getUsers(String appId);
	
	CommonResponse getCompanyInfo(String appId);
	
	CommonResponse getLicenseCount(String appId);
	
	CommonResponse generateRefreshToken(String appId);

	CommonResponse addUser(String appId, QuickBooksUserRequest userRequest);

	CommonResponse deleteUser(String appId, String id);

	CommonResponse getUserInfoByEmail(String appId, String email);
	
}
