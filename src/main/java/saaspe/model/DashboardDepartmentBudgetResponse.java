package saaspe.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class DashboardDepartmentBudgetResponse {
	private BigDecimal totalBudget;
	private String currency = "USD";
	private List<DepartmentBudgetResponse> quarter;
	@JsonIgnore
	private DepartmentBudgetResponse pastQuarter;
	@JsonIgnore
	private DepartmentBudgetResponse currentQuarter;
	@JsonIgnore
	private DepartmentBudgetResponse nextQuarter;

	public DashboardDepartmentBudgetResponse() {
		this.totalBudget = BigDecimal.valueOf(0);
	}
}
