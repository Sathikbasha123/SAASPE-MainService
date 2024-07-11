package saaspe.model;

import lombok.Data;

@Data
public class ChatHistoryResponse {

	private String conversationId;

	private String query;

	private AskamigoResponse response;

	private Boolean like;

	private String comments;

	private Long id;

	

}
