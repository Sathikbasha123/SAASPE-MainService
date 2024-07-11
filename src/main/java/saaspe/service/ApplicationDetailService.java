package saaspe.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

import javax.mail.MessagingException;

import org.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.storage.StorageException;

import freemarker.template.TemplateException;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.AdaptorValue;
import saaspe.model.ApplicationDetailsUpdateRequest;
import saaspe.model.ApplicationIdsRemoveRequest;
import saaspe.model.CommonResponse;
import saaspe.model.Credentails;
import saaspe.model.DeptOnboardingWorkFlowRequest;
import saaspe.model.PurchasedApplcationRequest;
import saaspe.model.SingleNewApplicationOnboardingRequest;

public interface ApplicationDetailService {

	CommonResponse modifyApplicationDetails(String applicationId, ApplicationDetailsUpdateRequest updateRquest)
			throws DataValidationException;

	CommonResponse removeApplicationDetails(String applicationId) throws DataValidationException;

	CommonResponse getApplicationListView();

	CommonResponse deleteAllByApplicationIds(ApplicationIdsRemoveRequest applicationIds)
			throws DataValidationException, JsonProcessingException;

	CommonResponse getApplicationOverview(String applicationId, String category, UserLoginDetails profile);

	CommonResponse newApplicationOnboarding(SingleNewApplicationOnboardingRequest onboardingRequest,
			UserLoginDetails profile) throws DataValidationException, JsonGenerationException;

	CommonResponse purchasedApplicationOnboard(PurchasedApplcationRequest onboardingRequest, UserLoginDetails profile)
			throws DataValidationException, JsonGenerationException, ParseException;

	CommonResponse saveApplicatoinOnboarding(MultipartFile applicationFile, UserLoginDetails profile)
			throws IOException, DataValidationException, JSONException, ParseException;

	CommonResponse applicatoinReviewerApproverListView(UserLoginDetails profile);

	CommonResponse applicatoinReviewerApproverDetailsView(String childRequestId, String requestId,
			UserLoginDetails profile) throws URISyntaxException, StorageException, JsonGenerationException, DataValidationException;

	CommonResponse applicationOnboardReview(String childRequestId, String requestId, UserLoginDetails profile,
			DeptOnboardingWorkFlowRequest onboardingWorkFlowRequest)
			throws DataValidationException, MessagingException, IOException, TemplateException, ParseException;

	void storeCredentials(Credentails credentails);

	CommonResponse provideKeys(String applicationId) throws JsonProcessingException, DataValidationException;

	CommonResponse saveNewAppDetails(AdaptorValue request, String applicationId)
			throws DataValidationException, JsonProcessingException;

	CommonResponse updateAppCredentials();

	CommonResponse updateRefreshTokens();

}
