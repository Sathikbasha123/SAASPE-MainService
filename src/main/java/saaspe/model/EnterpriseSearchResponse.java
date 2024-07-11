package saaspe.model;

import lombok.Data;

@Data
public class EnterpriseSearchResponse {
	private String query;
	private String data_source;
	private AskamigoResponse response;

}
