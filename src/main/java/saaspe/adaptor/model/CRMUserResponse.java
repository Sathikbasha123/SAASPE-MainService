package saaspe.adaptor.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CRMUserResponse {

	@NonNull
	private List<CommonUserResponse> users;
}

