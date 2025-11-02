package com.viet.data.processor;

import com.viet.data.dto.response.CSVParseResult;
import com.viet.data.exception.CSVProcessingException;
import com.viet.data.module.ColumnMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CSVProcessor {

    private static final int SAMPLE_SIZE = 1000;
    private static final int PREVIEW_ROWS = 10;
    private static final int MAX_SAMPLE_VALUES = 5;

    public CSVParseResult processCSV(MultipartFile file) {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .withIgnoreSurroundingSpaces())) {

            List<CSVRecord> records = parser.getRecords();
            Map<String, Integer> headerMap = parser.getHeaderMap();

            log.info("Processing CSV with {} columns and {} rows", headerMap.size(), records.size());

            // Analyze columns
            List<ColumnMetadata> columns = analyzeColumns(records, new ArrayList<>(headerMap.keySet()));

            // Get sample data for preview
            List<Map<String, Object>> sampleData = extractSampleData(records, headerMap, PREVIEW_ROWS);

            // Calculate basic statistics
            Map<String, Object> basicStats = calculateBasicStats(records, columns, headerMap);

            return CSVParseResult.builder()
                    .rowCount(records.size())
                    .columnCount(headerMap.size())
                    .columns(columns)
                    .sampleData(sampleData)
                    .headers(new ArrayList<>(headerMap.keySet()))
                    .basicStats(basicStats)
                    .build();

        } catch (Exception e) {
            log.error("Error processing CSV file: {}", e.getMessage());
            throw new CSVProcessingException("Failed to process CSV file: " + e.getMessage());
        }
    }

    private List<ColumnMetadata> analyzeColumns(List<CSVRecord> records, List<String> headers) {
        return headers.stream()
                .map(header -> analyzeSingleColumn(records, header))
                .collect(Collectors.toList());
    }

    private ColumnMetadata analyzeSingleColumn(List<CSVRecord> records, String columnName) {
        Set<String> uniqueValues = new HashSet<>();
        long nullCount = 0;
        List<String> sampleValues = new ArrayList<>();
        boolean isNumeric = true;
        boolean isCategorical = false;

        // Analyze first SAMPLE_SIZE records for performance
        int sampleSize = Math.min(records.size(), SAMPLE_SIZE);

        for (int i = 0; i < sampleSize; i++) {
            String value = records.get(i).get(columnName);

            if (value == null || value.trim().isEmpty()) {
                nullCount++;
            } else {
                uniqueValues.add(value);

                // Collect sample values
                if (sampleValues.size() < MAX_SAMPLE_VALUES) {
                    sampleValues.add(value);
                }

                // Check if numeric
                if (isNumeric && !isNumeric(value)) {
                    isNumeric = false;
                }
            }
        }

        // Determine if categorical (limited unique values)
        if (uniqueValues.size() <= 50 && uniqueValues.size() > 0) {
            isCategorical = true;
        }

        ColumnMetadata.DataType dataType = inferDataType(sampleValues, isNumeric, isCategorical);

        return new ColumnMetadata(
                columnName,
                dataType,
                (long) uniqueValues.size(),
                nullCount,
                sampleValues,
                isNumeric,
                isCategorical
        );
    }

    private boolean isNumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private ColumnMetadata.DataType inferDataType(List<String> sampleValues, boolean isNumeric, boolean isCategorical) {
        if (isNumeric) {
            // Check if it's integer or double
            for (String value : sampleValues) {
                try {
                    double d = Double.parseDouble(value);
                    if (d != Math.floor(d)) {
                        return ColumnMetadata.DataType.DOUBLE;
                    }
                } catch (NumberFormatException e) {
                    // Should not happen since we checked isNumeric
                }
            }
            return ColumnMetadata.DataType.INTEGER;
        }

        // Check for boolean
        if (sampleValues.stream().allMatch(v ->
                "true".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v) ||
                        "yes".equalsIgnoreCase(v) || "no".equalsIgnoreCase(v) ||
                        "1".equals(v) || "0".equals(v))) {
            return ColumnMetadata.DataType.BOOLEAN;
        }

        // Check for date/datetime (simple check)
        if (sampleValues.stream().anyMatch(this::looksLikeDate)) {
            return ColumnMetadata.DataType.DATETIME;
        }

        return ColumnMetadata.DataType.STRING;
    }

    private boolean looksLikeDate(String value) {
        // Simple date pattern matching
        return value.matches("\\d{4}-\\d{2}-\\d{2}") ||
                value.matches("\\d{2}/\\d{2}/\\d{4}") ||
                value.matches("\\d{4}-\\d{2}-\\d{2}.*\\d{2}:\\d{2}:\\d{2}");
    }

    private List<Map<String, Object>> extractSampleData(List<CSVRecord> records,
                                                        Map<String, Integer> headerMap,
                                                        int maxRows) {
        List<Map<String, Object>> sampleData = new ArrayList<>();
        List<String> headers = new ArrayList<>(headerMap.keySet());

        int rowCount = Math.min(records.size(), maxRows);
        for (int i = 0; i < rowCount; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            CSVRecord record = records.get(i);

            for (String header : headers) {
                String value = record.get(header);
                row.put(header, convertValue(value));
            }

            sampleData.add(row);
        }

        return sampleData;
    }

    private Object convertValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // Try numeric conversion
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            // Not a number, return as string
            return value;
        }
    }

    private Map<String, Object> calculateBasicStats(List<CSVRecord> records,
                                                    List<ColumnMetadata> columns,
                                                    Map<String, Integer> headerMap) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRows", records.size());
        stats.put("totalColumns", columns.size());
        stats.put("processingTime", System.currentTimeMillis());

        // Column type distribution
        Map<String, Long> typeDistribution = columns.stream()
                .collect(Collectors.groupingBy(
                        col -> col.getDataType().name(),
                        Collectors.counting()
                ));
        stats.put("typeDistribution", typeDistribution);

        // Data quality metrics
        long totalCells = (long) records.size() * columns.size();
        long nullCells = columns.stream()
                .mapToLong(ColumnMetadata::getNullCount)
                .sum();
        double completeness = 100.0 - ((double) nullCells / totalCells * 100);

        stats.put("dataQuality", Map.of(
                "completeness", Math.round(completeness * 100.0) / 100.0,
                "nullCount", nullCells,
                "nullPercentage", Math.round(((double) nullCells / totalCells * 100) * 100.0) / 100.0
        ));

        return stats;
    }
}
