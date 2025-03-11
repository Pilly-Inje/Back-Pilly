package com.inje.pilly.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PrescriptionUserResponseDTO {
    private Long prescriptionId;
    private String prescriptionName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private List<String> medicineNames;
}
