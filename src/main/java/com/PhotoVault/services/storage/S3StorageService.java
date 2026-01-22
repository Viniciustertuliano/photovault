package com.PhotoVault.services.storage;

import com.PhotoVault.exception.FileStorageException;
import com.PhotoVault.services.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);


    private final S3Client s3Client;

    private final String bucketName;

    public S3StorageService(S3Client s3Client, @Value("${AWS_S3_BUCKET_NAME}")String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            logger.info("S3 bucket '{}' exists", bucketName);
        }catch (NoSuchBucketException e) {
            logger.warn("S3 bucket '{}' NOT exists. Creating. . .", bucketName);
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            logger.info("S3 Bucket '{}' created successfully", bucketName);
        }
    }

    @Override
    public String store(MultipartFile file, String fileName) throws IOException {
        return store(file.getInputStream(), fileName, file.getSize(), file.getContentType());
    }

    @Override
    public String store(InputStream inputStream, String fileName, long size, String contentType) throws IOException {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .contentLength(size)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, size));
            logger.info("File '{}' uploaded to S3 Bucket '{}'", fileName, bucketName);
            return fileName;
        }catch (S3Exception e ){
            logger.error("Failed to upload file to S3: {}", e.getMessage());
            throw new FileStorageException("Clould not store file in S3: " + fileName, e);
        }
    }

    @Override
    public Resource load(String filePath) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            InputStream inputStream = s3Client.getObject(getObjectRequest);
            return new InputStreamResource(inputStream);
        } catch (NoSuchKeyException e) {
            throw new FileStorageException("File not found in S3: " + filePath, e);
        }catch (S3Exception e ){
            throw new FileStorageException("Could not load file from S3: " + filePath, e);
        }
    }

    @Override
    public void delete(String filePath) throws IOException {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            logger.info("File '{}' deleted from S3", filePath);
        }catch (S3Exception e ){
            logger.error("Failed to delete file from S3: {}", e.getMessage());
            throw new FileStorageException("Could not delete file from S3: " + filePath, e);
        }

    }

    @Override
    public boolean exists(String filePath) throws IOException {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        }catch (NoSuchKeyException e){
            return false;
        }
    }

    @Override
    public String getPublicUrl(String filePath) {
        return  String.format("https://%s.s3.amazonaws.com/%s", bucketName, filePath);
    }

    @Override
    public String generatePresignedUrl(String filePath, long expirationMinutes) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            S3Presigner presigner = S3Presigner.create();
            String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();
            presigner.close();
            return presignedUrl;

        }catch (S3Exception e ){
            throw new FileStorageException("Could not generate presigned URL: " + filePath, e);
        }
    }
}
