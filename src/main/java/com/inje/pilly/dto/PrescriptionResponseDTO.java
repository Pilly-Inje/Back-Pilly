package com.inje.pilly.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PrescriptionResponseDTO {
    private Long prescriptionId;
    private String message;

    public PrescriptionResponseDTO(long prescriptionId, String 처방전_저장_완료, List<String> feedbackMessages) {
    }
}
