package com.inje.pilly.service;

import com.inje.pilly.dto.HealthDataDTO;
import com.inje.pilly.dto.PredictHealthDataRequestDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
public class TrainService {

    private final WebClient webClient;

    public TrainService(WebClient webClient){
        this.webClient = webClient;
    }
    @Async //백그라운드에서 학습시킴
    public void trainModelIfNeeded(Long userId, List<HealthDataDTO> records) {
        PredictHealthDataRequestDTO request = new PredictHealthDataRequestDTO(userId, records);

        try {
            webClient.post()
                    .uri("http://localhost:8000/train")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            System.out.println("FastAPI에 학습 요청 완료");
        } catch (WebClientResponseException e) {
            System.err.println("학습 요청 실패: "  + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        }
    }
}
