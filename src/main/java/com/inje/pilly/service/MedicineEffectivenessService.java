package com.inje.pilly.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.inje.pilly.dto.HealthDataDTO;
import com.inje.pilly.dto.MedicineEffectivenessDTO;
import com.inje.pilly.dto.MedicineEffectivenessResponseDTO;
import com.inje.pilly.entity.Medicine;
import com.inje.pilly.entity.MedicineEffectiveness;
import com.inje.pilly.entity.User;
import com.inje.pilly.repository.MedicineEffectivenessRepository;
import com.inje.pilly.repository.MedicineRepository;
import com.inje.pilly.repository.PrescriptionMedicineRepository;
import com.inje.pilly.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

@Service
public class MedicineEffectivenessService {
    private final MedicineEffectivenessRepository medicineEffectivenessRepository;
    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final ObjectMapper objectMapper;
    private final SideEffectTrainService trainService;

    public MedicineEffectivenessService(MedicineEffectivenessRepository medicineEffectivenessRepository,UserRepository userRepository,MedicineRepository medicineRepository,ObjectMapper objectMapper,SideEffectTrainService trainService){
        this.medicineEffectivenessRepository = medicineEffectivenessRepository;
        this.userRepository = userRepository;
        this.medicineRepository = medicineRepository;
        this.objectMapper = objectMapper;
        this.trainService = trainService;
    }
    public MedicineEffectivenessResponseDTO  saveEffectiveness(MedicineEffectivenessDTO dto) throws JsonProcessingException {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        Medicine medicine = medicineRepository.findById(dto.getMedicineId())
                .orElseThrow(() -> new IllegalArgumentException("약 정보를 찾을 수 없습니다."));

        //부작용 정보 저장
        MedicineEffectiveness entity = new MedicineEffectiveness();
        entity.setUser(user);
        entity.setMedicine(medicine);
        entity.setRecordDate(dto.getRecordDate());
        entity.setEffectLevel(dto.getEffectLevel());
        entity.setSideEffectOccurred(dto.isSideEffectOccurred());
        entity.setComments(dto.getComments());

        String sideEffectsJson = objectMapper.writeValueAsString(dto.getSideEffects());
        System.out.println("📦 저장되는 부작용 JSON: " + sideEffectsJson);
        entity.setSideEffects(sideEffectsJson);

        MedicineEffectiveness saved = medicineEffectivenessRepository.save(entity);

        //학습 요청 트리거
        trainService.trainModelWithAllData();


        //응답
        MedicineEffectivenessResponseDTO response = new MedicineEffectivenessResponseDTO();
        response.setRecordDate(saved.getRecordDate());
        response.setEffectLevel(saved.getEffectLevel());
        response.setSideEffectOccurred(saved.isSideEffectOccurred());

        List<String> sideEffectsList = objectMapper.readValue(saved.getSideEffects(), new TypeReference<>() {});
        response.setSideEffects(sideEffectsList);


        response.setComments(saved.getComments());

        return response;
    }
}
