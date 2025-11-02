package com.viet.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prediction {
    private String type;
    private Map<String, Object> input;
    private Map<String, Object> output;
    private Double confidence;
}
