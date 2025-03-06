package com.my.multiroundconversationchatbackend;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMethodCache(basePackages = "com.my.multiroundconversationchatbackend")
@EnableMongoAuditing
@SpringBootApplication
public class MultiRoundConversationChatBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiRoundConversationChatBackendApplication.class, args);
    }

}
