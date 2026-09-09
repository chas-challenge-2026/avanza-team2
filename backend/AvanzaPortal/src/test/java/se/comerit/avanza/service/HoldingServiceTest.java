package se.comerit.avanza.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
public class HoldingServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private HoldingService holdingService;

    @BeforeEach
    void setUp() {
        holdingService = new HoldingService(jdbcTemplate);
    }

    @Test
    void shouldReturnHoldingsForUser() {

        // Arrange
        Integer userId = 1;

        Map<String, Object> holding = new HashMap<>();
        holding.put("id", 10);
        holding.put("ticker", "ERIC-B");

        List<Map<String, Object>> expectedHoldings = List.of(holding);

        when(jdbcTemplate.queryForList(anyString(), eq(userId))).thenReturn(expectedHoldings);

        // Act
        List<Map<String, Object>> actualHoldings = holdingService.getHoldingsForUser(userId);

        // Assert
        assertEquals(expectedHoldings, actualHoldings);
        assertEquals(1, actualHoldings.size());
        assertEquals("ERIC-B", actualHoldings.get(0).get("ticker"));

        verify(jdbcTemplate).queryForList(anyString(), eq(userId));



        
    }
    
    
}
