package com.tripmate.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.entity.OutboxEvent;
import com.tripmate.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 2000)   // poll every 2s
    @Transactional
    public void publishPending() {
        for (OutboxEvent row : outboxRepository.findByPublishedFalseOrderByCreatedAtAsc()) {
            try {
                ExpenseCreatedEvent event = objectMapper.readValue(row.getPayload(), ExpenseCreatedEvent.class);
                kafkaTemplate.send(row.getTopic(), row.getMessageKey(), event).get();
                row.setPublished(true);
            } catch (Exception e) {
                // leave published=false → retried next tick. Stop the loop so ordering is preserved.
                log.warn("Outbox publish failed for {}, will retry", row.getId(), e);
                break;
            }
        }
    }
}

