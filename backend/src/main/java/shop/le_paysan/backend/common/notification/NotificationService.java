package shop.le_paysan.backend.common.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import shop.le_paysan.backend.user.User;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    /** Ecrit systematiquement la notification in-app, independamment de l'email (cf. docs/02 UC-13). */
    @Transactional
    public void notify(User recipient, String type, String title, String message, String linkUrl) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLinkUrl(linkUrl);
        repository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<Notification> list(Long userId, boolean unreadOnly, Pageable pageable) {
        return unreadOnly
                ? repository.findByRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(userId, pageable)
                : repository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return repository.countByRecipientIdAndReadAtIsNull(userId);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = repository
                .findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
        notification.setReadAt(LocalDateTime.now());
        repository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 1000);
        repository.findByRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(userId, pageable).forEach(n -> {
            n.setReadAt(LocalDateTime.now());
            repository.save(n);
        });
    }
}
