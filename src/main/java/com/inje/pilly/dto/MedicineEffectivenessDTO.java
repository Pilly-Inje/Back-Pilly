package com.inje.pilly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineEffectivenessDTO {
    private Long userId;
    private Long medicineId;
    //private Long prescriptionId;
    private LocalDate recordDate;
    private int effectLevel;
    private boolean sideEffectOccurred;
    private List<String> sideEffects;  // ["두통", "복통", "두드러기", "구토" ,"가려움증" ]
    private String comments;
}
