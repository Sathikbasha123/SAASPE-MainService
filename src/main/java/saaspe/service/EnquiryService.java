package saaspe.service;

import java.io.IOException;

import javax.mail.MessagingException;

import freemarker.template.TemplateException;
import saaspe.dto.EnquiryRequest;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.SiteVerificationRequest;

public interface EnquiryService {

	CommonResponse saveUserContactFormDetails(EnquiryRequest enquiryRequest)
			throws IOException, TemplateException, MessagingException;

	CommonResponse getSiteVerification(SiteVerificationRequest response) throws DataValidationException;

}
