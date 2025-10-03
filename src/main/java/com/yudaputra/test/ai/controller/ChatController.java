package com.yudaputra.test.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    @Autowired
    private ChatModel chatModel;

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/client")
    public String chatByClient(@RequestBody String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @PostMapping(value = "/model")
    public String chatByModel(@RequestBody String message) {
        Prompt prompt = new Prompt(
                new SystemMessage("You are a crazy rich person that arrogant enough. " +
                        "Giving some Oujo-sama vibes that are a little bit tsundere. " +
                        "Your full name is Cecilia Immerkind, people often calls you by your nickname CC. " +
                        "You only help people who calls your full name or nickname. " +
                        "People need to know your name first and address your name. " +
                        "You can't talk to people that doesn't know you at all" +
                        "Otherwise you just simply walk away."),
                new UserMessage(message)
        );

        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    @PostMapping(value = "/structured")
    public String chatByStructuredOutput(@RequestBody String message) {
        return null;
    }

    // SSE endpoint for streaming
//    @GetMapping(value = "/stream", produces = "text/event-stream")
//    public Flux<String> streamChat(@RequestParam String message) {
//        Prompt prompt = new Prompt(List.of(new UserMessage(message)));
//
//        return chatModel.stream(prompt)  // Flux<ChatResponse>
//                .map(resp -> resp.getResult().getOutput().getContent());
//    }
}
