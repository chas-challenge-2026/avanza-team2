package se.comerit.avanza.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import se.comerit.avanza.dto.holdings.CreateHoldingRequestDTO;
import se.comerit.avanza.dto.holdings.HoldingResponseDTO;
import se.comerit.avanza.service.HoldingService;

@RestController
@RequestMapping("/api")
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }
    

    // Endpoint to list holdings for the authenticated user
    @GetMapping("/holdings")
    public ResponseEntity<HoldingResponseDTO> listHoldings(Authentication authentication, HttpSession session) {
        
        // Spring security authentication check instead of session check
        HoldingResponseDTO responseDTO = holdingService.getHoldingsForAuthenicatedUser(authentication.getName());
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/holdings/add")
    public ResponseEntity<Void> addHolding(@RequestBody CreateHoldingRequestDTO requestDTO, Authentication authentication, HttpSession session) {
        // Spring security authentication check instead of session check
        holdingService.addHoldingForAuthenticatedUser(authentication.getName(), requestDTO);

        // return
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/holdings/delete")
    public ResponseEntity<Void> deleteHolding(@RequestParam Integer holdingId,
            HttpSession session) {

        // Session check
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // IDOR VULNERABILITY: No ownership check — any logged-in user can delete any
        // holding
        // We just delete by holdingId directly without verifying it belongs to this
        // user
        // TODO: add WHERE account_id IN (SELECT id FROM accounts WHERE user_id = ?)
        // check
        holdingService.deleteHolding(holdingId);

        return ResponseEntity.noContent().build();
    }
}
