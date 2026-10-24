package shop.le_paysan.backend.delivery;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.common.GeoUtils;
import shop.le_paysan.backend.common.notification.NotificationService;
import shop.le_paysan.backend.delivery.dto.DeliveryRequestResponse;
import shop.le_paysan.backend.delivery.dto.EligibleLivreurResponse;
import shop.le_paysan.backend.order.Order;
import shop.le_paysan.backend.order.OrderItem;
import shop.le_paysan.backend.order.OrderRepository;
import shop.le_paysan.backend.product.ProductDeliveryOption;
import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.user.Role;
import shop.le_paysan.backend.user.User;
import shop.le_paysan.backend.user.UserRepository;

@Service
public class DeliveryRequestService {

    /** Cf. demande utilisateur : "localite ou des environs" — echelle de livraison urbaine,
     * plus restrictive que le rayon catalogue (50km, simple navigation du catalogue). */
    private static final double DELIVERY_RADIUS_KM = 25.0;

    private final DeliveryRequestRepository deliveryRequestRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public DeliveryRequestService(
            DeliveryRequestRepository deliveryRequestRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.deliveryRequestRepository = deliveryRequestRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<EligibleLivreurResponse> listEligibleLivreurs(Shop shop, Long orderId) {
        Order order = findOwnedByShop(shop, orderId);
        OrderItem reference = deliveryItem(order);

        Set<Long> alreadyRequested = deliveryRequestRepository
                .findByOrderIdAndStatus(orderId, DeliveryRequest.STATUS_EN_ATTENTE).stream()
                .map(r -> r.getLivreur().getId())
                .collect(java.util.stream.Collectors.toSet());

        return userRepository.findByRoles_Name(Role.LIVREUR).stream()
                .filter(User::isEnabled)
                .filter(User::isAvailable)
                .filter(u -> User.KYC_STATUS_VALIDE.equals(u.getKycStatus()))
                .filter(u -> !alreadyRequested.contains(u.getId()))
                .map(u -> toEligible(u, reference))
                .filter(java.util.Objects::nonNull)
                .filter(e -> e.distanceKm() == null || e.distanceKm() <= DELIVERY_RADIUS_KM)
                .sorted(Comparator.comparing(
                        EligibleLivreurResponse::distanceKm, Comparator.nullsLast(Double::compareTo)))
                .toList();
    }

    private EligibleLivreurResponse toEligible(User livreur, OrderItem reference) {
        if (reference.getDeliveryCityLat() == null || livreur.getCityLat() == null) {
            // Coordonnees manquantes (ancien compte ou ville hors liste) : on matche sur le nom de
            // ville uniquement, pas de calcul de distance possible.
            boolean sameCity = reference.getDeliveryCityName() != null
                    && reference.getDeliveryCityName().equalsIgnoreCase(livreur.getCity());
            return sameCity ? EligibleLivreurResponse.from(livreur, null) : null;
        }
        double distance = GeoUtils.haversineKm(
                reference.getDeliveryCityLat().doubleValue(), reference.getDeliveryCityLng().doubleValue(),
                livreur.getCityLat().doubleValue(), livreur.getCityLng().doubleValue());
        return EligibleLivreurResponse.from(livreur, distance);
    }

    @Transactional
    public List<DeliveryRequestResponse> broadcast(Shop shop, Long orderId, List<Long> livreurIds) {
        Order order = findOwnedByShop(shop, orderId);
        if (order.getAssignedLivreur() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette commande a déjà un livreur assigné");
        }
        deliveryItem(order);

        List<DeliveryRequestResponse> created = new java.util.ArrayList<>();
        for (Long livreurId : livreurIds) {
            if (deliveryRequestRepository.existsByOrderIdAndLivreurIdAndStatus(
                    orderId, livreurId, DeliveryRequest.STATUS_EN_ATTENTE)) {
                continue;
            }
            User livreur = userRepository
                    .findById(livreurId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livreur introuvable"));

            DeliveryRequest request = new DeliveryRequest();
            request.setOrder(order);
            request.setLivreur(livreur);
            deliveryRequestRepository.save(request);

            notificationService.notify(
                    livreur,
                    "DELIVERY_REQUEST",
                    "Nouvelle demande de livraison",
                    "La commande " + order.getOrderNumber() + " de \"" + order.getShop().getName()
                            + "\" est disponible pour livraison.",
                    "/livreur/demandes");

            created.add(DeliveryRequestResponse.from(request));
        }
        return created;
    }

    @Transactional(readOnly = true)
    public List<DeliveryRequestResponse> listForLivreur(User livreur) {
        return deliveryRequestRepository
                .findByLivreurIdAndStatusOrderByCreatedAtDesc(livreur.getId(), DeliveryRequest.STATUS_EN_ATTENTE)
                .stream()
                .map(DeliveryRequestResponse::from)
                .toList();
    }

    @Transactional
    public DeliveryRequestResponse accept(User livreur, Long requestId) {
        DeliveryRequest request = deliveryRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable"));
        if (!request.getLivreur().getId().equals(livreur.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable");
        }
        if (!DeliveryRequest.STATUS_EN_ATTENTE.equals(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette demande n'est plus disponible");
        }

        Order order = request.getOrder();
        int updated = orderRepository.assignLivreurIfUnassigned(order.getId(), livreur);
        if (updated == 0) {
            // Un autre livreur a accepte entre-temps : cette demande expire.
            request.setStatus(DeliveryRequest.STATUS_EXPIREE);
            request.setRespondedAt(LocalDateTime.now());
            deliveryRequestRepository.save(request);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cette commande a déjà été prise en charge par un autre livreur");
        }

        request.setStatus(DeliveryRequest.STATUS_ACCEPTEE);
        request.setRespondedAt(LocalDateTime.now());
        deliveryRequestRepository.save(request);

        // Les autres demandes en attente pour cette commande disparaissent (cf. demande utilisateur).
        for (DeliveryRequest other : deliveryRequestRepository.findByOrderIdAndStatus(
                order.getId(), DeliveryRequest.STATUS_EN_ATTENTE)) {
            if (!other.getId().equals(request.getId())) {
                other.setStatus(DeliveryRequest.STATUS_EXPIREE);
                other.setRespondedAt(LocalDateTime.now());
                deliveryRequestRepository.save(other);
            }
        }

        notificationService.notify(
                order.getShop().getOwner(),
                "DELIVERY_ACCEPTED",
                "Livreur trouvé",
                livreur.getFirstName() + " " + livreur.getLastName() + " a accepté de livrer la commande "
                        + order.getOrderNumber() + ".",
                "/vendeur/commandes/" + order.getId());

        return DeliveryRequestResponse.from(request);
    }

    private OrderItem deliveryItem(Order order) {
        return order.getItems().stream()
                .filter(i -> ProductDeliveryOption.MODE_LIVRAISON.equals(i.getDeliveryMode()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "Cette commande ne comporte pas de livraison à domicile"));
    }

    private Order findOwnedByShop(Shop shop, Long orderId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));
        if (!order.getShop().getId().equals(shop.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable");
        }
        return order;
    }
}
