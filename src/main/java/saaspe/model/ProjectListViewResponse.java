package saaspe.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ProjectListViewResponse {

	private String projectId;
	private String projectCode;
	private String projectName;
	private List<String> projectManagerEmail;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date projectStartDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date projectEndDate;
	private BigDecimal projctBudget;
	private String projctBudgetCurrency;
	private BigDecimal projectCost;
	private BigDecimal adminCost;
	private String currency;

}
