package saaspe.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileForm implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String origin;
    private String gender;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date birthDay;
    private String passport;
    private String mobile;
    private String address;
    private String image;
    @JsonProperty("isPreviewEnabled")
    private boolean isPreviewEnabled;
    private List<String> readMessages = new ArrayList<>();

}
