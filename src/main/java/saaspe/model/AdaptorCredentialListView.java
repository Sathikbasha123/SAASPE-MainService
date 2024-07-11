package saaspe.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdaptorCredentialListView {

	private String appName;
	private boolean isOauthRequired;
	private String flowType;
	private String callBackApiEndPoint;
	private List<AdaptorKeyValues> keyValues;
}
