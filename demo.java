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

import org.springframework.http.*;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class demo implements CommandLineRunner {

    private static final String INIT_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    public static void main(String[] args) {
        SpringApplication.run(demo.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        // Step 1: Send initial POST request
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Adwaith Shyju M"); // I've used your name from the resume
        requestBody.put("regNo", "22BAI10292"); // Use your actual registration number
        requestBody.put("email", "adwaithshyju2022@vitbhopal.ac.in"); // I've used your email from the resume

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(INIT_URL, entity, JsonNode.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("Failed to get webhook response");
            return;
        }

        JsonNode body = response.getBody();
        String webhookUrl = body.get("webhook").asText().trim();
        String accessToken = body.get("accessToken").asText().trim();
        JsonNode usersData = body.get("data").get("users");

        // Step 2: Parse users
        List<User> users = new ArrayList<>();
        for (JsonNode node : usersData) {
            int id = node.get("id").asInt();
            List<Integer> follows = new ArrayList<>();
            for (JsonNode follow : node.get("follows")) {
                follows.add(follow.asInt());
            }
            users.add(new User(id, follows));
        }

        // V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V //
        // REPLACE THE LOGIC BELOW WITH YOUR NEW SOLUTION                                    //
        // The current example finds the user ID that follows the most other users.          //

        // Step 3: Process the data to solve the new problem
        int maxFollows = -1;
        int userWithMaxFollows = -1;

        for (User user : users) {
            if (user.follows.size() > maxFollows) {
                maxFollows = user.follows.size();
                userWithMaxFollows = user.id;
            }
        }

        // Step 4: Prepare outcome
        // The 'outcome' object should be formatted exactly as the new problem requires.
        Object outcome;

        if (userWithMaxFollows != -1) {
            // Example: The problem might ask for the ID of the user with the most follows.
            outcome = userWithMaxFollows;
        } else {
            // Provide a default outcome if needed
            outcome = "No users found";
        }

        // REPLACE THE LOGIC ABOVE WITH YOUR NEW SOLUTION                                    //
        // ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ //

        // Step 5: Send result to webhook
        Map<String, Object> finalPayload = new HashMap<>();
        finalPayload.put("regNo", "REG12347"); // Use your actual registration number
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