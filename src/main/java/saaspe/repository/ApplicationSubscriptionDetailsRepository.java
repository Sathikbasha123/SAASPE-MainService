package saaspe.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ApplicationSubscriptionDetails;

public interface ApplicationSubscriptionDetailsRepository
		extends JpaRepository<ApplicationSubscriptionDetails, String> {

	@Query(value = "select * from saaspe_application_subscription_details  where application_id = :applicationId AND end_date is null", nativeQuery = true)
	ApplicationSubscriptionDetails findByApplicationId(String applicationId);

	@Query("SELECT a FROM ApplicationSubscriptionDetails a where a.endDate is null")
	List<ApplicationSubscriptionDetails> findRemainingubscriptions();

	@Query("SELECT a FROM ApplicationSubscriptionDetails a where a.createdOn BETWEEN :start AND :end and a.endDate is null")
	List<ApplicationSubscriptionDetails> getAllSubscriptionBtwDates(Date start, Date end);

	@Query("select a from ApplicationSubscriptionDetails a where a.subscriptionId = :subscriptionId")
	ApplicationSubscriptionDetails findBySubscriptionId(String subscriptionId);

	@Query("select a from ApplicationSubscriptionDetails a where a.subscriptionNumber = :subscriptionNumber")
	ApplicationSubscriptionDetails findBySubscriptionNumber(String subscriptionNumber);

	@Query(value = "select * from saaspe_application_subscription_details  where application_id = :applicationId ", nativeQuery = true)
	ApplicationSubscriptionDetails getByApplicationId(String applicationId);

}