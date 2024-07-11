package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import saaspe.entity.TenantDetails;

@Data
@JsonInclude(Include.NON_NULL)
public class TenantDetailsResponse {
    private TenantDetails details;
    private String Message;
}
