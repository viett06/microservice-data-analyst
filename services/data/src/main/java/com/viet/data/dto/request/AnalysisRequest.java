package com.viet.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    private String datasetId;
    private AnalysisType analysisType;
    private Map<String, Object> parameters;
    private List<String> selectedColumns;
    private String userId;

    public enum AnalysisType {
        DESCRIPTIVE_STATS,
        CORRELATION_ANALYSIS,
        TREND_ANALYSIS,
        PATTERN_DETECTION,
        OUTLIER_DETECTION,
        PREDICTIVE_ANALYSIS,
        CUSTOM_ANALYSIS
    }
}
