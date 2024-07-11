package saaspe;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import saaspe.entity.SequenceGenerator;
import saaspe.repository.SequenceGeneratorRepository;

//@Component
public class SequenceGeneratorDataInsert implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SequenceGeneratorDataInsert.class);

	@Autowired
	private SequenceGeneratorRepository sequenceGeneratorRepository;

	@Override
	public void run(String... args) throws Exception {
		List<SequenceGenerator> generators = sequenceGeneratorRepository.findAll();
		if (generators.isEmpty()) {
			log.info("*** Sequence generator data inserted  ***");
			SequenceGenerator sequenceGenerator = new SequenceGenerator();
			sequenceGenerator.setApplicatiionLicense(1);
			sequenceGenerator.setApplicationCategory(1);
			sequenceGenerator.setApplicationContacts(1);
			sequenceGenerator.setApplicationDetails(1);
			sequenceGenerator.setApplicationProvider(1);
			sequenceGenerator.setApplicationRequestId(1);
			sequenceGenerator.setApplicationSubscription(1);
			sequenceGenerator.setCloudSequenceId(1);
			sequenceGenerator.setContractRequestSequenceId(1);
			sequenceGenerator.setDepartmentSequence(1);
			sequenceGenerator.setDeptRequestId(1);
			sequenceGenerator.setInvoiceSequenceId(1);
			sequenceGenerator.setPaymentSequenceId(1);
			sequenceGenerator.setProjectSequenceId(1);
			sequenceGenerator.setRequestId(1);
			sequenceGenerator.setTenantSequence(1);
			sequenceGenerator.setUserOnboarding(1);
			sequenceGenerator.setUserRequestId(1);
			sequenceGenerator.setEnquirySequenceId(1);
			sequenceGeneratorRepository.save(sequenceGenerator);
		}
	}

}
