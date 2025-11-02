package com.viet.data.controller;

import com.viet.data.config.SecurityUtils;
import com.viet.data.dto.dtos.ApiResponse;
import com.viet.data.dto.dtos.DatasetDTO;
import com.viet.data.dto.request.AnalysisRequest;
import com.viet.data.dto.response.AnalysisResult;
import com.viet.data.service.DataProcessingService;
import com.viet.data.service.DatasetService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final SecurityUtils securityUtils;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DatasetDTO>> uploadDataset(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) { // ✅ Thay thế @RequestHeader bằng HttpServletRequest

        String userId = securityUtils.getCurrentUserId(request);
        String userRole = securityUtils.getCurrentUserRole(request);

        log.info("File upload request from user: {}, role: {}, file: {}",
                userId, userRole, file.getOriginalFilename());

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) { // ✅ Thêm HttpServletRequest

        String userId = securityUtils.getCurrentUserId(request);

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
            HttpServletRequest request) { // ✅ Thêm HttpServletRequest

        String userId = securityUtils.getCurrentUserId(request);

        try {
            // Validate user access to this dataset
            securityUtils.validateUserAccess(request, userId);

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
            HttpServletRequest httpRequest) { // ✅ Thêm HttpServletRequest

        String userId = securityUtils.getCurrentUserId(httpRequest);

        log.info("Analysis request from user: {} for dataset: {}, type: {}",
                userId, request.getDatasetId(), request.getAnalysisType());

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
            HttpServletRequest request) { // ✅ Thêm HttpServletRequest

        String userId = securityUtils.getCurrentUserId(request);

        try {
            // Validate user access before deletion
            securityUtils.validateUserAccess(request, userId);

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
    public ResponseEntity<ApiResponse<Object>> getServiceStats(HttpServletRequest request) {
        String userId = securityUtils.getCurrentUserId(request);

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

    // Thêm endpoint để kiểm tra headers từ Gateway
    @GetMapping("/debug/headers")
    public ResponseEntity<Map<String, String>> debugHeaders(HttpServletRequest request) {
        return ResponseEntity.ok(Map.of(
                "X-User-Id", request.getHeader("X-User-Id"),
                "X-User-Role", request.getHeader("X-User-Role"),
                "X-User-Email", request.getHeader("X-User-Email"),
                "Authorization", request.getHeader("Authorization")
        ));
    }
}