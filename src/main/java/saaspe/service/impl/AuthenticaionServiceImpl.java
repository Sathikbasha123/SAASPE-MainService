package saaspe.service.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.graph.models.ServicePrincipal;
import com.microsoft.graph.models.SignIn;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.ServicePrincipalCollectionPage;
import com.microsoft.graph.requests.ServicePrincipalCollectionRequestBuilder;
import com.microsoft.graph.requests.SignInCollectionPage;
import com.microsoft.graph.requests.UserCollectionPage;

import okhttp3.Request;
import saaspe.constant.Constant;
import saaspe.entity.ApplicationDetails;
import saaspe.entity.ApplicationLogoEntity;
import saaspe.entity.ApplicationProviderDetails;
import saaspe.entity.Applications;
import saaspe.entity.AuthenticationEntity;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLastLoginDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.AzureCredentials;
import saaspe.model.CommonResponse;
import saaspe.model.GraphApplicationLinkRequest;
import saaspe.model.GraphServicePrincipalResponse;
import saaspe.model.IntegrationsResponse;
import saaspe.model.Response;
import saaspe.repository.ApplicationDetailsRepository;
import saaspe.repository.ApplicationLogoRepository;
import saaspe.repository.ApplicationProviderDetailsRepository;
import saaspe.repository.ApplicationsRepository;
import saaspe.repository.AuthenticaionServiceRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.repository.UserLastLoginDetailRepository;
import saaspe.service.AuthenticaionService;

@Service
public class AuthenticaionServiceImpl implements AuthenticaionService {

	@Autowired
	private AuthenticaionServiceRepository authenticaionServiceRepository;

	@Autowired
	private ApplicationDetailsRepository applicationDetailsRepository;

	@Autowired
	private ApplicationLogoRepository applicationLogoRepository;

	@Autowired
	private ApplicationProviderDetailsRepository applicationProviderDetailsRepository;

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	private UserLastLoginDetailRepository userLastLoginDetailRepository;
	
	@Autowired
	private ApplicationsRepository applicationRepository;

	@Value("${authentication.authcode.url}")
	private String authCodeUrl;

	@Value("${authentication.authcode.scope.url}")
	private String scopeUrl;
	
	@Value("${authentication.getClientId}")
	private String clientIds;
	
	@Value("${authentication.getClientSecret}")
	private String clientSecret;
	
	@Value("${authentication.getTenantId}")
	private String tenantIds;
	
	@Value("${authentication.getRedirectUri}")
	private String redirectUri;
	
	@Value("${authentication.getEmailAddress}")
	private String emailAddress;

	@Override
	public CommonResponse getIntegrations(UserLoginDetails profile) throws DataValidationException {
		addAuthCredentials();
		List<IntegrationsResponse> integrationsResponses = new ArrayList<>();
		List<AuthenticationEntity> authenticationEntities = authenticaionServiceRepository.findAll();
		for (AuthenticationEntity entity : authenticationEntities) {
			IntegrationsResponse integrationsResponse = new IntegrationsResponse();
			integrationsResponse.setAppId(entity.getClinetId());
			integrationsResponse.setApplicationName(entity.getSsoIdentityProvider());
			ApplicationLogoEntity logoEntity = applicationLogoRepository
					.findByApplicationName(entity.getSsoIdentityProvider().trim());
			if (logoEntity == null) {
				throw new DataValidationException("Logo not found for" + entity.getSsoIdentityProvider(), null, null);
			}
			ApplicationProviderDetails applicationProviderDetails = applicationProviderDetailsRepository
					.findByProviderId(logoEntity.getProviderId().getProviderId());
			integrationsResponse.setCategory(applicationProviderDetails.getProviderCategory());
			integrationsResponse.setCompanyId(entity.getTenantId());
			integrationsResponse.setDiscription(logoEntity.getDescription());
			integrationsResponse.setHasAdminConsent(entity.getAdminConsent());
			integrationsResponse.setId(entity.getId());
			if (entity.getAccessToken() != null) {
				integrationsResponse.setIsConnected(entity.getIsConnected());
			} else {
				integrationsResponse.setIsConnected(false);
			}
			integrationsResponse.setLogo(logoEntity.getLogoUrl());
			integrationsResponses.add(integrationsResponse);
		}
		return new CommonResponse(HttpStatus.OK, new Response("IntegrationListResponse", integrationsResponses),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

    private void addAuthCredentials() {
		List<AuthenticationEntity> authenticationEntities = authenticaionServiceRepository.findAll();
		 AuthenticationEntity authenticationEntity ;
	    if (authenticationEntities.isEmpty()) {
	    	authenticationEntity = new AuthenticationEntity();
	        authenticationEntity.setClinetId(clientIds);
	        authenticationEntity.setClientSecret(clientSecret);
	        authenticationEntity.setTenantId(tenantIds);
	        authenticationEntity.setRedirectUri(redirectUri);
	        authenticationEntity.setEmailAddress(emailAddress);
	        authenticationEntity.setSsoIdentityProvider("Azure AD");
	        authenticationEntity.setAdminConsent(false);
	        authenticationEntity.setOpID("SAASPE");
	        authenticationEntity.setBuID("BUID");
	        authenticaionServiceRepository.save(authenticationEntity);
	    } 
	}


	@Override
	@Transactional
	public CommonResponse addAdminConsent(AzureCredentials azureCredentials, UserLoginDetails profile)
			throws DataValidationException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		if (azureCredentials.getTenantId().trim() == null) {
			throw new DataValidationException(azureCredentials.getTenantId() + Constant.ISNULL, null, null);
		}
		if (azureCredentials.getAdminConsent() == null) {
			throw new DataValidationException(azureCredentials.getAdminConsent() + Constant.ISNULL, null, null);
		}
		AuthenticationEntity emailRecord = authenticaionServiceRepository.findBySsoIdentityProvider(Constant.AZURE_AD);
		emailRecord.setTenantId(azureCredentials.getTenantId().trim());
		emailRecord.setUpdatedOn(new Date());
		emailRecord.setAdminConsent(azureCredentials.getAdminConsent());
		authenticaionServiceRepository.save(emailRecord);
		response.setAction("Admin Cosnent");
		response.setData("");
		commonResponse.setMessage(Constant.SUCCESS);
		commonResponse.setStatus(HttpStatus.ACCEPTED);
		commonResponse.setResponse(response);
		return commonResponse;
	}

	@Override
	@Transactional
	public CommonResponse addAuthCode(AzureCredentials azureCredentials, UserLoginDetails profile)
			throws DataValidationException, MalformedURLException, URISyntaxException, InterruptedException,
			ExecutionException {
		String authority = authCodeUrl;
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		AuthenticationEntity emailRecord = authenticaionServiceRepository.findBySsoIdentityProvider(Constant.AZURE_AD);
		if (azureCredentials.getAuthCode() != null) {
			emailRecord.setAuthCode(azureCredentials.getAuthCode());
		} else {
			throw new DataValidationException(azureCredentials.getAuthCode() + Constant.ISNULL, null, null);
		}
		Set<String> scopes = new HashSet<>();
		scopes.add(scopeUrl);
		ExecutorService service = Executors.newFixedThreadPool(1);
		AuthenticationContext context = new AuthenticationContext(authority, true, service);
		URI url = new URI(emailRecord.getRedirectUri());
		Future<AuthenticationResult> result = context.acquireTokenByAuthorizationCode(azureCredentials.getAuthCode(),
				url, new ClientCredential(emailRecord.getClinetId(), emailRecord.getClientSecret()), null);
		if (result.get().getAccessToken() == null) {
			throw new DataValidationException("Failed to Fetch Token!", null, null);
		}
		if (result.get().getRefreshToken() == null) {
			throw new DataValidationException("Failed to Fetch Refresh Token!", null, null);
		}
		emailRecord.setAccessToken(result.get().getAccessToken());
		emailRecord.setRefreshToken(result.get().getRefreshToken());
		emailRecord.setAccessTokenExpiry(result.get().getExpiresOnDate().toString());
		emailRecord.setIsConnected(true);
		emailRecord.setUpdatedOn(new Date());
		authenticaionServiceRepository.save(emailRecord);
		response.setAction("Auth Code");
		response.setData("");
		commonResponse.setMessage(Constant.SUCCESS);
		commonResponse.setStatus(HttpStatus.ACCEPTED);
		commonResponse.setResponse(response);
		return commonResponse;
	}

	@Override
	@Transactional
	public CommonResponse addGraphApplication(GraphApplicationLinkRequest applicationLinkRequest,
			UserLoginDetails profile) throws DataValidationException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		ApplicationDetails applicationDetails = applicationDetailsRepository
				.findByApplicationId(applicationLinkRequest.getAppId());
		List<Applications> applications = applicationRepository.findByApplicationId(applicationLinkRequest.getAppId());
		if (applicationLinkRequest.getAppId().length() == 0 || applicationLinkRequest.getGraphAppId().length() == 0) {
			throw new DataValidationException(
					applicationLinkRequest.getAppId() + " OR " + applicationLinkRequest.getGraphAppId() + " is empty",
					null, null);
		}

		if (applicationDetails == null) {
			throw new DataValidationException(applicationLinkRequest.getAppId() + " Doesn't Exist!", null, null);
		}
		applicationDetails.setGraphApplicationId(applicationLinkRequest.getGraphAppId());
		applicationDetails.setSsoEnabled(true);
		applicationDetails.setIdentityProvider(Constant.AZURE_AD);
		applicationDetailsRepository.save(applicationDetails);
		for(Applications application : applications) {
			application.setGraphApplicationId(applicationLinkRequest.getGraphAppId());
			application.setSsoEnabled(true);
			application.setIdentityProvider(Constant.AZURE_AD);
			applicationRepository.save(application);
		}
		response.setAction("ApplicationMappingSuccess");
		response.setData("");
		commonResponse.setMessage(Constant.SUCCESS);
		commonResponse.setStatus(HttpStatus.OK);
		commonResponse.setResponse(response);
		return commonResponse;
	}

	@Override
	public CommonResponse getGraphServicePrincipals(UserLoginDetails profile) {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		AuthenticationEntity emailRecord = authenticaionServiceRepository.findBySsoIdentityProvider(Constant.AZURE_AD);
		LinkedList<Option> requestOptions = new LinkedList<>();
		requestOptions.add(new HeaderOption(Constant.CONSISTENCY_LEVEL, Constant.EVENTUAL));
		List<List<ServicePrincipal>> allservicePrincipalList = new ArrayList<>();
		List<GraphServicePrincipalResponse> graphList = new ArrayList<>();
		ServicePrincipalCollectionPage servicePrincipals = getGraphToken(emailRecord.getAccessToken())
				.servicePrincipals().buildRequest(requestOptions).get();
		do {
			List<ServicePrincipal> currentPageServicePrincipal = servicePrincipals.getCurrentPage();
			Collections.addAll(allservicePrincipalList, currentPageServicePrincipal);
			ServicePrincipalCollectionRequestBuilder nextPage = servicePrincipals.getNextPage();
			servicePrincipals = nextPage == null ? null : nextPage.buildRequest().get();
		} while (servicePrincipals != null);

		for (List<ServicePrincipal> principals : allservicePrincipalList) {
			for (ServicePrincipal service : principals) {
				GraphServicePrincipalResponse principalResponse = new GraphServicePrincipalResponse();
				principalResponse.setAppDisplayName(service.appDisplayName);
				principalResponse.setAppId(service.id);
				principalResponse.setAppDescription(service.appDescription);
				graphList.add(principalResponse);
			}

		}

		response.setData(graphList);
		response.setAction("GraphServicePrincipalsResponse");
		commonResponse.setMessage(Constant.SUCCESS);
		commonResponse.setStatus(HttpStatus.OK);
		commonResponse.setResponse(response);
		return commonResponse;
	}

	public CommonResponse getNewAccessToken() throws MalformedURLException, InterruptedException, ExecutionException {
		String authority = authCodeUrl;
		ExecutorService service = Executors.newFixedThreadPool(1);
		AuthenticationContext context = new AuthenticationContext(authority, true, service);
		String applicationName = Constant.AZURE_AD;
		AuthenticationEntity azureAd = authenticaionServiceRepository.findBySsoIdentityProvider(applicationName);
		Set<String> scopes = new HashSet<>();
		scopes.add(scopeUrl);
		Future<AuthenticationResult> result = context.acquireTokenByRefreshToken(azureAd.getRefreshToken(),
				new ClientCredential(azureAd.getClinetId(), azureAd.getClientSecret()), null);
		azureAd.setAccessToken(result.get().getAccessToken());
		azureAd.setRefreshToken(result.get().getRefreshToken());
		azureAd.setAccessTokenExpiry(result.get().getExpiresOnDate().toString());
		azureAd.setIsConnected(true);
		azureAd.setUpdatedOn(new Date());
		authenticaionServiceRepository.save(azureAd);
		return null;
	}

	private GraphServiceClient<Request> getGraphToken(String token) {
		return GraphServiceClient.builder().authenticationProvider((URL requestUrl) -> {
			CompletableFuture<String> future = new CompletableFuture<>();
			future.complete(token);
			return future;
		}).buildClient();
	}

	

}
