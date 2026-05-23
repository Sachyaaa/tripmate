package com.tripmate.dto.response;

import com.tripmate.entity.enums.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItineraryItemResponse {
    private UUID id;
    private String title;
    private String time;
    private ItemCategory category;
    private String notes;
    private Integer position;
    private UserResponse createdBy;
}
