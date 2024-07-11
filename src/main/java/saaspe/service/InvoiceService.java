package saaspe.service;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import com.microsoft.azure.storage.StorageException;

import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.InvoiceRequest;

public interface InvoiceService {

	CommonResponse getInvoiceDetailsListview() throws DataValidationException, StorageException, URISyntaxException;

	CommonResponse removeInvoices(InvoiceRequest invoiceId) throws DataValidationException;

	CommonResponse uploadInvoices(String subscriptionId, String invoiceNumber, BigDecimal invoiceAmount, Date dueDate,
			MultipartFile file, String currency, UserLoginDetails loginDetails, String billPeriod)
			throws DataValidationException, URISyntaxException;

}
