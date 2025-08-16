package syu.likealion3.hackathon.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ErrorResponse(String code, String message, String timestamp) {
    public static ErrorResponse of(String code, String message) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new ErrorResponse(code, message, ts);
    }
}
