package com.tripmate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String coverEmoji;
    private String inviteToken;
    private UserResponse createdBy;
    private LocalDateTime createdAt;
    private List<TripMemberResponse> members;
    private List<ItineraryDayResponse> days;
    private List<ExpenseResponse> expenses;
}
