package saaspe.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class PurchasedSingleAppOnboardingRequest implements Serializable {

	private static final long serialVersionUID = -299482035708790407L;

	private NewAppOnboardInfo applicationInfo;

	private List<NewAppLicenseInfo> products;

	private List<NewAppContractInfo> contractInfo;


}
