package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AskamigoResponse {
	private String raw_response;
	private Object table;
	private String text;
	private boolean _table;

}
