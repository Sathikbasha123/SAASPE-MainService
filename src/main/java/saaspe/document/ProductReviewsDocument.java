package saaspe.document;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.model.ExtendedReview;

@Data
@Document(collection = "product-reviews")
public class ProductReviewsDocument {

	@Id
	private String _id;

	private String vendorId;

	private String vendorName;

	private String name;

	private String designation;

	private String companyDetails;

	private long rating;

	private String ratedOn;

	private String review;

	private ExtendedReview[] extendedReview;

	private long __v;

	private String UUID;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;
	
	private long amigoId;

}
