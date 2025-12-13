package com.lll.zjgsu.coursecloud.user.api;

import com.lll.zjgsu.coursecloud.user.dto.LoginRequest;
import com.lll.zjgsu.coursecloud.user.dto.LoginResponse;
import com.lll.zjgsu.coursecloud.user.model.User;
import com.lll.zjgsu.coursecloud.user.service.UserService;
import com.lll.zjgsu.coursecloud.user.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("登录请求: {}", request.getUsername());

        // 1. 验证用户名和密码
        User user = userService.findByUsername(request.getUsername());

        if (user == null || !user.getPassword().equals(request.getPassword())) {
            log.warn("用户名或密码错误: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("用户名或密码错误");
        }

        // 2. 生成 JWT Token (⭐ 注意 user.getId() 已经是 String 类型)
        String token = jwtUtil.generateToken(
                user.getId(),  // ⭐ 直接使用，不需要 toString()
                user.getUsername(),
                user.getRole()  // ⭐ 使用 getRole() 方法
        );

        log.info("登录成功，生成 Token，用户: {}", user.getUsername());

        // 3. 返回 Token 和用户信息
        return ResponseEntity.ok(new LoginResponse(token, user));
    }
}