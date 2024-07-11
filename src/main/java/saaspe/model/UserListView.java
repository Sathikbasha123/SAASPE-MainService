package saaspe.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saaspe.entity.ApplicationDetails;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListView {

    private String userAvatar;

    private String userName;

    private String userId;

    private String userEmail;

    private String userDesignation;

    private List<ApplicationDetailsRes> userApplications;

    @JsonIgnore
    private Set<ApplicationDetails> applicationId = new HashSet<>();

}
