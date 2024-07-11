package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "saspe_multicloud")
public class MultiCloud {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "Cloud_Id")
	private Integer cloudId;

	@Column(name = "Resource_Id")
	private String resourceId;

	@Column(name = "Service_Name")
	private String serviceName;

	@Column(name = "Account_Name")
	private String vendor;

	@Column(name = "Total_Amount_Spent")
	private Integer cost;

	@Column(name = "Price")
	private Integer price;

	@Column(name = "Subscription_Type")
	private String subscriptionType;

	@Column(name = "Renewal_Date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date renewalDate;

	@Column(name = "Renewal_Type")
	private String renewalType;

	@Column(name = "Category")
	private String category;

	@Column(name = "Email_Address")
	private String emailAddress;
	
	@NonNull
    @Column(name = "CURRENCY")
    private String currency;

}
