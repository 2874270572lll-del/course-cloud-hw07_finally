package com.zjgsu.coursecloud.enrollment.client;

import com.zjgsu.coursecloud.enrollment.dto.CourseDto;
import com.zjgsu.coursecloud.enrollment.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CatalogClientFallback implements CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogClientFallback.class);

    @Override
    public CourseDto getCourseById(String id) {
        log.warn("CatalogClient fallback triggered for course ID: {}", id);
        throw new ServiceUnavailableException("课程服务暂时不可用，请稍后再试");
    }

    @Override
    public CourseDto getCourseByCode(String code) {
        log.warn("CatalogClient fallback triggered for course code: {}", code);
        throw new ServiceUnavailableException("课程服务暂时不可用，请稍后再试");
    }

    @Override
    public Boolean hasCapacity(String id) {
        log.warn("CatalogClient fallback triggered for capacity check: {}", id);
        throw new ServiceUnavailableException("课程服务暂时不可用，请稍后再试");
    }

    @Override
    public Map<String, Object> test() {
        log.warn("🔥 Catalog service fallback triggered for test endpoint");
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Catalog service is temporarily unavailable");
        fallback.put("fallback", true);
        fallback.put("status", "CIRCUIT_OPEN");
        fallback.put("timestamp", LocalDateTime.now().toString());
        return fallback;
    }
}
