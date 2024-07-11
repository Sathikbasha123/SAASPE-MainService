package saaspe.adaptor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(content = Include.NON_NULL)
public class CommonUserResponse {

	@JsonProperty("code")
	 private String code;
	@JsonProperty("details")
	 private Details details;
	@JsonProperty("message")
	 private String message;
	@JsonProperty("status")
	 private String status;
}
