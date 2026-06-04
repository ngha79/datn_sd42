package com.base.dto.request.postCategory;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostCategoryRequest {

    @NotBlank
    private String postCategoryName;
}
