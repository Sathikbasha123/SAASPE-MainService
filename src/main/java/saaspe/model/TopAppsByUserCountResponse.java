package saaspe.model;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopAppsByUserCountResponse implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<TopAppByUsercountResponse> topAppByUserEmail;

}
