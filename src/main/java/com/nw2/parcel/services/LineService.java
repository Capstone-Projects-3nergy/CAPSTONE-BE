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

    public Map<String, Object> buildParcelFlex(
            String trackingNumber,
            String status,
            String viewUrl
    ) {

        return Map.of(
                "type", "bubble",

                "body", Map.of(
                        "type", "box",
                        "layout", "vertical",
                        "spacing", "md",
                        "contents", new Object[]{

                                Map.of(
                                        "type", "text",
                                        "text", "📦 New Parcel Arrived",
                                        "weight", "bold",
                                        "size", "xl"
                                ),

                                Map.of(
                                        "type", "text",
                                        "text", "Tracking: " + trackingNumber,
                                        "size", "sm",
                                        "color", "#555555",
                                        "weight", "bold"
                                ),

                                Map.of(
                                        "type", "text",
                                        "text", "Status: " + status,
                                        "size", "sm",
                                        "color", "#2E7D32"
                                ),

                                Map.of(
                                        "type", "text",
                                        "text",
                                        "Your parcel has arrived at the dormitory and is ready for pickup.",
                                        "wrap", true,
                                        "size", "sm",
                                        "margin", "md"
                                )
                        }
                ),

                "footer", Map.of(
                        "type", "box",
                        "layout", "vertical",
                        "contents", new Object[]{

                                Map.of(
                                        "type", "button",
                                        "style", "primary",
                                        "height", "sm",
                                        "action", Map.of(
                                                "type", "uri",
                                                "label", "View Parcel",
                                                "uri", viewUrl
                                        )
                                )
                        }
                )
        );
    }

    public Map<String, Object> buildOverdueFlex(
            String trackingNumber,
            String days,
            String viewUrl
    ) {
        return Map.of(
                "type", "bubble",

                "body", Map.of(
                        "type", "box",
                        "layout", "vertical",
                        "spacing", "md",
                        "contents", new Object[]{

                                Map.of(
                                        "type", "text",
                                        "text", "Parcel Overdue ⚠️",
                                        "weight", "bold",
                                        "size", "xl",
                                        "color", "#d32f2f"
                                ),

                                Map.of(
                                        "type", "text",
                                        "text", "Tracking: " + trackingNumber,
                                        "size", "sm",
                                        "color", "#555555",
                                        "weight", "bold",
                                        "wrap", true
                                ),

                                Map.of(
                                        "type", "text",
                                        "text", "Overdue: " + days + " days",
                                        "size", "sm",
                                        "color", "#d32f2f",
                                        "wrap", true
                                ),

                                Map.of(
                                        "type", "text",
                                        "text",
                                        "Please pick up your parcel as soon as possible.",
                                        "wrap", true,
                                        "size", "sm",
                                        "margin", "md"
                                )
                        }
                ),

                "footer", Map.of(
                        "type", "box",
                        "layout", "vertical",
                        "contents", new Object[]{

                                Map.of(
                                        "type", "button",
                                        "style", "primary",
                                        "height", "sm",
                                        "color", "#D32F2F",
                                        "action", Map.of(
                                                "type", "uri",
                                                "label", "View Parcel",
                                                "uri", viewUrl
                                        )
                                )
                        }
                )
        );
    }

    public Map<String, Object> buildAnnouncementFlex(
            String title,
            String message
    ) {
        return Map.of(
                "type", "bubble",

                "body", Map.of(
                        "type", "box",
                        "layout", "vertical",
                        "spacing", "md",
                        "contents", new Object[]{

                                Map.of(
                                        "type", "text",
                                        "text", "📢 Announcement",
                                        "weight", "bold",
                                        "size", "xl",
                                        "color", "#1976d2"
                                ),

                                Map.of(
                                        "type", "text",
                                        "text", title,
                                        "weight", "bold",
                                        "size", "md",
                                        "wrap", true,
                                        "margin", "md"
                                ),

                                Map.of(
                                        "type", "text",
                                        "text", message,
                                        "size", "sm",
                                        "wrap", true,
                                        "margin", "md",
                                        "color", "#555555"
                                )
                        }
                )
        );
    }
}