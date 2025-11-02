package com.viet.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisualizationSuggestion {
    private String chartType;
    private String title;
    private String description;
    private Map<String, Object> configuration;
}
