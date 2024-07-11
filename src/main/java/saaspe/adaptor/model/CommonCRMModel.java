package saaspe.adaptor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(content = Include.NON_NULL)
public class CommonCRMModel {

	
	private String first_name;
	private String email;
	private String last_name;
	private String id;
	private String phone;
	private String dob;
	private String role;
	private String profile;
	private String country_locale;
	private String country;
	
	public CommonCRMModel(String first_name, String email,String role, String profile) {
		super();
		this.first_name = first_name;
		this.email = email;
		this.role = role;
		this.profile = profile;
	}
}
