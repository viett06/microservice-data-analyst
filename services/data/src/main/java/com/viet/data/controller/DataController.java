package com.viet.data.controller;

import com.viet.data.dto.dtos.ApiResponse;
import com.viet.data.dto.dtos.DatasetDTO;
import com.viet.data.dto.request.AnalysisRequest;
import com.viet.data.dto.response.AnalysisResult;
import com.viet.data.service.DataProcessingService;
import com.viet.data.service.DatasetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@Slf4j
public class DataController {

    private final DataProcessingService dataProcessingService;
    private final DatasetService datasetService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DatasetDTO>> uploadDataset(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole) {

        log.info("File upload request from user: {}, file: {}", userId, file.getOriginalFilename());

        try {
            DatasetDTO dataset = dataProcessingService.processUpload(file, userId, userRole);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Dataset uploaded successfully", dataset));

        } catch (Exception e) {
            log.error("File upload failed for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("UPLOAD_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/datasets")
    public ResponseEntity<ApiResponse<List<DatasetDTO>>> getUserDatasets(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            List<DatasetDTO> datasets = datasetService.getUserDatasets(userId, page, size);
            return ResponseEntity.ok(ApiResponse.success(datasets));

        } catch (Exception e) {
            log.error("Error fetching datasets for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("FETCH_ERROR", e.getMessage()));
        }
    }

    @GetMapping("/datasets/{datasetId}")
    public ResponseEntity<ApiResponse<DatasetDTO>> getDataset(
            @PathVariable String datasetId,
            @RequestHeader("X-User-Id") String userId) {

        try {
            DatasetDTO dataset = datasetService.getDataset(datasetId, userId);
            return ResponseEntity.ok(ApiResponse.success(dataset));

        } catch (Exception e) {
            log.error("Error fetching dataset {} for user {}: {}", datasetId, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("DATASET_NOT_FOUND", e.getMessage()));
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<AnalysisResult>> analyzeDataset(
            @Valid @RequestBody AnalysisRequest request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Analysis request for dataset: {}, type: {}", request.getDatasetId(), request.getAnalysisType());

        try {
            request.setUserId(userId);
            AnalysisResult result = dataProcessingService.analyzeDataset(request);

            if ("FAILED".equals(result.getStatus())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("ANALYSIS_FAILED", result.getErrorMessage()));
            }

            return ResponseEntity.ok(ApiResponse.success("Analysis completed", result));

        } catch (Exception e) {
            log.error("Analysis failed for dataset {}: {}", request.getDatasetId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("ANALYSIS_ERROR", e.getMessage()));
        }
    }

    @DeleteMapping("/datasets/{datasetId}")
    public ResponseEntity<ApiResponse<Void>> deleteDataset(
            @PathVariable String datasetId,
            @RequestHeader("X-User-Id") String userId) {

        try {
            datasetService.deleteDataset(datasetId, userId);
            return ResponseEntity.ok(ApiResponse.success("Dataset deleted successfully", null));

        } catch (Exception e) {
            log.error("Error deleting dataset {} for user {}: {}", datasetId, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("DELETE_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Data Processing Service is healthy"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getServiceStats(@RequestHeader("X-User-Id") String userId) {
        try {
            long datasetCount = datasetService.getUserDatasetCount(userId);
            List<DatasetDTO> recentDatasets = datasetService.getRecentDatasets(userId, 5);

            Object stats = Map.of(
                    "totalDatasets", datasetCount,
                    "recentDatasets", recentDatasets
            );

            return ResponseEntity.ok(ApiResponse.success(stats));

        } catch (Exception e) {
            log.error("Error getting stats for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("STATS_ERROR", e.getMessage()));
        }
    }
}