package saaspe.docusign.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SignatureType {

	@JsonProperty("isDefault")
	private String isDefault = null;

	@JsonProperty("type")
	private String type = null;

}
