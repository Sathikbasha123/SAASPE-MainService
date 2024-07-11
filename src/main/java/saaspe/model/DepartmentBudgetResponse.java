package saaspe.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DepartmentBudgetResponse {

	private String name;

	private BigDecimal totalSpend;

	private BigDecimal adminCost;

	private String currency;

	public DepartmentBudgetResponse() {
		this.totalSpend = BigDecimal.valueOf(0);
	}

}
