package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ContractOnboadingRequesListView {

    private String contractName;

    private String applicationId;

    private String applicationName;
    
    private String departmentName;

    private String applicationLogo;

    private String requestId;

    private String onboardedByEmail;

    private String childRequestId;

    private String reviewedByEmail;
    
    private String onboardingStatus;

}
