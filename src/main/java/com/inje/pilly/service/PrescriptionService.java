package com.inje.pilly.service;

import com.inje.pilly.dto.*;
import com.inje.pilly.entity.Medicine;
import com.inje.pilly.entity.Prescription;
import com.inje.pilly.entity.HealthData;
import com.inje.pilly.entity.PrescriptionMedicine;
import com.inje.pilly.entity.User;
import com.inje.pilly.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service

public class PrescriptionService {
    private final UserRepository userRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionMedicineRepository prescriptionMedicineRepository;
    private final HealthDataRepository healthDataRepository;
    private final JdbcTemplate jdbcTemplate;
    private final WebClient webClient;

    public PrescriptionService(UserRepository userRepository,PrescriptionRepository prescriptionRepository,MedicineRepository medicineRepository,PrescriptionMedicineRepository prescriptionMedicineRepository,HealthDataRepository healthDataRepository,JdbcTemplate jdbcTemplate,WebClient webClient){
        this.userRepository = userRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.medicineRepository = medicineRepository;
        this.prescriptionMedicineRepository = prescriptionMedicineRepository;
        this.healthDataRepository = healthDataRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.webClient = webClient;
    }
    //처방전 저장 및 업데이트? 함
    public PrescriptionResponseDTO saveOrUpdatePrescription(PrescriptionRequestDTO prescriptionRequestDTO) {

        Long userId = prescriptionRequestDTO.getUserId();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Long prescriptionId = prescriptionRequestDTO.getPrescriptionId();
        List<String> medicineNames = prescriptionRequestDTO.getMedicineNames();
        List<Medicine> medicines = medicineRepository.findByMedicineNameIn(medicineNames);

        List<MedicinePredictionInput> inputList = medicines.stream()
                .map(m -> new MedicinePredictionInput(m.getMedicineId(), m.getMedicineName()))
                .toList();

        System.out.println("전달된 약 목록: " + medicineNames);

        List<String> feedbackMessages = new ArrayList<>();
        Prescription prescription;

        if (prescriptionId != null) {
            prescription = prescriptionRepository.findById(prescriptionId)
                    .orElseThrow(() -> new RuntimeException("Prescription not found with id: " + prescriptionId));

            prescription.setPrescriptionName(prescriptionRequestDTO.getPrescriptionName());
            prescription.setStartDate(prescriptionRequestDTO.getStartDate());
            prescription.setEndDate(prescriptionRequestDTO.getEndDate());
            prescription.setMorningTime(isValidTime(prescriptionRequestDTO.getMorningTime()) ?
                    LocalTime.parse(prescriptionRequestDTO.getMorningTime()) : null);
            prescription.setAfternoonTime(isValidTime(prescriptionRequestDTO.getAfternoonTime()) ?
                    LocalTime.parse(prescriptionRequestDTO.getAfternoonTime()) : null);
            prescription.setEveningTime(isValidTime(prescriptionRequestDTO.getEveningTime()) ?
                    LocalTime.parse(prescriptionRequestDTO.getEveningTime()) : null);


        } else {
            prescription = new Prescription();
            prescription.setPrescriptionName(prescriptionRequestDTO.getPrescriptionName());
            prescription.setStartDate(prescriptionRequestDTO.getStartDate());
            prescription.setEndDate(prescriptionRequestDTO.getEndDate());
            prescription.setMorningTime(isValidTime(prescriptionRequestDTO.getMorningTime()) ?
                    LocalTime.parse(prescriptionRequestDTO.getMorningTime()) : null);
            prescription.setAfternoonTime(isValidTime(prescriptionRequestDTO.getAfternoonTime()) ?
                    LocalTime.parse(prescriptionRequestDTO.getAfternoonTime()) : null);
            prescription.setEveningTime(isValidTime(prescriptionRequestDTO.getEveningTime()) ?
                    LocalTime.parse(prescriptionRequestDTO.getEveningTime()) : null);

        }
        prescription.setUser(user);
        Prescription savedPrescription = prescriptionRepository.save(prescription);
        updatePrescriptionMedicines(savedPrescription, medicineNames);

        // 사용자 최신 건강 상태 조회
        HealthData latestHealth = healthDataRepository.findTopByUser_UserIdOrderByRecordDateDesc(userId)
                .orElseThrow(() -> new RuntimeException("사용자 건강 정보가 없습니다."));

        int moodEncoded = switch (latestHealth.getMood()) {
            case "나쁨" -> 0;
            case "보통" -> 1;
            case "좋음" -> 2;
            default -> 1;
        };
        System.out.println("사용자 최신 건강 상태: " + latestHealth);

        List<SideEffectPredictRequestDTO> requestList = inputList.stream()
                .map(input -> new SideEffectPredictRequestDTO(
                        userId,
                        input.getMedicineId(),
                        input.getMedicineName(),
                        latestHealth.getFatigueLevel(),
                        latestHealth.getDizzinessLevel(),
                        moodEncoded,
                        latestHealth.getSleepHours()
                )).toList();

        SideEffectBatchPredictRequestDTO request = new SideEffectBatchPredictRequestDTO(requestList);

        try {
            SideEffectBatchPredictResponseDTO responses = webClient.post()
                    .uri("http://localhost:8000/predict-side-effect")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(SideEffectBatchPredictResponseDTO.class)
                    .block();

            if (responses != null && responses.getResult() != null) {
                for (SideEffectPredictResponseDTO result : responses.getResult()) {
                    if (result.getFeedback() != null && !result.getFeedback().isBlank()) {
                        feedbackMessages.add(result.getFeedback());
                    }
                }
            }


        } catch (Exception e) {
            System.out.println("부작용 예측 실패 (배치): " + e.getMessage());
        }
        return new PrescriptionResponseDTO(savedPrescription.getPrescriptionId(), "처방전 저장 완료", feedbackMessages);
    }

    private boolean isValidTime(String time) {
        return time != null && !time.trim().isEmpty();
    }

    //없는 약들도 ㄱ , 있는 약들 대조 ㄱ
    private void updatePrescriptionMedicines(Prescription prescription, List<String> medicineNames) {
        List<Medicine> medicines = new ArrayList<>();

        for (String medicineName : medicineNames) {
            Medicine medicine = medicineRepository.findByMedicineName(medicineName)
                    .orElseGet(() -> {
                        Medicine newMedicine = new Medicine();
                        newMedicine.setMedicineName(medicineName);
                        return medicineRepository.save(newMedicine);
                    });
            medicines.add(medicine);
        }

        for (Medicine medicine : medicines) {
            PrescriptionMedicine prescriptionMedicine = new PrescriptionMedicine();
            prescriptionMedicine.setPrescription(prescription);
            prescriptionMedicine.setMedicine(medicine);
            prescriptionMedicineRepository.save(prescriptionMedicine);
        }
    }

    //처방전 조회
    public List<PrescriptionUserResponseDTO> getPrescriptionsByUserId(Long userId) {
        // userId로 모든 처방전 조회 (start_date 기준으로 내림차순 정렬)
        List<Prescription> prescriptions = prescriptionRepository.findByUserIdOrderByStartDateDesc(userId);

        List<PrescriptionUserResponseDTO> responseDTOList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Prescription prescription : prescriptions) {
            List<PrescriptionMedicine> prescriptionMedicines = prescriptionMedicineRepository
                    .findByPrescription_PrescriptionId(prescription.getPrescriptionId());

            List<String> medicineNames = prescriptionMedicines.stream()
                    .map(prescriptionMedicine -> prescriptionMedicine.getMedicine() != null ?
                            prescriptionMedicine.getMedicine().getMedicineName() : "Custom Medicine")
                    .collect(Collectors.toList());

            // 복약 상태 결정
            String status;
            if (prescription.getStartDate().isAfter(today)) {
                status = "복약전"; // StartDate가 현재 날짜 이후면 복약전
            } else if (prescription.getEndDate().isBefore(today)) {
                status = "복약완료"; // EndDate가 현재 날짜 이전이면 복약완료
            } else {
                status = "복약중"; // 그 외에는 복약중
            }

            PrescriptionUserResponseDTO dto = new PrescriptionUserResponseDTO(
                    prescription.getPrescriptionId(),
                    prescription.getPrescriptionName(),
                    prescription.getStartDate(),
                    prescription.getEndDate(),
                    status,
                    medicineNames
            );

            responseDTOList.add(dto);
        }
        return responseDTOList;
    }

    //처방전 삭제
    @Transactional
    public PrescriptionDeleteResponseDTO deletePrescription(Long userId, Long prescriptionId) {
        String sql = "DELETE FROM prescription WHERE prescription_id = ? AND user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, prescriptionId, userId);

        if (rowsAffected == 0) {
            throw new IllegalArgumentException("해당 처방전이 존재하지 않거나 사용자가 아닙니다.");
            //return new PrescriptionDeleteResponseDTO(false, "처방전 삭제 실패");
        }else{
            return new PrescriptionDeleteResponseDTO(true,"처방전 삭제 왼료");
        }
    }

    //특정 처방전 정보 ㄱ
    public PrescriptionDetailResponseDTO getPrescriptionDetails(Long prescriptionId) {

        Optional<Prescription> prescription = prescriptionRepository.findById(prescriptionId);

        if (prescription.isPresent()) {

            Prescription prescriptionData = prescription.get();

            List<PrescriptionMedicine> prescriptionMedicines = prescriptionMedicineRepository.findByPrescriptionId(prescriptionId);

            List<MedicineDTO> medicineDTOList = new ArrayList<>();
            for (PrescriptionMedicine prescriptionMedicine : prescriptionMedicines) {
                Medicine medicine = medicineRepository.findById(prescriptionMedicine.getMedicine().getMedicineId()).orElse(null);
                if (medicine != null) {
                    MedicineDTO medicineDTO = new MedicineDTO();
                    medicineDTO.setMedicineId(medicine.getMedicineId());
                    medicineDTO.setMedicineName(medicine.getMedicineName());
                    medicineDTO.setMedicineImageUrl(medicine.getMedicineImage());
                    medicineDTOList.add(medicineDTO);
                }
            }

            PrescriptionDetailResponseDTO response = new PrescriptionDetailResponseDTO();
            response.setPrescriptionName(prescriptionData.getPrescriptionName());
            response.setStartDate(prescriptionData.getStartDate());
            response.setFile(prescriptionData.getFile());
            response.setMedicines(medicineDTOList);  // 약품 정보 리스트 설정

            return response;
        } else {
            throw new IllegalArgumentException("처방전을 찾을 수 없습니다.");
        }
    }
    //처방전 시간 수정 ㄱ
    public String updatePrescriptionTime(UpdateAlarmTimeRequestDTO request) {
        Optional<Prescription> optionalPrescription = prescriptionRepository.findById(request.getPrescriptionId());

        if (optionalPrescription.isEmpty()) {
            return "해당 처방전을 찾을 수 없습니다.";
        }

        Prescription prescription = optionalPrescription.get();

        LocalTime oldTime = LocalTime.parse(request.getOldTime(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime newTime = LocalTime.parse(request.getNewTime(), DateTimeFormatter.ofPattern("HH:mm"));

        if (oldTime.equals(prescription.getMorningTime())) {
            prescription.setMorningTime(newTime);
        } else if (oldTime.equals(prescription.getAfternoonTime())) {
            prescription.setAfternoonTime(newTime);
        } else if (oldTime.equals(prescription.getEveningTime())) {
            prescription.setEveningTime(newTime);
        } else {
            return "기존 시간과 일치하는 알람 시간이 없습니다.";
        }

        prescriptionRepository.save(prescription);

        return "처방전 알람 시간이 성공적으로 변경되었습니다.";
    }
    //처방전 시간 삭제 ㄱ
    public String deletePrescriptionTime(UpdateAlarmTimeRequestDTO request) {
        Optional<Prescription> optionalPrescription = prescriptionRepository.findById(request.getPrescriptionId());

        if (optionalPrescription.isEmpty()) {
            return "해당 처방전을 찾을 수 없습니다.";
        }

        Prescription prescription = optionalPrescription.get();

        // 문자열을 LocalTime으로 변환
        LocalTime deleteTime = LocalTime.parse(request.getOldTime(), DateTimeFormatter.ofPattern("HH:mm"));

        // 기존 시간과 일치하는 값을 삭제
        boolean isDeleted = false;
        if (deleteTime.equals(prescription.getMorningTime())) {
            prescription.setMorningTime(null);
            isDeleted = true;
        }
        if (deleteTime.equals(prescription.getAfternoonTime())) {
            prescription.setAfternoonTime(null);
            isDeleted = true;
        }
        if (deleteTime.equals(prescription.getEveningTime())) {
            prescription.setEveningTime(null);
            isDeleted = true;
        }

        if (!isDeleted) {
            return "삭제할 시간이 처방전에 존재하지 않습니다.";
        }

        prescriptionRepository.save(prescription);

        return "처방전 알람 시간이 성공적으로 삭제되었습니다.";
    }

    //날짜, 시간 기반 처방전 정보
    public List<PrescriptionUserResponseDTO> getPrescriptionInfoForTime(Long userId, LocalTime time) {
        LocalDate today = LocalDate.now();
        // 해당 날짜와 사용자의 처방전 리스트 조회
        List<Prescription> prescriptions = prescriptionRepository.findByUserIdAndDateRange(userId, today);

        // 결과 리스트
        List<PrescriptionUserResponseDTO> prescriptionDTOList = new ArrayList<>();

        // 각 처방전의 아침, 점심, 저녁 알람 시간을 체크
        for (Prescription prescription : prescriptions) {
            // 처방전의 시작일과 종료일을 확인
            if (isPrescriptionActive(prescription, today)) {
                // 해당 시간대에 알람이 설정되어 있는지 확인
                if (isAlarmTimeMatch(prescription, time)) {
                    // 처방전 정보와 약품 정보를 DTO에 저장
                    PrescriptionUserResponseDTO dto = new PrescriptionUserResponseDTO(
                            prescription.getPrescriptionId(),
                            prescription.getPrescriptionName(),
                            prescription.getStartDate(),
                            prescription.getEndDate(),
                            prescription.getStatus(),
                            getMedicineNamesByPrescriptionId(prescription.getPrescriptionId())
                    );
                    prescriptionDTOList.add(dto);
                }
            }
        }
        return prescriptionDTOList;
    }
    private boolean isPrescriptionActive(Prescription prescription, LocalDate date) {
        // 처방전의 시작일과 종료일을 체크하여 오늘 날짜가 범위에 포함되는지 확인
        return !date.isBefore(prescription.getStartDate()) && !date.isAfter(prescription.getEndDate());
    }

    private boolean isAlarmTimeMatch(Prescription prescription, LocalTime time) {
        // 처방전의 알람 시간대(아침, 점심, 저녁)와 비교하여 일치하는지 확인
        return (prescription.getMorningTime() != null && prescription.getMorningTime().equals(time)) ||
                (prescription.getAfternoonTime() != null && prescription.getAfternoonTime().equals(time)) ||
                (prescription.getEveningTime() != null && prescription.getEveningTime().equals(time));
    }
    private List<String> getMedicineNamesByPrescriptionId(Long prescriptionId) {
        // 해당 처방전 ID에 해당하는 약품명을 조회
        List<PrescriptionMedicine> medicines = prescriptionMedicineRepository.findByPrescriptionId(prescriptionId);
        List<String> medicineNames = medicines.stream()
                .map(prescriptionMedicine -> {
                    // prescriptionMedicine에서 medicineId를 사용하여 약품명을 조회
                    Medicine medicine = medicineRepository.findByMedicineId(prescriptionMedicine.getMedicine().getMedicineId())
                            .orElseThrow(() -> new RuntimeException("Medicine not found for id: " + prescriptionMedicine.getMedicine().getMedicineId()));
                    return medicine.getMedicineName();
                })
                .collect(Collectors.toList());
        return medicineNames;
    }
}