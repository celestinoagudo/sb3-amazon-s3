package com.demo.amazons3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class S3FileDataTransferObject {

    private String name;
    private String base64EncodedContent;
}
