package com.viet.data.exception;

import com.viet.data.dto.dtos.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileValidationException(FileValidationException ex) {
        log.warn("File validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("FILE_VALIDATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(DataProcessingException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataProcessingException(DataProcessingException ex) {
        log.error("Data processing error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("PROCESSING_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(DatasetNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleDatasetNotFoundException(DatasetNotFoundException ex) {
        log.warn("Dataset not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("DATASET_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("UNAUTHORIZED_ACCESS", ex.getMessage()));
    }

    @ExceptionHandler(CSVProcessingException.class)
    public ResponseEntity<ApiResponse<Object>> handleCSVProcessingException(CSVProcessingException ex) {
        log.error("CSV processing error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("CSV_PROCESSING_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(UnsupportedAnalysisTypeException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnsupportedAnalysisTypeException(UnsupportedAnalysisTypeException ex) {
        log.warn("Unsupported analysis type: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("UNSUPPORTED_ANALYSIS", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("File size exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("FILE_SIZE_EXCEEDED", "File size exceeds maximum limit"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
