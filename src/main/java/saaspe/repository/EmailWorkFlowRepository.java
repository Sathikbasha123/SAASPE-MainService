package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import saaspe.entity.EmailWorkFlowStatus;

public interface EmailWorkFlowRepository extends JpaRepository<EmailWorkFlowStatus, Long> {

    EmailWorkFlowStatus findByWorkFlowNumber(Long workFlowNumber);

}
