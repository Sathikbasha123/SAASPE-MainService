package saaspe.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class SingleProjectOnboardingRequest {

	private String departmentId;
	private String projectCode;
	private String projectName;
	private String projectDescription;
	private List<String> projectManagerEmail;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date projectStartDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date projectEndDate;
	private BigDecimal projectBudget;
	private String projectDepartmentName;
	private Integer applicationCount;
	private String currency;
	private Boolean isSingle;
	private List<SingleProjectApplicationOnboarding> applicationsInfo;
	@JsonIgnore
	private String applicationName;
	@JsonIgnore
	private String applicationStatus;
	@JsonIgnore
	private int rowNumber;

}
