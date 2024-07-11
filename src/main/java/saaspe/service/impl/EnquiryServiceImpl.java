package saaspe.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import saaspe.constant.Constant;
import saaspe.dto.EnquiryRequest;
import saaspe.entity.EnquiryDetails;
import saaspe.entity.SequenceGenerator;
import saaspe.exception.DataValidationException;
import saaspe.model.CloudFlareRequest;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.model.SiteVerificationRequest;
import saaspe.repository.EnquiryDetailRepository;
import saaspe.repository.SequenceGeneratorRepository;
import saaspe.service.EnquiryService;

@Service
public class EnquiryServiceImpl implements EnquiryService {

	@Autowired
	private EnquiryDetailRepository enquiryDetailsRepo;

	@Autowired
	private SequenceGeneratorRepository generatorRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private Configuration config;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${sendgrid.domain.name}")
	private String mailDomainName;

	@Value("${sendgrid.domain.sendername}")
	private String senderName;

	@Value("${sendgrid.domain.support}")
	private String mailSupportDomain;

	@Value("${cloudflare.secret}")
	private String cloudFlareSecret;
	
	@Value("${enquiry.verification.url}")
	private String verificationUrl;

	@Override
	public CommonResponse saveUserContactFormDetails(EnquiryRequest enquiryRequest)
			throws IOException, TemplateException, MessagingException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		EnquiryDetails details = new EnquiryDetails();
		String name = "ENQUIRY_0";
		Integer sequence = generatorRepository.getUserEnquirySequence();
		name = name.concat(sequence.toString());
		SequenceGenerator updateSequence = generatorRepository.getById(1);
		updateSequence.setEnquirySequenceId(++sequence);
		generatorRepository.save(updateSequence);
		details.setEnquiryId(name);
		details.setName(enquiryRequest.getName());
		details.setNumber(enquiryRequest.getNumber());
		details.setEmail(enquiryRequest.getEmail());
		details.setDesignation(enquiryRequest.getDesignation());
		details.setCompanyName(enquiryRequest.getCompanyName());
		details.setMessage(enquiryRequest.getMessage());
		details.setCreatedOn(new Date());
		details.setStartDate(new Date());
		details.setUpdatedOn(null);
		details.setEndDate(null);
		enquiryDetailsRepo.save(details);
		sendVerificationEmailToUser(enquiryRequest);
		response.setAction("SaveContractFormReponse");
		response.setData("");
		commonResponse.setMessage("Userdetails saved successfully");
		commonResponse.setStatus(HttpStatus.OK);
		commonResponse.setResponse(response);
		return commonResponse;
	}

	private void sendVerificationEmailToUser(EnquiryRequest enquiryRequest)
			throws IOException, TemplateException, MessagingException {
		String toAddress = enquiryRequest.getEmail();
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		MimeMessage message2 = mailSender.createMimeMessage();
		MimeMessageHelper helper2 = new MimeMessageHelper(message2);
		String subject = Constant.ENQUIRY_VERIFY_EMAIL_SUBJECT;
		Map<String, Object> model = new HashMap<>();
		model.put("name", enquiryRequest.getName());
		Template acknowledgementtemplate = config.getTemplate("ack-email.html");
		String acknowledgementcontent = FreeMarkerTemplateUtils.processTemplateIntoString(acknowledgementtemplate,
				model);
		Template supportmailtemplate = config.getTemplate("support-email.html");
		String supportmailcontent = FreeMarkerTemplateUtils.processTemplateIntoString(supportmailtemplate, model);
		acknowledgementcontent = acknowledgementcontent.replace("{{name}}", enquiryRequest.getName());
		supportmailcontent = supportmailcontent.replace("{{name}}", enquiryRequest.getName());
		supportmailcontent = supportmailcontent.replace("{{email}}", enquiryRequest.getEmail());
		supportmailcontent = supportmailcontent.replace("{{phone}}", enquiryRequest.getNumber());
		supportmailcontent = supportmailcontent.replace("{{company}}", enquiryRequest.getCompanyName());
		supportmailcontent = supportmailcontent.replace("{{designation}}", enquiryRequest.getDesignation());
		supportmailcontent = supportmailcontent.replace("{{message}}", enquiryRequest.getMessage());

		try {
			helper.setFrom(mailDomainName, senderName);
			helper.setTo(toAddress);
			helper.setText(acknowledgementcontent, true);
			helper2.setFrom(mailDomainName, senderName);
			helper2.setTo(mailSupportDomain);
			helper2.setSubject(subject);
			helper2.setText(supportmailcontent, true);
			helper2.setCc(new String[] { "navya.mallela@mind-graph.com", "sreenivasan.m@mind-graph.com",
					"bharat.sangapu@mind-graph.com", "sales@saaspe.com" });
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedEncodingException(e.getMessage());
		} catch (MessagingException e) {
			throw new MessagingException(e.getMessage());
		}
		mailSender.send(message);
		mailSender.send(message2);
	}

	@Override
	public CommonResponse getSiteVerification(SiteVerificationRequest response) throws DataValidationException {

		if (response.getResponse().isEmpty()) {
			throw new DataValidationException("Token should not be null", "Site verification failed",
					HttpStatus.BAD_REQUEST);
		}
		String url = verificationUrl;
		UriComponentsBuilder builderTemplate = UriComponentsBuilder.fromHttpUrl(url);
		CloudFlareRequest request = new CloudFlareRequest();
		request.setSecret(cloudFlareSecret);
		request.setResponse(response.getResponse());
		String urlTemplate = builderTemplate.toUriString();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<Object> responseEntity = restTemplate.exchange(urlTemplate, HttpMethod.POST,
				new HttpEntity<>(request, headers), Object.class);
		return new CommonResponse(responseEntity.getStatusCode(),
				new Response("Site verification response", responseEntity.getBody()), "Site verfication successfull");

	}
}
