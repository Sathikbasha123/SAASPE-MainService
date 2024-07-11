package saaspe.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Products {

	private String contractName;
	private String productName;
	private String productType;
	private BigDecimal unitPrice;
	private BigDecimal adminUnitPrice;
	private Integer quantity;
	private BigDecimal totalCost;
	private BigDecimal adminCost;
	private String currencyCode;
	private String unitPriceType;

}
