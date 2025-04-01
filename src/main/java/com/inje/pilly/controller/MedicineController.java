package com.inje.pilly.controller;

import com.inje.pilly.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/medicine")
public class MedicineController {
    @Autowired
    private MedicineService medicineService;

    @Operation(summary = "오늘 약 알람", description = "총 섭취 알약 갯수와 약 id, 시간별 처방전 id")
    @GetMapping("/todayAlarm/{userId}")
    public ResponseEntity<Map<String,Object>> getTodayAlarms(@PathVariable Long userId){
        Map<String,Object> alarms = medicineService.getTodayAlarms(userId);
        return ResponseEntity.ok(alarms);
    }
    @Operation(summary = "db-모든 약 정보", description = "id, name, img만")
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllMedicines() {
        return medicineService.getAllMedicines();
    }

    @Operation(summary = "특정 약 검색", description = "특정 약에 대한 상세 정보")
    @GetMapping("/search/{medicineId}")
    public ResponseEntity<Map<String, Object>> getMedicineDetail(@PathVariable Long medicineId) {
        return medicineService.searchMedicineDetail(medicineId);
    }

    @GetMapping("/update-origin-image-urls")
    public ResponseEntity<?> updateOriginImageUrls() {
        return medicineService.saveOnlyImageUrlsFromApi();
    }

//    @Operation(summary = "사용 X")
//    @GetMapping("/{medicineName}") //사용자 약 검색
//    public ResponseEntity<Map<String, Object>> searchMedicineDetail(@PathVariable String medicineName){
//        return medicineService.searchMedicineDetail(medicineName);
//    }
}