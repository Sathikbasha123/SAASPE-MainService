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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "saaspe_application_license_details")
@NoArgsConstructor
public class ApplicationLicenseDetails {

	@Column(name = "PRODUCT_NAME")
	private String productName;

	@Column(name = "PRODUCT_CATEGORY")
	private String productCategory;

	@Id
	@Column(name = "LICENSE_ID")
	private String licenseId;

	@Column(name = "QUANTITY")
	private Integer quantity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APPLICATION_ID", foreignKey = @ForeignKey(name = "FK_APPLICATOIN_ID"))
	private ApplicationDetails applicationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CONTRACT_ID", foreignKey = @ForeignKey(name = "FK_CONTRACT_ID"))
	private ApplicationContractDetails contractId;

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

	@Column(name = "LICENSE_MAPPED")
	private Integer licenseMapped;

	@Column(name = "LICENSE_UNMAPPED")
	private Integer licenseUnMapped;

	@Column(name = "UNIT_PRICE")
	private BigDecimal unitPrice;

	@Column(name = "TOTAL_COST")
	private BigDecimal totalCost;

	@Column(name = "CURRENCY")
	private String currency;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "USERS_LICESNCES", joinColumns = { @JoinColumn(name = "LICENSE_ID") }, inverseJoinColumns = {
			@JoinColumn(name = "USER_EMAIL") })
	private List<UserDetails> userId = new ArrayList<>();

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "START_DATE")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "END_DATE")
	private Date endDate;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SUBSCRIPTION_ID", foreignKey = @ForeignKey(name = "FK_SUBSCRIPTION_ID"))
	private ApplicationSubscriptionDetails subscriptionId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "LICENSE_START_DATE")
	private Date licenseStartDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "LICENSE_END_DATE")
	private Date licenseEndDate;

	@Column(name = "UNIT_PRICE_TYPE")
	private String unitPriceType;

	@Column(name = "CONVERTED_COST")
	private BigDecimal convertedCost = BigDecimal.valueOf(0.0);
}
