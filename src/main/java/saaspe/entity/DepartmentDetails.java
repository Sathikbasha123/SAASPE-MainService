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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@NoArgsConstructor
@Data
@Table(name = "department_details")
public class DepartmentDetails {

	@Id
	@Column(name = "DEPARTMENT_ID")
	private String departmentId;

	@NonNull
	@Column(name = "DEPARTMENT_NAME")
	private String departmentName;

//	@NonNull
	@Column(name = "DEPARTMENT_OWNER")
	private String departmentOwner;

	@NonNull
	@Column(name = "BUDGET")
	private BigDecimal budget;

	@NonNull
	@Column(name = "BUDGET_CURRENCY")
	private String budgetCurrency;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "DEPARTMENT_APPLICATION", joinColumns = {
			@JoinColumn(name = "DEPARTMENT_ID") }, inverseJoinColumns = { @JoinColumn(name = "APPLICATION_ID") })
	private List<ApplicationDetails> applicationId = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TENANT_ID", foreignKey = @ForeignKey(name = "FK_TENANT_ID"))
	private TenantDetails tenantId;

	@JsonIgnore
	@OneToMany(mappedBy = "departmentId", fetch = FetchType.LAZY)
	private List<UserDetails> userDetails = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "departmentId", fetch = FetchType.LAZY)
	private List<ProjectDetails> projectDetails = new ArrayList<>();

	// @NonNull
	@Column(name = "DEPARTMENT_ADMIN")
	private String departmentAdmin;

	@Column(name = "CREATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@Column(name = "UPDATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	@NonNull
	@Column(name = "CREATED_BY")
	private String createdBy;

	@NonNull
	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@NonNull
	@Column(name = "BUID")
	private String buID;

	@NonNull
	@Column(name = "OPID")
	private String opID = "SAASPE";

}
