package saaspe.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class NewAppLicenseInfo implements Serializable {

	private static final long serialVersionUID = -299482035708790407L;

	private String productType;
	private String productName;
	private BigDecimal unitPrice;
	private Integer quantity;
	private BigDecimal totalCost;
	private String currencyCode;
	private String contractName;
	private String unitPriceType;

}
