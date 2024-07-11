package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ApplicationCategoryMaster;

public interface ApplicationCategoryMasterRepository extends JpaRepository<ApplicationCategoryMaster, String> {

	ApplicationCategoryMaster findByCategoryId(String categoryId);

	ApplicationCategoryMaster findByCategoryName(String stringCellValue);

	@Query("select categoryName from ApplicationCategoryMaster")
	List<String> findCategoryName();
}
