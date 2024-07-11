package saaspe.adaptor.service;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import saaspe.adaptor.model.CommonZohoCRMRequest;
import saaspe.model.CommonResponse;

@Service
public interface ZohoCRMWrapperService {

	public void getGrantToken(HttpServletResponse response,String appId)  throws IOException;

	public CommonResponse getaccessToken(String appId,String code) throws  JsonProcessingException;

	public CommonResponse generateRefreshToken(String appId) throws JsonProcessingException;

	public CommonResponse addUserToCRM(String appId,CommonZohoCRMRequest request) ;

	public CommonResponse getUserFromCRM(String appId,String userType)  throws JsonProcessingException ;

	public CommonResponse updateUserInCRM(String accesstoken, CommonZohoCRMRequest request) ;

	public CommonResponse deleteUserInCRM(String appId, String userId);

	public CommonResponse getUserFromCRMById(String appId, String userId);

	public CommonResponse getOrganizationInCRM(String appId);

	public CommonResponse getUserProfiles(String appId);

	public CommonResponse getUserRoles(String appId);

	public CommonResponse getLicenseDetails(String appId);

	public CommonResponse getUserId(String email, String userType, String appId);

	public CommonResponse constructURL(String appId) throws IOException;
}
