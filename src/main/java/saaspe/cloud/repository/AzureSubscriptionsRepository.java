package saaspe.cloud.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.document.AzureSubscriptions;

public interface AzureSubscriptionsRepository extends MongoRepository<AzureSubscriptions, Integer> {

}
