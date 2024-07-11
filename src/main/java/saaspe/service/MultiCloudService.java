package saaspe.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import freemarker.template.TemplateException;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.BudgetRequest;
import saaspe.model.CloudOnboardRequest;
import saaspe.model.CommonResponse;
import saaspe.model.OptimizeRequest;
import saaspe.model.OptimizeRequestAws;
import saaspe.model.UserSubscriptionRequest;

public interface MultiCloudService {

	CommonResponse addUserSubscriptionDetails(UserSubscriptionRequest request) throws DataValidationException;

	CommonResponse getTotalSpendBasedOnServiceName() throws DataValidationException, ParseException;

	CommonResponse getCountBasedOnSubscriptionType(HttpServletRequest request) throws DataValidationException;

	CommonResponse getDetails() throws DataValidationException;

	CommonResponse getSpendingHistory() throws DataValidationException;

	CommonResponse getQuaterlyAmountByService() throws DataValidationException, ParseException;

	CommonResponse getRecentSpendingHistory() throws DataValidationException;

	CommonResponse spendingHistory(String renewalType) throws DataValidationException;

	CommonResponse getBudgetsByVendor(String category) throws ParseException;

	CommonResponse getSubscriptions() throws DataValidationException;

	CommonResponse getRecommendations(String category) throws DataValidationException;

	CommonResponse getAzureForecastData(String subscriptionId, Date startDate, Date endDate, String category)
			throws DataValidationException, ParseException;

	CommonResponse getResources(String category) throws DataValidationException;

	CommonResponse getCostSpendPerYearlyAndMonthly() throws DataValidationException, ParseException;

	CommonResponse optimizeEmailTriggerAzure(OptimizeRequest optimizeRequest)
			throws IOException, TemplateException, MessagingException;

	CommonResponse optimizeEmailTriggerAws(OptimizeRequestAws optimizeRequest)
			throws IOException, TemplateException, MessagingException;

	CommonResponse budgetEmailTriggerAzure()
			throws IOException, TemplateException, InterruptedException, MessagingException;

	CommonResponse getAllSupportedVendors() throws DataValidationException;

	CommonResponse cloudOnboard(CloudOnboardRequest cloudOnboardRequest, UserLoginDetails profile)
			throws DataValidationException;

	CommonResponse getIntegratedClouds() throws DataValidationException;

	CommonResponse budgetEmailTriggerAws()
			throws IOException, TemplateException, InterruptedException, MessagingException;

	CommonResponse removeCloudVendor(String cloudId) throws DataValidationException;

	CommonResponse getCreateBudget(BudgetRequest budgetRequest) throws DataValidationException;
	
	CommonResponse getMonthlySpendingHistory() throws DataValidationException;

}
