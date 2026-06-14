package com.abanoj.scheman.demo;

import com.abanoj.scheman.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DemoDataInitializer {

    private final DemoDataService demoDataService;
    private final UserRepository userRepository;

    @Value("${application.demo.enabled:false}")
    private boolean enabled;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (!enabled) return;
        if (!userRepository.existsByEmail(DemoDataService.DEMO_ADMIN_EMAIL)) {
            log.info("Demo mode: no demo data found, seeding initial dataset...");
            demoDataService.reset();
        } else {
            log.info("Demo mode: demo data already present, skipping initial seed.");
        }
    }
}
