package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class BudgetRequest {

	private String budgetScope;
	private String budgetName;
	private String resetPeriod;
	private String creationDate;
	private String expiryDate;
	private BudgetAmount budgetAmount;
	private String recipientEmail;
	private String languagePreference;
	private List<BudgetAlertDetailRequest> alertDetails;

}
