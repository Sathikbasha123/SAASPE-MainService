package saaspe.sso;

import saaspe.model.CommonResponse;

public interface SingleSignOnService {

	CommonResponse getAzureUserAccessAndRoles(String token);

}
