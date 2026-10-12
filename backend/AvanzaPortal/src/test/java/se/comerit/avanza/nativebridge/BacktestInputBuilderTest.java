package se.comerit.avanza.nativebridge;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import se.comerit.avanza.entity.HistoricalStockPrice;
import se.comerit.avanza.repository.HistoricalRepository;

class BacktestInputBuilderTest {

    @Test
    void buildsRowMajorPricesInRequestedTickerOrderAndSharedDateOrder() {
        HistoricalRepository repository = mock(HistoricalRepository.class);
        List<String> tickers = List.of("VOLV-B", "ERIC-B");
        LocalDate firstDate = LocalDate.of(2026, 10, 1);
        LocalDate secondDate = firstDate.plusDays(1);
        LocalDate thirdDate = secondDate.plusDays(1);
        when(repository.findByTickerInAndPriceDateBetween(tickers, firstDate, thirdDate))
                .thenReturn(List.of(
                        price("ERIC-B", thirdDate, "11"),
                        price("VOLV-B", secondDate, "21"),
                        price("ERIC-B", firstDate, "10"),
                        price("VOLV-B", thirdDate, "22"),
                        price("VOLV-B", firstDate, "20"),
                        price("ERIC-B", secondDate, "10.5")));
        BacktestInputBuilder builder = new BacktestInputBuilder(repository);

        BacktestInputBuilder.BacktestInput input = builder.build(tickers, firstDate, thirdDate);

        assertEquals(tickers, input.tickers());
        assertEquals(List.of(firstDate, secondDate, thirdDate), input.dates());
        assertEquals(2, input.instrumentCount());
        assertEquals(3, input.dayCount());
        assertArrayEquals(new double[] { 20.0, 10.0, 21.0, 10.5, 22.0, 11.0 }, input.prices());
        verify(repository).findByTickerInAndPriceDateBetween(tickers, firstDate, thirdDate);
    }

    @Test
    void excludesDatesWithoutPricesForEveryTicker() {
        HistoricalRepository repository = mock(HistoricalRepository.class);
        List<String> tickers = List.of("VOLV-B", "ERIC-B");
        LocalDate firstDate = LocalDate.of(2026, 10, 1);
        LocalDate secondDate = firstDate.plusDays(1);
        LocalDate thirdDate = secondDate.plusDays(1);
        LocalDate fourthDate = thirdDate.plusDays(1);
        when(repository.findByTickerInAndPriceDateBetween(tickers, firstDate, fourthDate))
                .thenReturn(List.of(
                        price("ERIC-B", firstDate, "10"),
                        price("ERIC-B", secondDate, "11"),
                        price("VOLV-B", secondDate, "21"),
                        price("ERIC-B", thirdDate, "12"),
                        price("VOLV-B", thirdDate, "22"),
                        price("VOLV-B", fourthDate, "23")));
        BacktestInputBuilder builder = new BacktestInputBuilder(repository);

        BacktestInputBuilder.BacktestInput input = builder.build(tickers, firstDate, fourthDate);

        assertEquals(List.of(secondDate, thirdDate), input.dates());
        assertArrayEquals(new double[] { 21.0, 11.0, 22.0, 12.0 }, input.prices());
    }

    @Test
    void rejectsInputWithFewerThanTwoSharedDates() {
        HistoricalRepository repository = mock(HistoricalRepository.class);
        List<String> tickers = List.of("VOLV-B", "ERIC-B");
        LocalDate date = LocalDate.of(2026, 10, 1);
        when(repository.findByTickerInAndPriceDateBetween(tickers, date, date))
                .thenReturn(List.of(price("VOLV-B", date, "20"), price("ERIC-B", date, "10")));
        BacktestInputBuilder builder = new BacktestInputBuilder(repository);

        assertThrows(IllegalStateException.class, () -> builder.build(tickers, date, date));
    }

    @Test
    void rejectsMissingOrInvalidArguments() {
        BacktestInputBuilder builder = new BacktestInputBuilder(mock(HistoricalRepository.class));
        LocalDate date = LocalDate.of(2026, 10, 1);

        assertThrows(IllegalArgumentException.class, () -> builder.build(List.of(), date, date));
        assertThrows(IllegalArgumentException.class, () -> builder.build(List.of(" "), date, date));
        assertThrows(IllegalArgumentException.class, () -> builder.build(List.of("AAPL"), null, date));
        assertThrows(IllegalArgumentException.class, () -> builder.build(List.of("AAPL"), date, date.minusDays(1)));
    }

    private HistoricalStockPrice price(String ticker, LocalDate date, String close) {
        return new HistoricalStockPrice(ticker, date, new BigDecimal(close));
    }
}