package com.inje.pilly.service;

import com.inje.pilly.dto.HealthDataRequestDTO;
import com.inje.pilly.dto.MedicineEffectivenessDTO;
import com.inje.pilly.dto.SideEffectTrainRecordDTO;
import com.inje.pilly.entity.HealthData;
import com.inje.pilly.repository.HealthDataRepository;
import com.inje.pilly.repository.MedicineEffectivenessRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SideEffectTrainService {
    private final WebClient webClient;
    private final HealthDataRepository healthDataRepository;

    public SideEffectTrainService(WebClient webClient,HealthDataRepository healthDataRepository){
        this.webClient = webClient;
        this.healthDataRepository = healthDataRepository;
    }

    public void trainModel(MedicineEffectivenessDTO dto) {
        HealthData healthData = healthDataRepository.findTopByUser_UserIdOrderByRecordDateDesc(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 건강 정보를 찾을 수 없습니다."));

        int fatigueLevel = healthData.getFatigueLevel();
        int dizzinessLevel = healthData.getDizzinessLevel();
        String mood = healthData.getMood();
        float sleepHours = healthData.getSleepHours();

        SideEffectTrainRecordDTO record = new SideEffectTrainRecordDTO(
                dto.getUserId(),
                dto.getMedicineId(),
                fatigueLevel,
                dizzinessLevel,
                mood,
                sleepHours,
                dto.isSideEffectOccurred(),
                dto.getSideEffects()
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("data", List.of(record));

        try {
            String response = webClient.post()
                    .uri("http://localhost:8000/train-side-effect")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("학습 요청 성공: " + response);
        } catch (Exception e) {
            System.err.println("FastAPI 학습 요청 실패: " + e.getMessage());
        }
    }
}
