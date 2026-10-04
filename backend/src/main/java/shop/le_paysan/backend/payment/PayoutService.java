package shop.le_paysan.backend.payment;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import shop.le_paysan.backend.shop.Shop;
import shop.le_paysan.backend.shop.ShopPayoutAccount;
import shop.le_paysan.backend.shop.ShopPayoutAccountRepository;

/**
 * Job planifie de reversement automatique (Option B, cf. PaymentService) : traite les
 * {@link VendorPayout} EN_ATTENTE en appelant l'API de transfert sortant Fedapay vers le
 * compte Mobile Money enregistre du vendeur.
 */
@Service
public class PayoutService {

    private static final Logger log = LoggerFactory.getLogger(PayoutService.class);

    private final VendorPayoutRepository payoutRepository;
    private final ShopPayoutAccountRepository payoutAccountRepository;
    private final FedapayClient fedapayClient;

    public PayoutService(
            VendorPayoutRepository payoutRepository,
            ShopPayoutAccountRepository payoutAccountRepository,
            FedapayClient fedapayClient) {
        this.payoutRepository = payoutRepository;
        this.payoutAccountRepository = payoutAccountRepository;
        this.fedapayClient = fedapayClient;
    }

    /**
     * Toutes les 15 min : suffisant pour un MVP, a ajuster en prod (cf. docs/04-architecture-technique.md §5).
     * @Transactional ici (et pas seulement sur processOne) : l'appel this.processOne() plus bas est une
     * auto-invocation qui ne passe pas par le proxy Spring, donc son @Transactional serait ignore sans
     * une transaction deja active englobante ici (sinon LazyInitializationException sur payout.getOrder()).
     */
    @Scheduled(fixedDelay = 15 * 60 * 1000)
    @Transactional
    public void processPendingPayouts() {
        List<VendorPayout> pending = payoutRepository.findByStatus(VendorPayout.STATUS_EN_ATTENTE);
        for (VendorPayout payout : pending) {
            processOne(payout);
        }
    }

    @Transactional
    public void processOne(VendorPayout payout) {
        Shop shop = payout.getShop();
        ShopPayoutAccount account = payoutAccountRepository.findById(shop.getId()).orElse(null);
        if (account == null) {
            payout.setStatus(VendorPayout.STATUS_ECHEC);
            payout.setFailureReason("Aucun compte Mobile Money de reversement renseigné pour cette boutique");
            payout.setProcessedAt(LocalDateTime.now());
            payoutRepository.save(payout);
            return;
        }

        String[] names = splitName(account.getAccountHolderName());
        FedapayClient.CustomerInfo customer = new FedapayClient.CustomerInfo(names[0], names[1], account.getPhoneNumber());

        try {
            FedapayClient.CreatedPayout result = fedapayClient.createPayout(payout.getAmount(), customer);
            payout.setFedapayPayoutId(result.id());
            payout.setStatus(VendorPayout.STATUS_ENVOYE);
            payout.setFailureReason(null);
        } catch (Exception e) {
            log.warn("Echec du reversement pour la commande {} : {}", payout.getOrder().getOrderNumber(), e.getMessage());
            payout.setStatus(VendorPayout.STATUS_ECHEC);
            payout.setFailureReason(e.getMessage());
        }
        payout.setProcessedAt(LocalDateTime.now());
        payoutRepository.save(payout);
    }

    @Transactional(readOnly = true)
    public Page<VendorPayout> listForAdmin(String status, Pageable pageable) {
        return status != null ? payoutRepository.findByStatus(status, pageable) : payoutRepository.findAll(pageable);
    }

    @Transactional
    public void retry(Long payoutId) {
        VendorPayout payout = payoutRepository
                .findById(payoutId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!VendorPayout.STATUS_ECHEC.equals(payout.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seul un reversement en échec peut être relancé");
        }
        processOne(payout);
    }

    private static String[] splitName(String fullName) {
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts.length == 2 ? parts : new String[] {parts[0], parts[0]};
    }
}
