package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.VerificationDetails;

public interface VerificationDetailsRepository extends JpaRepository<VerificationDetails, String> {

	VerificationDetails findByUserEmail(String email);

	VerificationDetails findByRefreshToken(String refreshToken);

	@Query("SELECT a FROM VerificationDetails a WHERE a.failedCount IS NOT NULL")
	List<VerificationDetails> findAllWithFailedCountNull();

}
