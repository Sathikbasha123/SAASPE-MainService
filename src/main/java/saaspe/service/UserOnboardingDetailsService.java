package saaspe.service;

import java.io.IOException;
import java.text.ParseException;

import javax.mail.MessagingException;

import org.springframework.web.multipart.MultipartFile;


import freemarker.template.TemplateException;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.UserOnboardingDetailsRequest;
import saaspe.model.UserOnboardingWorkFlowRequest;
import saaspe.model.UserSingleOnboardingRequest;

public interface UserOnboardingDetailsService {

	CommonResponse userSingleOnboarding(UserSingleOnboardingRequest userOnboardingDetails, UserLoginDetails profile)
			throws DataValidationException;

	CommonResponse modifyUserOnboardingDetails(UserOnboardingDetailsRequest userOnboardingDetails)
			throws DataValidationException;

	CommonResponse getUserOnboardingDetailsByUserId(String userId) throws DataValidationException;

	CommonResponse removeUserOnBoardingDetailsByUserId(String userId) throws DataValidationException;

	CommonResponse userOnboardingDetails() throws DataValidationException;

	CommonResponse userReviewerApproverListView(UserLoginDetails profile);

	CommonResponse saveUserOnboarding(MultipartFile usersFile, UserLoginDetails profile)
			throws  IOException, ParseException, DataValidationException;

	CommonResponse userOnboardReview(String childRequestId, String requestId, UserLoginDetails profile,
			UserOnboardingWorkFlowRequest onboardingWorkFlowRequest)
			throws DataValidationException,  IOException, TemplateException, MessagingException;

	CommonResponse userReviewerApproverDetailsView(String childRequestId, String requestId, UserLoginDetails profile) throws DataValidationException;

}
