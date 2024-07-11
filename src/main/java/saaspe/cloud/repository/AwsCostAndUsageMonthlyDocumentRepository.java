package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.document.AwsCostAndUsageMonthlyDocument;

public interface AwsCostAndUsageMonthlyDocumentRepository
		extends MongoRepository<AwsCostAndUsageMonthlyDocument, Long> {

	@Query("{'accountId' : :#{#accountId}}")
	List<AwsCostAndUsageMonthlyDocument> getByAccountId(String tenantId);

	@Query("{'subscriptionId' : ?0}")
	List<AwsCostAndUsageMonthlyDocument> findBySubscriptionId(String subscriptionId);

	@Query("{'subscriptionId' : :#{#subscriptionId}}")
	List<AwsCostAndUsageMonthlyDocument> getBySubscriptionId(String subscriptionId);

}
