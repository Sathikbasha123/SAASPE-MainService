package saaspe.aspect;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import saaspe.constant.Constant;
import saaspe.entity.UserActions;
import saaspe.model.API;
import saaspe.model.ChangePasswordRequest;
import saaspe.model.DeptUserPasswordRequest;
import saaspe.model.LoginRequest;
import saaspe.model.ResetPasswordRequest;
import saaspe.model.SignupRequest;
import saaspe.repository.UserActionsRepository;

@Aspect
@Component
@EnableAspectJAutoProxy
@Configuration
public class ControllerLoggingAspect {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private UserActionsRepository userActionsRepository;

	private static final Map<String, String> CLASS_TO_ACTION_MAP = new HashMap<>();

	private static final Set<String> USER_CONTROLLERS = new HashSet<>(Arrays.asList("UserDetailsController",
			"UserLoginController", "UserOnboardingDetailsController", "UserActionController"));

	// private static final int MAX_LENGTH = 7000;

	private final Logger logger = LoggerFactory.getLogger(ControllerLoggingAspect.class);

	@Around("@within(saaspe.aspect.ControllerLogging) || @annotation(saaspe.aspect.ControllerLogging)")
	public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.currentTimeMillis();
		String methodName = joinPoint.getSignature().getName();
		String className = joinPoint.getTarget().getClass().getSimpleName();

		Object[] args = joinPoint.getArgs();
		for (int i = 0; i < args.length; i++) {
			if(args[i]!=null) {
			if (args[i].getClass().getName().equalsIgnoreCase("saaspe.model.LoginRequest")) {
				LoginRequest loginRequest = mapper.convertValue(args[i], LoginRequest.class);
				loginRequest.setPassword("********");
				args[i] = loginRequest;
			}
			if (args[i].getClass().getName().equalsIgnoreCase("saaspe.model.ChangePasswordRequest")) {
				ChangePasswordRequest changePasswordRequest = mapper.convertValue(args[i], ChangePasswordRequest.class);
				changePasswordRequest.setConfirmNewPassword("********");
				changePasswordRequest.setNewPassword("********");
				changePasswordRequest.setOldPassword("********");
				args[i] = changePasswordRequest;
			}
			if (args[i].getClass().getName().equalsIgnoreCase("saaspe.model.SignupRequest")) {
				SignupRequest signupRequest = mapper.convertValue(args[i], SignupRequest.class);
				signupRequest.setPassword("********");
				args[i] = signupRequest;
			}
			if (args[i].getClass().getName().equalsIgnoreCase("saaspe.model.DeptUserPasswordRequest")) {
				DeptUserPasswordRequest passwordRequest = mapper.convertValue(args[i], DeptUserPasswordRequest.class);
				passwordRequest.setConfirmPassword("********");
				passwordRequest.setPassword("********");
				args[i] = passwordRequest;
			}
			if (args[i].getClass().getName().equalsIgnoreCase("saaspe.model.ResetPasswordRequest")) {
				ResetPasswordRequest resetPasswordRequest = mapper.convertValue(args[i], ResetPasswordRequest.class);
				resetPasswordRequest.setConfirmNewPassword("********");
				resetPasswordRequest.setNewPassword("********");
				args[i] = resetPasswordRequest;
			}
			}
		}
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = (requestAttributes instanceof ServletRequestAttributes)
				? ((ServletRequestAttributes) requestAttributes).getRequest()
				: null;
		HttpServletResponse response = (requestAttributes instanceof ServletRequestAttributes)
				? ((ServletRequestAttributes) requestAttributes).getResponse()
				: null;
		String methodType = (request != null) ? request.getMethod() : null;

		String traceId = (response != null) ? response.getHeader("X-Trace-Id") : null;

		// Set MDC trace id to the value in response header
		if (traceId != null) {
			org.slf4j.MDC.put("traceId", traceId);
		}

		StringBuilder logMessage = new StringBuilder();
		logMessage.append("[START] {}.{} [TRACE ID: {}]").append(System.lineSeparator());
		if (request != null) {
			logMessage.append("Request URL: {} {}, From: {}").append(System.lineSeparator());
			logMessage.append("Headers: {} [TRACE ID: {}]").append(System.lineSeparator());
			logMessage.append("Parameters: {}").append(System.lineSeparator());
		}
		logMessage.append("Method Arguments: {}").append(System.lineSeparator());

		logger.info(logMessage.toString(), className, methodName, traceId,
				(request != null) ? request.getMethod() : null, (request != null) ? request.getRequestURL() : null,
				(request != null) ? request.getHeader("X-From") : null,
				(request != null) ? getRequestHeaders(request) : null, traceId,
				(request != null) ? getRequestParameters(request) : null, Arrays.toString(args));

		Object result = null;
		API api = getAPIFromJoinPoint(joinPoint);
		try {
			result = joinPoint.proceed();
			String jsonResponse = mapper.writeValueAsString(result);
			api.setRsp(jsonResponse);
			updateDatabase(traceId, methodName, className, result, methodType,
					(request != null) ? request.getRequestURL().toString() : null, request);
			logger.info("Method Returned: {}", api);
		} catch (Throwable t) {
			String errorMessage = String.format("Exception in method %s: %s: %s", methodName, t.getMessage(), traceId);
			logger.error(errorMessage, t);
			updateDatabase(traceId, methodName, className, result, methodType,
					(request != null) ? request.getRequestURL().toString() : null, request);
		} finally {
			// Remove MDC trace id
			org.slf4j.MDC.remove("traceId");
			logger.info("[END] {}.{} [TIME ELAPSED: {}ms] [TRACE ID: {}]", className, methodName,
					System.currentTimeMillis() - startTime, traceId);
		}

		return result;
	}

	private Map<String, String> getRequestHeaders(HttpServletRequest request) {
		return Collections.list(request.getHeaderNames()).stream()
				.collect(Collectors.toMap(header -> header, request::getHeader));
	}

	private Map<Object, Object> getRequestParameters(HttpServletRequest request) {
		return request.getParameterMap().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> Arrays.toString(entry.getValue())));
	}

	private API getAPIFromJoinPoint(JoinPoint joinPoint) {
		Map<String, Object> ipl = new HashMap<>();
		int i = 1;
		for (Object obj : joinPoint.getArgs()) {
			if (obj != null && !obj.getClass().getName().startsWith("org.spring")) {
				if (obj.getClass().getName().equalsIgnoreCase("saaspe.model.LoginRequest")) {
					LoginRequest loginRequest = mapper.convertValue(obj, LoginRequest.class);
					loginRequest.setPassword("********");
					obj = loginRequest;
				}
				if (obj.getClass().getName().equalsIgnoreCase("saaspe.model.ChangePasswordRequest")) {
					ChangePasswordRequest changePasswordRequest = mapper.convertValue(obj, ChangePasswordRequest.class);
					changePasswordRequest.setConfirmNewPassword("********");
					changePasswordRequest.setNewPassword("********");
					changePasswordRequest.setOldPassword("********");
					obj = changePasswordRequest;
				}
				if (obj.getClass().getName().equalsIgnoreCase("saaspe.model.SignupRequest")) {
					SignupRequest signupRequest = mapper.convertValue(obj, SignupRequest.class);
					signupRequest.setPassword("********");
					obj = signupRequest;
				}
				if (obj.getClass().getName().equalsIgnoreCase("saaspe.model.DeptUserPasswordRequest")) {
					DeptUserPasswordRequest passwordRequest = mapper.convertValue(obj, DeptUserPasswordRequest.class);
					passwordRequest.setConfirmPassword("********");
					passwordRequest.setPassword("********");
					obj = passwordRequest;
				}
				if (obj.getClass().getName().equalsIgnoreCase("saaspe.model.ResetPasswordRequest")) {
					ResetPasswordRequest resetPasswordRequest = mapper.convertValue(obj, ResetPasswordRequest.class);
					resetPasswordRequest.setConfirmNewPassword("********");
					resetPasswordRequest.setNewPassword("********");
					obj = resetPasswordRequest;
				}
				ipl.put(i + ":" + obj.getClass().getName(), obj);
				i++;
			}
		}

		Signature signature = joinPoint.getSignature();
		API api = new API();
		api.setCls(signature.getDeclaringTypeName());
		api.setOpp(signature.getName());
		api.setIpl(ipl);
		return api;
	}

	static {
		CLASS_TO_ACTION_MAP.put("DepartmentController", "Department");
		CLASS_TO_ACTION_MAP.put("ApplicationDetailController", Constant.APPLICATION);
		CLASS_TO_ACTION_MAP.put("ProjectDetailsController", "Project");
		CLASS_TO_ACTION_MAP.put("MarketPlaceController", "MarketPlace");
		CLASS_TO_ACTION_MAP.put("SubscriptionController", "Subscription");
		CLASS_TO_ACTION_MAP.put("CurrencyController", "Currency");
		CLASS_TO_ACTION_MAP.put("ContractController", "Contract");
		CLASS_TO_ACTION_MAP.put("LicenseController", "License");
		CLASS_TO_ACTION_MAP.put("CLMController", "CLM");
		CLASS_TO_ACTION_MAP.put("DashboardController", "Dashboard");
		CLASS_TO_ACTION_MAP.put("MultiCloudController", "MultiCloud");
		CLASS_TO_ACTION_MAP.put("SubscriptionController", "Subscription");
		CLASS_TO_ACTION_MAP.put("EmailTriggerController", "CreatePassword");
		CLASS_TO_ACTION_MAP.put("EnquiryController", "Enquiry");
		CLASS_TO_ACTION_MAP.put("InvoiceController", "Invoice");
		CLASS_TO_ACTION_MAP.put("LogoController", "Logo");
		CLASS_TO_ACTION_MAP.put("ConversationDetailController", "saaspe amigo-AI");
		CLASS_TO_ACTION_MAP.put("CategoryController", "category");
		CLASS_TO_ACTION_MAP.put("UserActionsController", "Auditlogs");
	}

	private void updateDatabase(String traceId, String methodName, String className, Object result, String methodType,
			String url, HttpServletRequest request) throws IOException {
		if (!methodName.equalsIgnoreCase("GET") && request != null) {
			// String jsonResponse =
			// StringUtils.abbreviate(mapper.writeValueAsString(result), MAX_LENGTH);
			String jsonResponse = mapper.writeValueAsString(result);
			UserActions existingAction = userActionsRepository.findActionsBytraceId(traceId);
			if (existingAction != null) {
				existingAction.setTraceId(traceId);
				existingAction.setResponse(jsonResponse);
				String jsonString = existingAction.getResponse();
				JSONObject jsonObject = new JSONObject(jsonString);
				int statusCode = jsonObject.optInt("statusCodeValue", -1);
				existingAction.setStatusCode(statusCode);
				if (statusCode > 0) {
					if (statusCode == HttpServletResponse.SC_OK) {
						existingAction.setStatus("Success");
					} else if (statusCode >= HttpServletResponse.SC_BAD_REQUEST) {
						existingAction.setStatus("Failed");
					} else if (statusCode >= HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
						existingAction.setStatus("Failed");
					} else if (statusCode == HttpServletResponse.SC_CONFLICT) {
						existingAction.setStatus("Failed");
					} else if (statusCode >= HttpServletResponse.SC_UNAUTHORIZED) {
						existingAction.setStatus("Failed");
					} else if (statusCode >= HttpServletResponse.SC_FORBIDDEN) {
						existingAction.setStatus("Failed");
					} else {
						existingAction.setStatus("Failed");
					}
				}
				existingAction.setActionCategory(USER_CONTROLLERS.contains(className) ? "User"
						: CLASS_TO_ACTION_MAP.getOrDefault(className, ""));
				setActionAndSummary(existingAction, methodType, url, jsonResponse);
				userActionsRepository.save(existingAction);
			}
		}
	}

	private boolean isRequestedEndpoint(String url) {
		List<String> requestedEndpoints = Arrays.asList("/api/v1/application/excel",
				"/api/v1/application/purchase/onboard", "/api/v1/application/new/onboard",
				"/api/v1/department/single/onboard", "/api/v1/department/excel", "/api/v1/project/excel",
				"/api/v1/project/single/onboard", "/api/v1/user/onboarding/single-user",
				"/api/v1/user/onboarding/excel", "/addcontract");
		return requestedEndpoints.stream().anyMatch(url::endsWith);

	}

	private String getActionObject(String url) {
		String[] pathSegments = url.split("/");
		if (pathSegments.length >= 6 && "application".equals(pathSegments[5])) {
			if (pathSegments[6].equalsIgnoreCase("contract")) {
				return pathSegments[6];
			}
			return pathSegments[5];
		} else if (pathSegments.length >= 4 && "enquiry".equals(pathSegments[4])) {
			return pathSegments[4];
		} else if (pathSegments.length >= 3) {
			if (pathSegments[5].equalsIgnoreCase("project") || pathSegments[5].equalsIgnoreCase("department")
					|| pathSegments[5].equalsIgnoreCase("user"))
				return pathSegments[5];
			else
				return pathSegments[pathSegments.length - 3];
		} else {
			return "";
		}
	}

	private void setActionAndSummary(UserActions existingAction, String methodType, String url, String jsonResponse) {
		String actionObject = getActionObject(url);
		switch (methodType.toUpperCase()) {
		case "GET":
			existingAction.setAction("Viewed");
			existingAction.setActionSummary("Viewed " + actionObject);
			break;
		case "PUT":
			if (existingAction.getActionCategory().equalsIgnoreCase(Constant.APPLICATION)
					&& url.endsWith("/v1/application/update-by-applicationid")) {
				existingAction.setAction(Constant.EDITED);
				existingAction.setActionSummary("Edited Application");

			} else if (existingAction.getActionCategory().equalsIgnoreCase("CLM")
					&& url.endsWith("/clm/update/template/{templateId}")) {
				existingAction.setAction(Constant.EDITED);
				existingAction.setActionSummary("Edited Template");

			} else if (existingAction.getActionCategory().equalsIgnoreCase("User")
					&& url.endsWith("/details/ownership/transfer")) {
				existingAction.setAction(Constant.EDITED);
				existingAction.setActionSummary("Ownership Transferred");

			} else if (existingAction.getActionCategory().equalsIgnoreCase("User")
					&& url.endsWith("/user/details/modify-user")) {
				existingAction.setAction(Constant.EDITED);
				existingAction.setActionSummary("User Edited");

			} else if (existingAction.getActionCategory().equalsIgnoreCase("Multicloud")
					&& url.endsWith("/v1/user/details/workflow/status")) {
				existingAction.setAction("Updated");
				existingAction.setActionSummary("Updated workflow status");

			} else if (existingAction.getActionCategory().equalsIgnoreCase("User")
					&& url.endsWith("/v1/user/details/workflow/status")) {
				existingAction.setAction("Completed");
				existingAction.setActionSummary("Completed workflow status");

			} else if (url.endsWith("admin/application/link")) {
				existingAction.setAction(Constant.CREATED);
				existingAction.setActionSummary("Created Integrations");
				existingAction.setActionCategory("Integration");
			} else if (url.endsWith("application/license/link")) {
				existingAction.setAction(Constant.CREATED);
				existingAction.setActionSummary("Assigned License");
			} else {
				existingAction.setAction(Constant.DELETED);
				existingAction.setActionSummary(Constant.DELETED + " " + actionObject);
			}
			break;
		case "POST":
			if (url.endsWith("review")) {
				if (jsonResponse.toLowerCase().contains("approve")) {
					existingAction.setAction("Approved");
					existingAction.setActionSummary("Approved " + actionObject);
				} else if (jsonResponse.toLowerCase().contains("reject")) {
					existingAction.setAction("Rejected");
					existingAction.setActionSummary("Rejected " + actionObject);
				} else {
					existingAction.setAction("Requested");
					existingAction.setActionSummary("Requested " + actionObject);
				}
			} else if (url.endsWith("clm/addcontract")) {
				existingAction.setAction(Constant.CREATED);
				existingAction.setActionSummary("CLM contract created");
			} else if (isRequestedEndpoint(url)) {
				existingAction.setAction("Requested");
				existingAction.setActionSummary("Requested " + actionObject);
			} else if (existingAction.getActionCategory().equalsIgnoreCase(Constant.APPLICATION)
					&& url.endsWith("v1/application/file")) {
				existingAction.setAction(Constant.CREATED);
				existingAction.setActionSummary("Added document to Application");

			} else if (existingAction.getActionCategory().equalsIgnoreCase("User")) {
				if (url.endsWith("userprofile/login")) {
					existingAction.setAction("LoggedIn");
					existingAction.setActionSummary("User Login");
				} else if (url.endsWith("userprofile/signup")) {
					existingAction.setAction("Signup");
					existingAction.setActionSummary("User Signup");
				} else if (url.endsWith("userprofile/verify-otp")) {
					existingAction.setAction("Verify");
					existingAction.setActionSummary("Otp verify");
				} else if (url.endsWith("/api/userprofile/verify-email")) {
					existingAction.setAction("Verify");
					existingAction.setActionSummary("Verify Email");
				} else if (url.endsWith("userprofile/verify-email")) {
					existingAction.setAction("Verified");
					existingAction.setActionSummary("Verified Email by code");
				} else if (url.endsWith("create/admin")) {
					existingAction.setAction("Created");
					existingAction.setActionSummary("Contributor/Approver/Superadmin has been created");
				} else if (url.endsWith("userprofile/refresh/token")) {
					existingAction.setAction("GetRefreshtoken");
					existingAction.setActionSummary("Fetched Refresh token");
				} else if (url.endsWith("userprofile/reset-password")) {
					existingAction.setAction("UpdatedPassword");
					existingAction.setActionSummary("New password updated");
				} else if (url.endsWith("api/userprofile/consent-email/touser")) {
					existingAction.setAction("ConsentEmail");
					existingAction.setActionSummary("Consent Email send to user");
				} else if (url.endsWith("userprofile/changePassword")) {
					existingAction.setAction(Constant.CREATED);
					existingAction.setActionSummary("Updated Password");
					existingAction.setActionCategory("User");
				} else if (url.endsWith("/login/excel")) {
					existingAction.setAction(Constant.CREATED);
					existingAction.setActionSummary("Triggered last login Email");
					existingAction.setActionCategory("User");
				}
			} else if (url.endsWith("/conversation/feedback")) {
				existingAction.setAction(Constant.CREATED);
				existingAction.setActionSummary("Posted Feedback");
				existingAction.setActionCategory("saaspe amigo-AI");
			} else if (url.endsWith("/conversation/findbyprompt")) {
				existingAction.setAction("Requested");
				existingAction.setActionSummary("Request AI Response");
				existingAction.setActionCategory("saaspe amigo-AI");
			} else if (url.endsWith("/conversation/create")) {
				existingAction.setAction(Constant.CREATED);
				existingAction.setActionSummary("Created Conversation");
				existingAction.setActionCategory("saaspe amigo-AI");
			} else if (url.endsWith("/create/admin")) {
				existingAction.setAction(Constant.CREATED);
				existingAction.setActionSummary("Created Admin");
				existingAction.setActionCategory("User");
			} else if (url.endsWith("/siteverify")) {
				existingAction.setAction("Verify");
				existingAction.setActionSummary("Verified Site");
			} else if (existingAction.getActionCategory().equalsIgnoreCase("Dashboard")
					&& url.endsWith("/api/v1/dashboard/similar-apps")) {
				existingAction.setAction("Suggestions");
				existingAction.setActionSummary("Similar Applications provided(Based on category)");
			} else if (existingAction.getActionCategory().equalsIgnoreCase("MarketPlace")
					&& url.endsWith("/api/v1/marketplace/email")) {
				existingAction.setAction("ApplicationEnquiry");
				existingAction.setActionSummary("Enquiry sent to saaspe-support team");
			} else if (existingAction.getActionCategory().equalsIgnoreCase("CLM")) {
				if (url.endsWith("clm/addcontract")) {
					existingAction.setAction(Constant.CREATED);
					existingAction.setActionSummary("CLM contract created");
				} else if (url.endsWith("clm/create/template")) {
					existingAction.setAction(Constant.CREATED);
					existingAction.setActionSummary("CLM Template created");
				} else if (url.endsWith("clm/createEnvelopeMultiple")) {
					existingAction.setAction(Constant.CREATED);
					existingAction.setActionSummary("CLM Envelope created");
				}
			} else if (url.endsWith("/adaptor/save/details")) {
				existingAction.setAction(Constant.CREATED);
				existingAction.setActionSummary("Saved Adaptor details");
			} else if (url.endsWith("v1/currency/update")) {
				existingAction.setAction("Updated");
				existingAction.setActionSummary("Currency update");
			} else if (url.endsWith("/auth/create-password")) {
				existingAction.setAction(Constant.CREATED);
				existingAction.setActionSummary("Password created Sucessfully");
			} else if (url.endsWith("v1/invoice/upload")) {
				existingAction.setAction(Constant.CREATED);
				existingAction.setActionSummary("Invoice created Sucessfully");
			} else if (url.endsWith("/application/license/unmapped/count")) {
				existingAction.setAction("Fetch License");
				existingAction.setActionSummary("License count displayed");
			} else if (existingAction.getActionCategory().equalsIgnoreCase("MultiCloud")) {
				if (url.endsWith("cloud/budget/create")) {
					existingAction.setAction(Constant.CREATED);
					existingAction.setActionSummary("Azure budget created");
				} else if (url.endsWith("cloud/onboard")) {
					existingAction.setAction(Constant.CREATED);
					existingAction.setActionSummary("Cloud created");
				} else if (url.endsWith("cloud/optimize/azure/email")) {
					existingAction.setAction("Sent");
					existingAction.setActionSummary("Azure Mail sent");
				} else if (url.endsWith("cloud/subscribe")) {
					existingAction.setAction(Constant.CREATED);
					existingAction.setActionSummary("Cloud Subscription created");
				}
			}
			break;
		case "DELETE":
			if (existingAction.getActionCategory().equalsIgnoreCase(Constant.APPLICATION)
					&& url.endsWith("application/multiple-remove")) {
				existingAction.setAction(Constant.DELETED);
				existingAction.setActionSummary("Deleted Application");

			} else if (existingAction.getActionCategory().equalsIgnoreCase("Invoice")
					&& url.endsWith("/v1/invoice/remove")) {
				existingAction.setAction(Constant.DELETED);
				existingAction.setActionSummary("Deleted invoice");

			} else if (url.endsWith("user/details/multiple-remove")) {
				existingAction.setAction(Constant.DELETED);
				existingAction.setActionSummary("Deleted User");
				existingAction.setActionCategory("User");

			} else if (existingAction.getActionCategory().equalsIgnoreCase("Multicloud")
					&& url.endsWith("user/details/multiple-remove")) {
				existingAction.setAction(Constant.DELETED);
				existingAction.setActionSummary("Deleted Cloud");

			} else if (existingAction.getActionCategory().equalsIgnoreCase("saaspe amigo-AI")
					&& url.endsWith("v1/conversation/remove")) {
				existingAction.setAction(Constant.DELETED);
				existingAction.setActionSummary("Deleted Conversations");

			}
			existingAction.setAction(Constant.DELETED);
			existingAction.setActionSummary(Constant.DELETED + " " + actionObject);
			break;
		default:
			existingAction.setAction("");
			existingAction.setActionSummary("");
			break;
		}
	}

}