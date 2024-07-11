package saaspe.adaptor.service.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import saaspe.adaptor.model.AuthCodeResponse;
import saaspe.adaptor.model.QuickBooksUserRequest;
import saaspe.adaptor.model.QuickbooksUrls;
import saaspe.adaptor.service.QuickBookWrapperService;
import saaspe.constant.Constant;
import saaspe.entity.AdaptorDetails;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.repository.AdaptorDetailsRepsitory;

@Component
public class QuickBookWrapperImpl implements QuickBookWrapperService {

	@Value("${quickbooks-urls-file}")
	private String quickbooksUrls;

	@Value("${adapters.host.url}")
	private String adaptorsHost;

	@Autowired
	private AdaptorDetailsRepsitory adaptorDetailsRepository;

	private static final Logger log = LoggerFactory.getLogger(QuickBookWrapperImpl.class);

	public QuickbooksUrls getQuickbooksUrls() {
		ClassPathResource resource = new ClassPathResource(quickbooksUrls);
		ObjectMapper objectMapper = new ObjectMapper();
		QuickbooksUrls quickbooksUrl = null;
		try {
			quickbooksUrl = objectMapper.readValue(resource.getInputStream(), QuickbooksUrls.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return quickbooksUrl;
	}

	@Override
	public CommonResponse getUrl(String appId, String redirectUri) {
		try {
			AdaptorDetails adaptorDetails = adaptorDetailsRepository.findByApplicationId(appId);
			if (adaptorDetails == null) {
				return new CommonResponse(HttpStatus.NOT_FOUND, null, "Adaptor details not found for the given appId");
			}
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			params.add("redirectUri", redirectUri);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> requestEntity = new HttpEntity<>(headers);
			String authUrl = getQuickbooksUrls().getAuthUrl().replace(Constant.HOST, adaptorsHost);
			ObjectMapper objectMapper = new ObjectMapper();
			String url = objectMapper.convertValue(
					callClient(authUrl, requestEntity, params, HttpMethod.GET).getResponse().getData(), String.class);
			AuthCodeResponse authcode = new AuthCodeResponse();
			authcode.setUrl(url);
			authcode.setUniqueId(adaptorDetails.getId());
			return new CommonResponse(HttpStatus.OK, new Response("AuthcodeUrlResponse", authcode),
					"Successfully retrieved URL");
		} catch (Exception e) {
			log.error("Exception occurred in getUrl method: {}", e.getMessage(), e);
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null,
					"Exception occurred in getUrl method: " + e.getMessage());
		}
	}

	@Override
	public CommonResponse getAccessToken(String appId, String authCode, String realmId, Long uniqueId) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			params.add("authCode", authCode);
			params.add("realmId", realmId);
			params.add("uniqueId", String.valueOf(uniqueId));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(getQuickbooksUrls().getAccessTokenUrl().replace(Constant.HOST, adaptorsHost), request,
					params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling getAccessToken api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getAccessToken method");
		}
	}

	@Override
	public CommonResponse getUsers(String appId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(getQuickbooksUrls().getUsersListUrl().replace(Constant.HOST, adaptorsHost), request,
					params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling get user api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getUsers method");
		}
	}

	@Override
	public CommonResponse getCompanyInfo(String appId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(getQuickbooksUrls().getCompanyInfoUrl().replace(Constant.HOST, adaptorsHost), request,
					params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling get company api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getCompanyInfo method");
		}
	}

	@Override
	public CommonResponse getLicenseCount(String appId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(getQuickbooksUrls().getLicenseUrl().replace(Constant.HOST, adaptorsHost), request, params,
					HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling get License api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getLicenseCount method");
		}
	}

	@Override
	public CommonResponse generateRefreshToken(String appId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(getQuickbooksUrls().getRefreshTokenUrl().replace(Constant.HOST, adaptorsHost), request,
					params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling refresh token api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in generateRefreshToken method");
		}
	}

	public CommonResponse callClient(String url, HttpEntity<?> httpEntity, MultiValueMap<String, String> params, HttpMethod httpMethod) {
		ResponseEntity<CommonResponse> adaptorResponse = null;
		try {
			RestTemplate restTemplate1 = new RestTemplate();
			log.info("Preparing request...");
			if (params != null) {
				UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
				String finalUrl = builder.queryParams(params).build().toUriString();
				adaptorResponse = restTemplate1.exchange(finalUrl, httpMethod, httpEntity, CommonResponse.class);
			} else {
				adaptorResponse = restTemplate1.exchange(url, httpMethod, httpEntity, CommonResponse.class);
			}
			return adaptorResponse.getBody();
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

	@Override
	public CommonResponse addUser(String appId, QuickBooksUserRequest userRequest) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(userRequest, headers);
			return callClient(getQuickbooksUrls().getAddUserUrl().replace(Constant.HOST, adaptorsHost), request, params,
					HttpMethod.POST);
		} catch (Exception e) {
			log.info("exception occured while adding user {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in addUser method");
		}
	}

	@Override
	public CommonResponse deleteUser(String appId, String userEmail) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			params.add("userEmail", userEmail);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(getQuickbooksUrls().getDeleteUserUrl().replace(Constant.HOST, adaptorsHost), request,
					params, HttpMethod.POST);
		} catch (Exception e) {
			log.info("exception occured while deleting user {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in deleteUser method");
		}
	}

	@Override
	public CommonResponse getUserInfoByEmail(String appId, String userEmail) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			params.add("userEmail", userEmail);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(getQuickbooksUrls().getUserDetailsByEmailUrl().replace(Constant.HOST, adaptorsHost),
					request, params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling get user api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getUserInfoByEmail method");
		}
	}

}