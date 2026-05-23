package com.tripmate.dto.request;

import com.tripmate.entity.enums.SplitType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateExpenseRequest {
    private String title;
    private BigDecimal amount;
    private String currency;
    private UUID paidByUserId;
    private String category;
    private SplitType splitType;
    private LocalDate date;
    private List<ExpenseSplitRequest> splits;
}
