package com.tripmate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MarkPaidRequest {
    @NotNull
    private UUID fromUserId;

    @NotNull
    private UUID toUserId;

    @NotNull
    private UUID tripId;
}
