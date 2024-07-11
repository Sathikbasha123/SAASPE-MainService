package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.SequenceGenerator;

public interface SequenceGeneratorRepository extends JpaRepository<SequenceGenerator, Integer> {

	SequenceGenerator getById(Integer id);

	@Query("SELECT a.tenantSequence FROM saaspe.entity.SequenceGenerator a")
	Integer getTenantSequence();

	@Query("SELECT a.departmentSequence FROM saaspe.entity.SequenceGenerator a")
	Integer getDepartmentSequence();

	@Query("SELECT a.userOnboarding FROM saaspe.entity.SequenceGenerator a")
	Integer getUserOnboardingSequence();

	@Query("SELECT a.conversationsequenceId FROM saaspe.entity.SequenceGenerator a")
	Integer getconversationOnboardingSequence();

	@Query("SELECT a.applicationContacts FROM saaspe.entity.SequenceGenerator a")
	Integer getContractSequence();

	@Query("SELECT a.applicationDetails FROM saaspe.entity.SequenceGenerator a")
	Integer getApplicatiionDetailSequence();

	@Query("SELECT a.applicatiionLicense FROM saaspe.entity.SequenceGenerator a")
	Integer getLicenseSequence();

	@Query("SELECT a.requestId FROM saaspe.entity.SequenceGenerator a")
	Integer getRequestNumberSequence();

	@Query("SELECT a.applicationSubscription FROM saaspe.entity.SequenceGenerator a")
	Integer getApplicationSubscriptionSequence();

	@Query("SELECT a.deptRequestId FROM saaspe.entity.SequenceGenerator a")
	Integer getDeptReqSequence();

	@Query("SELECT a.userRequestId FROM saaspe.entity.SequenceGenerator a")
	Integer getUserReqSequence();

	@Query("SELECT a.paymentSequenceId FROM saaspe.entity.SequenceGenerator a")
	Integer getPaymentReqSequence();

	@Query("SELECT a.projectSequenceId FROM saaspe.entity.SequenceGenerator a")
	Integer getProjectSequence();

	@Query("SELECT a.contractRequestSequenceId FROM saaspe.entity.SequenceGenerator a")
	Integer getContractRequestNumberSequence();

	@Query("SELECT a.cloudSequenceId from saaspe.entity.SequenceGenerator a")
	Integer getCloudSequence();

	@Query("SELECT a.enquirySequenceId FROM saaspe.entity.SequenceGenerator a")
	Integer getUserEnquirySequence();

}
