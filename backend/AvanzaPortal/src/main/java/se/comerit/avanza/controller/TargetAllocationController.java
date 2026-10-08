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

    @PutMapping
    public ResponseEntity<List<TargetAllocationResponseDTO>> updateTargetAllocations(@Valid @RequestBody UpdateTargetAllocationsRequestDTO request, 
        Authentication authentication) {

        // Update the target allocations for the authenticated user
        List<TargetAllocationResponseDTO> allocations = targetAllocationService.updateTargetAllocationsForAuthenticatedUser(authentication.getName(),request);
        
        // Return the updated allocations in the response
        return ResponseEntity.ok(allocations);
    }

    
    
}
