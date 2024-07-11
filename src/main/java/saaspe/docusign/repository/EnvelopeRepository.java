package saaspe.docusign.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.docusign.document.EnvelopeDocument;

public interface EnvelopeRepository extends MongoRepository<EnvelopeDocument, Long> {

	@Query("{'envelopeId' : :#{#envelopeId}}")
	EnvelopeDocument findByEnvelopeId(String envelopeId);

	@Query("{'envelope.status' : :#{#status}}")
	Page<EnvelopeDocument> findByCustomStatusQuery(String status, Pageable pageable);

	@Query("{ 'envelope.sender.email' : ?0, 'envelope.status' : ?1 }")
	Page<EnvelopeDocument> findByEnvelopeIdAndStatus(String email, String status, Pageable pageable);

	@Query("{ 'envelope.sender.email' : ?0 }")
	List<EnvelopeDocument> findAllBySenderEmail(String email);
}
