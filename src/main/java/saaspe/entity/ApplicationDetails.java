package saaspe.entity;

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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "saaspe_application_details")
public class ApplicationDetails {

	@Column(name = "CATEGORY_ID")
	private String categoryId;

	@Column(name = "OWNER_EMAIL")
	private String ownerEmail;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CATEGORY_ID", foreignKey = @ForeignKey(name = "FK_CATEGORY_ID"), insertable = false, updatable = false)
	private ApplicationCategoryMaster applicationCategoryMaster;

	@NonNull
	@Column(name = "APPLICATION_NAME")
	private String applicationName;

	@Id
	@Column(name = "APPLICATION_ID")
	private String applicationId;

	@NonNull
	@Column(name = "OWNER")
	private String owner;

	@NonNull
	@Column(name = "OWNER_DEPARTMENT")
	private String ownerDepartment;

	@Column(name = "TAGS")
	private String tags;

	@NonNull
	@Column(name = "APPLICATION_STATUS")
	private String applicationStatus;

	@Column(name = "ACTIVE_CONTRACTS")
	private Integer activeContracts;

	@Column(name = "APPLICATION_DESCRIPTION", length = 500)
	private String applicationDescription;

	@NonNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "RENEWAL_DATE")
	private Date renewalDate;

	@NonNull
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

	@NonNull
	@Column(name = "BUID")
	private String buID;

	@NonNull
	@Column(name = "OPID")
	private String opID = "SAASPE";

	@NonNull
	@Column(name = "LOGO_URL")
	private String logoUrl;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE }, mappedBy = "applicationId")
	private List<DepartmentDetails> departmentDetails = new ArrayList<>();

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE }, mappedBy = "applicationId")
	private List<ProjectDetails> projectDetails = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE }, mappedBy = "applicationId")
	private List<UserDetails> userDetails = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "applicationId")
	private List<ApplicationLicenseDetails> licenseDetails = new ArrayList<>();

	@JsonIgnore
	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "applicationId")
	private ApplicationSubscriptionDetails subscriptionDetails;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "START_DATE")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "END_DATE")
	private Date endDate;

	@Column(name = "PROJECT_NAME")
	private String projectName;

	@Column(name = "GRAPH_APPLICATION_ID")
	private String graphApplicationId;

	@Column(name = "SSO_ENABLED")
	private Boolean ssoEnabled;

	@Column(name = "IDENTITY_PROVIDER")
	private String identityProvider;

	@OneToMany(mappedBy = "applicationId")
	private List<ApplicationContractDetails> contractDetails;

}
