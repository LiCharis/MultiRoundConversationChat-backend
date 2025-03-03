//package com.my.multiroundconversationchatbackend.service.conversation;
//
//import com.my.multiroundconversationchatbackend.constamt.PromptConstant;
//import com.my.multiroundconversationchatbackend.model.entity.DialogueRecord;
//import com.my.multiroundconversationchatbackend.model.entity.Message;
//import com.my.multiroundconversationchatbackend.service.chatmodels.ChatModelManager;
//import com.my.multiroundconversationchatbackend.service.prompt.PromptBuilderService;
//import com.my.multiroundconversationchatbackend.utils.DialogHistoryThreadLocal;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author lihaixu
// * @date 2025年03月02日 16:25
// */
//@Slf4j
//@Service
//public class ConversationService {
//
//    @Autowired
//    private PromptBuilderService promptBuilder;
//
//    @Autowired
//    private ChatModelManager chatModelManager;
//
//    // 对话窗口大小
//    private static final int WINDOW_SIZE = 10;
//    // 时间衰减系数
//    private static final double DECAY_FACTOR = 0.1;
//    //历史对话记录
//    private static final List<DialogueRecord> DIALOGUE_RECORDS = DialogHistoryThreadLocal.getHistory();
//
//
//    //todo 对于1、2、 3、5可以使用模版方法
//    public String doChat(Message message) {
////
////        // 1. 对话窗口管理
////        manageDialogueWindow(message);
////
////        // 2. 计算注意力权重
////        Map<Integer, Double> attentionWeights = calculateAttentionWeights();
////
//////
//////        String context = buildContext(attentionWeights);
////        log.info("dialogueHistory: {}",DialogHistoryThreadLocal.getHistory());
////        log.info("Attention weights: {}", attentionWeights);
////        log.info("message: {}", message);
////
////        // 3.构建提示词
////        String prompt = promptBuilder.buildPrompt(
////                DIALOGUE_RECORDS,
////                attentionWeights,
////                message.getContent()
////        );
////
////        // 4.调用语言模型
////        String response = chatModelManager.get("dev").generateResponse(PromptConstant.SYSTEM_PROMPT,prompt);
////
////        // 5. 更新对话历史
////        updateDialogueHistory(response);
//
//        return response;
//    }
//
//
//    public void doStreamChat(Message message, HttpServletResponse response) throws IOException {
//        response.setContentType("text/event-stream");
//        response.setCharacterEncoding("UTF-8");
//        response.setHeader("Cache-Control", "no-cache");
//        response.setHeader("Connection", "keep-alive");
//
//        PrintWriter writer = response.getWriter();
//
//        try {
//            // 1. 对话窗口管理
//            manageDialogueWindow(message);
//
//            // 2. 计算注意力权重
//            Map<Integer, Double> attentionWeights = calculateAttentionWeights();
//
////
////        String context = buildContext(attentionWeights);
//            log.info("dialogueHistory: {}", DIALOGUE_RECORDS);
//            log.info("Attention weights: {}", attentionWeights);
//            log.info("message: {}", message);
//
//            // 3.构建提示词
//            String prompt = promptBuilder.buildPrompt(
//                    DIALOGUE_RECORDS,
//                    attentionWeights,
//                    message.getContent()
//            );
//
//            chatModelManager.get("dev").generateStreamResponse(
//                    PromptConstant.SYSTEM_PROMPT,
//                    prompt,
//                    writer
//            );
//
//            //todo 如何在流式输出结束后更新对话历史？
////            // 5. 更新对话历史
////            updateDialogueHistory(response);
//        } catch (Exception e) {
//            log.error("Stream chat error", e);
//            writer.write("data: Error: " + e.getMessage() + "\n\n");
//            writer.flush();
//        }
//    }
//
//}
