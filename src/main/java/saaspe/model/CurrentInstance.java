package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class CurrentInstance {

    private String resourceId;

    private String instanceName;

    private List<TagValues> tags;

    ///need to import ec2class
    private Object eC2ResourceDetails;

    ///need to import resourceUtilizationclas
    private Object resourceUtilization;

    private String reservationCoveredHoursInLookbackPeriod;

    private String savingsPlansCoveredHoursInLookbackPeriod;

    private String onDemandHoursInLookbackPeriod;

    private String totalRunningHoursInLookbackPeriod;

    private String monthlyCost;

    private String currencyCode;
    
}
