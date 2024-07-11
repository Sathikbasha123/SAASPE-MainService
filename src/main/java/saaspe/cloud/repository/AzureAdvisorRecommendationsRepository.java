package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.document.AdvisorDocument;

public interface AzureAdvisorRecommendationsRepository extends MongoRepository<AdvisorDocument, String> {

	@Query("{'subscriptionId' : :#{#subscriptionId}}")
	List<AdvisorDocument> findBySubscriptionId(String subscriptionId);

}
