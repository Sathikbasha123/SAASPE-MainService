package saaspe.cloud.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.document.CostManagementDocument;

public interface CostManagementRepository extends MongoRepository<CostManagementDocument, Long> {

}
