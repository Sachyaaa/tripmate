package com.tripmate.dto.request;

import com.tripmate.entity.enums.ItemCategory;
import lombok.Data;

@Data
public class UpdateItemRequest {
    private String title;
    private String time;
    private ItemCategory category;
    private String notes;
    private Integer position;
}
