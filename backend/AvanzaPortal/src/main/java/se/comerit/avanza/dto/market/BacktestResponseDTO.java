package se.comerit.avanza.dto.market;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record BacktestResponseDTO(
        double totalReturn,
        double annualizedReturn,
        double maxDrawdown,
        double sharpeRatio,
        List<String> tickers,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") List<LocalDate> dates) {
}
