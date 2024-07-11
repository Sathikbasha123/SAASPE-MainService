package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import saaspe.entity.AdaptorCredential;

@Repository
public interface AdaptorCredentialRepository extends JpaRepository<AdaptorCredential, Integer> {
	AdaptorCredential findByApplicationName(String applicationName);

	boolean existsByApplicationName(String applicationName);

}
