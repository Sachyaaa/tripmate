package com.tripmate.controller;

import com.tripmate.dto.response.NotificationResponse;
import com.tripmate.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(notificationService.getNotifications(ud.getUsername()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable UUID id,
                                                           @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(notificationService.markAsRead(id, ud.getUsername()));
    }
}
