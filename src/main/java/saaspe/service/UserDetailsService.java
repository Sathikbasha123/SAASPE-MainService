package saaspe.service;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import freemarker.template.TemplateException;
import lombok.NonNull;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.AuthenticationException;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.CreateAdminRequest;
import saaspe.model.UserDetailsRequest;
import saaspe.model.UserEmailsRemoveRequest;
import saaspe.model.UserLastLoginRequest;
import saaspe.model.UserOnboardingRequest;
import saaspe.model.UserUpdateRequest;
import saaspe.model.updateUserOwnershipRequest;

public interface UserDetailsService {

	CommonResponse addUserDetails(@Valid UserDetailsRequest user)
			throws DataValidationException, AuthenticationException;

	CommonResponse modifyUserDetails(String userId, UserUpdateRequest userUpateRequest) throws DataValidationException;

	CommonResponse removeUserDetailsByUserEmail(String userEmail) throws DataValidationException;

	CommonResponse getUserDetails() throws DataValidationException;

	CommonResponse getUserDetailsByEmial(String email);

	CommonResponse getUsersDetialView() throws DataValidationException;

	CommonResponse getTopAppsByUsercount();

	CommonResponse getUserListView() throws DataValidationException;

	CommonResponse saveUserOnboardingData(List<UserOnboardingRequest> userOnboardingRequests)
			throws DataValidationException;

	CommonResponse deleteByUserEmails(UserEmailsRemoveRequest userEmails) throws DataValidationException, JsonProcessingException;

	CommonResponse getUserDetailsOverview(String userId) throws DataValidationException;

	CommonResponse getProfile(UserLoginDetails profile, HttpServletRequest provider) throws DataValidationException;

	CommonResponse getOwnerShipList(String userId) throws DataValidationException;

	CommonResponse getOwnerShipTransfer(updateUserOwnershipRequest ownershipRequest)
			throws DataValidationException, 
			IOException, TemplateException, MessagingException;

	CommonResponse getAllAdmins() throws DataValidationException;

	CommonResponse getPermissionByRole(@NonNull String role) throws DataValidationException;

	CommonResponse createAdminUsers(CreateAdminRequest adminRequest, UserLoginDetails profile)
			throws DataValidationException, 
			IOException, TemplateException, MessagingException;

	CommonResponse sendEmialToUser(UserLastLoginRequest userLastLoginRequest)
			throws  DataValidationException, IOException, TemplateException, MessagingException;

	CommonResponse lastLoginUsers(String dateRange, String applicationName) throws DataValidationException;

	CommonResponse workflowStatus(String category);

	CommonResponse workflowStatusUpdate(Long workFlowNumber) throws DataValidationException;

	CommonResponse userSpendAnalytics(String userId) throws DataValidationException;

	CommonResponse getDepartmentUsers(String depId) throws DataValidationException;

}
