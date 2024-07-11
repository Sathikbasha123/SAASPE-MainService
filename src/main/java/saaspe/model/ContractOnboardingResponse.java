package saaspe.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ContractOnboardingResponse {

    private String contractName;
    private String contractType;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date contractEndDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date contractStartDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date upcomingRenewalDate;
    private Boolean autoRenewal;
    private String renewalTerm;
    private String paymentMethod;
    private String cardHolderName;
    private String cardNumber;
    private String validThrough;
    private String walletName;
    private String billingFrequency;
    private Integer autoRenewalCancellation;
    private List<Products> products;
    private List<SupportDocumentsResponse> supportingDocsInfo;
    private ContractReviewerDetails reviewerDetails;

}
