package com.tripmate.event;

import com.tripmate.entity.Notification;
import com.tripmate.entity.TripMember;
import com.tripmate.repository.NotificationRepository;
import com.tripmate.repository.TripMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExpenseNotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final TripMemberRepository tripMemberRepository;   // you already have this

    @KafkaListener(topics = ExpenseEventProducer.TOPIC, groupId = "tripmate-notifications")
    public void onExpenseCreated(ExpenseCreatedEvent event, Acknowledgment ack) {
        List<TripMember> memberList = tripMemberRepository.findByTripId(event.tripId());

        for(TripMember member: memberList){
            if(!notificationRepository.existsByEventIdAndUserId(event.eventId(), member.getUser().getId())){

                Notification notification = Notification.builder()
                        .userId(member.getUser().getId())
                        .tripId(event.tripId())
                        .eventId(event.eventId())
                        .message(event.paidByName() + " added '" + event.title() + "' (" + event.currency() + event.amount() + ")")
                        .read(true)
                        .createdAt(LocalDateTime.now()).build();

                notificationRepository.save(notification);
            }
        }
        ack.acknowledge();
    }
}
