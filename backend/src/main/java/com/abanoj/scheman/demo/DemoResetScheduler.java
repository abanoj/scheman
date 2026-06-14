package com.abanoj.scheman.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DemoResetScheduler {

    private final DemoDataService demoDataService;

    @Value("${application.demo.enabled:false}")
    private boolean enabled;

    @Scheduled(cron = "${application.demo.reset-cron:0 0 */6 * * *}")
    public void reset() {
        if (!enabled) return;
        log.info("Demo scheduler: starting periodic reset...");
        demoDataService.reset();
    }
}
