package se.comerit.avanza.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.AlertsRepository;
import se.comerit.avanza.repository.TargetRepository;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertService alertService;

    @Mock
    private AlertsRepository alertsRepository;

    @Mock 
    private AccountRepository accountRepository;

    @Mock 
    private TargetRepository targetRepository;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(alertsRepository, accountRepository, targetRepository);
    }


    

    




    
    
}
