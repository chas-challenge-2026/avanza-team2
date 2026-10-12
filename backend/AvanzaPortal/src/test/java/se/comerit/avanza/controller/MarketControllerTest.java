package se.comerit.avanza.controller;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.dto.market.BacktestResponseDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.springframework.http.MediaType;
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

        @Test
        void getPriceReturnsOkForSupportedTicker() throws Exception {
                when(marketService.getPrice("ERIC-B"))
                                .thenReturn(Optional.of(new BigDecimal("94.96")));

                MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketController(marketService)).build();

                mockMvc.perform(get("/api/market/price/ERIC-B"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value(94.96));
        }

        @Test
        void getPriceReturnsNotFoundWhenPriceIsUnavailable() throws Exception {
                when(marketService.getPrice("UNKNOWN"))
                                .thenReturn(Optional.empty());

                MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketController(marketService)).build();

                mockMvc.perform(get("/api/market/price/UNKNOWN"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void postBacktestReturnsJsonAnalysisWithoutNativePointer() throws Exception {
                when(marketService.getBacktestAnalysis(org.mockito.ArgumentMatchers.any()))
                                .thenReturn(new BacktestResponseDTO(
                                                0.25,
                                                0.10,
                                                -0.08,
                                                1.5,
                                                java.util.List.of("ERIC-B", "VOLV-B"),
                                                java.util.List.of(java.time.LocalDate.parse("2026-10-01"))));

                MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketController(marketService)).build();

                mockMvc.perform(post("/api/market/backtest")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                        "tickers": ["ERIC-B", "VOLV-B"],
                                                        "startDate": "2026-10-01",
                                                        "endDate": "2026-10-06",
                                                        "strategy": "BUY_HOLD"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.totalReturn").value(0.25))
                                .andExpect(jsonPath("$.annualizedReturn").value(0.10))
                                .andExpect(jsonPath("$.maxDrawdown").value(-0.08))
                                .andExpect(jsonPath("$.sharpeRatio").value(1.5))
                                .andExpect(jsonPath("$.tickers[0]").value("ERIC-B"))
                                .andExpect(jsonPath("$.dates[0]").value("2026-10-01"))
                                .andExpect(jsonPath("$.result").doesNotExist())
                                .andExpect(jsonPath("$.prices").doesNotExist());
        }
}
