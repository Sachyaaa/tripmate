package com.tripmate.dto.response;

import com.tripmate.entity.enums.SplitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponse {
    private UUID id;
    private String title;
    private BigDecimal amount;
    private String currency;
    private UserResponse paidBy;
    private String category;
    private SplitType splitType;
    private LocalDate date;
    private LocalDateTime createdAt;
    private List<ExpenseSplitResponse> splits;
}
