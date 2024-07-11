package saaspe.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class DepartmentBudgetAnalyticsResponse {
	private BigDecimal allocatedAmount;

	private String currency;

	private List<DepartmentBudgetAnalytics> data;
}
