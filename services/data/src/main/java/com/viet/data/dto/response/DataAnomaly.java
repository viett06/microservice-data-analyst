package com.viet.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataAnomaly {
    private String columnName;
    private Integer rowIndex;
    private Object value;
    private Double anomalyScore;
    private String reason;
}
