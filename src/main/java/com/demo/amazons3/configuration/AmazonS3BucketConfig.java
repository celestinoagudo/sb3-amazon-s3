package com.demo.amazons3.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "amazons3.bucket")
public class AmazonS3BucketConfig {

    private String name;
    private String accessKey;
    private String secretKey;
}
