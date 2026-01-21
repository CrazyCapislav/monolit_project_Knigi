package dev.petr.book.infrastructure.config;

import dev.petr.book.domain.event.BookCommandEvent;
import dev.petr.book.domain.event.BookDomainEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class KafkaConfigTest {

    @Test
    void createsProducerAndConsumerBeans() {
        KafkaConfig config = new KafkaConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");

        ProducerFactory<String, BookDomainEvent> producerFactory = config.bookDomainProducerFactory();
        KafkaTemplate<String, BookDomainEvent> kafkaTemplate = config.bookDomainKafkaTemplate();
        ConsumerFactory<String, BookCommandEvent> consumerFactory = config.bookCommandConsumerFactory();
        ConcurrentKafkaListenerContainerFactory<String, BookCommandEvent> listenerFactory =
                config.bookCommandKafkaListenerContainerFactory();

        assertNotNull(producerFactory);
        assertNotNull(kafkaTemplate);
        assertNotNull(consumerFactory);
        assertNotNull(listenerFactory);
    }
}
