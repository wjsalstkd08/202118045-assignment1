package com.example.jms.wsd1.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ItemCreateRequest {
    @NotBlank(message = "이름은 필수입니다.")                     // [추가]
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")      // [추가]
    private String name;

    @NotNull(message = "가격은 필수입니다.")                       // [추가]
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")        // [추가]
    @Max(value = 10_000_000, message = "가격은 10,000,000 이하여야 합니다.") // [추가]
    private Integer price;

    // getter / setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
