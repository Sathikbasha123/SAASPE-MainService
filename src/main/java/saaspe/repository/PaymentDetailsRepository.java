package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.PaymentDetails;

public interface PaymentDetailsRepository extends JpaRepository<PaymentDetails, String> {

	@Query("select a from PaymentDetails a where a.applicationId = :applicationId ")
	List<PaymentDetails> findByApplicationId(String applicationId);

	@Query("select a from PaymentDetails a where a.contractId = :contractId ")
	PaymentDetails findByContractId(String contractId);

}
