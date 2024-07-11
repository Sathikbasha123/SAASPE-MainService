package saaspe.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import saaspe.constant.Constant;
import saaspe.entity.ApplicationSubscriptionDetails;
import saaspe.entity.InvoiceDetails;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.DocumentUploadResponse;
import saaspe.model.InvoiceRequest;
import saaspe.model.InvoicesListResponse;
import saaspe.model.Response;
import saaspe.repository.ApplicationSubscriptionDetailsRepository;
import saaspe.repository.InvoiceRepository;
import saaspe.service.InvoiceService;

@Service
public class InvoiceServiceImpl implements InvoiceService {

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private ApplicationSubscriptionDetailsRepository applicationSubscriptionDetailsRepository;

	@Autowired
	private CloudBlobClient cloudBlobClient;

	@Value("${azure.storage.container.name}")
	private String blobContainer;

	@Value("${azure.storage.container.invoices.name}")
	private String invoicePath;

	@Override
	public CommonResponse getInvoiceDetailsListview()
			throws DataValidationException, StorageException, URISyntaxException {
		List<InvoicesListResponse> duplicateList = new ArrayList<>();
		List<InvoiceDetails> invoiceDetails = invoiceRepository.getInvoceDetailsListview();
		if (invoiceDetails == null) {
			throw new DataValidationException("No Invoice Details found", null, null);
		}
		for (InvoiceDetails details : invoiceDetails) {
			InvoicesListResponse invoiceDetailsResponse = new InvoicesListResponse();
			invoiceDetailsResponse.setInvoiceNumber(details.getInvoiceNumber());
			ApplicationSubscriptionDetails applicationSubscriptionDetails = applicationSubscriptionDetailsRepository
					.findBySubscriptionId(details.getSubscriptionId());
			invoiceDetailsResponse.setSubscriptionName(applicationSubscriptionDetails.getSubscriptionName());
			invoiceDetailsResponse.setSubscriptionNumber(applicationSubscriptionDetails.getSubscriptionNumber());
			invoiceDetailsResponse
					.setApplicationName(applicationSubscriptionDetails.getApplicationId().getApplicationName());
			invoiceDetailsResponse.setApplicationLogo(applicationSubscriptionDetails.getApplicationId().getLogoUrl());
			invoiceDetailsResponse.setInvoiceAmount(details.getInvoiceAmount());
			invoiceDetailsResponse.setDueDate(details.getDueDate());
			if (details.getAmountDue() == null) {
				invoiceDetailsResponse.setInvoicePayable(false);
			} else {
				invoiceDetailsResponse.setDueAmount(details.getAmountDue());
				invoiceDetailsResponse.setInvoicePayable(true);
			}
			invoiceDetailsResponse.setInvoiceUrl(getBlobURI(invoicePath + details.getInvoiceNumber()));
			invoiceDetailsResponse.setInvoiceAmount(details.getInvoiceAmount());

			invoiceDetailsResponse.setInvoiceUrl(getBlobURI(invoicePath + details.getInvoiceNumber()));
			if (!duplicateList.contains(invoiceDetailsResponse)) {
				duplicateList.add(invoiceDetailsResponse);
			}
			invoiceDetailsResponse.setCurrency(details.getCurrency());
		}
		if(duplicateList == null || duplicateList.isEmpty()) {
			return new CommonResponse(HttpStatus.NOT_FOUND, new Response("InvoicesListResponse", duplicateList),
					"No innvoices found");
		}
		return new CommonResponse(HttpStatus.OK, new Response("InvoicesListResponse", duplicateList),
				"Data retrieved successfully");
	}

	private List<URI> getBlobURI(String fileName) throws StorageException, URISyntaxException {
		List<URI> uris = new ArrayList<>();
		try {
			String path = blobContainer;
			CloudBlobContainer container = cloudBlobClient.getContainerReference(path);
			for (ListBlobItem blobItem : container.listBlobs(fileName + "/")) {
				uris.add(blobItem.getUri());
			}
		} catch (URISyntaxException e) {
			throw new URISyntaxException("URL Error", "Unable to Connect to Azure, Please Check URL in properties");
		} catch (StorageException e) {
			throw new StorageException(Constant.INSUFFICIENT_STORAGE, Constant.CLIENT_ERROR, e);
		}
		return uris;
	}

	@Override
	public CommonResponse uploadInvoices(String subscriptionId, String invoiceNumber, BigDecimal invoiceAmount,
			Date dueDate, MultipartFile file, String currency, UserLoginDetails loginDetails, String billPeriod)
			throws DataValidationException, URISyntaxException {
		List<MultipartFile> files = new ArrayList<>();
		files.add(file);
		InvoiceDetails invoiceDetails = new InvoiceDetails();
		ApplicationSubscriptionDetails applicationSubscriptionDetails = applicationSubscriptionDetailsRepository
				.findBySubscriptionId(subscriptionId);
		if (invoiceRepository.getInvoceById(invoiceNumber) != null) {
			throw new DataValidationException("Invoice Number already Exist", "400", HttpStatus.BAD_REQUEST);
		}
		if (applicationSubscriptionDetails == null) {
			throw new DataValidationException("Subscription not found!", "404", HttpStatus.NOT_FOUND);
		}
		if (Constant.CURRENCY.stream().noneMatch(currency::equals)) {
			throw new DataValidationException("Currency is not match", "400", HttpStatus.BAD_REQUEST);
		}
		if (file.getSize() >= 5242880) {
			throw new DataValidationException("Uploaded file size is morethan 5mb!", "413", HttpStatus.BAD_REQUEST);
		}
		upload(files, invoiceNumber);
		invoiceDetails.setSubscriptionId(subscriptionId);
		invoiceDetails.setCreatedOn(new Date());
		invoiceDetails.setCreatedBy(loginDetails.getEmailAddress());
		invoiceDetails.setInvoiceDate(new Date());
		invoiceDetails.setBillPeriod(billPeriod);
		invoiceDetails.setSubscriptionEmail(applicationSubscriptionDetails.getSubscriptionOwnerEmail());
		invoiceDetails.setInvoiceNumber(invoiceNumber);
		invoiceDetails.setCurrency(currency);
		invoiceDetails.setApplicatoinId(applicationSubscriptionDetails.getApplicationId().getApplicationId());
		invoiceDetails.setInvoiceAmount(invoiceAmount);
		invoiceDetails.setDueDate(dueDate);
		invoiceRepository.save(invoiceDetails);
		return new CommonResponse(HttpStatus.OK, new Response("InvoiceUploadResponse", ""),
				"Invoice uploaded Successfully");
	}

	@Override
	public CommonResponse removeInvoices(InvoiceRequest invoiceId) throws DataValidationException {
		InvoiceDetails details = invoiceRepository.getInvoceById(invoiceId.getInvoiceId());
		if (details == null) {
			throw new DataValidationException("Invoice Not Found or already removed " + invoiceId, "403",
					HttpStatus.NOT_FOUND);
		}
		deleteBlob(blobContainer, invoiceId.getInvoiceId());
		invoiceRepository.deleteByInvoiceId(invoiceId.getInvoiceId());
		return new CommonResponse(HttpStatus.OK, new Response("InvoiceRemoveResponse", ""),
				"Invoice Removed Successfully");
	}

	private CommonResponse upload(List<MultipartFile> multipartFiles, String fileName)
			throws URISyntaxException {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		List<DocumentUploadResponse> list = new ArrayList<>();
		CloudBlockBlob blob = null;
		try {
			String path = fileName;
			for (MultipartFile file : multipartFiles) {
				DocumentUploadResponse uploadResponse = new DocumentUploadResponse();
				CloudBlobContainer container = cloudBlobClient.getContainerReference(blobContainer);
				blob = container.getBlockBlobReference(invoicePath + path + "/" + file.getOriginalFilename());
				blob.upload(file.getInputStream(), -1);
				uploadResponse.setFilePath(blob.getName());
				list.add(uploadResponse);
				response.setData(list);
			}
			response.setAction("MltipartFile");
			commonResponse.setMessage("File Upload Success");
			commonResponse.setStatus(HttpStatus.OK);
			commonResponse.setResponse(response);
			return commonResponse;
		} catch (StorageException e) {
			e.printStackTrace();
			response.setAction("MultipartFile");
			response.setData(e);
			commonResponse.setMessage("Storage Exception");
			commonResponse.setStatus(HttpStatus.CONFLICT);
			commonResponse.setResponse(response);
			return commonResponse;
		} catch (IOException e) {
			response.setAction("MultipartFile");
			response.setData(e);
			commonResponse.setMessage("I/O Exception");
			commonResponse.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
			commonResponse.setResponse(response);
			return commonResponse;

		}
	}

	private void deleteBlob(String containerName, String blobName) {
		try {
			CloudBlobContainer container = cloudBlobClient.getContainerReference(containerName);
			for (ListBlobItem item : container.listBlobs(blobName + "/", true)) {
				String[] elements = item.getUri().toString().split("/");
				String lastElement = elements[elements.length - 1];
				CloudBlockBlob blob = container.getBlockBlobReference(blobName + "/" + lastElement);
				if (blob.exists()) {
					blob.delete();
				}
			}
		} catch (URISyntaxException | StorageException e) {
			e.printStackTrace();
		} 
	}

}
