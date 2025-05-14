package com.service.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.opentelemetry.api.trace.Span;

@ControllerAdvice
public class JsonMaskingAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private JsonMasker mask;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {
        try {
            String json = mask.maskFields(objectMapper.writeValueAsString(body), "*");

            Span span = Span.current();
            if (span != null && span.getSpanContext().isValid()) {
                span.setAttribute("response.body", json);
            }

            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return body;
        }
    }
}
