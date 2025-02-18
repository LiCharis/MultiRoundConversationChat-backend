package com.my.knowledgegraphchatbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;

@SpringBootApplication
public class KnowledgeGraphChatBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeGraphChatBackendApplication.class, args);
    }

}
