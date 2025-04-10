package com.inje.pilly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodayAlarmResponseDTO {
    @Schema(description = "시간별 알람 리스트")
    private List<AlarmInfo> alarm;

    @Schema(description = "오늘 총 복용할 약의 개수", example = "3")
    private int totalMedicineCount;

    @Schema(description = "오늘 복용할 약의 ID 목록", example = "[11, 12, 15]")
    private List<Long> allMedicineIds;
}