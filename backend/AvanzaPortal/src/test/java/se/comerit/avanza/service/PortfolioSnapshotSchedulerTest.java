package se.comerit.avanza.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import se.comerit.avanza.config.PortfolioSnapshotScheduler;
import se.comerit.avanza.entity.User;
import se.comerit.avanza.repository.UserRepository;

class PortfolioSnapshotSchedulerTest {

    @Test
    void importsPricesBeforeCapturingEachUsersSnapshot() {
        MarketService marketService = mock(MarketService.class);
        PortfolioSnapshotService snapshotService = mock(PortfolioSnapshotService.class);
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setId(17L);

        when(userRepository.findAll()).thenReturn(List.of(user));

        PortfolioSnapshotScheduler scheduler = new PortfolioSnapshotScheduler(
                marketService,
                snapshotService,
                userRepository);

        scheduler.importPricesAndCaptureSnapshots();

        InOrder order = inOrder(marketService, userRepository, snapshotService);
        order.verify(marketService).importHistoricalData();
        order.verify(userRepository).findAll();
        order.verify(snapshotService).captureLatestSnapshotForUser(eq(17L), any(LocalDate.class));
    }
}