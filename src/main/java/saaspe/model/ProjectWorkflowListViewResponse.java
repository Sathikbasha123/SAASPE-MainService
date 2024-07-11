package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class ProjectWorkflowListViewResponse {

	private String requestId;

	private String childRequestId;

	private String projectCode;

	private String projectName;

	private List<String> projectManagerEmail;

	private String onboardedByEmail;

	private String onboardingStatus;

	private String reviewedByEmail;
}
