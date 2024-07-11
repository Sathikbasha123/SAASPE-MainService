package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class UserEmailsRemoveRequest {

	List<UserRemovalRequest> userRemovalRequest;

}
