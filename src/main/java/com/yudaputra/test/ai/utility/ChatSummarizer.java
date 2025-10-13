package com.yudaputra.test.ai.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class for summarizing chat log
 */
@Service
public class ChatSummarizer {

    private final Logger logger = LoggerFactory.getLogger(ChatSummarizer.class);

    private final ChatModel chatModel;

    public ChatSummarizer(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Summarizer Chat History
     * @param persona The persona to summarize {@code String}
     * @param systemMessage System message {@code String}
     * @param history Chat history in term of {@code String}
     */
    public void summarizeHistory(String persona, String systemMessage, List<Message> history) {
        StringBuilder convo = new StringBuilder();
        logger.info("[ChatSummarizer] Start to summarize, building String....");
        for (Message msg : history) {
            convo.append(msg.getText()).append("\n");
        }

        logger.info("[ChatSummarizer] Message combined is : {}", convo.toString());
        Prompt summaryPrompt = new Prompt(List.of(
                new SystemMessage(systemMessage),
                new UserMessage(convo.toString())
        ));

        ChatResponse summaryResponse = chatModel.call(summaryPrompt);
        String summaryText = summaryResponse.getResult().getOutput().getText();
        logger.info("[ChatSummarizer] Prompt has been summarizer.\nSummary : {}", summaryText);

        history.clear();
        history.add(new AssistantMessage("Summary for persona [" + persona + "]: " + summaryText));
    }
}
