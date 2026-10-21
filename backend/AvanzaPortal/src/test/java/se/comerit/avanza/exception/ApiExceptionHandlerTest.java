package se.comerit.avanza.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class ApiExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returnsUnauthorizedProblemWithoutLeakingCredentialDetails() throws Exception {
        mockMvc.perform(get("/errors/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Invalid username or password."));
    }

    @Test
    void returnsForbiddenProblemWithoutLeakingAuthorizationDetails() throws Exception {
        mockMvc.perform(get("/errors/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.detail").value("You are not allowed to perform this action."));
    }

    @Test
    void returnsServiceUnavailableWhenFxDataIsMissing() throws Exception {
        mockMvc.perform(get("/errors/fx-unavailable"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Service Unavailable"))
                .andExpect(jsonPath("$.detail").value("No FX rate data is currently available"));
    }

    @Test
    void returnsGenericProblemForUnexpectedExceptions() throws Exception {
        mockMvc.perform(get("/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."));
    }

    @RestController
    static class TestController {

        @GetMapping("/errors/fx-unavailable")
        void fxUnavailable() {
            throw new FxDataUnavailableException("No FX rate data is currently available");
        }

        @GetMapping("/errors/bad-credentials")
        void badCredentials() {
            throw new BadCredentialsException("sensitive credential detail");
        }

        @GetMapping("/errors/access-denied")
        void accessDenied() {
            throw new AccessDeniedException("sensitive authorization detail");
        }

        @GetMapping("/errors/unexpected")
        void unexpected() {
            throw new IllegalStateException("internal implementation detail");
        }
    }
}
