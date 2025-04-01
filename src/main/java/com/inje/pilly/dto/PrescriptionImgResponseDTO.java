package com.inje.pilly.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
public class PrescriptionImgResponseDTO {
    private Long userId;
    private Long prescriptionId;
    private String message;
    private List<String> extractedText;
//    public PrescriptionImgResponseDTO(Long userId, Long prescriptionId, String message, List<String> extractedText) {
//        this.userId = userId;
//        this.prescriptionId = prescriptionId;
//        this.message = message;
//        this.extractedText = extractedText;
//    }
}
