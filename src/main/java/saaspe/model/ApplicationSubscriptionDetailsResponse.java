package saaspe.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ApplicationSubscriptionDetailsResponse {

	private String subscriptionName;

	private String subscriptionId;

	private String currency;

	private String applicationName;

	private String applicationLogo;

	private String providerName;

	private String providerLogo;

	private List<ContractsUnderSubscription> contracts;

	private BigDecimal totalCost;

	private BigDecimal adminCost;

	private String subscriptionNumber;

}
