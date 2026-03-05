package com.nw2.parcel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LineService {

    @Value("${line.channel.access-token}")
    private String channelAccessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public void pushMessage(String lineUserId, Object messageBody) {

        String url = "https://api.line.me/v2/bot/message/push";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(channelAccessToken);

        Map<String, Object> requestBody = Map.of(
                "to", lineUserId,
                "messages", new Object[]{messageBody}
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(requestBody, headers);

        restTemplate.postForEntity(url, request, String.class);
    }

    // Simple text message
    public Map<String, Object> buildTextMessage(String text) {
        return Map.of(
                "type", "text",
                "text", text
        );
    }

    // Flex message (ใช้ bubble ที่คุณส่งมา)
    public Map<String, Object> buildFlexMessage(Object bubble) {
        return Map.of(
                "type", "flex",
                "altText", "Notification",
                "contents", bubble
        );
    }
}