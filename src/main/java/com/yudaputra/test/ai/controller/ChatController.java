package com.yudaputra.test.ai.controller;

import com.yudaputra.test.ai.entity.model.ChatRequest;
import com.yudaputra.test.ai.service.PersonaChatService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private PersonaChatService personaChatService;

    public ChatController() {}

    @PostMapping("/client")
    public ResponseEntity<String> chatByClient(@RequestBody ChatRequest request) {
        String response = "";
        try {
            response = personaChatService.getChatWithClient(request);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Persona not found!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error can't be processed. Please contact Admin!");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/model")
    public ResponseEntity<String> chatByModel(@RequestBody ChatRequest request) {
        String response = "";
        try {
            response = personaChatService.getChatResponseByPersona(request)
                    .getResult().getOutput().getText();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Persona not found!");
        } catch (Exception e) {
            logger.error("Exception : {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error can't be processed. Please contact Admin!");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/structured")
    public String chatByStructuredOutput(@RequestBody String message) {
        return null;
    }
}
