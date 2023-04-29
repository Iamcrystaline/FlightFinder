package com.app.main.api.aerodatabox;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@AllArgsConstructor
@ConfigurationProperties(prefix = "aerodatabox.api")
public class AeroDataBoxCredentials {

    @NotBlank
    private final String key;
    @NotBlank
    private final String host;
    private final String url;
}
