package com.viet.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataPattern {
    private String type;
    private String description;
    private Double confidence;
    private Map<String, Object> details;
}
