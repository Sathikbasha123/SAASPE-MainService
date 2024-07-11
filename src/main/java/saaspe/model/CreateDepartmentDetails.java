package saaspe.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class CreateDepartmentDetails {
	
	private String departmentName;
	private List<CreateDepartmentOwnerDetails> ownerDetails;
	private BigDecimal departmentBudget;
	private String currencyCode;
}
