package com.xparience.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ServerCheckController {

    @GetMapping("/")
    public ApiResponse<Map<String, String>> healthCheck() {
        return ApiResponse.success("Health check passed", Map.of("status", "UP"));
    }
}
