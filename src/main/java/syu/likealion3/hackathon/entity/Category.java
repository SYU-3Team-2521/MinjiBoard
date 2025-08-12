package syu.likealion3.hackathon.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 게시글 카테고리를 정의하는 열거형
 */
@Getter
public enum Category {
    MARKET("전통시장·마트"),
    MEDICAL("약국·병원"),
    FOOD("음식점·카페·편의점"),
    ETC("기타");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    /**
     * JSON 직렬화 시 한글명으로 출력
     */
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 문자열에서 Category로 변환 (대소문자 무시)
     */
    @JsonCreator
    public static Category fromString(String value) {
        if (value == null) return null;

        for (Category category : Category.values()) {
            if (category.name().equalsIgnoreCase(value) ||
                    category.displayName.equals(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + value);
    }
}