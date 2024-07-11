package saaspe.service;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.mail.MessagingException;

import com.microsoft.azure.storage.StorageException;

import freemarker.template.TemplateException;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.ApplicationContractDetailsUpdateRequest;
import saaspe.model.CommonResponse;
import saaspe.model.ContractOnboardingRequest;
import saaspe.model.DeptOnboardingWorkFlowRequest;

public interface ContractService {

	CommonResponse modifyApplicationContractDetails(String contractId,
			ApplicationContractDetailsUpdateRequest updateRequest) throws DataValidationException;

	CommonResponse getApplicationContractDetailView(String contractId, String category)
			throws DataValidationException, URISyntaxException, StorageException;

	CommonResponse getContractsListView() throws DataValidationException;

	CommonResponse upCommingContractRenewalReminderEmail()
			throws IOException, TemplateException, DataValidationException, MessagingException, InterruptedException;

	CommonResponse getContractsByApplicationId(String applicationId) throws DataValidationException ;

	CommonResponse addApplicationContract(ContractOnboardingRequest contractOnboardingRequest, String applicationId,
			UserLoginDetails profile) throws DataValidationException, java.text.ParseException ;

	CommonResponse contractReviewerApproverListView(UserLoginDetails profile) ;

	CommonResponse contractOnboardReview(String requestId, UserLoginDetails profile,
			DeptOnboardingWorkFlowRequest onboardingWorkFlowRequest)
			throws DataValidationException, TemplateException, java.text.ParseException, IOException;

	CommonResponse contractReviewerApproverDetailsView(String requestId, UserLoginDetails profile)
			throws URISyntaxException, StorageException, DataValidationException;

	CommonResponse updateContractStatus() throws DataValidationException;

}
