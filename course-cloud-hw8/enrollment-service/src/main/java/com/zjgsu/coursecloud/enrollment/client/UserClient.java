package com.zjgsu.coursecloud.enrollment.client;

import com.zjgsu.coursecloud.enrollment.dto.StudentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
    name = "user-service",
    fallbackFactory = UserClientFallbackFactory.class  // ⭐ 改用 FallbackFactory
)
public interface UserClient {
    @GetMapping("/api/students/{id}")
    StudentDto getStudentById(@PathVariable String id);

    @GetMapping("/api/students/by-student-id/{studentId}")
    StudentDto getStudentByStudentId(@PathVariable String studentId);

    @GetMapping("/api/students/test")
    Map<String, Object> test();
}
