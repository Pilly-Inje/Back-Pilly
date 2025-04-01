package com.inje.pilly.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MedicineEffectivenessResponseDTO {
    private LocalDate recordDate;
    private int effectLevel;
    private boolean sideEffectOccurred;
    private List<String> sideEffects;
    private String comments;
}
