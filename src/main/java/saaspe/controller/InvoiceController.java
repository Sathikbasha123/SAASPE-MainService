package saaspe.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import saaspe.aspect.ControllerLogging;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.InvoiceRequest;
import saaspe.model.Response;
import saaspe.service.InvoiceService;

@ControllerLogging
@RestController
@RequestMapping("/api/v1/invoice")
public class InvoiceController {

	@Autowired
	private InvoiceService invoiceService;

	private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);

	@GetMapping("/list-view")
	@PreAuthorize("hasAuthority('VIEW_INVOICE')")
	public ResponseEntity<CommonResponse> getInvoiceDetailsListview(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			log.info("*** Enter into getInvoiceDetailsListview ***");
			CommonResponse invoiceDetailsResponses = invoiceService.getInvoiceDetailsListview();
			return new ResponseEntity<>(invoiceDetailsResponses, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending getInvoiceDetailsListview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("InvoicesListResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getInvoiceDetailsListview method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("InvoiceListResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping(value = "/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
			MediaType.APPLICATION_PDF_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_PDF_VALUE })
	@PreAuthorize("hasAuthority('ADD_INVOICE')")
	public ResponseEntity<CommonResponse> superadminUploadInvoices(
			@RequestParam("subscriptionId") String subscriptionId,
			@RequestParam("invoiceAmount") BigDecimal invoiceAmount, @RequestParam("currency") String currency,
			@Valid @Pattern(regexp = "[a-zA-Z0-9\\-#\\.\\(\\)\\/%&\\s]", message = "length must be 3") @RequestParam("invoiceNumber") String invoiceNumber,
			@RequestParam("dueDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dueDate,
			@RequestParam("billPeriod") String billPeriod, @RequestPart("file") MultipartFile file,
			Authentication authentication) {
		CommonResponse commonResponse = new CommonResponse();
		UserLoginDetails profile = (UserLoginDetails) authentication.getPrincipal();
		try {
			commonResponse = invoiceService.uploadInvoices(subscriptionId, invoiceNumber, invoiceAmount, dueDate, file,
					currency, profile, billPeriod);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("InvoiceUploadResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending superadminUploadInvoices method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("AddInvoiceResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@DeleteMapping(value = "/remove")
	@PreAuthorize("hasAuthority('DELETE_INVOICE')")
	public ResponseEntity<CommonResponse> superadminRemoveInvoices(@RequestBody InvoiceRequest invoice) {
		CommonResponse commonResponse = new CommonResponse();
		try {
			commonResponse = invoiceService.removeInvoices(invoice);
			return ResponseEntity.ok(commonResponse);
		} catch (DataValidationException e) {
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("InvoiceRemoveResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** ending superadminRemoveInvoices method with error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("DeleteInvoiceResponse", new ArrayList<>()), e.getMessage()));
		}
	}
}
