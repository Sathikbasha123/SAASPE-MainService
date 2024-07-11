package saaspe.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class getTopAppsByMetricsResponse {

    private List<getTopAppByMetricsResponse> getTopAppResponse;

}
