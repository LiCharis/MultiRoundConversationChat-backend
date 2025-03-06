package com.my.multiroundconversationchatbackend.controller;

import com.my.multiroundconversationchatbackend.common.*;
import com.my.multiroundconversationchatbackend.exception.BusinessException;
import com.my.multiroundconversationchatbackend.exception.ThrowUtils;
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
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addChat(@RequestBody MessageBody messageBody, HttpServletRequest request) {


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
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChat(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = chatService.logicDeleteById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateChat(@RequestBody ChatUpdateRequest chatUpdateRequest, HttpServletRequest request) {
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

        //清除问答记录
        DialogHistoryManager.clear(httpSession);
        //清除计数器
        CounterManager.clear(httpSession);
        MessageBody one = chatService.getOneById(getRequest.getId(), httpSession);

        if (one == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "数据不存在");
        }
        return ResultUtils.success(one);

    }

//    @PostMapping("/getStreamRes")
//    public void getRes(@RequestBody Message message, HttpServletResponse response) throws IOException {
//        log.info("Input content: {}", message.getContent());
//        conversationService.doStreamChat(message, response);
//    }

    @PostMapping("/getRes")
    public BaseResponse<String> getRes(@RequestBody Message message, HttpSession httpSession, HttpServletRequest request) {
        log.info("Input content: {}", message.getContent());

        // 判断会话状态
        boolean isNewSession = httpSession.isNew();
        String sessionId = httpSession.getId();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("SESSION".equals(cookie.getName())) {
                    // 比较 cookie 中的会话 ID 和当前会话 ID
                    boolean isSameSession = sessionId.equals(cookie.getValue());
                    log.info("Cookie session matches current session: {}", isSameSession);
                    break;
                }
            }
        }

        log.info("cookie: {}", httpSession.getAttribute("Cookie"));

//        log.info("Model: {}",message.);
        String res = semanticConversationService.doChat(message, httpSession);
        log.info("Res: {}", res);

        return ResultUtils.success(res);
    }

}
