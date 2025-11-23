package com.Gym_PT.imageupload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner; // Presigner 임포트

@Configuration
public class S3Config {

    // 2번에서 변경한 'aws.s3...' 키를 읽어옵니다.
    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    // 공용으로 사용할 자격 증명(Credentials)과 리전(Region)을 Bean으로 분리
    @Bean
    public StaticCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    @Bean
    public Region awsRegion() {
        return Region.of(region);
    }

    // S3Client Bean
    @Bean
    public S3Client s3Client(Region awsRegion, StaticCredentialsProvider awsCredentialsProvider) {
        return S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    // S3Presigner Bean (추가)
    @Bean
    public S3Presigner s3Presigner(Region awsRegion, StaticCredentialsProvider awsCredentialsProvider) {
        return S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }
}