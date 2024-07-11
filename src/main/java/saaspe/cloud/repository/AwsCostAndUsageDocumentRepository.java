package saaspe.cloud.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.document.AwsCostAndUsageDocument;

public interface AwsCostAndUsageDocumentRepository extends MongoRepository<AwsCostAndUsageDocument, Long> {

	@Query("{'accountId' : :#{#accountId}}")
	List<AwsCostAndUsageDocument> getRecordsByAccountId(String accountId);

	List<AwsCostAndUsageDocument> findByCreatedOn(Date date);

}
