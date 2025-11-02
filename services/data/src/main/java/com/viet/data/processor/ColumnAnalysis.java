package com.viet.data.processor;

import com.viet.data.module.ColumnMetadata;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ColumnAnalysis {
    private ColumnMetadata.DataType dataType;
    private Long uniqueCount;
    private Long nullCount;
    private List<String> sampleValues;
    private boolean isNumeric;
    private boolean isCategorical;
}
