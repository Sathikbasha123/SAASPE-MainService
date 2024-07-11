package saaspe.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class MultiCloudResponse extends Response implements Serializable {

	private static final long serialVersionUID = 1L;

	private String vendorName;
	private String resourceId;
	private String serviceName;
	private String vendor;
	private Integer amountSpent;
	private Integer price;
	private String subscriptionType;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date renewalDate;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date subscriptionStartDate;
	private String renewalType;
	private String category;
	private String currency;
	private String logo;
}
