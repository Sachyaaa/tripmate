package com.tripmate.controller;

import com.tripmate.dto.request.CreateTripRequest;
import com.tripmate.dto.request.UpdateTripRequest;
import com.tripmate.dto.response.TripDetailResponse;
import com.tripmate.dto.response.TripResponse;
import com.tripmate.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping
    public ResponseEntity<List<TripResponse>> getTrips(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tripService.getTripsForUser(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@RequestBody @Valid CreateTripRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripService.createTrip(request, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripDetailResponse> getTripById(@PathVariable UUID id,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tripService.getTripById(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> updateTrip(@PathVariable UUID id,
                                                   @RequestBody UpdateTripRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tripService.updateTrip(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable UUID id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        tripService.deleteTrip(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // Public — returns trip preview for the invite page (no auth required)
    @GetMapping("/join/{token}")
    public ResponseEntity<Map<String, Object>> getTripPreview(@PathVariable String token) {
        return ResponseEntity.ok(tripService.getTripPreviewByToken(token));
    }

    // Permitted route but handled with explicit auth check in service layer
    @PostMapping("/join/{token}")
    public ResponseEntity<TripResponse> joinTrip(@PathVariable String token,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(tripService.joinTrip(token, userDetails.getUsername()));
    }

    @GetMapping("/{id}/invite")
    public ResponseEntity<Map<String, String>> getInviteUrl(@PathVariable UUID id,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tripService.getInviteUrl(id, userDetails.getUsername()));
    }
}
