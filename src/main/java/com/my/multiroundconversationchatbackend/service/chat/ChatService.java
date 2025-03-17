package com.my.multiroundconversationchatbackend.service.chat;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.my.multiroundconversationchatbackend.model.entity.Message;
import com.my.multiroundconversationchatbackend.model.entity.MessageBody;
import com.my.multiroundconversationchatbackend.repository.ChatRepository;
import com.my.multiroundconversationchatbackend.service.chat.conversation.SemanticConversationService;
import com.my.multiroundconversationchatbackend.utils.CounterManager;
import com.my.multiroundconversationchatbackend.utils.DialogHistoryManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ChatService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private ChatRepository chatRepository;

    /**
     * 通过id查询聊天信息
     *
     * @param id
     * @param httpSession
     * @return
     */
    public MessageBody getOneById(String id, HttpSession httpSession) {
        MessageBody messageBody = (MessageBody) redisTemplate.opsForValue().get("chat:cache:id:" + id);
        if (messageBody == null) {
            Query querydb = new Query();
            querydb.addCriteria(Criteria.where("Id").is(id).and("isDeleted").is(0));
            messageBody = mongoTemplate.findOne(querydb, MessageBody.class);
            redisTemplate.opsForValue().set("chat:cache:id:" + id, messageBody);
        }

        //填充历史记录，便于提问回顾上下文
        fillDialogHistory(messageBody, httpSession);
        return messageBody;
    }


    public MessageBody save(MessageBody messageBody) {
        // 1. 首先获取已存在的记录
        MessageBody existingMessage = chatRepository.findById(messageBody.getId())
                .orElse(null);

        if (existingMessage != null) {
            // 2. 获取新消息并追加到现有消息列表
            List<Message> existingMessages = existingMessage.getMessages();
            List<Message> newMessages = messageBody.getMessages();
            existingMessages.addAll(newMessages);

            // 3. 更新记录
            existingMessage.setMessages(existingMessages);
            // 4. 如果是第一条消息，设置为标题
            if (existingMessage.getTitle() == null || existingMessage.getTitle().isEmpty()) {
                existingMessage.setTitle(newMessages.get(0).getContent());
            }
            existingMessage.setCreateTime(new Date());
            existingMessage.setUpdateTime(new Date());
            // 5. 保存更新后的记录
            MessageBody updated = chatRepository.save(existingMessage);

            // 6. 清除缓存
            if (updated != null) {
                redisTemplate.delete("chat:cache:id:" + messageBody.getId());
            }

            return updated;
        } else {
            // 如果记录不存在，创建新记录
            messageBody.setTitle(messageBody.getMessages().get(0).getContent());
            return chatRepository.save(messageBody);
        }
    }

    public Boolean updateById(String id, String title) {

        Query query = new Query(Criteria.where("id").is(id).and("isDeleted").is(0));
        Update update = new Update()
                .set("title", title)
                .set("updateTime", new Date());
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, MessageBody.class);
        log.info("更新条数：{}", updateResult.getMatchedCount());
        if (updateResult.getMatchedCount() > 0) {
            redisTemplate.delete("chat:cache:id:" + id);
        }
        return true;
    }


    public List<MessageBody> getListById(Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId).and("isDeleted").is(0));
        List<MessageBody> messageBodyList = mongoTemplate.find(query, MessageBody.class);
        Collections.reverse(messageBodyList);
        return messageBodyList;
    }


    public Boolean logicDeleteById(String id, HttpSession httpSession) {

        clearDialogHistory(httpSession);

        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update()
                .set("isDeleted", 1)
                .set("updateTime", new Date());

        UpdateResult result = mongoTemplate.updateFirst(query, update, MessageBody.class);
        return result.getModifiedCount() > 0;

    }

    public void fillDialogHistory(MessageBody messageBody, HttpSession httpSession) {

        //清除历史信息
        clearDialogHistory(httpSession);

        //将查询到的记录进行字段映射，只需要存消息内容就好
        List<Message> messageList = messageBody.getMessages();
        int size = messageList.size();


        int recentMessageCount = 2 * DialogHistoryManager.MAX_HISTORY_SIZE;
        // 检查确保 messageList 至少有 6 个元素

        List<Message> recentMessages = null;
        if (size >= recentMessageCount) {
            recentMessages = messageList.subList(size - recentMessageCount, size);
        } else {
            // 处理 messageList 元素不足 6 的情况
            recentMessages = messageList; // 或者处理为其他逻辑
        }

        //取后2*MAX_DIALOG_SIZE,然后映射其内容到DialogRecord的query和response字段
        //将recentMessage中俩俩拆开分为各组，一组中的前一个content值赋值到dialogrecord的query字段，后一个赋值到response字段
        for (int i = 0; i < recentMessages.size(); i += 2) {
            if (i + 1 < recentMessages.size()) { // 确保还有一个元素
                // 将前一个 content 赋值给 query，后一个赋值给 response
                String query = recentMessages.get(i).getContent();
                String response = recentMessages.get(i + 1).getContent();

                SemanticConversationService.updateDialogueHistory(query, response, httpSession);
            }
        }
        //添加历史上记录数据
        //计算必要的数据
        SemanticConversationService.calculateCombinedWeights(httpSession);
    }


    public void clearDialogHistory(HttpSession httpSession) {
        //清除问答记录
        DialogHistoryManager.clear(httpSession);
        //清除计数器
        CounterManager.clear(httpSession);
    }


    /**
     * 批量逻辑删除
     *
     * @param ids ID列表
     * @return 删除成功的数量
     */
    public long batchLogicDeleteByIds(List<String> ids) {
        Query query = new Query(Criteria.where("_id").in(ids));
        Update update = new Update()
                .set("isDeleted", 1)
                .set("updateTime", new Date());

        UpdateResult result = mongoTemplate.updateMulti(query, update, MessageBody.class);
        return result.getModifiedCount();
    }

    /**
     * 根据用户ID逻辑删除
     *
     * @param userId 用户ID
     * @return 删除成功的数量
     */
    public long deleteByUserId(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("isDeleted").is(0));
        Update update = new Update()
                .set("isDeleted", 1)
                .set("updateTime", new Date());

        UpdateResult result = mongoTemplate.updateMulti(query, update, MessageBody.class);
        return result.getModifiedCount();
    }

    /**
     * 物理删除
     *
     * @param id 记录ID
     * @return 是否删除成功
     */
    public boolean physicalDelete(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        DeleteResult result = mongoTemplate.remove(query, MessageBody.class);
        return result.getDeletedCount() > 0;
    }
}
