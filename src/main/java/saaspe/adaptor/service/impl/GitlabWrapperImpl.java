package saaspe.adaptor.service.impl;

import java.io.IOException;
import java.util.List;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import saaspe.adaptor.model.AddGitlabMemberRequest;
import saaspe.adaptor.model.GitlabDeleteUserRequest;
import saaspe.adaptor.model.GitlabInvitationsResponse;
import saaspe.adaptor.model.GitlabUrls;
import saaspe.adaptor.service.GitlabWrapperService;
import saaspe.constant.Constant;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@Service
public class GitlabWrapperImpl implements GitlabWrapperService {

	@Value("${gitlab-urls-file}")
	private String gitlabUrls;
	
    @Value("${adapters.host.url}")
    private String adaptorsHost;

	@Autowired
	private RestTemplate restTemplate;

	public GitlabUrls getGitlabUrls() {
		ClassPathResource resource = new ClassPathResource(gitlabUrls);
		ObjectMapper objectMapper = new ObjectMapper();
		GitlabUrls gitlabUrl = null;
		try {
			gitlabUrl = objectMapper.readValue(resource.getInputStream(), GitlabUrls.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gitlabUrl;
	}

	public CommonResponse getAuthUri(String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getAuthURL().replace(Constant.HOST,adaptorsHost));
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

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getAccessToken().replace(Constant.HOST,adaptorsHost));
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
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Get Token response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse generateToken(String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getGenerateToken().replace(Constant.HOST,adaptorsHost));
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
	public CommonResponse getUserProfile(String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getUserProfile().replace(Constant.HOST,adaptorsHost));
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
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Get User profile response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse addGitlabMember(AddGitlabMemberRequest gitlabMemberRequest, String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getAssignResource().replace(Constant.HOST,adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(gitlabMemberRequest, headers),
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
	public CommonResponse removeGitlabMember(GitlabDeleteUserRequest removeGitlabMember, String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getDeassginResource().replace(Constant.HOST,adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(removeGitlabMember, headers),
					CommonResponse.class);
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
	public CommonResponse getUserGroups(String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getUserGroups().replace(Constant.HOST,adaptorsHost));
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
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Get User groups response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public CommonResponse getGroupProjects(String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getGroupProject().replace(Constant.HOST,adaptorsHost));
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
					new Response("Get Group's projects response", null), Constant.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public CommonResponse findUserId(String appId, String userName) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getFindUser().replace(Constant.HOST,adaptorsHost));
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add(Constant.APPID, appId);
		body.add("userName", userName);
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

	@Override
	public CommonResponse getAccessRoles() {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getAccessRoles().replace(Constant.HOST,adaptorsHost));
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR, new Response("Get Access roles response", null),
					Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse getSubscriptionInfo(String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getSubscriptionInfo().replace(Constant.HOST,adaptorsHost));
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
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Get Subscription Info response", null), Constant.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public CommonResponse getUsersList(String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getUsersList().replace(Constant.HOST,adaptorsHost));
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
	public CommonResponse getResourceMembers(String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getResourceMembers().replace(Constant.HOST,adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			e.printStackTrace();
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Fetch Gitlab resource members response", null), Constant.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public CommonResponse getInvitationsList(String appId) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getInvitationList().replace(Constant.HOST,adaptorsHost));
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
					new Response("Fetch invitation list response", null), Constant.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public CommonResponse revokeInvitation(String appId, String inviteEmail) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getGitlabUrls().getRevokeInvitation().replace(Constant.HOST,adaptorsHost));
		builder.queryParam(Constant.APPID, appId);
		builder.queryParam("email", inviteEmail);
		String url = builder.toUriString();
		ResponseEntity<CommonResponse> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), CommonResponse.class);
			return response.getBody();
		} catch (HttpClientErrorException.BadRequest e) {
			e.printStackTrace();
			return new Gson().fromJson(e.getResponseBodyAsString(), CommonResponse.class);
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response("Fetch invitation list response", null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public boolean hasInvited(String email, String appId) {

		CommonResponse invitationListResponse = getInvitationsList(appId);

		if (invitationListResponse.getStatus().equals(HttpStatus.OK)) {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				List<GitlabInvitationsResponse> users = objectMapper.readValue(
						objectMapper.writeValueAsString(invitationListResponse.getResponse().getData()),
						new TypeReference<List<GitlabInvitationsResponse>>() {
						});
				for (GitlabInvitationsResponse invitation : users) {
					if (invitation.getInvite_email().equalsIgnoreCase(email))
						return true;
				}
				return false;
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				return false;
			} 
		} else
			return false;
	}

}
