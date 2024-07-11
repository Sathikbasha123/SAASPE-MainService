package saaspe.service;

import java.text.ParseException;

import saaspe.dto.CurrencyDTO;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;

public interface CurrencyService {
	CommonResponse updateCurrency(CurrencyDTO currencyDTO) throws DataValidationException, ParseException;

	CommonResponse adminCurrency() throws DataValidationException;
}
