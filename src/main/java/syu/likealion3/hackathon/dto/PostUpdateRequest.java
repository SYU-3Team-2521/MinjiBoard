package syu.likealion3.hackathon.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import syu.likealion3.hackathon.entity.Category;

@Getter
@NoArgsConstructor
public class PostUpdateRequest {

    @NotBlank(message = "제목은 비워둘 수 없습니다.")
    private String title;

    @NotBlank(message = "본문은 비워둘 수 없습니다.")
    private String content;

    /** 수정 시에도 영문/한글 모두 허용 */
    @NotBlank(message = "카테고리는 비워둘 수 없습니다.")
    private String category;

    private String imageUrl; // 선택
    @NotBlank(message = "가게 이름은 비워둘 수 없습니다.")
    private String storeName;

    @NotBlank(message = "가게 주소는 비워둘 수 없습니다.")
    private String storeAddress;

    public Category toCategory() {
        return Category.fromString(category);
    }
}
