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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import se.comerit.avanza.dto.holdings.CreateHoldingRequestDTO;
import se.comerit.avanza.dto.holdings.HoldingResponseDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.HoldingsRepository;
import se.comerit.avanza.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class HoldingServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HoldingsRepository holdingsRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;
    
    private HoldingService holdingService;

    @BeforeEach
    void setUp() {
        holdingService = new HoldingService(jdbcTemplate, userRepository, accountRepository, holdingsRepository);
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
        // Act and Assert
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
        User user = new User();
        user.setId(7L);
        when(userRepository.findByEmail("anna@example.com")).thenReturn(Optional.of(user));
        when(holdingsRepository.deleteOwnedHolding(15L, 7L)).thenReturn(1);
        holdingService.deleteHoldingForAuthenticatedUser("anna@example.com", 15);

        // Assert
        verify(holdingsRepository).deleteOwnedHolding(15L, 7L);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldReturnHoldingsForAuthenticatedUser() {
        // Arrange: the authenticated identity matches a database user.
        String email = "anna@example.com";
        User user = new User();
        user.setId(7L);
        user.setName("Anna");
        user.setEmail(email);

        Map<String, Object> holding = new HashMap<>();
        holding.put("ticker", "ERIC-B");
        holding.put("quantity", new BigDecimal("10"));
        holding.put("avg_buy_price", new BigDecimal("50"));

        Account account = new Account();
        account.setId(3L);
        account.setAccount_type("ISK");
        account.setAccount_name("Annas ISK");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jdbcTemplate.queryForList(contains("FROM holdings h"), eq(7)))
                .thenReturn(List.of(holding));
        when(accountRepository.findByUserId(7L)).thenReturn(List.of(account));

        // Act
        HoldingResponseDTO result = holdingService.getHoldingsForAuthenticatedUser(email);

        // Assert: both queries use the user's database ID.
        assertEquals("Anna", result.userName());
        assertEquals(1, result.holdings().size());
        assertEquals("ERIC-B", result.holdings().get(0).ticker());
        assertEquals(742.0, result.holdings().get(0).marketValue());
        assertEquals(3L, result.accounts().get(0).id());
        assertEquals("ISK", result.accounts().get(0).accountType());
        verify(userRepository).findByEmail(email);
        verify(jdbcTemplate).queryForList(contains("FROM holdings h"), eq(7));
        verify(accountRepository).findByUserId(7L);
    }

    @Test
    void shouldRejectAuthenticatedUserWhoDoesNotExist() {
        // Arrange
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act and Assert: a missing user is rejected before holdings are fetched.
        assertThrows(BadCredentialsException.class,
                () -> holdingService.getHoldingsForAuthenticatedUser(email));
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldAddHoldingToOwnedAccount() {
        // Arrange: account 3 belongs to user 7.
        String email = "anna@example.com";
        User user = new User();
        user.setId(7L);
        user.setEmail(email);
        CreateHoldingRequestDTO request = new CreateHoldingRequestDTO(
                3, "aapl", "Apple", "5", "180.50", "USD");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(accountRepository.existsByIdAndUser_Id(3L, 7L)).thenReturn(true);

        // Act
        holdingService.addHoldingForAuthenticatedUser(email, request);

        // Assert: ownership is checked and the holding is saved with normalized values.
        verify(accountRepository).existsByIdAndUser_Id(3L, 7L);
        verify(jdbcTemplate).update(contains("INSERT INTO holdings"),
                eq(3), eq("AAPL"), eq("Apple"), eq(new BigDecimal("5")),
                eq(new BigDecimal("180.50")), eq("USD"));
    }

    @Test
    void shouldRejectAddingHoldingToUnownedAccount() {
        // Arrange: the account does not belong to the authenticated user.
        String email = "anna@example.com";
        User user = new User();
        user.setId(7L);
        CreateHoldingRequestDTO request = new CreateHoldingRequestDTO(
                3, "AAPL", "Apple", "5", "180.50", "USD");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(accountRepository.existsByIdAndUser_Id(3L, 7L)).thenReturn(false);

        // Act and Assert: unauthorized ownership must prevent any SQL operation.
        assertThrows(AccessDeniedException.class,
                () -> holdingService.addHoldingForAuthenticatedUser(email, request));
        verify(accountRepository).existsByIdAndUser_Id(3L, 7L);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldRejectAddingHoldingWhenUserDoesNotExist() {
        // Arrange
        String email = "missing@example.com";
        CreateHoldingRequestDTO request = new CreateHoldingRequestDTO(
                3, "AAPL", "Apple", "5", "180.50", "USD");
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act and Assert: reject the user before checking accounts or saving holdings.
        assertThrows(BadCredentialsException.class,
                () -> holdingService.addHoldingForAuthenticatedUser(email, request));
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(accountRepository, jdbcTemplate);
    }

    @Test
    void shouldRejectInvalidBuyPriceWithoutSavingHolding() {
        // Act and Assert: invalid numeric input must not reach the database.
        assertThrows(NumberFormatException.class,
                () -> holdingService.addHolding(
                        3, "AAPL", "Apple", "5", "invalid-price", "USD"));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldRejectDeletingUnownedOrMissingHolding() {
        // Arrange: no holding matches both the ID and the authenticated owner.
        User user = new User();
        user.setId(7L);
        when(userRepository.findByEmail("anna@example.com")).thenReturn(Optional.of(user));
        when(holdingsRepository.deleteOwnedHolding(15L, 7L)).thenReturn(0);

        // Act and Assert: never fall back to an unrestricted SQL delete.
        assertThrows(AccessDeniedException.class,
                () -> holdingService.deleteHoldingForAuthenticatedUser("anna@example.com", 15));
        verify(holdingsRepository).deleteOwnedHolding(15L, 7L);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldRejectDeletingWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class,
                () -> holdingService.deleteHoldingForAuthenticatedUser("missing@example.com", 15));
        verifyNoInteractions(holdingsRepository, jdbcTemplate);
    }

}
