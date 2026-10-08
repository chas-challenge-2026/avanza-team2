package se.comerit.avanza.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.comerit.avanza.dto.targetallocation.TargetAllocationRequestDTO;
import se.comerit.avanza.dto.targetallocation.TargetAllocationResponseDTO;
import se.comerit.avanza.dto.targetallocation.UpdateTargetAllocationsRequestDTO;
import se.comerit.avanza.exception.InvalidTargetAllocationException;
import se.comerit.avanza.service.TargetAllocationService;

class TargetAllocationControllerTest {

    private static final String USER_EMAIL = "anna@example.com";

    private TargetAllocationService targetAllocationService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        targetAllocationService = org.mockito.Mockito.mock(TargetAllocationService.class);
        TargetAllocationController controller = new TargetAllocationController(targetAllocationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        authentication = new UsernamePasswordAuthenticationToken(USER_EMAIL, null, List.of());
    }

    @Test
    void shouldReturnTargetAllocationsForAuthenticatedUser() throws Exception {
        List<TargetAllocationResponseDTO> response = List.of(
                new TargetAllocationResponseDTO(10L, "ISK", new BigDecimal("60.00")));
        when(targetAllocationService.getTargetAllocationsForAuthenticatedUser(USER_EMAIL))
                .thenReturn(response);

        mockMvc.perform(get("/api/target-allocations").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].accountType").value("ISK"))
                .andExpect(jsonPath("$[0].targetPercentage").value(60.00));

        verify(targetAllocationService).getTargetAllocationsForAuthenticatedUser(USER_EMAIL);
    }

    @Test
    void shouldUpdateTargetAllocationsForAuthenticatedUser() throws Exception {
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("ISK", "60.00"),
                allocation("KF", "40.00"));
        List<TargetAllocationResponseDTO> response = List.of(
                new TargetAllocationResponseDTO(10L, "ISK", new BigDecimal("60.00")),
                new TargetAllocationResponseDTO(11L, "KF", new BigDecimal("40.00")));

        when(targetAllocationService.updateTargetAllocationsForAuthenticatedUser(USER_EMAIL, request))
                .thenReturn(response);

        mockMvc.perform(put("/api/target-allocations")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountType").value("ISK"))
                .andExpect(jsonPath("$[1].accountType").value("KF"));

        verify(targetAllocationService)
                .updateTargetAllocationsForAuthenticatedUser(USER_EMAIL, request);
    }

    @Test
    void shouldReturnBadRequestWhenAllocationsAreEmpty() throws Exception {
        UpdateTargetAllocationsRequestDTO request = new UpdateTargetAllocationsRequestDTO(List.of());

        mockMvc.perform(put("/api/target-allocations")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(targetAllocationService);
    }

    @Test
    void shouldReturnBadRequestWhenPercentageIsOutsideValidRange() throws Exception {
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("ISK", "101.00"));

        mockMvc.perform(put("/api/target-allocations")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(targetAllocationService);
    }

    @Test
    void shouldReturnBadRequestWhenServiceRejectsTotalPercentage() throws Exception {
        UpdateTargetAllocationsRequestDTO request = request(
                allocation("ISK", "60.00"),
                allocation("KF", "30.00"));
        when(targetAllocationService.updateTargetAllocationsForAuthenticatedUser(
                org.mockito.ArgumentMatchers.eq(USER_EMAIL),
                any(UpdateTargetAllocationsRequestDTO.class)))
                .thenThrow(new InvalidTargetAllocationException(
                        "Target allocation percentages must total 100"));

        mockMvc.perform(put("/api/target-allocations")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private TargetAllocationRequestDTO allocation(String accountType, String percentage) {
        return new TargetAllocationRequestDTO(accountType, new BigDecimal(percentage));
    }

    private UpdateTargetAllocationsRequestDTO request(TargetAllocationRequestDTO... allocations) {
        return new UpdateTargetAllocationsRequestDTO(List.of(allocations));
    }
}
