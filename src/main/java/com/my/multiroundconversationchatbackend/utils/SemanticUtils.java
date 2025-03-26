package com.my.multiroundconversationchatbackend.utils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author lihaixu
 * @date 2025年03月22日 21:03
 */

@Slf4j
public class SemanticUtils {

    // 停用词集合
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "是", "在", "有", "和", "与", "了",
            "我", "你", "他", "她", "它", "这", "那",
            "都", "也", "就", "但", "而", "或", "如",
            "啊", "呢", "吧", "吗", "么", "嗯", "呀"
    ));

    // 重要词性标签
    private static final Set<String> IMPORTANT_POS = new HashSet<>(Arrays.asList(
            "n", "v", "adj", "adv", "vn", "an" // 名词、动词、形容词、副词、动名词、形容名词
    ));

    /**
     * 提取关键词
     */
    public static Set<String> extractKeywords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return Collections.emptySet();
        }

        try {
            // 1. 智能分词
            List<Term> terms = HanLP.segment(content);

            // 2. 提取关键词
            List<String> keywords = HanLP.extractKeyword(content, 5);

            // 3. 命名实体识别
            List<String> entities = HanLP.extractPhrase(content, 3);

            // 4. 合并结果并过滤
            Set<String> result = new HashSet<>();

            // 添加关键词
            result.addAll(keywords);

            // 添加重要词性的词
            result.addAll(terms.stream()
                    .filter(SemanticUtils::isImportantTerm)
                    .map(term -> term.word)
                    .limit(5)
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
//    IDF (Inverse Document Frequency) 是一个用于评估词语重要性的统计指标。这个简化版的IDF计算公式的作用是：
//
//    基本原理
//    公式: idf = log(N / (df + 1))
//    其中：
//    N = 100.0 (简化版中使用的文档总数常量)
//    df = freq (词语在文档中出现的频率)
//+1 是为了避免除零错误
//            log函数用于降低数值差异
//    作用解释：
//    对于高频词（freq大），IDF值较小
//    对于低频词（freq小），IDF值较大
//    这样可以降低常见词的权重，提升稀有词的权重
//    实际例子：
//    假设有一个词：
//    如果freq=1（罕见词）：idf = log(100/(1+1)) ≈ 3.91
//    如果freq=50（常见词）：idf = log(100/(50+1)) ≈ 0.69
//    这说明罕见词获得更高的IDF值，表明其可能更具有区分性和重要性。
//
//    在您的系统中，这个IDF值与TF(词频)相乘，用于确定词语在文本中的整体重要性，帮助提取关键信息和计算语义特征。
    public static Map<String, Double> calculateTopicDistribution(String content) {
        Map<String, Double> topicDist = new HashMap<>();

        if (content == null || content.trim().isEmpty()) {
            return topicDist;
        }

        try {
            // 1. 分词和词性标注
            List<Term> terms = HanLP.segment(content);

            // 2. 计算词频
            Map<String, Integer> wordFreq = new HashMap<>();
            terms.stream()
                    .map(term -> term.word)
                    .filter(word -> !STOP_WORDS.contains(word))
                    .forEach(word -> wordFreq.merge(word, 1, Integer::sum));

            // 3. 计算文档总词数
            double totalWords = wordFreq.values().stream().mapToDouble(Integer::doubleValue).sum();
            if (totalWords <= 0) {
                return topicDist;
            }

            // 4. 提取主题词和权重
            terms.stream()
                    .filter(SemanticUtils::isImportantTerm)
                    .forEach(term -> {
                        String word = term.word;
                        if (word.length() < 2 || STOP_WORDS.contains(word)) {
                            return;
                        }

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
     * 提取实体
     */
    public static List<String> extractEntities(String content) {
        if (content == null || content.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return HanLP.extractPhrase(content, 5);
        } catch (Exception e) {
            log.error("Error extracting entities: ", e);
            return Collections.emptyList();
        }
    }

    /**
     * 计算词频特征
     */
//    这个方法用于分析文本内容的词频特征，计算文本的语言丰富度和重要性得分。让我用例子来说明：
//
//    例子1：简单重复的文本
//    输入: "今天天气真好真好真好"
//    分析过程:
//
//    分词后: [今天, 天气, 真好, 真好, 真好]
//    独特词数(uniqueWords): 3 (今天、天气、真好)
//    总词数(totalWords): 5
//    重要词数(importantWords): 1 (只有"真好"出现超过1次)
//    得分会较低，因为文本重复性高，词汇多样性低。
//    例子2：丰富的文本
//    输入: "春天来了，花儿开放，小鸟唱歌，蝴蝶翩翩起舞"
//    分析过程:
//
//    分词后: [春天, 来了, 花儿, 开放, 小鸟, 唱歌, 蝴蝶, 翩翩, 起舞]
//    独特词数: 9
//    总词数: 9
//    重要词数: 0 (没有重复词)
//    得分会较高，因为：
//    词汇多样性高(diversityScore高)
//    每个词都是独特的
//    这个方法的主要用途：
//
//    评估文本质量
//            检测文本是否存在过度重复
//    分析文本的词汇丰富度
//    可用于内容审核、文章质量评估等场景
//    例如在内容管理系统中，可以用它来：
//
//    自动筛选垃圾内容（重复度过高的文本）
//    评估文章质量（词汇丰富度）
//    为SEO提供文本质量参考指标
//    辅助判断文本是否由AI生成（AI生成的文本可能词汇多样性较低）
    public static double calculateTermFrequency(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0.0;
        }

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
            if (totalWords <= 0) {
                return 0.0;
            }

            double uniqueWords = freqDist.size();

            // 4. 计算词频多样性指标
            double diversityScore = uniqueWords / Math.log(1 + totalWords);

            // 5. 计算重要词占比
            long importantWords = freqDist.entrySet().stream()
                    .filter(e -> e.getValue() > 1)
                    .count();

            double importanceScore = importantWords / (uniqueWords > 0 ? uniqueWords : 1);

            // 6. 综合评分
            return (diversityScore + importanceScore) / 2;

        } catch (Exception e) {
            log.error("Error calculating term frequency: ", e);
            return 0.0;
        }
    }

    /**
     * 计算语义连贯性
     */
//    例1：高连贯度
//
//// 主题分布
//"体育": 0.8
//        "娱乐": 0.1
//        "科技": 0.1
//
//// 计算熵
//    H = -(0.8 * log(0.8) + 0.1 * log(0.1) + 0.1 * log(0.1))
//// 连贯度
//    coherence = e^(-H)  // 结果接近1
//    例2：低连贯度
//
//// 主题分布
//"体育": 0.33
//        "娱乐": 0.33
//        "科技": 0.34
//
//// 计算熵
//    H = -(0.33 * log(0.33) + 0.33 * log(0.33) + 0.34 * log(0.34))
//// 连贯度
//    coherence = e^(-H)  // 结果接近0

//    特点：
//
//    熵值越小，表示分布越集中，连贯度越高
//    熵值越大，表示分布越分散，连贯度越低
//    结果范围在0到1之间，便于比较
//    这种计算方法能有效反映主题分布的集中程度，从而评估文本的语义连贯性。

    public static double calculateCoherence(Map<String, Double> topicDist) {
        if (topicDist == null || topicDist.isEmpty()) {
            return 0.0;
        }

        try {
            // 确保所有概率值都大于0且和为1
            double sum = topicDist.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

            // 如果和不为1，进行归一化
            Map<String, Double> normalizedDist = topicDist;
            if (Math.abs(sum - 1.0) > 1e-10) {
                normalizedDist = new HashMap<>(topicDist);
                normalizedDist.replaceAll((k, v) -> v / sum);
            }

            // 计算熵，添加小量epsilon避免log(0)
            double epsilon = 1e-10;
            double entropy = -normalizedDist.values().stream()
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

    /*
     * 辅助方法
     */
    private static boolean isImportantTerm(Term term) {
        if (term == null || term.nature == null) {
            return false;
        }
        String pos = term.nature.toString();
        return IMPORTANT_POS.stream().anyMatch(pos::startsWith);
    }

    private static double getPositionWeight(Term term) {
        if (term == null || term.nature == null) {
            return 0.2;
        }
        String pos = term.nature.toString();
        if (pos.startsWith("n")) return 1.0;  // 名词
        if (pos.startsWith("v")) return 0.8;  // 动词
        if (pos.startsWith("a")) return 0.6;  // 形容词
        if (pos.startsWith("d")) return 0.4;  // 副词
        return 0.2;  // 其他
    }

    private static void normalizeDistribution(Map<String, Double> dist) {
        double sum = dist.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum > 0) {
            dist.replaceAll((k, v) -> v / sum);
        }
    }
}

