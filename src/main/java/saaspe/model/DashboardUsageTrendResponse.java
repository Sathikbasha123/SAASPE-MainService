package saaspe.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class DashboardUsageTrendResponse {

	private Integer userCount;

	private Integer applicationCount;

	private String month;

	@JsonIgnore
	private Integer refId;

	@JsonIgnore
	private List<String> appCount;

	public DashboardUsageTrendResponse() {
		this.userCount = 0;
		this.applicationCount = 0;
	}
}
