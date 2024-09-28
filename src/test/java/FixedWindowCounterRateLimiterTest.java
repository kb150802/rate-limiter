import RateLimiter.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FixedWindowCounterRateLimiterTest {
    RateLimiter rateLimiter;
    @BeforeEach
    public void setup(){
        rateLimiter = new FixedWindowCounterRateLimiter(1,3);
    }

    @Test
    public void threadSafetyTest() throws InterruptedException {
        int threadCount = 100;
        TimeSource timeSource = new TimeSource(0L);
        RateLimiter mockedRateLimiter = new FixedWindowCounterRateLimiter(1,3,timeSource);
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
        assertTrue(allowedCount.get() <= 3, "Allowed more than accepted Requests " + allowedCount.get());
    }
}
