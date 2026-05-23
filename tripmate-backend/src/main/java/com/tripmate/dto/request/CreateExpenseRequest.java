package com.tripmate.dto.request;

import com.tripmate.entity.enums.SplitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateExpenseRequest {
    @NotBlank
    private String title;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private String currency = "INR";

    @NotNull
    private UUID paidByUserId;

    private String category;

    @NotNull
    private SplitType splitType;

    private LocalDate date;

    private List<ExpenseSplitRequest> splits;
}
