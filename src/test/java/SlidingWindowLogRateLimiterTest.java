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
        rateLimiter = new SlidingWindowLogRateLimiter(10,5);
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
        int threadCount = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger allowedCount = new AtomicInteger(0);
        Runnable task = ()->{
            if(rateLimiter.allowRequest()) {
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
