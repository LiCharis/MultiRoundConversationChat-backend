package com.my.knowledgegraphchatbackend.service.Impl;

import com.mongodb.client.result.UpdateResult;
import com.my.knowledgegraphchatbackend.common.ErrorCode;
import com.my.knowledgegraphchatbackend.exception.BusinessException;
import com.my.knowledgegraphchatbackend.model.dto.ChatUpdateRequest;
import com.my.knowledgegraphchatbackend.model.entity.MessageBody;
import com.my.knowledgegraphchatbackend.repository.ChatRepository;
import com.my.knowledgegraphchatbackend.service.ChatService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private ChatRepository chatRepository;

    @Override
    public MessageBody getOneById(String id) {
        //如果redis中有缓存记录，就返回缓存中的信息
        MessageBody redisOne = (MessageBody) redisTemplate.opsForValue().get(id);
        if (redisOne != null){
            return redisOne;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("Id").is(id));
        MessageBody one = mongoTemplate.findOne(query, MessageBody.class);
        //保存到缓存缓存中
        redisTemplate.opsForValue().set(id,one);
        return one;
    }

    @Override
    public MessageBody save(MessageBody messageBody) {
        String content = messageBody.getMessages().get(0).getContent();
        messageBody.setTitle(content);
        MessageBody save = chatRepository.save(messageBody);
        //新的记录或者每次记录消息条数更新都重新保存到redis中
        redisTemplate.opsForValue().set(save.getId(),save);
        return save;
    }

    @Override
    public Boolean updateById(String id,String title) {

        Query query= new Query(Criteria.where("id").is(id));
        Update update= new Update()
                .set("title",title);
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, MessageBody.class);

        System.out.println("更新条数：" + updateResult.getMatchedCount());
        //删除之前保存在redis中的信息
        redisTemplate.delete(id);
        return true;
    }

    @Override
    public List<MessageBody> getListById(Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        List<MessageBody> messageBodyList = mongoTemplate.find(query, MessageBody.class);
        Collections.reverse(messageBodyList);
        return messageBodyList;
    }

    @Override
    public Boolean deleteById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        MessageBody result = mongoTemplate.findAndRemove(query, MessageBody.class);
        return true;
    }
}
