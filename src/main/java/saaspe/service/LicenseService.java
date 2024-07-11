package saaspe.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import saaspe.dto.UserLicenseAssignDto;
import saaspe.exception.DataValidationException;
import saaspe.model.ApplicationsLicenseCountRequest;
import saaspe.model.CommonResponse;

public interface LicenseService {

    CommonResponse getUsersDetailsByLicebseId(String licenseId) throws DataValidationException;

    CommonResponse linkUserLicense(UserLicenseAssignDto licenseAssignDto) throws DataValidationException, JsonProcessingException;

    CommonResponse getAppplicationsLicenseCount(ApplicationsLicenseCountRequest applicationLicenseDetails)
            throws DataValidationException;

}
