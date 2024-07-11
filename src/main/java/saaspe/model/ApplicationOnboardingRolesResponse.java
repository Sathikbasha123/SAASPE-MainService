package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ApplicationOnboardingRolesResponse {

	private String requestID;

	private String applicationLogo;

	private String applicationName;

	private String onboardedByEmail;

}
