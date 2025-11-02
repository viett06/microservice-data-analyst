package com.viet.data.dto.dtos;

import com.viet.data.module.ColumnMetadata;
import com.viet.data.module.ColumnStatistics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetDTO {
    private String id;
    private String originalFileName;
    private Long fileSize;
    private Integer rowCount;
    private Integer columnCount;
    private List<ColumnMetadata> columns;
    private List<Map<String, Object>> sampleData;
    private Map<String, ColumnStatistics> basicStats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
}
