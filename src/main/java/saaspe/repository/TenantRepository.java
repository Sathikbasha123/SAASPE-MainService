package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import saaspe.entity.TenantDetails;

public interface TenantRepository extends JpaRepository<TenantDetails, String> {

    TenantDetails findByTenantId(String tenantId);

}
