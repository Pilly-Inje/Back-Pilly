package com.inje.pilly.dto;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PredictHealthDataRequestDTO {
    private Long userId;
    private List<HealthDataDTO> records;
}
