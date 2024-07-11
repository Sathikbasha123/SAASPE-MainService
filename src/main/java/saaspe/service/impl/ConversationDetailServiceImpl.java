package saaspe.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import saaspe.cloud.repository.ConversationDocumentRepository;
import saaspe.configuration.mongo.SequenceGeneratorService;
import saaspe.constant.Constant;
import saaspe.document.ConversationDocument;
import saaspe.entity.ConversationDetails;
import saaspe.entity.SequenceGenerator;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.AskamigoResponse;
import saaspe.model.ChatHistoryResponse;
import saaspe.model.CommonResponse;
import saaspe.model.Conversation;
import saaspe.model.ConversationDetailRequest;
import saaspe.model.EnterpriseSearchResponse;
import saaspe.model.EnterprisesearchRequest;
import saaspe.model.FeedbackRequest;
import saaspe.model.PromptResponse;
import saaspe.model.QueryRequest;
import saaspe.model.Response;
import saaspe.repository.ConversationDetailsRepository;
import saaspe.repository.SequenceGeneratorRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.service.ConversationDetailService;

@Service
public class ConversationDetailServiceImpl implements ConversationDetailService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private UserDetailsRepository userdetailrepository;

	@Autowired
	private SequenceGeneratorRepository generatorRepository;

	@Autowired
	private ConversationDetailsRepository conversationdetailrepository;

	@Autowired
	private ConversationDocumentRepository conversationDocumentRepository;

	@Autowired
	private SequenceGeneratorService sequenceGeneratorService;

	@Value("${conversation.url}")
	private String convoUrl;

	@Value("${ai.host.url}")
	private String aiHost;

	@Value("${enterprisesearch.host.url}")
	private String enterpriseSearchHost;

	@Value("${enterprisesearch.endpoint.url}")
	private String searchEndpoint;

	@Value("${saaspe.opid}")
	private String opID;

	@Value("${saaspe.buid}")
	private String buID;
	
	@Override
	public CommonResponse createConversation(ConversationDetailRequest conversationdetailrequest,
			UserLoginDetails userprofile) throws DataValidationException {
		if (conversationdetailrequest.getConversationTitle().trim().length() == 0
				|| conversationdetailrequest.getConversationTitle() == null) {
			throw new DataValidationException("Title must not be null or  Please provide valid length of the title",
					null, null);
		}

		UserDetails user = userdetailrepository.findByuserEmail(userprofile.getEmailAddress());
		ConversationDetails existingconversationvalidation = conversationdetailrepository
				.findByUserIdAndConversationTitle(user.getUserId(), conversationdetailrequest.getConversationTitle());
		if (existingconversationvalidation != null) {
			throw new DataValidationException(
					"Conversation with same title already exists for username " + user.getUserName(), null, null);
		}
		ConversationDetails condetails = new ConversationDetails();
		String convId = "CONV_0";
		Integer sequence = generatorRepository.getconversationOnboardingSequence();
		convId = convId.concat(sequence.toString());
		SequenceGenerator updateSequence = generatorRepository.getById(1);
		updateSequence.setConversationsequenceId(++sequence);
		generatorRepository.save(updateSequence);
		condetails.setConversationId(convId);
		condetails.setConversationTitle(conversationdetailrequest.getConversationTitle());
		condetails.setUserId(user.getUserId());
		condetails.setUserEmail(userprofile.getEmailAddress());
		condetails.setCreatedOn(new Date());
		condetails.setOpid("SAASPE");
		condetails.setBuid("BUID");
		condetails.setStartDate(new Date());
		conversationdetailrepository.save(condetails);
		Map<String, String> data = new HashMap<>();
		data.put("conversationId", convId);
		return new CommonResponse(HttpStatus.OK, new Response("ConversationDetailsResponse", data),
				"Conversation Details Saved Successfully");
	}

	@Override
	public CommonResponse getConversations(UserLoginDetails userprofile) throws DataValidationException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		List<ConversationDetails> conversationdetails = conversationdetailrepository
				.getConversationByUserEmail(userprofile.getEmailAddress());
		List<Conversation> list = new ArrayList<>();
		if (conversationdetails != null) {
			for (ConversationDetails conversation : conversationdetails) {
				Conversation convo = new Conversation();
				convo.setConversationId(conversation.getConversationId());
				convo.setConversationName(conversation.getConversationTitle());
				convo.setCreatedOn(conversation.getCreatedOn());
				list.add(convo);
			}
			response.setData(list);
			response.setAction("ConversationDetailResponse");
			commonResponse.setStatus(HttpStatus.OK);
			commonResponse.setMessage("Details retrieved successfully");
			commonResponse.setResponse(response);
		} else
			throw new DataValidationException("No Conversation available to display for the user "
					+ userprofile.getFirstName() + userprofile.getLastName(), "404", HttpStatus.NOT_FOUND);
		return commonResponse;
	}

	@Override
	public CommonResponse getChatHistoryBasedOnId(String conversatoinId, UserLoginDetails userprofile)
			throws DataValidationException {
		ConversationDetails conversationDetails = conversationdetailrepository
				.findByConversationId(conversatoinId.trim());
		if (conversationDetails == null) {
			throw new DataValidationException("Invalid Conversation Id!", null, null);
		}
		List<ConversationDocument> conversationDocuments = conversationDocumentRepository
				.findByConversationId(conversatoinId);
		List<ChatHistoryResponse> chatHistoryResponses = new ArrayList<>();
		for (ConversationDocument document : conversationDocuments) {
			ChatHistoryResponse chatHistoryResponse = new ChatHistoryResponse();
			chatHistoryResponse.setConversationId(document.getConversationId());
			chatHistoryResponse.setQuery(document.getQuery());
			chatHistoryResponse.setId(document.getId());
			AskamigoResponse response=new AskamigoResponse();
			response.set_table(document.getResponse().is_table());
			response.setTable(document.getResponse().getTable());
			response.setText(document.getResponse().getText());
			chatHistoryResponse.setResponse(response);
			chatHistoryResponse.setLike(document.getLike());
			chatHistoryResponse.setComments(document.getComments());
			chatHistoryResponses.add(chatHistoryResponse);
		}
		return new CommonResponse(HttpStatus.OK,
				new Response("ConversationDetailsHistoryResposne", chatHistoryResponses),
				"Conversation Details Retrived Successfully");
	}

	@Override
	public CommonResponse findByQuery(QueryRequest queryRequest, UserLoginDetails userprofile)
			throws DataValidationException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		UserDetails user = userdetailrepository.findByuserEmail(userprofile.getEmailAddress());
		checkQueryRequest(queryRequest, userprofile.getEmailAddress());
		List<ConversationDocument> firstCheckDocument = conversationDocumentRepository
				.findByConversationId(queryRequest.getConversationId());
		List<ConversationDocument> documents = new ArrayList<>();
		if (firstCheckDocument.isEmpty()) {
			JSONObject requestJson = new JSONObject();
			requestJson.put("text", queryRequest.getQuery());
			documents = getQueryFromAi(queryRequest, requestJson, user, userprofile);
		}
		int documentSize = firstCheckDocument.size();
		if (!firstCheckDocument.isEmpty()) {
			List<ConversationDocument> sprtedDocs = firstCheckDocument.stream()
					.sorted((d1, d2) -> d2.getCreatedOn().compareTo(d1.getCreatedOn())).collect(Collectors.toList());
			if (documentSize == 1) {
				ConversationDocument previousDoc = sprtedDocs.get(0);
				JSONObject firstQueryExist = new JSONObject();
				firstQueryExist.put("text", queryRequest.getQuery());
				String json = mapper.writeValueAsString(previousDoc.getResponse());
				JsonNode rootNode = mapper.readTree(json);
				int textLag1 = rootNode.get("text").asInt();
				String rawResponseLag1 = rootNode.get(Constant.RAW_RESPONSE).asText();
				firstQueryExist.put("text_lag1", textLag1);
				firstQueryExist.put("raw_response_lag1", rawResponseLag1);
				documents = getQueryFromAi(queryRequest, firstQueryExist, user, userprofile);
			} else if (documentSize >= 2) {
				JSONObject secondQueryExist = new JSONObject();
				secondQueryExist.put("text", queryRequest.getQuery());
				ConversationDocument previous1Doc = sprtedDocs.get(0);
				String json1 = mapper.writeValueAsString(previous1Doc.getResponse());
				JsonNode rootNode1 = mapper.readTree(json1);
				int textLag1 = rootNode1.get("text").asInt();
				String rawResponseLag1 = rootNode1.get(Constant.RAW_RESPONSE).asText();
				secondQueryExist.put("text_lag1", textLag1);
				secondQueryExist.put("raw_response_lag1", rawResponseLag1);

				ConversationDocument previous2Doc = sprtedDocs.get(1);
				String json2 = mapper.writeValueAsString(previous2Doc.getResponse());
				JsonNode rootNode2 = mapper.readTree(json2);
				int textLag2 = rootNode2.get("text").asInt();
				String rawResponseLag2 = rootNode2.get(Constant.RAW_RESPONSE).asText();
				secondQueryExist.put("text_lag2", textLag2);
				secondQueryExist.put("raw_response_lag2", rawResponseLag2);
				documents = getQueryFromAi(queryRequest, secondQueryExist, user, userprofile);
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("QueryResponse", documents),
				"Query Response Retrived Successfully");
	}

	private List<ConversationDocument> getQueryFromAi(QueryRequest queryRequest, JSONObject josnObject,
			UserDetails user, UserLoginDetails userprofile) throws JsonProcessingException {
		ConversationDocument conversationDocument = new ConversationDocument();
		
		String url = aiHost + convoUrl;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(josnObject.toString(), headers);
	    restTemplate.postForEntity(url, request, String.class);
//		if (response.getStatusCode().is2xxSuccessful()) {
//			Object iterator = mapper.readValue(response.getBody(), Object.class);
//			conversationDocument.setResponse(iterator);
//		} else {
//			conversationDocument.setResponse("Someting went wrong, please come back after some time!!");
//		}
		conversationDocument.setId(sequenceGeneratorService.generateSequence(ConversationDocument.SEQUENCE_NAME));
		conversationDocument.setAmigoId(sequenceGeneratorService.generateCommonSequence(Constant.SEQUENCE_NAME));
		conversationDocument.setUserEmail(userprofile.getEmailAddress());
		conversationDocument.setConversationId(queryRequest.getConversationId());
		conversationDocument.setUserId(user.getUserId());
		conversationDocument.setQuery(queryRequest.getQuery());
		conversationDocument.setCreatedOn(new Date());
		conversationDocument.setStartDate(new Date());
		conversationDocument.setBuID(buID);
		conversationDocument.setOpID(opID);
		conversationDocumentRepository.save(conversationDocument);
		List<ConversationDocument> docs = conversationDocumentRepository
				.findByConversationId(queryRequest.getConversationId());
		return docs.stream().sorted((d1, d2) -> d2.getCreatedOn().compareTo(d1.getCreatedOn()))
				.collect(Collectors.toList());
	}

	private List<ConversationDocument> getQueryFromEnterpriseSearch(EnterprisesearchRequest queryRequest,
			JSONObject jsonObject, UserDetails user, UserLoginDetails userProfile) throws JsonProcessingException {
		ConversationDocument conversationDocument = new ConversationDocument();
		AskamigoResponse response = new AskamigoResponse();

		String url = enterpriseSearchHost + searchEndpoint;
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		builder.queryParam("prompt", queryRequest.getPrompt());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		new HttpEntity<>(jsonObject.toString(), headers);
		try {
			ResponseEntity<EnterpriseSearchResponse> responses = restTemplate.exchange(builder.toUriString(),
					HttpMethod.GET, null, EnterpriseSearchResponse.class);
			if (responses.getStatusCode().is2xxSuccessful()) {
				conversationDocument.setResponse(responses.getBody().getResponse());
			} else if (responses.getStatusCode().is5xxServerError()) {
				response.setText("Please provide valid prompt, try again!!");
				conversationDocument.setResponse(response);
			} else if (responses.getStatusCode() == HttpStatus.BAD_REQUEST) {
				response.setText("Please provide valid prompt, try again!!");
				conversationDocument.setResponse(response);
			} else {
				response.setText("Something went wrong, please come back after some time!!");
				conversationDocument.setResponse(response);
			}
		} catch (Exception e) {
			response.setText("Please provide valid prompt, try again!!");
			conversationDocument.setResponse(response);
		}
		conversationDocument.setId(sequenceGeneratorService.generateSequence(ConversationDocument.SEQUENCE_NAME));
		conversationDocument.setAmigoId(sequenceGeneratorService.generateCommonSequence(Constant.SEQUENCE_NAME));
		conversationDocument.setUserEmail(userProfile.getEmailAddress());
		conversationDocument.setConversationId(queryRequest.getConversationId());
		conversationDocument.setUserId(user.getUserId());
		conversationDocument.setQuery(queryRequest.getPrompt());
		conversationDocument.setCreatedOn(new Date());
		conversationDocument.setStartDate(new Date());
		conversationDocument.setBuID(buID);
		conversationDocument.setOpID(opID);
		conversationDocumentRepository.save(conversationDocument);
		List<ConversationDocument> docs = conversationDocumentRepository
				.findByConversationId(queryRequest.getConversationId());
		return docs.stream().sorted((d1, d2) -> d2.getCreatedOn().compareTo(d1.getCreatedOn()))
				.collect(Collectors.toList());
	}

	private DataValidationException checkQueryRequest(QueryRequest queryRequest, String email)
			throws DataValidationException {
		if (queryRequest.getConversationId().isEmpty() || queryRequest.getQuery().isEmpty()) {
			if (queryRequest.getConversationId().isEmpty()) {
				throw new DataValidationException("Conversation ID is empty", null, null);
			} else {
				throw new DataValidationException("Query is empty", null, null);
			}
		}
		ConversationDetails conversationDetailsCheck = conversationdetailrepository
				.findByConversationId(queryRequest.getConversationId());
		if (conversationDetailsCheck == null) {
			throw new DataValidationException("Invalid Conversation Id!", null, null);
		}
		ConversationDetails conversationDetailsCheckForUser = conversationdetailrepository
				.findByconversationDetailsCheckForUser(queryRequest.getConversationId(), email);
		if (conversationDetailsCheckForUser == null) {
			throw new DataValidationException(
					"Invalid Conversation Request for the user with invalid conversation id!!", null, null);
		}
		return null;
	}

	@Override
	public CommonResponse deleteConversation(Conversation converstaionIds, UserLoginDetails userprofile)
			throws DataValidationException {
		UserDetails user = userdetailrepository.findByuserEmail(userprofile.getEmailAddress());
		List<String> list = converstaionIds.getConversationIds();
		for (String id : list) {
			ConversationDetails conversationDetails = conversationdetailrepository.findByConversationId(id);
			if (conversationDetails == null) {
				throw new DataValidationException("Conversation not found for id: " + id, "404", HttpStatus.NOT_FOUND);
			}
			if (!conversationDetails.getUserEmail().equalsIgnoreCase(userprofile.getEmailAddress())) {
				throw new DataValidationException("Conversation does not belong to the user", "400",
						HttpStatus.BAD_REQUEST);
			} else {
				conversationDetails.setUpdatedOn(new Date());
				conversationDetails.setEndDate(new Date());
				conversationdetailrepository.save(conversationDetails);
				List<ConversationDocument> conversationDoc = conversationDocumentRepository.findByConversationId(id);
				for (ConversationDocument conversation : conversationDoc) {
					if (conversation.getUserEmail().equals(user.getUserEmail())) {
						conversation.setUpdatedOn(new Date());
						conversation.setEndDate(new Date());
						conversationDocumentRepository.save(conversation);
					} else {
						throw new DataValidationException("Conversation does not belong to the user", "400",
								HttpStatus.BAD_REQUEST);
					}
				}
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("RemoveConversationDetails", new ArrayList<>()),
				"Conversation Details deleted successfully");
	}

	@Override
	public CommonResponse saveFeedback(FeedbackRequest feedbackrequest, UserLoginDetails userprofile)
			throws DataValidationException {
		checkSaveFeedback(feedbackrequest);
		ConversationDetails conversation = conversationdetailrepository
				.findByConversationId(feedbackrequest.getConversationId());
		List<ConversationDocument> conversationDocument = conversationDocumentRepository
				.findByConversationId(feedbackrequest.getConversationId());
		if (conversation == null || conversationDocument == null) {
			throw new DataValidationException("Conversation Not found user", "404", HttpStatus.NOT_FOUND);
		}
		UserDetails user = userdetailrepository.findByuserEmail(userprofile.getEmailAddress());
		for (ConversationDocument conversationdocument : conversationDocument) {
			if (conversation.getUserEmail().equalsIgnoreCase(user.getUserEmail())
					&& (conversationdocument.getId().equals(feedbackrequest.getId()))) {
				if (Boolean.TRUE.equals(feedbackrequest.getLike())) {
					conversationdocument.setUpdatedOn(new Date());
					conversationdocument.setLike(true);
					conversationdocument.setBuID(buID);
					conversationdocument.setOpID(opID);
					conversationDocumentRepository.save(conversationdocument);
				} else {
					conversationdocument.setUpdatedOn(new Date());
					conversationdocument.setLike(false);
					conversationdocument.setComments(feedbackrequest.getComment());
					conversationdocument.setBuID(buID);
					conversationdocument.setOpID(opID);
					conversationDocumentRepository.save(conversationdocument);
				}

			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("FeedbackConversationDetails", new ArrayList<>()),
				"Feedback Details saved successfully");
	}

	private void checkSaveFeedback(FeedbackRequest feedbackrequest) throws DataValidationException {
		if (feedbackrequest.getConversationId().isEmpty()) {
			throw new DataValidationException("ConversationId should not be null", null, null);
		} else if (feedbackrequest.getId() == 0) {
			throw new DataValidationException("Invalid id for the conversation", null, null);
		} else {
			if (Boolean.FALSE.equals(feedbackrequest.getLike()) && (feedbackrequest.getComment().isEmpty())) {
				throw new DataValidationException("comment is null", null, null);
			}
		}
	}

	@Override
	public CommonResponse findBypromt(EnterprisesearchRequest queryRequest, UserLoginDetails userprofile)
			throws JsonProcessingException {
		JSONObject requestJson = new JSONObject();
		requestJson.put("promt", queryRequest.getPrompt());
		UserDetails user = userdetailrepository.findByuserEmail(userprofile.getEmailAddress());
		List<ConversationDocument> documents;
		documents = getQueryFromEnterpriseSearch(queryRequest, requestJson, user, userprofile);
		List<PromptResponse> promptResponses = new ArrayList<>();
		for (ConversationDocument doc : documents) {
			PromptResponse promptResponse = new PromptResponse();
			promptResponse.setId(doc.getId());
			promptResponse.setConversationId(doc.getConversationId());
			promptResponse.setUserId(doc.getUserId());
			promptResponse.setUserEmail(doc.getUserEmail());
			promptResponse.setQuery(doc.getQuery());
			AskamigoResponse response=new AskamigoResponse();
			response.set_table(doc.getResponse().is_table());
			response.setTable(doc.getResponse().getTable());
			response.setText(doc.getResponse().getText());
			promptResponse.setResponse(response);
			promptResponse.setCreatedOn(doc.getCreatedOn());
			promptResponse.setUpdatedOn(doc.getUpdatedOn());
			promptResponse.setEndDate(doc.getEndDate());
			promptResponse.setStartDate(doc.getStartDate());
			promptResponse.setLike(doc.getLike());
			promptResponse.setComments(doc.getComments());
			promptResponses.add(promptResponse);
		}
		return new CommonResponse(HttpStatus.OK, new Response("QueryResponse", promptResponses),
				"Conversation Details Retrived Successfully");
	}

}
