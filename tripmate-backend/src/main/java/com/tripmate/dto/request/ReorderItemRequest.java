package com.tripmate.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ReorderItemRequest {
    private UUID id;
    private Integer position;
}
