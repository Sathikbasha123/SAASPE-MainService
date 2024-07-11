package saaspe.service;

import java.io.IOException;

import javax.mail.MessagingException;


import freemarker.template.TemplateException;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.MarketPlaceEmailRequest;

public interface MarketPlaceService {

	CommonResponse getMarketPlace(String range) throws DataValidationException;

	CommonResponse getProductReviews(String uUID) throws DataValidationException;

	CommonResponse marketPlaceEmailTrigger(MarketPlaceEmailRequest marketPlaceEmailRequest, UserLoginDetails profile)
			throws  IOException,
			TemplateException, MessagingException;

}
