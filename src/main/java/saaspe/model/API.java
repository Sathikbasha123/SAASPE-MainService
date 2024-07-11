package saaspe.model;

import java.util.Map;

import lombok.Data;

@Data
public class API {
	private String cls;
	private String opp;
	private Map<String, Object> ipl;
	private Object rsp;

}
