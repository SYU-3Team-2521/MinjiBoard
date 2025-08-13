package syu.likealion3.hackathon.entity;

/**
 * 게시글 카테고리
 * - enum 이름은 API 파라미터/응답에 그대로 쓰입니다.
 * - 한글 라벨은 화면 표시에만 참고용입니다.
 */
public enum Category {
    MARKET("전통시장&마트"),
    PHARMACY_HOSPITAL("약국&병원"),
    FOOD_CAFE_CONVENIENCE("음식점&카페&편의점"),
    ETC("기타");

    private final String labelKo;

    Category(String labelKo) {
        this.labelKo = labelKo;
    }

    public String getLabelKo() {
        return labelKo;
    }
}
