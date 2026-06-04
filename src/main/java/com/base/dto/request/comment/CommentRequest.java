package com.base.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequest {

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 2000, message = "Nội dung không vượt quá 2000 ký tự")
    private String content;

    private Long parentId; // null nếu là comment gốc, có giá trị nếu là reply
}
