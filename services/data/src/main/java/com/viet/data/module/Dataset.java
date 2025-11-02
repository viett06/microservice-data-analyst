package com.viet.data.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "datasets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {
    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("user_role")
    private String userRole;

    @Field("original_file_name")
    private String originalFileName;

    @Field("file_size")
    private Long fileSize;

    @Field("row_count")
    private Integer rowCount;

    @Field("column_count")
    private Integer columnCount;

    private List<ColumnMetadata> columns;

    @Field("storage_path")
    private String storagePath;

    private DatasetStatus status;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("sample_data")
    private List<Map<String, Object>> sampleData;

    @Field("basic_stats")
    private Map<String, ColumnStatistics> basicStats;

    public enum DatasetStatus {
        UPLOADING, PROCESSING, PROCESSED, FAILED, DELETED
    }
}