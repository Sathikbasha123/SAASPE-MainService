package saaspe.model;

import lombok.Data;

@Data
public class TopAppsBySpend {

    private String applicatonId;

    private String applicaitonName;

    private String logUrl;

    private Integer cost;

    private String currency;
}
