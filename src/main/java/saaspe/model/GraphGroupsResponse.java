package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class GraphGroupsResponse {

	private String odataContext;
	private List<Value> value;

}
