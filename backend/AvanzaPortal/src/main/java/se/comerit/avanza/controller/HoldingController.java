package se.comerit.avanza.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import se.comerit.avanza.dto.CreateHoldingRequestDTO;
import se.comerit.avanza.dto.HoldingResponseDTO;
import se.comerit.avanza.service.HoldingService;

@RestController
@RequestMapping("/api")
public class HoldingController {


    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }


    @GetMapping("/holdings")
    public ResponseEntity<HoldingResponseDTO> listHoldings(HttpSession session) {

        // Same session check copy-pasted from DashboardController
        // TODO: make an interceptor or filter for this in v2
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer userId = (Integer) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");

        List<Map<String, Object>> holdings = holdingService.getEnrichedHoldingsForUser(userId);
        List<Map<String, Object>> accounts = holdingService.getAccountsForUser(userId);

        HoldingResponseDTO responseDTO = new HoldingResponseDTO(userName, holdings, accounts);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/holdings/add")
    public ResponseEntity<Void> addHolding(@RequestBody CreateHoldingRequestDTO requestDTO, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        holdingService.addHolding(requestDTO.accountId(), requestDTO.ticker(), requestDTO.instrumentName(),
                requestDTO.quantity(), requestDTO.avgBuyPrice(), requestDTO.currency());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/holdings/delete")
    public ResponseEntity<Void> deleteHolding(@RequestParam Integer holdingId,
            HttpSession session) {

        // Session check
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // IDOR VULNERABILITY: No ownership check — any logged-in user can delete any holding
        // We just delete by holdingId directly without verifying it belongs to this user
        // TODO: add WHERE account_id IN (SELECT id FROM accounts WHERE user_id = ?) check
        holdingService.deleteHolding(holdingId);

        return ResponseEntity.noContent().build();
    }
}
