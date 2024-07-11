package saaspe.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import saaspe.currency.entity.CurrencyEntity;
import saaspe.currency.repository.CurrencyRepository;
import saaspe.dto.CurrencyDTO;
import saaspe.entity.ApplicationContractDetails;
import saaspe.entity.ApplicationLicenseDetails;
import saaspe.entity.Applications;
import saaspe.entity.UserDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.repository.ApplicationContractDetailsRepository;
import saaspe.repository.ApplicationLicenseDetailsRepository;
import saaspe.repository.ApplicationsRepository;
import saaspe.repository.UserDetailsRepository;
import saaspe.service.CurrencyService;
import saaspe.utils.CommonUtil;

@Service
public class CurrencyServiceImpl implements CurrencyService {

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private UserDetailsRepository userDetailsRepository;

	@Autowired
	private ApplicationContractDetailsRepository contractDetailsRepository;

	@Autowired
	private ApplicationLicenseDetailsRepository licenseDetailsRepository;
	
	@Autowired
	private ApplicationsRepository applicationRepository;

	@Override
	@Transactional
	public CommonResponse updateCurrency(CurrencyDTO currencyDTO) throws DataValidationException, ParseException {
		List<String> currencys = Arrays.asList("USD", "MYR", "PHP", "SGD", "INR", "EUR", "GBP", "AUD");
		if (!currencys.contains(currencyDTO.getCurrency())) {
			throw new DataValidationException("Please Provide Valid Currency", null, null);
		}

		List<UserDetails> superAdmins = userDetailsRepository.findAllSuperAdmin();
		for (UserDetails admin : superAdmins) {
			admin.setCurrency(currencyDTO.getCurrency());
			admin.setUpdatedOn(new Date());
			userDetailsRepository.save(admin);
		}
		List<ApplicationContractDetails> contractDetails = contractDetailsRepository.findAll();
		for (ApplicationContractDetails contract : contractDetails) {
			String contractCurrency = contract.getContractCurrency();
			Date contractStartDate = contract.getContractStartDate();
			Date startDate = contract.getStartDate();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			for (ApplicationLicenseDetails licenseDetails : contract.getLicenseDetails()) {
				if (contractCurrency.equalsIgnoreCase(currencyDTO.getCurrency())) {
					licenseDetails.setConvertedCost(licenseDetails.getTotalCost());
					licenseDetails = licenseDetailsRepository.save(licenseDetails);
				} else {
					CurrencyEntity currencyValue = null;
					if (contractStartDate.after(CommonUtil.convertLocalDatetoDate(LocalDate.now().plusDays(1)))) {
						currencyValue = currencyRepository
								.findByDateAndCurrecncy(formatter.parse(formatter.format(startDate)), contractCurrency);
					} else {
						currencyValue = currencyRepository.findByDateAndCurrecncy(
								formatter.parse(formatter.format(contractStartDate)), contractCurrency);
					}
					if (currencyDTO.getCurrency().equalsIgnoreCase("INR")
							&& !contractCurrency.equalsIgnoreCase("INR")) {
						licenseDetails.setConvertedCost(licenseDetails.getTotalCost().multiply(currencyValue.getInr()));
						licenseDetails = licenseDetailsRepository.save(licenseDetails);
					}
					if (currencyDTO.getCurrency().equalsIgnoreCase("USD")
							&& !contractCurrency.equalsIgnoreCase("USD")) {
						licenseDetails.setConvertedCost(licenseDetails.getTotalCost().multiply(currencyValue.getUsd()));
						licenseDetails = licenseDetailsRepository.save(licenseDetails);
					}
					if (currencyDTO.getCurrency().equalsIgnoreCase("MYR")
							&& !contractCurrency.equalsIgnoreCase("MYR")) {
						licenseDetails.setConvertedCost(licenseDetails.getTotalCost().multiply(currencyValue.getMyr()));
						licenseDetails = licenseDetailsRepository.save(licenseDetails);
					}
					if (currencyDTO.getCurrency().equalsIgnoreCase("PHP")
							&& !contractCurrency.equalsIgnoreCase("PHP")) {
						licenseDetails.setConvertedCost(licenseDetails.getTotalCost().multiply(currencyValue.getPhp()));
						licenseDetails = licenseDetailsRepository.save(licenseDetails);
					}
					if (currencyDTO.getCurrency().equalsIgnoreCase("SGD")
							&& !contractCurrency.equalsIgnoreCase("SGD")) {
						licenseDetails.setConvertedCost(licenseDetails.getTotalCost().multiply(currencyValue.getSgd()));
						licenseDetails = licenseDetailsRepository.save(licenseDetails);
					}
					if (currencyDTO.getCurrency().equalsIgnoreCase("EUR")
							&& !contractCurrency.equalsIgnoreCase("EUR")) {
						licenseDetails.setConvertedCost(licenseDetails.getTotalCost().multiply(currencyValue.getEur()));
						licenseDetails = licenseDetailsRepository.save(licenseDetails);
					}
					if (currencyDTO.getCurrency().equalsIgnoreCase("GBP")
							&& !contractCurrency.equalsIgnoreCase("GBP")) {
						licenseDetails.setConvertedCost(licenseDetails.getTotalCost().multiply(currencyValue.getGbp()));
						licenseDetails = licenseDetailsRepository.save(licenseDetails);
					}
					if (currencyDTO.getCurrency().equalsIgnoreCase("AUD")
							&& !contractCurrency.equalsIgnoreCase("AUD")) {
						licenseDetails.setConvertedCost(licenseDetails.getTotalCost().multiply(currencyValue.getAud()));
						licenseDetails = licenseDetailsRepository.save(licenseDetails);
					}
				}
				updateContractCurrency(licenseDetails);
			}
		}
		return new CommonResponse(HttpStatus.OK, new Response("getCurrencyResponse", ""),
				"Details Updated Successfully");
	}

	@Override
	public CommonResponse adminCurrency() throws DataValidationException {
		Map<String, Object> result = new HashMap<>();
		UserDetails userDetail = userDetailsRepository.getCurrency();
		result.put("updatedTime", userDetail.getUpdatedOn());
		result.put("adminCurrency", userDetail.getCurrency());
		return new CommonResponse(HttpStatus.OK, new Response("adminCurrencyResponse", result),
				"Details Updated Successfully");
	}
	
	private void updateContractCurrency(ApplicationLicenseDetails licenseDetails) {
		List<Applications> applications = applicationRepository.findAll();
		for(Applications application : applications) {
			if(application.getLicenseId()!=null && application.getLicenseId().equals(licenseDetails.getLicenseId())) {
				application.setConvertedCost(licenseDetails.getConvertedCost());
				applicationRepository.save(application);
			}
		}
	}
}
