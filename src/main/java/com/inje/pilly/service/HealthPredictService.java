package com.inje.pilly.service;

import org.springframework.web.reactive.function.client.WebClient;

import com.inje.pilly.dto.HealthDataDTO;
import com.inje.pilly.dto.PredictHealthDataRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.inje.pilly.dto.HealthDataPredictResultDTO;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthPredictService {

    private final HealthDataService healthDataService;
    private final WebClient webClient;


    public double predictFatigue(Long userId) {
        try {
            List<HealthDataDTO> records = healthDataService.getUserHealthData(userId); // 기존 메서드 재사용


            System.out.println("유저의 기록 개수: " + records.size());

            PredictHealthDataRequestDTO request = new PredictHealthDataRequestDTO(userId, records);

            HealthDataPredictResultDTO response = webClient.post()
                    .uri("/predict")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(HealthDataPredictResultDTO.class)
                    .block();

            if (response == null) {
                System.err.println("FastAPI 응답이 null입니다.");
                return 0;
            }

            return response.getPredictedFatigue();

        } catch (WebClientResponseException e) {
            System.err.println("예측 실패 (webclient 오류): " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return 0;
        } catch (Exception e) {
            System.err.println("예측 실패 (기타 오류): " + e.getMessage());
            return 0;
        }
    }

    public String getFatigueFeedback(double fatigue) {
        if (fatigue <= 4) {
            return "현재 피로도는 안정적이에요. 좋은 상태를 유지해보세요!";
        } else if (fatigue <= 6) {
            return "피로도가 약간 높아지고 있어요. 수면 시간을 늘려보세요.";
        } else {
            return "피로도가 높아질 가능성이 커요! 충분한 휴식과 규칙적인 복약이 필요해요.";
        }
    }
}
