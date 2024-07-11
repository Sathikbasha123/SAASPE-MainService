package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ApplicatoinsLogoResponse extends Response {

	private String applicationName;
	private String applicationDescription;
	private String logoURL;
	private String providerID;
	private String applicationPageURL;

}
