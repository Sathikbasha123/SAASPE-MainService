package saaspe.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MulticloudBudgetResponse extends Response {

	private BigDecimal budget;

	private String budgetName;

	private String currency;

	private String scope;

	private String resetPeriod;

	// @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private String creationDate;

	// @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private String expirationDate;

	private BigDecimal percentageConsumed;

}
