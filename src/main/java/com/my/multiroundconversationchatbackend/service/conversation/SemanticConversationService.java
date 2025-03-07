package com.my.multiroundconversationchatbackend.service.conversation;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.my.multiroundconversationchatbackend.model.entity.DialogueRecord;
import com.my.multiroundconversationchatbackend.model.entity.Message;
import com.my.multiroundconversationchatbackend.service.chatmodels.ChatModelManager;

import com.my.multiroundconversationchatbackend.utils.CounterManager;
import com.my.multiroundconversationchatbackend.utils.DialogHistoryManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author lihaixu
 * @date 2025年03月03日 12:12
 */
@Service
@Slf4j
public class SemanticConversationService{

    @Autowired
    private ChatModelManager chatModelManager;


    // 语义权重配置
    private static final double TIME_WEIGHT = 0.3;      // 时间权重
    private static final double FREQUENCY_WEIGHT = 0.3; // 频率权重
    private static final double SEMANTIC_WEIGHT = 0.4;  // 语义相关度权重
    private static final int MAX_KEYWORDS = 10;         // 每轮对话最大关键词数量

    // 对话窗口大小
    private static final int WINDOW_SIZE = 10;
    private static final double DECAY_FACTOR = 0.1;

    public String doChat(Message message, HttpSession httpSession) {

        //获取构建好的prompt
        String buildPrompt = processDialogue(message,httpSession);

        //调用接口获取响应
        String response = chatModelManager.get("dev").generateResponse("",buildPrompt);

        //更新对话历史
        updateDialogueHistory(message.getContent(),response,httpSession);

        //重新计算权重
        calculateCombinedWeights(httpSession);
        log.info("DialogHistory {}",DialogHistoryManager.getHistory(httpSession).size());

        return response;
    }

    /**
     * 处理多轮对话
     */
    public String processDialogue(Message message,HttpSession httpSession) {
        //如果是第一次询问，就直接返回
        if (DialogHistoryManager.getHistory(httpSession).isEmpty()) {
            return message.getContent();
        }
        // 1. 管理对话窗口
        manageDialogueWindow(httpSession);

        // 2. 构建优化后的提示词
        return buildOptimizedPrompt(message.getContent(),httpSession);
    }


    private void manageDialogueWindow(HttpSession httpSession) {
        // 如果超出窗口大小，移除最早的记录
        //因此最多可以根据最后 MAX_HISTORY_SIZE-1个回合的对话来回答
        while (DialogHistoryManager.isFull(httpSession)) {
            DialogHistoryManager.getHistory(httpSession).remove(0);
        }
    }

    public static void updateDialogueHistory(String query, String response,HttpSession httpSession) {
        String content = query + " " + response;
        // 提取语义特征
        Set<String> keywords = extractKeywords(content);
        Map<String, Double> topics = calculateTopicDistribution(content);
        double frequency = calculateTermFrequency(content);

        AtomicInteger counter = CounterManager.getCounterFromSession(httpSession);

        DialogueRecord dialogueRecord = new DialogueRecord(
                query,
                response,
                System.currentTimeMillis(),
                counter.getAndIncrement(),
                0,
                new DialogueRecord.SemanticFeature(keywords, frequency, topics)
        );
        List<DialogueRecord> dialogueRecordList = DialogHistoryManager.getHistory(httpSession);
        dialogueRecordList.add(dialogueRecord);
        // 显式更新 session 中的数据
        httpSession.setAttribute("DIALOG_HISTORY", dialogueRecordList);
        httpSession.setAttribute("SESSION_COUNTER",counter);
    }


    /**
     * 计算综合权重
     */
    public static void calculateCombinedWeights(HttpSession httpSession) {
        Map<Integer, Double> weights = new HashMap<>();
        List<DialogueRecord> dialogueRecordList = DialogHistoryManager.getHistory(httpSession);
        int size = DialogHistoryManager.size(httpSession);

        dialogueRecordList.forEach((dialogueRecord) -> {
            // 1. 计算时间衰减  取最后一个元素的turnIndex
            int currentTurn = dialogueRecordList.get(size - 1).getTurnIndex();

            // 计算时间衰减权重
            double timeWeight = Math.exp(-DECAY_FACTOR * (currentTurn - dialogueRecord.getTurnIndex()));

            DialogueRecord.SemanticFeature feature = dialogueRecord.getSemanticFeature();
            // 2. 计算频率权重
            double freqWeight = feature.getFrequency();

            // 3. 计算语义相关度
            double semanticWeight = calculateSemanticRelevance(feature);

            // 4. 合并权重
            double combinedWeight = TIME_WEIGHT * timeWeight +
                    FREQUENCY_WEIGHT * freqWeight +
                    SEMANTIC_WEIGHT * semanticWeight;

            weights.put(dialogueRecord.getTurnIndex(), combinedWeight);
        });

        // 归一化权重
        normalizeWeights(weights);
        //将归一化后的权重进行赋值
        dialogueRecordList.forEach((dialogueRecord) -> {
             dialogueRecord.setWeight(weights.get(dialogueRecord.getTurnIndex()));
        });
    }

    /**
     * 构建语义摘要
     */
    private String buildSemanticSummary(List<DialogueRecord> dialogueRecordList) {
        StringBuilder summary = new StringBuilder();
        summary.append("Semantic Context Summary:\n\n");

        // 1. 提取高权重关键信息
        TreeMap<Double, Set<String>> weightedKeywords = new TreeMap<>(Collections.reverseOrder());
        dialogueRecordList.forEach((dialogueRecord) -> {
            double weight = dialogueRecord.getWeight();
            dialogueRecord.getSemanticFeature().getKeywords().forEach(keyword ->
                    weightedKeywords.computeIfAbsent(weight, k -> new HashSet<>()).add(keyword)
            );
        });

        // 2. 构建主题摘要
        summary.append("Key Topics:\n");
        Map<String, Double> mergedTopics = mergeTopics(dialogueRecordList);
        mergedTopics.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .forEach(entry ->
                        summary.append("- ").append(entry.getKey())
                                .append(" (").append(String.format("%.2f", entry.getValue()))
                                .append(")\n")
                );

        // 3. 添加关键词摘要
        summary.append("\nKey Points:\n");
        weightedKeywords.entrySet().stream()
                .limit(MAX_KEYWORDS)
                .forEach(entry ->
                        entry.getValue().forEach(keyword ->
                                summary.append("- ").append(keyword)
                                        .append(" (weight: ").append(String.format("%.2f", entry.getKey()))
                                        .append(")\n")
                        )
                );

        return summary.toString();
    }

    /**
     * 构建优化后的提示词
     */
    private String buildOptimizedPrompt(String currentQuery,HttpSession httpSession) {
        List<DialogueRecord> dialogueRecordList = DialogHistoryManager.getHistory(httpSession);
        StringBuilder prompt = new StringBuilder();
        // 对话历史摘要
        prompt.append("=== Previous Dialogue Summary ===\n");
        for (DialogueRecord record : dialogueRecordList) {
            double weight = record.getWeight();
            Set<String> keywords = record.getSemanticFeature().getKeywords();
            prompt.append(String.format("Turn %d (Weight: %.2f):\n", record.getTurnIndex(), weight));
            prompt.append("Keywords: ").append(String.join(", ", keywords)).append("\n");
            prompt.append("Summary: ").append(summarizeContent(record)).append("\n");
        }

        // 主题分布
        prompt.append("=== Topic Distribution ===\n");
        Map<String, Double> globalTopics = new HashMap<>();
        for (DialogueRecord record : dialogueRecordList) {
            record.getSemanticFeature().getTopics().forEach((topic, weight) ->
                    globalTopics.merge(topic, weight*record.getWeight(), Double::sum)
            );
        }

        globalTopics.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .forEach(entry ->
                        prompt.append(String.format("- %s: %.2f\n", entry.getKey(), entry.getValue()))
                );

        // 当前查询
        prompt.append("\n=== Current Query ===\n");
        prompt.append(currentQuery).append("\n\n");

        // 响应指南
        prompt.append("Please provide a response that:\n");
        prompt.append("1. Addresses the current query directly\n");
        prompt.append("2. Maintains consistency with previous discussion\n");
        prompt.append("3. Builds upon established concepts\n");
        prompt.append("4. Provides concrete examples when appropriate\n");

        return prompt.toString();

    }

    /**
     * 内容摘要生成
     */
    private String summarizeContent(DialogueRecord record) {
        String combinedContent = record.getQuery() + " " + record.getResponse();

        // 1. 提取关键句
        List<String> sentences = HanLP.extractSummary(combinedContent, 2);

        // 2. 合并并精简
        return sentences.stream()
                .map(this::simplifyContent)
                .collect(Collectors.joining(" "));
    }

    /**
     * 精简内容
     */
    private String simplifyContent(String content) {
        // 可以使用更复杂的文本简化算法
        return content.length() > 20 ?
                content.substring(0, 17) + "..." :
                content;
    }

    /**
     * 辅助类：概念演进
     */
    @Data
    @AllArgsConstructor
    private static class ConceptEvolution {
        private String concept;
        private double evolutionScore;
        private List<Double> weightHistory;
    }




    // 停用词集合
    private static final Set<String> STOP_WORDS = loadStopWords();

    // 重要词性标签
    private static final Set<String> IMPORTANT_POS = new HashSet<>(Arrays.asList(
            "n", "v", "adj", "adv", "vn", "an" // 名词、动词、形容词、副词、动名词、形容名词
    ));

    /**
     * 提取关键词和语义特征
     */
    private static Set<String> extractKeywords(String content) {
        if (StringUtils.isBlank(content)) {
            return Collections.emptySet();
        }

        try {
            // 1. 智能分词
            List<Term> terms = HanLP.segment(content);

            // 2. 提取关键词
            List<String> keywords = HanLP.extractKeyword(content, 3);

            // 3. 命名实体识别
            List<String> entities = HanLP.extractPhrase(content,3);

            // 4. 合并结果并过滤
            Set<String> result = new HashSet<>();

            // 添加关键词
            result.addAll(keywords);

            // 添加重要词性的词
            result.addAll(terms.stream()
                    .filter(term -> isImportantTerm(term))
                    .map(term -> term.word)
                    .limit(4)
                    .collect(Collectors.toSet()));

            // 添加命名实体
            result.addAll(entities);

            return result.stream()
                    .filter(word -> word.length() >= 2)
                    .filter(word -> !STOP_WORDS.contains(word))
                    .collect(Collectors.toSet());

        } catch (Exception e) {
            log.error("Error extracting keywords: ", e);
            return Collections.emptySet();
        }
    }


    /**
     * 计算主题分布
     */
    private static Map<String, Double> calculateTopicDistribution(String content) {
        Map<String, Double> topicDist = new HashMap<>();

        try {
            // 1. 分词和词性标注
            List<Term> terms = HanLP.segment(content);

            // 2. 计算词频
            Map<String, Integer> wordFreq = new HashMap<>();
            terms.stream()
                    .map(term -> term.word)
                    .filter(word -> !STOP_WORDS.contains(word))
                    .forEach(word -> wordFreq.merge(word, 1, Integer::sum));

            // 3. 计算TF-IDF
            double totalWords = wordFreq.values().stream().mapToDouble(Integer::doubleValue).sum();

            // 4. 提取主题词和权重
            terms.stream()
                    .filter(term -> isImportantTerm(term))
                    .forEach(term -> {
                        String word = term.word;
                        int freq = wordFreq.getOrDefault(word, 0);

                        // TF计算
                        double tf = freq / totalWords;

                        // IDF计算（简化版）
                        double idf = Math.log(100.0 / (freq + 1));

                        // 词性权重
                        double posWeight = getPositionWeight(term);

                        // 最终权重
                        double weight = tf * idf * posWeight;

                        topicDist.merge(word, weight, Double::sum);
                    });

            // 5. 归一化
            normalizeDistribution(topicDist);

        } catch (Exception e) {
            log.error("Error calculating topic distribution: ", e);
        }

        return topicDist;
    }

    /**
     * 计算词频特征
     */
    private static double calculateTermFrequency(String content) {
        try {
            // 1. 分词
            List<Term> terms = HanLP.segment(content);

            // 2. 计算词频分布
            Map<String, Integer> freqDist = terms.stream()
                    .map(term -> term.word)
                    .filter(word -> !STOP_WORDS.contains(word))
                    .collect(Collectors.groupingBy(
                            word -> word,
                            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                    ));

            // 3. 计算统计特征
            double totalWords = freqDist.values().stream().mapToInt(Integer::intValue).sum();
            double uniqueWords = freqDist.size();

            // 4. 计算词频多样性指标
            double diversityScore = uniqueWords / Math.log(1 + totalWords);

            // 5. 计算重要词占比
            long importantWords = freqDist.entrySet().stream()
                    .filter(e -> e.getValue() > 1)
                    .count();

            double importanceScore = importantWords / uniqueWords;

            // 6. 综合评分
            return (diversityScore + importanceScore) / 2;

        } catch (Exception e) {
            log.error("Error calculating term frequency: ", e);
            return 0.0;
        }
    }

    /**
     * 计算语义相关度
     */
    private static double calculateSemanticRelevance(DialogueRecord.SemanticFeature feature) {
        try {
            // 1. 获取特征
            Set<String> keywords = feature.getKeywords();
            Map<String, Double> topicDist = feature.getTopics();

            // 2. 计算语义连贯性
            double coherence = calculateCoherence(topicDist);

            // 3. 计算关键词覆盖度
            double coverage = calculateKeywordCoverage(keywords);

            // 4. 计算主题聚焦度
            double focus = calculateTopicFocus(topicDist);

            // 5. 综合评分
            return 0.4 * coherence + 0.3 * coverage + 0.3 * focus;

        } catch (Exception e) {
            log.error("Error calculating semantic relevance: ", e);
            return 0.0;
        }
    }

    /**
     * 辅助方法
     */
    private static boolean isImportantTerm(Term term) {
        String pos = term.nature.toString();
        return IMPORTANT_POS.stream().anyMatch(pos::startsWith);
    }

    private static double getPositionWeight(Term term) {
        String pos = term.nature.toString();
        if (pos.startsWith("n")) return 1.0;  // 名词
        if (pos.startsWith("v")) return 0.8;  // 动词
        if (pos.startsWith("a")) return 0.6;  // 形容词
        if (pos.startsWith("ad")) return 0.4;  // 副词
        return 0.2;  // 其他
    }

    private static void normalizeDistribution(Map<String, Double> dist) {
        double sum = dist.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum > 0) {
            dist.replaceAll((k, v) -> v / sum);
        }
    }

    private static double calculateCoherence(Map<String, Double> topicDist) {
        if (topicDist.isEmpty()) return 0.0;

        try {
            // 确保所有概率值都大于0且和为1
            double sum = topicDist.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

            // 如果和不为1，进行归一化
            if (Math.abs(sum - 1.0) > 1e-10) {
                topicDist = new HashMap<>(topicDist);
                topicDist.replaceAll((k, v) -> v / sum);
            }

            // 计算熵，添加小量epsilon避免log(0)
            double epsilon = 1e-10;
            double entropy = -topicDist.values().stream()
                    .mapToDouble(p -> {
                        if (p < epsilon) return 0.0;
                        return p * Math.log(p);
                    })
                    .sum();

            // 归一化熵值到[0,1]区间
            return Math.exp(-entropy);

        } catch (Exception e) {
            log.error("Error calculating coherence: ", e);
            return 0.0;
        }
    }

    private static double calculateKeywordCoverage(Set<String> keywords) {
        if (keywords.isEmpty()) return 0.0;

        // 计算关键词的覆盖度
        int significantKeywords = (int) keywords.stream()
                .filter(word -> word.length() > 2)
                .count();

        return Math.min(1.0, significantKeywords / 10.0);
    }

    private static double calculateTopicFocus(Map<String, Double> topicDist) {
        if (topicDist.isEmpty()) return 0.0;

        // 计算主题的集中度
        double maxWeight = topicDist.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        return maxWeight;
    }

    private static Set<String> loadStopWords() {
        // 加载停用词表
        return new HashSet<>(Arrays.asList(
                "的", "是", "在", "有", "和", "与", "了",
                "我", "你", "他", "她", "它", "这", "那",
                "都", "也", "就", "但", "而", "或", "如"
                // 添加更多停用词
        ));
    }

    private static void normalizeWeights(Map<Integer, Double> weights) {
        double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        weights.replaceAll((k, v) -> v / sum);
    }

    private Map<String, Double> mergeTopics(List<DialogueRecord> dialogueRecordList) {
        // 合并所有主题分布
        Map<String, Double> mergedTopics = new HashMap<>();
       dialogueRecordList.forEach(feature ->
                feature.getSemanticFeature().getTopics().forEach((topic, weight) ->
                        mergedTopics.merge(topic, weight, Double::sum)
                )
        );
        return mergedTopics;
    }
}
