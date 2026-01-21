package dev.petr.exchange.infrastructure.messaging.producer;

import dev.petr.exchange.domain.event.ExchangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeEventProducer {

    private final KafkaTemplate<String, ExchangeEvent> kafkaTemplate;

    public void sendExchangeRequested(ExchangeEvent event) {
        event.setEventType("EXCHANGE_REQUESTED");
        log.info("Sending EXCHANGE_REQUESTED event: {}", event);
        kafkaTemplate.send("exchange-events", event.getExchangeId().toString(), event);
    }

    public void sendExchangeAccepted(ExchangeEvent event) {
        event.setEventType("EXCHANGE_ACCEPTED");
        log.info("Sending EXCHANGE_ACCEPTED event: {}", event);
        kafkaTemplate.send("exchange-events", event.getExchangeId().toString(), event);
    }

    public void sendExchangeRejected(ExchangeEvent event) {
        event.setEventType("EXCHANGE_REJECTED");
        log.info("Sending EXCHANGE_REJECTED event: {}", event);
        kafkaTemplate.send("exchange-events", event.getExchangeId().toString(), event);
    }
}
