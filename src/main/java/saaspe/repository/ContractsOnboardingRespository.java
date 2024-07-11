package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ContractOnboardingDetails;

public interface ContractsOnboardingRespository extends JpaRepository<ContractOnboardingDetails, Integer> {

	@Query("SELECT a FROM ContractOnboardingDetails a where a.requestNumber=:requestId and a.workGroup =:role and a.approvedRejected =:status")
	ContractOnboardingDetails findByRequestNumber(String requestId, String role, String status);

	@Query("SELECT a FROM ContractOnboardingDetails a where a.childRequestNumber=:childRequestId and a.workGroup =:role and a.approvedRejected =:status")
	ContractOnboardingDetails findByChildRequestNumber(String childRequestId, String role, String status);

	@Query(value = "SELECT * FROM SAASPE_CONTRACT_ONBOARDING where contract_name =:contractName order by created_on desc limit 1", nativeQuery = true)
	ContractOnboardingDetails findAllByContractName(String contractName);

	@Query(value = "SELECT * FROM saaspe_contract_onboarding a where a.work_group =:role and a.approved_rejected =:key ORDER BY CAST(REPLACE(SUBSTRING(request_number, 9), '_', '') AS INTEGER) ASC,CAST(REPLACE(SUBSTRING(child_request_number, 9), '_', '') AS INTEGER) ASC;", nativeQuery = true)
	List<ContractOnboardingDetails> getAllByName(String role, String key);

	@Query(value = "SELECT * FROM saaspe_contract_onboarding a where a.work_group IN ('Approver','Reviewer') and a.approved_rejected ='Review' ORDER BY CAST(REPLACE(SUBSTRING(request_number, 9), '_', '') AS INTEGER) ASC, CAST(REPLACE(SUBSTRING(child_request_number, 9), '_', '') AS INTEGER) ASC;", nativeQuery = true)
	List<ContractOnboardingDetails> findAllSuperAdminListView();

	@Query("SELECT a FROM ContractOnboardingDetails a where a.requestNumber=:requestId and a.workGroup IN ('Approver','Reviewer') and a.approvedRejected ='Review'")
	ContractOnboardingDetails findAllBySuperAdminRequestId(String requestId);

	@Query(value = "SELECT * FROM saaspe_contract_onboarding a where a.contract_name = :contractName order by a.created_on desc limit 1", nativeQuery = true)
	ContractOnboardingDetails findByContractName(String contractName);

	@Query(value = "SELECT * FROM SAASPE_CONTRACT_ONBOARDING where request_number =:requestId order by created_on desc limit 1", nativeQuery = true)
	ContractOnboardingDetails findByRequestIdForApplication(String requestId);

	@Query("SELECT a FROM ContractOnboardingDetails a where a.applicationId =:applicationId")
	List<ContractOnboardingDetails> findByApplicationId(String applicationId);
	
	@Query(value="select * from  saaspe_contract_onboarding where request_number =:requestId order by created_on desc limit 1",nativeQuery=true)
	ContractOnboardingDetails findByRequest(String requestId);

}
