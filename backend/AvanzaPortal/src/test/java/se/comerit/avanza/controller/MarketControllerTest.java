package se.comerit.avanza.controller;

import org.junit.jupiter.api.BeforeEach;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.service.MarketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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