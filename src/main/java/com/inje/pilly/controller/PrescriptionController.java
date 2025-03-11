package com.inje.pilly.controller;

import com.inje.pilly.dto.*;
import com.inje.pilly.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/prescription")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    @PostMapping("/create")
    public ResponseEntity<PrescriptionResponseDTO> saveOrUpdatePrescription(@RequestBody PrescriptionRequestDTO prescriptionRequestDTO){
        PrescriptionResponseDTO response = prescriptionService.saveOrUpdatePrescription(prescriptionRequestDTO);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/all/{userId}")
    public List<PrescriptionUserResponseDTO> getPrescriptionByUserId(@PathVariable Long userId){
        return prescriptionService.getPrescriptionsByUserId(userId);
    }
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
    @GetMapping("/one/{prescriptionId}")
    public ResponseEntity<PrescriptionDetailResponseDTO> getPrescriptionDetails(@PathVariable Long prescriptionId){
        PrescriptionDetailResponseDTO response = prescriptionService.getPrescriptionDetails(prescriptionId);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/updateTime")
    public ResponseEntity<Map<String, Object>> updatePrescriptionTime(@RequestBody UpdateAlarmTimeRequestDTO request) {
        String resultMessage = prescriptionService.updatePrescriptionTime(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", resultMessage);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-time")
    public ResponseEntity<Map<String, Object>> deletePrescriptionTime(@RequestBody UpdateAlarmTimeRequestDTO request) {
        String resultMessage = prescriptionService.deletePrescriptionTime(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", resultMessage);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/alarm/{userId}/{time}")
    public ResponseEntity<List<PrescriptionUserResponseDTO>> getPrescriptionForAlarm(@PathVariable Long userId, @PathVariable LocalTime time){
        List<PrescriptionUserResponseDTO> prescriptions = prescriptionService.getPrescriptionInfoForTime(userId,time);
        return ResponseEntity.ok(prescriptions);
    }
}
