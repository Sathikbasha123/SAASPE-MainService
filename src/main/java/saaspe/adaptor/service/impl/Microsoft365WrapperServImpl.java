package saaspe.adaptor.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;


import saaspe.adaptor.model.AuthCodeResponse;
import saaspe.adaptor.model.Microsoft365getUserlistResponse;
import saaspe.adaptor.service.Microsoft365WrapperService;
import saaspe.constant.Constant;
import saaspe.entity.AdaptorDetails;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.repository.AdaptorDetailsRepsitory;

@Service
public class Microsoft365WrapperServImpl implements Microsoft365WrapperService {

	@Value("${adapters.host.url}")
	private String adaptorsHost;

	@Value("${microsoft.gettoken.api.url}")
	private String getTokenUrl;

	@Value("${microsoft.authuri.api.url}")
	private String authUrl;

	@Value("${microsoft.getrefreshtoken.api.url}")
	private String refreshTokenUrl;

	@Value("${microsoft.getUser.api.url}")
	private String getUserUrl;

	@Value("${microsoft.createUser.api.url}")
	private String createUserUrl;

	@Value("${microsoft.deleteUser.api.url}")
	private String deleteUserUrl;

	@Value("${microsoft.updateUser.api.url}")
	private String updateUserUrl;

	@Value("${microsoft.subscribedskus.api.url}")
	private String subscribedSkusUrl;
	
	@Value("${microsoft.userLicenseDetails.api.url}")
	private String userLicenseDetailsUrl;

	@Value("${microsoft.assignlicense.api.url}")
	private String assignLicenseUrl;

	@Value("${microsoft.unAssignlicense.api.url}")
	private String unAssignLicenseUrl;

	@Autowired
	private AdaptorDetailsRepsitory adaptorDetailsRepository;

	private static final Logger log = LoggerFactory.getLogger(Microsoft365WrapperServImpl.class);

	public CommonResponse getAuthUri(String appId, String redirectUri) {
		try {
			log.info("call adaptor Hubspot authuri api ");
			AdaptorDetails adaptorDetails = adaptorDetailsRepository.findByApplicationId(appId);
			if (adaptorDetails == null) {
				return new CommonResponse(HttpStatus.NOT_FOUND, null, "Adaptor details not found for the given appId");
			}
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			params.add("redirectUri", redirectUri);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(headers);
			ObjectMapper objectMapper = new ObjectMapper();
			String url = objectMapper.convertValue(
					callClient(adaptorsHost + authUrl, request, params, HttpMethod.GET).getResponse().getData(),
					String.class);
			AuthCodeResponse authcode = new AuthCodeResponse();
			authcode.setUrl(url);
			authcode.setUniqueId(adaptorDetails.getId());
			return new CommonResponse(HttpStatus.OK, new Response("AuthcodeUrlResponse", authcode),
					"Successfully retrieved URL");
		} catch (Exception e) {
			log.info("exception occured while calling AuthUri api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getAuthUri method");
		}
	}

	public CommonResponse getToken(String appId, String code, Long uniqueId) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			params.add("code",code);
			params.add("uniqueId", String.valueOf(uniqueId));
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

	public CommonResponse getUser(String appId)  {
		
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + getUserUrl, request, params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling getUser api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getUser method");
		}
	}

	public CommonResponse createUser(String userEmail, String appId) {
		try {

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			params.add("userEmail",userEmail);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(headers);
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

	public CommonResponse updateUser(@RequestBody Microsoft365getUserlistResponse microsoft365getUserlistResponse,
			String userEmail, String appId) {
		ResponseEntity<CommonResponse> commonResponse = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId);
		params.add(Constant.USEREMAIL, userEmail);
		HttpEntity<?> request = new HttpEntity<>(microsoft365getUserlistResponse, headers);
		RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(adaptorsHost + updateUserUrl);
		String finalUrl = builder.queryParams(params).build().toUriString();
		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.PATCH, request, CommonResponse.class);
			return commonResponse.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("update user", null),
					ex.getLocalizedMessage());
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
	
	

	public CommonResponse getSubscribedSkus(String appId) {
	    try {
	        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	        params.add(Constant.APPID, appId);
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        HttpEntity<?> request = new HttpEntity<>(headers);
	        return callClient(adaptorsHost + subscribedSkusUrl, request, params, HttpMethod.GET);
	    } catch (RestClientException e) {
	        log.error("Rest client exception occurred while calling adaptor service: {}", e.getMessage());
	        return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Internal server error", null),
	                                   "Exception occurred in getSubscribedSkus method");
	    } catch (Exception e) {
	        log.error("Unexpected exception occurred while calling adaptor service: {}", e.getMessage());
	        return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Unexpected error", null),
	                                   "Exception occurred in getSubscribedSkus method");
	    }
	}

	
	public CommonResponse getUserLicenseDetails(String appId,String userEmail) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			params.add(Constant.USEREMAIL, userEmail);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + userLicenseDetailsUrl, request, params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling getUser api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getSubscribedSkus method");
		}
	}

	public CommonResponse assignLicense(String appId,String userEmail,String productName) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			params.add(Constant.USEREMAIL, userEmail);
			params.add("productName", productName);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + assignLicenseUrl, request, params, HttpMethod.POST);
		} catch (Exception e) {
			log.info("exception occured while calling assignLicense api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in assignLicense method");
		}

	}

	public CommonResponse unAssignLicense(String appId,String userEmail,String productName) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("appId", appId);
			params.add("userEmail", userEmail);
			params.add("productName", productName);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + unAssignLicenseUrl, request, params, HttpMethod.POST);
		} catch (Exception e) {
			log.info("exception occured while calling unAssignLicense api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in unAssignLicense method");
		}

	}


}
