package saaspe.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import lombok.NonNull;
import saaspe.entity.ApplicationContractDetails;

public interface ApplicationContractDetailsRepository extends JpaRepository<ApplicationContractDetails, String> {

	@Query("select a from ApplicationContractDetails a where a.contractId = :contractId AND a.endDate is null")
	ApplicationContractDetails findByContractId(String contractId);

	@Query("select a from ApplicationContractDetails a where a.contractId = :contractId")
	ApplicationContractDetails getByContractId(String contractId);

	@Query(value="select * from saaspe_application_contract_details a where a.contract_name = :contractName AND a.end_date is null LIMIT 1",nativeQuery = true)
	ApplicationContractDetails findByContractName(String contractName);

	@Query(value = "select * from saaspe_application_contract_details a where a.application_id =:applicationId and a.end_date is null;", nativeQuery = true)
	List<ApplicationContractDetails> findByApplicationId(String applicationId);

	@Query(value = "select * from saaspe_application_contract_details a where a.application_id =:applicationId ;", nativeQuery = true)
	List<ApplicationContractDetails> getContractsByApplicationId(String applicationId);

	@Query("select a from ApplicationContractDetails a where a.contractEndDate between :fromdate and :todate")
	List<ApplicationContractDetails> getByContractEndDate(@NonNull Date fromdate, @NonNull Date todate);

	@Query("select a from ApplicationContractDetails a where (a.autoRenew=true and a.renewalDate between :fromdate and :todate) or (a.autoRenew=false and a.contractEndDate between :fromdate and :todate)")
	List<ApplicationContractDetails> getByContractRenewalDateAndContratEndDate(@NonNull Date fromdate,
			@NonNull Date todate);

	@Query(value = "select * from saaspe_application_contract_details where end_date is null;", nativeQuery = true)
	List<ApplicationContractDetails> getContactDetails();

	@Query(value = "select * from saaspe_application_contract_details where subscription_id =:subscriptionId and end_date is null;", nativeQuery = true)
	List<ApplicationContractDetails> findBySubscriptionId(String subscriptionId);

	@Query(value = "SELECT * FROM saaspe_application_contract_details a where a.contract_name = :contractName and a.end_date is null order by a.created_on desc limit 1", nativeQuery = true)
	ApplicationContractDetails getDeletedContractsByContractName(String contractName);

	@Query(value = "SELECT * from saaspe_application_contract_details a where a.application_id =:applicationId and  a.contract_start_date BETWEEN :yearStartDate AND :currentDate ;", nativeQuery = true)
	List<ApplicationContractDetails> findContractsInCurrentYear(String applicationId, @NonNull Date yearStartDate,
			@NonNull Date currentDate);

	@Query(value = "select * from saaspe_application_contract_details a where a.application_id =:applicationId and a.contract_end_date >=:currentDate and a.end_date is NULL ;", nativeQuery = true)
	List<ApplicationContractDetails> findRemaingContracts(String applicationId, Date currentDate);

	@Query(value = "select * from saaspe_application_contract_details a where a.application_id =:applicationId and a.contract_status = 'Active' and a.end_date is null ;", nativeQuery = true)
	List<ApplicationContractDetails> findActiveContracts(String applicationId);

	@Query(value = "select * from saaspe_application_contract_details a where a.contract_status in ('Active','Expired') ;", nativeQuery = true)
	List<ApplicationContractDetails> findActiveExpiredContracts();

	@Query(value = "select * from saaspe_application_contract_details a where a.contract_status in ('Active','Expired') and a.application_id =:applicationId ;", nativeQuery = true)
	List<ApplicationContractDetails> findActiveExpiredContracts(String applicationId);

	@Query(value = "select * from saaspe_application_contract_details a where a.contract_status = 'Expired' ;", nativeQuery = true)
	List<ApplicationContractDetails> findExpiredContracts(String applicationId);

	@Query(value = "select * from saaspe_application_contract_details a where a.contract_status = 'Active';", nativeQuery = true)
	List<ApplicationContractDetails> findActiveContracts();

	@Query(value = "SELECT * FROM saaspe_application_contract_details a WHERE (a.auto_renew = true AND a.renewal_date >= :today) OR (a.auto_renew = false AND a.contract_end_date >= :today);", nativeQuery = true)
	List<ApplicationContractDetails> findContractsAndRenewals(Date today);

	@Query("SELECT a FROM ApplicationContractDetails a where a.contractEndDate BETWEEN :start AND :end")
	List<ApplicationContractDetails> getAllContractsBtwDates(Date start, Date end);

	@Query("SELECT a FROM ApplicationContractDetails a where a.renewalDate BETWEEN :start AND :end")
	List<ApplicationContractDetails> getAllRenewalsBtwDates(Date start, Date end);

	@Query(value = "select * from (select * from (select * from saaspe_application_contract_details a where a.application_id =:applicationId ) a where a.contract_end_date between :start AND :end OR a.contract_end_date > :end ) b where b.contract_start_date between :start AND :end OR b.contract_start_date < :start ;", nativeQuery = true)
	List<ApplicationContractDetails> findActiveContractsBtwConEndDate(String applicationId, Date start, Date end);

	@Query(value = "select * from (select * from saaspe_application_contract_details a where a.contract_end_date between :start AND :end OR a.contract_end_date > :end ) a where a.contract_start_date between :start AND :end OR a.contract_start_date < :start ;", nativeQuery = true)
	List<ApplicationContractDetails> findActiveConBtwConEndDate(Date start, Date end);

	@Query(value = "select * from (select * from saaspe_application_contract_details a where a.end_date is null and a.contract_status in ('Active','Expired') and a.contract_end_date between :start AND :end OR a.contract_end_date > :end ) a where a.contract_start_date between :start AND :end OR a.contract_start_date < :start ;", nativeQuery = true)
	List<ApplicationContractDetails> getActiveConBtwConEndDate(Date start, Date end);

	@Query(value = "select * from saaspe_application_contract_details a where a.contract_status = 'Active' and a.application_id =:applicationId ", nativeQuery = true)
	List<ApplicationContractDetails> findActiveContractsByAppId(String applicationId);

}
