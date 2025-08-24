package syu.likealion3.hackathon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "https://minsaengcheck.syu-likelion.org",
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://15.164.228.20:5173"
                )
                .allowedHeaders("*")
                .exposedHeaders("Location")
                .allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}