package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class TagValues {

    private String key;

    private List<String> values;

    private List<String> matchOptions;

}
