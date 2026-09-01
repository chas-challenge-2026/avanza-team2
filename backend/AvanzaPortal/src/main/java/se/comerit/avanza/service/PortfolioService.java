package se.comerit.avanza.service;

import org.springframework.stereotype.Service;
import java.util.List;

import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.entity.Account;

/**
 * PortfolioService is a layer in between the controller and the repository.
 * It provides methods that will be used in controller.
 */
@Service
public class PortfolioService {

    private AccountRepository accountRepository;

    public PortfolioService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Reconstructs Query 1 from v1 controller.
     * 
     * @param userId the ID of the user whose accounts are to be retrieved.
     * @return a list of accounts associated with the specified user.
     */
    public List<Account> getAllAccountsForUser(Long userId) {
        return accountRepository.findAllByUser_Id(userId);
    }
}
