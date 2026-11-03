package se.comerit.avanza.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import se.comerit.avanza.dto.rebalance.RebalancePreviewDTO;
import se.comerit.avanza.dto.rebalance.RebalanceRequestDTO;
import se.comerit.avanza.service.RebalanceService;

/**
 * Controller for per-account rebalance operations.
 */
@RestController
@RequestMapping("api/rebalance")
public class RebalanceController {
    private final RebalanceService rebalanceService;

    public RebalanceController(RebalanceService rebalanceService) {
        this.rebalanceService = rebalanceService;
    }

    /** Applies the user's chosen split to one account. */
    @PostMapping("/apply")
    public ResponseEntity<RebalancePreviewDTO> applyRebalance(
            @Valid @RequestBody RebalanceRequestDTO request, Authentication authentication) {
        return ResponseEntity.ok(rebalanceService.applyRebalance(
                authentication.getName(), request.accountId(), request.targetAktierPct()));
    }

    /** Shows the trades a rebalance would make, without changing anything. */
    @GetMapping("/preview")
    public ResponseEntity<RebalancePreviewDTO> previewRebalance(
            @RequestParam Long accountId, @RequestParam BigDecimal targetAktierPct,
            Authentication authentication) {
        return ResponseEntity.ok(rebalanceService.previewRebalance(
                authentication.getName(), accountId, targetAktierPct));
    }
}
