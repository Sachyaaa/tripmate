package com.tripmate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"event_id", "user_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
