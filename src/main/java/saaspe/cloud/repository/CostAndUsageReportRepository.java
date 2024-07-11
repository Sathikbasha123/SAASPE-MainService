package saaspe.cloud.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.document.CostManagementUsageDocument;

public interface CostAndUsageReportRepository extends MongoRepository<CostManagementUsageDocument, Long> {

}
