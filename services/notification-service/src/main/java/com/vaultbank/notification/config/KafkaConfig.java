package com.vaultbank.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic moneyDepositedTopic() {
        return TopicBuilder.name("money.deposited")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic moneyWithdrawnTopic() {
        return TopicBuilder.name("money.withdrawn")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic moneyTransferredTopic() {
        return TopicBuilder.name("money.transferred")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
