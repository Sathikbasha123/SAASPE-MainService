package saaspe.model;

import lombok.Data;

@Data
public class ProductReviewsResponse {

    private String vendorId;

    private String vendorName;

    private String name;

    private String designation;

    private String companyDetails;

    private long rating;

    private String ratedOn;

    private String review;

    private String UUID;

}
