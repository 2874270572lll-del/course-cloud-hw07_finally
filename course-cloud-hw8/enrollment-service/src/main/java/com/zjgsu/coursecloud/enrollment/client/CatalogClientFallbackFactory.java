package com.zjgsu.coursecloud.enrollment.client;

import com.zjgsu.coursecloud.enrollment.dto.CourseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CatalogClientFallbackFactory implements FallbackFactory<CatalogClient> {
    
    private static final Logger log = LoggerFactory.getLogger(CatalogClientFallbackFactory.class);

    @Override
    public CatalogClient create(Throwable cause) {
        return new CatalogClient() {
            @Override
            public CourseDto getCourseById(String id) {
                log.warn("🔥 CatalogClient fallback triggered for course ID: {}, reason: {}", 
                    id, cause.getMessage());
                throw new RuntimeException("课程服务暂时不可用，请稍后再试");
            }

            @Override
            public CourseDto getCourseByCode(String code) {
                log.warn("🔥 CatalogClient fallback triggered for course code: {}, reason: {}", 
                    code, cause.getMessage());
                throw new RuntimeException("课程服务暂时不可用，请稍后再试");
            }

            @Override
            public Boolean hasCapacity(String id) {
                log.warn("🔥 CatalogClient fallback triggered for capacity check: {}, reason: {}", 
                    id, cause.getMessage());
                throw new RuntimeException("课程服务暂时不可用，请稍后再试");
            }

            @Override
            public Map<String, Object> test() {
                log.warn("🔥 Catalog service fallback triggered for test endpoint, reason: {}", 
                    cause.getMessage());
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("error", "Catalog service is temporarily unavailable");
                fallback.put("fallback", true);
                fallback.put("status", "CIRCUIT_OPEN");
                fallback.put("timestamp", LocalDateTime.now().toString());
                fallback.put("reason", cause.getMessage());
                return fallback;
            }
        };
    }
}
