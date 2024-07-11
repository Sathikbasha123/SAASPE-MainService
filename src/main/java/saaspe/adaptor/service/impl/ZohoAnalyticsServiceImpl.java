package saaspe.adaptor.service.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import saaspe.adaptor.service.ZohoAnalyticsService;
import saaspe.constant.Constant;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.model.ZohoAnalyticsUrl;

@Service
public class ZohoAnalyticsServiceImpl implements ZohoAnalyticsService {

	@Value("${zohoanalytics-urls-file}")
	private String zohoAnalyticsUrls;

	@Value("${adapters.host.url}")
	private String adaptorsHost;

	@Autowired
	private RestTemplate restTemplate;

	public ZohoAnalyticsUrl getZohoAnalyticsUrls() {
		ClassPathResource resource = new ClassPathResource(zohoAnalyticsUrls);
		ObjectMapper objectMapper = new ObjectMapper();
		ZohoAnalyticsUrl zohoAnalyticsUrl = null;
		try {
			zohoAnalyticsUrl = objectMapper.readValue(resource.getInputStream(), ZohoAnalyticsUrl.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return zohoAnalyticsUrl;
	}

	@Override
	public CommonResponse getToken(String appId, String code) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohoAnalyticsUrls().getAccessToken().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		builder.queryParam("code", code);

		String url = builder.toUriString();
		try {
			ResponseEntity<CommonResponse> response = restTemplate.exchange(url, HttpMethod.GET,
					new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Get Token response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse generateToken(String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohoAnalyticsUrls().getGenerateToken().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);

		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Get Token response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse inviteUser(String appId, String userEmail) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohoAnalyticsUrls().getAssignResource().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		builder.queryParam("userEmail", userEmail);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Invite User response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse saveOrgDetail(String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohoAnalyticsUrls().getSaveOrganizationDetails().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Invite User response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse revokeAccess(String userEmail, String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohoAnalyticsUrls().getDeassginResource().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		builder.queryParam("userEmail", userEmail);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Revoke Gitlab Member response", null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse getOrgList(String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohoAnalyticsUrls().getOrganizationList().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Organization List response", null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse getUsersList(String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohoAnalyticsUrls().getUsersList().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Users list response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse getSubscriptionList(String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohoAnalyticsUrls().getSubscriptionDetails().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Subscription list response", null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

}
