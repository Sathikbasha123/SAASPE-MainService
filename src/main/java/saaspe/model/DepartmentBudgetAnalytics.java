package saaspe.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DepartmentBudgetAnalytics {
	private BigDecimal spend;

	private BigDecimal remaining;

	private String currency;

	private String month;
}
