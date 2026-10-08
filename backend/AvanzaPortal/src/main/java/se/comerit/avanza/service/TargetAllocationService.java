package se.comerit.avanza.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

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
    
}
