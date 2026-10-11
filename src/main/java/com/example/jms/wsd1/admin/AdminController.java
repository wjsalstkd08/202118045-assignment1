package com.example.jms.wsd1.admin;

import com.example.jms.wsd1.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final MaintenanceState maintenanceState;

    public AdminController(MaintenanceState maintenanceState) {
        this.maintenanceState = maintenanceState;
    }

    // 점검 모드 켜기/끄기. 켜면 /api/** 요청이 모두 503을 받는다.
    @PutMapping("/maintenance")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> setMaintenance(
            @Valid @RequestBody MaintenanceRequest request
    ) {
        maintenanceState.setEnabled(request.getEnabled());
        return ResponseEntity.ok(ApiResponse.success(Map.of("maintenance", request.getEnabled())));
    }

    // 예상하지 못한 서버 예외를 일부러 발생시킨다 → GlobalExceptionHandler가 500으로 변환
    @GetMapping("/error-test")
    public ResponseEntity<ApiResponse<Void>> errorTest() {
        throw new IllegalStateException("의도적으로 발생시킨 내부 오류 (클라이언트에게는 노출되지 않음)");
    }
}
