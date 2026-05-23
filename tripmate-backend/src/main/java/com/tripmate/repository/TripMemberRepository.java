package com.tripmate.repository;

import com.tripmate.entity.TripMember;
import com.tripmate.entity.enums.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripMemberRepository extends JpaRepository<TripMember, UUID> {
    List<TripMember> findByUserId(UUID userId);
    List<TripMember> findByTripId(UUID tripId);
    Optional<TripMember> findByTripIdAndUserId(UUID tripId, UUID userId);
    boolean existsByTripIdAndUserId(UUID tripId, UUID userId);
    long countByTripId(UUID tripId);
    boolean existsByTripIdAndUserIdAndRole(UUID tripId, UUID userId, MemberRole role);
}
