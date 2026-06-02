package com.base.dto.request.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBrandRequest {
    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(
            min = 2,
            max = 100,
            message = "Tên thương hiệu phải từ 2 đến 100 ký tự"
    )
    private String brandName;

    @Size(
            max = 500,
            message = "Mô tả thương hiệu không được vượt quá 500 ký tự"
    )
    private String brandLogo;
}