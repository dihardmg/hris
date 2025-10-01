package hris.hris.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@Slf4j
public class FaceRecognitionService {

    private static final double SIMILARITY_THRESHOLD = 0.7;

    public double verifyFace(String faceImageBase64, byte[] storedFaceTemplate) {
        try {
            byte[] faceImageData = Base64.getDecoder().decode(faceImageBase64);

            double similarity = calculateSimilarity(faceImageData, storedFaceTemplate);

            log.debug("Face verification similarity score: {}", similarity);

            return similarity;
        } catch (Exception e) {
            log.error("Face verification failed", e);
            throw new RuntimeException("Face verification processing error");
        }
    }

    public byte[] generateFaceTemplate(String faceImageBase64) {
        try {
            byte[] faceImageData = Base64.getDecoder().decode(faceImageBase64);

            return generateTemplate(faceImageData);
        } catch (Exception e) {
            log.error("Face template generation failed", e);
            throw new RuntimeException("Face template generation error");
        }
    }

    private double calculateSimilarity(byte[] faceImage1, byte[] faceImage2) {
        if (faceImage1 == null || faceImage2 == null) {
            return 0.0;
        }

        double[] features1 = extractFaceFeatures(faceImage1);
        double[] features2 = extractFaceFeatures(faceImage2);

        if (features1 == null || features2 == null || features1.length != features2.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < features1.length; i++) {
            dotProduct += features1[i] * features2[i];
            norm1 += features1[i] * features1[i];
            norm2 += features2[i] * features2[i];
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private double[] extractFaceFeatures(byte[] imageData) {
        double[] mockFeatures = new double[128];

        for (int i = 0; i < mockFeatures.length; i++) {
            mockFeatures[i] = Math.random();
        }

        return mockFeatures;
    }

    private byte[] generateTemplate(byte[] imageData) {
        double[] features = extractFaceFeatures(imageData);

        byte[] template = new byte[features.length * 8];
        for (int i = 0; i < features.length; i++) {
            long bits = Double.doubleToLongBits(features[i]);
            for (int j = 0; j < 8; j++) {
                template[i * 8 + j] = (byte) ((bits >> (j * 8)) & 0xff);
            }
        }

        return template;
    }

    public boolean isFaceImageValid(String faceImageBase64) {
        if (faceImageBase64 == null || faceImageBase64.isEmpty()) {
            return false;
        }

        try {
            if (faceImageBase64.startsWith("data:image/")) {
                faceImageBase64 = faceImageBase64.split(",")[1];
            }

            byte[] imageData = Base64.getDecoder().decode(faceImageBase64);
            return imageData.length > 1000;
        } catch (Exception e) {
            log.error("Invalid face image format", e);
            return false;
        }
    }
}