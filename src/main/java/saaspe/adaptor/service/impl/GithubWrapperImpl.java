package saaspe.adaptor.service.impl;

import java.io.IOException;

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

import saaspe.adaptor.model.GitHubInviteRequestBody;
import saaspe.adaptor.model.GithubUrls;
import saaspe.adaptor.model.RemoveUserRequest;
import saaspe.adaptor.model.UserUpdateRequest;
import saaspe.adaptor.service.GithubWrapperService;
import saaspe.constant.Constant;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@Service
public class GithubWrapperImpl implements GithubWrapperService {

	@Value("${github-urls-file}")
	private String githubUrls;
	
	@Value("${adapters.host.url}")
    private String adaptorsHost;
	
	public GithubUrls getGithubUrls() {
		ClassPathResource resource = new ClassPathResource(githubUrls);
		ObjectMapper objectMapper = new ObjectMapper();
		GithubUrls githubUrl = null;
		try {
			githubUrl = objectMapper.readValue(resource.getInputStream(), GithubUrls.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return githubUrl;
	}

	public CommonResponse getAuthUri(String appId) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGithubUrls().getAuthURL().replace(Constant.HOST,adaptorsHost));
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
	public CommonResponse getToken(String appId) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGithubUrls().getToken().replace(Constant.HOST,adaptorsHost));
		builder.queryParam(Constant.APPID,appId);
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
	public CommonResponse getUserDetails(String appId) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGithubUrls().getGetUser().replace(Constant.HOST,adaptorsHost));
		builder.queryParam(Constant.APPID,appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Get User details response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse inviteUser(GitHubInviteRequestBody gitHubInviteRequestBody, String appId) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGithubUrls().getInviteUser().replace(Constant.HOST,adaptorsHost));
		builder.queryParam(Constant.APPID,appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(gitHubInviteRequestBody, headers),
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
	public CommonResponse getOrganizationMembers(String appId) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGithubUrls().getGetMembers().replace(Constant.HOST,adaptorsHost));
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
					new Response("Get organization members details response", null), Constant.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public CommonResponse getActionsBilling(String appId) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGithubUrls().getActionsBilling().replace(Constant.HOST,adaptorsHost));
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
					new Response("Get actions billing response", null), Constant.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public CommonResponse getPackagesBilling(String appId) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGithubUrls().getPackagesBilling().replace(Constant.HOST,adaptorsHost));
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
					new Response("Get packages billing response", null), Constant.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public CommonResponse getSharedStorageBilling(String appId) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGithubUrls().getSharedStorageBilling().replace(Constant.HOST,adaptorsHost));
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
					new Response("Get shared storage billing response", null), Constant.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public CommonResponse removeOrganizationMember(String appId, RemoveUserRequest removeUserRequest) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGithubUrls().getRemoveMember().replace(Constant.HOST,adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(removeUserRequest,headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Remove github organization member response", null), Constant.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public CommonResponse updateMemberRole(String appId, UserUpdateRequest userUpdateRequest) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGithubUrls().getUpdateMembership().replace(Constant.HOST,adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(userUpdateRequest, headers),
					CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Update github organization member role response", null), Constant.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public CommonResponse getOrganizationDetails(String appId) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGithubUrls().getGetOrgDetails().replace(Constant.HOST,adaptorsHost));
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
					new Response("Get Organization details response", null), Constant.INTERNAL_SERVER_ERROR);
		}
	}
	
}
