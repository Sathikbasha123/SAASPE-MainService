package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ApplicationOwnerDetails;

public interface ApplicationOwnerRepository extends JpaRepository<ApplicationOwnerDetails, Integer> {

	@Query(value = "SELECT a FROM ApplicationOwnerDetails a WHERE a.applicationId =:id ")
	List<ApplicationOwnerDetails> findByApplicationId(String id);
	
	@Query(value = "SELECT a FROM ApplicationOwnerDetails a WHERE a.ownerEmail =:ownerEmail and a.endDate is null")
	List<ApplicationOwnerDetails> findByEmailId(String ownerEmail);
	
	@Query(value = "SELECT * FROM saaspe_application_owner_details a WHERE a.application_id =:id AND a.end_date is null ORDER BY a.priority ASC LIMIT 1", nativeQuery = true)
	ApplicationOwnerDetails findByAppId(String id);

}
