package saaspe.docusign.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.docusign.document.AuditEventDocument;

public interface AuditEventRepository extends MongoRepository<AuditEventDocument, Long>{

	@Query("{'envelopeId' : :#{#envelopeId}}")
	AuditEventDocument findByenvelopeId(String envelopeId);
}
