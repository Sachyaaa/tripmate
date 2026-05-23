package com.tripmate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseSplitResponse {
    private UUID userId;
    private String displayName;
    private BigDecimal amount;
    private boolean paid;
}
