package me.taeuk.record.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 실제 파일 시스템의 'uploads' 폴더를 /uploads/** 경로로 매핑
        // System.getProperty("user.dir")는 현재 프로젝트 실행 경로 반환 (외부 디렉토리에 서빙)
        String uploadPath = "file:" + System.getProperty("user.dir") + "/uploads/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(3600); // (선택) 캐시 1시간
    }
}
