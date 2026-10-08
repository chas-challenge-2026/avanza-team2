package se.comerit.avanza.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.comerit.avanza.dto.targetallocation.TargetAllocationResponseDTO;
import se.comerit.avanza.entity.TargetAllocations;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.repository.TargetRepository;
import se.comerit.avanza.repository.UserRepository;

@Service 
public class TargetAllocationService {

    private static final BigDecimal TOTAL_PERCANTAGE = new BigDecimal("100.0");

    private final TargetRepository targetRepository;
    private final UserRepository userRepository;

    public TargetAllocationService(TargetRepository targetRepository, UserRepository userRepository) {
        this.targetRepository = targetRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TargetAllocationResponseDTO> getTargetAllocationsForAuthenticatedUser (String email)
    {
        User user = findAuthenticatedUser(email);

        return targetRepository.findByUser_Id(user.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private TargetAllocationResponseDTO toResponseDTO(TargetAllocations allocation) {
    
    return new TargetAllocationResponseDTO(
            allocation.getId(),
            allocation.getAccount_type(),
            BigDecimal.valueOf(allocation.getTarget_pct()));
    }



    private User findAuthenticatedUser(String email) {
    return userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new BadCredentialsException(
                            "Authentication failed: User not found"));
    }


    
}
