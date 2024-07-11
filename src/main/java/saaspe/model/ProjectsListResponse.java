package saaspe.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ProjectsListResponse {

	private String projectName;

	private String projectId;

	private String projectCode;

	private List<String> projectManagerEmail;

	private BigDecimal projectBudget;

	private String projectBudgetCurrency;

	private BigDecimal projectCost;

	private BigDecimal projectAdminCost;

	private String departmentId;

	private String departmentName;

	private String currency;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date projectStartDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date projectEndDate;

}
