package com.my.knowledgegraphchatbackend.controller;

import com.my.knowledgegraphchatbackend.common.*;
import com.my.knowledgegraphchatbackend.exception.BusinessException;
import com.my.knowledgegraphchatbackend.exception.ThrowUtils;
import com.my.knowledgegraphchatbackend.model.dto.ChatUpdateRequest;
import com.my.knowledgegraphchatbackend.model.entity.MessageBody;

import com.my.knowledgegraphchatbackend.service.ChatService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    /**
     * 创建
     *
     * @param
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addChat(@RequestBody MessageBody messageBody, HttpServletRequest request) {


        if (messageBody== null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
       ThrowUtils.throwIf( ObjectUtils.isEmpty(messageBody.getId()) && ObjectUtils.isEmpty(messageBody.getUserId()), ErrorCode.OPERATION_ERROR);

        if (messageBody.getMessages().size() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"您还没有开始对话哦");
        }

        MessageBody save = chatService.save(messageBody);
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
        Boolean result = chatService.deleteById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateChat(@RequestBody ChatUpdateRequest chatUpdateRequest,HttpServletRequest request){
        if (chatUpdateRequest == null || chatUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String id = chatUpdateRequest.getId();
        String title = chatUpdateRequest.getTitle();
        if (StringUtils.isAnyEmpty(title)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标题无变化");
        }
        Boolean aBoolean = chatService.updateById(id,title);

        return ResultUtils.success(aBoolean);
    }


    @PostMapping("/getHistoryList")
    public BaseResponse<List<MessageBody>> getChatByUserId(@RequestBody GetRequest getRequest){
        if (getRequest == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<MessageBody> messageBodyList = chatService.getListById(getRequest.getUserId());

        return ResultUtils.success(messageBodyList);
    }

    @PostMapping("/getOne")
    public BaseResponse<MessageBody> getChatById(@RequestBody GetRequest getRequest){
        if (getRequest == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        MessageBody one = chatService.getOneById(getRequest.getId());
        if (one == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"数据不存在");
        }
        return ResultUtils.success(one);

    }


}
