package com.inje.pilly.controller;

import com.inje.pilly.dto.HealthDataDTO;
import com.inje.pilly.dto.HealthDataRequestDTO;
import com.inje.pilly.service.HealthDataService;
import com.inje.pilly.service.HealthPredictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/healthData")
public class HealthDataController {
    private HealthDataService healthDataService;
    private HealthPredictService healthPredictService;

    @Autowired
    public HealthDataController(HealthDataService healthDataService,HealthPredictService healthPredictService){
        this.healthDataService = healthDataService;
        this.healthPredictService = healthPredictService;
    }
    @PostMapping("/record")
    public ResponseEntity<String> recordHealthData(@RequestBody HealthDataRequestDTO dto){
        healthDataService.saveHealthData(dto);
        return ResponseEntity.ok("건강 데이터 저장 완료!");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<HealthDataDTO>> getUserHealthData(@PathVariable Long userId) {
        List<HealthDataDTO> data = healthDataService.getUserHealthData(userId);
        return ResponseEntity.ok(data);
    }
    @GetMapping("/predict/{userId}")
    public ResponseEntity<Map<String, Object>> predictFatigue(@PathVariable Long userId) {
        double result = healthPredictService.predictFatigue(userId);
        String message = healthPredictService.getFatigueFeedback(result);

        Map<String, Object> response = new HashMap<>();
        response.put("predictedFatigue", result);
        response.put("feedback", message);

        return ResponseEntity.ok(response);
    }
}
