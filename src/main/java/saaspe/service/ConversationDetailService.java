package saaspe.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.Conversation;
import saaspe.model.ConversationDetailRequest;
import saaspe.model.EnterprisesearchRequest;
import saaspe.model.FeedbackRequest;
import saaspe.model.QueryRequest;

public interface ConversationDetailService {

	CommonResponse createConversation(ConversationDetailRequest conversationdetailrequest, UserLoginDetails userprofile)
			throws DataValidationException;

	CommonResponse getConversations(UserLoginDetails userprofile) throws DataValidationException;

	CommonResponse getChatHistoryBasedOnId(String conversatoinId, UserLoginDetails userprofile)
			throws DataValidationException;

	CommonResponse findByQuery(QueryRequest queryRequest, UserLoginDetails userprofile)
			throws DataValidationException, JsonProcessingException;

	CommonResponse deleteConversation(Conversation conversationIds, UserLoginDetails userprofile)
			throws DataValidationException;

	CommonResponse saveFeedback(FeedbackRequest feedbackrequest, UserLoginDetails userprofile)
			throws DataValidationException;

	CommonResponse findBypromt(EnterprisesearchRequest queryRequest, UserLoginDetails userprofile) throws JsonProcessingException;
}
