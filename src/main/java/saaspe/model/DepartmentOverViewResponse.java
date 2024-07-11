package saaspe.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(Include.NON_NULL)
public class DepartmentOverViewResponse {
    private DepartmentDetailsOverviewResponse departmentOverviewResponse;

    private List<DepartmentDetailsApplicationsResponse> departmentapplicationsResponse;

    private List<DepartmentDetailsUsersResponse> departmentusersResponse;

    private List<ProjectListViewResponse> projectDepartmentResponse;
}
