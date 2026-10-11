package com.example.jms.wsd1.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ItemUpdateRequest {
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")              // [추가]
    @Pattern(regexp = ".*\\S.*", message = "이름은 공백만으로 이루어질 수 없습니다.") // [추가]
    private String name;

    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")               // [추가]
    @Max(value = 10_000_000, message = "가격은 10,000,000 이하여야 합니다.") // [추가]
    private Integer price;

    // getter / setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
