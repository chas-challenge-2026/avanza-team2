package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import se.comerit.avanza.dto.holdings.CreateHoldingRequestDTO;
import se.comerit.avanza.dto.holdings.HoldingResponseDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.Holdings;
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
        private MarketService marketService;

        private HoldingService holdingService;

        @BeforeEach
        void setUp() {
                holdingService = new HoldingService(userRepository, accountRepository, holdingsRepository,
                                marketService);
        }

        @Test
        void shouldReturnHoldingsForUser() {

                // Arrange
                Account account = new Account();
                account.setAccount_type("ISK");
                account.setAccount_name("Annas ISK");

                Holdings holding = new Holdings();
                holding.setId(10L);
                holding.setTicker("ERIC-B");
                holding.setAccount(account);

                when(holdingsRepository.findHoldingsForUser(1L)).thenReturn(List.of(holding));

                // Act
                List<Map<String, Object>> actualHoldings = holdingService.getHoldingsForUser(1);

                // Assert
                assertEquals(1, actualHoldings.size());
                assertEquals(10L, actualHoldings.get(0).get("id"));
                assertEquals("ERIC-B", actualHoldings.get(0).get("ticker"));
                assertEquals("ISK", actualHoldings.get(0).get("account_type"));
                assertEquals("Annas ISK", actualHoldings.get(0).get("account_name"));

                verify(holdingsRepository).findHoldingsForUser(1L);
        }

        @Test
        void shouldCalculateMarketValueAndPnl() {
                // Arrange
                Integer userId = 1;

                Account account = new Account();
                Holdings holding = new Holdings();
                holding.setTicker("ERIC-B");
                holding.setQuantity(new BigDecimal("10"));
                holding.setAvg_buy_price(new BigDecimal("50"));
                holding.setAccount(account);

                when(holdingsRepository.findHoldingsForUser(userId.longValue()))
                                .thenReturn(List.of(holding));
                when(marketService.getPrice("ERIC-B"))
                                .thenReturn(Optional.of(new BigDecimal("80.00")));

                // Act
                List<Map<String, Object>> result = holdingService.getEnrichedHoldingsForUser(userId);

                // Assert
                Map<String, Object> enrichedHolding = result.get(0);

                assertEquals(80.0, enrichedHolding.get("currentPrice"));
                assertEquals(800.0, enrichedHolding.get("marketValue"));
                assertEquals(300.0, enrichedHolding.get("pnl"));
                verify(holdingsRepository).findHoldingsForUser(userId.longValue());
                verify(marketService).getPrice("ERIC-B");
        }

        @Test
        void shouldReturnNullValuesWhenMarketPriceIsUnavailable() {
        // Arrange
        Integer userId = 1;

        Holdings holding = new Holdings();
        holding.setTicker("ERIC-B");
        holding.setQuantity(new BigDecimal("10"));
        holding.setAvg_buy_price(new BigDecimal("50"));
        holding.setAccount(new Account());

        when(holdingsRepository.findHoldingsForUser(userId.longValue()))
                .thenReturn(List.of(holding));

        when(marketService.getPrice("ERIC-B"))
                .thenReturn(Optional.empty());

        // Act
        List<Map<String, Object>> result =
                holdingService.getEnrichedHoldingsForUser(userId);

        // Assert
        Map<String, Object> enrichedHolding = result.get(0);

        assertNull(enrichedHolding.get("currentPrice"));
        assertNull(enrichedHolding.get("marketValue"));
        assertNull(enrichedHolding.get("pnl"));

        verify(marketService).getPrice("ERIC-B");
        }

        @Test
        void shouldReturnEmptyListWhenUserHasNoHoldings() {
                // Arrange
                Integer userId = 1;

                when(holdingsRepository.findHoldingsForUser(userId.longValue()))
                                .thenReturn(List.of());

                // Act
                List<Map<String, Object>> result = holdingService.getEnrichedHoldingsForUser(userId);

                // Assert
                assertTrue(result.isEmpty());
                verify(holdingsRepository).findHoldingsForUser(userId.longValue());
        }

        @Test
        void shouldRejectInvalidQuantity() {
                String email = "anna@example.com";
                User user = new User();
                user.setId(7L);
                when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
                when(accountRepository.existsByIdAndUser_Id(2L, 7L)).thenReturn(true);
                CreateHoldingRequestDTO request = new CreateHoldingRequestDTO(
                                2, "AAPL", "Apple", "felaktigt-tal", "180.50", "USD");

                assertThrows(
                                NumberFormatException.class,
                                () -> holdingService.addHoldingForAuthenticatedUser(email, request));

                verify(accountRepository).existsByIdAndUser_Id(2L, 7L);
                verifyNoInteractions(holdingsRepository);
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
                verifyNoInteractions(accountRepository);
        }

        @Test
        void shouldReturnHoldingsForAuthenticatedUser() {
                // Arrange: the authenticated identity matches a database user.
                String email = "anna@example.com";
                User user = new User();
                user.setId(7L);
                user.setName("Anna");
                user.setEmail(email);

                Account account = new Account();
                account.setId(3L);
                account.setAccount_type("ISK");
                account.setAccount_name("Annas ISK");

                Holdings holding = new Holdings();
                holding.setId(10L);
                holding.setTicker("ERIC-B");
                holding.setQuantity(new BigDecimal("10"));
                holding.setAvg_buy_price(new BigDecimal("50"));
                holding.setAccount(account);

                when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
                Pageable pageable = PageRequest.of(1, 10);
                when(holdingsRepository.findAllByUserId(7L, pageable))
                                .thenReturn(new PageImpl<>(List.of(holding), pageable, 11));
                when(accountRepository.findByUserId(7L)).thenReturn(List.of(account));
                when(marketService.getPrice("ERIC-B"))
                                .thenReturn(Optional.of(new BigDecimal("74.20")));

                // Act
                HoldingResponseDTO result = holdingService.getHoldingsForAuthenticatedUser(email, pageable);

                // Assert: both repositories use the authenticated user's database ID.
                assertEquals("Anna", result.userName());
                assertEquals(1, result.holdings().size());
                assertEquals("ERIC-B", result.holdings().get(0).ticker());
                assertEquals(742.0, result.holdings().get(0).marketValue());
                assertEquals(3L, result.accounts().get(0).id());
                assertEquals("ISK", result.accounts().get(0).accountType());
                assertEquals(1, result.page());
                assertEquals(10, result.size());
                assertEquals(11, result.totalElements());
                assertEquals(2, result.totalPages());
                verify(userRepository).findByEmail(email);
                verify(holdingsRepository).findAllByUserId(7L, pageable);
                verify(accountRepository).findByUserId(7L);
        }

        @Test
        void shouldRejectAuthenticatedUserWhoDoesNotExist() {
                // Arrange
                String email = "missing@example.com";
                when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

                // Act and Assert: a missing user is rejected before holdings are fetched.
                assertThrows(BadCredentialsException.class,
                                () -> holdingService.getHoldingsForAuthenticatedUser(email, Pageable.unpaged()));
                verify(userRepository).findByEmail(email);
                verifyNoInteractions(accountRepository, holdingsRepository);
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

                Account account = new Account();
                account.setId(3L);
                when(accountRepository.findById(3L)).thenReturn(Optional.of(account));

                // Act
                holdingService.addHoldingForAuthenticatedUser(email, request);

                // Assert: ownership is checked and the holding is saved with normalized values.
                ArgumentCaptor<Holdings> captor = ArgumentCaptor.forClass(Holdings.class);
                verify(holdingsRepository).save(captor.capture());

                Holdings savedHolding = captor.getValue();
                assertEquals("AAPL", savedHolding.getTicker());
                assertEquals("Apple", savedHolding.getInstrument_name());
                assertEquals(new BigDecimal("5"), savedHolding.getQuantity());
                assertEquals(new BigDecimal("180.50"), savedHolding.getAvg_buy_price());

                assertEquals(account, savedHolding.getAccount());
                verify(accountRepository).existsByIdAndUser_Id(3L, 7L);
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
                verifyNoInteractions(holdingsRepository);
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
                verifyNoInteractions(accountRepository, holdingsRepository);
        }

        @Test
        void shouldRejectInvalidBuyPriceWithoutSavingHolding() {
                String email = "anna@example.com";
                User user = new User();
                user.setId(7L);
                when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
                when(accountRepository.existsByIdAndUser_Id(3L, 7L)).thenReturn(true);
                CreateHoldingRequestDTO request = new CreateHoldingRequestDTO(
                                3, "AAPL", "Apple", "5", "invalid-price", "USD");

                assertThrows(NumberFormatException.class,
                                () -> holdingService.addHoldingForAuthenticatedUser(email, request));
                verify(accountRepository).existsByIdAndUser_Id(3L, 7L);
                verifyNoInteractions(holdingsRepository);
        }

        @Test
        void shouldRejectDeletingUnownedOrMissingHolding() {
                // Arrange: no holding matches both the ID and the authenticated owner.
                User user = new User();
                user.setId(7L);
                when(userRepository.findByEmail("anna@example.com")).thenReturn(Optional.of(user));
                when(holdingsRepository.deleteOwnedHolding(15L, 7L)).thenReturn(0);

                // Act and Assert: reject deletion when no holding belongs to the user
                assertThrows(AccessDeniedException.class,
                                () -> holdingService.deleteHoldingForAuthenticatedUser("anna@example.com", 15));
                verify(holdingsRepository).deleteOwnedHolding(15L, 7L);
        }

        @Test
        void shouldRejectDeletingWhenUserDoesNotExist() {
                when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
                assertThrows(BadCredentialsException.class,
                                () -> holdingService.deleteHoldingForAuthenticatedUser("missing@example.com", 15));
                verifyNoInteractions(accountRepository, holdingsRepository);
        }

}
