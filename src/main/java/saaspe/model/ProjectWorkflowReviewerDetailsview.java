package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ProjectWorkflowReviewerDetailsview {

	private String workGroupName;

	private String approvedByEmail;

	private String comments;

	private String approvalTimeStamp;
}
