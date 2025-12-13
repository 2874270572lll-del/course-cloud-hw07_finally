package com.lll.zjgsu.coursecloud.user.dto;

import com.lll.zjgsu.coursecloud.user.model.User;
import com.lll.zjgsu.coursecloud.user.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserInfo user;

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private String id;  // ⭐ 注意是 String 类型
        private String username;
        private String email;
        private UserType userType;
    }

    // ⭐ 便捷构造方法
    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getUserType()
        );
    }
}