package saaspe.model;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
//@JsonInclude(Include.NON_NULL)
public class InvoicesListResponse {

	private String invoiceNumber;

	private String subscriptionName;

	private String applicationName;

	private String applicationLogo;

	private BigDecimal invoiceAmount;

	private BigDecimal dueAmount;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date dueDate;

	private String currency;

	private List<URI> invoiceUrl;

	private Boolean invoicePayable;

	private String subscriptionNumber;

	public InvoicesListResponse() {
		this.dueAmount = BigDecimal.valueOf(0.0);
	}

}
