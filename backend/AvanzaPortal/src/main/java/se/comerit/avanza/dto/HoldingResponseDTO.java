package se.comerit.avanza.dto;

import java.util.List;
import java.util.Map;

public record HoldingResponseDTO(
        String userName,
        List<Map<String, Object>> holdings,
        List<Map<String, Object>> accounts
) {
}