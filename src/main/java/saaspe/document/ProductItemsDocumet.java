package saaspe.document;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.model.Rating;
import saaspe.model.Ratting;

@Data
@Document(collection = "product-items")
public class ProductItemsDocumet {

	@Id
	private String _id;

	private String UUID;

	private String logo;

	private String title;

	private String description;

	private String category;

	private String subCategory;

	private String __v;

	private Rating rating;

	private Ratting ratting;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;
	
	private long amigoId;


}