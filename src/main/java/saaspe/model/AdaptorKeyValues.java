package saaspe.model;

import org.json.JSONObject;

import lombok.Data;

@Data
public class AdaptorKeyValues {
	private String label;
	private String key;
	private String regex;
	private String type;
	private String required;

	public AdaptorKeyValues(JSONObject json) {
		if (json != null) {
			this.label = json.optString("label", null);
			this.key = json.optString("key", null);
			this.regex = json.optString("regex", null);
			this.type = json.optString("type", null);
			this.required = json.optString("required", null);
		}
	}
}
