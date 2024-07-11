package saaspe.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ApplicaitonDetailsListView {
	private String icon;

	private String shortDescription;

	private String status;

	private BigDecimal totalSpend;

	private String currency;

	private Integer activeContracts;

	private String appLink;

	private Boolean autoRenewals;

	private List<String> similarApps;

	private String category;

	private DepartmentOwnerResponse departmentdetails;
}
