package saaspe.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class MonthlySpendingResponse {
    private List<Map<String,Object>> monthlySpendingHistory;	
}

