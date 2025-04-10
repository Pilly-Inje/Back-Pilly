package com.inje.pilly.controller;

import com.inje.pilly.dto.TodayAlarmResponseDTO;
import com.inje.pilly.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary = "오늘 약 알람", description = "총 섭취 알약 갯수와 약 id, 시간별 처방전 id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "오늘의 약 알람 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TodayAlarmResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "해당 사용자의 알람 정보 없음")
            })
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

    @Operation(summary = "특정 약 검색", description = "특정 약에 대한 상세 정보",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "약 정보 조회 결과",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example =
                                            "{\n" +
                                                    "  \"success\": true,\n" +
                                                    "  \"message\": \"약 정보 조회 성공\",\n" +
                                                    "  \"data\": {\n" +
                                                    "    \"medicineId\": 1,\n" +
                                                    "    \"medicineName\": \"타이레놀\",\n" +
                                                    "    \"effect\": \"통증 완화\",\n" +
                                                    "    \"dosage\": \"하루 3회\",\n" +
                                                    "    \"caution\": \"간 기능 장애 주의\",\n" +
                                                    "    \"medicineImage\": \"https://example.com/image.png\",\n" +
                                                    "    \"sideEffectHistory\": [\n" +
                                                    "      {\n" +
                                                    "        \"userId\": 1,\n" +
                                                    "        \"recordDate\": \"2025-04-10\",\n" +
                                                    "        \"effectLevel\": 3,\n" +
                                                    "        \"sideEffectOccurred\": true,\n" +
                                                    "        \"sideEffects\": [\"어지러움\", \"구토\"],\n" +
                                                    "        \"comments\": \"복용 후 속이 안 좋음\"\n" +
                                                    "      }\n" +
                                                    "    ]\n" +
                                                    "  }\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "해당 ID의 약물이 존재하지 않음")
            })
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