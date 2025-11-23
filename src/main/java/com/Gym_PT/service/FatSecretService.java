package com.Gym_PT.service;

import com.Gym_PT.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class FatSecretService {

    private final RestTemplate restTemplate;
    private final HttpClient s3DownloadClient;

    private static final String TOKEN_URL = "https://oauth.fatsecret.com/connect/token";
    private static final String IMAGE_API_URL = "https://platform.fatsecret.com/rest/image-recognition/v2";

    @Value("${fatsecret.client-id}")
    private String clientId;
    @Value("${fatsecret.client-secret}")
    private String clientSecret;
    @Value("${fatsecret.scope}")
    private String scope;

    private String cachedToken;
    private Instant tokenExpiryTime;

    public FatSecretService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.s3DownloadClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    // [수정] 리턴 타입을 String(Raw JSON)으로 변경
    public String recognizeImageFromUrl(String imageUrl) throws IOException, InterruptedException {
        String token = getValidAccessToken();
        byte[] imageBytes;

        try {
            HttpRequest downloadRequest = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = s3DownloadClient.send(downloadRequest, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) throw new RuntimeException("S3 Download Error");
            imageBytes = response.body();
        } catch (Exception e) {
            throw new RuntimeException("S3 Error: " + e.getMessage(), e);
        }

        byte[] resizedImageBytes = resizeImage(imageBytes, 512, 512);
        String base64Image = Base64.getEncoder().encodeToString(resizedImageBytes);

        return callFatSecretApi(token, base64Image);
    }

    // [수정] Raw JSON String 반환
    private String callFatSecretApi(String token, String base64Image) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("image_b64", base64Image);
        requestMap.put("include_food_data", true);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                IMAGE_API_URL,
                entity,
                String.class
        );
        return response.getBody();
    }

    // (resizeImage, getValidAccessToken 메서드는 기존과 동일하므로 생략)
    private byte[] resizeImage(byte[] originalImageBytes, int targetWidth, int targetHeight) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(originalImageBytes);
        BufferedImage originalImage = ImageIO.read(bais);
        if (originalImage == null) return originalImageBytes;
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(outputImage, "jpg", baos);
        return baos.toByteArray();
    }

    private String getValidAccessToken() {
        if (cachedToken != null && tokenExpiryTime != null && tokenExpiryTime.isAfter(Instant.now().plusSeconds(60))) {
            return cachedToken;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", scope);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(TOKEN_URL, entity, TokenResponse.class);
        TokenResponse tokenResponse = Objects.requireNonNull(response.getBody());
        this.cachedToken = tokenResponse.accessToken();
        this.tokenExpiryTime = Instant.now().plusSeconds(tokenResponse.expiresIn());
        return this.cachedToken;
    }
}