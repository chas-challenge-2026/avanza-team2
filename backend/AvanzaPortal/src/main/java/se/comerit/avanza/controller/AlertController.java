package se.comerit.avanza.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.comerit.avanza.service.AlertService;

/**
 * import javax.servlet.http.HttpSession; 
 * This is the import for the older version of HttpSession, 
 * but we are currently using jakarta.servlet.http.HttpSession instead.
 */
import jakarta.servlet.http.HttpSession;

/**
 * GET /api/alerts?dismissed=false
 * PUT /api/alerts/{id}/dismiss
 * 
 * AlertController
 */
@RestController
@RequestMapping("/api")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/alerts?dismissed=false")
    public String listAlerts(
            HttpSession session,
            Model model) {

        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        Long userId = ((Number) session.getAttribute("userId")).longValue();

        model.addAttribute(
                "userName",
                session.getAttribute("userName"));

        model.addAttribute(
                "storedAlerts",
                alertService.getStoredAlerts(userId));

        model.addAttribute(
                "liveAlerts",
                alertService.generateLiveDriftAlerts(userId));

        model.addAttribute(
                "driftThreshold",
                alertService.getDriftThreshold());

        return "alerts";
    }

    @PostMapping("/alerts/{id}/dismiss")
    public String dismissAlert(
            @RequestParam Long alertId,
            HttpSession session) {

        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        alertService.dismissAlert(alertId);

        return "redirect:/alerts";
    }
}