package saaspe.adaptor.service;

import saaspe.adaptor.model.CreateZoomUserRequest;
import saaspe.model.CommonResponse;

public interface ZoomWrapperService {
	
	CommonResponse getAuthUri(String appId,String redirectUri);
	
	CommonResponse getToken(String appId, String code, Long uniqueId);

	CommonResponse getRefreshToken(String appId);

	CommonResponse getUser(String appId);

	 CommonResponse createUser(CreateZoomUserRequest createZoomUserRequest, String appId);
	
	CommonResponse deleteUser(String userEmail, String appId);
	
	CommonResponse getLicenseCount(String appId);

}
