package saaspe.docusign.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.docusign.document.CreateTemplate;

public interface CreateTemplateRepository extends MongoRepository<CreateTemplate, Long> {

	CreateTemplate findByTemplateId(String templateId);
}
