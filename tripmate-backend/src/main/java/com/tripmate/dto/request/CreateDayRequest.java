package com.tripmate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateDayRequest {
    private LocalDate dayDate;

    @NotNull
    private Integer dayNumber;

    private String title;
}
