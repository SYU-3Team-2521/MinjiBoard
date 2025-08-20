package syu.likealion3.hackathon.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateLikeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateLike(DuplicateLikeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("DUPLICATE_LIKE",
                        "이미 좋아요를 누르셨습니다. (게시물 당 좋아요 1개까지 가능합니다)"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("NOT_FOUND",
                        ex.getMessage() == null ? "Resource not found" : ex.getMessage()));
    }

    /** 400: @RequestBody 누락/JSON 파싱 실패 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("BAD_REQUEST", "요청 본문(JSON)이 비었습니다."));
    }

    /** 404: 매핑/정적 리소스 없음 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("NOT_FOUND", "요청하신 경로를 찾을 수 없습니다."));
    }

    /** 405: 메서드 미허용 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of("METHOD_NOT_ALLOWED",
                        "허용되지 않은 HTTP 메서드입니다. (" + ex.getMethod() + ")"));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        String msg = ex.getMessage();
        if (ex instanceof MethodArgumentNotValidException manv && manv.getBindingResult().hasErrors()) {
            msg = manv.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        if (ex instanceof BindException be && be.getBindingResult().hasErrors()) {
            msg = be.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        return ResponseEntity.badRequest().body(ErrorResponse.of("BAD_REQUEST", msg));
    }

    /** 업로드 관련(멀티파트, 용량, IO) → 400으로 명확히 */
    @ExceptionHandler({MultipartException.class, MaxUploadSizeExceededException.class, IOException.class})
    public ResponseEntity<ErrorResponse> handleUpload(Exception ex) {
        String msg = ex.getMessage();
        if (ex instanceof MaxUploadSizeExceededException) msg = "업로드 가능한 최대 용량을 초과했습니다.";
        log.warn("Upload error: {}", msg, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("UPLOAD_ERROR", msg == null ? "업로드 실패" : msg));
    }

    /** DB 무결성(널/길이/외래키 등) → 400으로 */
    @ExceptionHandler({DataIntegrityViolationException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleDataIntegrity(Exception ex) {
        log.warn("Data integrity violation: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("DATA_INTEGRITY", "저장할 수 없습니다. 입력값을 확인해주세요."));
    }

    /** 마지막 안전망: 요청 컨텍스트 포함 로깅 + 500 반환 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error: {} {} -> {}", req.getMethod(), req.getRequestURI(), ex.getClass().getName(), ex);
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) msg = "Internal server error";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", msg));
    }
}
