package com.app.main.api;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class MyRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.uri(requestTemplate.path().replaceAll("%3A", ":"));
    }
}
