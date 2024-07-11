package saaspe.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class DashboardTopCardResponse {
	private DashboardTopCardAnalysticsResponse application;
	private DashboardTopCardAnalysticsResponse subscription;
	private DashboardTopCardAnalysticsResponse renewal;
	private List<String> months;
}
