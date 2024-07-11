package saaspe.adaptor.service;

import saaspe.adaptor.model.ZohoPeopleInviteRequest;
import saaspe.model.CommonResponse;

public interface ZohoPeopleService {

	CommonResponse getAuthUri(String appId);

	CommonResponse getToken(String appId, String code);

	CommonResponse generateToken(String appId);

	CommonResponse getUsersList(String appId);

	CommonResponse addUser(ZohoPeopleInviteRequest zohoPeopleInviteRequest, String appId);

	CommonResponse revokeAccess(String userEmail, String appId);

	CommonResponse findUserByEmail(String appId, String userEmail);

}
