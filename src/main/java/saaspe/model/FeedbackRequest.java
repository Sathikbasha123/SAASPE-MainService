package saaspe.model;

import lombok.Data;

@Data
public class FeedbackRequest {

	private long id;
	private String conversationId;
	private Boolean like;
	private String comment;
}
