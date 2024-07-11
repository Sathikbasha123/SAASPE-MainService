package saaspe.dto;

import lombok.Data;

@Data
public class EnquiryRequest {
	
	private String name;
	
	private String number;
	
	private String message;
	
	private String email;
	
	private String companyName;
	
	private String designation;

}
