package dev.petr.publication.infrastructure.config;

import dev.petr.publication.domain.event.BookCommandEvent;
import dev.petr.publication.domain.event.BookDomainEvent;
import dev.petr.publication.domain.event.PublicationEvent;
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

        ProducerFactory<String, PublicationEvent> publicationProducerFactory = config.producerFactory();
        KafkaTemplate<String, PublicationEvent> publicationTemplate = config.kafkaTemplate();
        ProducerFactory<String, BookCommandEvent> commandProducerFactory = config.bookCommandProducerFactory();
        KafkaTemplate<String, BookCommandEvent> commandTemplate = config.bookCommandKafkaTemplate();
        ConsumerFactory<String, BookDomainEvent> consumerFactory = config.bookDomainEventConsumerFactory();
        ConcurrentKafkaListenerContainerFactory<String, BookDomainEvent> listenerFactory =
                config.bookDomainEventKafkaListenerContainerFactory();

        assertNotNull(publicationProducerFactory);
        assertNotNull(publicationTemplate);
        assertNotNull(commandProducerFactory);
        assertNotNull(commandTemplate);
        assertNotNull(consumerFactory);
        assertNotNull(listenerFactory);
    }
}
