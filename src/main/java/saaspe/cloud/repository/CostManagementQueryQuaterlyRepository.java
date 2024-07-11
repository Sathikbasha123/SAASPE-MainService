package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.document.CostManagementQueryQuaterlyDocument;

public interface CostManagementQueryQuaterlyRepository
		extends MongoRepository<CostManagementQueryQuaterlyDocument, Long> {

	@Query("{'name' : ?0}")
	CostManagementQueryQuaterlyDocument findByName(String name);

	@Query("{'subscriptionId' : ?0}")
	CostManagementQueryQuaterlyDocument findBySubscriptionId(String subscriptionId);

	@Query("{'subscriptionId' : :#{#subscriptionId}}")
	List<CostManagementQueryQuaterlyDocument> getBySubscriptionId(String subscriptionId);

}
