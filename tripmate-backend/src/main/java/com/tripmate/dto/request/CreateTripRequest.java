package com.tripmate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTripRequest {
    @NotBlank
    private String name;

    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String coverEmoji;
}
