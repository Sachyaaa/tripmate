package com.tripmate.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"event_id", "user_id"}))
@Builder
public class Notification {
    @GeneratedValue
    @Id
    UUID id;

    UUID userId;

    UUID tripId;

    UUID eventId;

    String message;

    @Column(name = "is_read")
    boolean read;

    LocalDateTime createdAt;
}
