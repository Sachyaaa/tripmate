package com.tripmate.controller;

import com.tripmate.dto.request.CreateDayRequest;
import com.tripmate.dto.request.CreateItemRequest;
import com.tripmate.dto.request.ReorderItemRequest;
import com.tripmate.dto.request.UpdateItemRequest;
import com.tripmate.dto.response.ItineraryDayResponse;
import com.tripmate.dto.response.ItineraryItemResponse;
import com.tripmate.service.ItineraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;

    @GetMapping("/api/trips/{tripId}/days")
    public ResponseEntity<List<ItineraryDayResponse>> getDays(@PathVariable UUID tripId,
                                                              @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(itineraryService.getDaysWithItems(tripId, ud.getUsername()));
    }

    @PostMapping("/api/trips/{tripId}/days")
    public ResponseEntity<ItineraryDayResponse> addDay(@PathVariable UUID tripId,
                                                       @RequestBody @Valid CreateDayRequest req,
                                                       @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itineraryService.addDay(tripId, req, ud.getUsername()));
    }

    @PostMapping("/api/days/{dayId}/items")
    public ResponseEntity<ItineraryItemResponse> addItem(@PathVariable UUID dayId,
                                                         @RequestBody @Valid CreateItemRequest req,
                                                         @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itineraryService.addItem(dayId, req, ud.getUsername()));
    }

    @PutMapping("/api/items/{itemId}")
    public ResponseEntity<ItineraryItemResponse> updateItem(@PathVariable UUID itemId,
                                                            @RequestBody UpdateItemRequest req,
                                                            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(itineraryService.updateItem(itemId, req, ud.getUsername()));
    }

    @DeleteMapping("/api/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID itemId,
                                           @AuthenticationPrincipal UserDetails ud) {
        itineraryService.deleteItem(itemId, ud.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/days/{dayId}/reorder")
    public ResponseEntity<Void> reorderItems(@PathVariable UUID dayId,
                                             @RequestBody List<ReorderItemRequest> reorders,
                                             @AuthenticationPrincipal UserDetails ud) {
        itineraryService.reorderItems(dayId, reorders, ud.getUsername());
        return ResponseEntity.ok().build();
    }
}
