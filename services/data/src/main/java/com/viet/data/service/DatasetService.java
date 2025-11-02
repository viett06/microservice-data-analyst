package com.viet.data.service;

import com.viet.data.dto.dtos.DatasetDTO;
import com.viet.data.exception.DatasetNotFoundException;
import com.viet.data.exception.UnauthorizedAccessException;
import com.viet.data.module.Dataset;
import com.viet.data.repository.DatasetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetService {

    private final DatasetRepository datasetRepository;

    public List<DatasetDTO> getUserDatasets(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Dataset> datasets = datasetRepository.findByUserId(userId, pageable);

        return datasets.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DatasetDTO getDataset(String datasetId, String userId) {
        Dataset dataset = datasetRepository.findByIdAndUserId(datasetId, userId)
                .orElseThrow(() -> new DatasetNotFoundException("Dataset not found: " + datasetId));

        return mapToDTO(dataset);
    }

    public void deleteDataset(String datasetId, String userId) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new DatasetNotFoundException("Dataset not found: " + datasetId));

        if (!dataset.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("User not authorized to delete this dataset");
        }

        // Delete file from storage
        // fileStorageService.deleteFile(dataset.getStoragePath());

        // Delete from database
        datasetRepository.delete(dataset);

        log.info("Dataset deleted: {} by user: {}", datasetId, userId);
    }

    public long getUserDatasetCount(String userId) {
        return datasetRepository.countByUserId(userId);
    }

    public List<DatasetDTO> getRecentDatasets(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        Page<Dataset> datasets = datasetRepository.findByUserId(userId, pageable);

        return datasets.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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