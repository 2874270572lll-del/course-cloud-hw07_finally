package com.zjgsu.coursecloud.enrollment.controller;

import com.zjgsu.coursecloud.enrollment.client.CatalogClient;
import com.zjgsu.coursecloud.enrollment.client.UserClient;
import com.zjgsu.coursecloud.enrollment.model.EnrollmentRecord;
import com.zjgsu.coursecloud.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentController.class);

    private final EnrollmentService enrollmentService;
    private final UserClient userClient;
    private final CatalogClient catalogClient;

    @Value("${server.port}")
    private String currentPort;

    public EnrollmentController(
            EnrollmentService enrollmentService,
            UserClient userClient,
            CatalogClient catalogClient) {
        this.enrollmentService = enrollmentService;
        this.userClient = userClient;
        this.catalogClient = catalogClient;
    }

    // ==================== Enrollment Endpoints ====================

    /**
     * ⭐ HW09: 添加用户信息获取（从 Gateway 传递的请求头）
     */
    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(
            @Valid @RequestBody EnrollmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        // ⭐ 记录来自 Gateway 的用户信息
        if (userId != null && username != null) {
            log.info("【实例 {}】收到选课请求 - 用户: {} (ID: {}, Role: {})",
                    currentPort, username, userId, role);
        } else {
            log.info("【实例 {}】收到选课请求（未认证）", currentPort);
        }

        EnrollmentRecord record = enrollmentService.enroll(request.courseId(), request.studentId());

        log.info("【实例 {}】选课成功 - 课程: {}, 学生: {}",
                currentPort, request.courseId(), request.studentId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new EnrollmentResponse(
                        record.getId(),
                        record.getCourseId(),
                        record.getStudentId(),
                        record.getEnrolledAt().toString()
                ));
    }

    /**
     * ⭐ HW09: 添加用户信息日志
     */
    @GetMapping("/course/{courseId}")
    public List<EnrollmentResponse> listByCourse(
            @PathVariable String courseId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Username", required = false) String username) {

        log.info("【实例 {}】查询课程选课列表 - 课程: {}, 用户: {} (ID: {})",
                currentPort, courseId, username, userId);

        return enrollmentService.listByCourse(courseId)
                .stream()
                .map(record -> new EnrollmentResponse(
                        record.getId(),
                        record.getCourseId(),
                        record.getStudentId(),
                        record.getEnrolledAt().toString()
                ))
                .toList();
    }

    /**
     * ⭐ HW09: 添加用户信息日志
     */
    @GetMapping("/student/{studentId}")
    public List<EnrollmentResponse> listByStudent(
            @PathVariable String studentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Username", required = false) String username) {

        log.info("【实例 {}】查询学生选课列表 - 学生: {}, 用户: {} (ID: {})",
                currentPort, studentId, username, userId);

        return enrollmentService.listByStudent(studentId)
                .stream()
                .map(record -> new EnrollmentResponse(
                        record.getId(),
                        record.getCourseId(),
                        record.getStudentId(),
                        record.getEnrolledAt().toString()
                ))
                .toList();
    }

    /**
     * ⭐ HW09: 添加用户信息日志
     */
    @GetMapping
    public List<EnrollmentResponse> listAll(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Username", required = false) String username) {

        log.info("【实例 {}】查询所有选课列表 - 用户: {} (ID: {})",
                currentPort, username, userId);

        return enrollmentService.listAll()
                .stream()
                .map(record -> new EnrollmentResponse(
                        record.getId(),
                        record.getCourseId(),
                        record.getStudentId(),
                        record.getEnrolledAt().toString()
                ))
                .toList();
    }

    // ==================== 测试接口（熔断验证）====================
    /**
     * ⭐ 保留原有的测试接口
     */
    @GetMapping("/test")
    public Map<String, Object> test(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Username", required = false) String username) {

        Map<String, Object> response = new HashMap<>();
        response.put("service", "enrollment-service");
        response.put("port", currentPort);
        response.put("hostname", getHostname());

        // ⭐ HW09: 添加用户信息到响应
        if (userId != null && username != null) {
            response.put("authenticated_user", Map.of(
                    "userId", userId,
                    "username", username
            ));
            log.info("【实例 {}】测试接口被调用 - 用户: {} (ID: {})",
                    currentPort, username, userId);
        } else {
            response.put("authenticated_user", "未认证");
            log.info("【实例 {}】测试接口被调用（未认证）", currentPort);
        }

        try {
            response.put("ip", InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            response.put("ip", "unknown");
        }

        response.put("timestamp", LocalDateTime.now());

        // 直接调用，不要任何 try-catch
        // Feign 的 fallback 会自动处理异常
        response.put("user-service", userClient.test());
        response.put("catalog-service", catalogClient.test());

        return response;
    }

    // ==================== 简化测试接口（不捕获异常）====================
    /**
     * ⭐ 保留原有的简化测试接口
     * 这个接口不捕获异常，用于测试 Feign fallback 是否真的工作
     */
    @GetMapping("/test-simple")
    public Map<String, Object> testSimple(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Username", required = false) String username) {

        log.info("🧪 【实例 {}】简化测试 - 直接调用 Feign Client（不捕获异常） - 用户: {}",
                currentPort, username);

        Map<String, Object> response = new HashMap<>();
        response.put("service", "enrollment-service");
        response.put("timestamp", LocalDateTime.now());

        // ⭐ HW09: 添加用户信息
        if (userId != null && username != null) {
            response.put("user", Map.of("userId", userId, "username", username));
        }

        // 直接调用，不 catch 异常
        response.put("user-service", userClient.test());
        response.put("catalog-service", catalogClient.test());

        return response;
    }

    // ==================== 辅助方法 ====================
    private String getHostname() {
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && !hostname.isEmpty()) {
            return hostname;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-" + currentPort;
        }
    }

    // ==================== 健康检查接口 ====================
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> healthResponse = new HashMap<>();
        healthResponse.put("status", "UP");
        healthResponse.put("service", "enrollment-service");
        healthResponse.put("port", currentPort);
        healthResponse.put("hostname", getHostname());
        healthResponse.put("timestamp", System.currentTimeMillis());
        return healthResponse;
    }

    // ==================== Record 定义 ====================
    public record EnrollmentRequest(
            @NotBlank String courseId,
            @NotBlank String studentId
    ) {}

    public record EnrollmentResponse(
            String id,
            String courseId,
            String studentId,
            String enrolledAt
    ) {}
}