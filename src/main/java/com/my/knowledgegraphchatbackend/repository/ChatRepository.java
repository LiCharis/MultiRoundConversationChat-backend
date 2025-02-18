package com.my.knowledgegraphchatbackend.repository;

import com.my.knowledgegraphchatbackend.model.entity.MessageBody;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ChatRepository extends MongoRepository<MessageBody,String> {
    @Query("{'userId': ?0}")
    List<MessageBody> findAllByUserId(long userId, Pageable pageable);


}
