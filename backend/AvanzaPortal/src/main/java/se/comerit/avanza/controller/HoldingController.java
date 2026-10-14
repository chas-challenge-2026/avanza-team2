package se.comerit.avanza.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import se.comerit.avanza.dto.holdings.CreateHoldingRequestDTO;
import se.comerit.avanza.dto.holdings.HoldingResponseDTO;
import se.comerit.avanza.service.HoldingService;

/**
 * Controller for managing user holdings.
 * Provides endpoints to list, add, and delete holdings for the authenticated
 * user.
 * HoldingController
 */
@RestController
@RequestMapping("/api/holdings")
@Validated
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    /**
     * Lists the holdings for the authenticated user.
     * Retrieves a paginated list of holdings for the authenticated user.
     * 
     * @param authentication the authentication object representing the current user
     * @param page           the page number for pagination
     * @param size           the number of holdings per page
     * @return a ResponseEntity containing the paginated list of holdings for the
     *         authenticated user
     */
    @GetMapping
    public ResponseEntity<HoldingResponseDTO> listHoldings(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Spring security authentication check instead of session check
        HoldingResponseDTO responseDTO = holdingService.getHoldingsForAuthenticatedUser(
                authentication.getName(), PageRequest.of(page, size));
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * 
     * Adds a new holding for the authenticated user.
     * 
     * @param requestDTO     the request DTO containing the details of the holding
     *                       to be added
     * @param authentication the authentication object representing the current user
     * @return a ResponseEntity indicating the result of the add operation
     */
    @PostMapping("/add")
    public ResponseEntity<Void> addHolding(@Valid @RequestBody CreateHoldingRequestDTO requestDTO,
            Authentication authentication) {
        // Spring security authentication check instead of session check
        holdingService.addHoldingForAuthenticatedUser(authentication.getName(), requestDTO);

        // return
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Deletes a holding for the authenticated user.
     * 
     * @param holdingId      the ID of the holding to be deleted
     * @param authentication the authentication object representing the current user
     * @return a ResponseEntity indicating the result of the delete operation
     */
    @PostMapping("/delete")
    public ResponseEntity<Void> deleteHolding(@RequestParam Integer holdingId, Authentication authentication) {
        // Spring security authentication check instead of session check
        holdingService.deleteHoldingForAuthenticatedUser(authentication.getName(), holdingId);

        return ResponseEntity.noContent().build();
    }
}
