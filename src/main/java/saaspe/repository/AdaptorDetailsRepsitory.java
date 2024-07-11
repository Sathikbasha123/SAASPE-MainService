package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import saaspe.entity.AdaptorDetails;

@Repository
public interface AdaptorDetailsRepsitory extends JpaRepository<AdaptorDetails, Long> {

	AdaptorDetails findByApplicationId(String appId);

	AdaptorDetails findByApplicationName(String appName);

	@Query("select Max(a.id) from AdaptorDetails a")
	Long findLatestId();
	
	boolean existsByApplicationId(String appId);
}
