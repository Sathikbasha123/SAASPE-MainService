package saaspe.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import saaspe.entity.SequenceGenerator;
import saaspe.entity.TenantDetails;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.model.TenantDetailsResponse;
import saaspe.repository.SequenceGeneratorRepository;
import saaspe.repository.TenantRepository;
import saaspe.service.TenantService;

@Service
public class TenantServiceImpl implements TenantService {

	@Autowired
	TenantRepository repository;

	@Autowired
	SequenceGeneratorRepository generatorRepository;

	@Override
	public CommonResponse getTenantDetails() {
		List<TenantDetails> list = repository.findAll();
		return new CommonResponse(HttpStatus.OK, new Response("Tenant Details", list), "Details updated successfully");
	}

	@Override
	public CommonResponse addDepartment(TenantDetails detailsRequest) {
		TenantDetails details = new TenantDetails();
		String name = "TENANT_0";
		Integer sequence = generatorRepository.getTenantSequence();
		name = name.concat(sequence.toString());
		SequenceGenerator updateSequence = generatorRepository.getById(1);
		updateSequence.setTenantSequence(++sequence);
		generatorRepository.save(updateSequence);
		details.setCreatedBy(detailsRequest.getCreatedBy());
		details.setTenantContactAddress(detailsRequest.getTenantContactAddress());
		details.setTenantContactEmail(detailsRequest.getTenantContactEmail());
		details.setTenantContactMobileNO(detailsRequest.getTenantContactMobileNO());
		details.setTenantContactName(detailsRequest.getTenantContactName());
		details.setTenantId(name);
		details.setTenantName(detailsRequest.getTenantName());
		details.setCreatedOn(new Date());
		repository.save(details);
		return new CommonResponse(HttpStatus.OK, new Response("TenantDetails", name), "Details saved successfully");
	}

	@Override
	public CommonResponse getTenantDetailById(String id) {
		TenantDetailsResponse response = new TenantDetailsResponse();
		if (repository.existsById(id)) {
			TenantDetails details = repository.findByTenantId(id);
			response.setDetails(details);
			response.setMessage("Data Fetched Successfully");
		} else {
			response.setMessage("Provided Id No " + id + "Doesn't Exist");
		}
		return new CommonResponse(HttpStatus.OK, new Response("TenantDetails", response),
				"Details updated successfully");
	}

	@Override
	public void deleteTenantById(String id) {
		repository.deleteById(id);
	}

	@Override
	public void modifyDepartment(TenantDetails detailsRequest) {
		TenantDetails tenant = repository.findByTenantId(detailsRequest.getTenantId());
		tenant.setTenantContactName(detailsRequest.getTenantContactName());
		tenant.setTenantName(detailsRequest.getTenantName());
		tenant.setTenantContactName(detailsRequest.getTenantContactName());
		tenant.setTenantContactMobileNO(detailsRequest.getTenantContactMobileNO());
		tenant.setTenantContactEmail(detailsRequest.getTenantContactEmail());
		tenant.setTenantContactAddress(detailsRequest.getTenantContactAddress());
		tenant.setCreatedBy(detailsRequest.getCreatedBy());
		tenant.setCreatedOn(detailsRequest.getCreatedOn());
		tenant.setUpdatedBy(detailsRequest.getUpdatedBy());
		tenant.setUpdatedOn(new Date());
		repository.save(tenant);
	}

}
