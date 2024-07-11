package saaspe.adaptor.service.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import saaspe.adaptor.model.CommonUserResponse;
import saaspe.adaptor.model.CommonZohoCRMRequest;
import saaspe.adaptor.model.ZohoCRMUrls;
import saaspe.adaptor.service.ZohoCRMWrapperService;
import saaspe.constant.Constant;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@Service
public class ZohoCRMWrapperServiceImpl implements ZohoCRMWrapperService {

	private static final Logger log = LoggerFactory.getLogger(ZohoCRMWrapperServiceImpl.class);

	@Value("${zohocrm-urls-file}")
	private String zohocrmUrlFile;

	@Value("${adapters.host.url}")
	private String adaptorsHost;

	public ZohoCRMUrls getZohoCRMUrl() {
		ClassPathResource resource = new ClassPathResource(zohocrmUrlFile);
		ObjectMapper objectMapper = new ObjectMapper();
		ZohoCRMUrls zohocrmUrls = null;
		try {
			zohocrmUrls = objectMapper.readValue(resource.getInputStream(), ZohoCRMUrls.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return zohocrmUrls;
	}

	@Override
	public void getGrantToken(HttpServletResponse response, String appId) throws IOException {

		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add(Constant.APPID, appId);

			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZohoCRMUrl().getGetGrantCode().replace(Constant.HOST,adaptorsHost));
			String finalUrl = builder.queryParams(params).build().toUriString();

			response.sendRedirect(finalUrl);
		} catch (IOException | IllegalStateException e) {

			e.printStackTrace();
		}
	}

	@Override
	public CommonResponse getaccessToken(String appId, String code)
			throws JsonProcessingException {

		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId);
		params.add("code", code);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZohoCRMUrl().getGetToken().replace(Constant.HOST,adaptorsHost));
		String finalUrl = builder.queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.POST, new HttpEntity<>(headers),
					CommonResponse.class);
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

	@Override
	public CommonResponse generateRefreshToken(String appId) throws JsonProcessingException {

		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId.trim());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZohoCRMUrl().getGenerateAccessToken().replace(Constant.HOST,adaptorsHost));
		String finalUrl = builder.queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.POST, new HttpEntity<>(headers),
					CommonResponse.class);
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

	@Override
	public CommonResponse getUserFromCRM(String appId, String userType) {

		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("type", userType);
		params.add(Constant.APPID, appId);
		HttpHeaders headers = new HttpHeaders();
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZohoCRMUrl().getGetOrganizationUser().replace(Constant.HOST,adaptorsHost));
		String finalUrl = builder.queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.GET, new HttpEntity<>(headers),
					CommonResponse.class);
			return commonResponse.getBody();
		}

		catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);

		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Fetch Organization details", null), ex.getLocalizedMessage());
		}

	}

	@Override
	public CommonResponse getUserFromCRMById(String appId, String userId) {

		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId);
		HttpHeaders headers = new HttpHeaders();
		RestTemplate restTemplate = new RestTemplate();
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getZohoCRMUrl().getGetUser().replace(Constant.HOST,adaptorsHost));
		String finalUrl = builder.pathSegment(userId).queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.GET, new HttpEntity<>(headers),
					CommonResponse.class);
			return commonResponse.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			log.info("request failed::HttpClientErrorException {}", e.getResponseBodyAsString());
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Fetch user details", null),
					ex.getLocalizedMessage());
		}

	}

	@Override
	public CommonResponse deleteUserInCRM(String appId, String userId) {

		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId);
		HttpHeaders headers = new HttpHeaders();
		RestTemplate restTemplate = new RestTemplate();
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getZohoCRMUrl().getDeleteUser().replace(Constant.HOST,adaptorsHost));
		String finalUrl = builder.pathSegment(userId).queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.DELETE, new HttpEntity<>(headers),
					CommonResponse.class);
			return commonResponse.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Update user to organization", null), ex.getLocalizedMessage());
		}
	}

	@Override
	public CommonResponse getOrganizationInCRM(String appId) {

		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId.trim());
		HttpHeaders headers = new HttpHeaders();
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZohoCRMUrl().getGetOrganizationDetails().replace(Constant.HOST,adaptorsHost));
		String finalUrl = builder.queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.GET, new HttpEntity<>(headers),
					CommonResponse.class);
			return commonResponse.getBody();
		}

		catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);

		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Fetch Organization details", null), ex.getLocalizedMessage());
		}

	}

	@Override
	public CommonResponse getUserProfiles(String appId) {

		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId.trim());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZohoCRMUrl().getGetProfiles().replace(Constant.HOST,adaptorsHost));
		String finalUrl = builder.queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.GET, new HttpEntity<>(headers),
					CommonResponse.class);
			return commonResponse.getBody();
		}

		catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);

		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Fetch profile details", null),
					ex.getLocalizedMessage());
		}

	}

	@Override
	public CommonResponse getUserRoles(String appId) {
		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId.trim());
		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZohoCRMUrl().getGetRoles().replace(Constant.HOST,adaptorsHost));
		String finalUrl = builder.queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.GET, new HttpEntity<>(headers),
					CommonResponse.class);
			return commonResponse.getBody();
		}

		catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);

		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Fetch role details", null),
					ex.getLocalizedMessage());
		}

	}

	@Override
	public CommonResponse getLicenseDetails(String appId) {

		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId);
		HttpHeaders headers = new HttpHeaders();
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZohoCRMUrl().getGetLicenseDetails().replace(Constant.HOST,adaptorsHost));
		String finalUrl = builder.queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.GET, new HttpEntity<>(headers),
					CommonResponse.class);
			return commonResponse.getBody();
		}

		catch (HttpClientErrorException.Unauthorized e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);

		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Fetch license details", null),
					ex.getLocalizedMessage());
		}

	}

	@Override
	public CommonResponse addUserToCRM(String appId, CommonZohoCRMRequest addUserRequest) {
	
		ResponseEntity<CommonResponse> commonResponse = null;
		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<CommonZohoCRMRequest> request = new HttpEntity<>(addUserRequest, headers);
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZohoCRMUrl().getCreateUser().replace(Constant.HOST,adaptorsHost));
        builder.queryParam(Constant.APPID, appId.trim());
		String finalUrl = builder.build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.POST, request, CommonResponse.class);
			return commonResponse.getBody();
		}

		catch (HttpClientErrorException.BadRequest e) {
			CommonUserResponse c = new Gson().fromJson(e.getResponseBodyAsString(), CommonUserResponse.class);
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Add user to organization", null),
					c.getMessage());
		}

		catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Add user to organization", null),
					ex.getMessage());
		}
	}

	@Override
	public CommonResponse updateUserInCRM(String appId, CommonZohoCRMRequest updateRequest) {
		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<CommonZohoCRMRequest> request = new HttpEntity<>(updateRequest, headers);
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZohoCRMUrl().getUpdateUser().replace(Constant.HOST,adaptorsHost));
		String finalUrl = builder.build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.PUT, request, CommonResponse.class);
			return commonResponse.getBody();
		}

		catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);

		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Update user details", null),
					ex.getLocalizedMessage());
		}

	}

	@Override
	public CommonResponse getUserId(String email, String userType, String appId) {
		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("userType", userType);
		params.add("email", email);
		params.add(Constant.APPID, appId);
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<?> request = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZohoCRMUrl().getGetUserId().replace(Constant.HOST,adaptorsHost));
		String finalUrl = builder.queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.GET, request, CommonResponse.class);
			return commonResponse.getBody();
		}

		catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);

		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Fetch User ID", null),
					ex.getLocalizedMessage());
		}

	}

	@Override
	public CommonResponse constructURL(String appId) throws IOException {

		ResponseEntity<CommonResponse> commonResponse = null;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Constant.APPID, appId);
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<?> request = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZohoCRMUrl().getCreateUserByURL().replace(Constant.HOST,adaptorsHost));
		String finalUrl = builder.queryParams(params).build().toUriString();

		try {
			commonResponse = restTemplate.exchange(finalUrl, HttpMethod.GET, request, CommonResponse.class);
			return commonResponse.getBody();
		}

		catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);

		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Create User Response", null),
					ex.getLocalizedMessage());
		}
	}

}
