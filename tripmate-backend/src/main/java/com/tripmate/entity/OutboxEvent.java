package com.tripmate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    @Id
    @GeneratedValue
    UUID id;
    String topic;         // where to publish (e.g. tripmate.expense.events)
    String messageKey;    // Kafka partition key (tripId as String)
    @Column(columnDefinition = "TEXT")
    String payload;       // the event serialized to JSON
    boolean published;    // default false
    LocalDateTime createdAt;
}
