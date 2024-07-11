package saaspe.service;

import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;

public interface SubscriptionService {

	CommonResponse getApplicationSubscriptionDetails() throws DataValidationException;

	CommonResponse getApplicationSubscriptionDetailsListview() throws DataValidationException;

	CommonResponse getListOfSubscriptions() throws DataValidationException;

}
