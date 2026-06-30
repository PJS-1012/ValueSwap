package com.valueswap.notification;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, String title, String message, NotificationType type,
                                   boolean read, LocalDateTime createdAt) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getTitle(), notification.getMessage(),
                notification.getType(), notification.isRead(), notification.getCreatedAt());
    }
}
