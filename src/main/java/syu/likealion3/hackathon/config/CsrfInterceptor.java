package syu.likealion3.hackathon.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.web.servlet.HandlerInterceptor;
import syu.likealion3.hackathon.util.SecurityUtil;

import java.io.IOException;
import java.time.Duration;

public class CsrfInterceptor implements HandlerInterceptor {
    private static final String COOKIE = "csrf_token";
    private static final String HEADER = "X-CSRF-Token";

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
        String method = req.getMethod();
        // GET/HEAD: CSRF 쿠키 없으면 발급(httponly=false: JS에서 헤더로 전송 가능하게)
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            if (getCookie(req, COOKIE) == null) {
                String token = SecurityUtil.generateTokenBase64Url();
                ResponseCookie c = ResponseCookie.from(COOKIE, token)
                        .httpOnly(false).secure(true).sameSite("Strict").path("/")
                        .maxAge(Duration.ofHours(6)).build();
                res.addHeader("Set-Cookie", c.toString());
            }
            return true;
        }
        // 변경 메서드: 헤더와 쿠키 비교
        String cookie = getCookie(req, COOKIE);
        String header = req.getHeader(HEADER);
        if (cookie != null && cookie.equals(header)) return true;

        res.sendError(403, "CSRF");
        return false;
    }

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (jakarta.servlet.http.Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
