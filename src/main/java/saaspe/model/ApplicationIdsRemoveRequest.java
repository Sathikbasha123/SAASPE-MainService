package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class ApplicationIdsRemoveRequest {

    private List<String> applicationIds;
}
