package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ApplicationOnboardingListViewResponse extends Response {

	private String requestId;
	private String childRequestId;
	private String applicationLogo;
	private String applicationName;
	private String onboardedByEmail;
	private String reviewedByEmail;
	private String onboardingStatus;

}
