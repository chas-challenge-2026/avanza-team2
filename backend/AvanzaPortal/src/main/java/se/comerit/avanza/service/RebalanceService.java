package se.comerit.avanza.service;

import org.springframework.stereotype.Service;

import se.comerit.avanza.repository.AccountRepository;
import se.comerit.avanza.repository.HoldingsRepository;

@Service
public class RebalanceService {

    /**
     * Apply rebalance operation means that method will adjust amounts of shares
     * to match target allocation. The adjustment will be calculated only per
     * account level.
     * 
     * @param entity
     */
    public void applyRebalance(String entity) {

    }

    /**
     * Preview rebalance operation
     * 
     * @param param
     * @return preview result
     */
    public void previewRebalance(String param) {
    }
}
