package com.inje.pilly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SideEffectPredictResponseDTO {
    private Map<String, Double> probabilities;
    private String feedback;
}
