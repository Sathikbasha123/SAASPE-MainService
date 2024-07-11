package saaspe.currency.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.currency.entity.CurrencyEntity;

public interface CurrencyRepository extends JpaRepository<CurrencyEntity, Integer> {

	@Query("Select a from CurrencyEntity a where a.date=:from and a.base =:base ")
	CurrencyEntity findByDateAndCurrecncy(Date from, String base);

}
