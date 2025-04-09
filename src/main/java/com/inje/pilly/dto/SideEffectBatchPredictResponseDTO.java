package com.inje.pilly.dto;

import lombok.*;

import java.util.List;
import java.util.Map;


@NoArgsConstructor
@AllArgsConstructor
public class SideEffectBatchPredictResponseDTO {

    private List<SideEffectPredictResponseDTO> result;

    public List<SideEffectPredictResponseDTO> getResult() {
        return result;
    }

    public void setResult(List<SideEffectPredictResponseDTO> result) {
        this.result = result;
    }
}
