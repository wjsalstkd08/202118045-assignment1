package com.example.jms.wsd1.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final MaintenanceInterceptor maintenanceInterceptor; // [추가]

    @Autowired
    public WebConfig(LoggingInterceptor loggingInterceptor,
                     MaintenanceInterceptor maintenanceInterceptor) { // [수정] 파라미터 추가
        this.loggingInterceptor = loggingInterceptor;
        this.maintenanceInterceptor = maintenanceInterceptor; // [추가]
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**", "/admin/**") // [수정] 적용할 URL 패턴 (관리용 API 로그도 남기기 위해 /admin/** 추가)
                .excludePathPatterns("/health"); // 제외할 URL 패턴 (예시)

        // [추가] 점검 모드 인터셉터: 로깅 다음에 실행되므로 503으로 막힌 요청도 로그에 남는다.
        registry.addInterceptor(maintenanceInterceptor)
                .addPathPatterns("/api/**");     // /admin/** 은 대상이 아니므로 점검 중에도 점검 모드를 끌 수 있다
    }
}
