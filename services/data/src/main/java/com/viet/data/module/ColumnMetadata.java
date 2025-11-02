package com.viet.data.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMetadata {
    private String name;
    private DataType dataType;
    private Long uniqueCount;
    private Long nullCount;
    private List<String> sampleValues;
    private Boolean isNumeric;
    private Boolean isCategorical;

    public enum DataType {
        STRING, INTEGER, DOUBLE, BOOLEAN, DATE, DATETIME, UNKNOWN
    }
}