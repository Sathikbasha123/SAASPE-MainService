package saaspe.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import saaspe.configuration.DateParser;
import saaspe.constant.Constant;
import saaspe.entity.ApplicationContractDetails;
import saaspe.entity.ApplicationDetails;
import saaspe.entity.ApplicationLicenseDetails;
import saaspe.entity.ApplicationLogoEntity;
import saaspe.entity.DepartmentDetails;
import saaspe.entity.ProjectApplicationStatus;
import saaspe.entity.ProjectDetails;
import saaspe.entity.ProjectManagerDetails;
import saaspe.entity.ProjectOnboardingDetails;
import saaspe.entity.Projects;
import saaspe.entity.SequenceGenerator;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.DeptApplicationUsageAnalystics;
import saaspe.model.NewApplicationOnboardingResposne;
import saaspe.model.ProjectApplicationListRequest;
import saaspe.model.ProjectDetailsUpdateRequest;
import saaspe.model.ProjectWorkflowDetailsView;
import saaspe.model.ProjectWorkflowListViewResponse;
import saaspe.model.ProjectWorkflowReviewRequest;
import saaspe.model.ProjectWorkflowReviewerDetailsview;
import saaspe.model.ProjectsListByDepartmentResponse;
import saaspe.model.ProjectsListResponse;
import saaspe.model.Response;
import saaspe.model.SingleProjectApplicationOnboarding;
import saaspe.model.SingleProjectOnboardingRequest;
import saaspe.repository.ApplicationContractDetailsRepository;
import saaspe.repository.ApplicationDetailsRepository;
import saaspe.repository.ApplicationLogoRepository;
import saaspe.repository.DepartmentRepository;
import saaspe.repository.ProjectApplicationStatusRepository;
import saaspe.repository.ProjectDetailsRepository;
import saaspe.repository.ProjectOnboardingDetailsRepository;
import saaspe.repository.ProjectOwnerRepository;
import saaspe.repository.ProjectsRepository;
import saaspe.repository.SequenceGeneratorRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.service.ProjectDetailsService;

@Service
public class ProjectDetailsServiceImpl implements ProjectDetailsService {

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	private ProjectDetailsRepository projectDetailsRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private ProjectOnboardingDetailsRepository projectOnboardingDetailsRepository;

	@Autowired
	private ProjectApplicationStatusRepository projectApplicationStatusRepository;

	@Autowired
	private SequenceGeneratorRepository sequenceGeneratorRepository;

	@Autowired
	private ApplicationLogoRepository applicationLogoRepository;

	@Autowired
	private ApplicationDetailsRepository applicationDetailsRepository;

	@Autowired
	private ProjectOwnerRepository ownerRepository;

	@Autowired
	ApplicationContractDetailsRepository applicationContractDetailsRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private Configuration config;

	@Value("${redirecturl.path}")
	private String redirectUrl;

	@Value("${sendgrid.domain.name}")
	private String mailDomainName;

	@Value("${sendgrid.domain.sendername}")
	private String senderName;

	@Value("${budget-mail.trigger.dev}")
	private boolean budgetMailTrigger;
	
	@Autowired
	private ProjectsRepository projectRepository;

	@Override
	@Transactional
	public CommonResponse projectSingleOnboarding(SingleProjectOnboardingRequest projectOnboardingRequest,
			UserLoginDetails profile) throws DataValidationException {
		NewApplicationOnboardingResposne onboardingResponse = new NewApplicationOnboardingResposne();
		ProjectOnboardingDetails projectDetails = new ProjectOnboardingDetails();
		List<ProjectDetails> projectdetails = projectDetailsRepository.findAll();
		String request = Constant.PROJECT_ID;
		Integer sequence = sequenceGeneratorRepository.getRequestNumberSequence();
		request = request.concat(sequence.toString());
		DepartmentDetails departmentDetails = departmentRepository
				.findByDepartmentId(projectOnboardingRequest.getDepartmentId());
		if (departmentDetails == null) {
			throw new DataValidationException("Selected department is not found, please select other department", null,
					null);
		}
		ProjectOnboardingDetails projectOnboardingDetails = projectOnboardingDetailsRepository
				.findAllByProjectName(projectOnboardingRequest.getProjectName());
		if (projectOnboardingDetails != null
				&& (projectOnboardingDetails.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW))) {
			throw new DataValidationException(
					"Project with " + projectOnboardingRequest.getProjectName() + " Already Exists", null, null);
		}
		if (projectDetailsRepository.findByProjectName(projectOnboardingRequest.getProjectName()) != null) {
			throw new DataValidationException(
					"Project with " + projectOnboardingRequest.getProjectName() + " Already Exists", null, null);
		}
		Set<String> nonDuplicate = new HashSet<>(projectOnboardingRequest.getProjectManagerEmail());
		if (nonDuplicate.size() != projectOnboardingRequest.getProjectManagerEmail().size()) {
			throw new DataValidationException("Duplicate project manager email found", null, null);
		}
		for (String projectManagerEmail : projectOnboardingRequest.getProjectManagerEmail()) {
			if (userDetailsRepository.findByDepartmentIdAndUserEmail(projectOnboardingRequest.getDepartmentId(),
					projectManagerEmail) == null) {
				throw new DataValidationException("Project Manager With "
						+ projectOnboardingRequest.getProjectManagerEmail() + " Doesn't Exist In Given Department",
						null, null);
			}
		}
		if (!Constant.CURRENCY.contains(projectOnboardingRequest.getCurrency())) {
			throw new DataValidationException("Currency code is not matching with the existing list", null, null);
		}
		for (SingleProjectApplicationOnboarding applicationOnboarding : projectOnboardingRequest
				.getApplicationsInfo()) {
			String appName = applicationOnboarding.getApplicationName();
			ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository.findByApplicationName(appName);
			if (applicationLogoEntity == null) {
				throw new DataValidationException(
						"Invalid application name, not present in the existing list: " + appName, null, null);
			}
			if (!Constant.APPLICATION_TYPE.contains(applicationOnboarding.getApplicationCategory())) {
				throw new DataValidationException("Invalid application status" + " "
						+ applicationOnboarding.getApplicationCategory() + " for the application: " + appName, null,
						null);
			}

			boolean departmentHasApplication = departmentDetails.getApplicationId().stream()
					.anyMatch(app -> app.getApplicationName().trim().equals(appName.trim())
							&& app.getActiveContracts() != null && app.getEndDate() == null
							&& applicationOnboarding.getApplicationCategory().equalsIgnoreCase(Constant.EXISTING));
			if (!departmentHasApplication && !applicationOnboarding.getApplicationCategory().equals("New")) {
				throw new DataValidationException("Department with " + departmentDetails.getDepartmentName()
						+ " doesn't have any application like " + appName, null, null);
			}

			for (ApplicationDetails application : departmentDetails.getApplicationId()) {
				if (application.getApplicationName().equalsIgnoreCase(appName)) {
					List<ApplicationContractDetails> activeContracts = applicationContractDetailsRepository
							.findActiveContractsByAppId(application.getApplicationId());

					if (activeContracts.isEmpty() && !applicationOnboarding.getApplicationCategory().equals("New")) {
						throw new DataValidationException("No active contract found for the application: " + appName,
								null, null);
					}
				}
			}
			if (applicationOnboarding.getApplicationCategory().trim().equalsIgnoreCase(Constant.EXISTING)) {
				boolean applicationBelongsToDepartment = departmentDetails.getApplicationId().stream()
						.anyMatch(app -> app.getApplicationName().trim().equalsIgnoreCase(appName)
								&& app.getOwnerDepartment().equalsIgnoreCase(departmentDetails.getDepartmentName()));
				if (!applicationBelongsToDepartment) {
					throw new DataValidationException(Constant.PROJECT_WITH_NAME
							+ projectOnboardingRequest.getProjectName() + Constant.APPLICATION + appName
							+ " does not belong to the given " + departmentDetails.getDepartmentName() + " department",
							null, null);
				}
			}
			boolean projectCodeExists = projectdetails.stream().anyMatch(project -> project.getProjectCode() != null
					&& project.getProjectCode().equalsIgnoreCase(projectOnboardingRequest.getProjectCode().trim()));
			if (projectCodeExists) {
				throw new DataValidationException(Constant.PROJECT_WITH_NAME + projectOnboardingRequest.getProjectName()
						+ Constant.APPLICATION + appName + " project code already exists", null, null);
			}
		}
		ObjectMapper obj = new ObjectMapper();

		try {
			String objToString = obj.writeValueAsString(projectOnboardingRequest);
			projectDetails.setProjectOnboardingRequest(objToString);
		} catch (JsonProcessingException e) {
			String objToString = "";
			throw new DataValidationException("EndUp Error Converting Object into String", objToString, null);
		}
		projectDetails.setApprovedRejected(Constant.REVIEW);
		projectDetails.setRequestNumber(request);
		projectDetails.setProjectCode(projectOnboardingRequest.getProjectCode());
		projectDetails.setCreatedBy(profile.getEmailAddress());
		projectDetails.setCreatedOn(new Date());
		projectDetails.setOnboardDate(new Date());
		projectDetails.setBudjetCurrency(projectOnboardingRequest.getCurrency());
		projectDetails.setOnboardedByUserEmail(profile.getEmailAddress());
		projectDetails.setOnboardingStatus(Constant.PENDING_WITH_REVIEWER);
		projectDetails.setProjectName(projectOnboardingRequest.getProjectName());
		projectDetails.setWorkGroup(Constant.REVIEWER);
		onboardingResponse.setRequestId(request);

		SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
		updateSequence.setRequestId(++sequence);
		sequenceGeneratorRepository.save(updateSequence);
		projectOnboardingDetailsRepository.save(projectDetails);
		return new CommonResponse(HttpStatus.CREATED, new Response("projectOnboardingRequest", onboardingResponse),
				"Project onboarding details submitted successfully");
	}

	@Override
	@Transactional
	public CommonResponse projectOnboardReview(String childRequestId, String requestId, UserLoginDetails profile,
			ProjectWorkflowReviewRequest onboardingWorkFlowRequest)
			throws DataValidationException, JsonGenerationException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		if (childRequestId == null && requestId == null) {
			response.setData("Check Parameters");
			response.setAction("Onboarding Work flow Action Response");
			commonResponse.setMessage("Wrong Param or Null values in param");
			commonResponse.setResponse(response);
			commonResponse.setStatus(HttpStatus.CONFLICT);
			return commonResponse;
		}
		if (childRequestId != null && requestId != null) {
			response.setData("Check Parameters");
			response.setAction("Onboarding Work flow Action Response");
			commonResponse.setMessage("Provide Either ChildRequestId or RequestId");
			commonResponse.setResponse(response);
			commonResponse.setStatus(HttpStatus.CONFLICT);
			return commonResponse;
		}
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)
				|| userDetails.getUserRole().equalsIgnoreCase(Constant.SUPERADMIN)) {
			if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
				if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
					if (requestId == null && childRequestId != null) {
						ProjectOnboardingDetails childRequest = projectOnboardingDetailsRepository
								.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
						if (childRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
									HttpStatus.CONFLICT);
						}
						ProjectOnboardingDetails projectOnboardingDetails = new ProjectOnboardingDetails();
						projectOnboardingDetails.setChildRequestNumber(childRequestId);
						projectOnboardingDetails.setWorkGroup(Constant.APPROVER);
						projectOnboardingDetails.setApprovedRejected(Constant.REVIEW);
						projectOnboardingDetails.setOnboardingStatus(Constant.PENDING_WITH_APPROVER);
						projectOnboardingDetails.setComments(onboardingWorkFlowRequest.getComments());
						projectOnboardingDetails.setCreatedOn(new Date());
						projectOnboardingDetails.setCreatedBy(childRequest.getCreatedBy());
						projectOnboardingDetails.setOpID(childRequest.getOpID());
						projectOnboardingDetails.setBuID(childRequest.getBuID());
						projectOnboardingDetails.setWorkGroupUserEmail(profile.getEmailAddress());
						projectOnboardingDetails.setUpdatedOn(new Date());
						projectOnboardingDetails.setUpdatedBy(profile.getEmailAddress());
						projectOnboardingDetails.setRequestNumber(childRequest.getRequestNumber());
						projectOnboardingDetails.setOnboardedByUserEmail(childRequest.getOnboardedByUserEmail());
						projectOnboardingDetails.setOnboardDate(childRequest.getOnboardDate());
						projectOnboardingDetails.setProjectName(childRequest.getProjectName());
						projectOnboardingDetails.setProjectCode(childRequest.getProjectCode());
						projectOnboardingDetails.setBudjetCurrency(childRequest.getBudjetCurrency());
						projectOnboardingDetails
								.setProjectOnboardingRequest(childRequest.getProjectOnboardingRequest());
						childRequest.setOnboardingStatus(Constant.APPROVED_BY_REVIEWER);
						childRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						childRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						childRequest.setEndDate(new Date());
						childRequest.setComments(onboardingWorkFlowRequest.getComments());
						childRequest.setUpdatedBy(profile.getFirstName());
						childRequest.setUpdatedOn(new Date());
						projectOnboardingDetailsRepository.save(projectOnboardingDetails);
						projectOnboardingDetailsRepository.save(childRequest);
					}
					if (requestId != null && childRequestId == null) {
						ProjectOnboardingDetails parentRequest = projectOnboardingDetailsRepository
								.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
						if (parentRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
									HttpStatus.CONFLICT);
						}
						ProjectOnboardingDetails projectOnboardingDetails = new ProjectOnboardingDetails();
						projectOnboardingDetails.setChildRequestNumber(childRequestId);
						projectOnboardingDetails.setWorkGroup(Constant.APPROVER);
						projectOnboardingDetails.setApprovedRejected(Constant.REVIEW);
						projectOnboardingDetails.setOnboardingStatus(Constant.PENDING_WITH_APPROVER);
						projectOnboardingDetails.setComments(onboardingWorkFlowRequest.getComments());
						projectOnboardingDetails.setCreatedOn(new Date());
						projectOnboardingDetails.setCreatedBy(parentRequest.getCreatedBy());
						projectOnboardingDetails.setOpID(parentRequest.getOpID());
						projectOnboardingDetails.setBuID(parentRequest.getBuID());
						projectOnboardingDetails.setWorkGroupUserEmail(profile.getEmailAddress());
						projectOnboardingDetails.setUpdatedOn(new Date());
						projectOnboardingDetails.setUpdatedBy(profile.getEmailAddress());
						projectOnboardingDetails.setRequestNumber(parentRequest.getRequestNumber());
						projectOnboardingDetails.setOnboardedByUserEmail(parentRequest.getOnboardedByUserEmail());
						projectOnboardingDetails.setOnboardDate(parentRequest.getOnboardDate());
						projectOnboardingDetails.setProjectName(parentRequest.getProjectName());
						projectOnboardingDetails.setProjectCode(parentRequest.getProjectCode());
						projectOnboardingDetails.setBudjetCurrency(parentRequest.getBudjetCurrency());
						projectOnboardingDetails
								.setProjectOnboardingRequest(parentRequest.getProjectOnboardingRequest());
						parentRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						parentRequest.setOnboardingStatus(Constant.APPROVED_BY_REVIEWER);
						parentRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						parentRequest.setEndDate(new Date());
						parentRequest.setComments(onboardingWorkFlowRequest.getComments());
						parentRequest.setUpdatedBy(profile.getFirstName());
						parentRequest.setUpdatedOn(new Date());
						projectOnboardingDetailsRepository.save(projectOnboardingDetails);
						projectOnboardingDetailsRepository.save(parentRequest);
					}
					return reviewSuccessResponse();
				} else {
					if (requestId != null) {
						ProjectOnboardingDetails rejectRequest = projectOnboardingDetailsRepository
								.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
						if (rejectRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
									HttpStatus.CONFLICT);
						}
						rejectRequest.setApprovedRejected(Constant.REJECTED);
						if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
							rejectRequest.setOnboardingStatus("Rejected by Reviewer");
						} else {
							rejectRequest.setWorkGroup(Constant.SUPERADMIN);
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
						}
						rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
						projectOnboardingDetailsRepository.save(rejectRequest);
						return reviewFailureResponse();

					} else {
						ProjectOnboardingDetails rejectRequest = projectOnboardingDetailsRepository
								.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
						if (rejectRequest == null) {
							throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId,
									HttpStatus.CONFLICT);
						}
						rejectRequest.setApprovedRejected(Constant.REJECTED);
						if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
							rejectRequest.setOnboardingStatus("Rejected by Reviewer");
						} else {
							rejectRequest.setWorkGroup(Constant.SUPERADMIN);
							rejectRequest.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
						}
						rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
						projectOnboardingDetailsRepository.save(rejectRequest);
						return reviewFailureResponse();
					}
				}
			} else {
				if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
					try {
						superAdminsaveData(requestId, childRequestId, profile, onboardingWorkFlowRequest);
					} catch (DataValidationException e) {
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
					}
					return reviewSuccessResponse();
				} else {
					if (requestId != null) {
						ProjectOnboardingDetails rejectRequestForReviewer = projectOnboardingDetailsRepository
								.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
						ProjectOnboardingDetails rejectRequestForApprover = projectOnboardingDetailsRepository
								.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
						if (rejectRequestForReviewer != null) {
							rejectRequestForReviewer.setApprovedRejected(Constant.REJECTED);
							rejectRequestForReviewer.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequestForReviewer.setComments(onboardingWorkFlowRequest.getComments());
							projectOnboardingDetailsRepository.save(rejectRequestForReviewer);
							return reviewFailureResponse();
						}
						if (rejectRequestForApprover != null) {
							rejectRequestForApprover.setApprovedRejected(Constant.REJECTED);
							rejectRequestForApprover.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequestForApprover.setComments(onboardingWorkFlowRequest.getComments());
							projectOnboardingDetailsRepository.save(rejectRequestForApprover);
							return reviewFailureResponse();
						}
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
					} else {
						ProjectOnboardingDetails rejectRequestForReviewer = projectOnboardingDetailsRepository
								.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
						ProjectOnboardingDetails rejectRequestForApprover = projectOnboardingDetailsRepository
								.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
						if (rejectRequestForReviewer != null) {
							rejectRequestForReviewer.setApprovedRejected(Constant.REJECTED);
							rejectRequestForReviewer.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequestForReviewer.setComments(onboardingWorkFlowRequest.getComments());
							projectOnboardingDetailsRepository.save(rejectRequestForReviewer);
							return reviewFailureResponse();
						}
						if (rejectRequestForApprover != null) {
							rejectRequestForApprover.setApprovedRejected(Constant.REJECTED);
							rejectRequestForApprover.setOnboardingStatus(Constant.REJECTED_BY_SUPERADMIN);
							rejectRequestForApprover.setComments(onboardingWorkFlowRequest.getComments());
							projectOnboardingDetailsRepository.save(rejectRequestForApprover);
							return reviewFailureResponse();
						}
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
					}
				}
			}
		} else {
			if (onboardingWorkFlowRequest.getAction().equalsIgnoreCase(Constant.APPROVE)) {
				if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER)) {
					if (requestId == null && childRequestId != null) {
						ProjectOnboardingDetails childRequest = projectOnboardingDetailsRepository
								.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
						if (childRequest == null) {
							throw new DataValidationException("Provide Valid Id", requestId, HttpStatus.CONFLICT);
						}
						childRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						childRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						childRequest.setOnboardingStatus("Approved By Approver");
						childRequest.setEndDate(new Date());
						childRequest.setUpdatedBy(profile.getFirstName());
						childRequest.setComments(onboardingWorkFlowRequest.getComments());
						childRequest.setUpdatedOn(new Date());

						String name = Constant.PROJECT_ID;
						Integer sequence = sequenceGeneratorRepository.getProjectSequence();
						name = name.concat(sequence.toString());

						SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
						updateSequence.setProjectSequenceId(++sequence);

						SingleProjectOnboardingRequest requestObject = applicationObjectDeserializer(
								childRequest.getProjectOnboardingRequest());
						DepartmentDetails departmentDetails = departmentRepository
								.findByDepartmentId(requestObject.getDepartmentId());
						ProjectDetails projectDetails = new ProjectDetails();
						projectDetails.setBuID(Constant.SAASPE);
						projectDetails.setCreatedBy(profile.getEmailAddress());
						projectDetails.setCreatedOn(new Date());
						projectDetails.setDepartmentId(departmentDetails);
						projectDetails.setProjectBudget(requestObject.getProjectBudget());
						projectDetails.setProjectDescription(requestObject.getProjectDescription());
						projectDetails.setBudgetCurrency(requestObject.getCurrency());
						projectDetails.setProjectId(name);
						projectDetails.setProjectName(childRequest.getProjectName());
						projectDetails.setProjectCode(childRequest.getProjectCode());
						projectDetails.setStartDate(requestObject.getProjectStartDate());
						projectDetails.setEndDate(requestObject.getProjectEndDate());
						projectDetails.setBudgetCurrency(requestObject.getCurrency());

						Integer i = 1;
						for (String managerEmail : requestObject.getProjectManagerEmail()) {
							ProjectManagerDetails managerDetails = new ProjectManagerDetails();
							managerDetails.setCreatedOn(new Date());
							managerDetails.setProjectId(name);
							managerDetails.setProjectManagerEmail(managerEmail);
							managerDetails.setPriority(i);
							managerDetails.setProjectName(childRequest.getProjectName());
							managerDetails.setStartDate(new Date());
							ownerRepository.save(managerDetails);
							i++;
							Projects projects = new Projects();
							projects.setProjectId(name);
							projects.setProjectName(requestObject.getProjectName());
							projects.setBudget(requestObject.getProjectBudget());
							projects.setBudgetCurrency(requestObject.getCurrency());
							projects.setDescription(requestObject.getProjectDescription());
							projects.setCreatedBy(profile.getEmailAddress());
							projects.setProjectCreatedOn(new Date());
							projects.setProjectStartDate(requestObject.getProjectStartDate());
							projects.setProjectEndDate(requestObject.getProjectEndDate());
							projects.setProjectCode(requestObject.getProjectCode());
							projects.setPriority(managerDetails.getPriority());
							projects.setProjectManagerEmail(managerDetails.getProjectManagerEmail());
							projects.setProjectManagerStartDate(managerDetails.getStartDate());
							projects.setProjectManagerCreatedOn(managerDetails.getCreatedOn());
							projects.setBuId(Constant.SAASPE);
							projects.setOpId(Constant.SAASPE);
							projects.setProjectStatus(projectDetails.getEndDate().before(new Date())? false:true);
							projectRepository.save(projects);
						}

						for (SingleProjectApplicationOnboarding applicationStatus : requestObject
								.getApplicationsInfo()) {
							ProjectApplicationStatus projectApplicationStatus = new ProjectApplicationStatus();
							projectApplicationStatus.setApplicationName(applicationStatus.getApplicationName());
							projectApplicationStatus.setApplicationStatus(applicationStatus.getApplicationCategory());
							projectApplicationStatus.setBuID(Constant.SAASPE);
							projectApplicationStatus.setCreatedBy(profile.getEmailAddress());
							projectApplicationStatus.setCreatedOn(new Date());
							projectApplicationStatus.setEndDate(requestObject.getProjectEndDate());
							projectApplicationStatus.setProjectId(name);
							projectApplicationStatus.setProjectName(childRequest.getProjectName());
							projectApplicationStatus.setStartDate(requestObject.getProjectStartDate());
							projectApplicationStatusRepository.save(projectApplicationStatus);
						}

						sequenceGeneratorRepository.save(updateSequence);
						projectDetailsRepository.save(projectDetails);
						projectOnboardingDetailsRepository.save(childRequest);
					}
					if (requestId != null && childRequestId == null) {
						ProjectOnboardingDetails parentRequest = projectOnboardingDetailsRepository
								.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
						if (parentRequest == null) {
							throw new DataValidationException("Provide Valid Id", requestId, HttpStatus.CONFLICT);
						}
						parentRequest.setWorkGroupUserEmail(profile.getEmailAddress());
						parentRequest.setApprovedRejected(onboardingWorkFlowRequest.getAction());
						parentRequest.setComments(onboardingWorkFlowRequest.getComments());
						parentRequest.setWorkGroup(Constant.APPROVER);
						parentRequest.setOnboardingStatus("Approved By Approver");
						parentRequest.setEndDate(new Date());
						parentRequest.setUpdatedBy(profile.getFirstName());
						parentRequest.setUpdatedOn(new Date());
						projectOnboardingDetailsRepository.save(parentRequest);

						String name = Constant.PROJECT_ID;
						Integer sequence = sequenceGeneratorRepository.getProjectSequence();
						name = name.concat(sequence.toString());

						SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
						updateSequence.setProjectSequenceId(++sequence);

						SingleProjectOnboardingRequest requestObject = applicationObjectDeserializer(
								parentRequest.getProjectOnboardingRequest());
						DepartmentDetails departmentDetails = departmentRepository
								.findByDepartmentId(requestObject.getDepartmentId());
						ProjectDetails projectDetails = new ProjectDetails();
						projectDetails.setBuID(Constant.SAASPE);
						projectDetails.setCreatedBy(profile.getEmailAddress());
						projectDetails.setCreatedOn(new Date());
						projectDetails.setDepartmentId(departmentDetails);
						projectDetails.setProjectBudget(requestObject.getProjectBudget());
						projectDetails.setProjectDescription(requestObject.getProjectDescription());
						projectDetails.setBudgetCurrency(requestObject.getCurrency());
						projectDetails.setProjectId(name);
						projectDetails.setProjectName(parentRequest.getProjectName());
						projectDetails.setProjectCode(parentRequest.getProjectCode());
						projectDetails.setStartDate(requestObject.getProjectStartDate());
						projectDetails.setEndDate(requestObject.getProjectEndDate());
						projectDetails.setBudgetCurrency(requestObject.getCurrency());

						Integer i = 1;
						for (String managerEmail : requestObject.getProjectManagerEmail()) {
							ProjectManagerDetails managerDetails = new ProjectManagerDetails();
							managerDetails.setCreatedOn(new Date());
							managerDetails.setProjectId(name);
							managerDetails.setProjectManagerEmail(managerEmail);
							managerDetails.setPriority(i);
							managerDetails.setProjectName(parentRequest.getProjectName());
							managerDetails.setStartDate(new Date());
							ownerRepository.save(managerDetails);
							i++;
							Projects projects = new Projects();
							projects.setProjectId(name);
							projects.setProjectName(requestObject.getProjectName());
							projects.setBudget(requestObject.getProjectBudget());
							projects.setBudgetCurrency(requestObject.getCurrency());
							projects.setDescription(requestObject.getProjectDescription());
							projects.setCreatedBy(profile.getEmailAddress());
							projects.setProjectCreatedOn(new Date());
							projects.setProjectStartDate(requestObject.getProjectStartDate());
							projects.setProjectEndDate(requestObject.getProjectEndDate());
							projects.setProjectCode(requestObject.getProjectCode());
							projects.setPriority(managerDetails.getPriority());
							projects.setProjectManagerEmail(managerDetails.getProjectManagerEmail());
							projects.setProjectManagerStartDate(managerDetails.getStartDate());
							projects.setProjectManagerCreatedOn(managerDetails.getCreatedOn());
							projects.setBuId(Constant.SAASPE);
							projects.setOpId(Constant.SAASPE);
							projects.setProjectStatus(projectDetails.getEndDate().before(new Date())? false:true);
							projectRepository.save(projects);
						}

						for (SingleProjectApplicationOnboarding applicationStatus : requestObject
								.getApplicationsInfo()) {
							ProjectApplicationStatus projectApplicationStatus = new ProjectApplicationStatus();
							projectApplicationStatus.setApplicationName(applicationStatus.getApplicationName());
							projectApplicationStatus.setApplicationStatus(applicationStatus.getApplicationCategory());
							projectApplicationStatus.setBuID(Constant.SAASPE);
							projectApplicationStatus.setCreatedBy(profile.getEmailAddress());
							projectApplicationStatus.setCreatedOn(new Date());
							projectApplicationStatus.setEndDate(requestObject.getProjectEndDate());
							projectApplicationStatus.setProjectId(name);
							projectApplicationStatus.setProjectName(parentRequest.getProjectName());
							projectApplicationStatus.setProjectCode(parentRequest.getProjectCode());
							projectApplicationStatus.setStartDate(requestObject.getProjectStartDate());
							projectApplicationStatusRepository.save(projectApplicationStatus);
						}

						sequenceGeneratorRepository.save(updateSequence);
						projectDetailsRepository.save(projectDetails);
						projectOnboardingDetailsRepository.save(parentRequest);
						return reviewSuccessResponse();
					}
					return reviewSuccessResponse();
				}
			} else {
				if (requestId != null) {
					ProjectOnboardingDetails rejectRequest = projectOnboardingDetailsRepository
							.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
					if (rejectRequest == null) {
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
					}
					rejectRequest.setOnboardingStatus("Rejected by Approver");
					rejectRequest.setApprovedRejected(Constant.REJECTED);
					rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
					projectOnboardingDetailsRepository.save(rejectRequest);
					return reviewFailureResponse();

				} else {
					ProjectOnboardingDetails rejectRequest = projectOnboardingDetailsRepository
							.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
					if (rejectRequest == null) {
						throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
					}
					rejectRequest.setApprovedRejected(Constant.REJECTED);
					rejectRequest.setOnboardingStatus("Rejected by Approver");
					rejectRequest.setComments(onboardingWorkFlowRequest.getComments());
					projectOnboardingDetailsRepository.save(rejectRequest);
					return reviewFailureResponse();
				}
			}
		}
		return commonResponse;
	}

	private CommonResponse reviewSuccessResponse() {
		return new CommonResponse(HttpStatus.OK,
				new Response("OnboardingWorkflowActionResponse", "Approved Successfully"), "Workflow action completed");
	}

	private CommonResponse reviewFailureResponse() {
		return new CommonResponse(HttpStatus.OK, new Response("OnboardingWorkflowActionResponse", "Workflow rejected"),
				"Workflow action completed");
	}

	private void superAdminsaveData(String requestId, String childRequestId, UserLoginDetails profile,
			ProjectWorkflowReviewRequest onboardingWorkFlowRequest)
			throws DataValidationException, JsonGenerationException {
		ProjectDetails projectDetails = new ProjectDetails();
		if (requestId == null && childRequestId != null) {
			ProjectOnboardingDetails superAdminRequest = projectOnboardingDetailsRepository
					.findAllBySuperAdmin(childRequestId);
			if (superAdminRequest == null) {
				throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
			}
			if (superAdminRequest.getWorkGroup().equalsIgnoreCase(Constant.REVIEWER)) {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setWorkGroup(Constant.SUPERADMIN);
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setUpdatedOn(new Date());

				String name = Constant.PROJECT_ID;
				Integer sequence = sequenceGeneratorRepository.getProjectSequence();
				name = name.concat(sequence.toString());
				SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
				updateSequence.setProjectSequenceId(++sequence);
				SingleProjectOnboardingRequest requestObject = applicationObjectDeserializer(
						superAdminRequest.getProjectOnboardingRequest());
				DepartmentDetails departmentDetails = departmentRepository
						.findByDepartmentId(requestObject.getDepartmentId());
				projectDetails.setBuID(Constant.SAASPE);
				projectDetails.setCreatedBy(profile.getEmailAddress());
				projectDetails.setCreatedOn(new Date());
				projectDetails.setDepartmentId(departmentDetails);
				projectDetails.setProjectBudget(requestObject.getProjectBudget());
				projectDetails.setProjectDescription(requestObject.getProjectDescription());
				projectDetails.setProjectId(name);
				projectDetails.setBudgetCurrency(requestObject.getCurrency());
				projectDetails.setProjectName(superAdminRequest.getProjectName());
				projectDetails.setProjectCode(superAdminRequest.getProjectCode());
				projectDetails.setStartDate(requestObject.getProjectStartDate());
				projectDetails.setEndDate(requestObject.getProjectEndDate());
				projectDetails.setBudgetCurrency(requestObject.getCurrency());

				Integer i = 1;
				for (String managerEmail : requestObject.getProjectManagerEmail()) {
					ProjectManagerDetails managerDetails = new ProjectManagerDetails();
					managerDetails.setCreatedOn(new Date());
					managerDetails.setProjectId(name);
					managerDetails.setProjectManagerEmail(managerEmail);
					managerDetails.setPriority(i);
					managerDetails.setProjectName(superAdminRequest.getProjectName());
					managerDetails.setStartDate(new Date());
					ownerRepository.save(managerDetails);
					i++;
					Projects projects = new Projects();
					projects.setProjectId(name);
					projects.setProjectName(requestObject.getProjectName());
					projects.setBudget(requestObject.getProjectBudget());
					projects.setBudgetCurrency(requestObject.getCurrency());
					projects.setDescription(requestObject.getProjectDescription());
					projects.setCreatedBy(profile.getEmailAddress());
					projects.setProjectCreatedOn(new Date());
					projects.setProjectStartDate(requestObject.getProjectStartDate());
					projects.setProjectEndDate(requestObject.getProjectEndDate());
					projects.setProjectCode(requestObject.getProjectCode());
					projects.setPriority(managerDetails.getPriority());
					projects.setProjectManagerEmail(managerDetails.getProjectManagerEmail());
					projects.setProjectManagerStartDate(managerDetails.getStartDate());
					projects.setProjectManagerCreatedOn(managerDetails.getCreatedOn());
					projects.setBuId(Constant.SAASPE);
					projects.setOpId(Constant.SAASPE);
					projects.setProjectStatus(projectDetails.getEndDate().before(new Date())? false:true);
					projectRepository.save(projects);
				}

				for (SingleProjectApplicationOnboarding applicationStatus : requestObject.getApplicationsInfo()) {
					ProjectApplicationStatus projectApplicationStatus = new ProjectApplicationStatus();
					projectApplicationStatus.setApplicationName(applicationStatus.getApplicationName());
					projectApplicationStatus.setApplicationStatus(applicationStatus.getApplicationCategory());
					projectApplicationStatus.setBuID(Constant.SAASPE);
					projectApplicationStatus.setCreatedBy(profile.getEmailAddress());
					projectApplicationStatus.setCreatedOn(new Date());
					projectApplicationStatus.setEndDate(requestObject.getProjectEndDate());
					projectApplicationStatus.setProjectId(name);
					projectApplicationStatus.setProjectName(superAdminRequest.getProjectName());
					projectApplicationStatus.setProjectCode(superAdminRequest.getProjectCode());
					projectApplicationStatus.setStartDate(requestObject.getProjectStartDate());
					projectApplicationStatusRepository.save(projectApplicationStatus);
				}

				sequenceGeneratorRepository.save(updateSequence);
				projectDetailsRepository.save(projectDetails);
				projectOnboardingDetailsRepository.save(superAdminRequest);
			} else {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setOnboardingStatus("Approved By SuperAdmin");
				superAdminRequest.setWorkGroup(Constant.SUPERADMIN);
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setOnboardDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setUpdatedOn(new Date());
				projectOnboardingDetailsRepository.save(superAdminRequest);
				String name = Constant.PROJECT_ID;
				Integer sequence = sequenceGeneratorRepository.getProjectSequence();
				name = name.concat(sequence.toString());

				SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
				updateSequence.setProjectSequenceId(++sequence);

				SingleProjectOnboardingRequest requestObject = applicationObjectDeserializer(
						superAdminRequest.getProjectOnboardingRequest());
				DepartmentDetails departmentDetails = departmentRepository
						.findByDepartmentId(requestObject.getDepartmentId());
				projectDetails.setBuID(Constant.SAASPE);
				projectDetails.setCreatedBy(profile.getEmailAddress());
				projectDetails.setCreatedOn(new Date());
				projectDetails.setDepartmentId(departmentDetails);
				projectDetails.setProjectBudget(requestObject.getProjectBudget());
				projectDetails.setProjectDescription(requestObject.getProjectDescription());
				projectDetails.setBudgetCurrency(requestObject.getCurrency());
				projectDetails.setProjectId(name);
				projectDetails.setProjectName(superAdminRequest.getProjectName());
				projectDetails.setProjectCode(superAdminRequest.getProjectCode());
				projectDetails.setStartDate(requestObject.getProjectStartDate());
				projectDetails.setEndDate(requestObject.getProjectEndDate());
				projectDetails.setBudgetCurrency(requestObject.getCurrency());

				Integer i = 1;
				for (String managerEmail : requestObject.getProjectManagerEmail()) {
					ProjectManagerDetails managerDetails = new ProjectManagerDetails();
					managerDetails.setCreatedOn(new Date());
					managerDetails.setProjectId(name);
					managerDetails.setProjectManagerEmail(managerEmail);
					managerDetails.setPriority(i);
					managerDetails.setProjectName(superAdminRequest.getProjectName());
					managerDetails.setStartDate(new Date());
					ownerRepository.save(managerDetails);
					i++;
					Projects projects = new Projects();
					projects.setProjectId(name);
					projects.setProjectName(requestObject.getProjectName());
					projects.setBudget(requestObject.getProjectBudget());
					projects.setBudgetCurrency(requestObject.getCurrency());
					projects.setDescription(requestObject.getProjectDescription());
					projects.setCreatedBy(profile.getEmailAddress());
					projects.setProjectCreatedOn(new Date());
					projects.setProjectStartDate(requestObject.getProjectStartDate());
					projects.setProjectEndDate(requestObject.getProjectEndDate());
					projects.setProjectCode(requestObject.getProjectCode());
					projects.setPriority(managerDetails.getPriority());
					projects.setProjectManagerEmail(managerDetails.getProjectManagerEmail());
					projects.setProjectManagerStartDate(managerDetails.getStartDate());
					projects.setProjectManagerCreatedOn(managerDetails.getCreatedOn());
					projects.setBuId(Constant.SAASPE);
					projects.setOpId(Constant.SAASPE);
					projects.setProjectStatus(projectDetails.getEndDate().before(new Date())? false:true);
					projectRepository.save(projects);
				}

				for (SingleProjectApplicationOnboarding applicationStatus : requestObject.getApplicationsInfo()) {
					ProjectApplicationStatus projectApplicationStatus = new ProjectApplicationStatus();
					projectApplicationStatus.setApplicationName(applicationStatus.getApplicationName());
					projectApplicationStatus.setApplicationStatus(applicationStatus.getApplicationCategory());
					projectApplicationStatus.setBuID(Constant.SAASPE);
					projectApplicationStatus.setCreatedBy(profile.getEmailAddress());
					projectApplicationStatus.setCreatedOn(new Date());
					projectApplicationStatus.setEndDate(requestObject.getProjectEndDate());
					projectApplicationStatus.setProjectId(name);
					projectApplicationStatus.setProjectName(superAdminRequest.getProjectName());
					projectApplicationStatus.setProjectCode(superAdminRequest.getProjectCode());
					projectApplicationStatus.setStartDate(requestObject.getProjectStartDate());
					projectApplicationStatusRepository.save(projectApplicationStatus);
				}

				sequenceGeneratorRepository.save(updateSequence);
				projectDetailsRepository.save(projectDetails);
				projectOnboardingDetailsRepository.save(superAdminRequest);
			}
		}
		if (requestId != null && childRequestId == null) {
			ProjectOnboardingDetails superAdminRequest = projectOnboardingDetailsRepository
					.findAllBySuperAdminRequestId(requestId);
			if (superAdminRequest == null) {
				throw new DataValidationException(Constant.PROVIDE_VALID_VALUE, requestId, HttpStatus.CONFLICT);
			}
			if (superAdminRequest.getWorkGroup().equalsIgnoreCase(Constant.REVIEWER)) {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setWorkGroup(Constant.SUPERADMIN);
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setUpdatedOn(new Date());
				String name = Constant.PROJECT_ID;
				Integer sequence = sequenceGeneratorRepository.getProjectSequence();
				name = name.concat(sequence.toString());

				SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
				updateSequence.setProjectSequenceId(++sequence);

				SingleProjectOnboardingRequest requestObject = applicationObjectDeserializer(
						superAdminRequest.getProjectOnboardingRequest());
				DepartmentDetails departmentDetails = departmentRepository
						.findByDepartmentId(requestObject.getDepartmentId());
				projectDetails.setBuID(Constant.SAASPE);
				projectDetails.setCreatedBy(profile.getEmailAddress());
				projectDetails.setCreatedOn(new Date());
				projectDetails.setDepartmentId(departmentDetails);
				projectDetails.setProjectBudget(requestObject.getProjectBudget());
				projectDetails.setProjectDescription(requestObject.getProjectDescription());
				projectDetails.setBudgetCurrency(requestObject.getCurrency());
				projectDetails.setProjectId(name);
				projectDetails.setProjectName(superAdminRequest.getProjectName());
				projectDetails.setProjectCode(superAdminRequest.getProjectCode());
				projectDetails.setStartDate(requestObject.getProjectStartDate());
				projectDetails.setEndDate(requestObject.getProjectEndDate());
				projectDetails.setBudgetCurrency(requestObject.getCurrency());

				Integer i = 1;
				for (String managerEmail : requestObject.getProjectManagerEmail()) {
					ProjectManagerDetails managerDetails = new ProjectManagerDetails();
					managerDetails.setCreatedOn(new Date());
					managerDetails.setProjectId(name);
					managerDetails.setProjectManagerEmail(managerEmail);
					managerDetails.setPriority(i);
					managerDetails.setProjectName(superAdminRequest.getProjectName());
					managerDetails.setStartDate(new Date());
					ownerRepository.save(managerDetails);
					i++;
					Projects projects = new Projects();
					projects.setProjectId(name);
					projects.setProjectName(requestObject.getProjectName());
					projects.setBudget(requestObject.getProjectBudget());
					projects.setBudgetCurrency(requestObject.getCurrency());
					projects.setDescription(requestObject.getProjectDescription());
					projects.setCreatedBy(profile.getEmailAddress());
					projects.setProjectCreatedOn(new Date());
					projects.setProjectStartDate(requestObject.getProjectStartDate());
					projects.setProjectEndDate(requestObject.getProjectEndDate());
					projects.setProjectCode(requestObject.getProjectCode());
					projects.setPriority(managerDetails.getPriority());
					projects.setProjectManagerEmail(managerDetails.getProjectManagerEmail());
					projects.setProjectManagerStartDate(managerDetails.getStartDate());
					projects.setProjectManagerCreatedOn(managerDetails.getCreatedOn());
					projects.setBuId(Constant.SAASPE);
					projects.setOpId(Constant.SAASPE);
					projects.setProjectStatus(projectDetails.getEndDate().before(new Date())? false:true);
					projectRepository.save(projects);
				}

				for (SingleProjectApplicationOnboarding applicationStatus : requestObject.getApplicationsInfo()) {
					ProjectApplicationStatus projectApplicationStatus = new ProjectApplicationStatus();
					projectApplicationStatus.setApplicationName(applicationStatus.getApplicationName());
					projectApplicationStatus.setApplicationStatus(applicationStatus.getApplicationCategory());
					projectApplicationStatus.setBuID(Constant.SAASPE);
					projectApplicationStatus.setCreatedBy(profile.getEmailAddress());
					projectApplicationStatus.setCreatedOn(new Date());
					projectApplicationStatus.setEndDate(requestObject.getProjectEndDate());
					projectApplicationStatus.setProjectId(name);
					projectApplicationStatus.setProjectName(superAdminRequest.getProjectName());
					projectApplicationStatus.setProjectCode(superAdminRequest.getProjectCode());
					projectApplicationStatus.setStartDate(requestObject.getProjectStartDate());
					projectApplicationStatusRepository.save(projectApplicationStatus);
				}

				sequenceGeneratorRepository.save(updateSequence);
				projectDetailsRepository.save(projectDetails);
				projectOnboardingDetailsRepository.save(superAdminRequest);
			} else {
				superAdminRequest.setWorkGroupUserEmail(profile.getEmailAddress());
				superAdminRequest.setApprovedRejected(Constant.APPROVE);
				superAdminRequest.setOnboardingStatus(Constant.APPROVED_BY_SUPERADMIN);
				superAdminRequest.setWorkGroup(Constant.SUPERADMIN);
				superAdminRequest.setComments(onboardingWorkFlowRequest.getComments());
				superAdminRequest.setEndDate(new Date());
				superAdminRequest.setUpdatedBy(profile.getFirstName());
				superAdminRequest.setOnboardDate(new Date());
				superAdminRequest.setUpdatedOn(new Date());
				String name = Constant.PROJECT_ID;
				Integer sequence = sequenceGeneratorRepository.getProjectSequence();
				name = name.concat(sequence.toString());
				SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
				updateSequence.setProjectSequenceId(++sequence);
				SingleProjectOnboardingRequest requestObject = applicationObjectDeserializer(
						superAdminRequest.getProjectOnboardingRequest());
				DepartmentDetails departmentDetails = departmentRepository
						.findByDepartmentId(requestObject.getDepartmentId());
				projectDetails.setBuID(Constant.SAASPE);
				projectDetails.setCreatedBy(profile.getEmailAddress());
				projectDetails.setCreatedOn(new Date());
				projectDetails.setDepartmentId(departmentDetails);
				projectDetails.setProjectBudget(requestObject.getProjectBudget());
				projectDetails.setProjectDescription(requestObject.getProjectDescription());
				projectDetails.setBudgetCurrency(requestObject.getCurrency());
				projectDetails.setProjectId(name);
				projectDetails.setProjectName(superAdminRequest.getProjectName());
				projectDetails.setProjectCode(superAdminRequest.getProjectCode());
				projectDetails.setStartDate(requestObject.getProjectStartDate());
				projectDetails.setEndDate(requestObject.getProjectEndDate());
				projectDetails.setBudgetCurrency(requestObject.getCurrency());

				Integer i = 1;
				for (String managerEmail : requestObject.getProjectManagerEmail()) {
					ProjectManagerDetails managerDetails = new ProjectManagerDetails();
					managerDetails.setCreatedOn(new Date());
					managerDetails.setProjectId(name);
					managerDetails.setProjectManagerEmail(managerEmail);
					managerDetails.setPriority(i);
					managerDetails.setProjectName(superAdminRequest.getProjectName());
					managerDetails.setStartDate(new Date());
					ownerRepository.save(managerDetails);
					i++;
					Projects projects = new Projects();
					projects.setProjectId(name);
					projects.setProjectName(requestObject.getProjectName());
					projects.setBudget(requestObject.getProjectBudget());
					projects.setBudgetCurrency(requestObject.getCurrency());
					projects.setDescription(requestObject.getProjectDescription());
					projects.setCreatedBy(profile.getEmailAddress());
					projects.setProjectCreatedOn(new Date());
					projects.setProjectStartDate(requestObject.getProjectStartDate());
					projects.setProjectEndDate(requestObject.getProjectEndDate());
					projects.setProjectCode(requestObject.getProjectCode());
					projects.setPriority(managerDetails.getPriority());
					projects.setProjectManagerEmail(managerDetails.getProjectManagerEmail());
					projects.setProjectManagerStartDate(managerDetails.getStartDate());
					projects.setProjectManagerCreatedOn(managerDetails.getCreatedOn());
					projects.setBuId(Constant.SAASPE);
					projects.setOpId(Constant.SAASPE);
					projects.setProjectStatus(projectDetails.getEndDate().before(new Date())? false:true);
					projectRepository.save(projects);
				}
				for (SingleProjectApplicationOnboarding applicationStatus : requestObject.getApplicationsInfo()) {
					ProjectApplicationStatus projectApplicationStatus = new ProjectApplicationStatus();
					projectApplicationStatus.setApplicationName(applicationStatus.getApplicationName());
					projectApplicationStatus.setApplicationStatus(applicationStatus.getApplicationCategory());
					projectApplicationStatus.setBuID(Constant.SAASPE);
					projectApplicationStatus.setCreatedBy(profile.getEmailAddress());
					projectApplicationStatus.setCreatedOn(new Date());
					projectApplicationStatus.setEndDate(requestObject.getProjectEndDate());
					projectApplicationStatus.setProjectId(name);
					projectApplicationStatus.setProjectName(superAdminRequest.getProjectName());
					projectApplicationStatus.setProjectCode(superAdminRequest.getProjectCode());
					projectApplicationStatus.setStartDate(requestObject.getProjectStartDate());
					projectApplicationStatusRepository.save(projectApplicationStatus);
				}

				sequenceGeneratorRepository.save(updateSequence);
				projectDetailsRepository.save(projectDetails);
				projectOnboardingDetailsRepository.save(superAdminRequest);
			}
		}
	}

	private SingleProjectOnboardingRequest applicationObjectDeserializer(String applicationOnboardingRequest)
			throws JsonGenerationException {
		ObjectMapper obj = new ObjectMapper();
		SingleProjectOnboardingRequest status = new SingleProjectOnboardingRequest();
		try {
			status = obj.readValue(applicationOnboardingRequest, SingleProjectOnboardingRequest.class);
		} catch (JsonProcessingException e) {
			throw new JsonGenerationException(e, null);
		}
		return status;
	}

	@Override
	public CommonResponse projectReviewerApproverListView(UserLoginDetails profile) throws JsonGenerationException {
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
			List<ProjectWorkflowListViewResponse> listOfDept = getListofProjects(Constant.REVIEWER, Constant.REVIEW);
			response.setData(listOfDept);
			response.setAction("Reviewer List view Response");
			commonResponse.setStatus(HttpStatus.OK);
			commonResponse.setMessage(Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
			commonResponse.setResponse(response);
			return commonResponse;
		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER)) {
			List<ProjectWorkflowListViewResponse> listOfDept = getListofProjects(Constant.APPROVER, Constant.REVIEW);
			response.setData(listOfDept);
			response.setAction("Approver List View Response");
			commonResponse.setStatus(HttpStatus.OK);
			commonResponse.setMessage(Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
			commonResponse.setResponse(response);
			return commonResponse;
		}
		List<ProjectOnboardingDetails> projectOnboarding = projectOnboardingDetailsRepository
				.findAllBySuperAdminListView();
		List<ProjectWorkflowListViewResponse> list = new ArrayList<>();
		for (ProjectOnboardingDetails project : projectOnboarding) {
			ProjectWorkflowListViewResponse listViewResponse = new ProjectWorkflowListViewResponse();
			SingleProjectOnboardingRequest deserializedObject = applicationObjectDeserializer(
					project.getProjectOnboardingRequest());
			listViewResponse.setProjectName(project.getProjectName());
			listViewResponse.setProjectCode(project.getProjectCode());
			listViewResponse.setProjectManagerEmail(deserializedObject.getProjectManagerEmail());
			listViewResponse.setOnboardedByEmail(project.getOnboardedByUserEmail());
			listViewResponse.setRequestId(project.getRequestNumber());
			listViewResponse.setOnboardingStatus(project.getOnboardingStatus());
			listViewResponse.setChildRequestId(project.getChildRequestNumber());
			if (project.getChildRequestNumber() != null) {
				listViewResponse.setChildRequestId(project.getChildRequestNumber());
			}
			list.add(listViewResponse);
		}
		return new CommonResponse(HttpStatus.OK, new Response("ProjectListView", list),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	private List<ProjectWorkflowListViewResponse> getListofProjects(String role, String key)
			throws JsonGenerationException {
		List<ProjectOnboardingDetails> projectOnboarding = projectOnboardingDetailsRepository.getAllByName(role, key);
		List<ProjectWorkflowListViewResponse> list = new ArrayList<>();
		for (ProjectOnboardingDetails projectonboarding : projectOnboarding) {
			ProjectWorkflowListViewResponse listViewResponse = new ProjectWorkflowListViewResponse();
			SingleProjectOnboardingRequest deserializedObject = applicationObjectDeserializer(
					projectonboarding.getProjectOnboardingRequest());
			listViewResponse.setProjectName(projectonboarding.getProjectName());
			listViewResponse.setProjectCode(projectonboarding.getProjectCode());
			listViewResponse.setProjectManagerEmail(deserializedObject.getProjectManagerEmail());
			listViewResponse.setOnboardedByEmail(projectonboarding.getOnboardedByUserEmail());
			listViewResponse.setRequestId(projectonboarding.getRequestNumber());
			if (role.equalsIgnoreCase(Constant.APPROVER)) {
				listViewResponse.setReviewedByEmail(projectonboarding.getWorkGroupUserEmail());
			}
			if (projectonboarding.getChildRequestNumber() != null) {
				listViewResponse.setChildRequestId(projectonboarding.getChildRequestNumber());
			}
			list.add(listViewResponse);
		}
		return list;
	}

	@Override
	public CommonResponse projectReviewerApproverDetailsView(String childRequestId, String requestId,
			UserLoginDetails profile) throws JsonGenerationException, DataValidationException {
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		ProjectWorkflowDetailsView detailViewResponse = new ProjectWorkflowDetailsView();
		SingleProjectOnboardingRequest projectDetails = new SingleProjectOnboardingRequest();
		ProjectWorkflowReviewerDetailsview reviewerDetails = new ProjectWorkflowReviewerDetailsview();
		List<SingleProjectApplicationOnboarding> list = new ArrayList<>();
		ProjectOnboardingDetails projectDetail = projectOnboardingDetailsRepository.findByRequest(requestId,
				childRequestId);
		if ((projectDetail.getApprovedRejected().equalsIgnoreCase(Constant.APPROVE)
				|| projectDetail.getApprovedRejected().equalsIgnoreCase(Constant.REJECTED))
				|| (projectDetail.getApprovedRejected().equalsIgnoreCase(Constant.REVIEW)
						&& projectDetail.getWorkGroup().equalsIgnoreCase(Constant.APPROVER)
						&& (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)))) {
			throw new DataValidationException("Onboarding flow for the requested project is completed already", null, HttpStatus.NO_CONTENT);
		}

		if (userDetails.getUserRole().equalsIgnoreCase(Constant.REVIEWER)) {
			if (requestId != null && childRequestId == null) {
				ProjectOnboardingDetails projectReviewer = projectOnboardingDetailsRepository
						.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
				SingleProjectOnboardingRequest projectRequestObject = applicationObjectDeserializer(
						projectReviewer.getProjectOnboardingRequest());
				DepartmentDetails departmentDetails = departmentRepository
						.findByDepartmentId(projectRequestObject.getDepartmentId());
				projectDetails.setProjectName(projectRequestObject.getProjectName());
				projectDetails.setProjectCode(projectRequestObject.getProjectCode());
				projectDetails.setProjectDescription(projectRequestObject.getProjectDescription());
				projectDetails.setProjectStartDate(projectRequestObject.getProjectStartDate());
				projectDetails.setProjectEndDate(projectRequestObject.getProjectEndDate());
				projectDetails.setProjectBudget(projectRequestObject.getProjectBudget());
				projectDetails.setCurrency(projectRequestObject.getCurrency());
				projectDetails.setProjectManagerEmail(projectRequestObject.getProjectManagerEmail());
				projectDetails.setProjectDepartmentName(departmentDetails.getDepartmentName());
				projectDetails.setApplicationCount(projectRequestObject.getApplicationsInfo().size());
				for (SingleProjectApplicationOnboarding applicationStatus : projectRequestObject
						.getApplicationsInfo()) {
					SingleProjectApplicationOnboarding applicationOnboarding = new SingleProjectApplicationOnboarding();
					ApplicationLogoEntity entity = applicationLogoRepository
							.findByApplicationName(applicationStatus.getApplicationName());
					applicationOnboarding.setApplicationLogo(entity.getLogoUrl());
					applicationOnboarding.setApplicationName(applicationStatus.getApplicationName());
					list.add(applicationOnboarding);
				}
				projectDetails.setApplicationsInfo(list);
				detailViewResponse.setProjectDetailsInfo(projectDetails);
			}
			if (requestId == null && childRequestId != null) {
				ProjectOnboardingDetails projectReviewer = projectOnboardingDetailsRepository
						.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);
				SingleProjectOnboardingRequest projectRequestObject = applicationObjectDeserializer(
						projectReviewer.getProjectOnboardingRequest());
				DepartmentDetails departmentDetails = departmentRepository
						.findByDepartmentId(projectRequestObject.getDepartmentId());
				projectDetails.setProjectName(projectRequestObject.getProjectName());
				projectDetails.setProjectCode(projectRequestObject.getProjectCode());
				projectDetails.setProjectDescription(projectRequestObject.getProjectDescription());
				projectDetails.setProjectStartDate(projectRequestObject.getProjectStartDate());
				projectDetails.setProjectEndDate(projectRequestObject.getProjectEndDate());
				projectDetails.setProjectBudget(projectRequestObject.getProjectBudget());
				projectDetails.setCurrency(projectRequestObject.getCurrency());
				projectDetails.setProjectManagerEmail(projectRequestObject.getProjectManagerEmail());
				projectDetails.setProjectDepartmentName(departmentDetails.getDepartmentName());
				projectDetails.setApplicationCount(projectRequestObject.getApplicationsInfo().size());
				for (SingleProjectApplicationOnboarding applicationStatus : projectRequestObject
						.getApplicationsInfo()) {
					SingleProjectApplicationOnboarding applicationOnboarding = new SingleProjectApplicationOnboarding();
					ApplicationLogoEntity entity = applicationLogoRepository
							.findByApplicationName(applicationStatus.getApplicationName());
					applicationOnboarding.setApplicationLogo(entity.getLogoUrl());
					applicationOnboarding.setApplicationName(applicationStatus.getApplicationName());
					list.add(applicationOnboarding);
				}
				projectDetails.setApplicationsInfo(list);
				detailViewResponse.setProjectDetailsInfo(projectDetails);
			}

		}
		if (userDetails.getUserRole().equalsIgnoreCase(Constant.APPROVER)) {
			if (requestId != null && childRequestId == null) {
				ProjectOnboardingDetails projectApprover = projectOnboardingDetailsRepository
						.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
				ProjectOnboardingDetails projectReviewer = projectOnboardingDetailsRepository
						.findByRequestNumber(requestId, Constant.REVIEWER, Constant.APPROVE);
				SingleProjectOnboardingRequest projectRequestObject = applicationObjectDeserializer(
						projectApprover.getProjectOnboardingRequest());
				DepartmentDetails departmentDetails = departmentRepository
						.findByDepartmentId(projectRequestObject.getDepartmentId());
				projectDetails.setProjectName(projectRequestObject.getProjectName());
				projectDetails.setProjectCode(projectRequestObject.getProjectCode());
				projectDetails.setProjectDescription(projectRequestObject.getProjectDescription());
				projectDetails.setProjectStartDate(projectRequestObject.getProjectStartDate());
				projectDetails.setProjectEndDate(projectRequestObject.getProjectEndDate());
				projectDetails.setProjectBudget(projectRequestObject.getProjectBudget());
				projectDetails.setCurrency(projectRequestObject.getCurrency());
				projectDetails.setProjectDepartmentName(departmentDetails.getDepartmentName());
				projectDetails.setProjectManagerEmail(projectRequestObject.getProjectManagerEmail());
				projectDetails.setApplicationCount(projectRequestObject.getApplicationsInfo().size());
				for (SingleProjectApplicationOnboarding applicationStatus : projectRequestObject
						.getApplicationsInfo()) {
					SingleProjectApplicationOnboarding applicationOnboarding = new SingleProjectApplicationOnboarding();
					ApplicationLogoEntity entity = applicationLogoRepository
							.findByApplicationName(applicationStatus.getApplicationName());
					applicationOnboarding.setApplicationLogo(entity.getLogoUrl());
					applicationOnboarding.setApplicationName(applicationStatus.getApplicationName());
					list.add(applicationOnboarding);
				}
				projectDetails.setApplicationsInfo(list);
				reviewerDetails.setApprovedByEmail(projectReviewer.getWorkGroupUserEmail());
				reviewerDetails.setWorkGroupName(projectReviewer.getWorkGroup());
				reviewerDetails.setComments(projectReviewer.getComments());
				reviewerDetails.setApprovalTimeStamp(projectReviewer.getEndDate().toString());
				detailViewResponse.setProjectDetailsInfo(projectDetails);
				detailViewResponse.setReviewerDetails(reviewerDetails);
			}
			if (requestId == null && childRequestId != null) {

				ProjectOnboardingDetails projectApprover = projectOnboardingDetailsRepository
						.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
				ProjectOnboardingDetails projectReviewer = projectOnboardingDetailsRepository
						.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.APPROVE);
				SingleProjectOnboardingRequest projectRequestObject = applicationObjectDeserializer(
						projectApprover.getProjectOnboardingRequest());
				DepartmentDetails departmentDetails = departmentRepository
						.findByDepartmentId(projectRequestObject.getDepartmentId());
				projectDetails.setProjectName(projectRequestObject.getProjectName());
				projectDetails.setProjectCode(projectRequestObject.getProjectCode());
				projectDetails.setProjectDescription(projectRequestObject.getProjectDescription());
				projectDetails.setProjectStartDate(projectRequestObject.getProjectStartDate());
				projectDetails.setProjectEndDate(projectRequestObject.getProjectEndDate());
				projectDetails.setProjectBudget(projectRequestObject.getProjectBudget());
				projectDetails.setCurrency(projectRequestObject.getCurrency());
				projectDetails.setProjectManagerEmail(projectRequestObject.getProjectManagerEmail());
				projectDetails.setProjectDepartmentName(departmentDetails.getDepartmentName());
				projectDetails.setApplicationCount(projectRequestObject.getApplicationsInfo().size());
				for (SingleProjectApplicationOnboarding applicationStatus : projectRequestObject
						.getApplicationsInfo()) {
					SingleProjectApplicationOnboarding applicationOnboarding = new SingleProjectApplicationOnboarding();
					applicationOnboarding.setApplicationLogo(applicationStatus.getApplicationLogo());
					ApplicationLogoEntity entity = applicationLogoRepository
							.findByApplicationName(applicationStatus.getApplicationName());
					applicationOnboarding.setApplicationLogo(entity.getLogoUrl());
					applicationOnboarding.setApplicationName(applicationStatus.getApplicationName());
					list.add(applicationOnboarding);
				}
				projectDetails.setApplicationsInfo(list);
				reviewerDetails.setApprovedByEmail(projectReviewer.getWorkGroupUserEmail());
				reviewerDetails.setWorkGroupName(projectReviewer.getWorkGroup());
				reviewerDetails.setComments(projectReviewer.getComments());
				reviewerDetails.setApprovalTimeStamp(projectReviewer.getEndDate().toString());
				detailViewResponse.setProjectDetailsInfo(projectDetails);
				detailViewResponse.setReviewerDetails(reviewerDetails);
			}
		}
		if (userDetails.getUserRole().equalsIgnoreCase("super_admin")) {
			if (requestId != null && childRequestId == null) {
				ProjectOnboardingDetails projectApprover = projectOnboardingDetailsRepository
						.findByRequestNumber(requestId, Constant.APPROVER, Constant.REVIEW);
				ProjectOnboardingDetails projectReviewerApproved = projectOnboardingDetailsRepository
						.findByRequestNumber(requestId, Constant.REVIEWER, Constant.APPROVE);
				ProjectOnboardingDetails projectReviewer = projectOnboardingDetailsRepository
						.findByRequestNumber(requestId, Constant.REVIEWER, Constant.REVIEW);
				if (projectApprover != null) {
					SingleProjectOnboardingRequest projectRequestObject = applicationObjectDeserializer(
							projectApprover.getProjectOnboardingRequest());
					DepartmentDetails departmentDetails = departmentRepository
							.findByDepartmentId(projectRequestObject.getDepartmentId());
					projectDetails.setProjectName(projectRequestObject.getProjectName());
					projectDetails.setProjectCode(projectRequestObject.getProjectCode());
					projectDetails.setProjectDescription(projectRequestObject.getProjectDescription());
					projectDetails.setProjectStartDate(projectRequestObject.getProjectStartDate());
					projectDetails.setProjectEndDate(projectRequestObject.getProjectEndDate());
					projectDetails.setProjectBudget(projectRequestObject.getProjectBudget());
					projectDetails.setCurrency(projectRequestObject.getCurrency());
					projectDetails.setProjectManagerEmail(projectRequestObject.getProjectManagerEmail());
					projectDetails.setProjectDepartmentName(departmentDetails.getDepartmentName());
					projectDetails.setApplicationCount(projectRequestObject.getApplicationsInfo().size());
					for (SingleProjectApplicationOnboarding applicationStatus : projectRequestObject
							.getApplicationsInfo()) {
						SingleProjectApplicationOnboarding applicationOnboarding = new SingleProjectApplicationOnboarding();
						ApplicationLogoEntity entity = applicationLogoRepository
								.findByApplicationName(applicationStatus.getApplicationName());
						applicationOnboarding.setApplicationLogo(entity.getLogoUrl());
						applicationOnboarding.setApplicationName(applicationStatus.getApplicationName());
						list.add(applicationOnboarding);
					}
					projectDetails.setApplicationsInfo(list);
					reviewerDetails.setApprovedByEmail(projectReviewerApproved.getWorkGroupUserEmail());
					reviewerDetails.setWorkGroupName(projectReviewerApproved.getWorkGroup());
					reviewerDetails.setComments(projectReviewerApproved.getComments());
					reviewerDetails.setApprovalTimeStamp(projectReviewerApproved.getEndDate().toString());
					detailViewResponse.setProjectDetailsInfo(projectDetails);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				} else {
					SingleProjectOnboardingRequest projectRequestObject = applicationObjectDeserializer(
							projectReviewer.getProjectOnboardingRequest());
					DepartmentDetails departmentDetails = departmentRepository
							.findByDepartmentId(projectRequestObject.getDepartmentId());
					projectDetails.setProjectName(projectRequestObject.getProjectName());
					projectDetails.setProjectCode(projectRequestObject.getProjectCode());
					projectDetails.setProjectDescription(projectRequestObject.getProjectDescription());
					projectDetails.setProjectStartDate(projectRequestObject.getProjectStartDate());
					projectDetails.setProjectEndDate(projectRequestObject.getProjectEndDate());
					projectDetails.setProjectBudget(projectRequestObject.getProjectBudget());
					projectDetails.setCurrency(projectRequestObject.getCurrency());
					projectDetails.setProjectManagerEmail(projectRequestObject.getProjectManagerEmail());
					projectDetails.setProjectDepartmentName(departmentDetails.getDepartmentName());
					projectDetails.setApplicationCount(projectRequestObject.getApplicationsInfo().size());
					for (SingleProjectApplicationOnboarding applicationStatus : projectRequestObject
							.getApplicationsInfo()) {
						SingleProjectApplicationOnboarding applicationOnboarding = new SingleProjectApplicationOnboarding();
						ApplicationLogoEntity entity = applicationLogoRepository
								.findByApplicationName(applicationStatus.getApplicationName());
						applicationOnboarding.setApplicationLogo(entity.getLogoUrl());
						applicationOnboarding.setApplicationName(applicationStatus.getApplicationName());
						list.add(applicationOnboarding);
					}
					projectDetails.setApplicationsInfo(list);
					detailViewResponse.setProjectDetailsInfo(projectDetails);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				}

			}
			if (requestId == null && childRequestId != null) {
				ProjectOnboardingDetails projectApprover = projectOnboardingDetailsRepository
						.findByChildRequestNumber(childRequestId, Constant.APPROVER, Constant.REVIEW);
				ProjectOnboardingDetails projectReviewerApproved = projectOnboardingDetailsRepository
						.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.APPROVE);
				ProjectOnboardingDetails projectReviewer = projectOnboardingDetailsRepository
						.findByChildRequestNumber(childRequestId, Constant.REVIEWER, Constant.REVIEW);

				if (projectApprover != null) {
					SingleProjectOnboardingRequest projectRequestObject = applicationObjectDeserializer(
							projectApprover.getProjectOnboardingRequest());
					DepartmentDetails departmentDetails = departmentRepository
							.findByDepartmentId(projectRequestObject.getDepartmentId());
					projectDetails.setProjectName(projectRequestObject.getProjectName());
					projectDetails.setProjectCode(projectRequestObject.getProjectCode());
					projectDetails.setProjectDescription(projectRequestObject.getProjectDescription());
					projectDetails.setProjectStartDate(projectRequestObject.getProjectStartDate());
					projectDetails.setProjectEndDate(projectRequestObject.getProjectEndDate());
					projectDetails.setProjectBudget(projectRequestObject.getProjectBudget());
					projectDetails.setCurrency(projectRequestObject.getCurrency());
					projectDetails.setProjectManagerEmail(projectRequestObject.getProjectManagerEmail());
					projectDetails.setProjectDepartmentName(departmentDetails.getDepartmentName());
					projectDetails.setApplicationCount(projectRequestObject.getApplicationsInfo().size());
					for (SingleProjectApplicationOnboarding applicationStatus : projectRequestObject
							.getApplicationsInfo()) {
						SingleProjectApplicationOnboarding applicationOnboarding = new SingleProjectApplicationOnboarding();
						ApplicationLogoEntity entity = applicationLogoRepository
								.findByApplicationName(applicationStatus.getApplicationName());
						applicationOnboarding.setApplicationLogo(entity.getLogoUrl());
						applicationOnboarding.setApplicationName(applicationStatus.getApplicationName());
						list.add(applicationOnboarding);
					}
					projectDetails.setApplicationsInfo(list);
					reviewerDetails.setApprovedByEmail(projectReviewerApproved.getWorkGroupUserEmail());
					reviewerDetails.setWorkGroupName(projectReviewerApproved.getWorkGroup());
					reviewerDetails.setComments(projectReviewerApproved.getComments());
					reviewerDetails.setApprovalTimeStamp(projectReviewerApproved.getEndDate().toString());
					detailViewResponse.setProjectDetailsInfo(projectDetails);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				} else {
					SingleProjectOnboardingRequest projectRequestObject = applicationObjectDeserializer(
							projectReviewer.getProjectOnboardingRequest());
					DepartmentDetails departmentDetails = departmentRepository
							.findByDepartmentId(projectRequestObject.getDepartmentId());
					projectDetails.setProjectName(projectRequestObject.getProjectName());
					projectDetails.setProjectCode(projectRequestObject.getProjectCode());
					projectDetails.setProjectDescription(projectRequestObject.getProjectDescription());
					projectDetails.setProjectStartDate(projectRequestObject.getProjectStartDate());
					projectDetails.setProjectEndDate(projectRequestObject.getProjectEndDate());
					projectDetails.setProjectBudget(projectRequestObject.getProjectBudget());
					projectDetails.setCurrency(projectRequestObject.getCurrency());
					projectDetails.setProjectManagerEmail(projectRequestObject.getProjectManagerEmail());
					projectDetails.setProjectDepartmentName(departmentDetails.getDepartmentName());
					projectDetails.setApplicationCount(projectRequestObject.getApplicationsInfo().size());
					for (SingleProjectApplicationOnboarding applicationStatus : projectRequestObject
							.getApplicationsInfo()) {
						SingleProjectApplicationOnboarding applicationOnboarding = new SingleProjectApplicationOnboarding();
						ApplicationLogoEntity entity = applicationLogoRepository
								.findByApplicationName(applicationStatus.getApplicationName());
						applicationOnboarding.setApplicationLogo(entity.getLogoUrl());
						applicationOnboarding.setApplicationName(applicationStatus.getApplicationName());
						list.add(applicationOnboarding);
					}
					projectDetails.setApplicationsInfo(list);
					detailViewResponse.setProjectDetailsInfo(projectDetails);
					detailViewResponse.setReviewerDetails(reviewerDetails);
				}
			}
		}
		return new CommonResponse(HttpStatus.OK,
				new Response("departmentOnboardingRequestDetailViewResponse", detailViewResponse),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse projectDetailsView(String projectId, UserLoginDetails profile)
			throws DataValidationException {
		SingleProjectOnboardingRequest response = new SingleProjectOnboardingRequest();
		List<SingleProjectApplicationOnboarding> list = new ArrayList<>();
		List<ApplicationLogoEntity> applicationLogoEntity = applicationLogoRepository.findAll();
		ProjectDetails projectDetails = projectDetailsRepository.findByProjectId(projectId);
		if (projectDetails == null) {
			throw new DataValidationException("Project With" + projectId + " Doesn't Exist", null, null);
		}
		List<ProjectApplicationStatus> applicationStatus = projectApplicationStatusRepository
				.findByProjectId(projectId);
		List<ApplicationDetails> applicationDetails = applicationDetailsRepository
				.findByProejctName(projectDetails.getProjectName());

		List<ProjectManagerDetails> projectManagers = ownerRepository.findByProjectId(projectId);
		response.setApplicationCount(applicationDetails.size());
		response.setDepartmentId(projectDetails.getDepartmentId().getDepartmentId());
		response.setProjectDepartmentName(projectDetails.getDepartmentId().getDepartmentName());
		response.setProjectDescription(projectDetails.getProjectDescription());
		response.setProjectEndDate(projectDetails.getEndDate());
		List<String> managers = projectManagers.stream().filter(projectManager -> projectManager.getEndDate() == null)
				.map(ProjectManagerDetails::getProjectManagerEmail).collect(Collectors.toList());
		response.setProjectManagerEmail(managers);
		response.setProjectName(projectDetails.getProjectName());
		response.setProjectCode(projectDetails.getProjectCode());
		response.setProjectStartDate(projectDetails.getStartDate());
		for (ProjectApplicationStatus application : applicationStatus) {
			SingleProjectApplicationOnboarding applicationOnboarding = new SingleProjectApplicationOnboarding();
			applicationLogoEntity.stream()
					.filter(logo -> application.getApplicationName().equalsIgnoreCase(logo.getApplicationName()))
					.findFirst().ifPresent(logo -> {
						applicationOnboarding.setApplicationLogo(logo.getLogoUrl());
						applicationOnboarding.setApplicationName(application.getApplicationName());
					});
			list.add(applicationOnboarding);
		}
		response.setApplicationsInfo(list);
		return new CommonResponse(HttpStatus.OK, new Response("projectDetailViewResponse", response),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getProjectDetailsByDeptId(String departmentId) throws DataValidationException {
		List<ProjectsListByDepartmentResponse> list = new ArrayList<>();
		DepartmentDetails departmentDetails = departmentRepository.findByDepartmentId(departmentId);
		List<ProjectDetails> projectDetails = projectDetailsRepository.getProjectsByDeptId(departmentId);
		if (!projectDetails.isEmpty()) {
			for (ProjectDetails details : projectDetails) {
				ProjectsListByDepartmentResponse projectsListByDepartmentResponse = new ProjectsListByDepartmentResponse();
				projectsListByDepartmentResponse.setProjectId(details.getProjectId());
				projectsListByDepartmentResponse.setProjectName(details.getProjectName());
				projectsListByDepartmentResponse.setProjectCode(details.getProjectCode());
				list.add(projectsListByDepartmentResponse);
			}
		} else {
			throw new DataValidationException(
					"there is no projects linked to the provided department: " + departmentDetails.getDepartmentName(),
					null, null);
		}
		return new CommonResponse(HttpStatus.OK, new Response("ProjectsListByDepartmentResponse", list),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	@Transactional
	public CommonResponse projectApplicationUpdate(ProjectDetailsUpdateRequest projectUpdateRequest,
			UserLoginDetails profile) throws DataValidationException {
		List<ProjectApplicationStatus> projectApplicationList = new ArrayList<>();
		ProjectDetails projectDetails = projectDetailsRepository.findByProjectIdAndDeptId(
				projectUpdateRequest.getProjectName(), projectUpdateRequest.getDepartmentId());
		DepartmentDetails departmentDetails = departmentRepository
				.findByDepartmentId(projectUpdateRequest.getDepartmentId());
		if (departmentDetails == null) {
			throw new DataValidationException("Department not found, please select other department", null, null);
		}
		if (projectDetails != null) {
			List<ProjectApplicationStatus> projectApplications = projectApplicationStatusRepository
					.findByProjectId(projectDetails.getProjectId());
			for (ProjectApplicationListRequest applicationStatus : projectUpdateRequest.getApplicationsInfo()) {
				List<ProjectApplicationStatus> applicationcheck = projectApplications.stream()
						.filter(p -> p.getApplicationName().equalsIgnoreCase(applicationStatus.getApplicationName()))
						.collect(Collectors.toList());
				ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
						.findByApplicationName(applicationStatus.getApplicationName());
				if (applicationLogoEntity == null) {
					throw new DataValidationException("Invalid application name, not present in existing list : "
							+ applicationStatus.getApplicationName(), null, null);
				}
				if (!applicationcheck.isEmpty()) {
					throw new DataValidationException(
							"Application " + applicationStatus.getApplicationName() + "Already linked to this project",
							null, null);
				}
				ProjectApplicationStatus projectApplicationStatus = new ProjectApplicationStatus();
				projectApplicationStatus.setApplicationName(applicationStatus.getApplicationName());
				projectApplicationStatus.setApplicationStatus(applicationStatus.getApplicationStatus());
				projectApplicationStatus.setBuID(Constant.SAASPE);
				projectApplicationStatus.setCreatedBy(profile.getEmailAddress());
				projectApplicationStatus.setCreatedOn(new Date());
				projectApplicationStatus.setEndDate(projectDetails.getEndDate());
				projectApplicationStatus.setProjectId(projectDetails.getProjectId());
				projectApplicationStatus.setProjectName(projectDetails.getProjectName());
				projectApplicationStatus.setStartDate(projectDetails.getStartDate());
				projectApplicationList.add(projectApplicationStatus);
			}
			projectApplicationStatusRepository.saveAll(projectApplicationList);
		} else {
			throw new DataValidationException("Department ID " + projectUpdateRequest.getDepartmentId()
					+ " Doesn't have any project like " + projectUpdateRequest.getProjectName(), null, null);
		}
		return new CommonResponse(HttpStatus.OK, new Response("projectApplicationsAddResponse", new ArrayList<>()),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse getProjectDetails() throws DataValidationException {
		List<ProjectDetails> projectDetails = projectDetailsRepository.getProjectDetails();
		List<ProjectsListResponse> list = new ArrayList<>();
		if (projectDetails == null) {
			throw new DataValidationException("there is no projects to dispaly", null, null);
		}
		for (ProjectDetails details : projectDetails) {
			ProjectsListResponse project = new ProjectsListResponse();
			project.setProjectId(details.getProjectId());
			project.setProjectEndDate(details.getEndDate());
			project.setProjectBudget(details.getProjectBudget());
			project.setProjectBudgetCurrency(details.getBudgetCurrency());
			List<ProjectManagerDetails> projectManagers = ownerRepository.findByProjectId(details.getProjectId());
			List<String> managers = projectManagers.stream()
					.filter(projectManager -> projectManager.getEndDate() == null)
					.map(ProjectManagerDetails::getProjectManagerEmail).collect(Collectors.toList());
			project.setProjectManagerEmail(managers);
			project.setProjectBudget(details.getProjectBudget());
			BigDecimal liceneseCost = BigDecimal.valueOf(0);
			BigDecimal adminCost = BigDecimal.valueOf(0);
			for (ApplicationDetails applicationDetails : details.getApplicationId()) {
				if (applicationDetails.getActiveContracts() != null) {
					for (ApplicationLicenseDetails licenseDetails : applicationDetails.getLicenseDetails()) {
						if (licenseDetails.getEndDate() == null) {
							liceneseCost = liceneseCost.add(licenseDetails.getTotalCost());
							adminCost = adminCost.add(licenseDetails.getConvertedCost());
							project.setCurrency(licenseDetails.getCurrency());
						}
					}
				}
			}
			project.setProjectCost(liceneseCost);
			project.setProjectAdminCost(adminCost);
			project.setProjectName(details.getProjectName());
			project.setProjectCode(details.getProjectCode());
			project.setProjectStartDate(details.getStartDate());
			project.setDepartmentId(details.getDepartmentId().getDepartmentId());
			project.setDepartmentName(details.getDepartmentId().getDepartmentName());
			list.add(project);
		}

		if (list == null || list.isEmpty()) {
			return new CommonResponse(HttpStatus.NOT_FOUND, new Response("ProjectDetailsListResponse", list),
					"No projects found");
		}
		return new CommonResponse(HttpStatus.OK, new Response("ProjectDetailsListResponse", list),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	@Transactional
	public CommonResponse projectMultipleOnboaring(MultipartFile projectFile, UserLoginDetails profile,
			String departmentRequest) throws DataValidationException, IOException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		List<String> erros = new ArrayList<>();
		NewApplicationOnboardingResposne applicationOnboardingResposne = new NewApplicationOnboardingResposne();
		Integer childNum = 1;
		String request = Constant.PROJECT_ID;
		Integer sequence = sequenceGeneratorRepository.getRequestNumberSequence();
		request = request.concat(sequence.toString());
		XSSFWorkbook workbook = new XSSFWorkbook(projectFile.getInputStream());
		XSSFSheet worksheet = workbook.getSheetAt(0);
		List<ProjectOnboardingDetails> list = new ArrayList<>();
		try {
			erros.addAll(projectExcelValidation(projectFile));
		} catch (DataValidationException e) {
			throw new DataValidationException(e.getMessage(), request, null);
		}
		List<SingleProjectOnboardingRequest> projectOnboardingRequests = new ArrayList<>();
		DepartmentDetails departmentDetails = departmentRepository.findByDepartmentId(departmentRequest);
		if (departmentDetails == null) {
			erros.add("Selected department is not found, please select other department");
		}
		if (!erros.isEmpty()) {
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response("ExcelUploadProjectResponse", erros),
					"Excel Upload Failed");
		}

		for (Row cellRow : worksheet) {
			if (cellRow.getPhysicalNumberOfCells() > 1 && cellRow.getRowNum() >= 1 && hasDataInRow(cellRow)) {
				DataFormatter formatter = new DataFormatter(Locale.US);
				SingleProjectOnboardingRequest projectOnboardingRequest = new SingleProjectOnboardingRequest();
				Date projectStartDate = DateParser.parse(formatter.formatCellValue(cellRow.getCell(5)).trim());
				Date projectEndDate = DateParser.parse(formatter.formatCellValue(cellRow.getCell(6)).trim());
				if (projectStartDate.compareTo(projectEndDate) > 0) {
					erros.add("End date Should be greater than Start Date ");
				}
				projectOnboardingRequest.setProjectName(formatter.formatCellValue(cellRow.getCell(1)).trim());
				projectOnboardingRequest.setProjectCode(formatter.formatCellValue(cellRow.getCell(0)).trim());

				List<String> projectManagers = new ArrayList<>();
				String projectManager1 = formatter.formatCellValue(cellRow.getCell(2)).trim();
				String projectManager2 = formatter.formatCellValue(cellRow.getCell(3)).trim();
				projectManagers.add(projectManager1);
				if (!projectManager2.isEmpty()) {
					projectManagers.add(projectManager2);
				}
				projectOnboardingRequest.setProjectManagerEmail(projectManagers);
				projectOnboardingRequest.setProjectDescription(formatter.formatCellValue(cellRow.getCell(4)).trim());
				projectOnboardingRequest.setProjectStartDate(projectStartDate);
				projectOnboardingRequest.setProjectEndDate(projectEndDate);
				projectOnboardingRequest
						.setProjectBudget(new BigDecimal(formatter.formatCellValue(cellRow.getCell(7)).trim()));
				projectOnboardingRequest.setApplicationName(formatter.formatCellValue(cellRow.getCell(8)).trim());
				projectOnboardingRequest.setApplicationStatus(formatter.formatCellValue(cellRow.getCell(9)).trim());

				projectOnboardingRequest.setCurrency(formatter.formatCellValue(cellRow.getCell(10)).trim());
				projectOnboardingRequest.setDepartmentId(departmentRequest);
				projectOnboardingRequest.setRowNumber(cellRow.getRowNum());
				projectOnboardingRequests.add(projectOnboardingRequest);
			}
		}

		Set<String> uniqueCurrencies = new HashSet<>();
		for (SingleProjectOnboardingRequest onboardingRequest : projectOnboardingRequests) {
			uniqueCurrencies.add(onboardingRequest.getCurrency());
		}
		if (uniqueCurrencies.size() > 1) {
			erros.add("Currency for all applications in the same project must be the same");
		}
		List<SingleProjectOnboardingRequest> distinctOnboardingList = new ArrayList<>();
		for (SingleProjectOnboardingRequest singleProjectOnboardingRequest : projectOnboardingRequests) {
			List<SingleProjectApplicationOnboarding> applicationInfo = new ArrayList<>();
			List<SingleProjectOnboardingRequest> onboardinglist = projectOnboardingRequests.stream()
					.filter(p -> p.getProjectName().equalsIgnoreCase(singleProjectOnboardingRequest.getProjectName()))
					.collect(Collectors.toList());
			List<SingleProjectOnboardingRequest> distinctCheck = distinctOnboardingList.stream()
					.filter(p -> p.getProjectName().equalsIgnoreCase(singleProjectOnboardingRequest.getProjectName()))
					.collect(Collectors.toList());
			if (distinctCheck.isEmpty()) {
				erros.addAll(projectBulkObjectValidate(onboardinglist, departmentDetails));
				for (SingleProjectOnboardingRequest onboardingRequest : onboardinglist) {
					SingleProjectApplicationOnboarding applicationStatus = new SingleProjectApplicationOnboarding();
					ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
							.findByApplicationName(onboardingRequest.getApplicationName());
					if (applicationLogoEntity == null) {
						applicationStatus.setApplicationLogo(request);
					}
					applicationStatus.setApplicationName(onboardingRequest.getApplicationName());
					applicationStatus.setApplicationCategory(onboardingRequest.getApplicationStatus());
					applicationInfo.add(applicationStatus);
				}
				singleProjectOnboardingRequest.setApplicationsInfo(applicationInfo);
				if (departmentDetails != null) {
					singleProjectOnboardingRequest.setDepartmentId(departmentDetails.getDepartmentId());
				}
				distinctOnboardingList.add(singleProjectOnboardingRequest);
			}
		}
		String childRequestNum = null;
		for (SingleProjectOnboardingRequest projectBulkObject : distinctOnboardingList) {
			ProjectOnboardingDetails projectOnboardingDetails = new ProjectOnboardingDetails();
			childRequestNum = request.concat("_0" + childNum);
			childNum++;
			ProjectOnboardingDetails projectStatus = projectOnboardingDetailsRepository
					.getProjectStatus(projectBulkObject.getProjectName());
			if (projectStatus == null) {
				ObjectMapper obj = new ObjectMapper();
				String objToString = obj.writeValueAsString(projectBulkObject);
				projectOnboardingDetails.setProjectOnboardingRequest(objToString);

				projectOnboardingDetails.setApprovedRejected(Constant.REVIEW);
				projectOnboardingDetails.setRequestNumber(request);
				projectOnboardingDetails.setCreatedBy(profile.getEmailAddress());
				projectOnboardingDetails.setCreatedOn(new Date());
				projectOnboardingDetails.setOnboardDate(new Date());
				projectOnboardingDetails.setOnboardedByUserEmail(profile.getEmailAddress());
				projectOnboardingDetails.setOnboardingStatus(Constant.PENDING_WITH_REVIEWER);
				projectOnboardingDetails.setProjectName(projectBulkObject.getProjectName());
				projectOnboardingDetails.setProjectCode(projectBulkObject.getProjectCode());
				projectOnboardingDetails.setBudjetCurrency(projectBulkObject.getCurrency());
				projectOnboardingDetails.setWorkGroup(Constant.REVIEWER);
				if (worksheet.getPhysicalNumberOfRows() != 1) {
					projectOnboardingDetails.setChildRequestNumber(childRequestNum);
				}
				list.add(projectOnboardingDetails);
			} else {
				if (projectStatus.getApprovedRejected().equalsIgnoreCase("Rejected")) {
					ObjectMapper obj = new ObjectMapper();
					String objToString = obj.writeValueAsString(projectBulkObject);
					projectOnboardingDetails.setProjectOnboardingRequest(objToString);

					projectOnboardingDetails.setApprovedRejected(Constant.REVIEW);
					projectOnboardingDetails.setRequestNumber(request);
					projectOnboardingDetails.setCreatedBy(profile.getEmailAddress());
					projectOnboardingDetails.setCreatedOn(new Date());
					projectOnboardingDetails.setOnboardDate(new Date());
					projectOnboardingDetails.setOnboardedByUserEmail(profile.getEmailAddress());
					projectOnboardingDetails.setOnboardingStatus(Constant.PENDING_WITH_REVIEWER);
					projectOnboardingDetails.setProjectName(projectBulkObject.getProjectName());
					projectOnboardingDetails.setProjectCode(projectBulkObject.getProjectCode());
					projectOnboardingDetails.setBudjetCurrency(projectBulkObject.getCurrency());
					projectOnboardingDetails.setWorkGroup(Constant.REVIEWER);
					if (worksheet.getPhysicalNumberOfRows() != 1) {
						projectOnboardingDetails.setChildRequestNumber(childRequestNum);
					}
					list.add(projectOnboardingDetails);
				} else {
					erros.add(Constant.PROJECT_WITH_NAME + projectBulkObject.getProjectName()
							+ " In the Stage of Review Or Already Approved");
				}
			}
		}
		if (erros.isEmpty()) {
			projectOnboardingDetailsRepository.saveAll(list);
			SequenceGenerator updateSequence = sequenceGeneratorRepository.getById(1);
			updateSequence.setRequestId(++sequence);
			sequenceGeneratorRepository.save(updateSequence);
			applicationOnboardingResposne.setRequestId(request);
			commonResponse.setMessage("Project Excel Upload Success");
			commonResponse.setStatus(HttpStatus.CREATED);
			response.setAction("Excel Upload Project");
			response.setData(applicationOnboardingResposne);
			commonResponse.setResponse(response);
		} else {
			applicationOnboardingResposne.setRequestId(request);
			commonResponse.setMessage("Project Excel Upload Failed");
			commonResponse.setStatus(HttpStatus.CONFLICT);
			response.setAction("Excel Upload Project");
			response.setData(erros);
			commonResponse.setResponse(response);
		}
		workbook.close();
		return commonResponse;
	}

	private List<String> projectExcelValidation(MultipartFile departmentFile)
			throws DataValidationException, IOException {
		XSSFWorkbook workbook = null;
		List<String> errors = new ArrayList<>();
		try {
			workbook = new XSSFWorkbook(departmentFile.getInputStream());
		} catch (IOException e) {
			throw new IOException(e.getMessage());
		}
		XSSFSheet worksheet = workbook.getSheetAt(0);
		int index = 0;
		int cellConstant = 0;
		List<String> columnNames = new ArrayList<>();
		boolean dataFound = false;
		for (Row cellRow : worksheet) {

			if (cellRow.getRowNum() == 0) {
				cellConstant = cellRow.getPhysicalNumberOfCells();
				for (int cell = 0; cell < cellConstant; cell++) {
					columnNames.add(cellRow.getCell(cell).getStringCellValue().trim());
				}
				if (!columnNames.get(0).equalsIgnoreCase("Project Code")) {
					workbook.close();
					throw new DataValidationException("Seems You're Uploading the Wrong Excel file. Please Check", null,
							null);
				}

			} else if (hasDataInRow(cellRow)) {
				dataFound = true;
				DataFormatter formatter = new DataFormatter(Locale.US);
				for (int cell = 0; cell < cellConstant; cell++) {
					String cellValue = formatter.formatCellValue(cellRow.getCell(cell)).trim();
					if (cell == 7) {
						try {
							BigDecimal budgetValue = new BigDecimal(cellValue);
							if (budgetValue.compareTo(BigDecimal.ZERO) < 0) {
								errors.add("Budget cannot be negative " + cellRow.getRowNum());
							}
						} catch (NumberFormatException e) {
							errors.add("Invalid budget value at row " + cellRow.getRowNum());
						}
					}
					if (cellValue == null || cellValue.isEmpty() && cell != 3) {
						errors.add(
								"Null or empty value in " + columnNames.get(cell) + " at row " + cellRow.getRowNum());
					}
				}
			}
			index++;
		}
		if (index == 1 || !dataFound) {
			errors.add("Please Enter the Data");
		}
		return errors;
	}

	private boolean hasDataInRow(Row row) {
		for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) {
			Cell cell = row.getCell(cellIndex);
			if (cell != null && cell.getCellType() != CellType.BLANK) {
				return true;
			}
		}
		return false;
	}

	private List<String> projectBulkObjectValidate(List<SingleProjectOnboardingRequest> onboardingRequest,
			DepartmentDetails departmentDetails) {
		List<String> errors = new ArrayList<>();
		List<String> applicationName = new ArrayList<>();
		List<ApplicationDetails> applicationDetails = departmentDetails.getApplicationId().stream()
				.filter(activeapp -> activeapp.getEndDate() == null).collect(Collectors.toList());
		List<ProjectDetails> projectdetails = projectDetailsRepository.findAll();
		SingleProjectOnboardingRequest objectConstant = onboardingRequest.get(0);
		DepartmentDetails projectdepartment = departmentRepository.findByDepartmentId(objectConstant.getDepartmentId());
		for (SingleProjectOnboardingRequest projectOnboaring : onboardingRequest) {
			if (projectOnboaring.getProjectName().equalsIgnoreCase(objectConstant.getProjectName())) {
				if (!projectOnboaring.getProjectManagerEmail().get(0).trim()
						.equalsIgnoreCase(objectConstant.getProjectManagerEmail().get(0).trim())
						|| !projectOnboaring.getProjectBudget().equals(objectConstant.getProjectBudget())) {
					errors.add(Constant.PROJECT_WITH_NAME + projectOnboaring.getProjectName()
							+ " have Mismatch Data at Row  " + projectOnboaring.getRowNumber());
				}
				if (!projectOnboaring.getProjectBudget().equals(objectConstant.getProjectBudget())) {
					errors.add(Constant.PROJECT_WITH_NAME + projectOnboaring.getProjectName()
							+ " have Mismatch Data at Row " + projectOnboaring.getRowNumber());
				}
				if (projectOnboaring.getApplicationName().equals(objectConstant.getApplicationName())
						&& projectOnboaring.getApplicationStatus().equalsIgnoreCase(
								objectConstant.getApplicationStatus())
						&& (projectOnboaring.getRowNumber() != 1)) {
					long countSameApplication = onboardingRequest.stream()
							.filter(request -> request.getApplicationStatus().equalsIgnoreCase("New"))
							.filter(request -> request.getApplicationName()
									.equalsIgnoreCase(objectConstant.getApplicationName()))
							.count();

					if (countSameApplication > 1) {
						errors.add(Constant.PROJECT_WITH_NAME + objectConstant.getProjectName()
								+ " has the same new application name appearing multiple times");
					}
				}
				if (!projectOnboaring.getApplicationName().equals(objectConstant.getApplicationName())
						&& projectOnboaring.getRowNumber() != 1) {
					Map<String, Long> newApplicationCounts = onboardingRequest.stream()
							.filter(request -> request.getApplicationStatus().equalsIgnoreCase("New"))
							.collect(Collectors.groupingBy(SingleProjectOnboardingRequest::getApplicationName,
									Collectors.counting()));
					boolean hasDuplicateNewApplications = newApplicationCounts.values().stream()
							.anyMatch(count -> count > 1);
					if (hasDuplicateNewApplications) {
						errors.add(Constant.PROJECT_WITH_NAME + objectConstant.getProjectName()
								+ " has the same new application name appearing multiple times");
					}
				}
				if ((projectOnboaring.getProjectManagerEmail().size() > 1)) {
					if (!projectOnboaring.getProjectManagerEmail().get(1).trim()
							.equalsIgnoreCase(objectConstant.getProjectManagerEmail().get(1).trim())) {
						errors.add(Constant.PROJECT_WITH_NAME + projectOnboaring.getProjectName()
								+ " have Mismatch Data at Row " + projectOnboaring.getRowNumber());
					}
					if (projectOnboaring.getProjectManagerEmail().get(1).trim()
							.equalsIgnoreCase(projectOnboaring.getProjectManagerEmail().get(0).trim())) {
						errors.add(" Project Manager primary and seconday Emails are same at Row "
								+ projectOnboaring.getRowNumber());
					}
				}
				if (!onboardingRequest.stream()
						.filter(request -> request.getApplicationStatus().trim().equalsIgnoreCase(Constant.EXISTING))
						.collect(Collectors.toList()).isEmpty()
						&& !projectOnboaring.getApplicationStatus().equalsIgnoreCase("new")) {
					List<ApplicationDetails> matchingApplications = applicationDetails.stream()
							.filter(application -> application.getApplicationName()
									.equalsIgnoreCase(projectOnboaring.getApplicationName()))
							.collect(Collectors.toList());
					if (matchingApplications.isEmpty()) {
						errors.add(Constant.PROJECT_WITH_NAME + projectOnboaring.getProjectName() + Constant.APPLICATION
								+ projectOnboaring.getApplicationName() + " does not belong to the given "
								+ departmentDetails.getDepartmentName() + Constant.DEPARTMENT_AT_ROW
								+ projectOnboaring.getRowNumber());
					} else if ((!matchingApplications.isEmpty()) && (matchingApplications.stream()
							.filter(s -> s.getOwnerDepartment().equalsIgnoreCase(projectdepartment.getDepartmentName()))
							.collect(Collectors.toList()) == null)) {
						errors.add(Constant.PROJECT_WITH_NAME + projectOnboaring.getProjectName() + Constant.APPLICATION
								+ projectOnboaring.getApplicationName() + " does not belongs to the given "
								+ departmentDetails.getDepartmentName() + Constant.DEPARTMENT_AT_ROW
								+ projectOnboaring.getRowNumber());
					}

					if (matchingApplications.stream()
							.filter(s -> s.getOwnerDepartment().equalsIgnoreCase(projectdepartment.getDepartmentName()))
							.collect(Collectors.toList()) == null) {
						errors.add(Constant.PROJECT_WITH_NAME + projectOnboaring.getProjectName() + " "
								+ Constant.APPLICATION + projectOnboaring.getApplicationName()
								+ " does not belongs to the given " + departmentDetails.getDepartmentName()
								+ Constant.DEPARTMENT_AT_ROW + " " + projectOnboaring.getRowNumber());
					}

				}
				if (!projectdetails.stream()
						.filter(project -> project.getProjectCode() != null
								&& project.getProjectCode().equalsIgnoreCase(projectOnboaring.getProjectCode().trim()))
						.collect(Collectors.toList()).isEmpty()) {
					errors.add(Constant.PROJECT_WITH_NAME + projectOnboaring.getProjectName() + Constant.APPLICATION
							+ projectOnboaring.getApplicationName() + " project code exists at Row "
							+ projectOnboaring.getRowNumber());

				} else if (!applicationDetails.stream()
						.filter(application -> application.getApplicationName()
								.equalsIgnoreCase(projectOnboaring.getApplicationName())
								&& projectOnboaring.getProjectName().equalsIgnoreCase(application.getProjectName()))
						.collect(Collectors.toList()).isEmpty()) {
					errors.add(Constant.PROJECT_WITH_NAME + projectOnboaring.getProjectName() + " Application "
							+ projectOnboaring.getApplicationName() + " not exists at Row "
							+ projectOnboaring.getRowNumber());
				}
			}
			for (String managerEmail : projectOnboaring.getProjectManagerEmail()) {
				if (userDetailsRepository.findByDepartmentIdAndUserEmail(projectOnboaring.getDepartmentId().trim(),
						managerEmail) == null) {
					errors.add("Project Manager With " + managerEmail + " Doesn't Exist In Given Department at Row "
							+ projectOnboaring.getRowNumber());
				}
			}
			applicationName.add(projectOnboaring.getApplicationName());
		}
		List<String> distinctApplication = applicationName.stream().distinct().collect(Collectors.toList());
		if (distinctApplication.size() != applicationName.size()) {
			errors.add(Constant.PROJECT_WITH_NAME + objectConstant.getProjectName() + " having Duplicate Application ");
		}
		return errors;
	}

	@Override
	public CommonResponse projectSpendAnalytics(String projectId) throws DataValidationException {
		List<DeptApplicationUsageAnalystics> analystics = new ArrayList<>();
		ProjectDetails projectDetail = projectDetailsRepository.findByProjectId(projectId);
		if (projectDetail != null) {
			if (!projectDetail.getApplicationId().isEmpty()) {
				projectDetail.getApplicationId().forEach(p -> {
					if (p.getActiveContracts() != null && p.getEndDate() == null) {
						DeptApplicationUsageAnalystics analystic = new DeptApplicationUsageAnalystics();
						analystic.setApplicationId(p.getApplicationId());
						analystic.setApplicationName(p.getApplicationName());
						analystic.setUserCount(p.getUserDetails().size());
						analystic.setTotalApplicationCost(BigDecimal.valueOf(
								p.getLicenseDetails().stream().mapToInt(q -> q.getTotalCost().intValue()).sum()));
						analystic.setTotalApplicationAdminCost(BigDecimal.valueOf(
								p.getLicenseDetails().stream().mapToInt(q -> q.getConvertedCost().intValue()).sum()));
						analystic.setCurrency(p.getLicenseDetails().get(0).getCurrency());
						analystics.add(analystic);
					}
				});
			}
		} else {
			throw new DataValidationException("Project Not Found for the Given Id " + projectId, null, null);
		}
		analystics.sort(Comparator.comparing(DeptApplicationUsageAnalystics::getTotalApplicationCost).reversed());
		return new CommonResponse(HttpStatus.OK, new Response("ProjectAnalysticsResponse", analystics),
				Constant.DETAILS_RETRIEVED_SUCCESSFULLY);
	}

	@Override
	public CommonResponse sendBudgetEmail()
			throws DataValidationException, IOException, TemplateException, MessagingException, InterruptedException {
		String toAddress = null;
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String subject = Constant.DEPARTMENT_BUDGET_ALERT;
		List<ProjectDetails> projectList = projectDetailsRepository.findAll();
		for (ProjectDetails project : projectList) {
			List<ProjectManagerDetails> owners = ownerRepository.findByProjectId(project.getProjectId());
			ProjectManagerDetails primaryowner = owners.stream().filter(owner -> owner.getPriority() == 1).findFirst()
					.orElse(null);
			if (primaryowner != null) {
				toAddress = primaryowner.getProjectManagerEmail();
			}
			BigDecimal emailPercentage = BigDecimal.valueOf(100.0);
			Map<String, Object> model = new HashMap<>();
			BigDecimal cost = BigDecimal.valueOf(0);
			BigDecimal adminCost = BigDecimal.valueOf(0);
			for (ApplicationDetails application : project.getApplicationId()) {
				if (application.getActiveContracts() != null) {
					List<ApplicationContractDetails> applicationContractDetails = application.getContractDetails();
					for (ApplicationContractDetails contractDetails : applicationContractDetails) {
						if (contractDetails.getContractStatus().equalsIgnoreCase("Active")
								|| contractDetails.getContractStatus().equalsIgnoreCase("Expired")) {
							for (ApplicationLicenseDetails licenseDetails : contractDetails.getLicenseDetails()) {
								cost = cost.add(licenseDetails.getTotalCost(), MathContext.DECIMAL32);
								adminCost = adminCost.add(licenseDetails.getConvertedCost(), MathContext.DECIMAL32);
							}
						}
					}
				}
			}
			BigDecimal remainingBudget = project.getProjectBudget().subtract(cost);
			Template t = config.getTemplate("project-budget-analysis.html");
			String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
			if (cost.compareTo(project.getProjectBudget()) > 0) {
				content = content.replace("{{marketing}}", project.getProjectName());
				content = content.replace("{{allocatedBudget}}", project.getProjectBudget().toString());
				content = content.replace("{{spend}}", cost.toString());
				content = content.replace("{{remaining}}", BigDecimal.valueOf(0).toString());
				content = content.replace("{{threshold}}", BigDecimal.valueOf(75).toString());
				content = content.replace("{{currentConsumed}}", BigDecimal.valueOf(100).toString());
			} else if (cost.compareTo(project.getProjectBudget()) < 0
					|| cost.compareTo(project.getProjectBudget()) == 0) {
				BigDecimal percentSpent = cost.divide(project.getProjectBudget(), 2, RoundingMode.HALF_UP)
						.multiply(new BigDecimal(100));
				BigDecimal currentPercentage = BigDecimal.valueOf(100).min(percentSpent);
				emailPercentage = currentPercentage;
				if (currentPercentage.compareTo(BigDecimal.valueOf(75)) == 0
						|| currentPercentage.compareTo(BigDecimal.valueOf(75)) > 0) {
					content = content.replace("{{marketing}}", project.getProjectName());
					content = content.replace("{{allocatedBudget}}", project.getProjectBudget().toString());
					content = content.replace("{{spend}}", cost.toString());
					content = content.replace("{{remaining}}", remainingBudget.toString());
					content = content.replace("{{threshold}}", BigDecimal.valueOf(75).toString());
					content = content.replace("{{currentConsumed}}", percentSpent.toString());
				}
			}
			try {
				helper.setFrom(mailDomainName, senderName);
				helper.setTo(toAddress);
				helper.setSubject(subject);
				helper.setText(content, true);
			} catch (UnsupportedEncodingException e) {
				throw new UnsupportedEncodingException(e.getMessage());
			} catch (MessagingException e) {
				throw new MessagingException(e.getMessage());
			}
			if ((emailPercentage.compareTo(BigDecimal.valueOf(75)) == 0
					|| emailPercentage.compareTo(BigDecimal.valueOf(75)) > 0) && (budgetMailTrigger)) {
				mailSender.send(message);

			}
			Thread.sleep(20000);
		}
		return null;
	}

	@Override
	public CommonResponse updateProjectStatus() {
		List<Projects> projects = projectRepository.findAll();
		for(Projects project : projects ) {
			if(project.getProjectStartDate().before(new Date())) {
				if(project.getProjectEndDate().after(new Date())) {
					project.setProjectStatus(true);
				}else {
					project.setProjectStatus(false);
				}
			}else if(project.getProjectStartDate().after(new Date())) {
				project.setProjectStatus(false);
			}else {
				project.setProjectStatus(true);
			}
		}
		return null;
	}
}
