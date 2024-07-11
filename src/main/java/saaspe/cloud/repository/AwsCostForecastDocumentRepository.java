package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import saaspe.document.AwsCostForecastDocument;

@Repository
public interface AwsCostForecastDocumentRepository extends MongoRepository<AwsCostForecastDocument, Long> {

    @Query("{'accountId' : :#{#accountId}}")
    List<AwsCostForecastDocument> getByAccountId(String tenantId);

}
