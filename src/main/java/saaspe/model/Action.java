package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class Action {

	private String actionId;
	private String budgetName;
	private String notificationType;
	private String actionType;
	private ActionThreshold actionThresholdType;
	private String definition;
	private String executionRoleArn;
	private String approvalModel;
	private String status;
	private List<Subscribers> subscribers;

}
