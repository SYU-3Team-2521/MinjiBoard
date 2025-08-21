package syu.likealion3.hackathon.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String ok() {
        return "OK";
    }

    @PersistenceContext
    private EntityManager em;

    @GetMapping("/health/db")
    public ResponseEntity<String> db() {
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            return ResponseEntity.ok("DB_OK");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("DB_ERROR: " + e.getMessage());
        }
    }
}
