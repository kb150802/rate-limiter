import RateLimiter.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SlidingWindowLogRateLimiterTest {
    RateLimiter rateLimiter;
    @BeforeEach
    public void setup() {
        rateLimiter = new SlidingWindowLogRateLimiter(1,5);
    }
    @Test
    public void testWithinLimit() {
        for(int i = 0; i < 5; ++ i) {
            assertTrue(rateLimiter.allowRequest());
        }
    }
    @Test
    public void testExceedLimit() {
        for(int i = 0;i < 5; ++ i) {
            assertTrue(rateLimiter.allowRequest());
        }
        assertFalse(rateLimiter.allowRequest());
    }
    @Test
    public void testThreadSafety() throws InterruptedException {
        int threadCount = 100;
        TimeSource timeSource = new TimeSource(0L);
        RateLimiter mockedRateLimiter = new SlidingWindowLogRateLimiter(1,5, timeSource);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger allowedCount = new AtomicInteger(0);
        Runnable task = ()->{
            if(mockedRateLimiter.allowRequest()) {
                allowedCount.incrementAndGet();
            }
            latch.countDown();
        };
        for(int i = 0; i < threadCount; ++i) {
            executorService.submit(task);
        }
        latch.await();
        executorService.shutdown();
        assertTrue(allowedCount.get() <= 5, "Allowed more than expected requests " + allowedCount.get() );
    }
}
