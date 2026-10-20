package shop.le_paysan.backend.shop;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import shop.le_paysan.backend.auth.SecurityUser;
import shop.le_paysan.backend.shop.dto.PayoutAccountRequest;
import shop.le_paysan.backend.shop.dto.PayoutAccountResponse;
import shop.le_paysan.backend.shop.dto.ShopRequestForm;
import shop.le_paysan.backend.shop.dto.ShopResponse;
import shop.le_paysan.backend.shop.dto.UpdateShopRequest;

@RestController
@RequestMapping("/api/v1/shops")
public class ShopController {

    private final ShopService service;

    public ShopController(ShopService service) {
        this.service = service;
    }

    @PostMapping(value = "/requests", consumes = "multipart/form-data")
    public ResponseEntity<ShopResponse> submitRequest(
            @AuthenticationPrincipal SecurityUser principal, @ModelAttribute ShopRequestForm form) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submitRequest(principal.getUser(), form));
    }

    @GetMapping("/me")
    public ShopResponse me(@AuthenticationPrincipal SecurityUser principal) {
        return service.getMyShop(principal.getUser().getId());
    }

    @PutMapping("/me")
    public ShopResponse updateMe(
            @AuthenticationPrincipal SecurityUser principal, @Valid @RequestBody UpdateShopRequest request) {
        return service.updateMyShop(principal.getUser().getId(), request);
    }

    @GetMapping("/me/payout-account")
    public PayoutAccountResponse payoutAccount(@AuthenticationPrincipal SecurityUser principal) {
        return service.getPayoutAccount(principal.getUser().getId());
    }

    @PutMapping("/me/payout-account")
    public PayoutAccountResponse upsertPayoutAccount(
            @AuthenticationPrincipal SecurityUser principal, @Valid @RequestBody PayoutAccountRequest request) {
        return service.upsertPayoutAccount(principal.getUser().getId(), request);
    }

    @GetMapping("/{id}")
    public ShopResponse getPublic(@PathVariable Long id) {
        return service.getPublic(id);
    }

    @GetMapping
    public Page<ShopResponse> listPublic(@RequestParam(required = false) String search, Pageable pageable) {
        return service.listPublic(search, pageable);
    }
}
