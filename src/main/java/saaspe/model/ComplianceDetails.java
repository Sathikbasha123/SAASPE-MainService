package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class ComplianceDetails {

    private List<String> noncompliantKeys;

    private List<String> keysWithNoncompliantValues;

    private Boolean complianceStatus;
    
}
