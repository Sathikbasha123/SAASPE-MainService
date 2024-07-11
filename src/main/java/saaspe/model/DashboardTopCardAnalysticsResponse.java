package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class DashboardTopCardAnalysticsResponse {
	private Integer firstMonth;

	private Integer secondMonth;

	private Integer thirdMonth;

	private Integer fourthmonth;

	private Integer applications;

	private Integer subscriptions;

	private Integer renewals;

	public DashboardTopCardAnalysticsResponse() {
		this.firstMonth = 0;
		this.secondMonth = 0;
		this.thirdMonth = 0;
		this.fourthmonth = 0;
	}
}
