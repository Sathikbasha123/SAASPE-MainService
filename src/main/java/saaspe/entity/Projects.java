package saaspe.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="saaspe_projects")
public class Projects {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name="PROJECT_ID")
	private String projectId;
	
	@Column(name="PROJECT_NAME")
	private String projectName;
	
	@Column(name="PROJECT_MANAGER")
	private String projectManager;
	
	@Column(name="PROJECT_BUDGET")
	private BigDecimal budget;
	
	@Column(name="BUDGET_CURRENCY")
	private String budgetCurrency;
	
	@Column(name="PROJECT_DESCRIPTION")
	private String description;
	
	@Column(name="PROJECT_CREATED_BY")
	private String createdBy;
	
	@Column(name="PROJECT_UPDATED_BY")
	private String updtaedBy;
	
	@Column(name="PROJECT_CREATED_ON")
	private Date projectCreatedOn;
	
	@Column(name="PROJECT_UPDATED_ON")
	private Date projectUpdatedOn;
	
	@Column(name="PROJECT_START_DATE")
	private Date projectStartDate;
	
	@Column(name="PROJECT_END_DATE")
	private Date projectEndDate;
	
	@Column(name="PROJECT_CODE")
	private String projectCode;
	
	@Column(name="PRIORITY")
	private Integer priority;
	
	@Column(name="PROJECT_MANAGER_EMAIL")
	private String projectManagerEmail;
	
	@Column(name="PROJECT_MANAGER_START_DATE")
	private Date projectManagerStartDate;
	
	@Column(name="PROJECT_MANAGER_END_DATE")
	private Date projectManagerEndDate;
	
	@Column(name="PROJECT_MANAGER_CREATED_ON")
	private Date projectManagerCreatedOn;
	
	@Column(name="PROJECT_MANAGER_UPDATED_ON")
	private Date projectManagerUpdatedOn;
	
	@Column(name="BUID")
	private String buId;
	
	@Column(name="OPID")
	private String opId;
	
	@Column(name="APPLICATION_ID")
	private String applicationId;
	
	@Column(name="PROJECT_STATUS")
	private Boolean projectStatus;

}
