package shop.le_paysan.backend.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import shop.le_paysan.backend.common.notification.NotificationService;

/**
 * Cf. plan "timeline de commande automatisée" (Phase 1) : filet de sécurité qui fait avancer
 * automatiquement les commandes quand personne (acheteur, vendeur) n'a agi dans les délais prévus.
 */
@Service
public class OrderAutoProgressionService {

    private static final int DELAI_ANNULATION_IMPAYEE_HEURES = 48;
    private static final int DELAI_CONFIRMATION_RETRAIT_JOURS = 5;
    private static final int DELAI_CONFIRMATION_LIVRAISON_JOURS = 7;
    private static final int DELAI_RAPPEL_AVIS_HEURES = 24;

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final NotificationService notificationService;

    public OrderAutoProgressionService(
            OrderRepository orderRepository, OrderService orderService, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    @Transactional
    public void run() {
        cancelStaleUnpaidOrders();
        autoConfirmDelivery(Order.STATUS_PRETE, DELAI_CONFIRMATION_RETRAIT_JOURS, "retrait");
        autoConfirmDelivery(Order.STATUS_EXPEDIEE, DELAI_CONFIRMATION_LIVRAISON_JOURS, "livraison");
        sendReviewReminders();
    }

    /** Cf. demande utilisateur : relance l'acheteur pour noter/donner un avis 24h apres la livraison. */
    private void sendReviewReminders() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(DELAI_RAPPEL_AVIS_HEURES);
        List<Order> delivered =
                orderRepository.findByStatusAndReviewReminderSentFalseAndUpdatedAtBefore(Order.STATUS_LIVREE, cutoff);
        for (Order order : delivered) {
            order.setReviewReminderSent(true);
            orderRepository.save(order);

            notificationService.notify(
                    order.getBuyerUser(),
                    "REVIEW_REMINDER",
                    "Donnez votre avis",
                    "Comment s'est passée votre commande " + order.getOrderNumber()
                            + " ? Donnez votre avis et notez les produits/la boutique.",
                    "/mon-espace/commandes");
        }
    }

    private void cancelStaleUnpaidOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(DELAI_ANNULATION_IMPAYEE_HEURES);
        List<Order> stale = orderRepository.findByStatusAndCreatedAtBefore(Order.STATUS_EN_ATTENTE_PAIEMENT, cutoff);
        for (Order order : stale) {
            order.setStatus(Order.STATUS_ANNULEE);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            orderService.recordHistory(
                    order,
                    Order.STATUS_ANNULEE,
                    OrderStatusHistory.CHANGED_BY_SYSTEME,
                    "Annulée automatiquement : paiement non reçu sous " + DELAI_ANNULATION_IMPAYEE_HEURES + "h");

            notificationService.notify(
                    order.getBuyerUser(),
                    "ORDER_STATUS_CHANGED",
                    "Commande annulée",
                    "Votre commande " + order.getOrderNumber() + " a été annulée faute de paiement reçu à temps.",
                    "/mon-espace/commandes");
        }
    }

    private void autoConfirmDelivery(String fromStatus, int delaiJours, String libelle) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(delaiJours);
        List<Order> stale = orderRepository.findByStatusAndUpdatedAtBefore(fromStatus, cutoff);
        for (Order order : stale) {
            order.setStatus(Order.STATUS_LIVREE);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            orderService.recordHistory(
                    order,
                    Order.STATUS_LIVREE,
                    OrderStatusHistory.CHANGED_BY_SYSTEME,
                    "Confirmée automatiquement après " + delaiJours + " jours sans action (" + libelle + ")");

            notificationService.notify(
                    order.getBuyerUser(),
                    "ORDER_STATUS_CHANGED",
                    "Commande livrée",
                    "Votre commande " + order.getOrderNumber() + " a été automatiquement marquée comme livrée.",
                    "/mon-espace/commandes");

            notificationService.notify(
                    order.getShop().getOwner(),
                    "ORDER_STATUS_CHANGED",
                    "Commande livrée",
                    "La commande " + order.getOrderNumber() + " a été automatiquement confirmée livrée après "
                            + delaiJours + " jours.",
                    "/vendeur/commandes");
        }
    }
}
