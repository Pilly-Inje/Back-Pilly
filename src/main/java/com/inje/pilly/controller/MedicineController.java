package com.inje.pilly.controller;

import com.inje.pilly.service.MedicineService;
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

    @GetMapping("/todayAlarm/{userId}")
    public ResponseEntity<Map<String,Object>> getTodayAlarms(@PathVariable Long userId){
        Map<String,Object> alarms = medicineService.getTodayAlarms(userId);
        return ResponseEntity.ok(alarms);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllMedicines() {
        return medicineService.getAllMedicines();
    }

    @GetMapping("/search/{medicineName}")
    public ResponseEntity<Map<String, Object>> getMedicineDetail(@PathVariable String medicineName) {
        return medicineService.searchMedicineDetail(medicineName);
    }
    @GetMapping("/{medicineName}") //사용자 약 검색
    public ResponseEntity<Map<String, Object>> searchMedicineDetail(@PathVariable String medicineName){
        return medicineService.searchMedicineDetail(medicineName);
    }
}
