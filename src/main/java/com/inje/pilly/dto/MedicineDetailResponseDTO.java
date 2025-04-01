package com.inje.pilly.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDetailResponseDTO {
    private Long medicineId;
    private String medicineName;
    private String effect;
    private String dosage; // 복용방법
    private String caution; // 주의사항
    private String medicineImageUrl;
}
