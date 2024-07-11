package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class DepartmentOnboardingRequestDetailViewResponse {
	
	private CreateDepartmentDetails departmentDetailsInfo;

	private DepartmentReviewerDetails reviewerDetails;

}
