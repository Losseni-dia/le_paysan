package shop.le_paysan.backend.commission;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import shop.le_paysan.backend.commission.dto.CommissionRateResponse;
import shop.le_paysan.backend.commission.dto.UpdateCommissionRequest;

@RestController
@RequestMapping("/api/admin/commission")
@PreAuthorize("hasRole('ADMIN')")
public class CommissionAdminController {

    private final CommissionService service;

    public CommissionAdminController(CommissionService service) {
        this.service = service;
    }

    @GetMapping
    public CommissionRateResponse current() {
        return service.getCurrentRate();
    }

    @GetMapping("/historique")
    public List<CommissionRateResponse> history() {
        return service.getHistory();
    }

    @PostMapping
    public CommissionRateResponse update(@Valid @RequestBody UpdateCommissionRequest request) {
        return service.updateRate(request.rate());
    }
}
