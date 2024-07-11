package saaspe.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "saspe_cloud_service_details")
public class CloudServiceDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "Vendor_Id")
	private Integer vendorId;

	@Column(name = "Resource_Id")
	private String resourceId;

	@Column(name = "Service_Name")
	private String serviceName;

	@Column(name = "Account_Name")
	private String vendor;

	@Column(name = "Subscription_Type")
	private String subscriptionType;
	
	@Column(name = "Price")
	private Integer price;

	@Column(name = "Category")
	private String category;

	@Column(name = "Logo")
	private String logo;

	@NonNull
	@Column(name = "CURRENCY")
	private String currency;
}