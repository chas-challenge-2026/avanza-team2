package se.comerit.avanza.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for rebalance operations
 * RebalanceController
 */
@RestController
@RequestMapping("api/rebalance")
public class RebalanceController {

    /**
     * Apply rebalance operation
     * 
     * @param entity
     * @return
     */
    @PostMapping("/apply")
    public String applyRebalance(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

    /**
     * Preview rebalance operation
     * 
     * @param param
     * @return
     */
    @GetMapping("/preview")
    public String previewRebalance(@RequestParam String param) {
        // TODO: process GET request
        return new String();
    }

}
