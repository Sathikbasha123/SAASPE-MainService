package saaspe.docusign.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.docusign.document.EventDocument;

public interface EventRepository extends MongoRepository<EventDocument, Long>{

}
