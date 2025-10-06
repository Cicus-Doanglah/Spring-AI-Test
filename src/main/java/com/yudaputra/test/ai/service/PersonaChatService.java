package com.yudaputra.test.ai.service;

import com.yudaputra.test.ai.entity.view.PersonaChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class PersonaChatService {

    private static final Logger logger = LoggerFactory.getLogger(PersonaChatService.class);

    @Autowired
    private Map<String, String> personas;

    @Autowired
    private ChatModel chatModel;

    public ChatResponse getChatResponseByPersona(String persona, String userMessage) {
        ChatResponse response;
        if (!personas.containsKey(persona)) {
            throw new NoSuchElementException("Persona with name " + persona + " not found!");
        }

        Prompt prompt = new Prompt(
                new SystemMessage(personas.get(persona)),
                new UserMessage(userMessage)
        );
        response = chatModel.call(prompt);
        return response;
    }
}
