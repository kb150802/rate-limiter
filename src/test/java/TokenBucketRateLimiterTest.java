import RateLimiter.RateLimiter;
import RateLimiter.TokenBucketRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

public class TokenBucketRateLimiterTest {
    private TokenBucketRateLimiter rateLimiter;
    @BeforeEach
    public void setUp() {
        rateLimiter = new TokenBucketRateLimiter(5, 1); // 5 tokens, 1 token per second
    }
    @Test
    public void testWithinLimit() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.allowRequest());
        }
    }
    @Test
    public void testExceedLimit() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.allowRequest());
        }
        assertFalse(rateLimiter.allowRequest()); // 6th request should be denied
    }

    @Test
    public void testTokenRefillAfterTime() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.allowRequest());
        }
        Thread.sleep(2000);
        assertTrue(rateLimiter.allowRequest());
        assertTrue(rateLimiter.allowRequest());
    }
    @Test
    public void testOverflowTokenHandling() throws InterruptedException {

        Thread.sleep(10000);
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.allowRequest()); // All 5 tokens should be used
        }
        assertFalse(rateLimiter.allowRequest());
    }
    @Test
    public void testTokenConsumptionConsistency() throws InterruptedException {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(3, 1);
        for (int i = 0; i < 3; i++) {
            assertTrue(rateLimiter.allowRequest());
            Thread.sleep(1000);
        }
        assertTrue(rateLimiter.allowRequest());
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        int threadCount = 10;
        TimeSource timeSource = new TimeSource(0L);
        RateLimiter mockedRateLimiter = new TokenBucketRateLimiter(5,1,timeSource);

        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger allowedCount = new AtomicInteger(0);

        Runnable task = () -> {
            if (mockedRateLimiter.allowRequest()) {
                allowedCount.incrementAndGet();
            }
            latch.countDown();
        };

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(task);
        }

        latch.await();
        executorService.shutdown();
        assertTrue(allowedCount.get() <= 5, "Allowed requests exceeded bucket capacity");
    }


}
