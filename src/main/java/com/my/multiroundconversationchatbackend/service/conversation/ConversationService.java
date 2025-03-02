package com.my.multiroundconversationchatbackend.service.conversation;

import com.my.multiroundconversationchatbackend.constamt.PromptConstant;
import com.my.multiroundconversationchatbackend.model.entity.DialogueRecord;
import com.my.multiroundconversationchatbackend.model.entity.Message;
import com.my.multiroundconversationchatbackend.service.chatmodels.ChatModelManager;
import com.my.multiroundconversationchatbackend.service.prompt.PromptBuilderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lihaixu
 * @date 2025年03月02日 16:25
 */
@Slf4j
@Service
public class ConversationService {

    @Autowired
    private PromptBuilderService promptBuilder;

    @Autowired
    private ChatModelManager chatModelManager;

    // 对话窗口大小
    private static final int WINDOW_SIZE = 10;
    // 时间衰减系数
    private static final double DECAY_FACTOR = 0.1;

    // 存储对话历史
    private List<DialogueRecord> dialogueHistory = new ArrayList<>();


    //todo 对于1、2、 3、5可以使用模版方法
    public String doChat(Message message) {
        // 1. 对话窗口管理
        manageDialogueWindow(message);

        // 2. 计算注意力权重
        Map<Integer, Double> attentionWeights = calculateAttentionWeights();

//
//        String context = buildContext(attentionWeights);
        log.info("dialogueHistory: {}", dialogueHistory);
        log.info("Attention weights: {}", attentionWeights);
        log.info("message: {}", message);

        // 3.构建提示词
        String prompt = promptBuilder.buildPrompt(
                dialogueHistory,
                attentionWeights,
                message.getContent()
        );

        // 4.调用语言模型
        String response = chatModelManager.get("dev").generateResponse(PromptConstant.SYSTEM_PROMPT,prompt);

        // 5. 更新对话历史
        updateDialogueHistory(response);

        return response;
    }


    public void doStreamChat(Message message, HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        PrintWriter writer = response.getWriter();

        try {
            // 1. 对话窗口管理
            manageDialogueWindow(message);

            // 2. 计算注意力权重
            Map<Integer, Double> attentionWeights = calculateAttentionWeights();

//
//        String context = buildContext(attentionWeights);
            log.info("dialogueHistory: {}", dialogueHistory);
            log.info("Attention weights: {}", attentionWeights);
            log.info("message: {}", message);

            // 3.构建提示词
            String prompt = promptBuilder.buildPrompt(
                    dialogueHistory,
                    attentionWeights,
                    message.getContent()
            );

            chatModelManager.get("dev").generateStreamResponse(
                    PromptConstant.SYSTEM_PROMPT,
                    prompt,
                    writer
            );

            //todo 如何在流式输出结束后更新对话历史？
//            // 5. 更新对话历史
//            updateDialogueHistory(response);
        } catch (Exception e) {
            log.error("Stream chat error", e);
            writer.write("data: Error: " + e.getMessage() + "\n\n");
            writer.flush();
        }
    }



    private void manageDialogueWindow(Message message) {
        // 添加新的对话记录
        DialogueRecord newRecord = new DialogueRecord(
                message.getContent(),
                "user",
                System.currentTimeMillis(),
                dialogueHistory.size()
        );
        dialogueHistory.add(newRecord);

        // 如果超出窗口大小，移除最早的记录
        while (dialogueHistory.size() > WINDOW_SIZE) {
            dialogueHistory.remove(0);
        }
    }

    private Map<Integer, Double> calculateAttentionWeights() {
        Map<Integer, Double> weights = new HashMap<>();
        int currentTurn = dialogueHistory.size() - 1;

        for (DialogueRecord record : dialogueHistory) {
            // 计算时间衰减权重
            double weight = Math.exp(-DECAY_FACTOR * (currentTurn - record.getTurnIndex()));
            weights.put(record.getTurnIndex(), weight);
        }

        // 归一化权重
        double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        weights.replaceAll((k, v) -> v / sum);

        return weights;
    }

    private String buildContext(Map<Integer, Double> weights) {
        StringBuilder context = new StringBuilder();

        for (DialogueRecord record : dialogueHistory) {
            double weight = weights.get(record.getTurnIndex());
            // 添加对话标记和角色信息
            context.append("<s>")
                    .append(record.getRole())
                    .append(": ")
                    .append(record.getContent())
                    .append("</s>")
                    .append(" [weight: ")
                    .append(String.format("%.2f", weight))
                    .append("]\n");
        }

        return context.toString();
    }

    private String generateResponse(String context) {
        // 这里应该集成具体的大语言模型API调用
        // 示例实现
        return "";
    }

    private void updateDialogueHistory(String response) {
        DialogueRecord systemResponse = new DialogueRecord(
                response,
                "system",
                System.currentTimeMillis(),
                dialogueHistory.size()
        );
        dialogueHistory.add(systemResponse);
    }
}
