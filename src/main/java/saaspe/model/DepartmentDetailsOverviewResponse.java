package saaspe.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDetailsOverviewResponse {

	private String departmentName;
	private BigDecimal departmentAvgMonthlySpend;
	private BigDecimal departmentAvgMonthlyAdminCost;
	private Integer departmentUserCount;
	private Integer departmentApplicationCount;
	private String currencySymbol;
	private Integer projectsCount;

}
