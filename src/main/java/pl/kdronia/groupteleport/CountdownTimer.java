package pl.kdronia.groupteleport;

import java.time.Duration;
import java.time.Instant;

public class CountdownTimer {

    private final Instant startTime;
    private final Duration totalDuration;
    private long lastDisplayedSecond;

    public CountdownTimer(int countdownSeconds) {
        this.startTime = Instant.now();
        this.totalDuration = Duration.ofSeconds(countdownSeconds);
        this.lastDisplayedSecond = -1;
    }

    public boolean isFinished() {
        return this.getElapsed().compareTo(this.totalDuration) >= 0;
    }

    public long getRemainingSeconds() {
        Duration remaining = this.totalDuration.minus(this.getElapsed());
        long seconds = remaining.getSeconds();

        if (remaining.toMillis() % 1000 != 0 && seconds > 0) {
            return seconds;
        }

        return Math.max(seconds, 1);
    }

    public boolean shouldDisplay() {
        long secondsLeft = this.getRemainingSeconds();
        if (secondsLeft == this.lastDisplayedSecond) {
            return false;
        }
        this.lastDisplayedSecond = secondsLeft;
        return true;
    }

    private Duration getElapsed() {
        return Duration.between(this.startTime, Instant.now());
    }

}