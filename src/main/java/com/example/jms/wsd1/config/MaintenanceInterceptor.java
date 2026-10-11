package com.example.jms.wsd1.config;

import com.example.jms.wsd1.admin.MaintenanceState;
import com.example.jms.wsd1.common.ApiResponse;
import com.example.jms.wsd1.common.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 미들웨어 2: 점검 모드 차단
 *
 * 점검 모드가 켜져 있으면 컨트롤러로 넘기지 않고(return false)
 * 503 Service Unavailable을 표준 응답 형식으로 바로 돌려준다.
 * 점검 모드 전환 API(/admin/**)에는 적용하지 않는다.
 */
@Component
public class MaintenanceInterceptor implements HandlerInterceptor {

    private final MaintenanceState maintenanceState;
    private final ObjectMapper objectMapper;

    public MaintenanceInterceptor(MaintenanceState maintenanceState, ObjectMapper objectMapper) {
        this.maintenanceState = maintenanceState;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!maintenanceState.isEnabled()) {
            return true;
        }
        ErrorCode code = ErrorCode.SERVICE_UNAVAILABLE;
        response.setStatus(code.getStatus().value());
        response.setHeader("Retry-After", "60");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                ApiResponse.error(code.name(), code.getDefaultMessage()));
        return false;
    }
}
