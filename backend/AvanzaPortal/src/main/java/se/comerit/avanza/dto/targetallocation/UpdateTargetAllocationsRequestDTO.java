package se.comerit.avanza.dto.targetallocation;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record UpdateTargetAllocationsRequestDTO(
    
    @NotEmpty
    List<@Valid TargetAllocationRequestDTO> allocations
) {
    
}
