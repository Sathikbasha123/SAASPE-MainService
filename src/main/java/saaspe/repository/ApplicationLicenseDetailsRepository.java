package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import saaspe.dto.TopAppsBySpendDAO;
import saaspe.entity.ApplicationLicenseDetails;

public interface ApplicationLicenseDetailsRepository extends JpaRepository<ApplicationLicenseDetails, String> {

	ApplicationLicenseDetails findByLicenseId(String licenseId);

	@Query(value = " select distinct f.appId,f.currency, f.license_cost_per_user*f.count as cost from (\r\n"
			+ "(SELECT count(a.user_email) as count , a.application_id as appId, a.license_id from saaspe_user_details a group by a.license_id, a.application_id  ) as c\r\n"
			+ "     inner join saaspe_application_license_details  b ON c.license_id = b.license_id) as f order by cost desc limit 10;", nativeQuery = true)
	List<TopAppsBySpendDAO> getTopAppsBySpend();

	@Query(value = "select * from saaspe_application_license_details  where application_id = :applicationId and end_date is null", nativeQuery = true)
	List<ApplicationLicenseDetails> getApplicationID(@Param("applicationId") String applicationId);

	@Query(value = "select * from saaspe_application_license_details  where application_id = :applicationId and end_date is null", nativeQuery = true)
	List<ApplicationLicenseDetails> getApplicationIDs(@Param("applicationId") String applicationId);

	@Query(value = "select * from saaspe_application_license_details where license_id = :licenseId and end_Date is null ", nativeQuery = true)
	ApplicationLicenseDetails getUsersDetailsByLicenseId(String licenseId);

	@Query(value = "SELECT * FROM saaspe_application_license_details a WHERE a.contract_id = :contractId ", nativeQuery = true)
	List<ApplicationLicenseDetails> getdByContractId(String contractId);

}