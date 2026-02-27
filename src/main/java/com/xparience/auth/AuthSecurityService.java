package com.xparience.auth;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthSecurityService {

    private static final int RATE_LIMIT_ATTEMPTS = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(15);
    private static final int CAPTCHA_THRESHOLD = 3;
    private static final int LOCKOUT_THRESHOLD = 10;
    private static final Duration LOCKOUT_DURATION = Duration.ofHours(24);

    private final Map<String, AttemptState> attemptsByIp = new ConcurrentHashMap<>();

    public void preCheck(String ipAddress, String captchaToken) {
        AttemptState state = attemptsByIp.computeIfAbsent(ipAddress, key -> new AttemptState());
        prune(state);

        if (state.lockedUntil != null && LocalDateTime.now().isBefore(state.lockedUntil)) {
            long retryAfterSeconds = Duration.between(LocalDateTime.now(), state.lockedUntil).toSeconds();
            throw new RuntimeException("Account temporarily locked due to repeated failed attempts. Retry in " + Math.max(1, retryAfterSeconds) + " seconds.");
        }

        int failedInWindow = failedInWindow(state, RATE_LIMIT_WINDOW);
        if (failedInWindow >= RATE_LIMIT_ATTEMPTS) {
            LocalDateTime earliest = state.failedAt.peekFirst();
            long retryAfterSeconds = earliest == null
                    ? RATE_LIMIT_WINDOW.toSeconds()
                    : Duration.between(LocalDateTime.now(), earliest.plus(RATE_LIMIT_WINDOW)).toSeconds();
            throw new RuntimeException("Too many attempts. Retry in " + Math.max(1, retryAfterSeconds) + " seconds.");
        }

        if (failedInWindow >= CAPTCHA_THRESHOLD && (captchaToken == null || captchaToken.isBlank())) {
            throw new RuntimeException("CAPTCHA is required after multiple failed attempts");
        }
    }

    public void recordFailure(String ipAddress) {
        AttemptState state = attemptsByIp.computeIfAbsent(ipAddress, key -> new AttemptState());
        prune(state);

        state.failedAt.addLast(LocalDateTime.now());
        int failedLast24h = failedInWindow(state, Duration.ofHours(24));
        if (failedLast24h >= LOCKOUT_THRESHOLD) {
            state.lockedUntil = LocalDateTime.now().plus(LOCKOUT_DURATION);
        }
    }

    public void recordSuccess(String ipAddress) {
        AttemptState state = attemptsByIp.computeIfAbsent(ipAddress, key -> new AttemptState());
        state.failedAt.clear();
        state.lockedUntil = null;
    }

    private int failedInWindow(AttemptState state, Duration window) {
        LocalDateTime cutoff = LocalDateTime.now().minus(window);
        int count = 0;
        for (LocalDateTime failedAt : state.failedAt) {
            if (failedAt.isAfter(cutoff)) {
                count++;
            }
        }
        return count;
    }

    private void prune(AttemptState state) {
        LocalDateTime cutoff = LocalDateTime.now().minus(Duration.ofHours(24));
        while (!state.failedAt.isEmpty() && state.failedAt.peekFirst().isBefore(cutoff)) {
            state.failedAt.pollFirst();
        }

        if (state.lockedUntil != null && LocalDateTime.now().isAfter(state.lockedUntil)) {
            state.lockedUntil = null;
        }
    }

    private static final class AttemptState {
        private final Deque<LocalDateTime> failedAt = new ArrayDeque<>();
        private LocalDateTime lockedUntil;
    }
}
