package com.zjgsu.coursecloud.enrollment.client;

import com.zjgsu.coursecloud.enrollment.dto.StudentDto;
import com.zjgsu.coursecloud.enrollment.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserClientFallback implements UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClientFallback.class);

    @Override
    public StudentDto getStudentById(String id) {
        log.warn("UserClient fallback triggered for student ID: {}", id);
        throw new ServiceUnavailableException("用户服务暂时不可用，请稍后再试");
    }

    @Override
    public StudentDto getStudentByStudentId(String studentId) {
        log.warn("UserClient fallback triggered for student number: {}", studentId);
        throw new ServiceUnavailableException("用户服务暂时不可用，请稍后再试");
    }

    @Override
    public Map<String, Object> test() {
        log.warn("🔥 User service fallback triggered for test endpoint");
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "User service is temporarily unavailable");
        fallback.put("fallback", true);
        fallback.put("status", "CIRCUIT_OPEN");
        fallback.put("timestamp", LocalDateTime.now().toString());
        return fallback;
    }
}
