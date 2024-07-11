package saaspe.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ApplicationWorkFlowDetailsView {
	private NewAppOnboardInfo applicationInfo;

	private List<NewAppLicenseInfo> products;

	private List<NewAppContractInfo> contractInfo;

	private List<SupportDocumentsResponse> supportingDocsInfo;

	private DepartmentReviewerDetails reviewerDetails;
	
}
