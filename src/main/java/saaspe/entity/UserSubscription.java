package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "saspe_user_subscription_details")
public class UserSubscription {

	@Column(name = "User_Subscription_Id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	private Integer userSubscriptionId;

	@Column(name = "Vendor_Id")
	private Integer vendorId;

	@Column(name = "Amount_Spent")
	private Integer amountSpent;

	@Column(name = "Renewal_Date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date renewalDate;

	@Column(name = "Renewal_Type")
	private String renewalType;
	
	@Column(name = "Subscription_Start_Date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date subscriptionStartDate;

	@Column(name = "Account_Name")
	private String accountName;
		
	@Column(name = "Subscription_Name")
	private String subscriptionName;
}
