package com.example.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*; 
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    private static final String INIT_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();


        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Adwaith Shyju M");
        requestBody.put("regNo", "22BAI10292");
        requestBody.put("email", "adwaithshyju2022@vitbhopal.ac.in");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(INIT_URL, entity, JsonNode.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("Failed to get webhook response. Status: " + response.getStatusCode());
            System.err.println("Response Body: " + response.getBody());
            return;
        }

        JsonNode body = response.getBody();
        System.out.println("DEBUG: Server Response Body: " + body.toPrettyString()); 

        String webhookUrl = body.get("webhook").asText().trim();
        String accessToken = body.get("accessToken").asText().trim();

        Object outcome = "The final answer required by the challenge";

    
        Map<String, Object> finalPayload = new HashMap<>();
        finalPayload.put("regNo", "22BAI10292");
        finalPayload.put("outcome", outcome);

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        HttpEntity<Map<String, Object>> resultEntity = new HttpEntity<>(finalPayload, headers);

        boolean success = false;
        for (int i = 0; i < 4; i++) {
            try {
                ResponseEntity<String> finalResponse = restTemplate.postForEntity(webhookUrl, resultEntity, String.class);
                if (finalResponse.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Successfully posted result on attempt " + (i + 1));
                    System.out.println("Response: " + finalResponse.getBody());
                    success = true;
                    break;
                }
            } catch (Exception e) {
                System.err.println("Attempt " + (i + 1) + " failed, retrying...");
                Thread.sleep(1000);
            }
        }

        if (!success) {
            System.err.println("Failed to post result after 4 attempts.");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class User {
        public int id;

        @JsonProperty("follows")
        public List<Integer> follows;

        public User() {}

        public User(int id, List<Integer> follows) {
            this.id = id;
            this.follows = follows;
        }
    }
}
