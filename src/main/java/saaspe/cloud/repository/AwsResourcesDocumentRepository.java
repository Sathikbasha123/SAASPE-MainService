package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.document.AwsResourcesDocument;

public interface AwsResourcesDocumentRepository extends MongoRepository<AwsResourcesDocument, Long> {

    @Query("{ 'accountId' : :#{#accountId}}")
    List<AwsResourcesDocument> getresoucresByResourcesGroup(String accountId);

}
