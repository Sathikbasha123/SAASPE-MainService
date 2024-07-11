package saaspe.docusign.service.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import saaspe.configuration.mongo.SequenceGeneratorService;
import saaspe.constant.Constant;
import saaspe.docusign.document.AuditEventDocument;
import saaspe.docusign.document.ClmContractDocument;
import saaspe.docusign.document.EnvelopeDocument;
import saaspe.docusign.document.EventDocument;
import saaspe.docusign.model.AuditEvent;
import saaspe.docusign.model.AuditEventsResponse;
import saaspe.docusign.model.DocumentResponse;
import saaspe.docusign.model.DocusignUrls;
import saaspe.docusign.model.EnvelopeResponse;
import saaspe.docusign.model.EventData;
import saaspe.docusign.model.EventFieldsList;
import saaspe.docusign.model.EventLogs;
import saaspe.docusign.repository.AuditEventRepository;
import saaspe.docusign.repository.ClmContractDocumentRepository;
import saaspe.docusign.repository.EnvelopeRepository;
import saaspe.docusign.repository.EventRepository;
import saaspe.docusign.service.EventService;
import saaspe.model.CommonResponse;
import saaspe.model.Response;

@Service
public class EventServiceImpl implements EventService {

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private SequenceGeneratorService sequenceGeneratorService;

	@Autowired
	private EnvelopeRepository envelopeRepository;

	@Autowired
	private AuditEventRepository auditEventRepository;

	@Autowired
	private ClmContractDocumentRepository clmContractDocumentRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${docusign-urls-file}")
	private String docusignUrls;

	@Value("${docusign.host.url}")
	private String docusignHost;

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
	public String handleEvent(String body) throws JsonProcessingException, ParseException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectMapper objectMapper = new ObjectMapper();
		EventLogs eventLogs = objectMapper.readValue(body, EventLogs.class);
		EventDocument document = new EventDocument();
		document.setId(sequenceGeneratorService.generateSequence(EventDocument.SEQUENCE_NAME));
		document.setAmigoId(sequenceGeneratorService.generateCommonSequence(Constant.SEQUENCE_NAME));
		document.setCreatedOn(new Date());
		document.setStartDate(new Date());
		document.setEvent(eventLogs.getEvent());
		document.setUri(eventLogs.getUri());
		document.setApiVersion(eventLogs.getApiVersion());
		document.setConfigurationId(eventLogs.getConfigurationId());
		document.setGeneratedDateTime(eventLogs.getGeneratedDateTime());
		document.setRetryCount(eventLogs.getRetryCount());
		EventData logs = new EventData();
		logs.setAccountId(eventLogs.getData().getAccountId());
		logs.setCreated(eventLogs.getData().getCreated());
		logs.setEnvelopeId(eventLogs.getData().getEnvelopeId());
		logs.setName(eventLogs.getData().getName());
		logs.setRecipientId(eventLogs.getData().getRecipientId());
		logs.setTemplateId(eventLogs.getData().getTemplateId());
		logs.setUserId(eventLogs.getData().getUserId());
		document.setData(logs);
		document.setBuID("BUID");
		document.setOpID(Constant.SAASPE);
		eventRepository.save(document);
		if (eventLogs.getData().getEnvelopeId() != null) {
			EnvelopeDocument existingEnvelopeDocument = envelopeRepository
					.findByEnvelopeId(eventLogs.getData().getEnvelopeId());
			String getEnvelopeById = getDousignUrl().getGetEnvelopeById().replace(Constant.HOST, docusignHost)
					+ eventLogs.getData().getEnvelopeId();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> httpEntity = new HttpEntity<>(headers);
			ResponseEntity<EnvelopeResponse> envelopeDataResponse = null;
			try {
				envelopeDataResponse = restTemplate.exchange(getEnvelopeById, HttpMethod.GET, httpEntity,
						EnvelopeResponse.class);
			} catch (HttpClientErrorException.BadRequest ex) {
				return "Unable to save updated envelope from event";
			}
			ClmContractDocument contractstatus = clmContractDocumentRepository
					.findByEnvelopeId(eventLogs.getData().getEnvelopeId());
			if (contractstatus != null) {
				String json1 = mapper.writeValueAsString(envelopeDataResponse.getBody().getEnvelope());
				JsonNode rootNode = mapper.readTree(json1);
				contractstatus.setStatus(rootNode.path("status").asText());
				String lastModifiedText = rootNode.path("lastModifiedDateTime").asText();
				String completedText = rootNode.path("completedDateTime").asText();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				Date lastModifiedDate = null;
				lastModifiedDate = dateFormat.parse(lastModifiedText);
				Date completedDate = dateFormat.parse(completedText);
				contractstatus.setStartDate(dateFormat.parse(rootNode.path("createdDateTime").asText()));
				contractstatus.setLastModifiedDateTime(lastModifiedDate != null ? lastModifiedDate : null);
				contractstatus.setCompletedDate(completedDate != null ? completedDate : null);
				clmContractDocumentRepository.save(contractstatus);
			}
			EnvelopeDocument envelopeDocument = new EnvelopeDocument();
			if (existingEnvelopeDocument == null) {
				envelopeDocument.setId(sequenceGeneratorService.generateSequence(EnvelopeDocument.SEQUENCE_NAME));
				envelopeDocument.setAmigoId(sequenceGeneratorService.generateSequence(Constant.SEQUENCE_NAME));
				envelopeDocument.setEnvelopeId(eventLogs.getData().getEnvelopeId());
				envelopeDocument.setCreatedOn(new Date());
				envelopeDocument.setEnvelope(envelopeDataResponse.getBody().getEnvelope());
				List<DocumentResponse> documentResponses = addDocumentResponse(envelopeDataResponse);
				envelopeDocument.setDocuments(documentResponses);
				envelopeDocument.setStartDate(new Date());
				envelopeDocument.setBuID("BUID");
				envelopeDocument.setOpID(Constant.SAASPE);
				envelopeRepository.save(envelopeDocument);
			} else {
				existingEnvelopeDocument.setUpdatedOn(new Date());
				existingEnvelopeDocument.setEnvelope(envelopeDataResponse.getBody().getEnvelope());
				List<DocumentResponse> documentResponses = new ArrayList<>();
				for (DocumentResponse documentResponse : envelopeDataResponse.getBody().getDocuments()) {
					DocumentResponse docResponse = new DocumentResponse();
					docResponse.setDocumentId(documentResponse.getDocumentId());
					docResponse.setDocumentIdGuid(documentResponse.getDocumentIdGuid());
					docResponse.setName(documentResponse.getName());
					docResponse.setDocumentBase64(documentResponse.getDocumentBase64());
					documentResponses.add(documentResponse);
				}
				existingEnvelopeDocument.setDocuments(documentResponses);
				envelopeRepository.save(existingEnvelopeDocument);
			}
		}
		return null;
	}

	@Override
	public CommonResponse getAuditEventsFromDocusign() throws JsonProcessingException {
		int pageSize = 100;
		int page = 0;
		ObjectMapper objectMapper = new ObjectMapper();
		ResponseEntity<AuditEvent> response = null;
		Page<EnvelopeDocument> envelopePage;
		do {
			envelopePage = envelopeRepository.findAll(PageRequest.of(page, pageSize));
			List<EnvelopeDocument> envelopeDocuments = envelopePage.getContent();
			for (EnvelopeDocument document : envelopeDocuments) {
				AuditEventDocument eventDocument = auditEventRepository.findByenvelopeId(document.getEnvelopeId());
				String url = getDousignUrl().getAuditLogs().replace(Constant.HOST, docusignHost)
						+ document.getEnvelopeId();
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				try {
					response = restTemplate.getForEntity(url, AuditEvent.class);
					if (response.getBody() != null) {
						if (eventDocument == null) {
							eventDocument = new AuditEventDocument();
							eventDocument.setAuditEvents(response.getBody().getAuditEvents());
							eventDocument
									.setId(sequenceGeneratorService.generateSequence(AuditEventDocument.SEQUENCE_NAME));
							eventDocument
							.setAmigoId(sequenceGeneratorService.generateCommonSequence(Constant.SEQUENCE_NAME));
							eventDocument.setCreatedOn(new Date());
							eventDocument.setStartDate(new Date());
							eventDocument.setEnvelopeId(document.getEnvelopeId());
							eventDocument.setBuID("BUID");
							eventDocument.setOpID(Constant.SAASPE);
						} else {
							eventDocument.setEnvelopeId(document.getEnvelopeId());
							eventDocument.setAuditEvents(response.getBody().getAuditEvents());
							eventDocument.setUpdatedOn(new Date());
						}
						auditEventRepository.save(eventDocument);
					}
				} catch (HttpClientErrorException.BadRequest ex) {
					String responseBody = ex.getResponseBodyAsString();
					Object errorResponse = objectMapper.readValue(responseBody, AuditEvent.class);
					return new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response("CLM Contract Response", errorResponse), "Contract Creation Failed");
				}

				AuditEventDocument auditDocCheckStatus = auditEventRepository
						.findByenvelopeId(document.getEnvelopeId());
				if (auditDocCheckStatus != null) {
					boolean isCompleted = auditDocCheckStatus.getAuditEvents().stream()
							.flatMap(event -> event.getEventFields().stream())
							.anyMatch(field -> field.getName().equalsIgnoreCase("EnvelopeStatus")
									&& field.getValue().equalsIgnoreCase("completed"));

					if (isCompleted) {
						checkEnvelopeStatus(document.getEnvelopeId());
					}
				}
			}
			page++;
		} while (envelopePage.hasNext());

		return new CommonResponse(HttpStatus.BAD_REQUEST, new Response("CLM Contract Response", response),
				"Contract Creation Failed");
	}

	private void checkEnvelopeStatus(String envelopeId) {
		String getEnvelopeById = getDousignUrl().getGetEnvelopeById().replace(Constant.HOST, docusignHost) + envelopeId;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<?> httpEntity = new HttpEntity<>(headers);

		try {
			ResponseEntity<EnvelopeResponse> envelopeDataResponse = restTemplate.exchange(getEnvelopeById,
					HttpMethod.GET, httpEntity, EnvelopeResponse.class);
			EnvelopeDocument existingEnvelopeDocument = envelopeRepository.findByEnvelopeId(envelopeId);
			if (existingEnvelopeDocument != null) {
				existingEnvelopeDocument.setUpdatedOn(new Date());
				existingEnvelopeDocument.setEnvelope(envelopeDataResponse.getBody().getEnvelope());
				List<DocumentResponse> documentResponses = new ArrayList<>();
				for (DocumentResponse documentResponse : envelopeDataResponse.getBody().getDocuments()) {
					DocumentResponse docResponse = new DocumentResponse();
					docResponse.setDocumentId(documentResponse.getDocumentId());
					docResponse.setDocumentIdGuid(documentResponse.getDocumentIdGuid());
					docResponse.setName(documentResponse.getName());
					docResponse.setDocumentBase64(documentResponse.getDocumentBase64());
					documentResponses.add(docResponse);
				}
				existingEnvelopeDocument.setDocuments(documentResponses);
				envelopeRepository.save(existingEnvelopeDocument);
			}
		} catch (HttpClientErrorException.BadRequest ex) {
			ex.printStackTrace();
		}
	}

	@Data
	public static class AuditEventsListResponse {
		private String envelopeId;
		private List<AuditEventsResponse> eventFields;
	}

	@Override
	public CommonResponse getAuditEvents(String envelopeId) {
		AuditEventDocument auditEventDocument = auditEventRepository.findByenvelopeId(envelopeId);
		List<EventFieldsList> fieldsLists = auditEventDocument.getAuditEvents();
		AuditEventsListResponse auditEventsListResponse = new AuditEventsListResponse();
		List<AuditEventsResponse> auditEventsResponsesList = new ArrayList<>();
		for (EventFieldsList eventField : fieldsLists) {
			for (int i = 0; i < eventField.getEventFields().size(); i += 12) {
				AuditEventsResponse auditEventsResponse = new AuditEventsResponse();
				auditEventsResponse.setLogTime(eventField.getEventFields().get(i).getValue());
				auditEventsResponse.setSource(eventField.getEventFields().get(i + 1).getValue());
				auditEventsResponse.setUserName(eventField.getEventFields().get(i + 2).getValue());
				auditEventsResponse.setUserId(eventField.getEventFields().get(i + 3).getValue());
				auditEventsResponse.setAction(eventField.getEventFields().get(i + 4).getValue());
				auditEventsResponse.setMessage(eventField.getEventFields().get(i + 5).getValue());
				auditEventsResponse.setEnvelopeStatus(eventField.getEventFields().get(i + 6).getValue());
				auditEventsResponse.setClientIPAddress(eventField.getEventFields().get(i + 7).getValue());
				auditEventsResponse.setInformation(eventField.getEventFields().get(i + 8).getValue());
				auditEventsResponse.setInformationLocalized(eventField.getEventFields().get(i + 9).getValue());
				auditEventsResponse.setGeoLocation(eventField.getEventFields().get(i + 10).getValue());
				auditEventsResponse.setLanguage(eventField.getEventFields().get(i + 11).getValue());
				auditEventsResponsesList.add(auditEventsResponse);
			}
		}
		auditEventsListResponse.setEnvelopeId(envelopeId);
		auditEventsListResponse.setEventFields(auditEventsResponsesList);
		return new CommonResponse(HttpStatus.OK, new Response("AuditEventResponse", auditEventsListResponse),
				"Details Retrieved Successfully");
	}

	private List<DocumentResponse> addDocumentResponse(ResponseEntity<EnvelopeResponse> envelopeDataResponse) {
		List<DocumentResponse> documentResponses = new ArrayList<>();
		for (DocumentResponse documentResponse : envelopeDataResponse.getBody().getDocuments()) {
			DocumentResponse docResponse = new DocumentResponse();
			docResponse.setDocumentId(documentResponse.getDocumentId());
			docResponse.setDocumentIdGuid(documentResponse.getDocumentIdGuid());
			docResponse.setName(documentResponse.getName());
			docResponse.setDocumentBase64(documentResponse.getDocumentBase64());
			documentResponses.add(documentResponse);
		}
		return documentResponses;
	}

//	private void saveAuditEvent(ResponseEntity<AuditEvent> response, AuditEventDocument auditEventDocument,
//			EnvelopeDocument document) {
//		if (response.getBody() != null) {
//			auditEventDocument.setAuditEvents(response.getBody().getAuditEvents());
//			auditEventDocument.setId(sequenceGeneratorService.generateSequence(AuditEventDocument.SEQUENCE_NAME));
//			auditEventDocument.setCreatedOn(new Date());
//			auditEventDocument.setStartDate(new Date());
//			auditEventDocument.setEnvelopeId(document.getEnvelopeId());
//			auditEventRepository.save(auditEventDocument);
//		}
//	}

}
