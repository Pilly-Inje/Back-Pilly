package com.inje.pilly.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class AlarmInfo {
    @Schema(description = "복용 알람 시간", example = "08:00")
    private String alarmTime;

    @Schema(description = "해당 시간에 복용할 약 개수", example = "2")
    private int medicineCount;

    @Schema(description = "해당 시간에 복용할 처방전 ID 리스트", example = "[101, 102]")
    private List<Long> prescriptionIds;
}
