package com.yudaputra.test.ai.entity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    @JsonProperty("persona")
    private String persona;
    @JsonProperty("message")
    private String message;

    public String getPersona() {
        return persona;
    }

    public void setPersona(String persona) {
        this.persona = persona;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "persona='" + persona + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
