package saaspe.cloud.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.document.AzureSpendingHistoryDocument;

public interface AzureSpendHistoryRepository extends MongoRepository<AzureSpendingHistoryDocument, String>{

	@Query("{'cloudProvider' : ?0}")
	AzureSpendingHistoryDocument findByCloudProvider(String cloudProvider);

	@Query("{'monthName' : ?0, 'year' : ?1}")
	AzureSpendingHistoryDocument findByMonthAndYear(String monthName, int year);

}
