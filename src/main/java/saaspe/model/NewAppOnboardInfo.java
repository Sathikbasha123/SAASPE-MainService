package saaspe.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class NewAppOnboardInfo implements Serializable {

	private static final long serialVersionUID = -299482035708790407L;

	private String applicationName;

	private String subscriptionName;

	private String subscriptionId;

	private String applicationCategory;

	private List<newApplicationOwnerDetailsRequest> ownerDetails;

	private String applicationOwnerDepartment;

	private String applicationProviderName;

	private String applicationJustification;

	private String applicationLogoUrl;

	private String projectName;

	private String subscriptionNumber;

}