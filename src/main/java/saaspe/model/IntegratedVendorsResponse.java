package saaspe.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class IntegratedVendorsResponse {

	private String cloudProviderName;

	private String cloudProviderLogo;

	private String clientId;

	private String tenantId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date onboardedDate;

}
