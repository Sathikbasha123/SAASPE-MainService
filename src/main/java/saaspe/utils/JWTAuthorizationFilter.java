package saaspe.utils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import saaspe.configuration.AzureConfig;
import saaspe.constant.Constant;
import saaspe.entity.UserLoginDetails;
import saaspe.model.DocusignCreateUserResponse;
import saaspe.model.DocusignUserCache;
import saaspe.model.GraphGroupsResponse;
import saaspe.repository.UserLoginDetailsRepository;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	@Value("${docusign.host.url}")
	private String docusignHost;
	private String redisPrefix;
	private String encryptionKey;
	private String jwtKey;
	private RedisUtility redisUtility;
	private RestTemplate restTemplate;

	public JWTAuthorizationFilter(AuthenticationManager authenticationManager, String encryptionKey, String jwtKey,
			RedisUtility redisUtility, UserLoginDetailsRepository userLoginDetailsRepository, String redisPrefix) {
		super(authenticationManager);
		this.restTemplate = new RestTemplate();
		this.redisUtility = redisUtility;
		this.encryptionKey = encryptionKey;
		this.jwtKey = jwtKey;
		this.redisPrefix = redisPrefix;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String header = request.getHeader(Constant.HEADER_STRING);
		if (header == null || !header.startsWith(Constant.TOKEN_PREFIX)) {
			chain.doFilter(request, response);
			return;
		}
		try {
			UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
			if (authentication != null) {
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			chain.doFilter(request, response);
		} catch (BadCredentialsException e) {
			sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", e.getLocalizedMessage());
		}
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request)
			throws JsonProcessingException {
		String token = request.getHeader(Constant.HEADER_STRING);
		String provider = request.getHeader(Constant.HEADER_PROVIDER_STRING);

		if (provider == null || provider.equalsIgnoreCase(Constant.HEADER_PROVIDER_NAME)) {
			return verifyToken(token);
		} else if (provider.equalsIgnoreCase("azure")) {
			return authenticateAzureUser(token);
		} else {
			throw new BadCredentialsException("Vendor Should be in the list or Vendor is Null");
		}
	}

	private UsernamePasswordAuthenticationToken verifyToken(String token) {
		if (token != null) {
			try {
				DecodedJWT jwt = JWT
						.require(Algorithm.HMAC256(EncryptionHelper.decrypt(encryptionKey, jwtKey).getBytes())).build()
						.verify(token.replace(Constant.TOKEN_PREFIX, ""));
				String user = jwt.getSubject();
				if (user != null) {
					Set<SimpleGrantedAuthority> authorities = extractAuthoritiesFromToken(jwt);
					UserLoginDetails profile = new UserLoginDetails();
					profile.setEmailAddress(jwt.getClaim(Constant.EMAIL).asString());
					return new UsernamePasswordAuthenticationToken(profile, null, authorities);
				}
			} catch (JWTVerificationException e) {
				throw new BadCredentialsException("Invalid Token");
			}
		}
		return null;
	}

	private Set<SimpleGrantedAuthority> extractAuthoritiesFromToken(DecodedJWT jwt) {
		Set<SimpleGrantedAuthority> authorities = new HashSet<>();
		String scopes = jwt.getClaim("scopes").asString();
		List<String> scopesList = new ArrayList<>(Arrays.asList(scopes.split(", ")));
		for (String scope : scopesList) {
			authorities.add(new SimpleGrantedAuthority(scope));
		}
		authorities.add(new SimpleGrantedAuthority(jwt.getClaim("role").asString()));
		return authorities;
	}

	private UsernamePasswordAuthenticationToken authenticateAzureUser(String token) throws JsonProcessingException {
		if (token != null) {
			DecodedJWT jwt = JWT.decode(token.replace(Constant.TOKEN_PREFIX, ""));
			String email = jwt.getClaim("upn").asString();
			TokenCache cacheValue = redisUtility.getValue(email);
			TokenCache docChcek = redisUtility.getValue(Constant.TOKEN + email);
			DocusignUserCache docusignUserCheck = redisUtility.getDocusignValue(redisPrefix + email);
			if (cacheValue == null) {
				validateAzureToken(token);
				GraphGroupsResponse groupsResponse = fetchUserGroups(token);
				if (groupsResponse != null) {
					boolean isUser = isUserInGroup(groupsResponse, "clm-users");
					if (!isUser) {
						throw new BadCredentialsException(
								"User Not present in the group,Please add user in clm-users group");
					}
					if (isUser) {
						Set<SimpleGrantedAuthority> authorities = new HashSet<>();
						List<String> scopesList = new ArrayList<>(Arrays.asList(Constant.ROLE_CLM.split(", ")));
						for (String scope : scopesList) {
							authorities.add(new SimpleGrantedAuthority(scope));
						}
						TokenCache cache = new TokenCache();
						cache.setEmailAddress(email);
						cache.setDisplayname(Constant.CLM_USER);
						cache.setExpiryDate(jwt.getExpiresAt());
						cache.setToken(token.replace(Constant.TOKEN_PREFIX, ""));
						redisUtility.setValue(email, cache, jwt.getExpiresAt());
						if (docChcek == null) {
							if (docusignUserCheck == null) {
								yourMethodToNotify(token);
							}
							TokenCache docCache = new TokenCache();
							docCache.setEmailAddress(Constant.TOKEN + email);
							docCache.setDisplayname(Constant.CLM_USER);
							docCache.setExpiryDate(jwt.getExpiresAt());
							docCache.setToken(token.replace(Constant.TOKEN_PREFIX, ""));
							redisUtility.setValue(Constant.TOKEN + email, docCache, jwt.getExpiresAt());
						}
						UserLoginDetails profile = new UserLoginDetails();
						profile.setEmailAddress(jwt.getClaim(Constant.EMAIL).asString());
						return new UsernamePasswordAuthenticationToken(profile, null, authorities);
					}
				} else {
					throw new BadCredentialsException("User Not present in the group");
				}
			} else {
				validateAzureToken(token);
				if (docusignUserCheck == null) {
					yourMethodToNotify(token);
				}
				if (docChcek == null) {
					TokenCache docCache = new TokenCache();
					docCache.setEmailAddress(Constant.TOKEN + email);
					docCache.setDisplayname(Constant.CLM_USER);
					docCache.setExpiryDate(jwt.getExpiresAt());
					docCache.setToken(token.replace(Constant.TOKEN_PREFIX, ""));
					redisUtility.setValue(Constant.TOKEN + email, docCache, jwt.getExpiresAt());
				}
				Set<SimpleGrantedAuthority> authorities = new HashSet<>();
				List<String> scopesList = new ArrayList<>(Arrays.asList(Constant.ROLE_CLM.split(", ")));
				for (String scope : scopesList) {
					authorities.add(new SimpleGrantedAuthority(scope));
				}
				UserLoginDetails profile = new UserLoginDetails();
				profile.setEmailAddress(jwt.getClaim(Constant.EMAIL).asString());
				return new UsernamePasswordAuthenticationToken(profile, null, authorities);
			}
		}
		return null;
	}

	private void validateAzureToken(String token) {
		boolean valid = AzureConfig.isValidToken(token);
		if (!valid) {
			throw new BadCredentialsException("Token Already Expired");
		}
	}

	public void yourMethodToNotify(String message) throws JsonProcessingException {
		Map<String, String> userDetails = getUserDetailsForSSOUser(message);
		getClmUsersList(userDetails.get(Constant.EMAIL), userDetails.get(Constant.FIRST_NAME),
				userDetails.get(Constant.LAST_NAME));
	}

	private void getClmUsersList(String userEmail, String firstName, String lastName) throws JsonProcessingException {
		URI uri = UriComponentsBuilder.fromUriString("http://saaspe-docusign-svc:8085/create/user")
				.queryParam("userEmail", userEmail).queryParam(Constant.FIRST_NAME, firstName)
				.queryParam(Constant.LAST_NAME, lastName).build().toUri();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, String.class);
		ObjectMapper mapper = new ObjectMapper();
		DocusignCreateUserResponse docusingCreateUserResponse = mapper.readValue(response.getBody(),
				DocusignCreateUserResponse.class);
		DocusignUserCache doCache = new DocusignUserCache();
		doCache.setUserName(docusingCreateUserResponse.getUser_name());
		doCache.setUserId(docusingCreateUserResponse.getId());
		doCache.setUserEmail(docusingCreateUserResponse.getEmail());
		redisUtility.setDocusingValue(redisPrefix + docusingCreateUserResponse.getEmail(), doCache);
	}

	private Map<String, String> getUserDetailsForSSOUser(String token) {
		Map<String, String> userDetails = new HashMap<>();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setBearerAuth(token.replace(Constant.TOKEN_PREFIX, ""));
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(Constant.GRAPH_GROUP_URL_ME, HttpMethod.GET, entity,
				String.class);
		String responseBody = response.getBody();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = objectMapper.readTree(responseBody);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		if (rootNode != null) {
			userDetails.put("displayName", rootNode.get("displayName").asText());
			userDetails.put(Constant.FIRST_NAME, rootNode.get("surname").asText());
			userDetails.put(Constant.EMAIL, rootNode.get("mail").asText());
			userDetails.put(Constant.LAST_NAME, rootNode.get("givenName").asText());
		}
		return userDetails;
	}

	private GraphGroupsResponse fetchUserGroups(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setBearerAuth(token.replace(Constant.TOKEN_PREFIX, ""));
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<GraphGroupsResponse> response = restTemplate.exchange(Constant.GRAPH_GROUP_URL, HttpMethod.GET,
				entity, GraphGroupsResponse.class);
		return response.getBody();
	}

	private boolean isUserInGroup(GraphGroupsResponse groupsResponse, String groupId) {
		for (saaspe.model.Value value : groupsResponse.getValue()) {
			if (value.getDisplayName().equalsIgnoreCase(groupId)) {
				return true;
			}
		}
		return false;
	}

	private void sendErrorResponse(HttpServletResponse response, int statusCode, String status, String message)
			throws IOException {
		response.setStatus(statusCode);
		response.setContentType("application/json");
		Map<String, String> object = new HashMap<>();
		object.put("message", message);
		object.put("status", status);
		String json = new Gson().toJson(object);
		response.getWriter().write(json);
	}

}