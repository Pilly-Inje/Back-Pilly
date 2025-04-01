package com.inje.pilly.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.inje.pilly.entity.Pharmacy;
import com.inje.pilly.repository.PharmacyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;


import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.ZoneId;


@Service
public class PharmacyService {

    @Value("${PharmacyApi.url}")
    private String API_URL;

    @Value("${PharmacyApi.key}")
    private String API_KEY;

    private final PharmacyRepository pharmacyRepository;

    public PharmacyService(PharmacyRepository pharmacyRepository) {
        this.pharmacyRepository = pharmacyRepository;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> fetchAndSavePharmacies() {
        List<JsonNode> dataList = fetchAllPharmacyData();
        System.out.println(" 약국 데이터 총합: " + dataList.size());

        int savedCount = savePharmacyDataToDB(dataList);
        System.out.println("DB에 저장된 약국 수: " + savedCount);

        return ResponseEntity.ok(Map.of(
                "success", savedCount > 0,
                "message", savedCount > 0 ? "약국 데이터를 성공적으로 저장했습니다." : "저장할 데이터가 없습니다.",
                "savedCount", savedCount
        ));
    }

    private List<JsonNode> fetchAllPharmacyData() {
        List<JsonNode> allData = new ArrayList<>();
        int page = 1;

        while (true) {
            JsonNode items = fetchApiData("&Q0=11&pageNo=" + page + "&numOfRows=50");
            if (items == null || !items.isArray() || items.size() == 0) break;

            for (JsonNode item : items) {
                allData.add(item);
            }

            System.out.println(" 페이지 " + page + "에서 받아온 데이터 수: " + items.size());
            page++;
        }

        return allData;
    }


    private JsonNode fetchApiData(String queryParam) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new org.springframework.http.converter.StringHttpMessageConverter(StandardCharsets.UTF_8));

        try {
            String url = API_URL + "?serviceKey=" + API_KEY + queryParam;
            System.out.println(" 요청 URL: " + url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String xml = response.getBody();

            if (xml == null || xml.contains("SERVICE ERROR")) {
                System.out.println(" API 오류: SERVICE ERROR");
                return null;
            }

            XmlMapper xmlMapper = new XmlMapper();
            JsonNode root = xmlMapper.readTree(xml);

            System.out.println(" 변환된 JSON:\n" + root.toPrettyString());

            //   response > body > items > item , item 까지 바로 접근
            JsonNode items = root.path("body").path("items").path("item");

            if (items.isMissingNode()) {
                System.out.println(" items.item 노드 없음");
                return null;
            }

            return items;

        } catch (Exception e) {
            System.out.println("XML 파싱 또는 API 오류: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    //연중무휴
    public List<Pharmacy> getAlwaysOpenPharmacies() {
        return pharmacyRepository.findAlwaysOpenPharmacies();
    }

    //약국 서치
    public List<Pharmacy> searchPharmacies(String keyword) {
        return pharmacyRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(keyword, keyword);
    }

    //심야 약국
    public List<Pharmacy> getNightPharmacies() {
        return pharmacyRepository.findNightPharmacies();
    }

    //지금 운영
    public List<Pharmacy> getOpenPharmaciesByNow() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        DayOfWeek currentDay = nowSeoul.getDayOfWeek(); // MONDAY ~ SUNDAY
        LocalTime currentTime = nowSeoul.toLocalTime();

        return pharmacyRepository.findAll().stream()
                .filter(pharmacy -> {
                    String open = null;
                    String close = null;

                    switch (currentDay) {
                        case MONDAY -> {
                            open = pharmacy.getMonOpen();
                            close = pharmacy.getMonClose();
                        }
                        case TUESDAY -> {
                            open = pharmacy.getTueOpen();
                            close = pharmacy.getTueClose();
                        }
                        case WEDNESDAY -> {
                            open = pharmacy.getWedOpen();
                            close = pharmacy.getWedClose();
                        }
                        case THURSDAY -> {
                            open = pharmacy.getThuOpen();
                            close = pharmacy.getThuClose();
                        }
                        case FRIDAY -> {
                            open = pharmacy.getFriOpen();
                            close = pharmacy.getFriClose();
                        }
                        case SATURDAY -> {
                            open = pharmacy.getSatOpen();
                            close = pharmacy.getSatClose();
                        }
                        case SUNDAY -> {
                            open = pharmacy.getSunOpen();
                            close = pharmacy.getSunClose();
                        }
                    }

                    if (open == null || close == null) return false;

                    try {
                        LocalTime openTime = LocalTime.parse(open);
                        LocalTime closeTime = LocalTime.parse(close);
                        return !currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();
    }

    @Transactional
    public int savePharmacyDataToDB(List<JsonNode> dataList) {
        List<Pharmacy> pharmacyList = new ArrayList<>();

        for (JsonNode item : dataList) {
            String name = item.path("dutyName").asText("").trim();
            String address = item.path("dutyAddr").asText("").trim();

            if (name.isEmpty() || address.isEmpty()) {
                System.out.println("⚠ 저장 제외 - 이름 또는 주소 누락: " + item);
                continue;
            }

            Pharmacy pharmacy = new Pharmacy();
            pharmacy.setName(name);
            pharmacy.setAddress(address);
            pharmacy.setPhone(item.path("dutyTel1").asText("").trim());
            pharmacy.setLatitude(item.path("wgs84Lat").asDouble(0.0));
            pharmacy.setLongitude(item.path("wgs84Lon").asDouble(0.0));

            pharmacy.setMonOpen(toTime(item.path("dutyTime1s").asText()));
            pharmacy.setMonClose(toTime(item.path("dutyTime1c").asText()));
            pharmacy.setTueOpen(toTime(item.path("dutyTime2s").asText()));
            pharmacy.setTueClose(toTime(item.path("dutyTime2c").asText()));
            pharmacy.setWedOpen(toTime(item.path("dutyTime3s").asText()));
            pharmacy.setWedClose(toTime(item.path("dutyTime3c").asText()));
            pharmacy.setThuOpen(toTime(item.path("dutyTime4s").asText()));
            pharmacy.setThuClose(toTime(item.path("dutyTime4c").asText()));
            pharmacy.setFriOpen(toTime(item.path("dutyTime5s").asText()));
            pharmacy.setFriClose(toTime(item.path("dutyTime5c").asText()));
            pharmacy.setSatOpen(toTime(item.path("dutyTime6s").asText()));
            pharmacy.setSatClose(toTime(item.path("dutyTime6c").asText()));
            pharmacy.setSunOpen(toTime(item.path("dutyTime7s").asText()));
            pharmacy.setSunClose(toTime(item.path("dutyTime7c").asText()));
            pharmacy.setHolOpen(toTime(item.path("dutyTime8s").asText()));
            pharmacy.setHolClose(toTime(item.path("dutyTime8c").asText()));

            pharmacyList.add(pharmacy);
        }

        if (!pharmacyList.isEmpty()) {
            pharmacyRepository.saveAll(pharmacyList);
            pharmacyRepository.flush();
            System.out.println("최종 DB 저장 수: " + pharmacyList.size());
        } else {
            System.out.println("저장할 데이터 없음");
        }

        return pharmacyList.size();
    }

    private String toTime(String rawTime) {
        if (rawTime == null) return null;
        String digits = rawTime.replaceAll("[^0-9]", "");
        if (digits.length() < 4) return null;
        return digits.substring(0, 2) + ":" + digits.substring(2, 4);
    }
}

