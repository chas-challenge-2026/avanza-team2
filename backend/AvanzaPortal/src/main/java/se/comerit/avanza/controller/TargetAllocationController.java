package se.comerit.avanza.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.comerit.avanza.dto.targetallocation.TargetAllocationResponseDTO;
import se.comerit.avanza.service.TargetAllocationService;


@RestController 
@RequestMapping("/api/target-allocations")
public class TargetAllocationController {

    private final TargetAllocationService targetAllocationService;

    public TargetAllocationController(TargetAllocationService targetAllocationService) {
        this.targetAllocationService = targetAllocationService;
    }

    @GetMapping
    public ResponseEntity<List<TargetAllocationResponseDTO>> getTargetAllocations(Authentication authentication) {

        // Get the target allocations for the authenticated user
        List<TargetAllocationResponseDTO> allocations = targetAllocationService.getTargetAllocationsForAuthenticatedUser(authentication.getName());
        
        // Return the allocations in the response
        return ResponseEntity.ok(allocations);
    }

    
    
}
