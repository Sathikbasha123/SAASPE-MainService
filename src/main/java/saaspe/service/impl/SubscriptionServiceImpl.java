package saaspe.service.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import saaspe.entity.ApplicationContractDetails;
import saaspe.entity.ApplicationLicenseDetails;
import saaspe.entity.ApplicationLogoEntity;
import saaspe.entity.ApplicationSubscriptionDetails;
import saaspe.exception.DataValidationException;
import saaspe.model.ApplicationSubscriptionDetailsResponse;
import saaspe.model.CommonResponse;
import saaspe.model.ContractsUnderSubscription;
import saaspe.model.RenewalsListResponse;
import saaspe.model.Response;
import saaspe.repository.ApplicationContractDetailsRepository;
import saaspe.repository.ApplicationLogoRepository;
import saaspe.repository.ApplicationSubscriptionDetailsRepository;
import saaspe.service.SubscriptionService;
import saaspe.utils.CommonUtil;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

	@Autowired
	private ApplicationSubscriptionDetailsRepository applicationSubscriptionDetailsRepository;

	@Autowired
	private ApplicationContractDetailsRepository applicationContractDetailsRepository;

	@Autowired
	private ApplicationLogoRepository applicationLogoRepository;

	@Override
	public CommonResponse getApplicationSubscriptionDetails() throws DataValidationException {
		List<ApplicationSubscriptionDetailsResponse> list = new ArrayList<>();
		List<ApplicationSubscriptionDetails> applicationSubscriptionDetails = applicationSubscriptionDetailsRepository
				.findRemainingubscriptions();
		if (!applicationSubscriptionDetails.isEmpty()) {
			for (ApplicationSubscriptionDetails details : applicationSubscriptionDetails) {
				List<ApplicationContractDetails> applicationContractDetails = applicationContractDetailsRepository
						.findBySubscriptionId(details.getSubscriptionId());
				if (!applicationContractDetails.isEmpty()) {
					String contractName = null;
					List<ContractsUnderSubscription> contractsUnderSubscriptions = new ArrayList<>();
					ApplicationSubscriptionDetailsResponse detailsResponse = new ApplicationSubscriptionDetailsResponse();
					detailsResponse.setSubscriptionName(details.getSubscriptionName());
					detailsResponse.setSubscriptionId(details.getSubscriptionId());
					detailsResponse.setApplicationName(details.getApplicationId().getApplicationName());
					detailsResponse.setApplicationLogo(details.getApplicationId().getLogoUrl());
					detailsResponse.setSubscriptionNumber(details.getSubscriptionNumber());
					ApplicationLogoEntity applicationLogoEntity = applicationLogoRepository
							.findByApplicationName(details.getApplicationId().getApplicationName());
					detailsResponse.setProviderName(applicationLogoEntity.getProviderId().getProviderName());
					detailsResponse.setProviderLogo(applicationLogoEntity.getProviderId().getLogoUrl());
					
					for (ApplicationContractDetails contractDetails : applicationContractDetails) {
						if(contractDetails.getContractStatus().equalsIgnoreCase("Active")) {
							contractName = contractDetails.getContractName();
							BigDecimal totalCost = BigDecimal.valueOf(0);
							BigDecimal adminCost = BigDecimal.valueOf(0);
							for (ApplicationLicenseDetails licenseDetails : contractDetails.getLicenseDetails()) {
								totalCost = totalCost.add(licenseDetails.getTotalCost(), MathContext.DECIMAL32);
								adminCost = adminCost.add(licenseDetails.getConvertedCost(), MathContext.DECIMAL32);
							}
							ContractsUnderSubscription contractsUnderSubscription = new ContractsUnderSubscription();
							contractsUnderSubscription.setContractName(contractName);
							contractsUnderSubscription.setContractStatus(contractDetails.getContractStatus());
							contractsUnderSubscriptions.add(contractsUnderSubscription);
							detailsResponse.setContracts(contractsUnderSubscriptions);
							detailsResponse.setTotalCost(totalCost);
							detailsResponse.setAdminCost(adminCost);
							detailsResponse.setCurrency(contractDetails.getContractCurrency());
							if(!list.contains(detailsResponse))
								list.add(detailsResponse);
						}
					}
				}
			}
		} else {
			throw new DataValidationException("No Subscriptions!", null, null);
		}
		return new CommonResponse(HttpStatus.OK, new Response("subscriptionListResponse", list),
				"Workflow action completed");
	}

	@Override
	public CommonResponse getApplicationSubscriptionDetailsListview() throws DataValidationException {
		List<RenewalsListResponse> list = new ArrayList<>();
		LocalDate today = LocalDate.now();
		Date currentDate = CommonUtil.convertLocalDatetoDate(today);
		List<ApplicationContractDetails> applicationContractDetails = applicationContractDetailsRepository
				.findContractsAndRenewals(currentDate);
		List<ApplicationSubscriptionDetails> applicationSubscriptionDetails = applicationSubscriptionDetailsRepository
				.findAll();
		if (applicationSubscriptionDetails.isEmpty()) {
			throw new DataValidationException("No subscriptions found", null, null);
		}
		for (ApplicationContractDetails contractDetails : applicationContractDetails) {
			RenewalsListResponse renewalsListResponse = new RenewalsListResponse();
			renewalsListResponse.setContractName(contractDetails.getContractName());
			renewalsListResponse.setContractId(contractDetails.getContractId());
			if (Boolean.TRUE.equals(contractDetails.getAutoRenew())) {
				renewalsListResponse.setUpcomingRenewalDate(contractDetails.getRenewalDate());
			} else {
				renewalsListResponse.setUpcomingRenewalDate(contractDetails.getContractEndDate());
			}
			renewalsListResponse.setReminderDate(contractDetails.getReminderDate());
			ApplicationSubscriptionDetails subscriptionDetails = applicationSubscriptionDetailsRepository
					.getByApplicationId(contractDetails.getApplicationId().getApplicationId());
			if (subscriptionDetails != null) {
				renewalsListResponse.setSubscriptionName(subscriptionDetails.getSubscriptionName());
			}
			renewalsListResponse.setApplicationName(contractDetails.getApplicationId().getApplicationName());
			renewalsListResponse.setApplicationLogo(contractDetails.getApplicationId().getLogoUrl());
			renewalsListResponse.setCurrency(contractDetails.getContractCurrency());

			ZoneId defaultZoneId = ZoneId.systemDefault();
			LocalDate date = LocalDate.now().plusDays(14);
			Date before = Date.from(date.atStartOfDay(defaultZoneId).toInstant());
			renewalsListResponse.setPaymentEnable(contractDetails.getContractEndDate().after(new Date())
					&& contractDetails.getContractEndDate().before(before));
			BigDecimal totalCost = BigDecimal.valueOf(0);
			BigDecimal adminCost = BigDecimal.valueOf(0);
			for (ApplicationLicenseDetails licenseDetails : contractDetails.getLicenseDetails()) {
				totalCost = totalCost.add(licenseDetails.getTotalCost(), MathContext.DECIMAL32);
				adminCost = adminCost.add(licenseDetails.getConvertedCost(), MathContext.DECIMAL32);
			}
			renewalsListResponse.setTotalCost(totalCost);
			renewalsListResponse.setAdminCost(adminCost);
			list.add(renewalsListResponse);
		}
		return new CommonResponse(HttpStatus.OK, new Response("RenewalsListResponse", list),
				"Details retrieved successfully");
	}

	@Override
	public CommonResponse getListOfSubscriptions() throws DataValidationException {
		List<ApplicationSubscriptionDetails> applicationSubscriptionDetails = applicationSubscriptionDetailsRepository
				.findAll();
		List<ApplicationSubscriptionDetailsResponse> applicationSubscriptionDetailsResponses = new ArrayList<>();
		for (ApplicationSubscriptionDetails details : applicationSubscriptionDetails) {
			ApplicationSubscriptionDetailsResponse detailsResponse = new ApplicationSubscriptionDetailsResponse();
			detailsResponse.setSubscriptionId(details.getSubscriptionId());
			detailsResponse.setSubscriptionName(details.getSubscriptionName());
			detailsResponse.setSubscriptionNumber(details.getSubscriptionNumber());
			applicationSubscriptionDetailsResponses.add(detailsResponse);
		}
		return new CommonResponse(HttpStatus.OK,
				new Response("SubscriptionLookUpResponse", applicationSubscriptionDetailsResponses),
				"Details retrieved successfully");
	}

}
