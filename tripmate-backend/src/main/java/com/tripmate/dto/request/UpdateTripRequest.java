package com.tripmate.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTripRequest {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String coverEmoji;
}
