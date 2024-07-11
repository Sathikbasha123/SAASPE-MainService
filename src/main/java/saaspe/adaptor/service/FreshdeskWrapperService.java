package saaspe.adaptor.service;

import saaspe.model.CommonResponse;

public interface FreshdeskWrapperService {

	CommonResponse getAccountDetails(String appId);

	CommonResponse inviteUser(String appId, String userEmail, String userName);

	CommonResponse revokeUserAccess(String appId, String userEmail);

	CommonResponse getUserList(String appId);

}
