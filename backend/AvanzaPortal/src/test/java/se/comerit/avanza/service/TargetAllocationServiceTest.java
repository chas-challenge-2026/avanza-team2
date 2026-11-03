package se.comerit.avanza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import se.comerit.avanza.dto.targetallocation.TargetAllocationRequestDTO;
import se.comerit.avanza.dto.targetallocation.TargetAllocationResponseDTO;
import se.comerit.avanza.dto.targetallocation.UpdateTargetAllocationsRequestDTO;
import se.comerit.avanza.entity.Account;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.exception.InvalidRequestException;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.TargetRepository;
import se.comerit.avanza.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TargetAllocationServiceTest {

    private static final String USER_EMAIL = "anna@example.com";
    private static final Long USER_ID = 1L;

    @Mock
    private TargetRepository targetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    private TargetAllocationService targetAllocationService;

    @BeforeEach
    void setUp() {
        targetAllocationService = new TargetAllocationService(targetRepository, userRepository, accountRepository);
    }

    @Test
    void shouldReturnTargetAllocationsForAuthenticatedUser() {
        User user = createUser();
        Account account = createAccount(user);
        TargetAllocations allocation = new TargetAllocations("Aktier", 60.0, user, account);
        allocation.setId(10L);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(targetRepository.findByUser_Id(USER_ID)).thenReturn(List.of(allocation));

        List<TargetAllocationResponseDTO> result = targetAllocationService
                .getTargetAllocationsForAuthenticatedUser(USER_EMAIL);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());
        assertEquals(20L, result.get(0).accountId());
        assertEquals("Aktier", result.get(0).accountType());
        assertEquals(0, new BigDecimal("60.0").compareTo(result.get(0).targetPercentage()));
        verify(userRepository).findByEmail(USER_EMAIL);
        verify(targetRepository).findByUser_Id(USER_ID);
    }

    @Test
    void shouldReplaceOnlySelectedAccountsAllocations() {
        User user = createUser();
        Account account = createAccount(user);
        List<TargetAllocations> existingAllocations = List.of(
                new TargetAllocations("Aktier", 100.0, user, account));
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("aktier", "60.00"),
                allocation("fonder", "40.00"));

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUser_Id(20L, USER_ID)).thenReturn(Optional.of(account));
        when(targetRepository.findByAccount_IdAndUser_Id(20L, USER_ID)).thenReturn(existingAllocations);
        when(targetRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<TargetAllocationResponseDTO> result = targetAllocationService
                .updateAccountTargetAllocationsForAuthenticatedUser(USER_EMAIL, 20L, request);

        assertEquals(2, result.size());
        assertEquals("Aktier", result.get(0).accountType());
        assertEquals("Fonder", result.get(1).accountType());
        verify(targetRepository).deleteAll(existingAllocations);
        verify(targetRepository).findByAccount_IdAndUser_Id(20L, USER_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TargetAllocations>> captor = ArgumentCaptor.forClass(List.class);
        verify(targetRepository).saveAll(captor.capture());

        List<TargetAllocations> savedAllocations = captor.getValue();
        assertEquals(2, savedAllocations.size());
        assertSame(user, savedAllocations.get(0).getUser());
        assertSame(user, savedAllocations.get(1).getUser());
        assertSame(account, savedAllocations.get(0).getAccount());
        assertSame(account, savedAllocations.get(1).getAccount());
        assertEquals("Aktier", savedAllocations.get(0).getAccount_type());
        assertEquals("Fonder", savedAllocations.get(1).getAccount_type());
        assertEquals(60.0, savedAllocations.get(0).getTarget_pct());
        assertEquals(40.0, savedAllocations.get(1).getTarget_pct());
    }

    @Test
    void shouldUpdatePortfolioAllocationsWithoutReplacingAccountTargets() {
        User user = createUser();
        List<TargetAllocations> existingAllocations = List.of(
                new TargetAllocations("ISK", 60.0, user),
                new TargetAllocations("KF", 40.0, user));
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("ISK", "50.00"),
                allocation("KF", "50.00"));

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(targetRepository.findByUser_IdAndAccountIsNull(USER_ID)).thenReturn(existingAllocations);
        when(targetRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        targetAllocationService.updateTargetAllocationsForAuthenticatedUser(USER_EMAIL, request);

        verify(targetRepository).deleteAll(existingAllocations);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TargetAllocations>> captor = ArgumentCaptor.forClass(List.class);
        verify(targetRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertSame(user, captor.getValue().get(0).getUser());
        assertNull(captor.getValue().get(0).getAccount());
        verifyNoInteractions(accountRepository);
    }

    @Test
    void shouldRejectAllocationsWhenTotalIsNotOneHundred() {
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("ISK", "60.00"),
                allocation("KF", "30.00"));

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> targetAllocationService
                        .updateTargetAllocationsForAuthenticatedUser(USER_EMAIL, request));

        assertEquals("Target allocation percentages must total 100", exception.getMessage());
        verifyNoInteractions(userRepository, targetRepository, accountRepository);
    }

    @Test
    void shouldRejectDuplicateAccountTypesIgnoringCase() {
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("ISK", "50.00"),
                allocation("isk", "50.00"));

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> targetAllocationService
                        .updateTargetAllocationsForAuthenticatedUser(USER_EMAIL, request));

        assertEquals("Duplicate account type: isk", exception.getMessage());
        verifyNoInteractions(userRepository, targetRepository, accountRepository);
    }

    @Test
    void shouldRejectUnsupportedAccountType() {
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("Savings", "100.00"));

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> targetAllocationService
                        .updateTargetAllocationsForAuthenticatedUser(USER_EMAIL, request));

        assertEquals("Unsupported account type: Savings", exception.getMessage());
        verifyNoInteractions(userRepository, targetRepository, accountRepository);
    }

    @Test
    void shouldRejectUpdateWhenAuthenticatedUserDoesNotExist() {
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("ISK", "100.00"));
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThrows(
                BadCredentialsException.class,
                () -> targetAllocationService
                        .updateTargetAllocationsForAuthenticatedUser(USER_EMAIL, request));

        verify(userRepository).findByEmail(USER_EMAIL);
        verifyNoInteractions(targetRepository, accountRepository);
    }

    @Test
    void shouldRejectGetWhenAuthenticatedUserDoesNotExist() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThrows(
                BadCredentialsException.class,
                () -> targetAllocationService
                        .getTargetAllocationsForAuthenticatedUser(USER_EMAIL));

        verify(userRepository).findByEmail(USER_EMAIL);
        verifyNoInteractions(targetRepository);
    }

    @Test
    void shouldRejectUpdateForAnotherUsersAccount() {
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("Aktier", "60.00"),
                allocation("Fonder", "40.00"));
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(createUser()));
        when(accountRepository.findByIdAndUser_Id(20L, USER_ID)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> targetAllocationService
                        .updateAccountTargetAllocationsForAuthenticatedUser(USER_EMAIL, 20L, request));

        verifyNoInteractions(targetRepository);
    }

    @Test
    void shouldRequireBothAssetTypesForAccountAllocations() {
        UpdateTargetAllocationsRequestDTO request = request(allocation("Aktier", "100.00"));

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> targetAllocationService
                        .updateAccountTargetAllocationsForAuthenticatedUser(USER_EMAIL, 20L, request));

        assertEquals("Account allocations must include Aktier and Fonder", exception.getMessage());
        verifyNoInteractions(userRepository, targetRepository, accountRepository);
    }

    private User createUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        return user;
    }

    private Account createAccount(User user) {
        Account account = new Account();
        account.setId(20L);
        account.setUser(user);
        return account;
    }

    private TargetAllocationRequestDTO allocation(String accountType, String percentage) {
        return new TargetAllocationRequestDTO(accountType, new BigDecimal(percentage));
    }

    private UpdateTargetAllocationsRequestDTO request(TargetAllocationRequestDTO... allocations) {
        return new UpdateTargetAllocationsRequestDTO(List.of(allocations));
    }
}
