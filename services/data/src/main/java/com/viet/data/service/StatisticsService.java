package com.viet.data.service;

import com.viet.data.dto.response.*;
import com.viet.data.module.ColumnMetadata;
import com.viet.data.module.ColumnStatistics;
import com.viet.data.module.Dataset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StatisticsService {

    public Map<String, ColumnStatistics> calculateBasicStatistics(CSVParseResult parseResult) {
        Map<String, ColumnStatistics> stats = new HashMap<>();

        for (ColumnMetadata column : parseResult.getColumns()) {
            ColumnStatistics columnStats = calculateColumnStatistics(column, parseResult);
            stats.put(column.getName(), columnStats);
        }

        return stats;
    }

    public Map<String, Object> calculateDescriptiveStats(Dataset dataset) {
        Map<String, Object> stats = new HashMap<>();

        // Dataset info
        stats.put("datasetInfo", Map.of(
                "totalRows", dataset.getRowCount(),
                "totalColumns", dataset.getColumnCount(),
                "fileSize", dataset.getFileSize(),
                "uploadDate", dataset.getCreatedAt()
        ));

        // Column statistics
        stats.put("columnStats", dataset.getBasicStats());

        // Data quality metrics
        stats.put("dataQuality", calculateDataQualityMetrics(dataset));

        // Type distribution
        Map<String, Long> typeDistribution = dataset.getColumns().stream()
                .collect(Collectors.groupingBy(
                        col -> col.getDataType().name(),
                        Collectors.counting()
                ));
        stats.put("typeDistribution", typeDistribution);

        return stats;
    }

    public Map<String, Map<String, Double>> calculateCorrelations(Dataset dataset) {
        Map<String, Map<String, Double>> correlations = new HashMap<>();

        // Get numeric columns only
        List<ColumnMetadata> numericColumns = dataset.getColumns().stream()
                .filter(col -> col.getIsNumeric() != null && col.getIsNumeric())
                .collect(Collectors.toList());

        // Calculate correlation matrix
        for (ColumnMetadata col1 : numericColumns) {
            Map<String, Double> rowCorrelations = new HashMap<>();
            for (ColumnMetadata col2 : numericColumns) {
                if (!col1.getName().equals(col2.getName())) {
                    // Simulate correlation calculation
                    double correlation = calculateSimulatedCorrelation(col1, col2);
                    rowCorrelations.put(col2.getName(), correlation);
                } else {
                    rowCorrelations.put(col2.getName(), 1.0);
                }
            }
            correlations.put(col1.getName(), rowCorrelations);
        }

        return correlations;
    }

    public List<DataPattern> detectPatterns(Dataset dataset) {
        List<DataPattern> patterns = new ArrayList<>();

        // Simulate pattern detection
        if (hasTimeSeriesData(dataset)) {
            DataPattern trendPattern = new DataPattern();
            trendPattern.setType("TREND");
            trendPattern.setDescription("Possible linear trend detected in time series data");
            trendPattern.setConfidence(0.85);
            trendPattern.setDetails(Map.of("direction", "increasing", "strength", "moderate"));
            patterns.add(trendPattern);
        }

        if (hasCategoricalData(dataset)) {
            DataPattern distributionPattern = new DataPattern();
            distributionPattern.setType("DISTRIBUTION");
            distributionPattern.setDescription("Skewed distribution detected in categorical data");
            distributionPattern.setConfidence(0.72);
            distributionPattern.setDetails(Map.of("skewness", "right", "impact", "moderate"));
            patterns.add(distributionPattern);
        }

        return patterns;
    }

    public List<DataAnomaly> detectOutliers(Dataset dataset) {
        List<DataAnomaly> anomalies = new ArrayList<>();

        // Simulate outlier detection
        List<ColumnMetadata> numericColumns = dataset.getColumns().stream()
                .filter(col -> col.getIsNumeric() != null && col.getIsNumeric())
                .collect(Collectors.toList());

        for (ColumnMetadata column : numericColumns) {
            // Simulate finding some outliers
            if (column.getNullCount() > 0) {
                DataAnomaly anomaly = new DataAnomaly();
                anomaly.setColumnName(column.getName());
                anomaly.setRowIndex(42); // Example row
                anomaly.setValue("EXTREME_VALUE");
                anomaly.setAnomalyScore(0.95);
                anomaly.setReason("Value significantly deviates from mean");
                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    public List<DataPattern> analyzeTrends(Dataset dataset) {
        List<DataPattern> trends = new ArrayList<>();

        // Simulate trend analysis
        if (hasTimeSeriesData(dataset)) {
            DataPattern seasonalPattern = new DataPattern();
            seasonalPattern.setType("SEASONALITY");
            seasonalPattern.setDescription("Weekly seasonality pattern detected");
            seasonalPattern.setConfidence(0.78);
            seasonalPattern.setDetails(Map.of("period", "7 days", "amplitude", "moderate"));
            trends.add(seasonalPattern);
        }

        return trends;
    }

    public List<VisualizationSuggestion> generateVisualizationSuggestions(Dataset dataset, AnalysisResult result) {
        List<VisualizationSuggestion> suggestions = new ArrayList<>();

        // Auto-generate visualization suggestions based on data characteristics
        if (hasNumericData(dataset)) {
            VisualizationSuggestion histSuggestion = new VisualizationSuggestion();
            histSuggestion.setChartType("histogram");
            histSuggestion.setTitle("Distribution Analysis");
            histSuggestion.setDescription("View distribution of numeric variables");
            histSuggestion.setConfiguration(Map.of("bins", 10, "normalize", true));
            suggestions.add(histSuggestion);

            VisualizationSuggestion scatterSuggestion = new VisualizationSuggestion();
            scatterSuggestion.setChartType("scatter_plot");
            scatterSuggestion.setTitle("Relationship Analysis");
            scatterSuggestion.setDescription("Explore relationships between variables");
            scatterSuggestion.setConfiguration(Map.of("trendline", true, "correlation", true));
            suggestions.add(scatterSuggestion);
        }

        if (hasCategoricalData(dataset)) {
            VisualizationSuggestion barSuggestion = new VisualizationSuggestion();
            barSuggestion.setChartType("bar_chart");
            barSuggestion.setTitle("Category Comparison");
            barSuggestion.setDescription("Compare frequencies across categories");
            barSuggestion.setConfiguration(Map.of("stacked", false, "horizontal", false));
            suggestions.add(barSuggestion);
        }

        if (hasTimeSeriesData(dataset)) {
            VisualizationSuggestion lineSuggestion = new VisualizationSuggestion();
            lineSuggestion.setChartType("line_chart");
            lineSuggestion.setTitle("Trend Analysis");
            lineSuggestion.setDescription("Analyze trends over time");
            lineSuggestion.setConfiguration(Map.of("smoothing", true, "markers", true));
            suggestions.add(lineSuggestion);
        }

        return suggestions;
    }

    public List<String> generateCorrelationInsights(Map<String, Map<String, Double>> correlations) {
        List<String> insights = new ArrayList<>();

        // Generate human-readable insights from correlation matrix
        correlations.forEach((col1, row) -> {
            row.forEach((col2, corr) -> {
                if (col1.compareTo(col2) < 0) { // Avoid duplicates
                    if (Math.abs(corr) > 0.8) {
                        String strength = corr > 0 ? "strong positive" : "strong negative";
                        insights.add(String.format("Very %s correlation between %s and %s (%.3f)",
                                strength, col1, col2, corr));
                    } else if (Math.abs(corr) > 0.5) {
                        String strength = corr > 0 ? "moderate positive" : "moderate negative";
                        insights.add(String.format("%s correlation between %s and %s (%.3f)",
                                strength, col1, col2, corr));
                    }
                }
            });
        });

        if (insights.isEmpty()) {
            insights.add("No strong correlations detected between numeric variables");
        }

        return insights;
    }

    private ColumnStatistics calculateColumnStatistics(ColumnMetadata column, CSVParseResult parseResult) {
        ColumnStatistics stats = new ColumnStatistics();
        stats.setColumnName(column.getName());
        stats.setDataType(column.getDataType());
        stats.setTotalCount((long) parseResult.getRowCount());
        stats.setNullCount(column.getNullCount());
        stats.setUniqueCount(column.getUniqueCount());
        stats.setNullPercentage((double) column.getNullCount() / parseResult.getRowCount() * 100);

        // Simulate numeric statistics
        if (column.getIsNumeric() != null && column.getIsNumeric()) {
            stats.setMin(0.0);
            stats.setMax(100.0);
            stats.setMean(50.0);
            stats.setMedian(49.5);
            stats.setStdDev(15.0);
            stats.setVariance(225.0);
            stats.setSkewness(0.1);
            stats.setKurtosis(-0.2);
            stats.setQuartiles(Arrays.asList(25.0, 50.0, 75.0));
        }

        return stats;
    }

    private double calculateSimulatedCorrelation(ColumnMetadata col1, ColumnMetadata col2) {
        // Simulate correlation based on column names (for demo purposes)
        String name1 = col1.getName().toLowerCase();
        String name2 = col2.getName().toLowerCase();

        if (name1.contains("price") && name2.contains("quantity")) {
            return -0.65;
        } else if (name1.contains("age") && name2.contains("income")) {
            return 0.72;
        } else if (name1.contains("height") && name2.contains("weight")) {
            return 0.85;
        } else {
            return (Math.random() * 0.6) - 0.3; // Random correlation between -0.3 and 0.3
        }
    }

    private Map<String, Object> calculateDataQualityMetrics(Dataset dataset) {
        Map<String, Object> quality = new HashMap<>();

        long totalCells = (long) dataset.getRowCount() * dataset.getColumnCount();
        long nullCells = dataset.getColumns().stream()
                .mapToLong(ColumnMetadata::getNullCount)
                .sum();

        double completeness = 100.0 - ((double) nullCells / totalCells * 100);
        double uniqueness = dataset.getColumns().stream()
                .mapToDouble(col -> (double) col.getUniqueCount() / dataset.getRowCount())
                .average()
                .orElse(0.0) * 100;

        quality.put("completeness", Math.round(completeness * 100.0) / 100.0);
        quality.put("uniqueness", Math.round(uniqueness * 100.0) / 100.0);
        quality.put("nullCount", nullCells);
        quality.put("nullPercentage", Math.round(((double) nullCells / totalCells * 100) * 100.0) / 100.0);
        quality.put("qualityScore", Math.round((completeness + uniqueness) / 2 * 100.0) / 100.0);

        return quality;
    }

    private boolean hasNumericData(Dataset dataset) {
        return dataset.getColumns().stream()
                .anyMatch(col -> col.getIsNumeric() != null && col.getIsNumeric());
    }

    private boolean hasCategoricalData(Dataset dataset) {
        return dataset.getColumns().stream()
                .anyMatch(col -> col.getIsCategorical() != null && col.getIsCategorical());
    }

    private boolean hasTimeSeriesData(Dataset dataset) {
        return dataset.getColumns().stream()
                .anyMatch(col -> col.getDataType() == ColumnMetadata.DataType.DATE ||
                        col.getDataType() == ColumnMetadata.DataType.DATETIME);
    }
}
