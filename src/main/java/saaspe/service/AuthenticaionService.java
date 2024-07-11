package saaspe.service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.AzureCredentials;
import saaspe.model.CommonResponse;
import saaspe.model.GraphApplicationLinkRequest;

public interface AuthenticaionService {

	CommonResponse getIntegrations(UserLoginDetails profile) throws DataValidationException;

	CommonResponse addAdminConsent(AzureCredentials azureCredentials, UserLoginDetails profile)
			throws DataValidationException;

	CommonResponse addAuthCode(AzureCredentials azureCredentials, UserLoginDetails profile) throws DataValidationException, MalformedURLException, URISyntaxException, InterruptedException, ExecutionException ;

	CommonResponse addGraphApplication(GraphApplicationLinkRequest applicationLinkRequest, UserLoginDetails profile) throws DataValidationException;

	CommonResponse getGraphServicePrincipals(UserLoginDetails profile) ;

	CommonResponse getNewAccessToken() throws MalformedURLException, InterruptedException, ExecutionException ;


}
