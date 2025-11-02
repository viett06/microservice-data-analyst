package com.viet.data.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnStatistics {
    private String columnName;
    private ColumnMetadata.DataType dataType;
    private Long totalCount;
    private Long nullCount;
    private Long uniqueCount;
    private Double nullPercentage;
    private Double min;
    private Double max;
    private Double mean;
    private Double median;
    private Double stdDev;
    private Double variance;
    private Map<String, Long> valueCounts;
    private Map<String, Double> valuePercentages;
    private Double skewness;
    private Double kurtosis;
    private List<Double> quartiles;
}
