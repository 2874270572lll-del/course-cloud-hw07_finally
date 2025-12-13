package com.zjgsu.coursecloud.enrollment.service;

import com.zjgsu.coursecloud.enrollment.model.EnrollmentRecord;
import com.zjgsu.coursecloud.enrollment.repository.EnrollmentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final RestTemplate restTemplate;
    private final EnrollmentRepository repository;

    private static final String USER_SERVICE_URL = "http://user-service";
    private static final String CATALOG_SERVICE_URL = "http://catalog-service";

    public EnrollmentService(RestTemplate restTemplate, EnrollmentRepository repository) {
        this.restTemplate = restTemplate;
        this.repository = repository;
    }

    public EnrollmentRecord enroll(String courseId, String studentId) {
        log.info("开始选课: studentId={}, courseId={}", studentId, courseId);

        // Check if already enrolled
        if (repository.existsByCourseIdAndStudentId(courseId, studentId)) {
            log.warn("学生已选该课程: studentId={}, courseId={}", studentId, courseId);
            throw new IllegalStateException("Student is already enrolled in this course");
        }

        // 1. ⭐ 调用 user-service (带熔断保护)
        Map<String, Object> studentResponse = callUserService(studentId);
        if (studentResponse.containsKey("error")) {
            log.error("学生验证失败(降级): {}", studentResponse.get("error"));
            throw new RuntimeException("User service unavailable: " + studentResponse.get("error"));
        }

        // 2. ⭐ 调用 catalog-service (带熔断保护)
        Map<String, Object> courseResponse = callCatalogService(courseId);
        if (courseResponse.containsKey("error")) {
            log.error("课程验证失败(降级): {}", courseResponse.get("error"));
            throw new RuntimeException("Catalog service unavailable: " + courseResponse.get("error"));
        }

        // 检查课程容量
        Object dataObj = courseResponse.get("data");
        Map<String, Object> courseData = dataObj instanceof Map ? (Map<String, Object>) dataObj : courseResponse;

        Integer capacity = (Integer) courseData.get("capacity");
        Integer enrolled = (Integer) courseData.get("enrolled");

        if (enrolled != null && capacity != null && enrolled >= capacity) {
            log.warn("课程已满: courseId={}, capacity={}, enrolled={}", courseId, capacity, enrolled);
            throw new IllegalStateException("Course capacity reached");
        }

        // 3. Create enrollment record
        EnrollmentRecord record = new EnrollmentRecord(courseId, studentId);
        EnrollmentRecord saved = repository.save(record);

        log.info("选课成功: studentId={}, courseId={}, enrollmentId={}", studentId, courseId, saved.getId());
        return saved;
    }

    // ⭐⭐⭐ 关键：用 @CircuitBreaker 注解保护 user-service 调用
    @CircuitBreaker(name = "user-service", fallbackMethod = "userServiceFallback")
    public Map<String, Object> callUserService(String studentId) {
        String userUrl = USER_SERVICE_URL + "/api/students/studentId/" + studentId;
        log.info("调用 user-service: {}", userUrl);

        try {
            Map<String, Object> response = restTemplate.getForObject(userUrl, Map.class);
            log.info("✅ user-service 调用成功，端口: {}", response.get("port"));
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            log.error("学生不存在: {}", studentId);
            throw new IllegalArgumentException("Student not found: " + studentId);
        }
    }

    // ⭐ user-service 降级方法
    private Map<String, Object> userServiceFallback(String studentId, Exception e) {
        log.warn("🔥 user-service 熔断降级触发! studentId={}, 原因: {}", studentId, e.getMessage());

        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "User service unavailable");
        fallback.put("studentId", studentId);
        fallback.put("status", "FALLBACK");
        fallback.put("message", "服务降级: " + e.getMessage());
        return fallback;
    }

    // ⭐⭐⭐ 关键：用 @CircuitBreaker 注解保护 catalog-service 调用
    @CircuitBreaker(name = "catalog-service", fallbackMethod = "catalogServiceFallback")
    public Map<String, Object> callCatalogService(String courseId) {
        String courseUrl = CATALOG_SERVICE_URL + "/api/courses/" + courseId;
        log.info("调用 catalog-service: {}", courseUrl);

        try {
            Map<String, Object> response = restTemplate.getForObject(courseUrl, Map.class);
            log.info("✅ catalog-service 调用成功，端口: {}", response.get("port"));
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            log.error("课程不存在: {}", courseId);
            throw new IllegalArgumentException("Course not found: " + courseId);
        }
    }

    // ⭐ catalog-service 降级方法
    private Map<String, Object> catalogServiceFallback(String courseId, Exception e) {
        log.warn("🔥 catalog-service 熔断降级触发! courseId={}, 原因: {}", courseId, e.getMessage());

        Map<String, Object> fallback = new HashMap<>();
        fallback.put("error", "Catalog service unavailable");
        fallback.put("courseId", courseId);
        fallback.put("status", "FALLBACK");
        fallback.put("message", "服务降级: " + e.getMessage());
        return fallback;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRecord> listByCourse(String courseId) {
        log.debug("查询课程的选课记录: courseId={}", courseId);
        return repository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRecord> listByStudent(String studentId) {
        log.debug("查询学生的选课记录: studentId={}", studentId);
        return repository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRecord> listAll() {
        log.debug("查询所有选课记录");
        return repository.findAll();
    }
}