package saaspe.service;

import java.io.IOException;
import java.text.ParseException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import freemarker.template.TemplateException;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.ClmContractRequest;
import saaspe.model.CommonResponse;

public interface CLMService {

	CommonResponse addClmContract(String json, MultipartFile[] createDocumentFiles, String[] createId,
			String[] deleteId, String templateId, HttpServletRequest request, UserLoginDetails profile)
			throws DataValidationException, JsonProcessingException, ParseException;

	CommonResponse getListOfClmContract(UserLoginDetails profile,int page, int limit, HttpServletRequest request, String status,
										String searchText, String order, String orderBy) throws DataValidationException, JsonProcessingException, ParseException;

	CommonResponse getClmContractDetailsView(String envelopeId)
			throws DataValidationException, JsonProcessingException, IllegalArgumentException, IllegalAccessException;

	CommonResponse upCommingContractRenewalReminderEmail()
			throws IOException, TemplateException, DataValidationException, MessagingException;

	CommonResponse updateTemplate(String json, String templateId, MultipartFile[] createDocumentFiles,
			MultipartFile[] updateDocumentFiles, String[] createId, String[] updateId)
			throws JsonProcessingException, DataValidationException;

	CommonResponse createTemplate(String json, MultipartFile[] createDocumentFiles, UserLoginDetails profile,
			HttpServletRequest request) throws JsonProcessingException, DataValidationException;

	CommonResponse createEnvelope(String json, MultipartFile[] createDocumentFiles, String[] createId,
			String[] deleteId, String templateId) throws JsonProcessingException;

	CommonResponse dashboardView(HttpServletRequest request, UserLoginDetails profile) throws JsonProcessingException;

	CommonResponse envelopeAudit(String envelopeid);

	CommonResponse getEnvelopeDocument(String envelopeId, String documentId);

	CommonResponse getlistEnvelopeRecipients(String envelopeId);

	CommonResponse getEnvelopeDocumentDetails(String envelopeId);

	CommonResponse listTemplateById(String templateId);

	CommonResponse listTemplates(String count, String start, String order, String orderBy, String searchText)
			throws IOException;

	CommonResponse getAllTemplates();

	CommonResponse getTemplateDocument(String templateId, String documentId);

}
