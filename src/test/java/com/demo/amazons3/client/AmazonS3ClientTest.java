package com.demo.amazons3.client;

import com.demo.amazons3.configuration.AmazonS3BucketConfig;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class AmazonS3ClientTest {

    @MockBean
    private AmazonS3BucketConfig amazonS3BucketConfig;

    @MockBean
    private S3Client s3Client;

    private AmazonS3Client unitUnderTest;

    @BeforeEach
    void setup() throws IOException {
        when(amazonS3BucketConfig.getAccessKey()).thenReturn("access-key");
        when(amazonS3BucketConfig.getSecretKey()).thenReturn("secret-key");
        when(amazonS3BucketConfig.getName()).thenReturn("test-bucket");
        unitUnderTest = new AmazonS3Client(amazonS3BucketConfig);
        ReflectionTestUtils.setField(unitUnderTest, "s3Client", s3Client);
        FileUtils.writeByteArrayToFile(new File("src/test/resources/test-text.txt"),
                "This is just a test".getBytes());
    }

    @Test
    void shouldUploadFileToBucket() {
        unitUnderTest.uploadFileToBucket("test-text.txt",
                new File("src/test/resources/test-text.txt"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void shouldDeleteFileFromBucket() {
        unitUnderTest.deleteFileFromBucket("test-text.txt");
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void shouldGetFileFromBucket() {
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes
                .fromByteArray(mock(GetObjectResponse.class), "test-text.txt".getBytes());
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);
        unitUnderTest.getFileAsBase64FromBucket("test-text.txt");
        verify(s3Client).getObjectAsBytes(any(GetObjectRequest.class));
    }
}