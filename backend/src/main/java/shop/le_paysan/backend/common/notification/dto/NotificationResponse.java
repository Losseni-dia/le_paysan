package shop.le_paysan.backend.common.notification.dto;

import java.time.LocalDateTime;

import shop.le_paysan.backend.common.notification.Notification;

public record NotificationResponse(
        Long id, String type, String title, String message, String linkUrl, LocalDateTime readAt, LocalDateTime createdAt) {

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.getLinkUrl(), n.getReadAt(), n.getCreatedAt());
    }
}
