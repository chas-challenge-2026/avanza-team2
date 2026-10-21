package se.comerit.avanza.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import se.comerit.avanza.dto.targetallocation.TargetAllocationResponseDTO;
import se.comerit.avanza.dto.targetallocation.UpdateTargetAllocationsRequestDTO;
import se.comerit.avanza.service.TargetAllocationService;

/**
 * Controller for managing target allocations.
 */
@RestController
@RequestMapping("/api/target-allocations")
public class TargetAllocationController {

    private final TargetAllocationService targetAllocationService;

    public TargetAllocationController(TargetAllocationService targetAllocationService) {
        this.targetAllocationService = targetAllocationService;
    }

    /**
     * Retrieves the target allocations for the authenticated user.
     *
     * @param authentication the authentication object containing the user's details
     * @return a response entity containing the list of target allocations
     */
    @GetMapping
    public ResponseEntity<List<TargetAllocationResponseDTO>> getTargetAllocations(Authentication authentication) {

        // Get the target allocations for the authenticated user
        List<TargetAllocationResponseDTO> allocations = targetAllocationService
                .getTargetAllocationsForAuthenticatedUser(authentication.getName());

        // Return the allocations in the response
        return ResponseEntity.ok(allocations);
    }

    /**
     * Updates the target allocations for the authenticated user.
     *
     * @param request        the request containing the updated target allocations
     * @param authentication the authentication object containing the user's details
     * @return a response entity containing the updated list of target allocations
     */
    @PutMapping
    public ResponseEntity<List<TargetAllocationResponseDTO>> updateTargetAllocations(
            @Valid @RequestBody UpdateTargetAllocationsRequestDTO request,
            Authentication authentication) {

        // Update the target allocations for the authenticated user
        List<TargetAllocationResponseDTO> allocations = targetAllocationService
                .updateTargetAllocationsForAuthenticatedUser(authentication.getName(), request);

        // Return the updated allocations in the response
        return ResponseEntity.ok(allocations);
    }

}
