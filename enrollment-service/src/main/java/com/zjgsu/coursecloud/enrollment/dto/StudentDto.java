package com.zjgsu.coursecloud.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {
    private String id;
    private String studentId;
    private String name;
    private String email;
    private String major;
    private Integer grade;
}
