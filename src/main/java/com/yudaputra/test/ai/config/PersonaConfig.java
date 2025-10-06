package com.yudaputra.test.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Configuration
public class PersonaConfig {

    @Value("${spring.ai.personas.path}")
    private String personasPath;

    @Bean
    public Map<String, String> personas() throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(Paths.get(personasPath))) {
            Map<String, Object> obj = yaml.load(in);
            return (Map<String, String>) obj.get("personas");
        }
    }
}
