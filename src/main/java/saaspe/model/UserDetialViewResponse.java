package saaspe.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetialViewResponse {

    private List<String> users;

    private Integer userCount;

    private List<String> logoUrl;

    private List<String> status;

    private List<String> designation;

    private List<String> type;

    private List<String> department;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private List<Date> onboardingDate;

}
