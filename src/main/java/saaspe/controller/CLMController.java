package saaspe.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import saaspe.aspect.ControllerLogging;
import saaspe.constant.Constant;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.ClmContractRequest;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.service.CLMService;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/clm")
public class CLMController {

	@Autowired
	private CLMService clmService;

	private static final Logger log = LoggerFactory.getLogger(CLMController.class);

	@PostMapping(value = "/addcontract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAuthority('ADD_CONTRACT')")
	public ResponseEntity<CommonResponse> addClmContract(@RequestParam(value = "body", required = true) String json,
			@RequestPart(value = "create-document-file", required = false) MultipartFile[] createDocumentFiles,
			@RequestParam(value = "create_id", required = false) String[] createId,
			@RequestParam(value = "delete_id", required = false) String[] deleteId,
			@RequestParam(value = "templateId", required = true) String templateId, Authentication authentication,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID,
			HttpServletRequest request) {
		try {
			UserLoginDetails profile = new UserLoginDetails();
			if (authentication != null) {
				profile = (UserLoginDetails) authentication.getPrincipal();
			}
			String provider = request.getHeader(Constant.HEADER_PROVIDER_STRING);
			CommonResponse applicationDetailsResponse;
			if (provider != null && provider.equalsIgnoreCase("azure")) {
				applicationDetailsResponse = clmService.addClmContract(json, createDocumentFiles, createId, deleteId,
						templateId,request,profile);
			} else {
				applicationDetailsResponse = clmService.addClmContract(json, createDocumentFiles, createId, deleteId,
						templateId,request,profile);
			}

			return ResponseEntity.status(HttpStatus.OK).body(applicationDetailsResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending addClmContract method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("addClmContract", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending addClmContract method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("addClmContract", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/list")
	public ResponseEntity<CommonResponse> getListOfClmContract(Authentication authentication,
			HttpServletRequest request,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int limit, @RequestParam(required = false) String status,
			@RequestParam(required = false) String searchText, @RequestParam(required = false) String order,
			@RequestParam(required = false, defaultValue = "contractName") String orderBy) {
		try {
			UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse applicationDetailsResponse = clmService.getListOfClmContract(profile, page, limit, request,
					status, searchText, order, orderBy);
			return ResponseEntity.status(HttpStatus.OK).body(applicationDetailsResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending getListOfClmContract method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("getListOfClmContract", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getListOfClmContract method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("getListOfClmContract", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/detail")
	public ResponseEntity<CommonResponse> getClmContractDetailsView(Authentication authentication,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID,
			@RequestParam String envelopeId) {
		try {
			CommonResponse applicationDetailsResponse = clmService.getClmContractDetailsView(envelopeId);
			return ResponseEntity.status(HttpStatus.OK).body(applicationDetailsResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending getClmContractDetailsView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("getDetailsViewofClmContract", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getClmContractDetailsView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("getDetailsViewofClmContract", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/d")
	@Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kuala_Lumpur")
	public ResponseEntity<CommonResponse> upCommingContractRenewalReminderEmail() {
		try {
			log.info("==== upCommingContractRenewalReminderEmail method started====" + LocalDate.now(ZoneId.systemDefault()));
			CommonResponse commonResponse = clmService.upCommingContractRenewalReminderEmail();
			log.info("==== upCommingContractRenewalReminderEmail method started====" + LocalDate.now(ZoneId.systemDefault()));
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending upCommingContractRenewalReminderEmail method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("contractDetailsOverviewResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending upCommingContractRenewalReminderEmail method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ContractRenewalResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PutMapping(value = "/update/template/{templateId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CommonResponse> updateTemplate(@RequestParam(value = "body", required = true) String json,
			@PathVariable(required = true) String templateId,
			@RequestPart(value = "create-document-file", required = false) MultipartFile[] createDocumentFiles,
			@RequestPart(value = "update-document-file", required = false) MultipartFile[] updateDocumentFiles,
			@RequestParam(value = "update_id", required = false) String[] updateId,
			@RequestParam(value = "delete_id", required = false) String[] deleteId) {
		try {
			CommonResponse commonResponse = clmService.updateTemplate(json, templateId, createDocumentFiles,
					updateDocumentFiles, updateId, deleteId);
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending updateTemplate method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("updateTemplateResponce", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			e.printStackTrace();
			log.error("*** Ending updateTemplate method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("updateTemplateResponce", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping(value = "/create/template", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CommonResponse> createTemplate(@RequestParam(value = "body", required = true) String json,
			@RequestPart(value = "create-document-file", required = false) MultipartFile[] createDocumentFiles,
			Authentication authentication,@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID, HttpServletRequest request) {
		try {
			UserLoginDetails profile = new UserLoginDetails();
			if (authentication != null) {
				profile = (UserLoginDetails) authentication.getPrincipal();
			}
			CommonResponse commonResponse = clmService.createTemplate(json, createDocumentFiles, profile, request);
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (DataValidationException e) {
			log.error("*** Ending createTemplate method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("CreateTemplateResponce", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			e.printStackTrace();
			log.error("*** Ending createTemplate method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("CreateTemplateResponce", new ArrayList<>()), e.getMessage()));
		}

	}

	@PostMapping(value = "/createEnvelopeMultiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CommonResponse> createEnvelope(@RequestParam(value = "body", required = true) String json,
			@RequestPart(value = "create-document-file", required = false) MultipartFile[] createDocumentFiles,
			@RequestParam(value = "create_id", required = false) String[] createId,
			@RequestParam(value = "delete_id", required = false) String[] deleteId,
			@RequestParam(value = "templateId", required = true) String templateId,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse commonResponse = clmService.createEnvelope(json, createDocumentFiles, createId, deleteId,
					templateId);
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("*** Ending CreateEnvelope method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("CreateEnvelopeResponce", new ArrayList<>()), e.getMessage()));
		}

	}

	@GetMapping("/dashboard/view")
	public ResponseEntity<CommonResponse> dashboardView(HttpServletRequest request, Authentication authentication) {

		try {
			UserLoginDetails profile = new UserLoginDetails();
			if (authentication != null) {
				profile = (UserLoginDetails) authentication.getPrincipal();
			}
			CommonResponse commonResponse = clmService.dashboardView(request, profile);
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (Exception e) {
			log.error("*** Ending DashboardView method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DashboardViewResponce", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/enevelope/{envelopeid}")
	public ResponseEntity<CommonResponse> envelopeAudit(@PathVariable String envelopeid) {
		try {
			CommonResponse commonResponse = clmService.envelopeAudit(envelopeid);
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (Exception e) {
			log.error("*** Ending envelopeAudit method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("EnvelopeResponce", new ArrayList<>()), e.getMessage()));
		}

	}

	@GetMapping("/getEnvelopeDocuments/{envelopeId}/{documentId}")
	public ResponseEntity<CommonResponse> getEnvelopeDocument(@PathVariable String envelopeId,
			@PathVariable String documentId) {
		try {
			CommonResponse commonResponse = clmService.getEnvelopeDocument(envelopeId, documentId);
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (Exception e) {
			log.error("*** Ending getEnvelopeDocument method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("EnvelopeDocumentResponce", new ArrayList<>()), e.getMessage()));
		}

	}

	@GetMapping("/listEnvelopeRecipients/{envelopeId}")
	public ResponseEntity<CommonResponse> getlistEnvelopeRecipients(@PathVariable String envelopeId) {
		try {
			CommonResponse commonResponse = clmService.getlistEnvelopeRecipients(envelopeId);
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (Exception e) {
			log.error("*** Ending getlistEnvelopeRecipients method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("EnvelopeRecipientResponce", new ArrayList<>()), e.getMessage()));
		}

	}

	@GetMapping("/getEnvelopeDocumentDetails/{envelopeId}")
	public ResponseEntity<CommonResponse> getEnvelopeDocumentDetails(@PathVariable String envelopeId) {
		try {
			CommonResponse commonResponse = clmService.getEnvelopeDocumentDetails(envelopeId);
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (Exception e) {
			log.error("*** Ending getEnvelopeDocumentDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("EnvelopeDocumentDetailResponce", new ArrayList<>()), e.getMessage()));
		}

	}

	@GetMapping("/template/{templateId}")
	public ResponseEntity<CommonResponse> listTemplateById(@PathVariable String templateId) {
		try {
			CommonResponse commonResponse = clmService.listTemplateById(templateId);
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (Exception e) {
			log.error("*** Ending listTemplateById method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("TemplateList Response", new ArrayList<>()), e.getMessage()));
		}

	}

	@GetMapping("/templates")
	public ResponseEntity<CommonResponse> listTemplates(@RequestParam(defaultValue = "10") String count,
			@RequestParam(defaultValue = "0") String start, @RequestParam(defaultValue = "asc") String order,
			@RequestParam(defaultValue = "name") String orderBy, @RequestParam(defaultValue = "a") String searchText) {
		try {
			CommonResponse commonResponse = clmService.listTemplates(count, start, order, orderBy, searchText);
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (Exception e) {
			log.error("*** Ending listTemplates method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("TemplateListResponse", new ArrayList<>()), e.getMessage()));
		}

	}

	@GetMapping("/get/templates")
	public ResponseEntity<CommonResponse> getAllTemplates() {
		try {
			CommonResponse commonResponse = clmService.getAllTemplates();
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (Exception e) {
			log.error("*** Ending getAllTemplates method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("TemplateListResponse", new ArrayList<>()), e.getMessage()));
		}

	}

	@GetMapping(value = "/getTemplateDocuments/{templateId}/{documentId}")
	public ResponseEntity<CommonResponse> getTemplateDocument(@PathVariable String templateId,
			@PathVariable String documentId) {
		try {
			CommonResponse commonResponse = clmService.getTemplateDocument(templateId, documentId);
			return ResponseEntity.status(HttpStatus.OK).body(commonResponse);
		} catch (Exception e) {
			log.error("*** Ending TemplateDocumentResponce method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("TemplateDocumentResponce", new ArrayList<>()), e.getMessage()));
		}

	}

}
