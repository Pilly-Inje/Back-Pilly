package com.inje.pilly.controller;

import com.inje.pilly.entity.Pharmacy;
import com.inje.pilly.service.PharmacyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pharmacy")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    public PharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    // 데이터 저장
    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> fetchAndSavePharmacies() {
        return pharmacyService.fetchAndSavePharmacies();
    }

    // 키워드 기반 검색
    @GetMapping("/search")
    public ResponseEntity<List<Pharmacy>> searchPharmacies(@RequestParam String keyword) {
        return ResponseEntity.ok(pharmacyService.searchPharmacies(keyword));
    }

    //심야약국
    @GetMapping("/night")
    public ResponseEntity<List<Pharmacy>> getNightPharmacies() {
        return ResponseEntity.ok(pharmacyService.getNightPharmacies());
    }

    //연중무휴
    @GetMapping("/alwaysopen")
    public ResponseEntity<List<Pharmacy>> getAlwaysOpenPharmacies() {
        return ResponseEntity.ok(pharmacyService.getAlwaysOpenPharmacies());
    }
    @GetMapping("/now")
    public ResponseEntity<List<Pharmacy>> getCurrentlyOpenPharmacies() {
        return ResponseEntity.ok(pharmacyService.getOpenPharmaciesByNow());
    }
}
