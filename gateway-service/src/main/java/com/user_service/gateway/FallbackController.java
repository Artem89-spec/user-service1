    package com.user_service.gateway;

    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RestController;

    @RestController
    public class FallbackController {
        @GetMapping("/fallback/users")
        public ResponseEntity<String> fallbackUsers() {
            return ResponseEntity.status(503).body("Сервис пользователей временно недоступен. Попробуйте позже.");
        }
    }
