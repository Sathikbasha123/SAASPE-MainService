package saaspe.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(content = Include.NON_NULL)
public class UserRemovalRequest {

	private String userEmail;
	private boolean hasCustomFields;
	private List<UserAdaptorApplicationFields> customFields;
}