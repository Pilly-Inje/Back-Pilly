package com.inje.pilly.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlarmDTO {
    private String alarmTime;
    private int medicineCount;
    private List<Long> prescriptionIds;
    private List<Long> allMedicineIds;
}
