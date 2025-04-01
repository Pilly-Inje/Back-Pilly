package com.inje.pilly.service;

import com.inje.pilly.entity.Medicine;
import com.inje.pilly.entity.Prescription;
import com.inje.pilly.repository.MedicineRepository;
import com.inje.pilly.repository.PrescriptionMedicineRepository;
import com.inje.pilly.repository.PrescriptionRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.CannedAccessControlList;


import jakarta.transaction.Transactional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MedicineService {
    private MedicineRepository medicineRepository;
    private PrescriptionRepository prescriptionRepository;
    private PrescriptionMedicineRepository prescriptionMedicineRepository;

    @Value("${MedicationApi.url}")
    private String API_URL;

    @Value("${MedicationKey.url}")
    private String API_KEY;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    @Autowired
    public MedicineService(
            MedicineRepository medicineRepository,
            PrescriptionRepository prescriptionRepository,
            PrescriptionMedicineRepository prescriptionMedicineRepository,
            AmazonS3 amazonS3
    ) {
        this.medicineRepository = medicineRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionMedicineRepository = prescriptionMedicineRepository;
        this.amazonS3 = amazonS3;
    }

    //  API에서 모든 약 데이터를 가져와 DB에 저장
    @Transactional
    public ResponseEntity<Map<String, Object>> loadAllMedicines() {
        JSONArray dataArray = fetchAllMedicineData();
        System.out.println(" API에서 가져온 데이터 개수: " + dataArray.length());

        int savedCount = saveMedicineDataToDB(dataArray);
        System.out.println(" DB에 저장된 데이터 개수: " + savedCount);

        return ResponseEntity.ok(Map.of(
                "success", savedCount > 0,
                "message", "API에서 모든 데이터를 가져와 DB에 저장 완료",
                "savedCount", savedCount
        ));
    }

    // 원본 url 다시 저장
    @Transactional
    public ResponseEntity<Map<String, Object>> saveOnlyImageUrlsFromApi() {
        JSONArray dataArray = fetchAllMedicineData();
        List<Medicine> medicines = medicineRepository.findAll(); // 순서 보장되는 JPA 메서드
        int updatedCount = 0;

        int minLength = Math.min(dataArray.length(), medicines.size());

        for (int i = 0; i < minLength; i++) {
            JSONObject item = dataArray.optJSONObject(i);
            if (item == null) continue;

            String itemImage = item.optString("itemImage", "").trim();
            if (itemImage.isEmpty()) continue;

            Medicine medicine = medicines.get(i);
            medicine.setOriginImageUrl(itemImage); // 순서대로 넣는다
            medicineRepository.save(medicine);
            updatedCount++;
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "이미지 URL 순서대로 업데이트 완료",
                "updatedCount", updatedCount
        ));
    }


    //url
    //  이미지 다운로드 후 S3에 업로드
    @Transactional
    public ResponseEntity<Map<String, Object>> updateMedicineImages() {
        List<Medicine> medicines = medicineRepository.findAll();
        int updatedCount = 0;

        for (Medicine medicine : medicines) {
            //  중복 업로드 방지
            if (medicine.getMedicineImage() != null && !medicine.getMedicineImage().isBlank()) {
                continue;
            }

            String imageUrl = medicine.getOriginImageUrl(); // 원본 이미지 URL 사용
            if (imageUrl == null || imageUrl.isBlank()) continue;

            try {
                // 1. 이미지 URL에서 바이트 배열로 안전하게 읽기
                URL url = new URL(imageUrl);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (InputStream is = url.openStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                }
                byte[] imageBytes = baos.toByteArray();

                // S3에 업로드할 메타데이터 설정
                String filename = "medicine_images/" + UUID.randomUUID() + ".jpg";
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType("image/jpeg");
                metadata.setContentLength(imageBytes.length); // 정확한 길이 설정

                // 업로드 요청 객체 생성
                PutObjectRequest request = new PutObjectRequest(
                        bucket,
                        filename,
                        new ByteArrayInputStream(imageBytes),
                        metadata
                );

                // S3에 업로드
                amazonS3.putObject(request);

                // S3 공개 URL로 DB 업데이트
                String publicUrl = amazonS3.getUrl(bucket, filename).toString();
                medicine.setMedicineImage(publicUrl);
                medicineRepository.save(medicine);
                updatedCount++;

            } catch (Exception e) {
                System.out.println("이미지 업로드 실패: " + medicine.getMedicineName() + " → " + e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "S3에 이미지 업로드 및 DB 업데이트 완료 (중복 방지)",
                "updatedCount", updatedCount
        ));
    }






    //  모든 약 정보 조회 (캐시 쓴거임)
    @Cacheable(value = "allMedicines", key = "'allMedicines'")
    public ResponseEntity<Map<String, Object>> getAllMedicines() {
        System.out.println("캐시 없음");
        List<Medicine> medicines = medicineRepository.findAll();
        if (medicines.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "등록된 약물이 없습니다.", "data", Collections.emptyList()));
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Medicine medicine : medicines) {
            if (medicine.getMedicineName() == null) {
                System.out.println(" 경고: NULL 값이 포함된 데이터 발견 → " + medicine);
                continue;
            }
            Map<String, Object> medicineInfo = new LinkedHashMap<>();
            medicineInfo.put("medicineId", medicine.getMedicineId());
            medicineInfo.put("medicineName", medicine.getMedicineName());
//            medicineInfo.put("effect", medicine.getEffect());
//            medicineInfo.put("dosage", medicine.getDosage());
//            medicineInfo.put("caution", medicine.getCaution());

            // 이미지가 null이 아니면 이미지도 추가
            if (medicine.getMedicineImage() != null) {
                medicineInfo.put("medicineImage", medicine.getMedicineImage());
            }
            results.add(medicineInfo);
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "모든 약 정보를 조회했습니다.", "data", results));
    }

    //특정 약 상세 정보 조회
    public ResponseEntity<Map<String, Object>> searchMedicineDetail(String medicineName) {
        List<Medicine> medicines = medicineRepository.findByMedicineNameContainingIgnoreCase(medicineName);

        if (medicines.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "해당 이름을 포함하는 약물이 없습니다.",
                    "data", Collections.emptyList()
            ));
        }

        List<Map<String, Object>> results = new LinkedList<>();
        for (Medicine medicine : medicines) {
            Map<String, Object> medicineInfo = new LinkedHashMap<>();
            medicineInfo.put("medicineId", medicine.getMedicineId());
            medicineInfo.put("medicineName", medicine.getMedicineName());
            medicineInfo.put("effect", medicine.getEffect());
            medicineInfo.put("dosage", medicine.getDosage());
            medicineInfo.put("caution", medicine.getCaution());
            medicineInfo.put("medicineImage", medicine.getMedicineImage());
            results.add(medicineInfo);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "약 목록 조회 성공",
                "data", results
        ));
    }

    //  API 데이터 저장 (이름 없는 데이터 필터링 추가)
    @Transactional
    public int saveMedicineDataToDB(JSONArray dataArray) {
        List<Medicine> medicineList = new ArrayList<>();
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject item = dataArray.optJSONObject(i);
            if (item == null) continue;

            String medicineName = item.optString("itemName", "").trim();

            //  이름이 없는 데이터는 저장안함
            if (medicineName.isEmpty()) {
                System.out.println("이름이 없는 약이 발견되어 저장하지 않음 → " + item.toString());
                continue;
            }

            Optional<Medicine> existingMedicine = medicineRepository.findByMedicineName(medicineName);
            if (existingMedicine.isPresent()) {
                System.out.println("이미 존재하는 약물이므로 저장하지 않음 → " + medicineName);
                continue;
            }

            Medicine medicine = new Medicine();
            medicine.setMedicineName(medicineName);
            medicine.setEffect(item.optString("efcyQesitm", ""));
            medicine.setDosage(item.optString("useMethodQesitm", ""));
            medicine.setCaution(item.optString("atpnWarnQesitm", ""));
            medicine.setOriginImageUrl(item.optString("itemImage", ""));

            System.out.println(" 저장할 약 정보: " + medicine.getMedicineName());
            medicineList.add(medicine);
        }

        if (!medicineList.isEmpty()) {
            medicineRepository.saveAll(medicineList);
            medicineRepository.flush();
            System.out.println("총 " + medicineList.size() + "개의 약이 저장됨.");
        } else {
            System.out.println("저장할 데이터가 없음.");
        }
        return medicineList.size();
    }

    //  전체 API 데이터 가져오기 (반복 요청으로 전체 데이터 저장)
    private JSONArray fetchAllMedicineData() throws JSONException {
        JSONArray allData = new JSONArray();
        int pageNo = 1;
        int totalCount = 0;

        do {
            JSONObject response = fetchApiData("&pageNo=" + pageNo + "&numOfRows=100");
            if (response == null) break;

            JSONObject body = response.optJSONObject("body");
            if (body == null) break;

            JSONArray items = body.optJSONObject("items").optJSONArray("item");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    allData.put(items.get(i));
                }
            }

            //  총 데이터 개수 확인
            totalCount = body.optInt("totalCount", 0);
            pageNo++;

            System.out.println("현재까지 가져온 데이터 개수: " + allData.length());
        } while (allData.length() < totalCount);

        return allData;
    }

    //  API 호출 로직
    private JSONObject fetchApiData(String queryParam) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            String url = API_URL + "?serviceKey=" + API_KEY + queryParam;
            System.out.println("최종 API 요청 URL: " + url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();

            if (responseBody == null || responseBody.contains("SERVICE ERROR")) {
                System.out.println("API 요청 실패: SERVICE ERROR 발생");
                return null;
            }

            return XML.toJSONObject(responseBody).optJSONObject("response");

        } catch (Exception e) {
            System.out.println("API 요청 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
//
//    //약 정보 조회
//    public MedicineDetailResponseDTO getMedicineDetails(Long medicineId){
//        Optional<Medicine> medicine = medicineRepository.findByMedicineId(medicineId);
//
//        MedicineDetailResponseDTO response = new MedicineDetailResponseDTO();
//        response.setMedicineId(medicine.get().getMedicineId());
//        response.setMedicineName(medicine.get().getMedicineName());
//        response.setEffect(medicine.get().getEffect());
//        response.setDosage(medicine.get().getDosage());
//        response.setCaution(medicine.get().getCaution());
//
//        return response;
//    }

    //오늘 약 알람 리스트
    public Map<String, Object> getTodayAlarms(Long userId) {
        LocalDate today = LocalDate.now();

        List<Prescription> prescriptions = prescriptionRepository.findByUserIdAndDateRange(userId, today);

        Map<LocalTime, List<Long>> alarmPrescriptionMap = new HashMap<>();
        Map<LocalTime, Integer> alarmCountMap = new HashMap<>();
        Set<Long> allMedicineIds   = new HashSet<>();

        for (Prescription prescription : prescriptions) {
            addAlarmCount(alarmPrescriptionMap, alarmCountMap, prescription.getMorningTime(), prescription.getPrescriptionId());
            addAlarmCount(alarmPrescriptionMap, alarmCountMap, prescription.getAfternoonTime(), prescription.getPrescriptionId());
            addAlarmCount(alarmPrescriptionMap, alarmCountMap, prescription.getEveningTime(), prescription.getPrescriptionId());

            List<Long> medicineIds = prescriptionMedicineRepository.findMedicineIdsByPrescriptionId(prescription.getPrescriptionId());
            allMedicineIds .addAll(medicineIds);
        }

        List<Map<String, Object>> alarmList = alarmCountMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // 시간 순 정렬
                .filter(entry -> entry.getKey() != null) // null 값 제거
                .map(entry -> {
                    Map<String, Object> alarmData = new LinkedHashMap<>();
                    alarmData.put("alarmTime", entry.getKey().format(DateTimeFormatter.ofPattern("HH:mm")));
                    alarmData.put("medicineCount", entry.getValue());
                    alarmData.put("prescriptionIds", alarmPrescriptionMap.get(entry.getKey()));
                    return alarmData;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("alarm", alarmList);
        result.put("totalMedicineCount", allMedicineIds.size());
        result.put("allMedicineIds", new ArrayList<>(allMedicineIds)); // Set -> List 변환

        return result;
    }

    private void addAlarmCount(Map<LocalTime, List<Long>> alarmPrescriptionMap, Map<LocalTime, Integer> alarmCountMap, LocalTime alarmTime, Long prescriptionId) {
        alarmPrescriptionMap.computeIfAbsent(alarmTime, k -> new ArrayList<>()).add(prescriptionId);
        if (alarmTime != null) {
            int medicineCount = prescriptionMedicineRepository.countByPrescriptionId(prescriptionId);
            alarmCountMap.put(alarmTime, alarmCountMap.getOrDefault(alarmTime, 0) + medicineCount);
        }
    }
}