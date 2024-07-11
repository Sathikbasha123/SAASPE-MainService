package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import saaspe.entity.Applications;

@Repository
public interface ApplicationsRepository extends JpaRepository<Applications, Integer> {
	
	@Query(value="SELECT * FROM saaspe_applications WHERE application_id= :applicationId",nativeQuery = true)
	List<Applications> findByApplicationId(String applicationId);
	
	@Query(value="SELECT * FROM saaspe_applications WHERE contract_id= :contractId",nativeQuery = true)
	List<Applications> findByContractId(String contractId);
	
	@Query(value="SELECT * FROM saaspe_applications WHERE license_id= :licenseId",nativeQuery = true)
	Applications findByLicenseId(String licenseId);
	
	@Query(value="SELECT * FROM saaspe_applications WHERE application_id= :applicationId ORDER BY application_id LIMIT 1",nativeQuery = true)
	Applications findByAppId(String applicationId);

}
