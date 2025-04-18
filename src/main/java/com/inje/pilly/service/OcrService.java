package com.inje.pilly.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.inje.pilly.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class OcrService {
    private final MedicineRepository medicineRepository;

    public OcrService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    // GCS URL로 OCR 텍스트 추출
    public List<String> extractTextFromImage(String gcsImageUrl) throws IOException{
        System.out.println("Received fileUrl: "+gcsImageUrl);
        String gcsUri = convertToGcsUri(gcsImageUrl);

        ClassPathResource resource = new ClassPathResource("ocr-project.json");

        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        try(ImageAnnotatorClient vision = ImageAnnotatorClient.create(settings)){
            ImageSource imageSource = ImageSource.newBuilder().setGcsImageUri(gcsUri).build();
            Image image = Image.newBuilder().setSource(imageSource).build();

            Feature feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image)
                    .build();

            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));
            List<AnnotateImageResponse> responses = response.getResponsesList();

            if (responses.isEmpty()) {
                throw new RuntimeException("OCR 요청에 대한 응답이 없습니다.");
            }

            StringBuilder extractedText = new StringBuilder();
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    throw new RuntimeException("OCR 처리 중 오류가 발생했습니다: " + res.getError().getMessage());
                }
                // 추출된 텍스트
                extractedText.append(res.getTextAnnotationsList().get(0).getDescription());
            }
            System.out.println(extractedText);
            return filterMedicineNames(extractedText.toString());
        }
    }
    // GCS URL로 변환시킴
    private String convertToGcsUri(String gcsImageUrl) {
        // 로그 추가
        System.out.println("Received GCS URL: " + gcsImageUrl);

        // URL이 null이거나 비어 있는지 확인
        if (gcsImageUrl == null || gcsImageUrl.isEmpty()) {
            throw new IllegalArgumentException("Invalid GCS URL: URL is null or empty");
        }

        String bucketName;
        String filePath;

        if (gcsImageUrl.contains("/o/")) {
            // 다운로드 URL 형식 처리 (https://storage.googleapis.com/download/storage/v1/b/{버킷명}/o/{파일명}?alt=media)
            String[] urlParts = gcsImageUrl.split("/o/");
            if (urlParts.length < 2) {
                throw new IllegalArgumentException("Invalid GCS URL format: " + gcsImageUrl);
            }

            bucketName = urlParts[0].split("/b/")[1];
            filePath = urlParts[1].split("\\?")[0]; // 쿼리 문자열 제거

        } else if (gcsImageUrl.startsWith("https://storage.googleapis.com/")) {
            // Public URL 형식 처리 (https://storage.googleapis.com/{버킷명}/{파일명})
            String[] urlParts = gcsImageUrl.replace("https://storage.googleapis.com/", "").split("/", 2);
            if (urlParts.length < 2) {
                throw new IllegalArgumentException("Invalid GCS URL format: " + gcsImageUrl);
            }

            bucketName = urlParts[0]; // 첫 번째 부분이 버킷 이름
            filePath = urlParts[1];   // 두 번째 부분이 파일 경로
        } else {
            throw new IllegalArgumentException("Unknown GCS URL format: " + gcsImageUrl);
        }

        // gs:// 형식으로 변환
        String gcsUri = "gs://" + bucketName + "/" + filePath;

        // 변환된 GCS URI 로그 추가
        System.out.println("Converted GCS URI: " + gcsUri);

        return gcsUri;
    }


    //문자열 유사도 계산
    private double calculateSimilarity(String ocrName, String dbName) {
        int distance = getLevenshteinDistance(ocrName, dbName);
        int maxLength = Math.max(ocrName.length(), dbName.length());
        return 1.0 - (double) distance / maxLength; // 유사도 계산
    }
    private int getLevenshteinDistance(String s1, String s2) {
        int lenS1 = s1.length();
        int lenS2 = s2.length();
        int[][] dp = new int[lenS1 + 1][lenS2 + 1];

        // 초기화
        for (int i = 0; i <= lenS1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= lenS2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= lenS1; i++) {
            for (int j = 1; j <= lenS2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }

        return dp[lenS1][lenS2];
    }

    // 문자열의 유사도를 계산하여 80% 이상 일치하는 경우에만 필터링
    private boolean isSimilar(String ocrName, String dbName) {
        // 숫자와 공백을 모두 제거하고 비교
        String ocrNameClean = ocrName.replaceAll("[\\s0-9]", "");
        String dbNameClean = dbName.replaceAll("[\\s0-9]", "");
        // 숫자를 제외한 이름만 비교한 후 유사도를 계산
        double similarity = calculateSimilarity(ocrNameClean, dbNameClean);

        return similarity >= 0.8; // 유사도 80% 이상일 경우 true 반환
    }

    // OCR 텍스트에서 약 이름만 추출하는 메서드 + medicine db랑 비교
    private List<String> filterMedicineNames(String text) {
        String[] lines = text.split("\n");
        Set<String> extractedNames = new HashSet<>();
        System.out.print("mg: ");
        //mg 먼저 ㄱ
        Pattern mgPattern = Pattern.compile("[가-힣]+(?:[\\s\\-][A-Za-z0-9가-힣]+)*(100|200|300|...)mg");
        for (String line : lines) {
            Matcher matcher = mgPattern.matcher(line);
            while (matcher.find()) {
                String match = matcher.group().trim();
                extractedNames.add(match);
                System.out.print(match+", ");
            }
        }
        System.out.println("\n정,슐,액: ");
        //"정", "슐", "액"으로 끝나는 단어만
        Pattern otherPattern  = Pattern.compile("[가-힣]+(?:[\\s\\-][A-Za-z0-9가-힣]+)*(정|액|슐)");

        for (String line : lines) {
            Matcher matcher = otherPattern .matcher(line);
            while (matcher.find()) {
                String match = matcher.group().trim();
                extractedNames.add(match);
                System.out.println(match+", ");
            }
        }

        // DB에서 실제 존재하는 약품명 가져오기
        List<String> medicineNames = medicineRepository.findAllMedicineNames();

        System.out.println("db : "+ medicineNames);
        // OCR로 추출된 약품명과 DB 비교하여 유사도 80% 이상인 것만 필터링
        List<String> filteredNames = extractedNames.stream()
                .filter(ocrName -> medicineNames.stream()
                        .anyMatch(dbName -> isSimilar(ocrName, dbName))) // 유사도가 80% 이상인 경우만 필터링
                .collect(Collectors.toList());

        // DB에서 정확한 약품명으로 대체하기
        List<String> exactMatchedNames = filteredNames.stream()
                .map(ocrName -> medicineNames.stream()
                        .filter(dbName -> isSimilar(ocrName, dbName)) // 유사도가 80% 이상인 DB 이름을 매칭
                        .findFirst() // 첫 번째로 일치하는 약품명을 선택
                        .orElse(null))
                .distinct()  // 중복 제거
                .collect(Collectors.toList());

        System.out.println("exactMatchedNames: " + exactMatchedNames);

        return exactMatchedNames;  // 정확한 약품명 리스트 반환
    }
}
