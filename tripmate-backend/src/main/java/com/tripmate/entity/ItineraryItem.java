package com.tripmate.entity;

import com.tripmate.entity.enums.ItemCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "itinerary_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private ItineraryDay day;

    @Column(nullable = false)
    private String title;

    private String time;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ItemCategory category = ItemCategory.OTHER;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private Integer position = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
