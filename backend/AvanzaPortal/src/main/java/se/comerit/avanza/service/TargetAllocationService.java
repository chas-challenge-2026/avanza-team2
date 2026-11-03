package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * Service for managing target allocations for authenticated users.
 */
@Service
public class TargetAllocationService {

    private static final BigDecimal TOTAL_PERCENTAGE = new BigDecimal("100.0");

    private final TargetRepository targetRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public TargetAllocationService(TargetRepository targetRepository, UserRepository userRepository,
            AccountRepository accountRepository) {
        this.targetRepository = targetRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Retrieves the target allocations for the authenticated user.
     *
     * @param email the email of the authenticated user
     * @return a list of target allocation response DTOs
     */
    @Transactional(readOnly = true)
    public List<TargetAllocationResponseDTO> getTargetAllocationsForAuthenticatedUser(String email) {
        User user = findAuthenticatedUser(email);

        return targetRepository.findByUser_Id(user.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Updates the target allocations for the authenticated user.
     *
     * @param email   the email of the authenticated user
     * @param request the request containing the updated target allocations
     * @return a list of target allocation response DTOs
     */
    @Transactional
    public List<TargetAllocationResponseDTO> updateTargetAllocationsForAuthenticatedUser(String email,
            UpdateTargetAllocationsRequestDTO request) {

        validatePortfolioAllocations(request.allocations());
        User user = findAuthenticatedUser(email);

        List<TargetAllocations> existingAllocations = targetRepository.findByUser_IdAndAccountIsNull(user.getId());
        targetRepository.deleteAll(existingAllocations);

        List<TargetAllocations> updatedAllocations = request.allocations()
                .stream()
                .map(allocation -> new TargetAllocations(
                        normalizeAccountType(allocation.accountType()),
                        allocation.targetPercentage().doubleValue(),
                        user))
                .toList();

        return targetRepository.saveAll(updatedAllocations)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public List<TargetAllocationResponseDTO> updateAccountTargetAllocationsForAuthenticatedUser(
            String email, Long accountId, UpdateTargetAllocationsRequestDTO request) {

        validateAccountAllocations(request.allocations());
        User user = findAuthenticatedUser(email);
        Account account = accountRepository.findById(accountId)
            .filter(foundAccount -> foundAccount.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Replace only the target allocations for the selected account.
        List<TargetAllocations> existingAllocations = targetRepository
                .findByAccount_IdAndUser_Id(account.getId(), user.getId());
        targetRepository.deleteAll(existingAllocations);

        // Create new allocations based on the request
        List<TargetAllocations> updatedAllocations = request.allocations()
                .stream()
                .map(allocation -> new TargetAllocations(
                        normalizeAssetType(allocation.accountType()),
                        allocation.targetPercentage().doubleValue(),
                        user,
                        account))
                .toList();

        // Save the new allocations and return the response DTOs
        return targetRepository.saveAll(updatedAllocations)
                .stream()
                .map(this::toResponseDTO)
                .toList();

    }

    /**
     * Converts a TargetAllocations entity to a TargetAllocationResponseDTO.
     *
     * @param allocation the target allocation entity
     * @return the corresponding response DTO
     */
    private TargetAllocationResponseDTO toResponseDTO(TargetAllocations allocation) {
        return new TargetAllocationResponseDTO(
                allocation.getId(),
                allocation.getAccount() == null ? null : allocation.getAccount().getId(),
                allocation.getAccount_type(),
                BigDecimal.valueOf(allocation.getTarget_pct()));
    }

    /**
     * Finds the authenticated user by email.
     *
     * @param email the email of the authenticated user
     * @return the authenticated user entity
     * @throws BadCredentialsException if the user is not found
     */
    private User findAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException(
                        "Authentication failed: User not found"));
    }

    /**
     * Normalizes the account type to a standard format.
     *
     * @param accountType the account type to normalize
     * @return the normalized account type
     * @throws InvalidRequestException if the account type is unsupported
     */
    private String normalizeAssetType(String accountType) {
        return switch (accountType.trim().toUpperCase(Locale.ROOT)) {
            case "AKTIER" -> "Aktier";
            case "FONDER" -> "Fonder";
            default -> throw new InvalidRequestException(
                    "Unsupported allocation type: " + accountType);
        };
    }

    private String normalizeAccountType(String accountType) {
        return switch (accountType.trim().toUpperCase(Locale.ROOT)) {
            case "ISK" -> "ISK";
            case "KF" -> "KF";
            case "DEPA", "DEPÅ" -> "Depa";
            case "PENSION" -> "Pension";
            default -> throw new InvalidRequestException("Unsupported account type: " + accountType);
        };
    }

    /**
     * Validates the target allocations to ensure they meet the required criteria.
     *
     * @param allocations the list of target allocation request DTOs
     * @throws InvalidRequestException if the allocations are invalid
     */
    private void validateAccountAllocations(
            List<TargetAllocationRequestDTO> allocations) {

        validateTotalPercentage(allocations);

        Set<String> allocationTypes = new HashSet<>();

        for (TargetAllocationRequestDTO allocation : allocations) {
            String normalizedType = normalizeAssetType(allocation.accountType()).toLowerCase(Locale.ROOT);

            if (!allocationTypes.add(normalizedType)) {
                throw new InvalidRequestException(
                        "Duplicate allocation type: " + allocation.accountType());
            }
        }

        if (!allocationTypes.equals(Set.of("aktier", "fonder"))) {
            throw new InvalidRequestException("Account allocations must include Aktier and Fonder");
        }
    }

    private void validatePortfolioAllocations(List<TargetAllocationRequestDTO> allocations) {
        validateTotalPercentage(allocations);

        Set<String> accountTypes = new HashSet<>();

        for (TargetAllocationRequestDTO allocation : allocations) {
            String normalizedType = normalizeAccountType(allocation.accountType()).toLowerCase(Locale.ROOT);

            if (!accountTypes.add(normalizedType)) {
                throw new InvalidRequestException(
                        "Duplicate account type: " + allocation.accountType());
            }
        }
    }

    private void validateTotalPercentage(List<TargetAllocationRequestDTO> allocations) {
        BigDecimal total = allocations.stream()
                .map(TargetAllocationRequestDTO::targetPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(TOTAL_PERCENTAGE) != 0) {
            throw new InvalidRequestException(
                    "Target allocation percentages must total 100");
        }
    }
}
