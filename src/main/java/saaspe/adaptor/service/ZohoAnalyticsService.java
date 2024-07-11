package saaspe.adaptor.service;

import saaspe.model.CommonResponse;

public interface ZohoAnalyticsService {

	CommonResponse getToken(String appId, String code);

	CommonResponse generateToken(String appId);

	CommonResponse inviteUser(String appId, String userEmail);

	CommonResponse saveOrgDetail(String appId);

	CommonResponse revokeAccess(String userEmail, String appId);

	CommonResponse getOrgList(String appId);

	CommonResponse getUsersList(String appId);

	CommonResponse getSubscriptionList(String appId);

}
