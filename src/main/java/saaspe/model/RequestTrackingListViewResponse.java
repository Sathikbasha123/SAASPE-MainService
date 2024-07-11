package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class RequestTrackingListViewResponse{

    private String requestId;
    private String childRequestId;
    private String onboardingRequestName;
    private String onboardingRequestAvatar;
    private String onboardingStatus;
    private String onboardingComments;
    private String applicationName;
    private String applicationLogo;

}
