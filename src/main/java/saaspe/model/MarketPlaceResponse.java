package saaspe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class MarketPlaceResponse {

    private Rating rating;

    private String _id;

    private String id;

    private String logo;

    private String title;

    private String description;

    private String category;

    private String subCategory;

    private String __v;

    private String UUID;
}
