package saaspe.adaptor.service;

import saaspe.model.CommonResponse;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import saaspe.adaptor.model.CreateHubSpotUserRequest;
import saaspe.adaptor.model.HubSpotSubscriptionRequest;
import saaspe.exception.DataValidationException;

public interface HubSpotWrapperService{

	CommonResponse getAuthUri(String appId);

	CommonResponse getToken(String appId);

	CommonResponse getRefreshToken(String appId);

	CommonResponse getUser(String appId);

	CommonResponse createUser(CreateHubSpotUserRequest hubSpotUserRequest, String appId);

	CommonResponse getByAccesssCode(String licenseCountUrl, String appId);

	CommonResponse getLoginAuditLogs(String userEmail, String appId);

	CommonResponse getSecurityAuditLogs(String userEmail, String appId);

	CommonResponse getByAccountInfoAuditlogs(String appId);
	
	CommonResponse createsubscription(HubSpotSubscriptionRequest hubSpotSubscriptionRequest,String appId);
	
	CommonResponse createunsubscription(HubSpotSubscriptionRequest hubSpotSubscriptionRequest, String appId);

	CommonResponse deleteUser(String userEmail, String appId);

	List<String> getUserIds(String appId) throws JsonProcessingException, DataValidationException ;

	CommonResponse getSubscription(String subscriptionUrl, String appId);
		
	
}

