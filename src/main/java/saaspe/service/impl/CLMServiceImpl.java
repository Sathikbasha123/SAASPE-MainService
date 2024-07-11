package saaspe.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.Data;
import saaspe.configuration.mongo.SequenceGeneratorService;
import saaspe.constant.Constant;
import saaspe.docusign.document.ClmContractDocument;
import saaspe.docusign.document.CreateTemplate;
import saaspe.docusign.document.EnvelopeDocument;
import saaspe.docusign.model.CreateTemplateModel;
import saaspe.docusign.model.DocumentResponse;
import saaspe.docusign.model.DocusignUrls;
import saaspe.docusign.model.EnvelopeResponse;
import saaspe.docusign.repository.ClmContractDocumentRepository;
import saaspe.docusign.repository.CreateTemplateRepository;
import saaspe.docusign.repository.EnvelopeRepository;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.ClmContractListPagination;
import saaspe.model.ClmContractListResponce;
import saaspe.model.ClmContractRequest;
import saaspe.model.CommonResponse;
import saaspe.model.DashboardViewResponce;
import saaspe.model.DocusignUserCache;
import saaspe.model.ExpiringContractResponce;
import saaspe.model.LatestContractResponce;
import saaspe.model.Response;
import saaspe.service.CLMService;
import saaspe.utils.RedisUtility;

@Service
public class CLMServiceImpl implements CLMService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RedisUtility redisUtility;

	@Autowired
	private JavaMailSender mailSender;

	@Value("${redirecturl.path}")
	private String redirectUrl;

	@Autowired
	private Configuration config;

	@Value("${sendgrid.domain.name}")
	private String mailDomainName;

	@Value("${sendgrid.domain.sendername}")
	private String senderName;

	@Autowired
	private EnvelopeRepository envelopeRepository;

	@Autowired
	private SequenceGeneratorService sequenceGeneratorService;

	@Autowired
	private ClmContractDocumentRepository clmContractDocumentRepository;

	@Autowired
	private CreateTemplateRepository createTemplateRepository;

	private Random random = new Random();

	@Value("${docusign-urls-file}")
	private String docusignUrls;

	@Value("${docusign.host.url}")
	private String docusignHost;

	@Value("${spring.media.host}")
	private String mediaHost;

	@Value("${spring.image.key}")
	private String imageKey;

	@Value("${sendgrid.domain.support}")
	private String supportEmail;

	@Value("${saaspe.folder.id}")
	private String saaspefolderId;

	@Value("${docusign.user.id}")
	private String docusignUserId;

	@Value("${docusign-admin-email}")
	private String docusignAdminEmail;
	
	@Value("${docusign.prefix}")
	private String redisPrefix;

	private static final Logger log = LoggerFactory.getLogger(CLMServiceImpl.class);

	public DocusignUrls getDousignUrl() {
		ClassPathResource resource = new ClassPathResource(docusignUrls);
		ObjectMapper objectMapper = new ObjectMapper();
		DocusignUrls docusignUrl = null;
		try {
			docusignUrl = objectMapper.readValue(resource.getInputStream(), DocusignUrls.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return docusignUrl;
	}

	@Override
	@Transactional
	public CommonResponse addClmContract(String json, MultipartFile[] createDocumentFiles, String[] createId,
			String[] deleteId, String templateId, HttpServletRequest request, UserLoginDetails profile)
			throws DataValidationException, JsonProcessingException, ParseException {
		ObjectMapper mapper = new ObjectMapper();
		String token = request.getHeader(Constant.HEADER_STRING);
		String xAuthProvider = request.getHeader(Constant.X_AUTH_PROVIDER);
		String email = null;
		DocusignUserCache userId = null;
		ClmContractRequest jsonrequest = mapper.readValue(json, ClmContractRequest.class);
		int minRange = 1000;
		int maxRange = 9999;
		List<String> createIdsList = createId != null ? Arrays.asList(createId) : new ArrayList<>();
		List<String> deleteIdsList = deleteId != null ? Arrays.asList(deleteId) : new ArrayList<>();
		if (createId == null && createDocumentFiles == null) {
			return new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.CLM_CONTRACT_RESPONSE, new ArrayList<>()),
					"Provide existing documentId or upload document to send envelope");

		}
		if (xAuthProvider == null || xAuthProvider.equalsIgnoreCase(Constant.INTERNAL)) {
			email = profile.getEmailAddress();
		} else {
			DecodedJWT jwt = JWT.decode(token.replace(Constant.BEARER, ""));
			email = jwt.getClaim("upn").asString();
			userId = redisUtility.getDocusignValue(redisPrefix + email);
		}
		UriComponentsBuilder builderTemplate = UriComponentsBuilder
				.fromHttpUrl(getDousignUrl().getListTemplateById().replace(Constant.HOST, docusignHost));
		builderTemplate.path(templateId);
		String urlTemplate = builderTemplate.toUriString();
		HttpHeaders headersTemplate = new HttpHeaders();
		headersTemplate.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<Object> responseEntity = restTemplate.exchange(urlTemplate, HttpMethod.GET, null, Object.class);
		if (responseEntity.getBody() == null) {
			throw new DataValidationException("Template id provided is not in the existing list", null, null);
		}
		ArrayList<saaspe.model.Document> documentRequests = new ArrayList<>();
		if (createDocumentFiles != null) {
			for (MultipartFile createDocumentFile : createDocumentFiles) {
				saaspe.model.Document documentRequest = new saaspe.model.Document();
				try {
					documentRequest
							.setDocumentBase64(Base64.getEncoder().encodeToString(createDocumentFile.getBytes()));
					documentRequest.setName(createDocumentFile.getOriginalFilename().substring(0,
							createDocumentFile.getOriginalFilename().indexOf(".")));
					documentRequest.setCategory("createFile");
					int randomNumber = random.nextInt(maxRange - minRange + 1) + minRange;
					documentRequest.setDocumentId(String.valueOf(randomNumber));
					documentRequests.add(documentRequest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		jsonrequest.setDocuments(documentRequests);
		if (createId != null) {
			for (String createid : createIdsList) {
				saaspe.model.Document documentRequest = new saaspe.model.Document();
				documentRequest.setDocumentId(createid);
				documentRequest.setCategory("createId");
				documentRequests.add(documentRequest);
			}
		}
		jsonrequest.setDocuments(documentRequests);
		if (!deleteIdsList.isEmpty()) {
			for (String deleteid : deleteIdsList) {
				saaspe.model.Document documentRequest = new saaspe.model.Document();
				documentRequest.setDocumentId(deleteid);
				documentRequest.setCategory(Constant.DELETE);
				documentRequests.add(documentRequest);
			}
		}
		jsonrequest.setDocuments(documentRequests);
		jsonrequest.setUserEmail(email);
		ClmContractDocument clmContractDocument = new ClmContractDocument();
		EnvelopeDocument envelopeDocument = new EnvelopeDocument();
		clmContractDocument.setId(sequenceGeneratorService.generateSequence(ClmContractDocument.SEQUENCE_NAME));
		clmContractDocument.setAmigoId(sequenceGeneratorService.generateCommonSequence(Constant.SEQUENCE_NAME));
		clmContractDocument.setTemplateId(templateId);
		clmContractDocument.setContractName(jsonrequest.getContractName());
		clmContractDocument.setContractStartDate(jsonrequest.getContractStartDate());
		clmContractDocument.setContractEndDate(jsonrequest.getContractEndDate());
		clmContractDocument.setBuID("BUID");
		clmContractDocument.setOpID(Constant.SAASPE);
		clmContractDocument.setCreatedOn(new Date());
		clmContractDocument.setCreatedBy(email);
		clmContractDocument.setRenewalReminderNotification(jsonrequest.getRenewalReminderNotification());
		clmContractDocument.setContractPeriod(jsonrequest.getContractPeriod());
		clmContractDocument.setStatus("sent");
		clmContractDocument.setVersion("1.0");
		clmContractDocument.setOrder(0);
		clmContractDocument.setUniqueString(generateRandomString(6));
		clmContractDocument.setReferenceId(templateId);
		clmContractDocument.setReferenceType("Template");
		clmContractDocument.setReviewerSigningOrder(jsonrequest.getReviewerSigningOrder());
		clmContractDocument.setSignerSigningOrder(jsonrequest.getSignerSigningOrder());

		UriComponentsBuilder builder = UriComponentsBuilder.newInstance().scheme("http").host("saaspe-docusign-svc")
				.port(8085).path(getDousignUrl().getCreateEnvelope());
		if (userId != null && xAuthProvider != null && xAuthProvider.equalsIgnoreCase("Azure")) {
			builder.queryParam("userId", userId.getUserId());
		} else {
			builder.queryParam("userId", docusignUserId);
		}
		builder.queryParam(Constant.TEMPLATE_ID, templateId);
		String url = builder.toUriString();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> requestEntity = new HttpEntity<>(jsonrequest, headers);
		ResponseEntity<Object> response = null;
		log.info("*** createEnvelope rest call with url: {}", url);
		try {
			response = restTemplate.postForEntity(url, requestEntity, Object.class);
		} catch (HttpClientErrorException.BadRequest ex) {
			String responseBody = ex.getResponseBodyAsString();
			Object errorResponse = mapper.readValue(responseBody, Object.class);
			return new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.CLM_CONTRACT_RESPONSE, errorResponse), "Contract Creation Failed");
		}
		String json1 = mapper.writeValueAsString(response.getBody());
		JsonNode rootNode = mapper.readTree(json1);
		String envelopeId = rootNode.get("envelopeId").asText();
		if (envelopeId != null) {
			String envelopeDataUrl = getDousignUrl().getGetEnvelopeById().replace(Constant.HOST, docusignHost)
					+ envelopeId;
			HttpEntity<?> httpEntity = new HttpEntity<>(headers);
			ResponseEntity<EnvelopeResponse> envelopeDataResponse = null;
			try {
				envelopeDataResponse = restTemplate.exchange(envelopeDataUrl, HttpMethod.GET, httpEntity,
						EnvelopeResponse.class);
			} catch (HttpClientErrorException.BadRequest ex) {
				String responseBody = ex.getResponseBodyAsString();
				Object errorResponse = mapper.readValue(responseBody, Object.class);
				return new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response(Constant.CLM_CONTRACT_RESPONSE, errorResponse), "Contract Creation Failed");
			}
			envelopeDocument.setId(sequenceGeneratorService.generateSequence(EnvelopeDocument.SEQUENCE_NAME));
			envelopeDocument.setAmigoId(sequenceGeneratorService.generateCommonSequence(Constant.SEQUENCE_NAME));
			envelopeDocument.setEnvelopeId(envelopeId);
			envelopeDocument.setCreatedOn(new Date());
			envelopeDocument.setEnvelope(envelopeDataResponse.getBody().getEnvelope());
			List<DocumentResponse> documentResponses = new ArrayList<>();
			for (DocumentResponse documentResponse : envelopeDataResponse.getBody().getDocuments()) {
				DocumentResponse docResponse = new DocumentResponse();
				docResponse.setDocumentId(documentResponse.getDocumentId());
				docResponse.setDocumentIdGuid(documentResponse.getDocumentIdGuid());
				docResponse.setName(documentResponse.getName());
				docResponse.setDocumentBase64(documentResponse.getDocumentBase64());
				documentResponses.add(documentResponse);
			}
			String json2 = mapper.writeValueAsString(envelopeDataResponse.getBody().getEnvelope());
			JsonNode rootNode2 = mapper.readTree(json2);
			envelopeDocument.setDocuments(documentResponses);
			envelopeDocument.setStartDate(new Date());
			envelopeDocument.setBuID("BUID");
			envelopeDocument.setOpID(Constant.SAASPE);
			envelopeRepository.save(envelopeDocument);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			clmContractDocument.setStartDate(dateFormat.parse(rootNode2.path("createdDateTime").asText()));
			clmContractDocument.setSenderEmail(rootNode2.path(Constant.SENDER).path(Constant.EMAIL).asText());
			clmContractDocument.setSenderName(rootNode2.path(Constant.SENDER).path(Constant.USERNAME).asText());
			clmContractDocument.setEnvelopeId(envelopeId);
			clmContractDocumentRepository.save(clmContractDocument);
		} else {
			throw new DataValidationException("create contract failed, try again!", null, null);
		}
		return new CommonResponse(HttpStatus.CREATED, new Response(Constant.CLM_CONTRACT_RESPONSE, envelopeId),
				"Contract details submitted successfully");
	}

	@Override
	public CommonResponse getListOfClmContract(UserLoginDetails profile, int page, int limit,
			HttpServletRequest request, String status, String searchText, String order, String orderBy)
			throws DataValidationException, JsonProcessingException {
		Sort sort = null;
		PageRequest pageable = PageRequest.of(page, limit);
		if (order != null && !order.isEmpty()) {
			Sort.Direction sortDirection = Sort.Direction.ASC;
			if (order.equalsIgnoreCase("desc")) {
				sortDirection = Sort.Direction.DESC;
			}
			sort = Sort.by(sortDirection, orderBy);
			pageable = PageRequest.of(page, limit, sort);
		}
		String token = request.getHeader(Constant.HEADER_STRING);
		String xAuthProvider = request.getHeader(Constant.HEADER_PROVIDER_STRING);
		String email = null;
		long totalCount = 0;
		Page<ClmContractDocument> pages = null;
		if (xAuthProvider == null || xAuthProvider.equalsIgnoreCase(Constant.INTERNAL)) {
			pages = clmContractDocumentRepository.findAll(pageable);
			totalCount = clmContractDocumentRepository.findAll().size();
		} else {
			DecodedJWT jwt = JWT.decode(token.replace(Constant.BEARER, ""));
			email = jwt.getClaim("upn").asString();
			pages = clmContractDocumentRepository.findByCreatedBy(email, pageable);
			totalCount = clmContractDocumentRepository.findAllByCreatedBy(email).size();
		}
		Collation collation = Collation.of("en").strength(Collation.ComparisonLevel.primary());
		List<ClmContractListResponce> clMContractEntityList = new ArrayList<>();
		if (status == null && order == null && searchText == null) {
			List<ClmContractDocument> list = pages.getContent();
			for (ClmContractDocument contractDocument : list) {
				ClmContractListResponce contractresponce = new ClmContractListResponce();
				contractresponce.setContractId(contractDocument.getId());
				contractresponce.setTemplateId(contractDocument.getTemplateId());
				contractresponce.setContractName(contractDocument.getContractName());
				contractresponce.setContractStartDate(contractDocument.getContractStartDate());
				contractresponce.setContractEndDate(contractDocument.getContractEndDate());
				contractresponce.setRenewalReminderNotification(contractDocument.getRenewalReminderNotification());
				contractresponce.setTemplateId(contractDocument.getTemplateId());
				contractresponce.setEnvelopeId(contractDocument.getEnvelopeId());
				contractresponce.setContractPeriod(contractDocument.getContractPeriod());
				contractresponce.setSenderMail(contractDocument.getSenderEmail());
				contractresponce.setStatus(contractDocument.getStatus());
				contractresponce.setSenderName(
						contractDocument.getSenderName() != null ? contractDocument.getSenderName() : null);
				contractresponce
						.setStartDate(contractDocument.getStartDate() != null ? contractDocument.getStartDate() : null);
				contractresponce.setCompeletedDate(
						contractDocument.getCompletedDate() != null ? contractDocument.getCompletedDate() : null);
				clMContractEntityList.add(contractresponce);
			}

		} else {
			List<ClmContractDocument> lists = null;
			if (status != null) {
				if (Constant.CLM_ENVELOPE_STATUS.stream().noneMatch(status::equalsIgnoreCase)) {
					throw new DataValidationException(
							"Status should be in the following values completed, created, declined, delivered, sent, signed, voided",
							null, null);
				}
				if (searchText != null && !searchText.isEmpty()) {
					if(email!=null) {
						lists = clmContractDocumentRepository.findByCreatedByAndField(email, orderBy, searchText, status, pageable, collation);
						totalCount = clmContractDocumentRepository.findByCreatedByAndFieldCount(email, orderBy, searchText, status, collation)
								.size();
					}else {
						lists = clmContractDocumentRepository.findByField(orderBy, searchText, status, pageable, collation);
						totalCount = clmContractDocumentRepository.findByFieldCount(orderBy, searchText, status, collation)
								.size();
					}
				} else {
					if(email != null) {
						lists = clmContractDocumentRepository.findByCreatedByAndField(email, orderBy, "^", status, pageable, collation);
						totalCount = clmContractDocumentRepository.findByCreatedByAndFieldCount(email, orderBy, "^", status, collation).size();
					}else {
						lists = clmContractDocumentRepository.findByField(orderBy, "^", status, pageable, collation);
						totalCount = clmContractDocumentRepository.findByFieldCount(orderBy, "^", status, collation).size();
					}
				}
			} else {
				if (searchText != null && !searchText.isEmpty()) {
					if(email != null) {
						lists = clmContractDocumentRepository.findByCreatedByAndFieldWithoutStatus(email, orderBy, "^" + searchText, pageable,
								collation);
						totalCount = clmContractDocumentRepository
								.findByCreatedByAndFieldWithoutStatusCount(email, orderBy, "^" + searchText, collation).size();
					}else {
						lists = clmContractDocumentRepository.findByFieldWithoutStatus(orderBy, "^" + searchText, pageable,
								collation);
						totalCount = clmContractDocumentRepository
								.findByFieldWithoutStatusCount(orderBy, "^" + searchText, collation).size();
					}
				} else {
					if(email != null) {
						lists = clmContractDocumentRepository.findByCreatedByAndFieldWithoutStatus(email, "^", "^", pageable, collation);
						totalCount = clmContractDocumentRepository.findByCreatedByAndFieldWithoutStatusCount(email, "^", "^", collation)
								.size();
					}else {
						lists = clmContractDocumentRepository.findByFieldWithoutStatus("^", "^", pageable, collation);
						totalCount = clmContractDocumentRepository.findByFieldWithoutStatusCount("^", "^", collation)
								.size();
					}
				}
			}

			for (ClmContractDocument contractDocument : lists) {
				ClmContractListResponce contractresponce = new ClmContractListResponce();
				contractresponce.setContractId(contractDocument.getId());
				contractresponce.setTemplateId(contractDocument.getTemplateId());
				contractresponce.setContractName(contractDocument.getContractName());
				contractresponce.setContractStartDate(contractDocument.getContractStartDate());
				contractresponce.setContractEndDate(contractDocument.getContractEndDate());
				contractresponce.setRenewalReminderNotification(contractDocument.getRenewalReminderNotification());
				contractresponce.setTemplateId(contractDocument.getTemplateId());
				contractresponce.setEnvelopeId(contractDocument.getEnvelopeId());
				contractresponce.setContractPeriod(contractDocument.getContractPeriod());
				contractresponce.setSenderMail(contractDocument.getSenderEmail());
				contractresponce.setStatus(contractDocument.getStatus());
				contractresponce.setSenderName(
						contractDocument.getSenderName() != null ? contractDocument.getSenderName() : null);
				contractresponce
						.setStartDate(contractDocument.getStartDate() != null ? contractDocument.getStartDate() : null);
				contractresponce.setCompeletedDate(
						contractDocument.getCompletedDate() != null ? contractDocument.getCompletedDate() : null);
				clMContractEntityList.add(contractresponce);
			}
		}
		ClmContractListPagination data = new ClmContractListPagination(totalCount, clMContractEntityList);
		Response responseData = new Response(Constant.CLM_CONTRACT_RESPONSE, data);
		return new CommonResponse(HttpStatus.OK, responseData, "Contract details fetched successfully");
	}

	@Override
	public CommonResponse getClmContractDetailsView(String envelopeId)
			throws DataValidationException, JsonProcessingException {
		ContractDetailsViewResponse detailsViewResponse = new ContractDetailsViewResponse();
		ClmContractDocument contract = clmContractDocumentRepository.findByEnvelopeId(envelopeId);
		EnvelopeDocument envelopeDocument = envelopeRepository.findByEnvelopeId(envelopeId);
		ObjectMapper mapper = new ObjectMapper();
		String json1 = mapper.writeValueAsString(envelopeDocument.getEnvelope());
		JsonNode rootNode = mapper.readTree(json1);
		if (contract == null) {
			throw new DataValidationException("Please provide valid Envelope Id", null, null);
		}
		detailsViewResponse.setContractName(contract.getContractName());
		detailsViewResponse.setContractStartDate(contract.getContractStartDate());
		detailsViewResponse.setContractEndDate(contract.getContractEndDate());
		detailsViewResponse.setRenewalReminderNotification(contract.getRenewalReminderNotification());
		detailsViewResponse.setContractPeriod(contract.getContractPeriod());
		detailsViewResponse.setSenderName(rootNode.get(Constant.SENDER).get(Constant.USERNAME).asText());
		detailsViewResponse.setSenderEmail(rootNode.get(Constant.SENDER).get(Constant.EMAIL).asText());
		detailsViewResponse.setCreatedDate(rootNode.get(Constant.CREATED_DATE_TIME).asText());
		detailsViewResponse.setDeliveredDate(rootNode.get("deliveredDateTime").asText());
		detailsViewResponse.setExpiryDate(rootNode.get("expireDateTime").asText());
		detailsViewResponse.setEmailSubject(rootNode.get("emailSubject").asText());
		detailsViewResponse.setStatus(rootNode.get(Constant.STATUS).asText());
		if (rootNode.get(Constant.COMPLETED_DATE_TIME).asText() != null) {
			detailsViewResponse.setCompletedDate(rootNode.get(Constant.COMPLETED_DATE_TIME).asText());
		} else {
			detailsViewResponse.setCompletedDate(null);
		}
		return new CommonResponse(HttpStatus.OK, new Response(Constant.CLM_CONTRACT_RESPONSE, detailsViewResponse),
				"Contract Details Retrieved Successfully");
	}

	@Data
	public class ContractDetailsViewResponse {
		private String contractName;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		private Date contractStartDate;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		private Date contractEndDate;
		private int renewalReminderNotification;
		private String senderName;
		private String senderEmail;
		private String createdDate;
		private String deliveredDate;
		private String expiryDate;
		private String completedDate;
		private String emailSubject;
		private String status;
		private int contractPeriod;
	}

	@Override
	@Transactional
	public CommonResponse upCommingContractRenewalReminderEmail()
			throws IOException, TemplateException, DataValidationException, MessagingException {
		List<ClmContractDocument> clMContractEntities = clmContractDocumentRepository.findAll();
		if (clMContractEntities.isEmpty()) {
			throw new DataValidationException("There is no contracts for renewal-reminder", null, null);
		}
		for (ClmContractDocument contractDetails : clMContractEntities) {
			if (contractDetails.getContractEndDate() != null) {
				long timeDifference = new Date().getTime() - contractDetails.getContractEndDate().getTime();
				long daysDifference = TimeUnit.MILLISECONDS.toDays(timeDifference) % 365;
				if (daysDifference <= contractDetails.getRenewalReminderNotification() && daysDifference > 0) {
					sendReminderEmail(contractDetails.getContractName(), contractDetails.getCreatedBy(), daysDifference,
							contractDetails.getContractEndDate(), contractDetails.getEnvelopeId());
				}
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("ContractRenewalReminderResponse", new ArrayList<>()),
				"Reminder mail had been sent successfully");
	}

	private void sendReminderEmail(String contractName, String emailAddress, long daysDifference, Date renewalDate,
			String envelopeId) throws IOException, TemplateException, MessagingException {
		String toAddress = emailAddress;
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String subject = Constant.CONTRACT_EMAIL_SUBJECT;
		Map<String, Object> model = new HashMap<>();
		Template t = config.getTemplate("clm-renewal-reminder.html");
		String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
		content = content.replace("{{days}}", String.valueOf(daysDifference));
		content = content.replace("{{contractName}}", contractName);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		content = content.replace("{{renewalDate}}", String.valueOf(formatter.format(renewalDate)));
		content = content.replace("{{hostname}}", redirectUrl);
		content = content.replace("{{envelopeId}}", envelopeId);
		content = content.replace("{{supportEmail}}", supportEmail);
		content = content.replace("{{orgName}}", senderName);
		content = content.replace("{{mediaHost}}", mediaHost);
		content = content.replace("{{imageKey}}", imageKey);
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
		mailSender.send(message);
	}

	@Override
	public CommonResponse updateTemplate(String json, String templateId, MultipartFile[] createDocumentFiles,
			MultipartFile[] updateDocumentFiles, String[] updateId, String[] deleteId)
			throws JsonProcessingException, DataValidationException {
		CommonResponse commonresponce = new CommonResponse();
		Response respose = new Response();
		int minRange = 1000;
		int maxRange = 9999;
		Map<String, Object> responseData = new HashMap<>();
		ObjectMapper objectmapper = new ObjectMapper();
		UpdateTemplate jsonrequest = objectmapper.readValue(json, UpdateTemplate.class);

		List<String> updateIdsList = updateId != null ? Arrays.asList(updateId) : new ArrayList<>();
		List<String> deleteIdsList = deleteId != null ? Arrays.asList(deleteId) : new ArrayList<>();

		if (updateId != null) {
			if (updateId != deleteId) {
				responseData.put("update_id", updateId);
			} else {
				throw new DataValidationException("update_id should not be same as delete_id", "400",
						HttpStatus.BAD_REQUEST);
			}
		}

		if (deleteId != null) {
			responseData.put("delete_id", deleteId);
		}
		List<String> createdIds = new ArrayList<>();
		List<DocumentRequest> documentRequests = new ArrayList<>();
		if (createDocumentFiles != null) {
			for (MultipartFile createDocumentFile : createDocumentFiles) {
				DocumentRequest documentRequest = new DocumentRequest();
				try {
					int randomNumber = random.nextInt(maxRange - minRange + 1) + minRange;
					if (documentRequests.stream()
							.filter(p -> p.getDocumentId().equalsIgnoreCase(String.valueOf(randomNumber)))
							.collect(Collectors.toList()).isEmpty()) {
						documentRequest
								.setDocumentBase64(Base64.getEncoder().encodeToString(createDocumentFile.getBytes()));
						documentRequest.setDocumentId(String.valueOf(randomNumber));
						documentRequest.setName(createDocumentFile.getOriginalFilename().substring(0,
								createDocumentFile.getOriginalFilename().indexOf(".")));
						documentRequest.setCategory("create");
						documentRequests.add(documentRequest);
						createdIds.add(String.valueOf(randomNumber));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			responseData.put(Constant.CREATE_ID, createdIds);
		}
		if (updateDocumentFiles != null) {
			int j = 0;
			for (MultipartFile updateDocumentFile : updateDocumentFiles) {
				DocumentRequest documentRequest = new DocumentRequest();
				try {
					documentRequest
							.setDocumentBase64(Base64.getEncoder().encodeToString(updateDocumentFile.getBytes()));
					documentRequest.setDocumentId(updateIdsList.get(j));
					documentRequest.setName(updateDocumentFile.getOriginalFilename().substring(0,
							updateDocumentFile.getOriginalFilename().indexOf(".")));
					documentRequest.setCategory("update");
				} catch (IOException e) {
					e.printStackTrace();
				}
				documentRequests.add(documentRequest);
				j++;
			}
		}
		if (deleteIdsList != null) {
			for (String deleteid : deleteIdsList) {
				DocumentRequest documentRequest = new DocumentRequest();
				documentRequest.setDocumentId(deleteid);
				documentRequest.setCategory(Constant.DELETE);
				documentRequests.add(documentRequest);
			}

		}
		jsonrequest.setDocuments(documentRequests);
		List<String> errors = new ArrayList<>();
		String url = getDousignUrl().getUpdateTemplate().replace(Constant.HOST, docusignHost);
		url = url.replace(Constant.TEMPLATEID, templateId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> requestEntity = new HttpEntity<>(jsonrequest, headers);
		restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Object.class);

		if (errors.isEmpty()) {
			respose.setAction(Constant.UPDATE_TEMPLATE_RESPONSE);
			commonresponce.setStatus(HttpStatus.OK);
			commonresponce.setResponse(respose);
			commonresponce.setMessage("Template Updated Sucessfully");
			respose.setData(responseData);
			responseData.put(Constant.TEMPLATE_NAME, jsonrequest.getTemplateName());
		} else {
			commonresponce.setMessage("Update Template Failed");
			commonresponce.setResponse(new Response(Constant.UPDATE_TEMPLATE_RESPONSE, errors));
			commonresponce.setStatus(HttpStatus.BAD_REQUEST);
			respose.setAction(Constant.UPDATE_TEMPLATE_RESPONSE);
			respose.setData(new ArrayList<>());
			responseData.put(Constant.TEMPLATE_NAME, jsonrequest.getTemplateName());
		}
		return commonresponce;
	}

	@Data
	public static class UpdateTemplate {
		private String templateName;
		private String templateDescritpion;
		private String emailSubject;
		private String emailMessage;
		private Boolean signerCanSignONMobile;
		private List<SignerRequest> signers;
		private List<DocumentRequest> documents;
		private RemainderRequest reminders;
		private ExpirationRequest expirations;
		private Boolean allowComments;
		private Boolean enforceSignerVisibility;
		private Boolean recipientLock;
		private Boolean messageLock;
		private Boolean signingOrder;
		private String status;
	}

	@Data
	public static class SignerRequest {
		private String name;
		private String email;
		private String recipientType;
		private String recipientRole;
		private Boolean canSignOffline;
		private String routingOrder;

	}

	@Data
	public static class DocumentRequest {
		private String name;
		private String documentId;
		private String documentBase64;
		private String category;
	}

	@Data
	public static class RemainderRequest {
		private Boolean reminderEnabled;
		private String reminderDelay;
		private String reminderFrequency;
	}

	@Data
	public static class ExpirationRequest {
		private Boolean expiryEnabled;
		private String expiryAfter;
		private String expiryWarn;
	}

	@Override
	public CommonResponse createTemplate(String json, MultipartFile[] createDocumentFiles, UserLoginDetails profile,
			HttpServletRequest request) throws JsonProcessingException, DataValidationException {
		String token = request.getHeader(Constant.HEADER_STRING);
		String xAuthProvider = request.getHeader(Constant.X_AUTH_PROVIDER);
		String email = null;
		String folderId = saaspefolderId;
		DocusignUserCache userId = null;
		if (xAuthProvider == null || xAuthProvider.equalsIgnoreCase(Constant.INTERNAL)) {
			email = profile.getEmailAddress();
		} else {
			DecodedJWT jwt = JWT.decode(token.replace(Constant.BEARER, ""));
			email = jwt.getClaim("upn").asString();
			userId = redisUtility.getDocusignValue(redisPrefix + email);
		}
		CommonResponse commonresponce = new CommonResponse();
		Response respose = new Response();
		int minRange = 1000;
		int maxRange = 9999;
		Map<String, Object> responseData = new HashMap<>();
		ObjectMapper objectmapper = new ObjectMapper();
		UpdateTemplate jsonrequest = objectmapper.readValue(json, UpdateTemplate.class);

		List<DocumentRequest> documentRequests = new ArrayList<>();
		List<String> createdIds = new ArrayList<>();
		if (createDocumentFiles != null) {
			for (MultipartFile createDocumentFile : createDocumentFiles) {
				DocumentRequest documentRequest = new DocumentRequest();
				try {
					int randomNumber = random.nextInt(maxRange - minRange + 1) + minRange;
					if (documentRequests.stream()
							.filter(p -> p.getDocumentId().equalsIgnoreCase(String.valueOf(randomNumber)))
							.collect(Collectors.toList()).isEmpty()) {
						documentRequest
								.setDocumentBase64(Base64.getEncoder().encodeToString(createDocumentFile.getBytes()));

						documentRequest.setDocumentId(String.valueOf(randomNumber));
						documentRequest.setName(createDocumentFile.getOriginalFilename().substring(0,
								createDocumentFile.getOriginalFilename().indexOf(".")));
						documentRequest.setCategory("create");
						documentRequests.add(documentRequest);
						createdIds.add(String.valueOf(randomNumber));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			responseData.put(Constant.CREATE_ID, createdIds);
		}
		jsonrequest.setDocuments(documentRequests);
		// DocusignUserCache userId =
		// redisUtility.getDocusignValue(Constant.DOCUSIGN_REDIS_PREFIX + profile);
		List<String> errors = new ArrayList<>();
		String url = getDousignUrl().getCreateTemplate().replace(Constant.HOST, docusignHost);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		builder.queryParam(Constant.EMAIL, email);
		builder.queryParam("folderId", folderId);

		if (userId != null && xAuthProvider != null && xAuthProvider.equalsIgnoreCase("Azure")) {
			builder.queryParam("userId", userId.getUserId());
		} else {
			builder.queryParam("userId", docusignUserId);
		}
		HttpEntity<Object> requestEntity = new HttpEntity<>(jsonrequest, headers);
		ResponseEntity<Object> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.POST,
				requestEntity, Object.class);
		if (responseEntity.getBody() != null) {
			fromString(responseEntity.getBody().toString());
			CreateTemplate templateCreate = new CreateTemplate();
			templateCreate.setId(sequenceGeneratorService.generateSequence(CreateTemplate.SEQUENCE_NAME));
			templateCreate.setAmigoId(sequenceGeneratorService.generateCommonSequence(Constant.SEQUENCE_NAME));
			templateCreate.setTemplateId(extractTemplateId(responseEntity.getBody().toString()));
			templateCreate.setTemplateName(jsonrequest.getTemplateName());
			templateCreate.setBuID("BUID");
			templateCreate.setOpID(Constant.SAASPE);
			createTemplateRepository.save(templateCreate);
		}
		if (errors.isEmpty()) {
			respose.setAction(Constant.CREATE_TEMPLATE_RESPONSE);
			commonresponce.setStatus(HttpStatus.OK);
			commonresponce.setResponse(respose);
			commonresponce.setMessage("Template Created Sucessfully");
			respose.setData(responseData);
			responseData.put(Constant.TEMPLATE_NAME, jsonrequest.getTemplateName());
		} else {
			commonresponce.setMessage("Create Template Failed");
			commonresponce.setResponse(new Response(Constant.CREATE_TEMPLATE_RESPONSE, errors));
			commonresponce.setStatus(HttpStatus.BAD_REQUEST);
			respose.setAction(Constant.CREATE_TEMPLATE_RESPONSE);
			respose.setData(new ArrayList<>());
		}
		return commonresponce;
	}

	public static CreateTemplateModel fromString(String input) {
		CreateTemplateModel yourObject = new CreateTemplateModel();
		Pattern pattern = Pattern.compile("(\\w+)=(\\w+|\"[^\"]+\")|templateId=(.*)(, |$)");
		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			String key = matcher.group(1);
			String value = matcher.group(2);
			if ("name".equals(key)) {
				yourObject.setName("null".equals(value) ? null : value);
			}
			if (Constant.TEMPLATE_ID.equals(key)) {
				yourObject.setTemplateId("null".equals(value) ? null : value);
				if (value == null) {
					value = matcher.group(3); // Capture the full templateId value
					yourObject.setTemplateId("null".equals(value) ? null : value);
				}
			}
		}
		return yourObject;
	}

	public static String extractTemplateId(String input) {
		Pattern pattern = Pattern.compile("templateId=([^,]+)");
		Matcher matcher = pattern.matcher(input);

		if (matcher.find()) {
			return matcher.group(1);
		}

		return null; // Return null if templateId is not found
	}

	@Override
	public CommonResponse createEnvelope(String json, MultipartFile[] createDocumentFiles, String[] createId,
			String[] deleteId, String templateId) throws JsonProcessingException {
		CommonResponse commonresponce = new CommonResponse();
		Response respose = new Response();
		Map<String, Object> responseData = new HashMap<>();
		ObjectMapper objectmapper = new ObjectMapper();
		EsignaturePojo jsonrequest = objectmapper.readValue(json, EsignaturePojo.class);
		int minRange = 1000; // Minimum 4-digit number (inclusive)
		int maxRange = 9999; // Maximum 4-digit number (inclusive)

		List<String> createIdsList = createId != null ? Arrays.asList(createId) : new ArrayList<>();
		List<String> deleteIdsList = deleteId != null ? Arrays.asList(deleteId) : new ArrayList<>();

		if (createId != null) {
			responseData.put(Constant.CREATE_ID, createId);
		}
		if (deleteId != null) {
			responseData.put("delete_id", deleteId);
		}

		ArrayList<Document> documentRequests = new ArrayList<>();
		if (createDocumentFiles != null) {
			for (MultipartFile createDocumentFile : createDocumentFiles) {
				Document documentRequest = new Document();
				try {
					documentRequest
							.setDocumentBase64(Base64.getEncoder().encodeToString(createDocumentFile.getBytes()));
					documentRequest.setName(createDocumentFile.getOriginalFilename().substring(0,
							createDocumentFile.getOriginalFilename().indexOf(".")));
					documentRequest.setCategory("createFile");
					int randomNumber = random.nextInt(maxRange - minRange + 1) + minRange;
					documentRequest.setDocumentId(String.valueOf(randomNumber));
					documentRequests.add(documentRequest);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		jsonrequest.setDocuments(documentRequests);
		if (createId != null) {
			for (String createid : createIdsList) {
				Document documentRequest = new Document();
				documentRequest.setDocumentId(createid);
				documentRequest.setCategory("createId");
				documentRequests.add(documentRequest);
			}
		}
		jsonrequest.setDocuments(documentRequests);
		if (deleteIdsList != null) {
			for (String deleteid : deleteIdsList) {
				Document documentRequest = new Document();
				documentRequest.setDocumentId(deleteid);
				documentRequest.setCategory(Constant.DELETE);
				documentRequests.add(documentRequest);
			}
		}
		jsonrequest.setDocuments(documentRequests);

		String baseUrl = getDousignUrl().getCreateEnvelopeMultiple().replace(Constant.HOST, docusignHost);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);
		builder.queryParam(Constant.TEMPLATE_ID, templateId);
		String url = builder.toUriString();
		List<String> errors = new ArrayList<>();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> requestEntity = new HttpEntity<>(jsonrequest, headers);
		restTemplate.exchange(url, HttpMethod.POST, requestEntity, Object.class);
		if (errors.isEmpty()) {
			respose.setAction("CreateEnvelopeResponse");
			commonresponce.setStatus(HttpStatus.OK);
			commonresponce.setResponse(respose);
			commonresponce.setMessage("Create Envelope Completed Sucessfully");
			respose.setData(responseData);
		} else {
			commonresponce.setMessage("Create Envelope Failed");
			commonresponce.setResponse(new Response("CreateEnvelopeResponse", errors));
			commonresponce.setStatus(HttpStatus.BAD_REQUEST);
			respose.setData(new ArrayList<>());
		}
		return commonresponce;
	}

	@Override
	public CommonResponse dashboardView(HttpServletRequest request, UserLoginDetails profile)
			throws JsonProcessingException {
		String token = request.getHeader(Constant.HEADER_STRING);
		String xAuthProvider = request.getHeader(Constant.X_AUTH_PROVIDER);
		String email = null;
		Date today = new Date();
		List<ClmContractDocument> totalContracts ;
		List<ClmContractDocument> latest10envelopes ;
		List<ClmContractDocument> latest10expiredcontracts ;
		if (xAuthProvider == null || xAuthProvider.equalsIgnoreCase(Constant.INTERNAL)) {
			totalContracts = clmContractDocumentRepository
					.findAll();
			latest10envelopes = clmContractDocumentRepository
					.findTop10OrderByCreatedOnDesc(Sort.by(Sort.Direction.DESC, "createdOn"));
			latest10expiredcontracts = clmContractDocumentRepository
					.findTop10ByContractEndDateLessThanOrderByContractEndDateDesc(today);
		} else {
			DecodedJWT jwt = JWT.decode(token.replace(Constant.BEARER, ""));
			email = jwt.getClaim("upn").asString();
			totalContracts = clmContractDocumentRepository
					.findAllByCreatedBy(email);
			latest10envelopes = clmContractDocumentRepository
					.findTop10ByCreatedByOrderByCreatedOnDesc(email,
							Sort.by(Sort.Direction.DESC, "createdOn"));
			latest10expiredcontracts = clmContractDocumentRepository
					.findTop10ByContractEndDateLessThanAndCreatedByOrderByContractEndDateDesc(today, email);
		}
		DashboardViewResponce dashboardview = new DashboardViewResponce();
		ObjectMapper mapper = new ObjectMapper();
		totalContracts.forEach(s -> s.getStatus());
		int signCompletedCount = 0;
		int signInProgressCount = 0;
		for (ClmContractDocument clms : totalContracts) {
			if (clms.getStatus() != null) {
				signCompletedCount = (clms.getStatus() != null && clms.getStatus().equalsIgnoreCase("Completed"))
						? signCompletedCount + 1
						: signCompletedCount;
				dashboardview.setSignCompleted(signCompletedCount);

				signInProgressCount = (clms.getStatus() != null && clms.getStatus().equalsIgnoreCase("Sent"))
						? signInProgressCount + 1
						: signInProgressCount;
				dashboardview.setSignInProgress(signInProgressCount);

			} else {
				EnvelopeDocument envelopedocuemnt = envelopeRepository.findByEnvelopeId(clms.getEnvelopeId());
				if (envelopedocuemnt != null) {
					String envelopeJson = mapper.writeValueAsString(envelopedocuemnt.getEnvelope());
					JsonNode rootNode = mapper.readTree(envelopeJson);
					if (rootNode.path(Constant.STATUS).asText().equalsIgnoreCase("Completed")) {
						signCompletedCount = signCompletedCount + 1;
					}
					dashboardview.setSignCompleted(signCompletedCount);
					if (rootNode.path(Constant.STATUS).asText().equalsIgnoreCase("sent")) {
						signInProgressCount = signInProgressCount + 1;
					}
				}
			}
		}
		dashboardview.setTotalContracts(totalContracts.size());
		if (latest10envelopes.size() > 10)
			latest10envelopes = latest10envelopes.subList(0, 10);
		List<LatestContractResponce> latestContractsList = new ArrayList<>();
		for (ClmContractDocument contract : latest10envelopes) {
			LatestContractResponce latestcontract = new LatestContractResponce();
			latestcontract.setContractId(contract.getId());
			latestcontract.setTemplateId(contract.getTemplateId());
			latestcontract.setContractName(contract.getContractName());
			latestcontract.setContractStartDate(contract.getContractStartDate());
			latestcontract.setContractEndDate(contract.getContractEndDate());
			latestcontract.setRenewalReminderNotification(contract.getRenewalReminderNotification());
			latestcontract.setTemplateId(contract.getTemplateId());
			latestcontract.setEnvelopeId(contract.getEnvelopeId());
			latestcontract.setContractPeriod(contract.getContractPeriod());
			latestcontract.setSenderName(contract.getSenderName());
			latestcontract.setSenderMail(contract.getSenderEmail());
			latestcontract.setStatus(contract.getStatus());
			latestcontract.setStartDate(contract.getStartDate() != null ? contract.getStartDate() : null);
			latestcontract.setCompeletedDate(contract.getCompletedDate() != null ? contract.getCompletedDate() : null);
			latestContractsList.add(latestcontract);
		}
		List<ExpiringContractResponce> expiringContractsList = new ArrayList<>();
		int count = 0;
		for (ClmContractDocument contract : latest10expiredcontracts) {
			if (count >= 10) {
				break;
			}
			ExpiringContractResponce expiringcontract = new ExpiringContractResponce();
			expiringcontract.setContractId(contract.getId());
			expiringcontract.setTemplateId(contract.getTemplateId());
			expiringcontract.setContractName(contract.getContractName());
			expiringcontract.setContractStartDate(contract.getContractStartDate());
			expiringcontract.setContractEndDate(contract.getContractEndDate());
			expiringcontract.setRenewalReminderNotification(contract.getRenewalReminderNotification());
			expiringcontract.setTemplateId(contract.getTemplateId());
			expiringcontract.setEnvelopeId(contract.getEnvelopeId());
			expiringcontract.setContractPeriod(contract.getContractPeriod());
			expiringcontract.setContractPeriod(contract.getContractPeriod());
			expiringcontract.setSenderName(contract.getSenderName());
			expiringcontract.setSenderMail(contract.getSenderEmail());
			expiringcontract.setStatus(contract.getStatus());
			expiringcontract.setStartDate(contract.getStartDate() != null ? contract.getStartDate() : null);
			expiringcontract
					.setCompeletedDate(contract.getCompletedDate() != null ? contract.getCompletedDate() : null);
			expiringContractsList.add(expiringcontract);
			count++;
		}
		List<ExpiringContractResponce> sortedContracts = expiringContractsList.stream()
				.sorted(Comparator.comparing(ExpiringContractResponce::getContractEndDate))
				.collect(Collectors.toList());
		dashboardview.setExpiredContracts(sortedContracts);
		dashboardview.setLatestContracts(latestContractsList);
		Response responseData = new Response("Dashboardview Response", dashboardview);
		return new CommonResponse(HttpStatus.OK, responseData, "Dashboard details fetched successfully");
	}

	@Data
	public static class EsignaturePojo {
		private List<Document> documents;
		private String emailSubject;
		private String emailMessage;
		private String allowComments;
		private Boolean enforceSignerVisibility;
		private String recipientLock;
		private String messageLock;
		private Reminders reminders;
		private Expiration expirations;
		private Recipients recipients;
		private String status;
		private String useAccountDefaults;
		private Boolean signerCanSignOnMobile;
	}

	@Data
	public static class Reminders {
		private String reminderDelay;
		private String reminderFrequency;
		private Boolean reminderEnabled;
	}

	@Data
	public static class Expiration {
		private String expireAfter;
		private String expireEnabled;
		private String expireWarn;
	}

	@Data
	public static class Recipients {
		private List<Signer> signers;
		private List<Carboncopy> cc;
	}

	@Data
	public static class Document {
		private String documentBase64;
		private String documentId;
		private String fileExtension;
		private String name;
		private String category;
	}

	@Data
	public static class Carboncopy {
		private String email;
		private String name;
		private String routingOrder;
	}

	@Data
	public static class Signer {
		private String email;
		private String name;
		private String routingOrder;
		private String recipientType;
		private Tabs tabs;
		private String roleName;
	}

	@Data
	public static class Tabs {
		private List<SignHereTabs> signHereTabs;
	}

	@Data
	public static class SignHereTabs {

		private String xPosition;
		private String yPosition;
		private String documentId;
		private String pageNumber;
	}

	@Override
	public CommonResponse envelopeAudit(String envelopeid) {
		String url = getDousignUrl().getAuditLogs().replace(Constant.HOST, docusignHost) + envelopeid;
		ResponseEntity<Object> responseEntity = null;
		try {
			responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, Object.class);
		} catch (HttpServerErrorException ex) {
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response("DocusignAuditLogs", new ArrayList<>()),
					"Audit details failed");
		}
		return new CommonResponse(HttpStatus.OK, new Response("AuditdetailsResponce", responseEntity.getBody()),
				"Audit details fetched successfully");
	}

	@Override
	public CommonResponse getEnvelopeDocument(String envelopeId, String documentId) {
		ObjectMapper mapper = new ObjectMapper();
		String url = getDousignUrl().getEnvelopeDocuments().replace(Constant.HOST, docusignHost);
		url = url.replace(Constant.ENVELOPE_ID, envelopeId);
		url = url.replace("{documentId}", documentId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
					String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.ENVELOPE_DOCUMENT_RESPONSE, response.getBody()),
						"EnvelopeDocument Fetched Successfully");
			} else {
				return new CommonResponse(response.getStatusCode(),
						new Response(Constant.ENVELOPE_DOCUMENT_RESPONSE, null),
						Constant.HTTP_STATUS_CODE + response.getStatusCodeValue());
			}
		} catch (HttpClientErrorException.BadRequest ex) {
			String responseBody = ex.getResponseBodyAsString();
			Object errorResponse = null;
			try {
				errorResponse = mapper.readValue(responseBody, Object.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.ENVELOPE_DOCUMENT_RESPONSE, errorResponse), "EnvelopeDocument Fetch Failed");
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response(Constant.ENVELOPE_DOCUMENT_RESPONSE, null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse getlistEnvelopeRecipients(String envelopeId) {
		ObjectMapper mapper = new ObjectMapper();
		String url = getDousignUrl().getListEnvelopeRecipients().replace(Constant.HOST, docusignHost);
		url = url.replace(Constant.ENVELOPE_ID, envelopeId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
					Object.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.LISTENVELOPE_RESPONSE, response.getBody()),
						"ListEnvelope Recipients Fetched Successfully");
			} else {
				return new CommonResponse(response.getStatusCode(), new Response(Constant.LISTENVELOPE_RESPONSE, null),
						Constant.HTTP_STATUS_CODE + response.getStatusCodeValue());
			}
		} catch (HttpClientErrorException.BadRequest ex) {
			String responseBody = ex.getResponseBodyAsString();
			Object errorResponse = null;
			try {
				errorResponse = mapper.readValue(responseBody, Object.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.LISTENVELOPE_RESPONSE, errorResponse),
					"ListEnvelope Recipients Fetch Failed");
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response(Constant.LISTENVELOPE_RESPONSE, null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse getEnvelopeDocumentDetails(String envelopeId) {
		ObjectMapper mapper = new ObjectMapper();
		String url = getDousignUrl().getEnvelopeDocumentDetails().replace(Constant.HOST, docusignHost);
		url = url.replace(Constant.ENVELOPE_ID, envelopeId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
					Object.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.ENVELOPE_DOCUMENT_DETAILS_RESPONSE, response.getBody()),
						"EnvelopeDocumentdetails Fetched Successfully");
			} else {
				return new CommonResponse(response.getStatusCode(),
						new Response(Constant.ENVELOPE_DOCUMENT_DETAILS_RESPONSE, null),
						Constant.HTTP_STATUS_CODE + response.getStatusCodeValue());
			}
		} catch (HttpClientErrorException.BadRequest ex) {
			String responseBody = ex.getResponseBodyAsString();
			Object errorResponse = null;
			try {
				errorResponse = mapper.readValue(responseBody, Object.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.ENVELOPE_DOCUMENT_DETAILS_RESPONSE, errorResponse),
					"EnvelopeDocumentdetails Fetch Failed");
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response(Constant.ENVELOPE_DOCUMENT_DETAILS_RESPONSE, null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse listTemplateById(String templateId) {
		ObjectMapper mapper = new ObjectMapper();
		String url = getDousignUrl().getListTemplateById().replace(Constant.HOST, docusignHost);
		url = url.replace(Constant.TEMPLATEID, templateId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<Object> responceEntity = null;
		try {
			responceEntity = restTemplate.exchange(url, HttpMethod.GET, null, Object.class);
			if (responceEntity.getStatusCode() == HttpStatus.OK) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.TEMPLATE_LIST_RESPONSE, responceEntity.getBody()),
						"TemplateList Fetched Successfully");
			} else {
				return new CommonResponse(responceEntity.getStatusCode(),
						new Response(Constant.TEMPLATE_LIST_RESPONSE, null),
						Constant.HTTP_STATUS_CODE + responceEntity.getStatusCodeValue());
			}
		} catch (HttpClientErrorException.BadRequest ex) {
			String responseBody = ex.getResponseBodyAsString();
			Object errorResponse = null;
			try {
				errorResponse = mapper.readValue(responseBody, Object.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("TemplateListResponce Response", errorResponse), "TemplateListResponce Fetch Failed");
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response(Constant.TEMPLATE_LIST_RESPONSE, null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public CommonResponse listTemplates(String count, String start, String order, String orderBy, String searchText) {
		String url = getDousignUrl().getTemplates().replace(Constant.HOST, docusignHost);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		builder.queryParam("count", count);
		builder.queryParam("start", start);
		builder.queryParam("order", order);
		builder.queryParam("orderBy", orderBy);
		builder.queryParam("searchText", searchText);
		builder.queryParam("folderId", saaspefolderId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<Object> responceEntity = null;
		try {
			responceEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, null, Object.class);
		} catch (HttpClientErrorException.BadRequest e) {
			return new CommonResponse(HttpStatus.BAD_REQUEST, new Response("TemplateResponce", ""), e.getMessage());
		}

		return new CommonResponse(HttpStatus.OK, new Response("", responceEntity.getBody()),
				"Template details fetched successfully");
	}

	@Override
	public CommonResponse getAllTemplates() {
		List<CreateTemplate> templates = createTemplateRepository.findAll();
		return new CommonResponse(HttpStatus.OK, new Response("TemplateDetailsResponse", templates),
				"Template details fetched successfully");
	}

	@Override
	public CommonResponse getTemplateDocument(String templateId, String documentId) {
		ObjectMapper mapper = new ObjectMapper();
		String url = getDousignUrl().getTemplateDocuments().replace(Constant.HOST, docusignHost);
		url = url.replace(Constant.TEMPLATEID, templateId);
		url = url.replace("{documentId}", documentId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
					byte[].class);
			if (response.getStatusCode() == HttpStatus.OK) {
				return new CommonResponse(HttpStatus.OK,
						new Response(Constant.TEMPLATE_DOCUMENT_RESPONSE, response.getBody()),
						"TemplateDocument Fetched Successfully");
			} else {
				return new CommonResponse(response.getStatusCode(),
						new Response(Constant.TEMPLATE_DOCUMENT_RESPONSE, null),
						Constant.HTTP_STATUS_CODE + response.getStatusCodeValue());
			}
		} catch (HttpClientErrorException.BadRequest ex) {
			String responseBody = ex.getResponseBodyAsString();
			Object errorResponse = null;
			try {
				errorResponse = mapper.readValue(responseBody, Object.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.TEMPLATE_DOCUMENT_RESPONSE, errorResponse), "TemplateDocument Fetch Failed");
		} catch (RestClientException ex) {
			ex.printStackTrace();
			return new CommonResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					new Response(Constant.TEMPLATE_DOCUMENT_RESPONSE, null), Constant.INTERNAL_SERVER_ERROR);
		}
	}

	public String generateRandomString(int length) {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int randomIndex = random.nextInt(characters.length());
			sb.append(characters.charAt(randomIndex));
		}
		return sb.toString();
	}

}
