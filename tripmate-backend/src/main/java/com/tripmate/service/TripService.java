package com.tripmate.service;

import com.tripmate.dto.request.CreateTripRequest;
import com.tripmate.dto.request.UpdateTripRequest;
import com.tripmate.dto.response.*;
import com.tripmate.entity.*;
import com.tripmate.entity.enums.MemberRole;
import com.tripmate.exception.BadRequestException;
import com.tripmate.exception.ResourceNotFoundException;
import com.tripmate.exception.TripAccessDeniedException;
import com.tripmate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;
    private final ItineraryDayRepository itineraryDayRepository;
    private final ItineraryItemRepository itineraryItemRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final List<String> MEMBER_COLORS = List.of(
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
            "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9"
    );

    public List<TripResponse> getTripsForUser(String email) {
        User user = findUserByEmail(email);
        return tripMemberRepository.findByUserId(user.getId()).stream()
                .map(m -> toTripResponse(m.getTrip()))
                .toList();
    }

    @Transactional
    public TripResponse createTrip(CreateTripRequest req, String email) {
        User user = findUserByEmail(email);
        Trip trip = Trip.builder()
                .name(req.getName())
                .description(req.getDescription())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .coverEmoji(req.getCoverEmoji() != null ? req.getCoverEmoji() : "✈️")
                .createdBy(user)
                .inviteToken(generateInviteToken())
                .build();
        trip = tripRepository.save(trip);

        TripMember member = TripMember.builder()
                .trip(trip)
                .user(user)
                .role(MemberRole.CREATOR)
                .color(MEMBER_COLORS.get(0))
                .build();
        tripMemberRepository.save(member);
        return toTripResponse(trip);
    }

    @Transactional(readOnly = true)
    public TripDetailResponse getTripById(UUID tripId, String email) {
        User user = findUserByEmail(email);
        verifyMembership(tripId, user.getId());
        Trip trip = findTripById(tripId);

        List<TripMember> members = tripMemberRepository.findByTripId(tripId);
        List<ItineraryDay> days = itineraryDayRepository.findByTripIdOrderByDayNumber(tripId);
        List<ItineraryDayResponse> dayResponses = days.stream().map(day -> {
            List<ItineraryItem> items = itineraryItemRepository.findByDayIdOrderByPosition(day.getId());
            return toDayResponse(day, items);
        }).toList();

        List<Expense> expenses = expenseRepository.findByTripIdOrderByDateDescCreatedAtDesc(tripId);
        List<ExpenseResponse> expenseResponses = expenses.stream().map(e -> {
            var splits = expenseSplitRepository.findByExpenseId(e.getId());
            return toExpenseResponse(e, splits);
        }).toList();

        return TripDetailResponse.builder()
                .id(trip.getId())
                .name(trip.getName())
                .description(trip.getDescription())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .coverEmoji(trip.getCoverEmoji())
                .inviteToken(trip.getInviteToken())
                .createdBy(AuthService.toUserResponse(trip.getCreatedBy()))
                .createdAt(trip.getCreatedAt())
                .members(members.stream().map(this::toMemberResponse).toList())
                .days(dayResponses)
                .expenses(expenseResponses)
                .build();
    }

    @Transactional
    public TripResponse updateTrip(UUID tripId, UpdateTripRequest req, String email) {
        User user = findUserByEmail(email);
        verifyMembership(tripId, user.getId());
        Trip trip = findTripById(tripId);
        if (req.getName() != null) trip.setName(req.getName());
        if (req.getDescription() != null) trip.setDescription(req.getDescription());
        if (req.getStartDate() != null) trip.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) trip.setEndDate(req.getEndDate());
        if (req.getCoverEmoji() != null) trip.setCoverEmoji(req.getCoverEmoji());
        return toTripResponse(tripRepository.save(trip));
    }

    @Transactional
    public void deleteTrip(UUID tripId, String email) {
        User user = findUserByEmail(email);
        if (!tripMemberRepository.existsByTripIdAndUserIdAndRole(tripId, user.getId(), MemberRole.CREATOR)) {
            throw new TripAccessDeniedException("Only the trip creator can delete this trip");
        }
        tripRepository.deleteById(tripId);
    }

    @Transactional
    public TripResponse joinTrip(String token, String email) {
        User user = findUserByEmail(email);
        Trip trip = tripRepository.findByInviteToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invite link"));
        if (tripMemberRepository.existsByTripIdAndUserId(trip.getId(), user.getId())) {
            return toTripResponse(trip);
        }
        long memberCount = tripMemberRepository.countByTripId(trip.getId());
        String color = MEMBER_COLORS.get((int) (memberCount % MEMBER_COLORS.size()));
        TripMember member = TripMember.builder()
                .trip(trip).user(user).role(MemberRole.MEMBER).color(color).build();
        tripMemberRepository.save(member);
        return toTripResponse(trip);
    }

    public Map<String, String> getInviteUrl(UUID tripId, String email) {
        User user = findUserByEmail(email);
        verifyMembership(tripId, user.getId());
        Trip trip = findTripById(tripId);
        return Map.of(
                "inviteToken", trip.getInviteToken(),
                "inviteUrl", frontendUrl + "/join/" + trip.getInviteToken()
        );
    }

    public Map<String, Object> getTripPreviewByToken(String token) {
        Trip trip = tripRepository.findByInviteToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invite link"));
        long memberCount = tripMemberRepository.countByTripId(trip.getId());
        return Map.of(
                "id", trip.getId(),
                "name", trip.getName(),
                "coverEmoji", trip.getCoverEmoji(),
                "memberCount", memberCount
        );
    }

    private String generateInviteToken() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        String token = sb.toString();
        return tripRepository.findByInviteToken(token).isPresent() ? generateInviteToken() : token;
    }

    private void verifyMembership(UUID tripId, UUID userId) {
        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
            throw new TripAccessDeniedException("You are not a member of this trip");
        }
    }

    private Trip findTripById(UUID tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private TripResponse toTripResponse(Trip trip) {
        long memberCount = tripMemberRepository.countByTripId(trip.getId());
        return TripResponse.builder()
                .id(trip.getId()).name(trip.getName()).description(trip.getDescription())
                .startDate(trip.getStartDate()).endDate(trip.getEndDate())
                .coverEmoji(trip.getCoverEmoji()).inviteToken(trip.getInviteToken())
                .createdBy(AuthService.toUserResponse(trip.getCreatedBy()))
                .memberCount((int) memberCount).createdAt(trip.getCreatedAt())
                .build();
    }

    private TripMemberResponse toMemberResponse(TripMember m) {
        return TripMemberResponse.builder()
                .id(m.getId()).userId(m.getUser().getId())
                .displayName(m.getUser().getDisplayName()).email(m.getUser().getEmail())
                .avatarUrl(m.getUser().getAvatarUrl()).role(m.getRole())
                .color(m.getColor()).joinedAt(m.getJoinedAt())
                .build();
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

    private ExpenseResponse toExpenseResponse(Expense e, List<ExpenseSplit> splits) {
        return ExpenseResponse.builder()
                .id(e.getId()).title(e.getTitle()).amount(e.getAmount())
                .currency(e.getCurrency()).paidBy(AuthService.toUserResponse(e.getPaidBy()))
                .category(e.getCategory()).splitType(e.getSplitType())
                .date(e.getDate()).createdAt(e.getCreatedAt())
                .splits(splits.stream().map(s -> ExpenseSplitResponse.builder()
                        .userId(s.getUser().getId()).displayName(s.getUser().getDisplayName())
                        .amount(s.getAmount()).paid(s.isPaid()).build()).toList())
                .build();
    }
}
