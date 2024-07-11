package saaspe.adaptor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddGitlabMemberRequest {

	String email;
	int accessLevel;
}
