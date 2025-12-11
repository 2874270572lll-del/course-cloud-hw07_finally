package com.zjgsu.coursecloud.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private String id;
    private String code;
    private String title;
    private String instructor;
    private String schedule;
    private Integer capacity;
    //已选人数
    private Integer enrolled;
}
