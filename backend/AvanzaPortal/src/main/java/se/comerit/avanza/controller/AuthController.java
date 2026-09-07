package se.comerit.avanza.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.comerit.avanza.entity.User;
import se.comerit.avanza.service.AuthService;

/**
 * Controller responsible for handling authentication-related HTTP requests.
 *
 * The controller handles login and logout requests and manages the user
 * session.
 * Authentication and password verification are handled by AuthService.
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    /**
     * Creates an AuthController with the required AuthService.
     *
     * @param authService service responsible for authentication logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Attempts to authenticate a user using their email and password.
     *
     * Authentication is delegated to AuthService.
     * If authentication succeeds, the user's information is stored
     * in the HTTP session.
     *
     * @param email    the email address entered by the user
     * @param password the password entered by the user
     * @param session  the current HTTP session
     * @param model    the model used to display login errors
     * @return a redirect to the dashboard on success or the login page on failure
     */
    @PostMapping("/auth/login")
    public String doLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        User user = authService.authenticate(email, password);

        if (user == null) {
            model.addAttribute("error", "Fel e-post eller lösenord.");
            return "login";
        }

        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("tenantId", user.getId());

        return "redirect:/";
    }

    /**
     * Logs out the current user.
     *
     * The HTTP session is invalidated so that the user is no longer logged in.
     *
     * @param session the current HTTP session
     * @return a redirect to the login page
     */
    @DeleteMapping("/auth/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/auth/login";
    }
}