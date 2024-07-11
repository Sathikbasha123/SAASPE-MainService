package saaspe.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AzureSubscriptionsResponse extends Response {

	private String subscriptionId;

	private String subscriptionName;

	private String defaultCurrency;

}
