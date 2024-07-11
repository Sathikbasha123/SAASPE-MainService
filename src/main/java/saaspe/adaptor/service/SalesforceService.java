package saaspe.adaptor.service;

import saaspe.model.CommonResponse;

public interface SalesforceService {

	CommonResponse generateToken(String appId);

	CommonResponse createUser(String appId, String userEmail, String userId, String firstName);

	CommonResponse getUserList(String appId);

	CommonResponse revokeAccess(String appId, String userEmail, String userId);

	CommonResponse getLicenseDetails(String appId);

}
