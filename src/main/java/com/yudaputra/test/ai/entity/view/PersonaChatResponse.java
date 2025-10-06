package com.yudaputra.test.ai.entity.view;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
public class PersonaChatResponse {
    private String persona;
    private String response;
    private LocalDateTime timestamp = LocalDateTime.now();

    public String getPersona() {
        return persona;
    }

    public void setPersona(String persona) {
        this.persona = persona;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PersonaChatResponse{" +
                "persona='" + persona + '\'' +
                ", response='" + response + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
