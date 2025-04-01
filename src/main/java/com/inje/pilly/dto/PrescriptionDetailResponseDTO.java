package com.inje.pilly.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionDetailResponseDTO {
    private String prescriptionName;
    private LocalDate startDate;
    private String file;
    private List<MedicineDTO> medicines;
}
