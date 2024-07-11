package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import saaspe.entity.EnquiryDetails;

@Service
public interface EnquiryDetailRepository extends JpaRepository<EnquiryDetails, String>{

}
