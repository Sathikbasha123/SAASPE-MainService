package saaspe.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ContractDashBoardResponse {

	private BigDecimal totalContractsSpend;
	private BigDecimal totalContractSpendAdminCost;
	private Integer totalActiveContracts;
	private Integer totalExpiringContracts;
	private String currency;

}
