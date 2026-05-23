package com.tripmate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItineraryDayResponse {
    private UUID id;
    private LocalDate dayDate;
    private Integer dayNumber;
    private String title;
    private List<ItineraryItemResponse> items;
}
