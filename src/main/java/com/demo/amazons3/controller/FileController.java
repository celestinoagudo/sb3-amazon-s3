package com.demo.amazons3.controller;

import com.demo.amazons3.dto.S3FileDataTransferObject;
import com.demo.amazons3.services.FileManagerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("s3-demo")
@AllArgsConstructor
public class FileController {

    private final FileManagerService fileManagerService;

    @PostMapping("/file/upload")
    public ResponseEntity<String> uploadFile(@RequestBody S3FileDataTransferObject fileToUpload) {
        fileManagerService.uploadFileToBucket(fileToUpload);
        return ResponseEntity.of(Optional.of("File Uploaded Successfully!"));
    }

    @DeleteMapping("file/remove/{name:.+}")
    public ResponseEntity<String> deleteFile(@PathVariable("name") final String name) {
        fileManagerService.deleteFileFromBucket(name);
        return ResponseEntity.of(Optional.of("File Deleted Successfully!"));
    }

    @GetMapping("file/{name:.+}")
    public ResponseEntity<String> getFile(@PathVariable("name") final String name) {
        return ResponseEntity.of(Optional.of(fileManagerService.getFileFromBucket(name)));
    }
}
