package saaspe.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDetailsLicensesResponse {

	private String productName;

	private String contractId;

	private Integer quantity;

	private BigDecimal unitPrice;

	private BigDecimal totalCost;

	private String currencyCode;

	private String licenseId;

	private String contractName;

	private Integer mappedLicenses;

	private Integer unmappedLicenses;

	private String productType;

	private String unitPriceType;
	
	private BigDecimal adminCost;
	
	private BigDecimal adminUnitCost;

}
