package saaspe.adaptor.service;


import saaspe.model.CommonResponse;

public interface DatadogWrapperService {

	CommonResponse createUser(String userEmail, String appId);
	
	CommonResponse getUser(String appId);

	CommonResponse deleteUser(String userEmail, String appId);

}