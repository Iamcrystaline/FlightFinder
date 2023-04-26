package com.app.main;

import com.app.main.aerodatabox.AeroDataBoxClient;
import com.app.main.aerodatabox.AeroDataBoxCredentials;
import com.google.gson.Gson;
import feign.Feign;
import feign.gson.GsonDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@RequiredArgsConstructor
@ConfigurationPropertiesScan
public class Config {

    private final AeroDataBoxCredentials aeroDataBoxCredentials;

    @Bean
    Clock getClock() {
        return Clock.systemUTC();
    }

    @Bean
    Gson getGson() {
        return new Gson();
    }

    @Bean
    AeroDataBoxClient getAeroDataBoxClient() {
        return Feign.builder()
                .decoder(new GsonDecoder())
                // #TODO: do i need timeout?
                // .options(new Request.Options(5, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, true))
                .requestInterceptor(new MyRequestInterceptor())
                .target(AeroDataBoxClient.class, aeroDataBoxCredentials.getUrl());
    }
}
