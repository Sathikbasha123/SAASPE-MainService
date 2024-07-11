package saaspe.adaptor.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import saaspe.adaptor.service.HubSpotWrapperService;
import saaspe.constant.Constant;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.adaptor.model.CreateHubSpotUserRequest;
import saaspe.adaptor.model.HubSpotGetUserlistResponse;
import saaspe.adaptor.model.HubSpotSubscriptionRequest;
import saaspe.model.Response;

@Service
public class HubSpotWrapperServImpl implements HubSpotWrapperService {

	@Value("${hubspot.gettoken.api.url}")
	private String getTokenUrl;

	@Value("${hubspot.authuri.api.url}")
	private String authUrl;

	@Value("${hubspot.getUser.api.url}")
	private String getUserUrl;

	@Value("${hubspot.createUser.api.url}")
	private String createUserUrl;

	@Value("${hubspot.login.auditlogs.api.url}")
	private String loginauditlogUrl;

	@Value("${hubspot.security.auditlogs.api.url}")
	private String securityauditlogUrl;

	@Value("${hubspot.getAccount.info.api.url}")
	private String accountinfoauditlogUrl;

	@Value("${hubspot.getLicense.count.api.url}")
	private String licenseCountUrl;

	@Value("${hubspot.create.subscription.api.url}")
	private String createsubUrl;

	@Value("${hubspot.unsubscription.info.api.url}")
	private String createunsubUrl;

	@Value("${hubspot.deleteUser.api.url}")
	private String deleteUserUrl;

	@Value("${hubspot.getrefreshtoken.api.url}")
	private String refreshTokenUrl;

	@Value("${adapters.host.url}")
	private String adaptorsHost;

	@Value("${hubspot.subscription.info.api.url}")
	private String subscriptionUrl;

	private static final Logger log = LoggerFactory.getLogger(HubSpotWrapperServImpl.class);

	public CommonResponse getAuthUri(String appId) {
		try {
			log.info("call adaptor Hubspot authuri api ");
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + authUrl, request, params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling AuthUri api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getAuthuri method");
		}
	}

	public CommonResponse getToken(String appId) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + getTokenUrl, request, params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling getToken api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getToken method");
		}
	}

	public CommonResponse getRefreshToken(String appId) {

		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		HttpHeaders headers = new HttpHeaders();
		params.add(Constant.APPID, appId);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<?> request = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(adaptorsHost + refreshTokenUrl);
		String finalUrl = builder.queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.GET, request, CommonResponse.class);
			return commonResponse.getBody();
		}

		catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);

		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Generate Token", null),
					ex.getLocalizedMessage());
		}
	}

	public CommonResponse getUser(String appId) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + getUserUrl, request, params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("Exception occurred while calling getUser API from adaptor: {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getUser method");
		}
	}

	public CommonResponse createUser(CreateHubSpotUserRequest createHubSpotUserRequest, String appId) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(createHubSpotUserRequest, headers);
			return callClient(adaptorsHost + createUserUrl, request, params, HttpMethod.POST);
		} catch (Exception e) {
			log.info("exception occured while calling createUser api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in createUser method");
		}
	}

	public CommonResponse deleteUser(String userEmail, String appId) {

		ResponseEntity<CommonResponse> commonResponse = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId);
		params.add(Constant.USEREMAIL, userEmail);
		HttpEntity<?> request = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(adaptorsHost + deleteUserUrl);
		String finalUrl = builder.queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.DELETE, request, CommonResponse.class);
			return commonResponse.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("delete user", null),
					ex.getLocalizedMessage());
		}
	}

	public CommonResponse getByAccesssCode(String url, String appId) {
		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + licenseCountUrl, request, params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling getLicenseCount api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getByAccesssCode method");
		}
	}

	public CommonResponse getSubscription(String url, String appId) {
		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + subscriptionUrl, request, params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling getLicenseCount api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getSubscription method");
		}
	}

	public CommonResponse getByAccountInfoAuditlogs(String appId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + accountinfoauditlogUrl, request, params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling getAccountInfoAuditlogs api from adaptor {}",
					e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getByAccountInfoAuditlogs method");
		}
	}

	public CommonResponse getLoginAuditLogs(String userEmail, String appId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.USEREMAIL, userEmail);
			params.add(Constant.APPID, appId);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + loginauditlogUrl, request, params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling getLoginAuditLogs api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getLoginAuditLogs method");
		}
	}

	public CommonResponse getSecurityAuditLogs(String userEmail, String appId) {
		try {
			log.info("Hi");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.USEREMAIL, userEmail);
			params.add(Constant.APPID, appId);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + securityauditlogUrl, request, params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling getSecurityAuditLogs api from adaptor {}",
					e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getSecurityAuditLogs method");
		}
	}

	public CommonResponse createsubscription(HubSpotSubscriptionRequest hubSpotSubscriptionRequest, String appId) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(hubSpotSubscriptionRequest, headers);
			return callClient(adaptorsHost + createsubUrl, request, params, HttpMethod.POST);
		} catch (Exception e) {
			log.info("exception occured while calling subscription api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in createsubscription method");
		}
	}

	public CommonResponse createunsubscription(HubSpotSubscriptionRequest hubSpotSubscriptionRequest, String appId) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(hubSpotSubscriptionRequest, headers);
			return callClient(adaptorsHost + createunsubUrl, request, params, HttpMethod.POST);
		} catch (Exception e) {
			log.info("exception occured while calling subscription api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in createunsubscription method");
		}
	}

	public CommonResponse callClient(String url, HttpEntity<?> httpEntity, MultiValueMap<String, String> params, HttpMethod httpMethod) {
		ResponseEntity<CommonResponse> adaptorresponse = null;
		try {
			RestTemplate restTemplate1 = new RestTemplate();
			log.info("inside callclient method");
			if (params != null) {
				UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
				String finalUrl = builder.queryParams(params).build().toString();
				log.info(finalUrl);
				adaptorresponse = restTemplate1.exchange(finalUrl, httpMethod, httpEntity, CommonResponse.class);
			} else {
				adaptorresponse = restTemplate1.exchange(url, httpMethod, httpEntity, CommonResponse.class);
			}
			return adaptorresponse.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			log.info("request failed::HttpClientErrorException {}", e.getResponseBodyAsString());
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (Exception e) {
			log.info("exception occured while calling adaptor {}", e.getLocalizedMessage());
			e.printStackTrace();
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in calling adaptor method");
		}
	}

	public List<String> getUserIds(String appId) throws JsonProcessingException, DataValidationException {
		CommonResponse response = getUser(appId);

		if (response != null && response.getStatus() == HttpStatus.OK) {

			ObjectMapper objectMapper = new ObjectMapper();
			List<HubSpotGetUserlistResponse> userList = objectMapper.readValue(
					objectMapper.writeValueAsString(response.getResponse().getData()),
					new TypeReference<List<HubSpotGetUserlistResponse>>() {
					});
			return userList.stream().map(HubSpotGetUserlistResponse::getId).collect(Collectors.toList());
		} else {
			if (response != null) {
				log.error("Failed to get user data. Status code: {}, Response: {}", response.getStatus(),
						response.getResponse());
			} else {
				log.error("Failed to get user data. The response is null.");
			}
			throw new DataValidationException("Failed to get user data", null, HttpStatus.BAD_REQUEST);
		}
	}

}
