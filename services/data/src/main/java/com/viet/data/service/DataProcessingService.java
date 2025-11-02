package com.viet.data.service;

import com.viet.data.dto.dtos.DatasetDTO;
import com.viet.data.dto.request.AnalysisRequest;
import com.viet.data.dto.response.AnalysisResult;
import com.viet.data.dto.response.CSVParseResult;
import com.viet.data.exception.*;
import com.viet.data.module.Dataset;
import com.viet.data.processor.CSVProcessor;
import com.viet.data.repository.DatasetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataProcessingService {

    private final DatasetRepository datasetRepository;
    private final CSVProcessor csvProcessor;
    private final FileStorageService fileStorageService;
    private final StatisticsService statisticsService;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    public DatasetDTO processUpload(MultipartFile file, String userId, String userRole) {
        log.info("Processing file upload for user: {}, file: {}", userId, file.getOriginalFilename());

        try {
            // Validate file
            validateFile(file);

            // Process CSV
            CSVParseResult parseResult = csvProcessor.processCSV(file);

            // Store file
            String storagePath = fileStorageService.storeFile(file, userId);

            // Create dataset entity
            Dataset dataset = createDatasetEntity(file, userId, userRole, parseResult, storagePath);

            // Calculate basic statistics
            dataset.setBasicStats(statisticsService.calculateBasicStatistics(parseResult));

            // Save to database
            Dataset savedDataset = datasetRepository.save(dataset);

            log.info("Dataset processed successfully: {} for user {}", savedDataset.getId(), userId);

            return mapToDTO(savedDataset);

        } catch (Exception e) {
            log.error("Error processing dataset for user {}: {}", userId, e.getMessage());
            throw new DataProcessingException("Failed to process dataset: " + e.getMessage());
        }
    }

    public AnalysisResult analyzeDataset(AnalysisRequest request) {
        log.info("Starting analysis for dataset: {}, type: {}", request.getDatasetId(), request.getAnalysisType());

        try {
            // Get dataset
            Dataset dataset = datasetRepository.findById(request.getDatasetId())
                    .orElseThrow(() -> new DatasetNotFoundException("Dataset not found: " + request.getDatasetId()));

            // Verify user ownership
            if (!dataset.getUserId().equals(request.getUserId())) {
                throw new UnauthorizedAccessException("User not authorized to access this dataset");
            }

            long startTime = System.currentTimeMillis();

            // Perform analysis based on type
            AnalysisResult result = performAnalysis(dataset, request);

            result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            result.setAnalyzedAt(LocalDateTime.now());
            result.setStatus("COMPLETED");

            log.info("Analysis completed for dataset: {} in {} ms",
                    request.getDatasetId(), result.getProcessingTimeMs());

            return result;

        } catch (Exception e) {
            log.error("Error analyzing dataset {}: {}", request.getDatasetId(), e.getMessage());

            AnalysisResult errorResult = new AnalysisResult();
            errorResult.setDatasetId(request.getDatasetId());
            errorResult.setAnalysisType(request.getAnalysisType());
            errorResult.setStatus("FAILED");
            errorResult.setErrorMessage(e.getMessage());
            errorResult.setAnalyzedAt(LocalDateTime.now());

            return errorResult;
        }
    }

    private Dataset createDatasetEntity(MultipartFile file, String userId, String userRole,
                                        CSVParseResult parseResult, String storagePath) {
        Dataset dataset = new Dataset();
        dataset.setId(UUID.randomUUID().toString());
        dataset.setUserId(userId);
        dataset.setUserRole(userRole);
        dataset.setOriginalFileName(file.getOriginalFilename());
        dataset.setFileSize(file.getSize());
        dataset.setRowCount(parseResult.getRowCount());
        dataset.setColumnCount(parseResult.getColumnCount());
        dataset.setColumns(parseResult.getColumns());
        dataset.setStoragePath(storagePath);
        dataset.setStatus(Dataset.DatasetStatus.PROCESSED);
        dataset.setCreatedAt(LocalDateTime.now());
        dataset.setUpdatedAt(LocalDateTime.now());
        dataset.setSampleData(parseResult.getSampleData());

        return dataset;
    }

    private AnalysisResult performAnalysis(Dataset dataset, AnalysisRequest request) {
        AnalysisResult result = new AnalysisResult();
        result.setId(UUID.randomUUID().toString());
        result.setDatasetId(dataset.getId());
        result.setUserId(dataset.getUserId());
        result.setAnalysisType(request.getAnalysisType());
        result.setParameters(request.getParameters());

        switch (request.getAnalysisType()) {
            case DESCRIPTIVE_STATS:
                result.setSummary(statisticsService.calculateDescriptiveStats(dataset));
                break;

            case CORRELATION_ANALYSIS:
                result.setCorrelations(statisticsService.calculateCorrelations(dataset));
                break;

            case TREND_ANALYSIS:
                result.setPatterns(statisticsService.analyzeTrends(dataset));
                break;

            case OUTLIER_DETECTION:
                result.setAnomalies(statisticsService.detectOutliers(dataset));
                break;

            case PATTERN_DETECTION:
                result.setPatterns(statisticsService.detectPatterns(dataset));
                break;

            default:
                throw new UnsupportedAnalysisTypeException(
                        "Unsupported analysis type: " + request.getAnalysisType());
        }

        // Generate visualization suggestions
        result.setVisualizationSuggestions(
                statisticsService.generateVisualizationSuggestions(dataset, result));

        // Prepare frontend data
        result.setFrontendData(prepareFrontendData(result));

        return result;
    }

    private Map<String, Object> prepareFrontendData(AnalysisResult result) {
        Map<String, Object> frontendData = new HashMap<>();

        // Transform data for frontend consumption
        frontendData.put("analysisType", result.getAnalysisType().name());
        frontendData.put("summary", result.getSummary());
        frontendData.put("charts", generateChartConfigs(result));
        frontendData.put("insights", extractInsights(result));
        frontendData.put("timestamp", LocalDateTime.now().toString());

        return frontendData;
    }

    private List<Map<String, Object>> generateChartConfigs(AnalysisResult result) {
        List<Map<String, Object>> charts = new ArrayList<>();

        // Correlation matrix chart
        if (result.getCorrelations() != null && !result.getCorrelations().isEmpty()) {
            Map<String, Object> correlationChart = new HashMap<>();
            correlationChart.put("type", "heatmap");
            correlationChart.put("title", "Correlation Matrix");
            correlationChart.put("data", result.getCorrelations());
            correlationChart.put("description", "Shows relationships between numeric variables");
            charts.add(correlationChart);
        }

        // Descriptive statistics chart
        if (result.getSummary() != null && result.getSummary().containsKey("columnStats")) {
            Map<String, Object> statsChart = new HashMap<>();
            statsChart.put("type", "bar");
            statsChart.put("title", "Descriptive Statistics");
            statsChart.put("data", result.getSummary().get("columnStats"));
            statsChart.put("description", "Basic statistical measures for each column");
            charts.add(statsChart);
        }

        return charts;
    }

    private List<String> extractInsights(AnalysisResult result) {
        List<String> insights = new ArrayList<>();

        // Generate automatic insights based on analysis results
        if (result.getCorrelations() != null) {
            insights.addAll(statisticsService.generateCorrelationInsights(result.getCorrelations()));
        }

        if (result.getAnomalies() != null && !result.getAnomalies().isEmpty()) {
            insights.add("Found " + result.getAnomalies().size() + " potential outliers in the data");
        }

        if (result.getPatterns() != null && !result.getPatterns().isEmpty()) {
            insights.add("Detected " + result.getPatterns().size() + " significant patterns in the data");
        }

        // Add data quality insights
        if (result.getSummary() != null && result.getSummary().containsKey("dataQuality")) {
            Map<String, Object> dataQuality = (Map<String, Object>) result.getSummary().get("dataQuality");
            double completeness = (Double) dataQuality.get("completeness");
            if (completeness < 90) {
                insights.add("Data completeness is " + completeness + "%, consider data cleaning");
            }
        }

        return insights;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new FileValidationException("Only CSV files are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileValidationException("File size exceeds 50MB limit");
        }
    }

    private DatasetDTO mapToDTO(Dataset dataset) {
        return DatasetDTO.builder()
                .id(dataset.getId())
                .originalFileName(dataset.getOriginalFileName())
                .fileSize(dataset.getFileSize())
                .rowCount(dataset.getRowCount())
                .columnCount(dataset.getColumnCount())
                .columns(dataset.getColumns())
                .sampleData(dataset.getSampleData())
                .basicStats(dataset.getBasicStats())
                .createdAt(dataset.getCreatedAt())
                .updatedAt(dataset.getUpdatedAt())
                .status(dataset.getStatus().name())
                .build();
    }
}
