package saaspe.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;


import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import saaspe.constant.Constant;
import saaspe.document.ProductItemsDocumet;
import saaspe.document.ProductReviewsDocument;
import saaspe.entity.UserDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.marketplace.repository.ProductItemsDocumetRepository;
import saaspe.marketplace.repository.ProductReviewsRepository;
import saaspe.model.CommonResponse;
import saaspe.model.MarketPlaceEmailRequest;
import saaspe.model.MarketPlaceResponse;
import saaspe.model.ProductReviewsResponse;
import saaspe.model.Rating;
import saaspe.model.Response;
import saaspe.repository.UserDetailsRepository;
import saaspe.service.MarketPlaceService;

@Service
public class MarketPlaceServiceImpl implements MarketPlaceService {

	@Autowired
	private ProductItemsDocumetRepository productItemsDocumetRepository;

	@Autowired
	private ProductReviewsRepository productReviewsRepository;

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private Configuration config;

	@Value("${redirecturl.path}")
	private String redirectUrl;

	@Value("${sendgrid.domain.name}")
	private String mailDomainName;

	@Value("${sendgrid.domain.sendername}")
	private String senderName;

	@Value("${sendgrid.domain.support}")
	private String supportMail;

	@Override
	public CommonResponse getMarketPlace(String range) throws DataValidationException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		List<MarketPlaceResponse> marketPlaceResponses = new ArrayList<>();
		List<ProductItemsDocumet> itemsDocumets = productItemsDocumetRepository.findAll();
	
		for (ProductItemsDocumet documet : itemsDocumets) {
			Rating rating = new Rating();
			MarketPlaceResponse marketPlaceResponse = new MarketPlaceResponse();
			marketPlaceResponse.setId(documet.get_id());
			marketPlaceResponse.set__v(null);
			marketPlaceResponse.setCategory(documet.getCategory());
			marketPlaceResponse.setDescription(documet.getDescription());
			marketPlaceResponse.setLogo(documet.getLogo());
			marketPlaceResponse.setTitle(documet.getTitle());
			marketPlaceResponse.setSubCategory(documet.getSubCategory());
			marketPlaceResponse.setUUID(documet.getUUID());
			rating.setRatedBy(documet.getRatting().getRattedBy());
			rating.setRating(documet.getRatting().getRatting());
			marketPlaceResponse.setRating(rating);
			marketPlaceResponses.add(marketPlaceResponse);
		}

		if (range != null) {
			String limit = range;
			String[] splitedData = limit.split("-");
			List<String> list = new ArrayList<>();
			for (String eachNum : splitedData) {
				list.add(eachNum.trim());
			}
			int firstIndex = Integer.parseInt(list.get(0));
			int lastIndex = Integer.parseInt(list.get(1));
			if (firstIndex >= lastIndex) {
				throw new DataValidationException("First Index greater than last index!!", null, null);
			}
			if (firstIndex >= marketPlaceResponses.size()) {
				throw new DataValidationException("First Index size is greater than current reocrd szie!!", null, null);
			}
			if (firstIndex <= marketPlaceResponses.size() && lastIndex <= marketPlaceResponses.size()) {
				response.setData(marketPlaceResponses.subList(firstIndex, lastIndex));
			}
			if (lastIndex >= marketPlaceResponses.size()) {
				response.setData(marketPlaceResponses.subList(firstIndex, marketPlaceResponses.size()));
			}
		} else {
			response.setData(marketPlaceResponses);
		}
		response.setAction("marketPlaceResponse");
		commonResponse.setStatus(HttpStatus.OK);
		commonResponse.setMessage("Details retrieved successfully");
		commonResponse.setResponse(response);
		return commonResponse;
	}

	@Override
	public CommonResponse getProductReviews(String uUID) throws DataValidationException {
		
		List<ProductReviewsDocument> reviewsDocument = productReviewsRepository.findByUUID(uUID);

	    if (reviewsDocument == null || reviewsDocument.isEmpty()) {
	        throw new DataValidationException("No Reviews for given UUID", null, null);
	    }
		List<ProductReviewsResponse> list = new ArrayList<>();
		for (ProductReviewsDocument document : reviewsDocument) {
			ProductReviewsResponse productReviewsResponse = new ProductReviewsResponse();
			productReviewsResponse.setCompanyDetails(document.getCompanyDetails());
			productReviewsResponse.setDesignation(document.getDesignation());
			productReviewsResponse.setName(document.getName());
			productReviewsResponse.setRatedOn(document.getRatedOn());
			productReviewsResponse.setRating(document.getRating());
			productReviewsResponse.setReview(document.getReview());
			productReviewsResponse.setUUID(document.getUUID());
			productReviewsResponse.setVendorId(document.getVendorId());
			productReviewsResponse.setVendorName(document.getVendorName());
			list.add(productReviewsResponse);
		}
		return new CommonResponse(HttpStatus.OK, new Response("productReviewResponse", list),
				"Data retrieved successfully");
	}

	@Override
	public CommonResponse marketPlaceEmailTrigger(MarketPlaceEmailRequest marketPlaceEmailRequest,
			UserLoginDetails profile) throws
			IOException, TemplateException, MessagingException {
		String toAddress = supportMail;
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String subject = Constant.MARKETPLACE_EMAIL_SUBJECT;
		Map<String, Object> model = new HashMap<>();
		Template t = config.getTemplate("marketplace-enquiry.html");
		ProductItemsDocumet itemsDocumet = productItemsDocumetRepository
				.getProductItemsDocumetById(marketPlaceEmailRequest.getProductId());
		UserDetails userDetails = userDetailsRepository.findByuserEmail(profile.getEmailAddress());
		String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
		content = content.replace("{{ApplicationLogo}}", itemsDocumet.getLogo());
		content = content.replace("{{emailAddress}}", profile.getEmailAddress());
		content = content.replace("{{requirment}}", marketPlaceEmailRequest.getEnquiryMessage());
		content = content.replace("{{ApplicationService}}", itemsDocumet.getTitle());
		content = content.replace("{{contactName}}", userDetails.getUserName());
		try {
			helper.setFrom(mailDomainName, senderName);
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setText(content, true);
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedEncodingException(e.getMessage());
		} catch (MessagingException e) {
			throw new MessagingException(e.getMessage());
		}
		mailSender.send(message);
		return new CommonResponse(HttpStatus.OK, new Response("MarketplaceEnquiryEmailResponse", new ArrayList<>()),
				"Email triggered successfully");
	}

}
