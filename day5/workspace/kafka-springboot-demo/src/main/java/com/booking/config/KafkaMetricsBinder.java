package com.booking.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class KafkaMetricsBinder {

    private final DefaultKafkaProducerFactory<?, ?> producerFactory;
    private final DefaultKafkaConsumerFactory<?, ?> consumerFactory;
    private final MeterRegistry meterRegistry;

    public KafkaMetricsBinder(DefaultKafkaProducerFactory<?, ?> producerFactory,
                              DefaultKafkaConsumerFactory<?, ?> consumerFactory,
                              MeterRegistry meterRegistry) {
        this.producerFactory = producerFactory;
        this.consumerFactory = consumerFactory;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void bindClientMetrics() {
        // create a single producer, bind its client metrics, then close it
        try {
            Producer<?, ?> p = producerFactory.createProducer();
            new KafkaClientMetrics(p).bindTo(meterRegistry);
            p.close();
        } catch (Exception ignored) { }

        // create a single consumer, bind metrics, then close it
        try {
            Consumer<?, ?> c = consumerFactory.createConsumer();
            new KafkaClientMetrics(c).bindTo(meterRegistry);
            c.close();
        } catch (Exception ignored) { }
    }
}

