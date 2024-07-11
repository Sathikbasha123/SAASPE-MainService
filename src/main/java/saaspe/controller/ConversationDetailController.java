package saaspe.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.constant.Constant;
import saaspe.entity.UserLoginDetails;
import saaspe.model.CommonResponse;
import saaspe.model.Conversation;
import saaspe.model.ConversationDetailRequest;
import saaspe.model.EnterprisesearchRequest;
import saaspe.model.FeedbackRequest;
import saaspe.model.QueryRequest;
import saaspe.model.Response;
import saaspe.service.ConversationDetailService;

@RestController
@RequestMapping("api/v1/conversation")
@ControllerLogging
public class ConversationDetailController {

	@Autowired
	private ConversationDetailService conversationdetailservice;

	private static final Logger log = LoggerFactory.getLogger(ConversationDetailController.class);

	@PostMapping("/create")
	public ResponseEntity<CommonResponse> createConversation(
			@RequestBody ConversationDetailRequest conversationdetailrequest, Authentication authentication) {
		try {
			log.info("*** in create conv ");
			UserLoginDetails userprofile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = conversationdetailservice.createConversation(conversationdetailrequest,
					userprofile);
			return new ResponseEntity<>(response, response.getStatus());
		} catch (Exception e) {
			log.error("*** Ending createConversation method with an error ***", e);
			return new ResponseEntity<>(
					new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response(Constant.CONVERSATION_DETAIL_RESPONSE, new ArrayList<>()), e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping("/fetch/byuser")
	public ResponseEntity<CommonResponse> getConversations(Authentication authentication) {
		try {
			UserLoginDetails userprofile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = conversationdetailservice.getConversations(userprofile);
			return new ResponseEntity<>(response, response.getStatus());

		} catch (Exception e) {
			log.error("*** Ending getConversations method with an error ***", e);
			return new ResponseEntity<>(
					new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response(Constant.CONVERSATION_DETAIL_RESPONSE, new ArrayList<>()), e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("history/{conversatoinId}")
	public ResponseEntity<CommonResponse> getChatHistoryBasedOnId(@PathVariable String conversatoinId,
			Authentication authentication) {
		try {
			UserLoginDetails userprofile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = conversationdetailservice.getChatHistoryBasedOnId(conversatoinId, userprofile);
			return new ResponseEntity<>(response, response.getStatus());

		} catch (Exception e) {
			log.error("*** Ending getChatHistoryBasedOnId method with an error ***", e);
			return new ResponseEntity<>(
					new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response(Constant.CONVERSATION_DETAIL_RESPONSE, new ArrayList<>()), e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/query")
	public ResponseEntity<CommonResponse> findByQuery(@RequestBody QueryRequest queryRequest,
			Authentication authentication,@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			UserLoginDetails userprofile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = conversationdetailservice.findByQuery(queryRequest, userprofile);
			return new ResponseEntity<>(response, response.getStatus());

		} catch (Exception e) {
			log.error("*** Ending findByQuery method with an error ***", e);
			return new ResponseEntity<>(
					new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response(Constant.CONVERSATION_DETAIL_RESPONSE, new ArrayList<>()), e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/findbyprompt")
	public ResponseEntity<CommonResponse> findBypromt(@RequestBody EnterprisesearchRequest queryRequest,
			Authentication authentication) {
		try {
			UserLoginDetails userprofile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = conversationdetailservice.findBypromt(queryRequest, userprofile);
			return new ResponseEntity<>(response, response.getStatus());

		} catch (Exception e) {
			log.error("*** Ending findByQuery method with an error ***", e);
			return new ResponseEntity<>(
					new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response(Constant.CONVERSATION_DETAIL_RESPONSE, new ArrayList<>()), e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/remove")
	public ResponseEntity<CommonResponse> deleteConversation(@RequestBody Conversation conversationIds,
			Authentication authentication) {
		try {
			UserLoginDetails userprofile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = conversationdetailservice.deleteConversation(conversationIds, userprofile);
			return new ResponseEntity<>(response, response.getStatus());

		} catch (Exception e) {
			log.error("*** Ending deleteConversation method with an error ***", e);
			return new ResponseEntity<>(
					new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response("ConversationDeleteResponse", new ArrayList<>()), e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping("/feedback")
	public ResponseEntity<CommonResponse> saveFeedback(@RequestBody FeedbackRequest feedbackrequest,
			Authentication authentication,@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			UserLoginDetails userprofile = (UserLoginDetails) authentication.getPrincipal();
			CommonResponse response = conversationdetailservice.saveFeedback(feedbackrequest, userprofile);
			return new ResponseEntity<>(response, response.getStatus());

		} catch (Exception e) {
			log.error("*** Ending saveFeedback method with an error ***", e);
			return new ResponseEntity<>(
					new CommonResponse(HttpStatus.BAD_REQUEST,
							new Response("ConversationFeedbackResponse", new ArrayList<>()), e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}

	}
}
