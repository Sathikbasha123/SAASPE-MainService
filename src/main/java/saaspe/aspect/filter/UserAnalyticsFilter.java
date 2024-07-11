package saaspe.aspect.filter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import saaspe.entity.UserActions;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.repository.UserActionsRepository;
import saaspe.repository.UserDetailsRepository;

@Component
public class UserAnalyticsFilter implements Filter {

	@Autowired
	private UserActionsRepository userActionsRepository;

	@Autowired
	private UserDetailsRepository userDetailsRepo;

	@Value("${redirecturl.path}")
	private String redirectUrl;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String url = httpRequest.getRequestURI();
		String methodType = httpRequest.getMethod();
		if (!methodType.equalsIgnoreCase("GET")) {
			if (isSwaggerPath(httpRequest.getRequestURI())) {
				chain.doFilter(request, response);
				return;
			}
			String traceId = httpResponse.getHeader("X-Trace-Id");
			UserActions userActions = buildUserActions(httpRequest, httpResponse, url, methodType, traceId);
			userActionsRepository.save(userActions);
		}
		chain.doFilter(request, response);
	}

	private UserActions buildUserActions(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String url,
			String methodType, String traceId) {
		long startTime = System.currentTimeMillis();
		Map<Object, Object> requestParameters = getRequestParameters(httpRequest);
		long endTime = System.currentTimeMillis();
		long diff = endTime - startTime;
		int responseStatus = httpResponse.getStatus();
		UserActions userActions = new UserActions();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UserLoginDetails) {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			userActions.setUserEmail(profile.getEmailAddress());
			UserDetails userRole = userDetailsRepo.findByuserEmail(profile.getEmailAddress());
			if (userRole != null) {
				userActions.setRole(userRole.getUserRole());
			}
		}
		Instant utcNow = Instant.now();
		ZonedDateTime istTime = ZonedDateTime.ofInstant(utcNow, ZoneId.of("Asia/Kolkata"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		String formattedTime = istTime.format(formatter);
		LocalDateTime localDateTime = LocalDateTime.parse(formattedTime, formatter);
		Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		userActions.setTimeTaken(diff + "ms");
		userActions.setEndpoint(redirectUrl + url);
		userActions.setCreatedOn(date);
		userActions.setMethodType(methodType);
		String status = getStatusDescription(responseStatus);
		userActions.setStatusCode(responseStatus);
		userActions.setStatus(status);
		userActions.setRequest(requestParameters.toString());

		if (traceId != null) {
			MDC.put("traceId", traceId);
		}
		userActions.setTraceId(traceId);
		return userActions;
	}

	private Map<Object, Object> getRequestParameters(HttpServletRequest request) {
		return request.getParameterMap().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> Arrays.toString(entry.getValue())));
	}

	private boolean isSwaggerPath(String path) {
		AntPathMatcher pathMatcher = new AntPathMatcher();
		String[] swaggerPaths = { "/swagger-ui.html", "/v2/api-docs", "/swagger-resources/**", "/webjars/**" };
		for (String swaggerPath : swaggerPaths) {
			if (pathMatcher.match(swaggerPath, path)) {
				return true;
			}
		}
		return false;
	}

	private String getStatusDescription(int statusCode) {
		if (statusCode == HttpServletResponse.SC_OK) {
			return "Success";
		} else if (statusCode >= HttpServletResponse.SC_BAD_REQUEST) {
			return "Bad request";
		} else if (statusCode >= HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
			return "Server Error";
		} else if (statusCode == HttpServletResponse.SC_CONFLICT) {
			return "Conflict";
		} else if (statusCode >= HttpServletResponse.SC_UNAUTHORIZED) {
			return "Un Authorized";
		} else if (statusCode >= HttpServletResponse.SC_FORBIDDEN) {
			return "Forbidden";
		} else {
			return "Failed";
		}
	}

}
