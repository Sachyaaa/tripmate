package com.tripmate.dto.response;

import com.tripmate.entity.enums.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripMemberResponse {
    private UUID id;
    private UUID userId;
    private String displayName;
    private String email;
    private String avatarUrl;
    private MemberRole role;
    private String color;
    private LocalDateTime joinedAt;
}
