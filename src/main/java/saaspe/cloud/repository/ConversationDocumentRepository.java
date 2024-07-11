package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.document.ConversationDocument;

public interface ConversationDocumentRepository extends MongoRepository<ConversationDocument, Long> {

	List<ConversationDocument> findByConversationId(String conversatoinId);

}
