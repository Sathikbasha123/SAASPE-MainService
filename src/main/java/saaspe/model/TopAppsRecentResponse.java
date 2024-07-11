package saaspe.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class TopAppsRecentResponse {

    private String applicationID;
    private String applicationLogo;
    private String applicationName;
    private String applicationShortDescription;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date applicationCreatedDate;

}
