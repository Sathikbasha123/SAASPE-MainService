package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ApplicationLogoEntity;

public interface ApplicationLogoRepository extends JpaRepository<ApplicationLogoEntity, Integer> {

	ApplicationLogoEntity findByApplicationName(String applicationName);

	@Query("select a from ApplicationLogoEntity a where a.cloud = true ")
	List<ApplicationLogoEntity> findCloduVendors();

	@Query("select applicationName from ApplicationLogoEntity")
	List<String> findApplicationName();
}
