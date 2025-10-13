package com.yudaputra.test.ai.controller;

import com.yudaputra.test.ai.entity.model.ChatRequest;
import com.yudaputra.test.ai.service.PersonaChatService;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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

    @PostMapping(value = "/model/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatByModelStream(@RequestBody ChatRequest request) {
        return personaChatService.streamChatResponseByPersona(request)
                .onErrorResume(NoSuchElementException.class,
                        e -> Flux.just("Error: Persona not found!"))
                .onErrorResume(Exception.class,
                        e -> {
                            logger.error("Exception during streaming: {}", e.getMessage());
                            return Flux.just("Error: Unable to process request.");
                        });
    }
}
