package saaspe.model;

import lombok.Data;

@Data
public class QueryRequest {

	private String query;

	private String conversationId;

	private String text_lag2;

	private String raw_response_lag2;

	private String text_lag1;

	private String raw_response_lag1;

}
