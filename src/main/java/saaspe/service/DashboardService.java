package saaspe.service;

import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.SimilarApplicationsRequest;

public interface DashboardService {

	CommonResponse getTopAppsBySpend();

	CommonResponse getTopAppsByMetrics();

	CommonResponse getInvoices();

	CommonResponse getTopAppsRecentlyAdded();

	CommonResponse getContractDetails();

	CommonResponse getUpcomingRenewals();

	CommonResponse getDashboardView();

	CommonResponse getSimilarApplications(SimilarApplicationsRequest similarapplicationsrequest);

	CommonResponse getRequestTracking(String category) throws DataValidationException;

	CommonResponse dashBoardTopCardAnalystics() throws DataValidationException;

	CommonResponse dashBoardDepartmentBudgetAnalystics() throws DataValidationException;

	CommonResponse dashBoardExpenseByDepartmentAnalystics() throws DataValidationException;

	CommonResponse dashboardSpendHistory() throws DataValidationException;

	CommonResponse usageTrends() throws DataValidationException;

}