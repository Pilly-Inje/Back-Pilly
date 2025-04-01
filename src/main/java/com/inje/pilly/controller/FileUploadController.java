package com.inje.pilly.controller;

import com.inje.pilly.dto.PrescriptionImgResponseDTO;
import com.inje.pilly.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileUploadController {


    private FileUploadService fileUploadService;

    @Autowired
    public FileUploadController(FileUploadService fileUploadService){
        this.fileUploadService = fileUploadService;
    }

    // 이미지 업로드
    @Operation(summary = "처방전 사진 업로드")
    @PostMapping("/upload")
    public ResponseEntity<PrescriptionImgResponseDTO> createPrescription(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) throws IOException {
        PrescriptionImgResponseDTO response = fileUploadService.createPrescription(userId, file);
        return ResponseEntity.ok(response);
    }

    // 처방전 이미지 URL 조회
    @Operation(summary = "사용 x")
    @GetMapping("/prescription/{prescription_id}")
    public ResponseEntity<String> getPrescriptionFileUrl(@PathVariable("prescriptionId") Long prescriptionId) {
        String fileUrl = fileUploadService.getPrescriptionFileUrl(prescriptionId);
        return ResponseEntity.ok(fileUrl); // JSON으로 반환
    }
}
