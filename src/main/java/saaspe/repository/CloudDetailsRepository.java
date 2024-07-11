package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.CloudServiceDetails;

public interface CloudDetailsRepository extends JpaRepository<CloudServiceDetails, Integer> {

    @Query(value = "Select * from saspe_cloud_service_details where service_name =:serviceName ;", nativeQuery = true)
    CloudServiceDetails findByServiceName(String serviceName);

}
