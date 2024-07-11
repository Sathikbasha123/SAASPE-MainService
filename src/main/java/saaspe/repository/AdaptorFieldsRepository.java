package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import saaspe.entity.AdaptorFields;

public interface AdaptorFieldsRepository extends JpaRepository<AdaptorFields, Integer> {

	boolean existsByApplicationNameIgnoreCase(String applicationName);
	
	AdaptorFields findByApplicationNameIgnoreCase(String applicationName);
}

