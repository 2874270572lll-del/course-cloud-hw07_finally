package com.zjgsu.coursecloud.enrollment.client;

import com.zjgsu.coursecloud.enrollment.dto.CourseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
    name = "catalog-service",
    fallbackFactory = CatalogClientFallbackFactory.class  // ⭐ 改用 FallbackFactory
)
public interface CatalogClient {
    @GetMapping("/api/courses/{id}")
    CourseDto getCourseById(@PathVariable String id);

    @GetMapping("/api/courses/by-code/{code}")
    CourseDto getCourseByCode(@PathVariable String code);

    @GetMapping("/api/courses/{id}/has-capacity")
    Boolean hasCapacity(@PathVariable String id);

    @GetMapping("/api/courses/test")
    Map<String, Object> test();
}
