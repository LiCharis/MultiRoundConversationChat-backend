package com.my.multiroundconversationchatbackend.service.prompt;

import com.my.multiroundconversationchatbackend.model.entity.DialogueRecord;
import com.my.multiroundconversationchatbackend.service.conversation.ConversationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lihaixu
 * @date 2025年03月02日 16:45
 */
@Service
public class PromptBuilderService {

    public String buildPrompt(List<DialogueRecord> history, Map<Integer, Double> weights, String currentQuery) {
        StringBuilder prompt = new StringBuilder();

        // 2. 添加对话历史上下文摘要
        prompt.append("Previous conversation summary:\n");
        String historySummary = buildHistorySummary(history, weights);
        prompt.append(historySummary).append("\n\n");

        // 3. 添加最近几轮重要对话
        prompt.append("Recent important exchanges:\n");
        String recentExchanges = buildRecentExchanges(history, weights);
        prompt.append(recentExchanges).append("\n\n");

        // 4. 添加当前问题
        prompt.append("Current query: ").append(currentQuery).append("\n");
        prompt.append("Please provide a helpful response based on the above context.");

        return prompt.toString();
    }

    private String buildHistorySummary(List<DialogueRecord> history, Map<Integer, Double> weights) {
        StringBuilder summary = new StringBuilder();

        // 按权重排序对话记录
        List<DialogueRecord> sortedHistory = history.stream()
                .sorted((a, b) -> Double.compare(
                        weights.get(b.getTurnIndex()),
                        weights.get(a.getTurnIndex())
                ))
                .collect(Collectors.toList());

        // 提取重要对话要点
        for (DialogueRecord record : sortedHistory) {
            if (weights.get(record.getTurnIndex()) > 0.2) { // 只包含权重较高的对话
                summary.append("- ")
                        .append(record.getRole())
                        .append(": ")
                        .append(summarizeContent(record.getContent()))
                        .append("\n");
            }
        }

        return summary.toString();
    }

    private String buildRecentExchanges(List<DialogueRecord> history, Map<Integer, Double> weights) {
        StringBuilder exchanges = new StringBuilder();

        // 获取最近3轮对话
        int start = Math.max(0, history.size() - 6); // 3轮问答共6条记录
        for (int i = start; i < history.size(); i++) {
            DialogueRecord record = history.get(i);
            exchanges.append(record.getRole())
                    .append(" (weight: ")
                    .append(String.format("%.2f", weights.get(record.getTurnIndex())))
                    .append("): ")
                    .append(record.getContent())
                    .append("\n");
        }

        return exchanges.toString();
    }

    private String summarizeContent(String content) {
        // 这里可以接入文本摘要模型
        // 简单实现：截取前50个字符
        return content.length() > 50 ?
                content.substring(0, 50) + "..." :
                content;
    }
}
