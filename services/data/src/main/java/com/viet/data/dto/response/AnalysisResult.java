package com.viet.data.dto.response;

import com.viet.data.dto.request.AnalysisRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private String id;
    private String datasetId;
    private String userId;
    private AnalysisRequest.AnalysisType analysisType;
    private Map<String, Object> parameters;
    private Map<String, Object> summary;
    private List<DataPattern> patterns;
    private List<DataAnomaly> anomalies;
    private Map<String, Map<String, Double>> correlations;
    private List<Prediction> predictions;
    private List<VisualizationSuggestion> visualizationSuggestions;
    private LocalDateTime analyzedAt;
    private Long processingTimeMs;
    private String status;
    private String errorMessage;
    private Map<String, Object> frontendData;
}
