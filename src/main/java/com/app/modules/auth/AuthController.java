package com.app.modules.auth;

import com.app.modules.auth.dto.AuthResponseDto;
import com.app.modules.auth.dto.LoginDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody LoginDto dto) {
        return authService.login(dto);
    }
}
