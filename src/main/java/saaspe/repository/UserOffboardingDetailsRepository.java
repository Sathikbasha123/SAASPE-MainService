package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import saaspe.entity.UserOffboardingDetails;
import saaspe.entity.UserOnboarding;

@Repository
public interface UserOffboardingDetailsRepository extends JpaRepository<UserOffboardingDetails, String> {

	UserOnboarding findByUserId(String userId);

}
