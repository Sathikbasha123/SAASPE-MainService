package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class AzureResourceListResponse extends Response {

	private String resourceName;
	private String resourceType;
	private String subscriptionName;
	private String resourceLocation;
	private Object resourceTags;

}
