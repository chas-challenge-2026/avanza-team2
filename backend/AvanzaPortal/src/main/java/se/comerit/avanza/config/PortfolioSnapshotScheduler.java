package se.comerit.avanza.config;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import se.comerit.avanza.entity.User;
import se.comerit.avanza.repository.UserRepository;
import se.comerit.avanza.service.MarketService;
import se.comerit.avanza.service.PortfolioSnapshotService;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class PortfolioSnapshotScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioSnapshotScheduler.class);

    private final MarketService marketService;
    private final PortfolioSnapshotService snapshotService;
    private final UserRepository userRepository;

    public PortfolioSnapshotScheduler(
            MarketService marketService,
            PortfolioSnapshotService snapshotService,
            UserRepository userRepository) {
        this.marketService = marketService;
        this.snapshotService = snapshotService;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "${portfolio.snapshot.cron:0 0 7 * * MON-FRI}", zone = "Europe/Stockholm")
    public void importPricesAndCaptureSnapshots() {
        LocalDate throughDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        try {
            int importedRows = marketService.importHistoricalData();
            logger.info("Daily snapshot job imported {} historical price rows", importedRows);
        } catch (RuntimeException exception) {
            logger.warn("Historical import failed; trying snapshots using stored prices", exception);
        }

        for (User user : userRepository.findAll()) {
            try {
                int savedRows = snapshotService.captureLatestSnapshotForUser(user.getId(), throughDate);
                logger.info("Daily snapshot job saved {} rows for user {}", savedRows, user.getId());
            } catch (RuntimeException exception) {
                logger.warn("Daily snapshot capture failed for user {}", user.getId(), exception);
            }
        }
    }
}