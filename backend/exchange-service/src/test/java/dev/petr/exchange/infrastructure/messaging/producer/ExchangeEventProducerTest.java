package dev.petr.exchange.infrastructure.messaging.producer;

import dev.petr.exchange.domain.event.ExchangeEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExchangeEventProducerTest {

    @Mock
    private KafkaTemplate<String, ExchangeEvent> kafkaTemplate;

    @InjectMocks
    private ExchangeEventProducer producer;

    @Test
    void sendExchangeRequested_setsTypeAndSends() {
        ExchangeEvent event = ExchangeEvent.builder().exchangeId(1L).build();
        producer.sendExchangeRequested(event);
        assertEquals("EXCHANGE_REQUESTED", event.getEventType());
        verify(kafkaTemplate).send("exchange-events", "1", event);
    }

    @Test
    void sendExchangeAccepted_setsTypeAndSends() {
        ExchangeEvent event = ExchangeEvent.builder().exchangeId(2L).build();
        producer.sendExchangeAccepted(event);
        assertEquals("EXCHANGE_ACCEPTED", event.getEventType());
        verify(kafkaTemplate).send("exchange-events", "2", event);
    }

    @Test
    void sendExchangeRejected_setsTypeAndSends() {
        ExchangeEvent event = ExchangeEvent.builder().exchangeId(3L).build();
        producer.sendExchangeRejected(event);
        assertEquals("EXCHANGE_REJECTED", event.getEventType());
        verify(kafkaTemplate).send("exchange-events", "3", event);
    }
}
