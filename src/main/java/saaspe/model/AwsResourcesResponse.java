package saaspe.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class AwsResourcesResponse {

    private String resourceId;

    private String resourceName;

    private String resourceType;

    private String accountId;

    private String region;

    private List<Tag> resourceTags;

}
