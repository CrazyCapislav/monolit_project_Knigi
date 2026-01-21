package dev.petr.exchange.infrastructure.config;

import dev.petr.exchange.domain.event.BookCommandEvent;
import dev.petr.exchange.domain.event.ExchangeEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class KafkaConfigTest {

    @Test
    void createsProducerBeans() {
        KafkaConfig config = new KafkaConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");

        ProducerFactory<String, ExchangeEvent> exchangeProducerFactory = config.producerFactory();
        KafkaTemplate<String, ExchangeEvent> exchangeTemplate = config.kafkaTemplate();
        ProducerFactory<String, BookCommandEvent> commandProducerFactory = config.bookCommandProducerFactory();
        KafkaTemplate<String, BookCommandEvent> commandTemplate = config.bookCommandKafkaTemplate();

        assertNotNull(exchangeProducerFactory);
        assertNotNull(exchangeTemplate);
        assertNotNull(commandProducerFactory);
        assertNotNull(commandTemplate);
    }
}
