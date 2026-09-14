package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;

import se.comerit.avanza.dto.holdings.HoldingResponseDTO;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class HoldingServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;
    
    private HoldingService holdingService;

    @BeforeEach
    void setUp() {
        holdingService = new HoldingService(jdbcTemplate, userRepository, accountRepository);
    }

    @Test
    void shouldReturnHoldingsForUser() {

        // Arrange
        Integer userId = 1;

        Map<String, Object> holding = new HashMap<>();
        holding.put("id", 10);
        holding.put("ticker", "ERIC-B");

        List<Map<String, Object>> expectedHoldings = List.of(holding);

        when(jdbcTemplate.queryForList(anyString(), eq(userId))).thenReturn(expectedHoldings);

        // Act
        List<Map<String, Object>> actualHoldings = holdingService.getHoldingsForUser(userId);

        // Assert
        assertEquals(expectedHoldings, actualHoldings);
        assertEquals(1, actualHoldings.size());
        assertEquals("ERIC-B", actualHoldings.get(0).get("ticker"));

        verify(jdbcTemplate).queryForList(anyString(), eq(userId));



        
    }

    @Test
    void shouldCalculateMarketValueAndPnl() {
        // Arrange
        Integer userId = 1;

        Map<String, Object> holding = new HashMap<>();
        holding.put("ticker", "ERIC-B");
        holding.put("quantity", new BigDecimal("10"));
        holding.put("avg_buy_price", new BigDecimal("50"));

        when(jdbcTemplate.queryForList(anyString(), eq(userId)))
                .thenReturn(List.of(holding));

        // Act
        List<Map<String, Object>> result =
                holdingService.getEnrichedHoldingsForUser(userId);

        // Assert
        Map<String, Object> enrichedHolding = result.get(0);

        assertEquals(74.20, enrichedHolding.get("currentPrice"));
        assertEquals(742.0, enrichedHolding.get("marketValue"));
        assertEquals(242.0, enrichedHolding.get("pnl"));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoHoldings() {
        // Arrange
        Integer userId = 1;

        when(jdbcTemplate.queryForList(anyString(), eq(userId)))
                .thenReturn(List.of());

        // Act
        List<Map<String, Object>> result =
                holdingService.getEnrichedHoldingsForUser(userId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUppercaseTickerWhenAddingHolding() {
        // Act
        holdingService.addHolding(
                2,
                "aapl",
                "Apple",
                "5",
                "180.50",
                "USD"
        );

        // Assert
        verify(jdbcTemplate).update(
                anyString(),
                eq(2),
                eq("AAPL"),
                eq("Apple"),
                eq(new BigDecimal("5")),
                eq(new BigDecimal("180.50")),
                eq("USD")
        );
    }

    @Test
    void shouldRejectInvalidQuantity() {
        // Act och Assert
        assertThrows(
                NumberFormatException.class,
                () -> holdingService.addHolding(
                        2,
                        "AAPL",
                        "Apple",
                        "felaktigt-tal",
                        "180.50",
                        "USD"
                )
        );

        verify(jdbcTemplate, never())
                .update(anyString(), any(Object[].class));
    }

    @Test
    void shouldDeleteHoldingById() {
        // Act
        holdingService.deleteHolding(15);

        // Assert
        verify(jdbcTemplate).update(anyString(), eq(15));
    }

    @Test
    void shouldReturnHoldingsForAuthenticatedUser() {
        // Arrange: JWT-identiteten motsvarar en användare i databasen.
        String email = "anna@example.com";
        User user = new User();
        user.setId(7L);
        user.setName("Anna");
        user.setEmail(email);

        Map<String, Object> holding = new HashMap<>();
        holding.put("ticker", "ERIC-B");
        holding.put("quantity", new BigDecimal("10"));
        holding.put("avg_buy_price", new BigDecimal("50"));

        Map<String, Object> account = new HashMap<>();
        account.put("id", 3);
        account.put("account_type", "ISK");
        account.put("account_name", "Annas ISK");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jdbcTemplate.queryForList(contains("FROM holdings h"), eq(7)))
                .thenReturn(List.of(holding));
        when(jdbcTemplate.queryForList(contains("FROM accounts"), eq(7)))
                .thenReturn(List.of(account));

        // Act
        HoldingResponseDTO result = holdingService.getHoldingsForAuthenicatedUser(email);

        // Assert: båda frågorna använder användarens databas-ID.
        assertEquals("Anna", result.userName());
        assertEquals(1, result.holdings().size());
        assertEquals("ERIC-B", result.holdings().get(0).get("ticker"));
        assertEquals(742.0, result.holdings().get(0).get("marketValue"));
        assertEquals(List.of(account), result.accounts());
        verify(userRepository).findByEmail(email);
        verify(jdbcTemplate).queryForList(contains("FROM holdings h"), eq(7));
        verify(jdbcTemplate).queryForList(contains("FROM accounts"), eq(7));
    }

    @Test
    void shouldRejectAuthenticatedUserWhoDoesNotExist() {
        // Arrange
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act och Assert: saknad användare stoppas innan innehav hämtas.
        assertThrows(BadCredentialsException.class,
                () -> holdingService.getHoldingsForAuthenicatedUser(email));
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(jdbcTemplate);
    }

    
    
}
