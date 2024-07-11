package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Clm {
	private boolean enabled;
	private boolean consentGiven;
	private String consentUrl;
	// @JsonIgnore
	private String error;
}
