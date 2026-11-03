package se.comerit.avanza.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import se.comerit.avanza.dto.rebalance.RebalancePreviewDTO;
import se.comerit.avanza.dto.rebalance.RebalanceTradeDTO;
import se.comerit.avanza.exception.ApiExceptionHandler;
import se.comerit.avanza.exception.InvalidRequestException;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.service.RebalanceService;

class RebalanceControllerTest {

    private static final String EMAIL = "anna@example.com";

    private RebalanceService rebalanceService;
    private MockMvc mockMvc;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        rebalanceService = mock(RebalanceService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RebalanceController(rebalanceService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
        authentication = new UsernamePasswordAuthenticationToken(EMAIL, null, List.of());
    }

    private RebalancePreviewDTO preview() {
        return new RebalancePreviewDTO(
                5L,
                new BigDecimal("1000.00"),
                new BigDecimal("60.00"),
                new BigDecimal("40.00"),
                new BigDecimal("50.00"),
                new BigDecimal("50.00"),
                true,
                List.of(new RebalanceTradeDTO(1L, "AAA", "Aktier", "SELL", new BigDecimal("1.6666"),
                        new BigDecimal("60"), new BigDecimal("99.99"))),
                null);
    }

    @Test
    void shouldReturnPreviewForAuthenticatedUser() throws Exception {
        when(rebalanceService.previewRebalance(EMAIL, 5L, new BigDecimal("50"))).thenReturn(preview());

        mockMvc.perform(get("/api/rebalance/preview")
                .param("accountId", "5")
                .param("targetAktierPct", "50")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(5))
                .andExpect(jsonPath("$.currentAktierPct").value(60.00))
                .andExpect(jsonPath("$.targetAktierPct").value(50.00))
                .andExpect(jsonPath("$.drifted").value(true))
                .andExpect(jsonPath("$.trades[0].ticker").value("AAA"))
                .andExpect(jsonPath("$.trades[0].side").value("SELL"));

        verify(rebalanceService).previewRebalance(EMAIL, 5L, new BigDecimal("50"));
    }

    @Test
    void shouldRejectPreviewWithoutRequiredParameters() throws Exception {
        mockMvc.perform(get("/api/rebalance/preview")
                .param("accountId", "5")
                .principal(authentication))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rebalanceService);
    }

    @Test
    void shouldReturnNotFoundWhenPreviewAccountIsNotOwned() throws Exception {
        when(rebalanceService.previewRebalance(EMAIL, 99L, new BigDecimal("50")))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(get("/api/rebalance/preview")
                .param("accountId", "99")
                .param("targetAktierPct", "50")
                .principal(authentication))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldApplyRebalanceForAuthenticatedUser() throws Exception {
        when(rebalanceService.applyRebalance(EMAIL, 5L, new BigDecimal("50"))).thenReturn(preview());

        mockMvc.perform(post("/api/rebalance/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":5,\"targetAktierPct\":50}")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(5))
                .andExpect(jsonPath("$.trades.length()").value(1));

        verify(rebalanceService).applyRebalance(EMAIL, 5L, new BigDecimal("50"));
    }

    @Test
    void shouldRejectApplyWithMissingAccountId() throws Exception {
        mockMvc.perform(post("/api/rebalance/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetAktierPct\":50}")
                .principal(authentication))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rebalanceService);
    }

    @Test
    void shouldRejectApplyWithTargetOutsideRange() throws Exception {
        mockMvc.perform(post("/api/rebalance/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":5,\"targetAktierPct\":101}")
                .principal(authentication))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/rebalance/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":5,\"targetAktierPct\":-1}")
                .principal(authentication))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rebalanceService);
    }

    @Test
    void shouldReturnBadRequestWhenServiceRejectsTarget() throws Exception {
        when(rebalanceService.applyRebalance(any(), any(), any()))
                .thenThrow(new InvalidRequestException("Target percentage must be between 0 and 100"));

        mockMvc.perform(post("/api/rebalance/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":5,\"targetAktierPct\":50}")
                .principal(authentication))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenApplyAccountIsNotOwned() throws Exception {
        when(rebalanceService.applyRebalance(EMAIL, 99L, new BigDecimal("50")))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(post("/api/rebalance/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":99,\"targetAktierPct\":50}")
                .principal(authentication))
                .andExpect(status().isNotFound());
    }
}
