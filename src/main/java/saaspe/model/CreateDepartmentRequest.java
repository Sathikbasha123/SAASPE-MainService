package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class CreateDepartmentRequest {
	private CreateDepartmentDetails departmentInfo;
	private String isSingle;
}
