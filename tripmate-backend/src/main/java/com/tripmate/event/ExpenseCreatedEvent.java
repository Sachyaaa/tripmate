package com.tripmate.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ExpenseCreatedEvent(
        UUID eventId,        // unique per event → the consumer's idempotency key
        UUID tripId,         // becomes the Kafka message KEY → ordering per trip
        UUID expenseId,
        String title,
        BigDecimal amount,
        String currency,
        UUID paidByUserId,
        String paidByName,
        Instant occurredAt
) {}
