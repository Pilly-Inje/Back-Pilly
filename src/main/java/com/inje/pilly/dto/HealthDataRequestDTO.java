package com.inje.pilly.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthDataRequestDTO {
    private Long userId;
    private String recordDate;
    private int fatigueLevel;
    private int dizzinessLevel;
    private String mood;
    private float sleepHours;

}
