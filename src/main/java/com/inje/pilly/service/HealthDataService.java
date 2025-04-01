package com.inje.pilly.service;

import com.inje.pilly.dto.HealthDataDTO;
import com.inje.pilly.dto.HealthDataRequestDTO;
import com.inje.pilly.entity.HealthData;
import com.inje.pilly.entity.User;
import com.inje.pilly.repository.HealthDataRepository;
import com.inje.pilly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.inje.pilly.dto.PredictHealthDataRequestDTO;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthDataService {

    private final UserRepository userRepository;

    private final HealthDataRepository healthDataRepository;

    private final TrainService trainService;

    public HealthDataService(UserRepository userRepository, HealthDataRepository healthDataRepository,TrainService trainService){
        this.userRepository = userRepository;
        this.healthDataRepository = healthDataRepository;
        this.trainService = trainService;
    }
    public void saveHealthData(HealthDataRequestDTO dto){
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        HealthData data = HealthData.builder()
                .user(user)
                .recordDate(LocalDate.parse(dto.getRecordDate()))
                .fatigueLevel(dto.getFatigueLevel())
                .dizzinessLevel(dto.getDizzinessLevel())
                .mood(dto.getMood())
                .sleepHours(dto.getSleepHours())
                .build();

        healthDataRepository.save(data);

        long count = healthDataRepository.countByUser_UserId(dto.getUserId());

        if (count >= 4) {
            // 학습 요청 트리거
            List<HealthDataDTO> records = getUserHealthData(dto.getUserId());
            trainService.trainModelIfNeeded(dto.getUserId(),records); //비동기 실행으로 바꿈 ->save랑 같이 돌리면 너무 오래 걸림ㅡㅡ
        }
    }

    public List<HealthDataDTO> getUserHealthData(Long userId) {
        return healthDataRepository.findByUser_UserIdOrderByRecordDateAsc(userId)
                .stream()
                .map(data -> new HealthDataDTO(
                        data.getRecordDate().toString(),
                        data.getFatigueLevel(),
                        data.getDizzinessLevel(),
                        data.getMood(),
                        data.getSleepHours()
                ))
                .collect(Collectors.toList());
    }
}
