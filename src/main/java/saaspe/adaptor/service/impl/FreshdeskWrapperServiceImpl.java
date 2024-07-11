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

import saaspe.adaptor.model.FreshdeskUrls;
import saaspe.adaptor.service.FreshdeskWrapperService;
import saaspe.constant.Constant;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@Service
public class FreshdeskWrapperServiceImpl implements FreshdeskWrapperService {

	@Value("${freshdesk-urls-file}")
	private String freshdeskUrls;

	@Value("${adapters.host.url}")
	private String adaptorsHost;

	@Autowired
	private RestTemplate restTemplate;

	public FreshdeskUrls getFreshdeskUrls() {
		ClassPathResource resource = new ClassPathResource(freshdeskUrls);
		ObjectMapper objectMapper = new ObjectMapper();
		FreshdeskUrls freshdeskUrls = null;
		try {
			freshdeskUrls = objectMapper.readValue(resource.getInputStream(), FreshdeskUrls.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return freshdeskUrls;
	}

	@Override
	public CommonResponse getAccountDetails(String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getFreshdeskUrls().getAccountDetails().replace(Constant.HOST, adaptorsHost));
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
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Get Auth Uri Response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public CommonResponse inviteUser(String appId, String userEmail, String userName) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getFreshdeskUrls().getInviteUser().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		builder.queryParam("userName", userName);
		builder.queryParam("userEmail", userEmail);
		String url = builder.toUriString();
		try {
			ResponseEntity<CommonResponse> response = restTemplate.exchange(url, HttpMethod.POST,
					new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Invite user response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse revokeUserAccess(String appId, String userEmail) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getFreshdeskUrls().getRevokeUserAccess().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		builder.queryParam("userEmail", userEmail);
		String url = builder.toUriString();
		try {
			ResponseEntity<CommonResponse> response = restTemplate.exchange(url, HttpMethod.DELETE,
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
	public CommonResponse getUserList(String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getFreshdeskUrls().getUserList().replace(Constant.HOST, adaptorsHost));
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
					new Response("Get User list response", null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

}
