package syu.likealion3.hackathon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 설정
 * - Credentials(쿠키) 허용
 * - X-CSRF-Token, X-PIN 등 커스텀 헤더 허용
 * - 여러 Origin을 콤마로 설정 가능: app.cors.allowed-origins
 *
 * application-*.yml 예시:
 * app:
 *   cors:
 *     allowed-origins: "https://example.com,https://admin.example.com,http://localhost:3000"
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = allowedOrigins.split("\\s*,\\s*");

                registry.addMapping("/api/**")
                        // 자격 증명(쿠키/Authorization 헤더) 허용
                        .allowCredentials(true)

                        // 허용 Origin (와일드카드 불가: allowCredentials(true) 사용 시 반드시 명시)
                        .allowedOrigins(origins)

                        // 허용 메서드
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")

                        // 허용 헤더(프론트에서 보낼 수 있는 요청 헤더)
                        .allowedHeaders(
                                "Content-Type",
                                "X-CSRF-Token",
                                "X-PIN",
                                "Accept",
                                "Origin",
                                "Authorization" // 필요 없다면 제거 가능
                        )

                        // 노출 헤더(브라우저에서 읽을 수 있는 응답 헤더)
                        .exposedHeaders(
                                "Set-Cookie" // 필요 시 응답 헤더 확인 가능
                        )

                        // Preflight 캐시 시간(초)
                        .maxAge(3600);
            }
        };
    }
}
