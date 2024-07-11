package saaspe.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class MultiCloudSpendResponse extends Response implements Serializable {

	private static final long serialVersionUID = 1L;

	private String vendorName;
	private String serviceName;
	private BigDecimal totalAmountSpent;	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private List<Date> renewalDates;
	private String resourceId;
	private String logo;
	private String renewaltype;
	private String currency;
	
}
