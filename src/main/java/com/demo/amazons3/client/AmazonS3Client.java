package com.demo.amazons3.client;

import com.demo.amazons3.configuration.AmazonS3BucketConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import static org.apache.commons.io.FileUtils.*;
import static software.amazon.awssdk.core.sync.RequestBody.fromFile;

@Slf4j
@Component
public class AmazonS3Client {

    private final AmazonS3BucketConfig amazonS3BucketConfig;
    private S3Client s3Client;

    public AmazonS3Client(final AmazonS3BucketConfig amazonS3BucketConfig) {
        this.amazonS3BucketConfig = amazonS3BucketConfig;
    }

    public void uploadFileToBucket(final String name, final File file) {
        log.info("Uploading file {}", name);
        final var uploadRequest =
                PutObjectRequest.builder()
                        .bucket((amazonS3BucketConfig.getName())).key(name).build();
        try {
            s3Client.putObject(uploadRequest, fromFile(file));
            forceDelete(file);
            log.info("Successfully uploaded object with key {}", name);
        } catch (final SdkClientException sdkClientException) {
            log.error("SDK Exception encountered while uploading file to a bucket {}",
                    sdkClientException.getMessage(), sdkClientException);
        } catch (final IOException exception) {
            log.error("An IO Exception is encountered while deleting file {}", name, exception);
        }
    }

    public void deleteFileFromBucket(final String name) {
        final var deleteRequest = DeleteObjectRequest.builder()
                .bucket(amazonS3BucketConfig.getName()).key(name).build();
        try {
            s3Client.deleteObject(deleteRequest);
        } catch (final SdkClientException sdkClientException) {
            log.error("SDK Exception encountered while deleting file from a bucket: {}",
                    sdkClientException.getMessage(), sdkClientException);
        }
    }

    public String getFileAsBase64FromBucket(final String name) {
        var s3ObjectInputStream = getFileInputStreamFromBucket(name);
        var file = new File(name);
        try {
            copyInputStreamToFile(s3ObjectInputStream, file);
            final var base64Encoded = Base64.getEncoder()
                    .encodeToString(readFileToByteArray(file));
            forceDelete(file);
            return base64Encoded;
        } catch (final IOException exception) {
            log.error("An IOException is encountered while writing to File {}", name, exception);
        }
        return null;
    }

    private InputStream getFileInputStreamFromBucket(final String name) {
        final var retrieveObjectRequest = GetObjectRequest.builder()
                .bucket(amazonS3BucketConfig.getName()).key(name).build();
        var objectAsBytes = s3Client.getObjectAsBytes(retrieveObjectRequest);
        return objectAsBytes.asInputStream();
    }

    @PostConstruct
    private void initializeAmazonS3Client() {
        final var awsBasicCredentials = AwsBasicCredentials.builder()
                .secretAccessKey(amazonS3BucketConfig.getSecretKey())
                .accessKeyId(amazonS3BucketConfig.getAccessKey()).build();
        s3Client = S3Client.builder().credentialsProvider(
                        StaticCredentialsProvider.create(awsBasicCredentials))
                .region(Region.AP_NORTHEAST_1).build();
    }
}
