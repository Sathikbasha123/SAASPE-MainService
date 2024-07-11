package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.document.BudgetsDocument;

public interface BudgetRepository extends MongoRepository<BudgetsDocument, Long> {

    @Query("{'subscriptionId' : :#{#subscriptionId}}")
    List<BudgetsDocument> findBySubscriptionId(String subscriptionId);

    @Query("{ 'name' : ?0 }")
	BudgetsDocument findByBudgetName(String name);
    
}
