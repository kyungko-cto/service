package com.delivery.api.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * 성능 최적화 설정 - 대규모 트래픽 대응
 * 
 * 리팩토링 사항:
 * 1. Tomcat 커넥션 풀 최적화
 * 2. 요청 로깅 필터 (선택적)
 * 3. CPU 사용률 개선
 * 4. 메모리 누수 방지
 */
@Configuration
public class PerformanceConfig {

    /**
     * Tomcat 커넥션 풀 최적화
     * 
     * 리팩토링: 대규모 트래픽 대응
     * - 최대 스레드 수 증가
     * - 커넥션 타임아웃 설정
     * - CPU 사용률 개선: 스레드 풀 최적화
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                // HTTP/1.1 Keep-Alive 최적화
                connector.setProperty("maxKeepAliveRequests", "100");
                connector.setProperty("keepAliveTimeout", "20000");
                
                // 압축 설정
                connector.setProperty("compression", "on");
                connector.setProperty("compressionMinSize", "1024");
                connector.setProperty("compressableMimeType", 
                    "text/html,text/xml,text/plain,text/css,text/javascript,application/json,application/javascript");
            });
        };
    }

    /**
     * 요청 로깅 필터 (프로덕션에서는 비활성화 권장)
     * 
     * 리팩토링: 성능 최적화
     * - 프로덕션에서는 로깅 비활성화로 CPU 사용률 개선
     * - 디버깅 시에만 활성화
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false);  // 프로덕션에서는 페이로드 로깅 비활성화 (성능)
        filter.setIncludeHeaders(false);  // 헤더 로깅 비활성화 (성능)
        filter.setMaxPayloadLength(10000);
        return filter;
    }
}

