package com.inje.pilly.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

//처방전 생성 요청을 위한 DTO (클라이언트로부터 전달받은 데이터)
@Getter
@Setter
@AllArgsConstructor
public class PrescriptionRequestDTO {
    private Long userId;
    private Long prescriptionId;
    private String prescriptionName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String morningTime;
    private String  afternoonTime;
    private String  eveningTime;
    private List<String> medicineNames; // 약품명 리스트
    private MultipartFile file;
}
