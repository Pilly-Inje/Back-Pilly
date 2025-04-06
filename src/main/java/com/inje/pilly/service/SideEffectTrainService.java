package com.inje.pilly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inje.pilly.dto.MedicineEffectivenessDTO;
import com.inje.pilly.dto.SideEffectTrainRecordDTO;
import com.inje.pilly.entity.HealthData;
import com.inje.pilly.repository.HealthDataRepository;
import com.inje.pilly.repository.MedicineEffectivenessRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.management.ObjectName;
import java.time.LocalDate;
import java.util.*;

@Service
public class SideEffectTrainService {
    private final WebClient webClient;
    private final HealthDataRepository healthDataRepository;
    private final MedicineEffectivenessRepository medicineEffectivenessRepository;
    private final ObjectMapper objectMapper;

    public SideEffectTrainService(WebClient webClient, HealthDataRepository healthDataRepository, MedicineEffectivenessRepository medicineEffectivenessRepository, ObjectMapper objectMapper){
        this.webClient = webClient;
        this.healthDataRepository = healthDataRepository;
        this.medicineEffectivenessRepository = medicineEffectivenessRepository;
        this.objectMapper = objectMapper;
    }
    @Async
    public void trainModelWithAllData() {
        try {

            List<Object[]> rawData = medicineEffectivenessRepository.findTrainableSideEffectDataRaw();
            List<SideEffectTrainRecordDTO> dtoList = new ArrayList<>();

            for (Object[] row : rawData) {
                Long userId = row[0] != null ? ((Number) row[0]).longValue() : null;
                Long medicineId = row[1] != null ? ((Number) row[1]).longValue() : null;
                int fatigue = row[2] != null ? ((Number) row[2]).intValue() : 0;
                int dizziness = row[3] != null ? ((Number) row[3]).intValue() : 0;
                String mood = (String) row[4];
                float sleep = row[5] != null ? ((Number) row[5]).floatValue() : 0;
                boolean occurred = row[6] != null && (Boolean) row[6];

                String json = (String) row[7];
                List<String> sideEffects = json != null ? objectMapper.readValue(json, new TypeReference<>() {}) : new ArrayList<>();

                LocalDate recordDate = row[8] != null ? ((java.sql.Date) row[8]).toLocalDate() : null;

                dtoList.add(new SideEffectTrainRecordDTO(userId, medicineId, fatigue, dizziness, mood, sleep, occurred, sideEffects, recordDate != null ? recordDate.toString() : null));
                System.out.println("row 배열 출력: " + Arrays.toString(row));
            }
            Map<String, SideEffectTrainRecordDTO> uniqueMap = new HashMap<>();
            for (SideEffectTrainRecordDTO dto : dtoList) {
                String key = dto.getUserId() + "_" + dto.getRecordDate();
                uniqueMap.putIfAbsent(key, dto);
            }
            List<SideEffectTrainRecordDTO> deduplicated = new ArrayList<>(uniqueMap.values());

            System.out.println("총 학습 레코드 수 (중복 제거 전): " + dtoList.size());
            System.out.println("총 학습 레코드 수 (중복 제거 후): " + deduplicated.size());


            Map<String, Object> payload = new HashMap<>();
            payload.put("data", deduplicated);
            System.out.println("data: "+payload);
            String response = webClient.post()
                    .uri("http://localhost:8000/train-side-effect")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("FastAPI 학습 응답: " + response);

        } catch (Exception e) {
            System.err.println("학습 요청 실패: " + e.getMessage());
        }
    }
}

