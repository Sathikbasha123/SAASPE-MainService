package saaspe.service;

import java.io.IOException;


import javax.mail.MessagingException;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonGenerationException;


import freemarker.template.TemplateException;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.ProjectDetailsUpdateRequest;
import saaspe.model.ProjectWorkflowReviewRequest;
import saaspe.model.SingleProjectOnboardingRequest;

public interface ProjectDetailsService {

	CommonResponse projectSingleOnboarding(SingleProjectOnboardingRequest projectOnboardingRequest,
			UserLoginDetails profile) throws DataValidationException;

	CommonResponse projectOnboardReview(String childRequestId, String requestId, UserLoginDetails profile,
			ProjectWorkflowReviewRequest onboardingWorkFlowRequest)
			throws DataValidationException, JsonGenerationException;

	CommonResponse projectReviewerApproverDetailsView(String childRequestId, String requestId, UserLoginDetails profile)
			throws JsonGenerationException, DataValidationException;

	CommonResponse projectReviewerApproverListView(UserLoginDetails profile) throws JsonGenerationException;

	CommonResponse projectDetailsView(String projectId, UserLoginDetails profile) throws DataValidationException;

	CommonResponse getProjectDetailsByDeptId(String departmentId) throws DataValidationException;

	CommonResponse projectApplicationUpdate(ProjectDetailsUpdateRequest projectUpdateRequest, UserLoginDetails profile)
			throws DataValidationException;

	CommonResponse getProjectDetails() throws DataValidationException;

	CommonResponse projectMultipleOnboaring(MultipartFile projectFile, UserLoginDetails profile,
			String departmentRequest) throws DataValidationException, IOException;

	CommonResponse projectSpendAnalytics(String projectId) throws DataValidationException;

	CommonResponse sendBudgetEmail() throws DataValidationException, IOException, TemplateException, MessagingException, InterruptedException;

	CommonResponse updateProjectStatus();
}
