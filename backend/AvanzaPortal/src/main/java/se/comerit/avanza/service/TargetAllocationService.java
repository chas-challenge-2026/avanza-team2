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
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.repository.TargetRepository;
import se.comerit.avanza.repository.UserRepository;

@Service 
public class TargetAllocationService {

    private static final BigDecimal TOTAL_PERCENTAGE = new BigDecimal("100.0");

    private final TargetRepository targetRepository;
    private final UserRepository userRepository;

    public TargetAllocationService(TargetRepository targetRepository, UserRepository userRepository) {
        this.targetRepository = targetRepository;
        this.userRepository = userRepository;
    }
    
    // Get target allocations for the authenticated user
    @Transactional(readOnly = true)
    public List<TargetAllocationResponseDTO> getTargetAllocationsForAuthenticatedUser (String email)
    {
        User user = findAuthenticatedUser(email);

        return targetRepository.findByUser_Id(user.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // Update target allocations for the authenticated user
    @Transactional
    public List<TargetAllocationResponseDTO> updateTargetAllocationsForAuthenticatedUser( String email, UpdateTargetAllocationsRequestDTO request) {

        // Validate the request
        validateAllocations(request.allocations());
        User user = findAuthenticatedUser(email);

        // Delete existing allocations for the user
        List<TargetAllocations> existingAllocations = targetRepository.findByUser_Id(user.getId());
        targetRepository.deleteAll(existingAllocations);

        // Create new allocations based on the request
        List<TargetAllocations> updatedAllocations = request.allocations()
            .stream()
            .map(allocation -> new TargetAllocations(
                    normalizeAccountType(allocation.accountType()),
                    allocation.targetPercentage().doubleValue(),
                    user))
            .toList();
        
        // Save the new allocations and return the response DTOs
        return targetRepository.saveAll(updatedAllocations)
                .stream()
                .map(this::toResponseDTO)
                .toList();

    }

    // Helper method
    private TargetAllocationResponseDTO toResponseDTO(TargetAllocations allocation) {
    return new TargetAllocationResponseDTO(
            allocation.getId(),
            allocation.getAccount_type(),
            BigDecimal.valueOf(allocation.getTarget_pct()));
    }


    // Helper method
    private User findAuthenticatedUser(String email) {
    return userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new BadCredentialsException(
                            "Authentication failed: User not found"));
    }
    
    // Helper method
    private String normalizeAccountType(String accountType) {
        return switch (accountType.trim().toUpperCase(Locale.ROOT)) {
            case "ISK" -> "ISK";
            case "KF" -> "KF";
            case "DEPA", "DEPÅ" -> "Depa";
            case "PENSION" -> "Pension";
            default -> throw new IllegalArgumentException(
                    "Unsupported account type: " + accountType);
        };
    }


    // Helper method
    private void validateAllocations(
        List<TargetAllocationRequestDTO> allocations) {

    BigDecimal total = allocations.stream()
            .map(TargetAllocationRequestDTO::targetPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (total.compareTo(TOTAL_PERCENTAGE) != 0) {
        throw new IllegalArgumentException(
                "Target allocation percentages must total 100");
    }

    Set<String> accountTypes = new HashSet<>();

    for (TargetAllocationRequestDTO allocation : allocations) {
        String normalizedType = normalizeAccountType(allocation.accountType())
                .toLowerCase(Locale.ROOT);

        if (!accountTypes.add(normalizedType)) {
            throw new IllegalArgumentException(
                    "Duplicate account type: " + allocation.accountType());
        }
    }
    }

}
