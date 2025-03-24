package com.my.multiroundconversationchatbackend.config;

import com.my.multiroundconversationchatbackend.service.chat.conversation.DialogManager;
import com.my.multiroundconversationchatbackend.service.chat.conversation.DialogueProcessor;
import com.my.multiroundconversationchatbackend.service.chat.conversation.PromptBuilder;
import com.my.multiroundconversationchatbackend.service.chat.conversation.SemanticProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.List;


/**
 * @author lihaixu
 * @date 2025年03月22日 21:42
 * 对话处理器配置
 */
@Configuration
public class DialogueProcessorConfig {

    /**
     * 配置对话处理器链
     */
    @Bean
    public List<DialogueProcessor> dialogueProcessors(
            DialogManager dialogueManager,
            SemanticProcessor semanticProcessor,
            PromptBuilder promptBuilder) {

        //定义责任链的先后顺序
        return Arrays.asList(
                dialogueManager,       // 1. 管理对话历史
                semanticProcessor,     // 2. 处理语义特征
                promptBuilder          // 3. 构建优化提示词
        );
    }

    /**
     * 配置任务执行器
     */
    @Bean
    public TaskExecutor myTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("dialogue-processor-");
        executor.initialize();
        return executor;
    }
}
