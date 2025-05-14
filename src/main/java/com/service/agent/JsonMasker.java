package com.service.agent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class JsonMasker {
    private static final Logger log = LoggerFactory.getLogger(JsonMasker.class);

    @Value("${response.masked.fields:}")
    private String FIELDS;
    private Pattern FIELD_PATTERN;

    @PostConstruct
    public void init() {
        if (FIELDS == null || FIELDS.isBlank()) {
            FIELD_PATTERN = null; 
            log.warn("No fields configured for masking. Skipping masking.");
            return;
        }
        log.info("Masked fields property: " + FIELDS);
        FIELD_PATTERN = Pattern.compile(
                "\"(" + FIELDS.replaceAll(",", "|") + ")\"\\s*:\\s*(\".*?\"|\\d+(\\.\\d+)?|true|false|null)");
    }

    public String maskFields(String json, String maskChar) {
        if (FIELD_PATTERN == null) {
            return json;
        }
        Matcher matcher = FIELD_PATTERN.matcher(json);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(json, lastEnd, matcher.start(2));
            String originalValue = matcher.group(2);
            boolean quoted = originalValue.startsWith("\"");
            int length = quoted ? originalValue.length() - 2 : originalValue.length();
            String masked = maskChar.repeat(length);
            result.append("\"" + masked + "\"");
            lastEnd = matcher.end(2);
        }
        result.append(json.substring(lastEnd));
        log.info("Masked Response: "+result);
        return result.toString();
    }
}