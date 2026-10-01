package shop.le_paysan.backend.common.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.common.notification.dto.NotificationResponse;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public Page<NotificationResponse> list(
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            Pageable pageable) {
        return service.list(principal.getUser().getId(), unreadOnly, pageable).map(NotificationResponse::from);
    }

    @GetMapping("/unread-count")
    public java.util.Map<String, Long> unreadCount(@AuthenticationPrincipal SecurityUser principal) {
        return java.util.Map.of("count", service.unreadCount(principal.getUser().getId()));
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id) {
        service.markAsRead(principal.getUser().getId(), id);
    }

    @PatchMapping("/read-all")
    public void markAllAsRead(@AuthenticationPrincipal SecurityUser principal) {
        service.markAllAsRead(principal.getUser().getId());
    }
}
