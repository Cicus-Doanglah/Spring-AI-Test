package com.yudaputra.test.ai.service;

import com.yudaputra.test.ai.entity.model.ChatRequest;
import com.yudaputra.test.ai.utility.ChatSummarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class PersonaChatService {

    private static final Logger logger = LoggerFactory.getLogger(PersonaChatService.class);

    @Autowired
    private Map<String, String> personas;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ChatSummarizer chatSummarizer;

    private final ChatClient chatClient;

    private final Map<String, List<Message>> personaHistories = new HashMap<>();

    private static final int SUMMARY_THRESHOLD = 5;

    public PersonaChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Function to use ChatClient for Persona Chat Service
     * @param chatRequest Base chat request json {@code ChatRequest}
     * @return response from the AI model in {@code String} format
     */
    public String getChatWithClient(ChatRequest chatRequest) {
        String response = "";
        if (!personas.containsKey(chatRequest.getPersona())) {
            throw new NoSuchElementException("Persona with name " + chatRequest.getPersona() + " not found!");
        }

        UserMessage userMessage = new UserMessage(chatRequest.getMessage());

        var promptBuilder = chatClient.prompt()
                .system(personas.get(chatRequest.getPersona()));
        promptBuilder = promptBuilder.user(userMessage.getText());

        response = promptBuilder.call().content();
        return response;
    }

    /**
     * Function to get Chat Response with custom persona and memory.
     * @param chatRequest Base chat request json {@code ChatRequest}
     * @return a response from the AI model in format of {@code ChatResponse}
     */
    public ChatResponse getChatResponseByPersona(ChatRequest chatRequest) {
        ChatResponse response;
        String persona = chatRequest.getPersona();
        String userPrompt = chatRequest.getMessage();

        if (!personas.containsKey(persona)) {
            throw new NoSuchElementException("Persona with name " + persona + " not found!");
        }

        /* Getting history of used persona. */
        List<Message> history = personaHistories.computeIfAbsent(
                persona, k -> new ArrayList<>());

        /* Summarize chat if log is more than 5 */
        if (history.size() >= SUMMARY_THRESHOLD * 2) {
            chatSummarizer.summarizeHistory(persona, personas.get("summarizer"), history);
        }

        /* Add new user prompt to history */
        UserMessage userMessage = new UserMessage(userPrompt);
        history.add(userMessage);

        List<Message> messages = new ArrayList<>(history);
        logger.info("Messages : {}", messages);

        SystemMessage systemMessage = new SystemMessage(personas.get(persona));
        messages.add(0, systemMessage);

        Prompt prompt = new Prompt(messages);

        /* Caller and save the reply from the AI model */
        response = chatModel.call(prompt);

        String replyString = response.getResult().getOutput().getText();
        if (replyString != null && !replyString.isEmpty()) {
            history.add(new AssistantMessage(replyString));
        }

        return response;
    }

    /**
     * Function to get Chat Response with custom persona and memory.
     * @param chatRequest Base chat request json {@code ChatRequest}
     * @return a response from the AI model in format of {@code Flux<String>}
     */
    public Flux<String> streamChatResponseByPersona(ChatRequest chatRequest) {
        String persona = chatRequest.getPersona();
        String userPrompt = chatRequest.getMessage();

        if (!personas.containsKey(persona)) {
            throw new NoSuchElementException("Persona with name " + persona + " not found!");
        }

        /* Getting history of used persona. */
        List<Message> history = personaHistories.computeIfAbsent(
                persona, k -> new ArrayList<>());

        /* Summarize chat if log is more than 5 */
        if (history.size() >= SUMMARY_THRESHOLD * 2) {
            chatSummarizer.summarizeHistory(persona, personas.get("summarizer"), history);
        }

        /* Add new user prompt to history */
        UserMessage userMessage = new UserMessage(userPrompt);
        history.add(userMessage);

        List<Message> messages = new ArrayList<>(history);
        logger.info("Messages for stream : {}", messages);

        SystemMessage systemMessage = new SystemMessage(personas.get(persona));
        messages.add(0, systemMessage);

        Prompt prompt = new Prompt(messages);

        StringBuffer fullResponse = new StringBuffer();

        return chatModel.stream(prompt)
                .map(chatResponse -> {
                    String chunk = chatResponse.getResult().getOutput().getText();
                    if (chunk == null) {
                        chunk = "";
                    }
                    fullResponse.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    String completeResponse = fullResponse.toString();
                    if (!completeResponse.isEmpty()) {
                        history.add(new AssistantMessage(completeResponse));
                        logger.info("Add stream response to history: {}", completeResponse);
                    }
                })
                .doOnError(error -> {
                    logger.error("Stream chat error: {}", error.getMessage());
                });
    }
}
