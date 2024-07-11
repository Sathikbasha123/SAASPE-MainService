package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.document.CostManagementQueryMonthlyDocument;

public interface CostManagementQueryMonthlyRepository
		extends MongoRepository<CostManagementQueryMonthlyDocument, Long> {

	@Query("{'name' : ?0}")
	CostManagementQueryMonthlyDocument findByName(String name);

	@Query("{'subscriptionId' : ?0}")
	List<CostManagementQueryMonthlyDocument> findBySubscriptionId(String subscriptionId);

	@Query("{'subscriptionId' : :#{#subscriptionId}}")
	List<CostManagementQueryMonthlyDocument> getBySubscriptionId(String subscriptionId);

}
