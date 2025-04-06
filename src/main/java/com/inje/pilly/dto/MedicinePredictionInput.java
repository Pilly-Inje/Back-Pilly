package com.inje.pilly.dto;

import lombok.*;
import org.springframework.stereotype.Service;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MedicinePredictionInput {
    private Long medicineId;
    private String medicineName;
}
