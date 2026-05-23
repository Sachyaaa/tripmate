package com.tripmate.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ExpenseSplitRequest {
    private UUID userId;
    private BigDecimal amount;
}
