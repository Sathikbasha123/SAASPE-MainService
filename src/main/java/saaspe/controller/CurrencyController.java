package saaspe.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.dto.CurrencyDTO;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.service.CurrencyService;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/currency/")
public class CurrencyController {

	@Autowired
	private CurrencyService currencyService;

	private static final Logger log = LoggerFactory.getLogger(CurrencyController.class);

	@PostMapping("/update")
	@PreAuthorize("hasAuthority('EDIT_CURRENCY')")
	public ResponseEntity<CommonResponse> updateCurrency(@RequestBody CurrencyDTO currencyDTO) {
		try {
			CommonResponse currency = currencyService.updateCurrency(currencyDTO);
			return new ResponseEntity<>(currency, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending updateCurrency method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("UpdateCurrencyResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending updateCurrency method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("UpdateCurrencyResponse", new ArrayList<>()), e.getMessage()));
		}
	}

	@GetMapping("/admin/preference")
	public ResponseEntity<CommonResponse> adminCurrency() {
		try {
			CommonResponse currency = currencyService.adminCurrency();
			return new ResponseEntity<>(currency, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending adminCurrency method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("adminCurrencyResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending adminCurrency method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("AdminPrefrenceResponse", new ArrayList<>()), e.getMessage()));
		}
	}

}
