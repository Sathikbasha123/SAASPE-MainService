package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class UserOnboardingListView {

	private String requestId;
	private String childRequestId;
	private String userAvatar;
	private String userName;
	private String onboardedByEmail;
	private String reviewedByEmail;
	private String onboardingStatus;

}
