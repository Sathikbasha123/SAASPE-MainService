package saaspe.adaptor.service;

import saaspe.adaptor.model.ConfluenceCreateUser;
import saaspe.model.CommonResponse;

public interface ConfluenceWrapperService {

	CommonResponse createUser(ConfluenceCreateUser confluenceCreateUser, String appId);

	CommonResponse getUserList(String appId);

	CommonResponse deleteUser(String accountId, String appId);


}
