package com.tripmate.repository;

import com.tripmate.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
