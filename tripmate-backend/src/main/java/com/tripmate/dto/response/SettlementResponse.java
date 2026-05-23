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
public class SettlementResponse {
    private UUID fromUserId;
    private String fromUserName;
    private String fromUserColor;
    private UUID toUserId;
    private String toUserName;
    private String toUserColor;
    private BigDecimal amount;
    private String currency;
}
