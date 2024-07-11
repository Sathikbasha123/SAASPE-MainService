package saaspe.service;

import saaspe.entity.TenantDetails;
import saaspe.model.CommonResponse;

public interface TenantService {

	CommonResponse getTenantDetails();

	CommonResponse addDepartment(TenantDetails detailsRequest);

	CommonResponse getTenantDetailById(String id);

    void deleteTenantById(String id);

    void modifyDepartment(TenantDetails detailsRequest);

}
