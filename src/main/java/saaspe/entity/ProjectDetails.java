package saaspe.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "saaspe_department_project_details")
public class ProjectDetails {

	@Column(name = "PROJECT_NAME")
	private String projectName;

	@Id
	@Column(name = "PROJECT_ID")
	private String projectId;

	@Column(name = "PROJECT_MANAGER")
	private String projectManager;

	@Column(name = "PROJECT_BUDGET")
	private BigDecimal projectBudget;

	@Column(name = "BUDGET_CURRENCY")
	private String budgetCurrency;

	@Column(name = "PROJECT_DESCRIPTION")
	private String projectDescription;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "START_DATE")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "END_DATE")
	private Date endDate;

	@Column(name = "CREATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@Column(name = "UPDATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@Column(name = "BUID")
	private String buID;

	@Column(name = "OPID")
	private String opID = "SAASPE";
	
	@Column(name = "PROJECT_CODE")
	private String projectCode;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "PROJECT_APPLICATION", joinColumns = { @JoinColumn(name = "PROJECT_ID") }, inverseJoinColumns = {
			@JoinColumn(name = "APPLICATION_ID") })
	private List<ApplicationDetails> applicationId = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEPARTMENT_ID", foreignKey = @ForeignKey(name = "DEPARTMENT_ID"))
	private DepartmentDetails departmentId;
}
