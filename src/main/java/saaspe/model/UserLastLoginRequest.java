package saaspe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLastLoginRequest {

	@NonNull
	private String applicationId;

	@NonNull
	private String dateRange;

	@NonNull
	private String ownerName;

	@NonNull
	private String ownerEmail;

	@NonNull
	private String subject;

	@NonNull
	private String message;

}
