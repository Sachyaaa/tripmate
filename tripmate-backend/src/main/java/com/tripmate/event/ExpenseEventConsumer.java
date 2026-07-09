package com.tripmate.event;

import com.tripmate.entity.ActivityLog;
import com.tripmate.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseEventConsumer {
    private final ActivityLogRepository activityLogRepository;

    @KafkaListener(topics = ExpenseEventProducer.TOPIC)
    // groupId comes from application.properties
    public void onExpenseCreated(ExpenseCreatedEvent event, Acknowledgment ack) {
        if(activityLogRepository.existsByEventId(event.eventId())){
            log.warn("Duplicate event id, skipping");
            return;
        }

        String message = event.paidByName() + " added " + event.currency() + event.amount() + " for " + event.title();
        ActivityLog log = ActivityLog.builder()
                .tripId(event.tripId())
                .eventId(event.eventId())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        activityLogRepository.save(log);

        ack.acknowledge();
    }
}
