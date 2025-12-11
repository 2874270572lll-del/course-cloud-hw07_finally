//package com.zjgsu.coursecloud.enrollment.controller;
//
//import com.zjgsu.coursecloud.enrollment.client.CatalogClient;
//import com.zjgsu.coursecloud.enrollment.client.UserClient;
//import com.zjgsu.coursecloud.enrollment.model.EnrollmentRecord;
//import com.zjgsu.coursecloud.enrollment.service.EnrollmentService;
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.NotBlank;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.net.InetAddress;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/enrollments")
//public class EnrollmentController {
//
//    private final EnrollmentService enrollmentService;
//    private final UserClient userClient;
//    private final CatalogClient catalogClient;
//
//    @Value("${server.port}")
//    private String currentPort;
//
//    public EnrollmentController(
//            EnrollmentService enrollmentService,
//            UserClient userClient,
//            CatalogClient catalogClient) {
//        this.enrollmentService = enrollmentService;
//        this.userClient = userClient;
//        this.catalogClient = catalogClient;
//    }
//
//    // ==================== Enrollment Endpoints ====================
//    @PostMapping
//    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
//        EnrollmentRecord record = enrollmentService.enroll(request.courseId(), request.studentId());
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(new EnrollmentResponse(
//                        record.getId(),
//                        record.getCourseId(),
//                        record.getStudentId(),
//                        record.getEnrolledAt().toString()
//                ));
//    }
//
//    @GetMapping("/course/{courseId}")
//    public List<EnrollmentResponse> listByCourse(@PathVariable String courseId) {
//        return enrollmentService.listByCourse(courseId)
//                .stream()
//                .map(record -> new EnrollmentResponse(
//                        record.getId(),
//                        record.getCourseId(),
//                        record.getStudentId(),
//                        record.getEnrolledAt().toString()
//                ))
//                .toList();
//    }
//
//    @GetMapping("/student/{studentId}")
//    public List<EnrollmentResponse> listByStudent(@PathVariable String studentId) {
//        return enrollmentService.listByStudent(studentId)
//                .stream()
//                .map(record -> new EnrollmentResponse(
//                        record.getId(),
//                        record.getCourseId(),
//                        record.getStudentId(),
//                        record.getEnrolledAt().toString()
//                ))
//                .toList();
//    }

//    @GetMapping
//    public List<EnrollmentResponse> listAll() {
//        return enrollmentService.listAll()
//                .stream()
//                .map(record -> new EnrollmentResponse(
//                        record.getId(),
//                        record.getCourseId(),
//                        record.getStudentId(),
//                        record.getEnrolledAt().toString()
//                ))
//                .toList();
//    }
//
//    // ==================== 测试接口（熔断验证）====================
//    @GetMapping("/test")
//    public Map<String, Object> test() {
//        Map<String, Object> response = new HashMap<>();
//        response.put("service", "enrollment-service");
//        response.put("port", currentPort);
//        response.put("hostname", getHostname());
//
//        try {
//            response.put("ip", InetAddress.getLocalHost().getHostAddress());
//        } catch (Exception e) {
//            response.put("ip", "unknown");
//        }
//
//        response.put("timestamp", LocalDateTime.now());
//
//        // ⭐ 关键修改：直接调用 Feign Client，让 fallback 自动处理
//        // 不要用 try-catch 捕获，Feign 的熔断机制会自动调用 fallback
//        response.put("user-service", userClient.test());
//        response.put("catalog-service", catalogClient.test());
//
//        return response;
//    }

//    // ==================== 辅助方法 ====================
//    private String getHostname() {
//        String hostname = System.getenv("HOSTNAME");
//        if (hostname != null && !hostname.isEmpty()) {
//            return hostname;
//        }
//        try {
//            return InetAddress.getLocalHost().getHostName();
//        } catch (Exception e) {
//            return "unknown-" + currentPort;
//        }
//    }
//
//    // ==================== 健康检查接口 ====================
//    @GetMapping("/health")
//    public Map<String, Object> health() {
//        Map<String, Object> healthResponse = new HashMap<>();
//        healthResponse.put("status", "UP");
//        healthResponse.put("service", "enrollment-service");
//        healthResponse.put("port", currentPort);
//        healthResponse.put("hostname", getHostname());
//        healthResponse.put("timestamp", System.currentTimeMillis());
//        return healthResponse;
//    }

//    // ==================== Record 定义 ====================
//    public record EnrollmentRequest(
//            @NotBlank String courseId,
//            @NotBlank String studentId
//    ) {}
//
//    public record EnrollmentResponse(
//            String id,
//            String courseId,
//            String studentId,
//            String enrolledAt
//    ) {}
//}

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
    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentRecord record = enrollmentService.enroll(request.courseId(), request.studentId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new EnrollmentResponse(
                        record.getId(),
                        record.getCourseId(),
                        record.getStudentId(),
                        record.getEnrolledAt().toString()
                ));
    }

    @GetMapping("/course/{courseId}")
    public List<EnrollmentResponse> listByCourse(@PathVariable String courseId) {
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

    @GetMapping("/student/{studentId}")
    public List<EnrollmentResponse> listByStudent(@PathVariable String studentId) {
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

    @GetMapping
    public List<EnrollmentResponse> listAll() {
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
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "enrollment-service");
        response.put("port", currentPort);
        response.put("hostname", getHostname());

        try {
            response.put("ip", InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            response.put("ip", "unknown");
        }

        response.put("timestamp", LocalDateTime.now());

        // ⭐⭐⭐ 直接调用，不要任何 try-catch！
        // Feign 的 fallback 会自动处理异常
        response.put("user-service", userClient.test());
        response.put("catalog-service", catalogClient.test());

        return response;
    }

    // ==================== 简化测试接口（不捕获异常）====================
    /**
     * 这个接口不捕获异常，用于测试 Feign fallback 是否真的工作
     * 如果 fallback 工作，应该返回 fallback 的 Map；如果不工作，会抛出异常
     */
    @GetMapping("/test-simple")
    public Map<String, Object> testSimple() {
        log.info("🧪 简化测试 - 直接调用 Feign Client（不捕获异常）");

        Map<String, Object> response = new HashMap<>();
        response.put("service", "enrollment-service");
        response.put("timestamp", LocalDateTime.now());

        // 直接调用，不 catch 异常
        // 如果 fallback 生效，应该返回 UserClientFallback.test() 的结果
        // 如果 fallback 不生效，会抛出异常到全局异常处理器
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