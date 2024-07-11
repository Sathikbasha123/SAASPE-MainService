package saaspe.service;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.web.multipart.MultipartFile;


import freemarker.template.TemplateException;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.AuthenticationException;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.DeptOnboardingWorkFlowRequest;
import saaspe.model.DeptUserPasswordRequest;
import saaspe.model.ListOfCreateDeptartmentRequest;

public interface DepartmentService {

	CommonResponse getDepartmentListView();

	CommonResponse getDepartmentOverview(String departmentId, String request) throws DataValidationException;

	CommonResponse departmentSingleOnboarding(ListOfCreateDeptartmentRequest createDeptartmentRequest,
			UserLoginDetails profile) throws DataValidationException,  IOException, TemplateException;

	CommonResponse departmentOnboardReview(String childRequestId, String requestId, UserLoginDetails profile,
			DeptOnboardingWorkFlowRequest onboardingWorkFlowRequest)
			throws DataValidationException, 
			IOException, TemplateException, MessagingException;

	CommonResponse departmentReviewerApproverListView(UserLoginDetails profile);

	CommonResponse departmentReviewerApproverDetailsView(String childRequestId, String requestId,
			UserLoginDetails profile) throws DataValidationException;

	CommonResponse saveDepartmentOnboarding(MultipartFile departmentFile, UserLoginDetails profile)
			throws IOException, DataValidationException;

	CommonResponse departmentUserListWithoutLicenseMapped(String licenseId, String departmentId)
			throws DataValidationException;

	CommonResponse createPassword(DeptUserPasswordRequest pwdRequest)
			throws DataValidationException, 
			IOException, TemplateException, AuthenticationException;

	CommonResponse deptApplicationUsage(String deptId) throws DataValidationException;

	CommonResponse deptSpendAnalytics(String deptId) throws DataValidationException;

	CommonResponse deptBudgetAnalytics(String deptId) throws DataValidationException;

	CommonResponse sendBudgetEmail() throws IOException, MessagingException, TemplateException, InterruptedException;
}
