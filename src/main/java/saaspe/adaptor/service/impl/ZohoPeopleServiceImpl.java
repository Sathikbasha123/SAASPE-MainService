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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import saaspe.adaptor.model.ZohoPeopleInviteRequest;
import saaspe.adaptor.service.ZohoPeopleService;
import saaspe.constant.Constant;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.model.ZohoPeopleUrls;

@Service
public class ZohoPeopleServiceImpl implements ZohoPeopleService {

	@Value("${zohopeople-urls-file}")
	private String zohoPeopleUrls;

	@Value("${adapters.host.url}")
	private String adaptorsHost;

	@Autowired
	private RestTemplate restTemplate;

	public ZohoPeopleUrls getZohPeopleUrls() {
		ClassPathResource resource = new ClassPathResource(zohoPeopleUrls);
		ObjectMapper objectMapper = new ObjectMapper();
		ZohoPeopleUrls zohoPeopleUrl = null;
		try {
			zohoPeopleUrl = objectMapper.readValue(resource.getInputStream(), ZohoPeopleUrls.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return zohoPeopleUrl;
	}

	@Override
	public CommonResponse getAuthUri(String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohPeopleUrls().getAuthURL().replace(Constant.HOST, adaptorsHost));
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
	public CommonResponse getToken(String appId, String code) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohPeopleUrls().getAccessToken().replace(Constant.HOST, adaptorsHost));
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
				.fromHttpUrl(getZohPeopleUrls().getGenerateToken().replace(Constant.HOST, adaptorsHost));
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
	public CommonResponse getUsersList(String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohPeopleUrls().getUsersList().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {

			CommonResponse commonResponse = new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			return commonResponse;

		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Get Users list response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse addUser(ZohoPeopleInviteRequest zohoPeopleInviteRequest, String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohPeopleUrls().getAssignResource().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(zohoPeopleInviteRequest, headers),
					CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Add Gitlab memeber response", null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse revokeAccess(String userEmail, String appId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohPeopleUrls().getDeassginResource().replace(Constant.HOST, adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		builder.queryParam("userEmail", userEmail);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(headers), CommonResponse.class);
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
	public CommonResponse findUserByEmail(String appId, String email) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(getZohPeopleUrls().getSearchUser().replace(Constant.HOST, adaptorsHost));
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add(Constant.APPID, appId);
		body.add("email", email);
		String url = builder.queryParams(body).toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Find User response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

}
