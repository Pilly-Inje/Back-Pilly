package com.inje.pilly.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inje.pilly.dto.HealthDataRequestDTO;
import com.inje.pilly.dto.MedicineEffectivenessDTO;
import com.inje.pilly.dto.MedicineEffectivenessResponseDTO;
import com.inje.pilly.entity.MedicineEffectiveness;
import com.inje.pilly.service.MedicineEffectivenessService;
import com.inje.pilly.service.SideEffectTrainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medicineEffectiveness")
public class MedicineEffectivenessController {
    private final MedicineEffectivenessService medicineEffectivenessService;

    public MedicineEffectivenessController(MedicineEffectivenessService medicineEffectivenessService){
        this.medicineEffectivenessService = medicineEffectivenessService;
    }
    @PostMapping
    public ResponseEntity<MedicineEffectivenessResponseDTO> createEffectiveness(@RequestBody MedicineEffectivenessDTO dto) throws JsonProcessingException {
        // 부작용 정보를 저장-> 학습 요청 트리거
        MedicineEffectivenessResponseDTO response = medicineEffectivenessService.saveEffectiveness(dto);
        return ResponseEntity.ok(response);
    }
//    @PostMapping("/train")
//    public ResponseEntity<String> train(@RequestBody HealthDataRequestDTO dto) {
//        trainService.trainModel(dto);
//        return ResponseEntity.ok("학습 요청 완료");
//    }
}
