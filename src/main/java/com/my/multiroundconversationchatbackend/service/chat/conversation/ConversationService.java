package com.my.multiroundconversationchatbackend.service.chat.conversation;

import com.my.multiroundconversationchatbackend.common.ErrorCode;
import com.my.multiroundconversationchatbackend.model.entity.DialogueContext;
import com.my.multiroundconversationchatbackend.model.entity.Message;
import com.my.multiroundconversationchatbackend.service.chat.chatmodels.BaseChatModelService;
import com.my.multiroundconversationchatbackend.service.chat.chatmodels.ChatModelManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author lihaixu
 * @date 2025年03月22日 21:30
 */
@Service
@Slf4j
public class ConversationService {

    @Autowired
    private List<DialogueProcessor> dialogueProcessors;

    @Autowired
    private DialogManager dialogueManager;

    @Autowired
    private ChatModelManager chatModelManager;

    @Autowired
    private TaskExecutor myTaskExecutor;

    /**
     * 处理聊天请求
     */
    public String doChat(Message message, String modelName, HttpSession session) {
        log.info("Processing chat request with model: {}", modelName);

        // 创建对话上下文
        DialogueContext context = DialogueContext.builder()
                .query(message.getContent())
                .session(session)
                .modelName(modelName)
                .build();

        // 按顺序应用所有处理器
        for (DialogueProcessor processor : dialogueProcessors) {
            try {
                context = processor.process(context);
            } catch (Exception e) {
                log.error("Error in processor {}: {}", processor.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        // 模型API调用可以异步
        DialogueContext finalContext = context;
        CompletableFuture<String> responseFuture = CompletableFuture.supplyAsync(() -> {
            // 获取优化后的提示词
            String optimizedPrompt = finalContext.getAttribute("optimizedPrompt");
            // 调用模型生成回答
            BaseChatModelService modelService = chatModelManager.get(modelName);
            try {
                return modelService.doGenerate(optimizedPrompt, "");
            } catch (Exception e) {
                log.error("Error calling model API: {}", e.getMessage(), e);
                return ErrorCode.API_CALL_ERROR.getMessage();
            }
        }, myTaskExecutor);

        // 获取响应并完成流程
        String response;
        try {
            response = responseFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Model API timeout or error", e);
            response = ErrorCode.API_CALL_ERROR.getMessage();
        }

        // 设置响应并更新历史
        context.setResponse(response);
        dialogueManager.updateHistory(context);

         return response;
    }


}
