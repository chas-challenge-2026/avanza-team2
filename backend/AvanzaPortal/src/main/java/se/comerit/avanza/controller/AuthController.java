package se.comerit.avanza.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.comerit.avanza.entity.User;
import se.comerit.avanza.service.AuthService;

/**
 * Controller responsible for handling authentication-related HTTP requests.
 *
 * The controller handles login and logout requests and manages the user session.
 * Authentication and password verification are handled by AuthService.
 */
@Controller
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
     * Displays the login page.
     *
     * If the user is already logged in, they are redirected to the dashboard.
     *
     * @param session the current HTTP session
     * @return the login view or a redirect to the dashboard
     */
    @GetMapping("/login")
    public String loginPage(HttpSession session) {

        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }

        return "login";
    }

    /**
     * Attempts to authenticate a user using their email and password.
     *
     * Authentication is delegated to AuthService.
     * If authentication succeeds, the user's information is stored
     * in the HTTP session.
     *
     * @param email the email address entered by the user
     * @param password the password entered by the user
     * @param session the current HTTP session
     * @param model the model used to display login errors
     * @return a redirect to the dashboard on success or the login page on failure
     */
    @PostMapping("/login")
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
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }
}