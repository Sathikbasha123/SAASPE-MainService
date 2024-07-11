package saaspe.adaptor.service.impl;

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

import com.google.gson.Gson;

import saaspe.adaptor.model.ConfluenceCreateUser;
import saaspe.adaptor.service.ConfluenceWrapperService;
import saaspe.constant.Constant;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@Service
public class ConfluenceWrapperServImpl implements ConfluenceWrapperService{
	
	@Value("${confluence.createUser.api.url}")
	private String createUserUrl;
	
	@Value("${confluence.getUserlist.api.url}")
	private String getUserUrl;
	
	@Value("${confluence.deleteUser.api.url}")
	private String deleteUserUrl;
	
	@Value("${adapters.host.url}")
	private String adaptorsHost;

	private static final Logger log = LoggerFactory.getLogger(ConfluenceWrapperServImpl.class);
	
	public CommonResponse createUser(ConfluenceCreateUser confluenceCreateUser, String appId) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> request = new HttpEntity<>(confluenceCreateUser, headers);
			return callClient(adaptorsHost + createUserUrl, request, params, HttpMethod.POST);
		} catch (Exception e) {
			log.info("exception occured while calling createUser api from adaptor {}", e.getLocalizedMessage());
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response(e.getLocalizedMessage(), null),
					"Exception occurred in createUser method");
		}
	}
	
	
	public CommonResponse getUserList(String appId) {
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
	
	
	public CommonResponse deleteUser(String accountId, String appId) {

		ResponseEntity<CommonResponse> commonResponse = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId);
		params.add(Constant.ACCOUNT_ID , accountId);
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
