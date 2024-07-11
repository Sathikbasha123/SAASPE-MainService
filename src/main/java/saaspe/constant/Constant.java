package saaspe.constant;

import java.util.Arrays;
import java.util.List;

public class Constant {

	private Constant() {
		super();
	}

	public static final String HEADER_PROVIDER_NAME = "Internal";
	public static final String HEADER_PROVIDER_STRING = "X-Auth-Provider";
	public static final String GRAPH_GROUP_URL_ME = "https://graph.microsoft.com/v1.0/me";
	public static final String GRAPH_GROUP_URL = "https://graph.microsoft.com/v1.0/me/transitiveMemberOf/microsoft.graph.group";

	public static final String USER_DETAILS_NOT_FOUND = "user details not found!";
	public static final String USER_DETAILS_ALL_READY_REGISTERED = "user details already exist!";
	public static final String APPLICATION_NOT_FOUND = "appplication id not found!";
	public static final String APPLICATION_DETAILS_UPDATED = "Application Contract Details Updated!";
	public static final String APPLICATION_CONTACT_NOT_FOUND = "application contract details not found!";

	public static final String USER_DATA_REMOVE = "user details removed!";
	public static final String USER_DATA_SHOULD_HAVE = "user details should not be null or empty!";

	public static final String ROLE_SUPER_ADMIN = "VIEW_USER, VIEW_APPLICATION, VIEW_DEPARTMENT, VIEW_ONBOARDINGMGMT, REVIEW_ONBOARDINGMGMT, APPROVE_ONBOARDINGMGMT, ADD_ADMINUSER, VIEW_ADMINUSER, EDIT_ADMINUSER, DELETE_ADMINUSER, ADD_MULTICLOUD, EDIT_MULTICLOUD, DELETE_MULTICLOUD, VIEW_MULTICLOUD, ADD_INVOICE, DELETE_INVOICE, VIEW_PROJECT, VIEW_INTEGRATION, VIEW_DASHBOARD, VIEW_INVOICE, VIEW_SUBSCRIPTION, VIEW_MARKETPLACE, VIEW_CONTRACT, EDIT_CURRENCY, CREATE_BUDGET, REVIEW_USER, APPROVE_USER, REVIEW_DEPARTMENT, APPROVE_DEPARTMENT, REVIEW_APPLICATION, APPROVE_APPLICATION, REVIEW_PROJECT, APPROVE_PROJECT";
	public static final String ROLE_REVIEWER = "VIEW_USER, VIEW_APPLICATION, VIEW_DEPARTMENT, VIEW_ONBOARDINGMGMT, REVIEW_ONBOARDINGMGMT, APPROVE_ONBOARDINGMGMT, VIEW_MULTICLOUD, VIEW_PROJECT, VIEW_INTEGRATION, VIEW_DASHBOARD, VIEW_INVOICE, VIEW_SUBSCRIPTION, VIEW_MARKETPLACE, VIEW_CONTRACT, REVIEW_USER, APPROVE_USER, REVIEW_DEPARTMENT, APPROVE_DEPARTMENT, REVIEW_APPLICATION, APPROVE_APPLICATION, REVIEW_PROJECT, APPROVE_PROJECT";
	public static final String ROLE_APPROVER = "VIEW_USER, VIEW_APPLICATION, VIEW_DEPARTMENT, VIEW_ONBOARDINGMGMT, REVIEW_ONBOARDINGMGMT, APPROVE_ONBOARDINGMGMT, VIEW_MULTICLOUD, VIEW_PROJECT, VIEW_INTEGRATION, VIEW_DASHBOARD, VIEW_INVOICE, VIEW_SUBSCRIPTION, VIEW_MARKETPLACE, VIEW_CONTRACT, REVIEW_USER, APPROVE_USER, REVIEW_DEPARTMENT, APPROVE_DEPARTMENT, REVIEW_APPLICATION, APPROVE_APPLICATION, REVIEW_PROJECT, APPROVE_PROJECT";
	public static final String ROLE_CONTRIBUTOR = "VIEW_USER, VIEW_APPLICATION, VIEW_DEPARTMENT, ADD_USER, ADD_APPLICATION, ADD_DEPARTMENT, EDIT_USER, EDIT_APPLICATION, EDIT_DEPARTMENT, DELETE_USER, DELETE_APPLICATION, DELETE_DEPARTMENT, VIEW_REQUESTMGMT, ENABLE_INTEGRATION, REMOVE_INTEGRATION, MAP_INTEGRATION, VIEW_MULTICLOUD, VIEW_INTEGRATION, VIEW_DASHBOARD, VIEW_INVOICE, VIEW_SUBSCRIPTION, VIEW_PROJECT, EDIT_PROJECT, VIEW_MARKETPLACE, VIEW_CONTRACT, ADD_CONTRACT, EDIT_CONTRACT, ADD_PROJECT";
	public static final String ROLE_SUPPORT = "ADD_WORKFLOW, EDIT_WORKFLOW, VIEW_WORKFLOW, VIEW_USER, VIEW_APPLICATION, VIEW_DEPARTMENT, VIEW_MULTICLOUD, VIEW_PROJECT, VIEW_INTEGRATION, VIEW_DASHBOARD, VIEW_INVOICE, VIEW_SUBSCRIPTION, VIEW_MARKETPLACE, VIEW_CONTRACT, ADD_INVOICE, DELETE_INVOICE";
	public static final String ROLE_CLM = "VIEW_CONTRACT, ADD_CONTRACT, EDIT_CONTRACT";

	public static final String APPLICATION_DETAILS_NOT_FOUND = "application details not found!";

	public static final String CLOUD_DETAILS_NOT_FOUND = "service details not found!";

	public static final int EMAIL_VERIFICATION_CODE_EXPIRE_DATE = 2880;

	public static final String USER_ID_ERROR_MESSAGE = "UserId or EmailAddress must be valid";
	public static final String USER_ID_ERROR_KEY = "userId";
	public static final String VERIFY_INITIATE_URL = "/api/userprofile/verify-initiate";
	public static final String RESET_INITIATE_URL = "/api/userprofile/reset-initiate";
	public static final String VERIFY_EMAIL_ERROR_KEY = "emailAddress";
	public static final String VERIFY_EMAIL_ERROR_MESSAGE = "Email is already verified";

	public static final String USER_NAME_OR_EMAIL_REQUIRED_ERROR_MESSAGE = "EmailAddress or Password is required";

	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";
	public static final String SIGN_UP_URL = "/api/userprofile/signup";
	public static final String LOGIN_URL = "/api/userprofile/login";
	public static final String RESET_PASSWORD_URL = "/api/userprofile/reset-password";
	public static final String VERIFY_EMAIL_URL = "/api/userprofile/verify-email";
	public static final String VERIFY_OTP = "/api/userprofile/verify-otp";
	public static final String CREATE_PASSWORD = "/api/auth/create-password";
	public static final String REFRESH_TOKEN = "/api/userprofile/refresh/token";
	public static final String DOCUSIGN_EVENTS = "/docusign/events";
	public static final String ENQUIRY = "/api/enquiry";
	public static final String SITE_VERIFY = "/api/siteverify";
	public static final String CONFIRM_PASSWORD_ERROR_MESSAGE = "Password and Confirm Password don't match";
	public static final String NEW_PASSWORD_EQUALS_OLD_PASSWORD_ERROR_MESSAGE = "New Password cannot be the same as Old Password";

	public static final String ACTIVE = "Active";
	public static final String EXPIRED = "Expired";
	public static final String INACTIVE = "InActive";

	public static final String REVIEWER = "Reviewer";
	public static final String APPROVER = "Approver";

	public static final String ADMIN = "admin";
	public static final String TOTAL = "total";

	public static final String SUPER_ADMIN = "superadmin";
	public static final String SUPERADMIN = "super_admin";
	public static final List<String> USER_ROLES = Arrays.asList(REVIEWER, APPROVER, SUPERADMIN, "contributor",
			"support", "custom");

	public static final String APPROVE = "Approve";
	public static final String REVIEW = "Review";
	public static final String REJECTED = "Rejected";
	public static final String TOTAL_SPEND = "totalSpend";

	public static final String SAASPE = "SAASPE";
	public static final String BUID = "BUID_01";
	public static final String BUID_02 = "BUID_02";
	public static final String DASHBOARD_ANALYSTICS_RESPONSE = "DashBoardAnalysticsResponse";

	public static final String EMAIL = "email";
	public static final String TOKEN = "token";
	public static final String CLM_USER = "clm-user";
	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String PROJECT_WITH_NAME = "Project with Name ";
	public static final String DEPARTMENT_AT_ROW = " department at Row ";
	public static final String CONTRIBUTOR = "CONTRIBUTOR";

	public static final String PENDING_WITH_REVIEWER = "Pending With Reviewer";
	public static final String PENDING_WITH_APPROVER = "Pending With Approver";
	public static final String APPROVED_BY_SUPERADMIN = "Approved By SuperAdmin";
	public static final String REJECTED_BY_SUPERADMIN = "Rejected by SuperAdmin";
	public static final String APPROVED_BY_REVIEWER = "Approved by Reviewer";
	public static final String REJECTED_BY_REVIEWER = "Rejected by Reviewer";
	public static final String APPROVED_BY_APPROVER = "Approved by Approver";
	public static final String REJECTED_BY_APPROVER = "Rejected by Approver";

	public static final String APPLICATION_ID = "ADET_0";
	public static final String DEPARTMENT_ID = "DEPT_0";
	public static final String USER_ID = "USER_0";
	public static final String LICENSE_ID = "LICE_0";
	public static final String CONTRACT_ID = "CONT_0";
	public static final String INVOICE_ID = "INV_0";
	public static final String SUBSCRIPTION_ID = "SUB_0";
	public static final String PROJECT_ID = "PROJ_0";

	public static final String URI = "\\u00A0";

	public static final String CONTRACT_EMAIL_SUBJECT = "Contract Renewal Reminder";
	public static final String APPLICATION_EMAIL_SUBJECT = "New Application Onboarding Request";
	public static final String USER_EMAIL_SUBJECT = "create-password";
	public static final String MARKETPLACE_EMAIL_SUBJECT = "Marketplace Enquiry";
	public static final String MULTICLOUD_AZURE_EMAIL_SUBJECT = "Mutlticloud Azure Recommendation";
	public static final String MULTICLOUD_AWS_EMAIL_SUBJECT = "Mutlticloud AWS Recommendation";
	public static final String BUDGET_EMAIL_SUBJECT = "Budget Reminder mail";
	public static final String USER_VERIFY_SUBJECT = "Please verify your registration";
	public static final String USER_PWDRESET_EMAIL_SUBJECT = "Reset Password";
	public static final String USER_SIGNUP_EMAIL_SUBJECT = "Signup Rejected!";
	public static final String ENQUIRY_VERIFY_EMAIL_SUBJECT = "Please verify sales enquiry";

	public static final String DETAILS_RETRIEVED_SUCCESSFULLY = "Details retrieved successfully";
	public static final String DETAILS_UPDATED_SUCCESSFULLY = "Details updated successfully";
	public static final String USER_DETAILS = "UserDetails";
	public static final String GIT_LAB = "GitLab";
	public static final String DEPARTMENT = "Department";
	public static final String APPLICATION = " Application ";
	public static final String TRACK = "Track";
	public static final String PROJECT = "Project";
	public static final String ON_BOARDING = "Onboarding";
	public static final String USER = "User";
	public static final String IN_PROGRESS = "InProgress";
	public static final String COMPLETED = "Completed";

	public static final String NO_LAST_LOGIN = " : don't have any last login user with in the given range";

	public static final String USER_0 = "USER_0";

	public static final String VALID_ID = "Provide Valid Id";

	public static final String USER_DETAILS_RESPONSE = "UserDetailsResponse";
	public static final String USER_RESPONSE = "UserResponse";

	public static final String AVATAR_13 = "https://saaspemedia.blob.core.windows.net/images/avatar/svg/avatar-13.svg";

	public static final String ONBOARDING_WORK_FLOW_ACTION_RESPONSE = "OnboardingWorkFlowActionResponse";

	public static final String PROVIDE_VALID_VALUE = "Provide Valid Value";

	public static final String CLIENT_ERROR = "A Client side exception occurred, please get back after sometime!!";

	public static final String INSUFFICIENT_STORAGE = "INSUFFICIENT_STORAGE";

	public static final String DEPARTMENT_BUDGET_ALERT = "Department Budget Alert";

	public static final String URL_ERROR = "URL Error";

	public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

	public static final String UNABLE_TO_CONNECT_TO_AZURE = "Unable to Connect to Azure, Please Check URL in properties";

	public static final String START_TIME = "startTime";

	public static final String END_TIME = "endTime";

	public static final String EXISTING = "Existing";

	public static final String X_AUTH_PROVIDER = "X-Auth-Provider";

	public static final String TIME_TAKEN = "timeTaken";

	public static final String AUTH_USER_CRED_ERROR = "*** Ending addUserCredentials method with an error ***";

	public static final String INTEGRATION_LIST_RESPONSE = "IntegrationsListResponse";

	public static final String AUTH_CODE = "auth_code";
	public static final String DEV = "dev";

	public static final String MISMATCH_ERROR = " does not match.";
	public static final String NOT_EXIST = " Does Not Exist";

	public static final String USER_ACCESS_AND_ROLES_RESPONSE = "UserAccessAndRolesResponse";
	public static final String USER_PROFILE = "UserProfile";
	public static final String LOGIN_RESPONSE = "LoginResponse";
	public static final String ENDING_RESET_PASSWORD_ERROR = "*** Ending resetPassword method with an error *** ";
	public static final String NAME = "{{name}}";
	public static final String USER_NOT_FOUND = "User not found";
	public static final String DATA_RETRIEVED_SUCCESSFULLY = "Data retrieved successfully";
	public static final String LICENSE_RESPONSE = "LicenseResponse";

	public static final String ONE_TIME = "OneTime";
	public static final String ANNUALLY = "Annually";
	public static final String ADMIN_SPEND = "adminSpend";

	public static final String SEMI_ANNUALLY = "Semi-Annually";
	public static final String MONTHLY = "Monthly";
	public static final String QUARTERLY = "Quarterly";
	public static final String USER_NAME = "USER_NAME";
	public static final String USER_EMAIL = "USER_EMAIL";
	public static final String USER_DESIGNATION = "USER_DESIGNATION";
	public static final String USER_LAST_LOGIN = "USER_LAST_LOGIN";
	public static final String USER_LOGIN = "userlogin.xlsx";
	public static final String WORK_FLOW = "workflow.html";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String APP_NAME = "{{applicationName}}";
	public static final String SELECTED_RANGE = "{{selctedRange}}";
	public static final String MESSAGE = "{{message}}";

	public static final String REQUEST_APP = "REQ_APP_0";
	public static final String DEPARTMENT_WITH_NAME = "Department with Name ";
	public static final String DEPARTMENT_PROJECT = "Department with project ";
	public static final String APPLICATION_EMAIL_ADDRESS = "Application Owner with Email addresss ";
	public static final String FOR_DEPARTMENT_WITH_NAME = " For Department With Name ";
	public static final String FOR_CONTRACT = " For Contract ";
	public static final String AT_ROW = " at row ";
	public static final String USER_WITH_EMAIL = "User with Email ";
	public static final String CATEGORY_WITH_NAME = "Category with Name ";
	public static final String APPLICATION_ONBOARDING_LIST_VIEW_RESPONSE = "applicationOnboardingListViewResponse";
	public static final String CONTRACT_DETAILS_OVERVIEW_RESPONSE = "contractDetailsOverviewResponse";
	public static final String CONTRACT_ONBOARDING_LIST_VIEW_RESPONSE = "contractOnboardingListViewResponse";
	public static final String VALID_VALUE = "Provide Valid Value";
	public static final String MULTI_CLOUD_RESPONSE = "MulticloudResponse";
	public static final String UNBLENDED_COST = "UnblendedCost";
	public static final String OPTIMIZE_EMAIL_TRIGGER_RESPONSE = "OptimizeEmailTriggerResponse";
	public static final String CONVERSATION_DETAIL_RESPONSE = "ConversationDetailResponse";
	public static final String CLM_CONTRACT_RESPONSE = "CLM Contract Response";
	public static final String UPDATE_TEMPLATE_RESPONSE = "UpdateTemplateResponse";
	public static final String CREATE_TEMPLATE_RESPONSE = "CreateTemplateResponse";
	public static final String ENVELOPE_DOCUMENT_RESPONSE = "Envelope document Response";
	public static final String HTTP_STATUS_CODE = "HTTP status code: ";
	public static final String LISTENVELOPE_RESPONSE = "ListEnvelope Recipients Response";
	public static final String ENVELOPE_DOCUMENT_DETAILS_RESPONSE = "EnvelopeDocumentdetails Response";
	public static final String TEMPLATE_LIST_RESPONSE = "TemplateList Response";
	public static final String TEMPLATE_DOCUMENT_RESPONSE = "Template document Response";

	public static final String CURRENCY_MISMATCH = "Currency does not match.";

	public static final String ROW_NUMBER = "rowNumber";
	public static final String EXCEL_UPLOAD_APPLICATION = "Excel Upload Application";
	public static final String EXCEL_UPLOAD_FAILED = "Application Excel Upload Failed";

	public static final String APPLICATION_NAME = "ApplicationName";
	public static final String APP_CATEGORY = "ApplicationCategory";
	public static final String APP_OWNER_NAME = "ApplicationOwnerName";
	public static final String APP_OWNER_EMAIL = "ApplicationOwnerEmailAddress";
	public static final String SECONDARY_OWNER_EMAIL = "SecondaryOwnerEmailAddress";
	public static final String OWNER_DEPT = "OwnerDepartment";
	public static final String PROJECT_NAME = "ProjectName";
	public static final String SUB_ID = "SubscriptionID";
	public static final String PROVIDER_NAME = "ProviderName";
	public static final String CONT_NAME = "ContractName";
	public static final String CONT_START_DATE = "ContractStartDate(DD/MM/YYYY)";
	public static final String CONT_END_DATE = "ContractEndDate(DD/MM/YYYY)";
	public static final String AUTO_RENEWAL = "AutoRenewal";
	public static final String PROD_TYPE = "ProductType";
	public static final String PROD_NAME = "ProductName";
	public static final String UNIT_PRICE_STRING = "UnitPrice";
	public static final String UNIT_PRICE_TYPE = "UnitPriceType";
	public static final String QUANTITY = "Quantity";
	public static final String TOTAL_COST = "TotalCost";
	public static final String CONT_TYPE = "ContractType";
	public static final String CURRENCY_STRING = "Currency";
	public static final String BILL_FREQUENCY = "BillingFrequency";
	public static final String CONT_TENURE = "ContractTenure(Years)";
	public static final String NEXT_RENEWAL = "NextRenewalDate(DD/MM/YYYY)";
	public static final String PAY_METHOD = "PaymentMethod";
	public static final String WALLET_NAME = "WalletName";
	public static final String CARD_HOLDER_NAME = "CardholderName";
	public static final String CARD_NUMBER = "CardNumber";
	public static final String VALID_THROUGH = "ValidThrough";
	public static final String CANCELLATION_NOTICE = "CancellationNotice(Days)";
	public static final String WALLET = "Wallet";
	public static final String AZURE = "Azure";
	public static final String RAW_RESPONSE = "raw_response";
	public static final String TEMPLATE_ID = "templateId";
	public static final String CREATE_ID = "create_id";
	public static final String ENVELOPE_ID = "{envelopeId}";
	public static final String USERNAME = "userName";
	public static final String STATUS = "status";
	public static final String CREATED_DATE_TIME = "createdDateTime";
	public static final String COMPLETED_DATE_TIME = "completedDateTime";
	public static final String TEMPLATEID = "{templateId}";
	public static final String TEMPLATE_NAME = "template_name";
	public static final String DOCUSIGN_REDIS_PREFIX = "DS";


	public static final List<String> BILLING_FREQUENCY = Arrays.asList(MONTHLY, QUARTERLY, SEMI_ANNUALLY, ANNUALLY,
			ONE_TIME);
	public static final List<String> CONTRACT_TYPE = Arrays.asList("Annual", "Month-To-Month", ANNUALLY);
	public static final List<String> CLOUD_PROVIDER = Arrays.asList("GCP", AZURE, "AWS");
	public static final List<String> CURRENCY = Arrays.asList("USD", "MYR", "PHP", "SGD", "INR", "EUR", "GBP", "AUD");
	public static final List<String> PRODUCT_TYPE = Arrays.asList("Licenses", "Platform", "Professional Services",
			"ProfessionalServices");
	public static final List<String> UNIT_PRICE = Arrays.asList("per month", "per year", "per contract tenure");
	public static final List<String> USER_ROLE = Arrays.asList("Contributor", REVIEWER, APPROVER, SUPERADMIN,
			CONTRIBUTOR, "REVIEWER", "APPROVER", "SUPER_ADMIN","SUPPORT","Support");

	public static final List<String> DESIGNATIONFORSIGNUPUSER = Arrays.asList("President/ Vice President", "CxO",
			"Director", "Manager", "Procurement Executive", "PMO");
	public static final List<String> DESIGNATIONFORONBOARDUSER = Arrays.asList("Software Engineer", "Technical Lead",
			"Software Architect", "UI/UX Engineer", "QA Engineer", "Platform Engineer", "Team Lead", "Consultant",
			"Platform Architect", "HR Recruiter", "Platform Admin", "Business Analyst", "Product Owner",
			"Operations Staff", "Operations Lead", "Accountant");
	public static final List<String> APPLICATION_TYPE = Arrays.asList("New", EXISTING);
	public static final List<String> GENDER = Arrays.asList("Male", "Female", "Others");
	public static final List<String> EMPLOYEMENT = Arrays.asList("Permanent", "Contract");
	public static final List<String> AZURE_BUDGET_PERIOD = Arrays.asList(MONTHLY, QUARTERLY, ANNUALLY, "BillingMonth",
			"BillingQuarter", "BillingAnnual");

	public static final List<String> AZURE_BUDGET_THERSHOLD_TYPE = Arrays.asList("Actual", "Forecasted");
	public static final List<String> CLM_ENVELOPE_STATUS = Arrays.asList("completed", "created", "declined",
			"delivered", "sent", "signed", "voided");
	public static final List<String> COUNTRY_CODE = Arrays.asList("+1", "+20", "+27", "+30", "+31", "+32", "+33", "+34",
			"+36", "+39", "+41", "+42", "+43", "+44", "+45", "+46", "+47", "+48", "+60", "+61", "+62", "+63", "+64",
			"+65", "+66", "+81", "+84", "+90", "+91", "+92", "+93", "+94", "+95", "+98", "+213", "+216", "+220", "+221",
			"+223", "+224", "+225", "+226", "+227", "+229", "+230", "+231", "+232", "+233", "+234", "+235", "+237",
			"+238", "+239", "+240", "+241", "+242", "+243", "+244", "+245", "+246", "+248", "+249", "+250", "+251",
			"+253", "+254", "+255", "+256", "+257", "+260", "+261", "+262", "+263", "+265", "+266", "+267", "+268",
			"+269", "+290", "+291", "+297", "+298", "+299", "+350", "+351", "+352", "+353", "+357", "+358", "+359",
			"+370", "+371", "+372", "+373", "+374", "+375", "+376", "+377", "+378", "+380", "+381", "+382", "+383",
			"+387", "+389", "+420", "+421", "+423", "+500", "+501", "+502", "+503", "+504", "+505", "+506", "+507",
			"+509", "+590", "+591", "+592", "+594", "+595", "+596", "+597", "+599", "+681", "+682", "+683", "+685",
			"+686", "+687", "+688", "+689", "+690", "+691", "+692", "+694", "+695", "+697", "+698", "+699", "+850",
			"+852", "+853", "+855", "+856", "+872", "+880", "+881", "+882", "+884", "+886", "+888", "+960", "+961",
			"+962", "+963", "+964", "+965", "+966", "+967", "+968", "+969", "+971", "+972", "+972", "+972", "+973",
			"+974", "+975", "+976", "+977", "+994");

	public static final String DELETED = "Deleted";
	public static final String CREATED = "Created";
	public static final String EDITED = "Edited";
	public static final String DELETE = "delete";
	public static final String SENDER = "sender";
	public static final String INTERNAL = "internal";
	public static final String BEARER = "Bearer ";

	public static final String APP_ID = "?appId=";
	public static final String ACCOUNT_ID = "accountId";
	public static final String GROUPS = "groups";
	public static final String APPID = "appId";
	public static final String HOST = "{host}";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String APPLICATION_JSON = "application/json";
	public static final String USEREMAIL = "userEmail";
	public static final String MONGODB = "mongodb://";
	public static final String AUTHMECHANISM = "?authMechanism=SCRAM-SHA-256&authSource=admin";
	public static final String CLOUD_VENDORS_RESPONSE = "CloudVendorsResponse";
	public static final String SAVE_NEW_APP_RESPONSE = "SaveNewAppResponse";
	public static final String ACCESS_TOKEN = "accessToken";
	public static final String CLIENT_ID = "client_id";
	public static final String CLIENT_SECRET = "client_secret";
	public static final String SUCCESS = "Success";
	public static final String CONSISTENCY_LEVEL = "ConsistencyLevel";
	public static final String EVENTUAL = "eventual";
	public static final String UTF = "UTF-8";
	public static final String ZOHOCRM = "ZohoCRM";
	public static final String ZOHOPEOPLE = "Zoho People";
	public static final String ZOHOANALYTICS = "Zoho Analytics";
	public static final String PER_YEAR = "per year";
	public static final String APPLN = "Application ";
	public static final String APIKEY = "apikey";
	public static final String ADAPTORS_SAVE_RESPONSE = "Adapters details saved successfully";
	public static final String ISNULL = "is null!";
	public static final String AZURE_AD = "Azure AD";
	public static final List<String> OAUTH_APPS = Arrays.asList("MICROSOFT 365", "QUICKBOOKS");
	public static final String APP = "&appId=";
	public static final String INVALID_CREDENTIALS = "Invalid credentials";
	public static final String LICENSE_NOT_ASSIGNED = "License is not assigned for the user ";

	public static final String CODE = "code";
	public static final String QUICKBOOOKS = "Quickbooks";
	public static final String CREATE_ADMIN_RESPONSE = "CreateAdminResponse";
	public static final String SEND_EMAIL_TO_USER_EROR = "*** ending sendEmailToUser method with error ***";
	

	public static final String NO_APPLICATIONS_FOUND = "No applications found!";

	public static final String CONFLUENCE = "Confluence";
	public static final String DATADOG = "Datadog";
	public static final String SALESFORCE = "Salesforce";
	public static final String MICROSOFT_365 = "Microsoft365";
	public static final String APITOKEN = "apiToken";
	public static final String ZOOM = "Zoom";

	public static final String SAVE_NEW_APPLICATION_RESPONSE = "SaveNewApplicationResponse";
	public static final String ADAPTORS_DETAILS_SAVED_SUCCESSFULLY = "Adaptor details saved successfully";
	
	
	public static final String SEQUENCE_NAME = "CommonDocumentSequence";

}
