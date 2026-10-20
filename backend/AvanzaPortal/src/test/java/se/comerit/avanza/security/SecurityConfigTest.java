package se.comerit.avanza.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import se.comerit.avanza.controller.AuthController;
import se.comerit.avanza.service.AuthService;
import jakarta.servlet.http.Cookie;

import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void login_isPublic() throws Exception {
        when(authService.authenticate(any())).thenReturn(
                new se.comerit.avanza.dto.auth.LoginResponseDTO(
                        "test-token", "Test User", "test@example.com"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"test@example.com","password":"password"}
                        """))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie",
                        containsString("jwt=test-token")));
    }

    @Test
    void logout_isPublic() throws Exception {
        mockMvc.perform(delete("/api/auth/logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    void protectedApi_withoutJwt_isRejected() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedApi_withValidJwt_isAllowed() throws Exception {
        when(jwtUtil.extractUsername("test-token")).thenReturn("test@example.com");
        when(jwtUtil.validateToken("test-token", "test@example.com")).thenReturn(true);

        mockMvc.perform(get("/api/auth/me")
                .cookie(new Cookie("jwt", "test-token")))
                .andExpect(status().isNoContent());
    }
}
