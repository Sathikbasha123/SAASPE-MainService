package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class DepartmentOnboardingReviewerListViewResponse {
	
	private String requestId;
	private String departmentName;
	private String onBoardedByEmail;
	private String reviewedByEmail;
	private String onboardingStatus;
	private String childRequestId;

}
