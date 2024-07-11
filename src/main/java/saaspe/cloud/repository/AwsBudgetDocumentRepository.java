package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.document.AwsBudgetDocument;


public interface AwsBudgetDocumentRepository extends MongoRepository<AwsBudgetDocument, Long> {

    @Query("{'accountId' : :#{#accountId}}")
    List<AwsBudgetDocument> getByBudgetsAccountId(String accountId);

}
