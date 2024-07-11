package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.document.ResourceGroupsActualCostDocument;

public interface ResourceGroupsActualCostRepository extends MongoRepository<ResourceGroupsActualCostDocument, Long> {

	@Query("{'subscriptionId' : ?0}")
	ResourceGroupsActualCostDocument findBySubscriptionId(String subscriptionId);

	@Query("{'subscriptionId' : :#{#subscriptionId}}")
	List<ResourceGroupsActualCostDocument> getBySubscriptionId(String subscriptionId);

}
