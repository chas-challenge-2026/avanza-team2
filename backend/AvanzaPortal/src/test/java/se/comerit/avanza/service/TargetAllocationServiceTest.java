package se.comerit.avanza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.exception.InvalidTargetAllocationException;
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

    private TargetAllocationService targetAllocationService;

    @BeforeEach
    void setUp() {
        targetAllocationService = new TargetAllocationService(targetRepository, userRepository);
    }

    @Test
    void shouldReturnTargetAllocationsForAuthenticatedUser() {
        User user = createUser();
        TargetAllocations allocation = new TargetAllocations("ISK", 60.0, user);
        allocation.setId(10L);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(targetRepository.findByUser_Id(USER_ID)).thenReturn(List.of(allocation));

        List<TargetAllocationResponseDTO> result = targetAllocationService
                .getTargetAllocationsForAuthenticatedUser(USER_EMAIL);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());
        assertEquals("ISK", result.get(0).accountType());
        assertEquals(0, new BigDecimal("60.0").compareTo(result.get(0).targetPercentage()));
        verify(userRepository).findByEmail(USER_EMAIL);
        verify(targetRepository).findByUser_Id(USER_ID);
    }

    @Test
    void shouldReplaceAllocationsForAuthenticatedUser() {
        User user = createUser();
        List<TargetAllocations> existingAllocations = List.of(
                new TargetAllocations("ISK", 100.0, user));
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("isk", "60.00"),
                allocation("depå", "40.00"));

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(targetRepository.findByUser_Id(USER_ID)).thenReturn(existingAllocations);
        when(targetRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<TargetAllocationResponseDTO> result = targetAllocationService
                .updateTargetAllocationsForAuthenticatedUser(USER_EMAIL, request);

        assertEquals(2, result.size());
        assertEquals("ISK", result.get(0).accountType());
        assertEquals("Depa", result.get(1).accountType());
        verify(targetRepository).deleteAll(existingAllocations);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TargetAllocations>> captor = ArgumentCaptor.forClass(List.class);
        verify(targetRepository).saveAll(captor.capture());

        List<TargetAllocations> savedAllocations = captor.getValue();
        assertEquals(2, savedAllocations.size());
        assertSame(user, savedAllocations.get(0).getUser());
        assertSame(user, savedAllocations.get(1).getUser());
        assertEquals("ISK", savedAllocations.get(0).getAccount_type());
        assertEquals("Depa", savedAllocations.get(1).getAccount_type());
        assertEquals(60.0, savedAllocations.get(0).getTarget_pct());
        assertEquals(40.0, savedAllocations.get(1).getTarget_pct());
    }

    @Test
    void shouldRejectAllocationsWhenTotalIsNotOneHundred() {
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("ISK", "60.00"),
                allocation("KF", "30.00"));

        InvalidTargetAllocationException exception = assertThrows(
                InvalidTargetAllocationException.class,
                () -> targetAllocationService
                        .updateTargetAllocationsForAuthenticatedUser(USER_EMAIL, request));

        assertEquals("Target allocation percentages must total 100", exception.getMessage());
        verifyNoInteractions(userRepository, targetRepository);
    }

    @Test
    void shouldRejectDuplicateAccountTypesIgnoringCase() {
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("ISK", "50.00"),
                allocation("isk", "50.00"));

        InvalidTargetAllocationException exception = assertThrows(
                InvalidTargetAllocationException.class,
                () -> targetAllocationService
                        .updateTargetAllocationsForAuthenticatedUser(USER_EMAIL, request));

        assertEquals("Duplicate account type: isk", exception.getMessage());
        verifyNoInteractions(userRepository, targetRepository);
    }

    @Test
    void shouldRejectUnsupportedAccountType() {
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("Savings", "100.00"));

        InvalidTargetAllocationException exception = assertThrows(
                InvalidTargetAllocationException.class,
                () -> targetAllocationService
                        .updateTargetAllocationsForAuthenticatedUser(USER_EMAIL, request));

        assertEquals("Unsupported account type: Savings", exception.getMessage());
        verifyNoInteractions(userRepository, targetRepository);
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
        verifyNoInteractions(targetRepository);
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

    private User createUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        return user;
    }

    private TargetAllocationRequestDTO allocation(String accountType, String percentage) {
        return new TargetAllocationRequestDTO(accountType, new BigDecimal(percentage));
    }

    private UpdateTargetAllocationsRequestDTO request(TargetAllocationRequestDTO... allocations) {
        return new UpdateTargetAllocationsRequestDTO(List.of(allocations));
    }
}
