package com.my.multiroundconversationchatbackend.service.chat.conversation;

import com.my.multiroundconversationchatbackend.model.entity.DialogueContext;
import com.my.multiroundconversationchatbackend.model.entity.DialogueRecord;
import com.my.multiroundconversationchatbackend.model.entity.SemanticFeature;
import com.my.multiroundconversationchatbackend.model.entity.chatReference.ChatReference;
import com.my.multiroundconversationchatbackend.service.chatReference.service.ChatReferenceService;
import com.my.multiroundconversationchatbackend.utils.DialogHistoryManager;
import com.my.multiroundconversationchatbackend.utils.SemanticUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


/**
 * @author lihaixu
 * @date 2025年03月22日 21:11
 * 语义处理器 - 负责处理语义特征和权重计算
 */
@Order(2)
@Service
@Slf4j
public class SemanticProcessor implements DialogueProcessor {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private TaskExecutor myTaskExecutor;

    @Autowired
    private ChatReferenceService chatReferenceService;

    // 权重配置
    private static final double DECAY_FACTOR = 0.1;

    @Override
    public DialogueContext process(DialogueContext context) {
        String currentQuery = context.getQuery();
        HttpSession session = context.getSession();


        // Feature计算
        calculateFeature(context);

        // 重新计算历史权重
        List<DialogueRecord> history = DialogHistoryManager.getHistory(session);
        if (!history.isEmpty()) {
            calculateCombinedWeights(currentQuery,session);
        }
        return context;
    }

    /**
     * Feature计算
     */
    public void calculateFeature(DialogueContext context){
        String currentQuery = context.getQuery();

        CompletableFuture<Set<String>> keywordsFuture = CompletableFuture.supplyAsync(
                () -> SemanticUtils.extractKeywords(currentQuery), myTaskExecutor);

        CompletableFuture<Map<String, Double>> topicsFuture = CompletableFuture.supplyAsync(
                () -> SemanticUtils.calculateTopicDistribution(currentQuery), myTaskExecutor);

        CompletableFuture<float[]> embeddingFuture = CompletableFuture.supplyAsync(
                () -> embeddingService.getEmbedding(currentQuery), myTaskExecutor);

        CompletableFuture<Double> frequencyFuture = CompletableFuture.supplyAsync(
                ()->SemanticUtils.calculateTermFrequency(currentQuery), myTaskExecutor);

        CompletableFuture<Double> coherenceScoreFuture = CompletableFuture.supplyAsync(
                () -> SemanticUtils.calculateCoherence(SemanticUtils.calculateTopicDistribution(currentQuery)),myTaskExecutor);

        CompletableFuture.allOf(keywordsFuture, topicsFuture, embeddingFuture, frequencyFuture, coherenceScoreFuture);

        // 生成语义特征
        // 等待所有特征计算完成并组合结果
        SemanticFeature feature = null;
        try {
            feature = SemanticFeature.builder()
                    .keywords(keywordsFuture.get())
                    .topics(topicsFuture.get())
                    .embedding(embeddingFuture.get())
                    .frequency(frequencyFuture.get())
                    .coherenceScore(coherenceScoreFuture.get())
                    .build();
        } catch (Exception e) {
            log.error("Error in parallel semantic processing", e);
            // 降级处理 - 顺序计算
            feature = SemanticFeature.fromContent(currentQuery, embeddingService);
        }
        context.setAttribute("semanticFeature", feature);
    }


    /**
     * 计算综合权重
     */
    @Async("myTaskExecutor")
    public void calculateCombinedWeights(String currentQuery, HttpSession session) {
        ChatReference chatReference = chatReferenceService.findChatReferenceByUserId(1L);
        Map<Integer, Double> weights = new HashMap<>();
        List<DialogueRecord> history = DialogHistoryManager.getHistory(session);
        int size = history.size();

        if (size == 0) return;

        // 获取当前回合
        int currentTurn = history.get(size - 1).getTurnIndex() + 1;

        // 计算当前查询的嵌入向量
        float[] queryEmbedding = embeddingService.getEmbedding(currentQuery);

        history.forEach(record -> {
            // 1. 时间衰减权重
            double timeWeight = Math.exp(-DECAY_FACTOR * (currentTurn - record.getTurnIndex()));

            // 2. 频率权重
            SemanticFeature feature = record.getSemanticFeature();
            double freqWeight = feature.getFrequency();

            // 3. 语义相似度
            double semanticSimilarity = 0.5; // 默认值
            if (feature.getEmbedding() != null) {
                semanticSimilarity = embeddingService.calculateSimilarity(
                        queryEmbedding, feature.getEmbedding());
            }

            // 4. 计算综合权重
            double combinedWeight =
                    chatReference.getTimeWeight() * timeWeight +
                            chatReference.getFrequencyWeight() * freqWeight +
                            chatReference.getSemanticWeight() * semanticSimilarity;

            weights.put(record.getTurnIndex(), combinedWeight);
        });

        // 归一化权重
        normalizeWeights(weights);

        // 更新权重
        history.forEach(record ->
                record.setWeight(weights.get(record.getTurnIndex())));

        log.debug("Updated weights for {} dialogue records", history.size());
    }

    /**
     * 归一化权重
     */
    private void normalizeWeights(Map<Integer, Double> weights) {
        double sum = weights.values().stream()
                .mapToDouble(Double::doubleValue).sum();
        if (sum > 0) {
            weights.replaceAll((k, v) -> v / sum);
        }
    }
}

