package com.my.multiroundconversationchatbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@SpringBootApplication
public class MultiRoundConversationChatBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiRoundConversationChatBackendApplication.class, args);
    }

}
