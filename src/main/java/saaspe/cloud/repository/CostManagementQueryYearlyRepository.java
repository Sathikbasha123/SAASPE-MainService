package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.document.CostManagementQueryYearlyDocument;

public interface CostManagementQueryYearlyRepository extends MongoRepository<CostManagementQueryYearlyDocument, Long> {

	@Query("{'name' : ?0}")
	CostManagementQueryYearlyDocument findByName(String name);

	@Query("{'subscriptionId' : ?0}")
	CostManagementQueryYearlyDocument findBySubscriptionId(String subscriptionId);

	@Query("{'subscriptionId' : :#{#subscriptionId}}")
	List<CostManagementQueryYearlyDocument> getBySubscriptionId(String subscriptionId);

}
