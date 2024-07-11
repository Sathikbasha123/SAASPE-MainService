package saaspe.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DashBoardDepartmentExpenseResponse {
	private String category;

	private BigDecimal value;

	private BigDecimal adminCost;

	private String currency;

}
