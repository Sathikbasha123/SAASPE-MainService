package saaspe.cloud.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.document.BudgetsDocument;

public interface MulticloudBudgetRepository extends MongoRepository<BudgetsDocument, Long> {

	List<BudgetsDocument> findByUpdatedOn(Date date);

}
