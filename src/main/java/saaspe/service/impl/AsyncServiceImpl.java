package saaspe.service.impl;

import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.graph.models.ServicePrincipal;
import com.microsoft.graph.models.SignIn;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.SignInCollectionPage;
import com.microsoft.graph.requests.UserCollectionPage;

import okhttp3.Request;
import saaspe.constant.Constant;
import saaspe.entity.ApplicationDetails;
import saaspe.entity.AuthenticationEntity;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLastLoginDetails;
import saaspe.repository.ApplicationDetailsRepository;
import saaspe.repository.AuthenticaionServiceRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.repository.UserLastLoginDetailRepository;

@Service
public class AsyncServiceImpl {
	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	private UserLastLoginDetailRepository userLastLoginDetailRepository;
	
	@Autowired
	private AuthenticaionServiceRepository authenticaionServiceRepository;
	
	@Autowired
	private ApplicationDetailsRepository applicationDetailsRepository;
	
	
	public void getUsersId() {
		 ExecutorService executor = Executors.newSingleThreadExecutor();
	        executor.submit(() -> {
		String applicationName = Constant.AZURE_AD;
		AuthenticationEntity azureAd = authenticaionServiceRepository.findBySsoIdentityProvider(applicationName);
		List<UserDetails> userDetailsList = userDetailsRepository.findAllByGraphUserId();
		LinkedList<Option> requestOptions = new LinkedList<>();
		requestOptions.add(new HeaderOption(Constant.CONSISTENCY_LEVEL, Constant.EVENTUAL));
		for (UserDetails mail : userDetailsList) {
			String userMail = "startswith(mail, 'userMail')";
			userMail = userMail.replace("userMail", mail.getUserEmail());
			UserCollectionPage users = getGraphToken(azureAd.getAccessToken()).users().buildRequest(requestOptions)
					.filter(userMail).get();
			for (User user : users.getCurrentPage()) {
				mail.setIdentityId(user.id);
				userDetailsRepository.save(mail);
			}
		}
		 executor.shutdown();
	        });
		return;
	}

	
	
	public void getUsersUnderApplicationLogin() throws InterruptedException {
		
		 ExecutorService executor = Executors.newSingleThreadExecutor();
	        executor.submit(() -> {


		LinkedList<Option> requestOptions = new LinkedList<>();
		requestOptions.add(new HeaderOption(Constant.CONSISTENCY_LEVEL, Constant.EVENTUAL));
		String applicationName = Constant.AZURE_AD;
		AuthenticationEntity azureAd = authenticaionServiceRepository.findBySsoIdentityProvider(applicationName);
		List<UserDetails> userDetails = userDetailsRepository.findByGraphIdentityId();
		List<ApplicationDetails> applicationDetails = applicationDetailsRepository.getGraphApplicationId();
		for (UserDetails user : userDetails) {
			for (ApplicationDetails appId : applicationDetails) {
				ServicePrincipal servicePrincipal = getGraphToken(azureAd.getAccessToken())
						.servicePrincipals(appId.getGraphApplicationId()).buildRequest().get();
				String userIdAndAppID = "(userId eq 'uId' and appId eq 'aId')";
				userIdAndAppID = userIdAndAppID.replace("uId", user.getIdentityId());
				userIdAndAppID = userIdAndAppID.replace("aId", servicePrincipal.appId);
				SignInCollectionPage signInCollectionPage = getGraphToken(azureAd.getAccessToken()).auditLogs()
						.signIns().buildRequest(requestOptions).filter(userIdAndAppID).get();
				if (!signInCollectionPage.getCurrentPage().isEmpty()) {
					UserLastLoginDetails lastLog = userLastLoginDetailRepository
							.findByGraphUserIdAppId(user.getIdentityId(), appId.getApplicationId());
					if (lastLog == null) {
						saveUserLastLogin(user, signInCollectionPage, appId);
					} else {
						List<SignIn> signIn = signInCollectionPage.getCurrentPage();
						signIn.forEach(p -> {
							Date date = Date.from(p.createdDateTime.toInstant());
							lastLog.setLastLoginTime(date);
						});
						lastLog.setUpdatedOn(new Date());
						userLastLoginDetailRepository.save(lastLog);
					}
				}
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		 executor.shutdown();
    });
		
		return;
	}

	private void saveUserLastLogin(UserDetails user, SignInCollectionPage signInCollectionPage,
			ApplicationDetails appId) {
		
		UserLastLoginDetails lastLoginDetails = new UserLastLoginDetails();
		lastLoginDetails.setCreatedOn(new Date());
		lastLoginDetails.setOpID(Constant.SAASPE);
		lastLoginDetails.setBuID(Constant.BUID);
		lastLoginDetails.setUserIdentityId(user.getIdentityId());
		lastLoginDetails.setStartDate(new Date());
		lastLoginDetails.setUserEmail(user.getUserEmail());
		lastLoginDetails.setUserId(user.getUserId());
		lastLoginDetails.setUserName(user.getUserName());
		List<SignIn> signIn = signInCollectionPage.getCurrentPage();
		if (signIn.size() >= 2) {
			SignIn sign = signIn.get(1);
			Date date = Date.from(sign.createdDateTime.toInstant());
			lastLoginDetails.setLastLoginTime(date);
		} else {
			signIn.forEach(p -> {
				Date date = Date.from(p.createdDateTime.toInstant());
				lastLoginDetails.setLastLoginTime(date);
			});
		}
		lastLoginDetails.setUserApplicationId(appId.getGraphApplicationId());
		lastLoginDetails.setApplicationId(appId.getApplicationId());
		userLastLoginDetailRepository.save(lastLoginDetails);
	}
	
	private GraphServiceClient<Request> getGraphToken(String token) {
		return GraphServiceClient.builder().authenticationProvider((URL requestUrl) -> {
			CompletableFuture<String> future = new CompletableFuture<>();
			future.complete(token);
			return future;
		}).buildClient();
	}

}
