package com.inje.pilly.service;

import com.google.cloud.storage.*;
import com.inje.pilly.dto.PrescriptionImgResponseDTO;
import com.inje.pilly.entity.Prescription;
import com.inje.pilly.entity.User;
import com.inje.pilly.repository.PrescriptionRepository;
import com.inje.pilly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class FileUploadService {

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final OcrService ocrService;

    public FileUploadService(PrescriptionRepository prescriptionRepository,UserRepository userRepository, @Lazy OcrService ocrService){
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
        this.ocrService = ocrService;
    }

    // GCS에 파일 업로드
    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // GCS에 바로 파일 업로드
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).setContentType(file.getContentType()).build();
        Blob blob = storage.create(blobInfo, file.getBytes());  // GCS에 파일을 바로 업로드

        System.out.println("gcs 링크: "+blob.getMediaLink());
        // GCS URL 반환
        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    }

    public PrescriptionImgResponseDTO createPrescription(Long userId, MultipartFile  file) throws IOException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        String fileUrl = uploadFile(file);
        // 새로운 처방전을 생성하고 userId와 fileUrl을 설정합니다.
        Prescription prescription = new Prescription();
        prescription.setUser(user);
        prescription.setFile(fileUrl);
        Prescription savedPrescription = prescriptionRepository.save(prescription);

        List<String> extractedText = ocrService.extractTextFromImage(fileUrl);
        // 처방전 생성 시 필요한 다른 필드들도 설정 가능
        return new PrescriptionImgResponseDTO(userId, savedPrescription.getPrescriptionId(), "사진 업로드 성공", extractedText);
    }

    // 처방전의 파일 URL 조회
    public String getPrescriptionFileUrl(Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        return prescription.getFile();
    }
}
