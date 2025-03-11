package com.inje.pilly.controller;

import com.inje.pilly.dto.PrescriptionImgResponseDTO;
import com.inje.pilly.entity.Prescription;
import com.inje.pilly.service.FileUploadService;
import com.inje.pilly.service.OcrService;
import com.inje.pilly.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    // 이미지 업로드
    @PostMapping("/upload")
    public ResponseEntity<PrescriptionImgResponseDTO> createPrescription(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) throws IOException {
        PrescriptionImgResponseDTO response = fileUploadService.createPrescription(userId, file);
        return ResponseEntity.ok(response);
    }

    // 처방전 이미지 URL 조회
    @GetMapping("/prescription/{prescription_id}")
    public ResponseEntity<String> getPrescriptionFileUrl(@PathVariable("prescriptionId") Long prescriptionId) {
        String fileUrl = fileUploadService.getPrescriptionFileUrl(prescriptionId);
        return ResponseEntity.ok(fileUrl); // JSON으로 반환
    }
}
