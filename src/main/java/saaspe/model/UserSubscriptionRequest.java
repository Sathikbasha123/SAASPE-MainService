package saaspe.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UserSubscriptionRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer amountSpent;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date renewalDate;
	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private String renewalType;
	private String accountName;

}
