package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class AdaptorCustomField {
	private String applicationName;
	private List<AdaptorKeyValues> fields;
}
