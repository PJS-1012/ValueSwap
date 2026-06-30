package com.valueswap.matching;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "valueswap.matching", name = "scheduler-enabled", havingValue = "true")
public class MatchingScheduler {
    private final MatchingService matchingService;

    public MatchingScheduler(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @Scheduled(fixedDelayString = "${valueswap.matching.interval-ms:300000}")
    public void discoverMatches() {
        matchingService.runMatching();
    }
}
