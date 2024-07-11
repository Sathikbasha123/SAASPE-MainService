package saaspe.model;

import lombok.Data;

@Data
public class Notification {

	private String notificationType;
	private String comparisonOperator;
	private Double threshold;
	private String thresholdType;
	private String notificationState;

}
