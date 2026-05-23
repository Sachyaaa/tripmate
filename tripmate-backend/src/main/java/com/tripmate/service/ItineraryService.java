package com.tripmate.service;

import com.tripmate.dto.request.CreateDayRequest;
import com.tripmate.dto.request.CreateItemRequest;
import com.tripmate.dto.request.ReorderItemRequest;
import com.tripmate.dto.request.UpdateItemRequest;
import com.tripmate.dto.response.ItineraryDayResponse;
import com.tripmate.dto.response.ItineraryItemResponse;
import com.tripmate.dto.response.UserResponse;
import com.tripmate.entity.ItineraryDay;
import com.tripmate.entity.ItineraryItem;
import com.tripmate.entity.Trip;
import com.tripmate.entity.User;
import com.tripmate.entity.enums.ItemCategory;
import com.tripmate.exception.ResourceNotFoundException;
import com.tripmate.exception.TripAccessDeniedException;
import com.tripmate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryDayRepository dayRepository;
    private final ItineraryItemRepository itemRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    public List<ItineraryDayResponse> getDaysWithItems(UUID tripId, String email) {
        verifyMembership(tripId, email);
        return dayRepository.findByTripIdOrderByDayNumber(tripId).stream()
                .map(day -> toDayResponse(day, itemRepository.findByDayIdOrderByPosition(day.getId())))
                .toList();
    }

    @Transactional
    public ItineraryDayResponse addDay(UUID tripId, CreateDayRequest req, String email) {
        verifyMembership(tripId, email);
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
        ItineraryDay day = ItineraryDay.builder()
                .trip(trip).dayDate(req.getDayDate())
                .dayNumber(req.getDayNumber()).title(req.getTitle())
                .build();
        day = dayRepository.save(day);
        return toDayResponse(day, List.of());
    }

    @Transactional
    public ItineraryItemResponse addItem(UUID dayId, CreateItemRequest req, String email) {
        ItineraryDay day = dayRepository.findById(dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Day not found"));
        verifyMembership(day.getTrip().getId(), email);
        User user = userRepository.findByEmail(email).orElseThrow();
        long count = itemRepository.countByDayId(dayId);
        ItineraryItem item = ItineraryItem.builder()
                .day(day).title(req.getTitle()).time(req.getTime())
                .category(req.getCategory() != null ? req.getCategory() : ItemCategory.OTHER)
                .notes(req.getNotes())
                .position(req.getPosition() != null ? req.getPosition() : (int) count + 1)
                .createdBy(user)
                .build();
        return toItemResponse(itemRepository.save(item));
    }

    @Transactional
    public ItineraryItemResponse updateItem(UUID itemId, UpdateItemRequest req, String email) {
        ItineraryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        verifyMembership(item.getDay().getTrip().getId(), email);
        if (req.getTitle() != null) item.setTitle(req.getTitle());
        if (req.getTime() != null) item.setTime(req.getTime());
        if (req.getCategory() != null) item.setCategory(req.getCategory());
        if (req.getNotes() != null) item.setNotes(req.getNotes());
        if (req.getPosition() != null) item.setPosition(req.getPosition());
        return toItemResponse(itemRepository.save(item));
    }

    @Transactional
    public void deleteItem(UUID itemId, String email) {
        ItineraryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        verifyMembership(item.getDay().getTrip().getId(), email);
        itemRepository.delete(item);
    }

    @Transactional
    public void reorderItems(UUID dayId, List<ReorderItemRequest> reorders, String email) {
        ItineraryDay day = dayRepository.findById(dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Day not found"));
        verifyMembership(day.getTrip().getId(), email);
        for (ReorderItemRequest r : reorders) {
            itemRepository.findById(r.getId()).ifPresent(item -> {
                item.setPosition(r.getPosition());
                itemRepository.save(item);
            });
        }
    }

    private void verifyMembership(UUID tripId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, user.getId())) {
            throw new TripAccessDeniedException("You are not a member of this trip");
        }
    }

    private ItineraryDayResponse toDayResponse(ItineraryDay day, List<ItineraryItem> items) {
        return ItineraryDayResponse.builder()
                .id(day.getId()).dayDate(day.getDayDate()).dayNumber(day.getDayNumber())
                .title(day.getTitle())
                .items(items.stream().map(this::toItemResponse).toList())
                .build();
    }

    private ItineraryItemResponse toItemResponse(ItineraryItem item) {
        UserResponse createdBy = item.getCreatedBy() != null
                ? AuthService.toUserResponse(item.getCreatedBy()) : null;
        return ItineraryItemResponse.builder()
                .id(item.getId()).title(item.getTitle()).time(item.getTime())
                .category(item.getCategory()).notes(item.getNotes())
                .position(item.getPosition()).createdBy(createdBy)
                .build();
    }
}
