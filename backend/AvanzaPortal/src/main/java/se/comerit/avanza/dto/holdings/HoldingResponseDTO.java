package se.comerit.avanza.dto.holdings;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record HoldingResponseDTO(
        String userName,
        List<HoldingItemDTO> holdings,
        List<HoldingAccountDTO> accounts,
        @Min(0) int page,
        @Min(1) @Max(100) int size,
        long totalElements,
        int totalPages) {
}
