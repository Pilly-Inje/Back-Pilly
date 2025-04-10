package com.inje.pilly.controller;

import com.inje.pilly.dto.*;
import com.inje.pilly.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/prescription")
public class PrescriptionController {
    @Autowired
    private PrescriptionService prescriptionService;

    @Operation(summary = "처방전 저장", description = "주의! 처방전 사진이 있는 처방전인 경우 prescriptionId 포함, 직접 추가(즉, 사진 업로드가 없는 경우)는 prescriptionId 제외")
    @PostMapping("/create")
    public ResponseEntity<PrescriptionResponseDTO> saveOrUpdatePrescription(@RequestBody PrescriptionRequestDTO prescriptionRequestDTO){
        PrescriptionResponseDTO response = prescriptionService.saveOrUpdatePrescription(prescriptionRequestDTO);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "모든 처방전 조회", description = "모든 처방전 리스트")
    @GetMapping("/all/{userId}")
    public List<PrescriptionUserResponseDTO> getPrescriptionByUserId(@PathVariable Long userId){
        return prescriptionService.getPrescriptionsByUserId(userId);
    }
    @Operation(summary = "특정 처방전 삭제", description = "처방전 삭제")
    @DeleteMapping("delete/{userId}/{prescriptionId}")
    public ResponseEntity<PrescriptionDeleteResponseDTO> deletePrescription(
            @PathVariable Long userId, @PathVariable Long prescriptionId){
        PrescriptionDeleteResponseDTO response = prescriptionService.deletePrescription(userId, prescriptionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response); // 성공 시 200 OK
        } else {
            return ResponseEntity.badRequest().body(response); // 실패 시 400 Bad Request
        }
    }
    @Operation(summary = "특정 처방전 조회", description = "특정 처방전 상세정보")
    @GetMapping("/one/{prescriptionId}")
    public ResponseEntity<PrescriptionDetailResponseDTO> getPrescriptionDetails(@PathVariable Long prescriptionId){
        PrescriptionDetailResponseDTO response = prescriptionService.getPrescriptionDetails(prescriptionId);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "특정 처방전 시간 수정")
    @PutMapping("/updateTime")
    public ResponseEntity<MessageResponseDTO> updatePrescriptionTime(@RequestBody UpdateAlarmTimeRequestDTO request) {
        String resultMessage = prescriptionService.updatePrescriptionTime(request);

        return ResponseEntity.ok(new MessageResponseDTO(resultMessage));
    }
    @Operation(summary = "특정 처방전 시간 삭제")
    @DeleteMapping("/delete-time")
    public ResponseEntity<MessageResponseDTO> deletePrescriptionTime(@RequestBody UpdateAlarmTimeRequestDTO request) {
        String resultMessage = prescriptionService.deletePrescriptionTime(request);

        return ResponseEntity.ok(new MessageResponseDTO(resultMessage));
    }
    @Operation(summary = "날짜, 시간별 처방전 ")
    @GetMapping("/alarm/{userId}/{time}")
    public ResponseEntity<List<PrescriptionUserResponseDTO>> getPrescriptionForAlarm(@PathVariable Long userId, @PathVariable LocalTime time){
        List<PrescriptionUserResponseDTO> prescriptions = prescriptionService.getPrescriptionInfoForTime(userId,time);
        return ResponseEntity.ok(prescriptions);
    }
}
