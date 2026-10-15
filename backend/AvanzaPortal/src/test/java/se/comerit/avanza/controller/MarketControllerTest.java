package se.comerit.avanza.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import se.comerit.avanza.dto.market.BacktestRequestDTO;
import se.comerit.avanza.dto.market.BacktestResponseDTO;
import se.comerit.avanza.dto.market.FxRateResponseDTO;
import se.comerit.avanza.exception.ApiExceptionHandler;
import se.comerit.avanza.exception.UnsupportedCurrencyPairException;
import se.comerit.avanza.service.MarketService;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

                MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketController(marketService))
                                .setControllerAdvice(new ApiExceptionHandler())
                                .build();

                mockMvc.perform(get("/api/market/fx/eur/usd"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.rate").value(1.1539));
        }

        @Test
        void getFxReturnsBadRequestForUnknownPair() throws Exception {
                when(marketService.getFx("xx", "usd"))
                                .thenThrow(new UnsupportedCurrencyPairException("Unknown currency pair"));

                MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketController(marketService))
                                .setControllerAdvice(new ApiExceptionHandler())
                                .build();

                mockMvc.perform(get("/api/market/fx/xx/usd"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("Bad Request"))
                                .andExpect(jsonPath("$.detail").value("Unknown currency pair"));
        }

        @Test
        void getPriceReturnsOkForSupportedTicker() throws Exception {
                when(marketService.getPrice("ERIC-B"))
                                .thenReturn(Optional.of(new BigDecimal("94.96")));

                MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketController(marketService))
                                .setControllerAdvice(new ApiExceptionHandler())
                                .build();

                mockMvc.perform(get("/api/market/price/ERIC-B"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value(94.96));
        }

        @Test
        void getPriceReturnsNotFoundWhenPriceIsUnavailable() throws Exception {
                when(marketService.getPrice("UNKNOWN"))
                                .thenReturn(Optional.empty());

                MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketController(marketService))
                                .setControllerAdvice(new ApiExceptionHandler())
                                .build();

                mockMvc.perform(get("/api/market/price/UNKNOWN"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Not Found"))
                                .andExpect(jsonPath("$.detail").value("Price not found for ticker: UNKNOWN"));
        }

        @Test
        void getBacktestAnalysisReturnsOkWithAnalysis() throws Exception {
                BacktestRequestDTO request = new BacktestRequestDTO(
                                List.of("ERIC-B", "VOLV-B"),
                                LocalDate.parse("2025-01-01"),
                                LocalDate.parse("2025-12-31"),
                                "BUY_HOLD");
                BacktestResponseDTO response = new BacktestResponseDTO(
                                0.15,
                                0.15,
                                -0.08,
                                1.25,
                                List.of("ERIC-B", "VOLV-B"),
                                List.of(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31")));
                when(marketService.getBacktestAnalysis(request)).thenReturn(response);

                MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketController(marketService))
                                .setControllerAdvice(new ApiExceptionHandler())
                                .build();

                mockMvc.perform(post("/api/market/backtest")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "tickers": ["ERIC-B", "VOLV-B"],
                                                  "startDate": "2025-01-01",
                                                  "endDate": "2025-12-31",
                                                  "strategy": "BUY_HOLD"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalReturn").value(0.15))
                                .andExpect(jsonPath("$.annualizedReturn").value(0.15))
                                .andExpect(jsonPath("$.maxDrawdown").value(-0.08))
                                .andExpect(jsonPath("$.sharpeRatio").value(1.25))
                                .andExpect(jsonPath("$.tickers[0]").value("ERIC-B"))
                                .andExpect(jsonPath("$.tickers[1]").value("VOLV-B"))
                                .andExpect(jsonPath("$.dates[0]").value("2025-01-01"))
                                .andExpect(jsonPath("$.dates[1]").value("2025-12-31"));
        }

        @Test
        void getBacktestAnalysisReturnsBadRequestForIllegalArgumentException() throws Exception {
                BacktestRequestDTO request = new BacktestRequestDTO(
                                List.of("ERIC-B"),
                                LocalDate.parse("2025-01-01"),
                                LocalDate.parse("2025-12-31"),
                                "BUY_HOLD");
                when(marketService.getBacktestAnalysis(request))
                                .thenThrow(new IllegalArgumentException("Invalid backtest parameters"));

                MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketController(marketService)).build();

                mockMvc.perform(post("/api/market/backtest")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "tickers": ["ERIC-B"],
                                                  "startDate": "2025-01-01",
                                                  "endDate": "2025-12-31",
                                                  "strategy": "BUY_HOLD"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string(""));
        }

        @Test
        void getBacktestAnalysisReturnsBadRequestForIllegalStateException() throws Exception {
                BacktestRequestDTO request = new BacktestRequestDTO(
                                List.of("ERIC-B"),
                                LocalDate.parse("2025-01-01"),
                                LocalDate.parse("2025-12-31"),
                                "BUY_HOLD");
                when(marketService.getBacktestAnalysis(request))
                                .thenThrow(new IllegalStateException("Historical data is unavailable"));

                MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MarketController(marketService)).build();

                mockMvc.perform(post("/api/market/backtest")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "tickers": ["ERIC-B"],
                                                  "startDate": "2025-01-01",
                                                  "endDate": "2025-12-31",
                                                  "strategy": "BUY_HOLD"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string(""));
        }

}
