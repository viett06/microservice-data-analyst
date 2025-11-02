package com.viet.data.dto.response;

import com.viet.data.module.ColumnMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CSVParseResult {
    private Integer rowCount;
    private Integer columnCount;
    private List<ColumnMetadata> columns;
    private List<Map<String, Object>> sampleData;
    private List<String> headers;
    private Map<String, Object> basicStats;
}
