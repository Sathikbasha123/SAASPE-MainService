package saaspe.model;

import lombok.Data;

@Data
public class ProjectWorkflowDetailsView {
	private SingleProjectOnboardingRequest projectDetailsInfo;
	private ProjectWorkflowReviewerDetailsview reviewerDetails;

}
