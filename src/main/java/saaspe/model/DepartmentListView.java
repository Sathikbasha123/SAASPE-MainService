package saaspe.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class DepartmentListView {

	private String departmentName;

	private String departmentOwner;

	private Integer noOfUsers;

	private Integer noOfApps;

	private BigDecimal budgets;

	private String departmentId;

	private String userLogo;

	private Integer projectsCount;

	private List<String> departmentOwnerEmail;

	private List<CreateDepartmentOwnerDetails> ownerDetails;

	private BigDecimal totalSpend;

	private BigDecimal totalSpendYTD;

	private BigDecimal adminCostYTD;

	private BigDecimal adminCost;
	
	private String budgetCurrency;

	public DepartmentListView() {
		this.adminCostYTD = BigDecimal.valueOf(0.0);
		this.adminCost = BigDecimal.valueOf(0.0);
	}

}
