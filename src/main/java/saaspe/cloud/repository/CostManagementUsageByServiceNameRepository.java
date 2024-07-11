package saaspe.cloud.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.document.CostManagementUsageByServiceNameDocument;

public interface CostManagementUsageByServiceNameRepository
		extends MongoRepository<CostManagementUsageByServiceNameDocument, Long> {

	@Query("{'subscriptionId' : ?0}")
	CostManagementUsageByServiceNameDocument findBySubscriptionId(String susbcriptionid);

	@Query("{'subscriptionId' : :#{#subscriptionId}}")
	List<CostManagementUsageByServiceNameDocument> getBySubscriptionId(String subscriptionId);

	@Query("{ updatedOn: { $gte: ?startDate, $lt: ?endDate } }")
	List<CostManagementUsageByServiceNameDocument> findByUpdatedOn(Date startDate, Date endDate);

}
