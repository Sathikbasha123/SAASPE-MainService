package saaspe.docusign.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.docusign.document.ClmContractDocument;
import saaspe.entity.ClMContractEntity;

public interface ClmContractDocumentRepository extends MongoRepository<ClmContractDocument, Long>{

	@Query("{'envelopeId' : :#{#envelopeId}}")
	ClmContractDocument findByEnvelopeId(String envelopeId);

	List<ClmContractDocument> findByUniqueString(String uniqueString);

	List<ClmContractDocument> findTop10BySenderEmailOrderByCreatedOnDesc(String senderEmail);

	@Query("{'createdBy': ?0, $or: [ {'status': {$ne: 'voided'}},{'newEnvelopeId': {$exists: false}},{$and: [ {'status': 'voided'}, {'newEnvelopeId': {$exists: true, $eq: null}}]}]}")
	List<ClmContractDocument> findTop10ByCreatedByOrderByCreatedOnDesc(String email, Sort sort);
	
	@Query("{$or: [ {'status': {$ne: 'voided'}},{'newEnvelopeId': {$exists: false}},{$and: [ {'status': 'voided'}, {'newEnvelopeId': {$exists: true, $eq: null}}]}]}")
	List<ClmContractDocument> findTop10OrderByCreatedOnDesc(Sort sort);

	@Query("{'contractEndDate' : { $lt: ?0 }}")
	List<ClmContractDocument> findTop10ContractsBeforeTodayOrderByContractEndDateDesc(Date today);

	@Query("{'contractEndDate': { $lt: ?0 }, 'createdBy': ?1, $or: [ {'status': {$ne: 'voided'}}, {'newEnvelopeId': {$exists: false}}, {$and: [ {'status': 'voided'}, {'newEnvelopeId': {$exists: true, $eq: null}}]}]}")
	List<ClmContractDocument> findTop10ByContractEndDateLessThanAndCreatedByOrderByContractEndDateDesc(Date today,
			String email);
	
	@Query("{'contractEndDate': { $lt: ?0 }, $or: [ {'status': {$ne: 'voided'}}, {'newEnvelopeId': {$exists: false}}, {$and: [ {'status': 'voided'}, {'newEnvelopeId': {$exists: true, $eq: null}}]}]}")
	List<ClmContractDocument> findTop10ByContractEndDateLessThanOrderByContractEndDateDesc(Date today);

	List<ClmContractDocument> findAllBySenderEmail(String email);
	
	List<ClmContractDocument> findAllByCreatedBy(String email);

	Page<ClmContractDocument> findBySenderEmail(String senderEmail, Pageable pageable);
	
	Page<ClmContractDocument> findByCreatedBy(String email, Pageable pageable);

	@Query("{ $and: [ {'?0': { $regex: ?1, $options: 'i' }},{'senderEmail':?2} ]}")
	List<ClmContractDocument> findByCollatedField(String fieldname, String regex, String email, Pageable page,
			Collation collation);

	@Query("{ $and: [ {'?0': { $regex: ?1, $options: 'i' }},{'senderEmail':?2}, {'status': ?3} ] }")
	List<ClmContractDocument> findByCollatedFieldByStatus(String fieldName, String regex, String email, String status,
			Pageable page, Collation collation);

	@Query("{ $and: [ {'status':?0},{'senderEmail':?1} ]}")
	List<ClmContractDocument> findAllContractsByStatus(Pageable pageablee, String status, String email);

	@Query("{'senderEmail':?0}")
	Page<ClmContractDocument> findAllContracts(String email, Pageable pageable);

	@Query(value = "{ $and: [ { ?0: { $regex: ?1, $options: 'i' } }, { 'senderEmail': ?2 }, { 'status': ?3 } ] }", count = true)
	long countByCollatedFieldByStatus(String fieldName, String regex, String email, String status, Collation collation);

	@Query(value = "{ $and: [ { ?0: { $regex: ?1, $options: 'i' } }, { 'senderEmail': ?2 } ] }", count = true)
	long countByCollatedField(String fieldname, String regex, String email, Collation collation);

	long countByStatusAndSenderEmail(String status, String email);

	long countBySenderEmail(String email);

	@Query("{ 'envelopeId' : { $in: ?0 } }")
	List<ClmContractDocument> findByEnvelopeIdIn(List<String> listOfEnvelopeIds);

	Page<ClmContractDocument> findBySenderEmailOrderByContractNameAsc(String senderEmail, Pageable pageable);

	Page<ClmContractDocument> findBySenderEmailAndStatusOrderByContractNameAsc(String senderEmail, String status,
			Pageable pageable);

	Page<ClmContractDocument> findBySenderEmailOrderByContractNameDesc(String senderEmail, Pageable pageable);

	Page<ClmContractDocument> findBySenderEmailAndStatusOrderByContractNameDesc(String senderEmail, String status,
			Pageable pageable);

	@Query(value = "{'senderEmail': ?0,'contractName': { $regex: ?1, $options: 'i' }}")
	Page<ClmContractDocument> findBySenderEmailAndContractNameStartingWithIgnoreCase(String senderEmail,
			String searchText, Pageable pageable);

	@Query(value = "{'senderEmail': ?0,'contractName': { $regex: ?1, $options: 'i' },'status':?2}")
	Page<ClmContractDocument> findBySenderEmailAndStatusAndContractNameStartingWithIgnoreCase(String senderEmail,
			String searchText, String status, Pageable pageable);

	@Query("{ $and :[ {'$or':[ {'contractName': { $regex:?1,$options:'i' } },{'startDate': { $regex:?1,$options:'i' } },{'completedDate': { $regex:?1,$options:'i' }} ]}, {'status': ?2}] }")
	List<ClmContractDocument> findByField(String orderBy, String searchText, String status,
			PageRequest pageable, Collation collation);
	
	@Query("{'createdBy': ?0, $and :[ {'$or':[ {'contractName': { $regex:?2,$options:'i' } },{'startDate': { $regex:?2,$options:'i' } },{'completedDate': { $regex:?2,$options:'i' }} ]}, {'status': ?3}] }")
	List<ClmContractDocument> findByCreatedByAndField(String email, String orderBy, String searchText, String status,
			PageRequest pageable, Collation collation);

	@Query("{ $and :[ {'$or':[ {'contractName': { $regex:?1,$options:'i' } },{'startDate': { $regex:?1,$options:'i' } },{'completedDate': { $regex:?1,$options:'i' }} ]}, {'status': ?2}] }")
	List<ClmContractDocument> findByFieldCount(String orderBy, String searchText, String status,
			 Collation collation);
	
	@Query("{'createdBy': ?0, $and :[ {'$or':[ {'contractName': { $regex:?2,$options:'i' } },{'startDate': { $regex:?2,$options:'i' } },{'completedDate': { $regex:?2,$options:'i' }} ]}, {'status': ?3}] }")
	List<ClmContractDocument> findByCreatedByAndFieldCount(String email, String orderBy, String searchText, String status,
			 Collation collation);
	
	@Query("{ $and :[ {'$or':[ {'contractName': { $regex:?1,$options:'i' } },{'startDate': { $regex:?1,$options:'i' } },{'completedDate': { $regex:?1,$options:'i' }} ]}] }")
	List<ClmContractDocument> findByFieldWithoutStatus(String orderBy, String searchText,
			PageRequest pageable, Collation collation);
	
	@Query("{'createdBy': ?0, $and :[ {'$or':[ {'contractName': { $regex:?1,$options:'i' } },{'startDate': { $regex:?1,$options:'i' } },{'completedDate': { $regex:?1,$options:'i' }} ]}] }")
	List<ClmContractDocument> findByCreatedByAndFieldWithoutStatus(String email, String orderBy, String searchText,
			PageRequest pageable, Collation collation);
	
	@Query("{ $and :[ {'$or':[ {'contractName': { $regex:?1,$options:'i' } },{'startDate': { $regex:?1,$options:'i' } },{'completedDate': { $regex:?1,$options:'i' }} ]}] }")
	List<ClmContractDocument> findByFieldWithoutStatusCount(String orderBy, String searchText,
			 Collation collation);
	
	@Query("{'createdBy': ?0, $and :[ {'$or':[ {'contractName': { $regex:?1,$options:'i' } },{'startDate': { $regex:?1,$options:'i' } },{'completedDate': { $regex:?1,$options:'i' }} ]}] }")
	List<ClmContractDocument> findByCreatedByAndFieldWithoutStatusCount(String email, String orderBy, String searchText,
			 Collation collation);
	
}
