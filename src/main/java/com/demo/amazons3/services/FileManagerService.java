package com.demo.amazons3.services;

import com.demo.amazons3.client.AmazonS3Client;
import com.demo.amazons3.dto.S3FileDataTransferObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
@AllArgsConstructor
public class FileManagerService {

    private final AmazonS3Client amazonS3Client;

    public void uploadFileToBucket(final S3FileDataTransferObject fileToUpload) {
        var decodedContent = Base64.getDecoder()
                .decode(fileToUpload.getBase64EncodedContent().getBytes(StandardCharsets.UTF_8));
        var file = new File(fileToUpload.getName());
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(decodedContent);
        } catch (final FileNotFoundException exception) {
            log.error("File Not Found Exception is encountered while uploading {}", fileToUpload.getName(), exception);
        } catch (final IOException exception) {
            log.error("IO Exception is encountered while uploading {}", fileToUpload.getName(), exception);
        }
        amazonS3Client.uploadFileToBucket(fileToUpload.getName(), file);
    }

    public void deleteFileFromBucket(final String name) {
        amazonS3Client.deleteFileFromBucket(name);
    }

    public String getFileFromBucket(final String name) {
        return amazonS3Client.getFileAsBase64FromBucket(name);
    }
}
