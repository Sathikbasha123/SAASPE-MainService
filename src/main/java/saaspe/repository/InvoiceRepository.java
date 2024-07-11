package saaspe.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.InvoiceDetails;

public interface InvoiceRepository extends JpaRepository<InvoiceDetails, String> {


	@Query(value = "Select * from invoice_details;", nativeQuery = true)
	List<InvoiceDetails> getInvoceDetailsListview();

	@Query(value = "Select * from invoice_details where invoice_number =:invoiceId ;", nativeQuery = true)
	InvoiceDetails getInvoceById(String invoiceId);

	@Modifying
	@Transactional
	@Query("delete from InvoiceDetails where invoiceNumber=:invoiceId")
	void deleteByInvoiceId(String invoiceId);

}
