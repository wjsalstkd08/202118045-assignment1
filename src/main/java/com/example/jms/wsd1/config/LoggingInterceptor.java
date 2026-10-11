package com.example.jms.wsd1.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();

        // [추가] 요청 ID와 시작 시각 기록 → 요청 시작/완료 로그를 짝지어 보고, 처리 시간을 계산하기 위함
        String requestId = java.util.UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute("requestId", requestId);
        request.setAttribute("startTime", System.currentTimeMillis());
        response.setHeader("X-Request-Id", requestId); // [추가] 클라이언트도 요청 ID를 확인할 수 있게 응답 헤더로 전달

        System.out.println("[LoggingInterceptor] 요청 시작: " + method + " " + uri +
                (query != null ? "?" + query : "")
                + " (id=" + requestId + ")"); // [추가] 요청 ID 출력
        return true; // false로 하면 컨트롤러로 안 넘어감
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        long elapsed = System.currentTimeMillis() - (long) request.getAttribute("startTime"); // [추가] 처리 시간
        System.out.println("[LoggingInterceptor] 요청 완료, status=" + response.getStatus()
                + ", " + elapsed + "ms (id=" + request.getAttribute("requestId") + ")"); // [추가] 처리 시간, 요청 ID 출력
    }
}
