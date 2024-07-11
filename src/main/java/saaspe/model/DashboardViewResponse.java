package saaspe.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardViewResponse {

	private Integer applications;

	private Integer subscriptions;

	private Integer renewals;

	private BigDecimal totalSpendYTD;
	
	private BigDecimal totalSpend;
	
	private BigDecimal adminCost;
	
	private BigDecimal adminCostYTD;

}
