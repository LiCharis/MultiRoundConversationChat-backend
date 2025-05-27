package com.my.multiroundconversationchatbackend.service.chat.conversation;

import com.my.multiroundconversationchatbackend.model.entity.DialogueContext;
import com.my.multiroundconversationchatbackend.model.entity.DialogueRecord;
import com.my.multiroundconversationchatbackend.model.entity.chatReference.ChatReference;
import com.my.multiroundconversationchatbackend.service.chatReference.service.ChatReferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lihaixu
 * @date 2025年03月22日 21:12
 * 提示词构建器 - 负责构建优化的提示词
 */
@Order(3)
@Service
@Slf4j
public class PromptBuilder implements DialogueProcessor {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private ChatReferenceService chatReferenceService;

    @Override
    public DialogueContext process(DialogueContext context) {
        String optimizedPrompt = buildOptimizedPrompt(context);
        context.setAttribute("optimizedPrompt", optimizedPrompt);
        log.debug("Built optimized prompt of length: {}", optimizedPrompt.length());
        return context;
    }

    /**
     * 构建优化的提示词
     */
    public String buildOptimizedPrompt(DialogueContext context) {
        String query = context.getQuery();
        List<DialogueRecord> history = context.getHistory();

        StringBuilder prompt = new StringBuilder();

        // 系统指令
        prompt.append("# System Instruction\n");
        prompt.append("You are a helpful assistant engaged in a multi-turn conversation. ");
        prompt.append("Maintain context awareness and provide concise, relevant responses.\n\n");

        // 空历史处理
        if (history.isEmpty()) {
            prompt.append("# Current Query\n");
            prompt.append(query).append("\n\n");
            return prompt.toString();
        }

        // 对话摘要部分
        prompt.append("# Conversation Summary\n");

        // 找出相关性最高的历史记录
        List<DialogueRecord> relevantHistory = getRelevantHistory(query, history);

        for (DialogueRecord record : relevantHistory) {
            prompt.append(String.format("Turn %d (Weight: %.2f):\n",
                    record.getTurnIndex(), record.getWeight()));

            Set<String> keywords = record.getSemanticFeature().getKeywords();
            if (keywords != null && !keywords.isEmpty()) {
                prompt.append("Keywords: ")
                        .append(String.join(", ", keywords))
                        .append("\n");
            }

            prompt.append("User: ").append(record.getQuery()).append("\n");
            prompt.append("Assistant: ")
                    .append(summarizeContent(record.getResponse()))
                    .append("\n\n");
        }

        // 主题分析
        Map<String, Double> globalTopics = mergeTopics(history);
        if (!globalTopics.isEmpty()) {
            prompt.append("# Main Topics\n");
            globalTopics.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry ->
                            prompt.append(String.format("- %s: %.2f\n",
                                    entry.getKey(), entry.getValue()))
                    );
            prompt.append("\n");
        }

        // 当前查询
        prompt.append("# Current Query\n");
        prompt.append(query).append("\n\n");

        // 响应指南
        prompt.append("# Response Guidelines\n");
        prompt.append("1. Address the query directly\n");
        prompt.append("2. Maintain consistency with previous context\n");
        prompt.append("3. Build upon established concepts\n");
        prompt.append("4. Be concise and focused\n");

        return prompt.toString();
    }

    /**
     * 获取与当前查询相关的历史记录
     */
    private List<DialogueRecord> getRelevantHistory(String query, List<DialogueRecord> history) {
        // 如果历史较少，直接返回全部
        if (history.size() <= 5) {
            return new ArrayList<>(history);
        }

        // 计算当前查询的嵌入向量
        float[] queryEmbedding = embeddingService.getEmbedding(query);
        //当前用户参数设置
        ChatReference chatReference = chatReferenceService.findChatReferenceByUserId(1L);

        // 创建包含相似度信息的临时列表
        List<Map.Entry<DialogueRecord, Double>> scoredRecords = new ArrayList<>();

        // 计算每条历史记录的相似度分数
        for (int i = 0; i < history.size(); i++) {
            DialogueRecord record = history.get(i);

            // 时间因子 - 越新的记录权重越高
            double timeFactor = Math.exp(-0.1 * (history.size() - 1 - i));

            // 语义相似度
            double similarity = 0.5; // 默认值
            if (record.getSemanticFeature() != null &&
                    record.getSemanticFeature().getEmbedding() != null) {
                similarity = embeddingService.calculateSimilarity(
                        queryEmbedding, record.getSemanticFeature().getEmbedding());
            }

            // 综合评分
            double score = chatReference.getSimilarityWeight() * similarity + chatReference.getTimeFactorWeight() * timeFactor;

            scoredRecords.add(new AbstractMap.SimpleEntry<>(record, score));
        }

        // 按分数排序并返回前5条
        return scoredRecords.stream()
                .sorted(Map.Entry.<DialogueRecord, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 对内容进行摘要
     */
    private String summarizeContent(String content) {
        if (content == null || content.length() <= 100) {
            return content;
        }

        // 使用简单的摘要方法 - 实际中可以使用更复杂的算法
        List<String> sentences = Arrays.asList(content.split("(?<=[.!?])\\s+"));
        if (sentences.size() <= 2) {
            return content;
        }

        // 取前两句
        return sentences.stream()
                .limit(2)
                .collect(Collectors.joining(" ")) + "...";
    }

    /**
     * 合并主题分布
     */
    private Map<String, Double> mergeTopics(List<DialogueRecord> history) {
        Map<String, Double> mergedTopics = new HashMap<>();

        for (DialogueRecord record : history) {
            if (record.getSemanticFeature() == null ||
                    record.getSemanticFeature().getTopics() == null) {
                continue;
            }

            double weight = record.getWeight();
            record.getSemanticFeature().getTopics().forEach((topic, value) ->
                    mergedTopics.merge(topic, value * weight, Double::sum)
            );
        }

        return mergedTopics;
    }
}

