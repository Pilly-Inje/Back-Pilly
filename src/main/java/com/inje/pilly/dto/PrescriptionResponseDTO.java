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
    private List<String> sideEffectFeedback;
}
