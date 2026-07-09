package com.tripmate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseEventProducer {

    public static final String TOPIC = "tripmate.expense.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishExpenseCreated(ExpenseCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.tripId().toString(), event)
                .whenComplete((result, ex) ->{
                    if(ex==null){
                        log.info("partition:{}", result.getRecordMetadata().partition());
                        log.info("offset:{}", result.getRecordMetadata().offset());

                    }else{
                        log.error("Event send failed for trip id: {} with error : {}",event.tripId(), ex.getMessage());
                    }

                });
    }
}
