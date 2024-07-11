package saaspe.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
@Table(name = "saaspe_department_onboarding")
public class DepartmentOnboarding {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "NUMBER")
	private Integer number;

	@Column(name = "DEPARTMENT_ID")
	private String departmentId;

	@Column(name = "DEPARTMENT_NAME")
	private String departmentName;

	@Column(name = "DEPARTMENT_OWNER")
	private String departmentOwner;

	@Column(name = "DEPARTMENT_OWNER_EMAIL")
	private String departmentOwnerEmail;

	@Column(name = "BUDGET")
	private BigDecimal budget;

	@Column(name = "BUDGET_CURRENCY")
	private String budgetCurrency;

	@Column(name = "REQUEST_NUMBER")
	private String requestNumber;

	@Column(name = "CHILD_REQUEST_NUMBER")
	private String childRequestNumber;

	@Column(name = "ONBOARD_BY_USEREMAIL")
	private String onboardByUserEmail;

	@Column(name = "OPID")
	private String opID;

	@Column(name = "BUID")
	private String buID;

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

	@Column(name = "WORK_GROUP")
	private String workGroup;

	@Column(name = "APPROVED_REJECTED")
	private String approvedRejected;

	@Column(name = "WORK_GROUP_USEREMAIL")
	private String workGroupUserEmail;

	@Column(name = "COMMENTS")
	private String comments;

	@Column(name = "END_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date endDate;

	@Column(name = "ONBOARD_DATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date onBoardDate;

	@Column(name = "ONBOARDING_STATUS")
	private String onboardingStatus;
}
