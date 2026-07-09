package com.tripmate.service;

import com.tripmate.dto.response.NotificationResponse;
import com.tripmate.entity.Notification;
import com.tripmate.entity.User;
import com.tripmate.exception.ResourceNotFoundException;
import com.tripmate.exception.TripAccessDeniedException;
import com.tripmate.repository.NotificationRepository;
import com.tripmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationResponse> getNotifications(String email) {
        User user = findUser(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, String email) {
        User user = findUser(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        // A user may only mark their OWN notifications as read.
        if (!notification.getUserId().equals(user.getId())) {
            throw new TripAccessDeniedException("This notification does not belong to you");
        }
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .tripId(n.getTripId())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
