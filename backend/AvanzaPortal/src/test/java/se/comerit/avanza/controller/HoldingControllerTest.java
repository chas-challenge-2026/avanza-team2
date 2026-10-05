package se.comerit.avanza.controller;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import se.comerit.avanza.dto.holdings.*;
import se.comerit.avanza.service.HoldingService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HoldingControllerTest {
    private HoldingService service;
    private HoldingController controller;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        service = mock(HoldingService.class);
        controller = new HoldingController(service);
        authentication = new UsernamePasswordAuthenticationToken("anna@example.com", null);
    }

    @Test
    void shouldListHoldingsUsingAuthenticatedIdentity() {
        HoldingResponseDTO response = new HoldingResponseDTO("Anna", List.of(), List.of());
        when(service.getHoldingsForAuthenticatedUser("anna@example.com")).thenReturn(response);
        var result = controller.listHoldings(authentication);
        assertEquals(200, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(service).getHoldingsForAuthenticatedUser("anna@example.com");
    }

    @Test
    void shouldAddHoldingUsingAuthenticatedIdentity() {
        var request = new CreateHoldingRequestDTO(3, "AAPL", "Apple", "5", "180.50", "USD");
        assertEquals(201, controller.addHolding(request, authentication).getStatusCode().value());
        verify(service).addHoldingForAuthenticatedUser("anna@example.com", request);
    }

    @Test
    void shouldDeleteHoldingUsingAuthenticatedIdentity() {
        assertEquals(204, controller.deleteHolding(15, authentication).getStatusCode().value());
        verify(service).deleteHoldingForAuthenticatedUser("anna@example.com", 15);
    }

    @Test
    void shouldPreserveExistingJsonFieldNames() throws Exception {
        // Typed DTOs must not silently change the frontend's JSON contract.
        var holding = new HoldingItemDTO(15L, "AAPL", "Apple", new BigDecimal("5"),
                new BigDecimal("180.50"), "USD", "ISK", "My account", 187.32, 936.60, 34.10);
        var response = new HoldingResponseDTO("Anna", List.of(holding),
                List.of(new HoldingAccountDTO(3L, "ISK", "My account")));
        var json = new ObjectMapper().valueToTree(response);
        assertEquals("Apple", json.path("holdings").get(0).path("instrument_name").asText());
        assertEquals(180.50, json.path("holdings").get(0).path("avg_buy_price").asDouble());
        assertEquals("My account", json.path("accounts").get(0).path("account_name").asText());
        assertFalse(json.path("holdings").get(0).has("instrumentName"));
    }
}
