package com.demo.amazons3.client;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.demo.amazons3.configuration.AmazonS3BucketConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Slf4j
@Component
public class AmazonS3Client {

    private final AmazonS3BucketConfig amazonS3BucketConfig;
    private AmazonS3 amazonS3;

    public AmazonS3Client(AmazonS3BucketConfig amazonS3BucketConfig) {
        this.amazonS3BucketConfig = amazonS3BucketConfig;
    }

    public void uploadFileToBucket(final String name, final File file) {
        log.info("Uploading file {}", name);
        final var uploadRequest = new PutObjectRequest(amazonS3BucketConfig.getName(),
                name, file);
        try {
            amazonS3.putObject(uploadRequest);
            FileUtils.forceDelete(file);
            log.info("Successfully uploaded object with key {}", name);
        } catch (final SdkClientException sdkClientException) {
            log.error("SDK Exception encountered while uploading file to a bucket {}",
                    sdkClientException.getMessage(), sdkClientException);
        } catch (final IOException exception) {
            log.error("An IO Exception is encountered while deleting file {}", name, exception);
        }
    }

    public void deleteFileFromBucket(final String name) {
        final var deleteRequest = new DeleteObjectRequest(amazonS3BucketConfig.getName(), name);
        try {
            amazonS3.deleteObject(deleteRequest);
        } catch (final SdkClientException sdkClientException) {
            log.error("SDK Exception encountered while deleting file from a bucket: {}",
                    sdkClientException.getMessage(), sdkClientException);
        }
    }

    public String getFileAsBase64FromBucket(final String name) {
        var s3ObjectInputStream = getFileInputStreamFromBucket(name);
        var file = new File(name);
        try {
            FileUtils.copyInputStreamToFile(s3ObjectInputStream, file);
            final var base64Encoded = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file));
            FileUtils.forceDelete(file);
            return base64Encoded;
        } catch (final IOException exception) {
            log.error("An IOException is encountered while writing to File {}", name, exception);
        }
        return null;
    }

    private InputStream getFileInputStreamFromBucket(final String name) {
        var s3Object = amazonS3.getObject(amazonS3BucketConfig.getName(), name);
        return s3Object.getObjectContent();
    }

    @PostConstruct
    private void initializeAmazonS3Client() {
        final var awsCredentials = new BasicAWSCredentials(amazonS3BucketConfig.getAccessKey(),
                amazonS3BucketConfig.getSecretKey());
        amazonS3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.AP_NORTHEAST_1).build();
    }
}
