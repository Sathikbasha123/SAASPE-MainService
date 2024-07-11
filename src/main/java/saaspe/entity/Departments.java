package saaspe.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
@Table(name = "saaspe_departments")
public class Departments {


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@NonNull
	@Column (name = "DEPARTMENT_ID")
	private String departmentId;

	@NonNull
	@Column(name = "DEPARTMENT_NAME")
	private String departmentName;

	@Column(name = "DEPARTMENT_OWNER")
	private String departmentOwner;

	@Column(name = "BUDGET")
	private BigDecimal budget;

	@Column(name = "BUDGET_CURRENCY")
	private String budgetCurrency;

	@Column(name = "DEPARTMENT_OWNER_EMAIL")
	private String departmentOwnerEmail;
	
	@Column(name = "IS_ONBOARDING")
	private Boolean isOnboarding;
	
	@Column(name = "PRIORITY")
	private Integer priority;
	
	@Column(name = "DEPARTMENT_CREATED_ON")
	private Date departmentCreatedOn;
	
	@Column(name = "DEPARTMENT_OWNER_CREATED_ON")
	private Date departmentOwnerCreatedOn;
	
	@Column(name = "DEPARTMENT_UPDATED_ON")
	private Date departmentUpdatedOn;
	
	@Column(name = "DEPARTMENT_OWNER_UPDATED_ON")
	private Date departmentOwnerUpdatedOn;
	
	@Column(name = "END_DATE")
	private Date endDate;
	
	@Column(name = "DEPARTMENT_CREATED_BY")
	private String departmentCreatedBy;
	
	@Column(name = "DEPARTMENT_UPDATED_BY")
	private String departmentUpdateBy;
	
	@NonNull
	@Column(name = "OPID")
	private String opID = "SAASPE";

	@NonNull
	@Column(name = "BUID")
	private String buID;
	
	@Column(name = "DEPARTMENT_ADMIN")
	private String departmentAdmin;
	
	@Column( name = "APPLICATION_ID")
	private String applicationId;
	
}
