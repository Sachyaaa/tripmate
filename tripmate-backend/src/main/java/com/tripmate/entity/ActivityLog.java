package com.tripmate.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activity_log",
        uniqueConstraints = @UniqueConstraint(columnNames = "event_id"))
@Builder
public class ActivityLog {

    @GeneratedValue
    @Id
    UUID id;
    UUID tripId;
    UUID eventId;
    String message;
    LocalDateTime createdAt;
}
