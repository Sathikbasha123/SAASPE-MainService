package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ClMContractEntity;

public interface ClMContractRepository extends JpaRepository<ClMContractEntity, Long> {

	@Query(value = "select * from clm_contract_entity  a where a.enevelope_id =:envelopeId ;", nativeQuery = true)
	ClMContractEntity findByEnvelopeId(String envelopeId);

}
