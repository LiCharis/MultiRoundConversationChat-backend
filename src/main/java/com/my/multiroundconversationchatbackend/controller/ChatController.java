package com.my.multiroundconversationchatbackend.controller;

import com.my.multiroundconversationchatbackend.common.*;
import com.my.multiroundconversationchatbackend.exception.BusinessException;
import com.my.multiroundconversationchatbackend.exception.ThrowUtils;
import com.my.multiroundconversationchatbackend.model.dto.ChatRequest;
import com.my.multiroundconversationchatbackend.model.dto.ChatUpdateRequest;
import com.my.multiroundconversationchatbackend.model.entity.Message;
import com.my.multiroundconversationchatbackend.model.entity.MessageBody;

import com.my.multiroundconversationchatbackend.service.ChatService;
import com.my.multiroundconversationchatbackend.service.conversation.SemanticConversationService;

import com.my.multiroundconversationchatbackend.utils.CounterManager;
import com.my.multiroundconversationchatbackend.utils.DialogHistoryManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    @Autowired
    private SemanticConversationService semanticConversationService;

    /**
     * 创建
     *
     * @param
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addChat(@RequestBody MessageBody messageBody) {


        if (messageBody == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ThrowUtils.throwIf(ObjectUtils.isEmpty(messageBody.getId()) && ObjectUtils.isEmpty(messageBody.getUserId()), ErrorCode.OPERATION_ERROR);

        if (messageBody.getMessages().size() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您还没有开始对话哦");
        }

        MessageBody save = chatService.save(messageBody);
        if (save == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChat(@RequestBody DeleteRequest deleteRequest, HttpSession httpSession) {
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = chatService.logicDeleteById(deleteRequest.getId(), httpSession);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateChat(@RequestBody ChatUpdateRequest chatUpdateRequest) {
        if (chatUpdateRequest == null || chatUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String id = chatUpdateRequest.getId();
        String title = chatUpdateRequest.getTitle();
        if (StringUtils.isAnyEmpty(title)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题无变化");
        }
        Boolean aBoolean = chatService.updateById(id, title);

        return ResultUtils.success(aBoolean);
    }


    @PostMapping("/getHistoryList")
    public BaseResponse<List<MessageBody>> getChatByUserId(@RequestBody GetRequest getRequest) {
        if (getRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<MessageBody> messageBodyList = chatService.getListById(getRequest.getUserId());

        return ResultUtils.success(messageBodyList);
    }

    @PostMapping("/getOne")
    public BaseResponse<MessageBody> getChatById(@RequestBody GetRequest getRequest, HttpSession httpSession) {
        if (getRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        MessageBody one = chatService.getOneById(getRequest.getId(), httpSession);

        if (one == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "数据不存在");
        }
        return ResultUtils.success(one);

    }

    @PostMapping("/clearHistory")
    public BaseResponse<Boolean> clearChat(HttpSession httpSession) {
        chatService.clearDialogHistory(httpSession);
        return ResultUtils.success(true);
    }

//    @PostMapping("/getStreamRes")
//    public void getRes(@RequestBody Message message, HttpServletResponse response) throws IOException {
//        log.info("Input content: {}", message.getContent());
//        conversationService.doStreamChat(message, response);
//    }

    @PostMapping("/getRes")
    public BaseResponse<String> getRes(@RequestBody ChatRequest chatRequest, HttpSession httpSession) {
        if (chatRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String model = chatRequest.getModel();
        String content = chatRequest.getMessages().getContent();
        if (StringUtils.isEmpty(model)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isEmpty(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        log.info("choose model: {}", model);
        log.info("input content: {}", content);
        String res = semanticConversationService.doChat(chatRequest.getMessages(), model, httpSession);
        log.info("Res: {}", res);

        return ResultUtils.success(res);
    }

}
