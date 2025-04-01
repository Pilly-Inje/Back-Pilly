package com.inje.pilly.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAlarmTimeRequestDTO {
    private Long prescriptionId;
    private String oldTime;
    private String newTime;
}
