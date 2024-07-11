package saaspe.model;

import lombok.Data;

@Data
public class BudgetAlertDetailRequest {
	private String type;
	private String threshold;

}
