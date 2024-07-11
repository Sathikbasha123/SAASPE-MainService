package saaspe.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ApplicationPricingDetails;

public interface ApplicationPricingDetailsRepository extends JpaRepository<ApplicationPricingDetails, String> {

    @Query("select a from ApplicationPricingDetails a where a.licenseId = :licenseId")
    Optional<ApplicationPricingDetails> findById(String licenseId);

}
