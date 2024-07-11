package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.dto.SubscriptionDao;
import saaspe.entity.UserSubscription;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer> {

    @Query(value = "SELECT * from saspe_user_subscription_details order by renewal_date DESC LIMIT 10 ;", nativeQuery = true)
    List<UserSubscription> findByAccountNameOrderedByDate();

    @Query(value = "SELECT SUM(amount_spent)AS count,subscription_name AS subscriptionname FROM saspe_user_subscription_details where subscription_name =:vendorName GROUP BY subscription_name order by count DESC ;", nativeQuery = true)
    List<SubscriptionDao> getTotalSpendBasedOnServiceName(String vendorName);

    @Query(value = "select a.subscriptionname, a.accountname,a.count from (select subscription_name as subscriptionname ,account_name as accountname, COUNT(account_name) as count FROM saspe_user_subscription_details GROUP BY subscription_name,account_name) a where a.subscriptionname =:vendorName ;", nativeQuery = true)
    List<SubscriptionDao> getCountBasedOnSubscriptionType(String vendorName);

    @Query(value = "SELECT DISTINCT subscription_name as subscriptionName FROM saspe_user_subscription_details a where a.subscription_name = 'Google' ;", nativeQuery = true)
    List<SubscriptionDao> getVendorDetails();

    @Query(value = "SELECT * from saspe_user_subscription_details order by renewal_date DESC ;", nativeQuery = true)
    List<UserSubscription> findByAccountOrderedByDate();

    @Query(value = "SELECT SUM(amount_spent)AS count,subscription_name AS subscriptionname,renewal_type AS renewaltype FROM saspe_user_subscription_details GROUP BY subscription_name,renewal_type having renewal_type =:renewalType AND subscription_name =:vendorName order by count DESC ;", nativeQuery = true)
    List<SubscriptionDao> getSpendBasedOnServiceName(String vendorName, String renewalType);
}
