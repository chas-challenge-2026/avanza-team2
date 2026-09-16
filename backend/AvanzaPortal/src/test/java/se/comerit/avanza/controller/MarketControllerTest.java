package se.comerit.avanza.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.service.MarketService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MarketControllerTest {

    @Mock
    private MarketService marketService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void getFxReturnsOkWithRate() throws Exception {
        when(marketService.getFx("eur", "usd"))
                .thenReturn(new FxRateResponseDTO("2026-09-16", "EUR", "USD", 1.1539));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketController(marketService)).build();

        mockMvc.perform(get("/api/market/fx/eur/usd"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(1.1539));
    }

    @Test
    void getFxReturnsBadRequestForUnknownPair() throws Exception {
        when(marketService.getFx("xx", "usd"))
                .thenThrow(new IllegalArgumentException("Unknown currency pair"));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketController(marketService)).build();

        mockMvc.perform(get("/api/market/fx/xx/usd"))
                .andExpect(status().isBadRequest());
    }
}
