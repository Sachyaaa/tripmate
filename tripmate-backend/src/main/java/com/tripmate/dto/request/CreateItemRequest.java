package com.tripmate.dto.request;

import com.tripmate.entity.enums.ItemCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateItemRequest {
    @NotBlank
    private String title;

    private String time;
    private ItemCategory category;
    private String notes;
    private Integer position;
}
