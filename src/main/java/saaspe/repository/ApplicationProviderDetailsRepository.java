package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ApplicationProviderDetails;

public interface ApplicationProviderDetailsRepository extends JpaRepository<ApplicationProviderDetails, String> {

	@Query("SELECT a FROM ApplicationProviderDetails a WHERE a.providerId = :providerId")
	ApplicationProviderDetails findByProviderId(String providerId);

	@Query("SELECT a FROM ApplicationProviderDetails a WHERE a.providerName = :providerName")
	ApplicationProviderDetails findByProviderName(String providerName);

}
