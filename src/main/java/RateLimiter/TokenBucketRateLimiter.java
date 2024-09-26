package RateLimiter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketRateLimiter implements RateLimiter {
    private final Integer capacity;
    private final Integer fillRate;
    private final AtomicLong tokens;
    private final AtomicLong lastLookupTime;

    public TokenBucketRateLimiter(Integer capacity, Integer fillRate) {
        this.capacity = capacity;
        this.fillRate = fillRate;
        this.tokens = new AtomicLong(capacity);
        this.lastLookupTime = new AtomicLong(System.nanoTime());
    }

    @Override
    public boolean allowRequest() {
        updateTokenCount();

        if (tokens.get() > 0) {
            return tokens.decrementAndGet() >= 0;
        }
        return false;

    }
    private void updateTokenCount() {
        long currentTime = System.nanoTime();
        long timeSinceLastRefill = currentTime -  lastLookupTime.get();
        long tokensToAdd = (timeSinceLastRefill * fillRate) / TimeUnit.SECONDS.toNanos(1);
        tokens.updateAndGet(currentTokens -> Math.min(currentTokens + tokensToAdd, capacity));
        lastLookupTime.set(currentTime);
    }
}
