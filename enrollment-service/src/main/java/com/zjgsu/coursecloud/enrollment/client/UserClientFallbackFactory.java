package com.zjgsu.coursecloud.enrollment.client;

import com.zjgsu.coursecloud.enrollment.dto.StudentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    
    private static final Logger log = LoggerFactory.getLogger(UserClientFallbackFactory.class);

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public StudentDto getStudentById(String id) {
                log.warn("🔥 UserClient fallback triggered for student ID: {}, reason: {}", 
                    id, cause.getMessage());
                throw new RuntimeException("用户服务暂时不可用，请稍后再试");
            }

            @Override
            public StudentDto getStudentByStudentId(String studentId) {
                log.warn("🔥 UserClient fallback triggered for student number: {}, reason: {}", 
                    studentId, cause.getMessage());
                throw new RuntimeException("用户服务暂时不可用，请稍后再试");
            }

            @Override
            public Map<String, Object> test() {
                log.warn("🔥 User service fallback triggered for test endpoint, reason: {}", 
                    cause.getMessage());
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("error", "User service is temporarily unavailable");
                fallback.put("fallback", true);
                fallback.put("status", "CIRCUIT_OPEN");
                fallback.put("timestamp", LocalDateTime.now().toString());
                fallback.put("reason", cause.getMessage());
                return fallback;
            }
        };
    }
}
