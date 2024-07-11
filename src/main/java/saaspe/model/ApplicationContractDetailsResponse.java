package saaspe.model;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ApplicationContractDetailsResponse {

	private String licenseId;
	private String productName;
	private Integer quantity;
	private BigDecimal unitPrice;
	private BigDecimal totalCost;
	private String currencyCode;
	private Integer mappedLicenses;
	private Integer unmappedLicenses;
	private String unitPriceType;
	private BigDecimal adminCost;
	private List<URI> fileUrl;

}
