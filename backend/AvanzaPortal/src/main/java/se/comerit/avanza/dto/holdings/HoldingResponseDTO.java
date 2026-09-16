package se.comerit.avanza.dto.holdings;

import java.util.List;

public record HoldingResponseDTO(
                String userName,
                List<HoldingItemDTO> holdings,
                List<HoldingAccountDTO> accounts) {
}
