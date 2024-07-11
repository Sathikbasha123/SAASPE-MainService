package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class OwnerShipDetails {

	private Boolean isOwner;

	private OwnerShipDetailsResponse OwnerShipDetails;
	
	private Boolean hasCustomFields;
	
	private List<AdaptorCustomField> customFields;

}
