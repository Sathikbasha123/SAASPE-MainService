package saaspe.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ContractLicenseDetailResponse {

	private String productType;

	private String productName;

	private String contractId;

	private Integer quantity;

	private BigDecimal unitPrice;

	private BigDecimal totalCost;

	private String unitPriceType;

	private BigDecimal adminCost;
	
	private BigDecimal adminUnitPrice;
}
