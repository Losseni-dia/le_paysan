package shop.le_paysan.backend.admin.dashboard;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import shop.le_paysan.backend.admin.dashboard.dto.DashboardResponse;
import shop.le_paysan.backend.admin.dashboard.dto.StatsResponse;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardAdminController {

    private final DashboardService service;

    public DashboardAdminController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return service.getDashboard();
    }

    @GetMapping("/statistiques")
    public StatsResponse stats() {
        return service.getStats();
    }
}
