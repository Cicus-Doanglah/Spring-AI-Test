package com.yudaputra.test.ai.controller;

import com.yudaputra.test.ai.entity.model.ChatRequest;
import com.yudaputra.test.ai.service.PersonaChatService;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    @Autowired
    private PersonaChatService personaChatService;

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
    public ResponseEntity<String> chatByModel(@RequestBody ChatRequest request) {
        String response = "";
        try {
            response = personaChatService.getChatResponseByPersona(request.getPersona(), request.getMessage())
                    .getResult().getOutput().getText();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Persona not found!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error can't be processed. Please contact Admin!");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/structured")
    public String chatByStructuredOutput(@RequestBody String message) {
        return null;
    }
}
