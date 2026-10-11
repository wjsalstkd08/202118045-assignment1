package com.example.jms.wsd1.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// [추가] 새 파일: PUT /api/v3/items/{id}/price 요청 본문
public class ItemPriceUpdateRequest {
    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    @Max(value = 10_000_000, message = "가격은 10,000,000 이하여야 합니다.")
    private Integer price;

    // getter / setter
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
