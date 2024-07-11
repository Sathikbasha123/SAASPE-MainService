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
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import saaspe.adaptor.model.AuthCodeResponse;
import saaspe.adaptor.model.CreateZoomUserRequest;
import saaspe.adaptor.service.ZoomWrapperService;
import saaspe.constant.Constant;
import saaspe.entity.AdaptorDetails;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.repository.AdaptorDetailsRepsitory;

@Service
public class ZoomWrapperServiceImpl implements ZoomWrapperService {

	@Value("${adapters.host.url}")
	private String adaptorsHost;
	
	@Value("${zoom.gettoken.api.url}")
	private String getTokenUrl;

	@Value("${zoom.authuri.api.url}")
	private String authUrl;

	@Value("${zoom.getrefreshtoken.api.url}")
	private String refreshTokenUrl;

	@Value("${zoom.getUser.api.url}")
	private String getUserUrl;

	@Value("${zoom.createUser.api.url}")
	private String createUserUrl;

	@Value("${zoom.deleteUser.api.url}")
	private String deleteUserUrl;
	
	@Value("${zoom.getLicenseCount.api.url}")
	private String getLicenseCountUrl;
	
	@Autowired
	private AdaptorDetailsRepsitory adaptorDetailsRepository;

	private static final Logger log = LoggerFactory.getLogger(ZoomWrapperServiceImpl.class);

	public CommonResponse getAuthUri(String appId, String redirectUri) {
		try {
			log.info("call adaptor zoom authuri api ");
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

	public CommonResponse createUser(CreateZoomUserRequest createZoomUserRequest, String appId) {
		try {

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(createZoomUserRequest,headers);
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
		
   public CommonResponse getLicenseCount(String appId)  {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(headers);
			return callClient(adaptorsHost + getLicenseCountUrl, request, params, HttpMethod.GET);
		} catch (Exception e) {
			log.info("exception occured while calling getUser api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in getUser method");
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
	

}
