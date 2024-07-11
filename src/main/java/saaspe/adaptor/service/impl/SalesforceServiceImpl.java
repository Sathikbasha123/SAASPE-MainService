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

import saaspe.adaptor.model.SalesforceUrls;
import saaspe.adaptor.service.SalesforceService;
import saaspe.constant.Constant;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@Service
public class SalesforceServiceImpl implements SalesforceService {

	@Value("${salesforce-urls-file}")
	private String salesforceUrls;

	@Value("${adapters.host.url}")
	private String adaptorsHost;

	@Autowired
	private RestTemplate restTemplate;

	public SalesforceUrls getSalesforceUrls() {
		ClassPathResource resource = new ClassPathResource(salesforceUrls);
		ObjectMapper objectMapper = new ObjectMapper();
		SalesforceUrls salesforceUrl = null;
		try {
			salesforceUrl = objectMapper.readValue(resource.getInputStream(), SalesforceUrls.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return salesforceUrl;
	}

	@Override
	public CommonResponse generateToken(String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getSalesforceUrls().getGenerateToken().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		try {
			ResponseEntity<CommonResponse> response = restTemplate.exchange(url, HttpMethod.POST,
					new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Generate token Response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse createUser(String appId, String userEmail, String userId, String firstName) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getSalesforceUrls().getCreateUser().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		builder.queryParam("userEmail", userEmail);
		builder.queryParam("userName", userId);
		builder.queryParam("firstName", firstName);
		String url = builder.toUriString();
		try {
			ResponseEntity<CommonResponse> response = restTemplate.exchange(url, HttpMethod.POST,
					new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Create user Response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse getUserList(String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getSalesforceUrls().getUserList().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		try {
			ResponseEntity<CommonResponse> response = restTemplate.exchange(url, HttpMethod.GET,
					new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("User list response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse revokeAccess(String appId, String userEmail, String userId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getSalesforceUrls().getRevokeAccess().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		builder.queryParam("userEmail", userEmail);
		builder.queryParam("userName", userId);
		String url = builder.toUriString();
		try {
			ResponseEntity<CommonResponse> response = restTemplate.exchange(url, HttpMethod.PUT,
					new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Revoke user access response", null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse getLicenseDetails(String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getSalesforceUrls().getLicenseDetails().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		try {
			ResponseEntity<CommonResponse> response = restTemplate.exchange(url, HttpMethod.GET,
					new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Get license details response", null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

}
