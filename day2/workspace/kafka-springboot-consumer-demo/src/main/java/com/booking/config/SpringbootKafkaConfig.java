package com.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Properties;

@Configuration
public class SpringbootKafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template){
        FixedBackOff fixedBackOff = new FixedBackOff(2000, 3);
        return new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(template),
                fixedBackOff
        );
    }
}
